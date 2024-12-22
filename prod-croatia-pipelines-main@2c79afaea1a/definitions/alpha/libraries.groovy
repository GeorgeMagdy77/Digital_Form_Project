/*
this file covers the generation of all alpha library pipeline jobs

the jobs it creates, are the 'validate' and 'release' jobs

the repositories it adds jobs for are dynamically loaded, using a call to the Bitbucket API, and looking for any repositories that have the label 'library' on them
*/
import com.croatia.pipelines.helpers.BitbucketPaginatedAPICall

// load a list of repositories from bitbucket, and generate jobs for them if they match the label
def projects = []
def repositories = BitbucketPaginatedAPICall.get('https://bitbucket.nrapp.cloud/rest/api/latest/projects/alpha/repos', "Bearer $BITBUCKET_TOKEN")
for (repository in repositories) {
  def labels = BitbucketPaginatedAPICall.get("https://bitbucket.nrapp.cloud/rest/api/latest/projects/alpha/repos/${repository.slug}/labels", "Bearer $BITBUCKET_TOKEN")
  for (label in labels) {
    // check the library label is present
    if (label && label.name && label.name == 'library') {
      projects << repository.slug
      break
    }
  }
}

for (project in projects) {
  // validate will run against all branches, should be triggered automatically by webhooks
  multibranchPipelineJob("alpha/validate/${project}") {
    branchSources {
      branchSource {
        source {
          BbS {
            id(project)
            credentialsId('jenkins-git')
            sshCredentialsId('opc-master')
            projectName('Alpha')
            repositoryName(project)
            serverId('croatia')
            mirrorName('')
            traits {
              gitBranchDiscovery()
              gitTagDiscovery()
              pruneStaleBranchTrait()
              pruneStaleBranchTrait()
            }
          }
        }
      }
    }
    triggers {
      BitbucketWebhookMultibranchTrigger {
        refTrigger(true)
        pullRequestTrigger(false)
      }
    }
  }

  // release jobs can be run against main & release branches. Run manually, or by an associated change via validate
  pipelineJob("alpha/release/${project}") {
    parameters {
      gitParameter {
        name('BRANCH')
        type('Branch')
        defaultValue('origin/main')
        branchFilter('.*')
        listSize('0')
        requiredParameter(true)
        branch('')
        tagFilter('.*')
        sortMode('ASCENDING_SMART')
        selectedValue('DEFAULT')
        useRepository('')
        quickFilterEnabled(false)
      }
    }
    environmentVariables {
      env('PIPELINE_TYPE', 'release')
    }
    definition {
      cpsScm {
        lightweight(false)
        scm {
          git {
            branch('${BRANCH}')
            remote {
              credentials('opc-master')
              name('origin')
              url("ssh://git@bitbucket.nrapp.cloud/alpha/${project}.git")
            }
            extensions {
              wipeOutWorkspace()
            }
          }
        }
        scriptPath('Jenkinsfile')
      }
    }
  }
}
