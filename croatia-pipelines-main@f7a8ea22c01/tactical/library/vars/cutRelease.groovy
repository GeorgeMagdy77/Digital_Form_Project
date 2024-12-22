import com.croatia.pipelines.helpers.BitbucketPaginatedAPICall

def call() {
  pipeline {
    agent {
      kubernetes {
        inheritFrom 'infrastructure'
      }
    }
    environment {
      OPC_MASTER = credentials('opc-master')
      BITBUCKET_TOKEN = credentials('jenkins-git')
    }
    stages {
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
            def versionedRepos = [:]
            for (repoVersion in params.REPOSITORY_VERSIONS.split('\n')) {
              def splitRepoVersion = repoVersion.split(':')
              if (splitRepoVersion.size() < 2) {
                continue
              }
              versionedRepos[splitRepoVersion[0]] = splitRepoVersion[1]
            }

            def repos = []
            if (params.RUN_ALL) {
              def unfilteredRepos = BitbucketPaginatedAPICall.get('https://bitbucket.projectcroatia.cloud/rest/api/latest/projects/alpha/repos', "Bearer $BITBUCKET_TOKEN_PSW")
              for (repo in unfilteredRepos) {
                def labels = BitbucketPaginatedAPICall.get("https://bitbucket.projectcroatia.cloud/rest/api/latest/projects/alpha/repos/${repo.slug}/labels", "Bearer $BITBUCKET_TOKEN_PSW")
                for (label in labels) {
                  if (label && label.name && label.name == 'microservice') {
                    repos << repo.slug
                    break
                  }
                }
              }
            } else {
              versionedRepos.each{
                key, value -> repos << key
              }
            }

            for (repo in repos) {
              def version = params.DEFAULT_VERSION
              if (versionedRepos.containsKey(repo)) {
                version = versionedRepos[repo]
              }
              if (version.split('/').size() > 1) {
                version = "${repo}/${version}"
              }
              checkout scm: BbS(branches: [[name: "*/main"]], credentialsId: 'jenkins-git', extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: repo]], id: repo, mirrorName: '', projectName: 'Alpha', repositoryName: repo, serverId: 'croatia', sshCredentialsId: 'opc-master')
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
