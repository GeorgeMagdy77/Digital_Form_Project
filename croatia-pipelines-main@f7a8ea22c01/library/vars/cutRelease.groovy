/**
* cutRelease is a pipeline to generate release branches for all alpha microservices in bitbbucket
* it has a number of behaviour flags which change the nature of how it runs, depending on the requirements
*
* @param: RUN_ALL bool to determine if a branch should be cut for all microservices in bitbucket, defaults to false
* @param: DEFAULT_VERSION string for the default git version to use when cutting a branch, if not specified in REPOSITORY_VERSIONS
* @param: RELEASE_TYPE string of either 'release' or 'hotfix', which act as the branch prefix
* @param: RELEASE string of the name of the release generated
* @param: REPOSITORY_VERSIONS string in a multiline format, matching the form of ${repository name}:${repository version}. Overrides the 
* DEFAULT_VERSION parameter. If RUN_ALL isn't set, only repositories specified here have branches generated
*/

import com.croatia.pipelines.helpers.BitbucketPaginatedAPICall

/**
* call is the primary entrypoint for Jenkins
*/
def call() {
  pipeline {
    agent {
      kubernetes {
        inheritFrom 'infrastructure'
      }
    }
    // load git credentials
    environment {
      OPC_MASTER = credentials('opc-master')
      BITBUCKET_TOKEN = credentials('jenkins-git')
    }
    stages {
      // setup git credentials
      stage('Setup') {
        steps {
          script {
            currentBuild.displayName = "${params.RELEASE_TYPE}/${params.RELEASE}"
          }
          sh '''
          mkdir -p ~/.ssh
          cp "\$OPC_MASTER" ~/.ssh/opc-master
          '''
        }
      }
      stage('Cut') {
        steps {
          script {
            // load the repos and versions in REPOSITORY_VERSIONS into a map
            def versionedRepos = [:]
            for (repoVersion in params.REPOSITORY_VERSIONS.split('\n')) {
              def splitRepoVersion = repoVersion.split(':')
              if (splitRepoVersion.size() < 2) {
                versionedRepos[splitRepoVersion[0]] = params.DEFAULT_VERSION
              } else {
                versionedRepos[splitRepoVersion[0]] = splitRepoVersion[1]
              }
            }

            // if RUN_ALL is on, then find all repositories with the 'microservice' label, and push to a list
            def repos = []
            if (params.RUN_ALL) {
              def unfilteredRepos = BitbucketPaginatedAPICall.get('https://bitbucket.nrapp.cloud/rest/api/latest/projects/alpha/repos', "Bearer $BITBUCKET_TOKEN_PSW")
              for (repo in unfilteredRepos) {
                def labels = BitbucketPaginatedAPICall.get("https://bitbucket.nrapp.cloud/rest/api/latest/projects/alpha/repos/${repo.slug}/labels", "Bearer $BITBUCKET_TOKEN_PSW")
                for (label in labels) {
                  if (label && label.name && label.name == 'microservice') {
                    repos << repo.slug
                    break
                  }
                }
              }
            // otherwise, only use the repos from REPOSITORY_VERSIONS as the list
            } else {
              versionedRepos.each{
                key, value -> repos << key
              }
            }

            // loop throgh the list loaded from bitbucket or from REPOSITORY_VERSIONS
            for (repo in repos) {
              // assuming that bitbucket was called, check to see if the version of the repository did have an explicit override from REPOSITORY_VERSIONS
              // if it does, set that as the version, otherside, default to using DEFAULT_VERSION
              def version = params.DEFAULT_VERSION
              if (versionedRepos.containsKey(repo)) {
                version = versionedRepos[repo]
              }
              if (version.split('/').size() > 1) {
                version = "${repo}/${version}"
              }
              // checkout the repository locally, from the main branch, the version will be used later
              checkout scm: BbS(branches: [[name: "*/main"]], credentialsId: 'jenkins-git', extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: repo]], id: repo, mirrorName: '', projectName: 'Alpha', repositoryName: repo, serverId: 'croatia', sshCredentialsId: 'opc-master')
              // set git credentials, and then attempt to create a branch from the specified (or loaded from bitbucket) version directly
              // once done, push the new or updated branch to bitbucket
              sh """
              cd $repo
              git config --local core.sshCommand "/usr/bin/ssh -i ~/.ssh/opc-master"
              git config --local user.name "Jenkins"
              git config --local user.email "jenkins@projectcroatia.cloud"
              git fetch --all --tags
              set +e
              FULLCOMMIT=\$(git rev-parse $version)
              if [ \$? -ne 0 ]; then
                FULLCOMMIT=\$(git rev-parse HEAD)
              fi
              set -e
              git checkout -b ${params.RELEASE_TYPE}/${params.RELEASE} \$FULLCOMMIT
              git push -f --set-upstream $repo ${params.RELEASE_TYPE}/${params.RELEASE}
              cd ..
              """
            }
          }
        }
      }
    }
  }
}
