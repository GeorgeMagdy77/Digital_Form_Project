/*
this file covers the generation of all kustomize repositories

the jobs it creates, are the 'validate' and 'release'

the repositories it adds jobs for are dynamically loaded, using a call to the Bitbucket API, and looking for any repositories that have the label 'kustomize' on them
*/
import com.croatia.pipelines.helpers.BitbucketPaginatedAPICall

def kustomizeRepositories = []
def repositories = BitbucketPaginatedAPICall.get('https://bitbucket.nrapp.cloud/rest/api/latest/projects/dig/repos', "Bearer $BITBUCKET_TOKEN")
for (repository in repositories) {
  def labels = BitbucketPaginatedAPICall.get("https://bitbucket.nrapp.cloud/rest/api/latest/projects/dig/repos/${repository.slug}/labels", "Bearer $BITBUCKET_TOKEN")
  for (label in labels) {
    if (label && label.name && label.name == 'kustomize-prod') {
      kustomizeRepositories << repository.slug
      break
    }
  }
}

for (repository in kustomizeRepositories) {
  multibranchPipelineJob("kustomize/validate/$repository") {
    branchSources {
      branchSource {
        source {
          BbS {
            id(repository)
            credentialsId('jenkins-git')
            sshCredentialsId('opc-master')
            projectName('dig')
            repositoryName(repository)
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

  pipelineJob("kustomize/release/$repository") {
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
              url("ssh://git@bitbucket.nrapp.cloud/dig/${repository}.git")
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
