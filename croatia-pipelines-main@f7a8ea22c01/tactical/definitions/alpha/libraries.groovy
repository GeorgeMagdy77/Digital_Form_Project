import com.croatia.tactical.pipelines.helpers.BitbucketPaginatedAPICall

def projects = []
def repositories = BitbucketPaginatedAPICall.get('https://bitbucket.projectcroatia.cloud/rest/api/latest/projects/alpha/repos', "Bearer $BITBUCKET_TOKEN")
for (repository in repositories) {
  def labels = BitbucketPaginatedAPICall.get("https://bitbucket.projectcroatia.cloud/rest/api/latest/projects/alpha/repos/${repository.slug}/labels", "Bearer $BITBUCKET_TOKEN")
  for (label in labels) {
    if (label && label.name && label.name == 'library') {
      projects << repository.slug
      break
    }
  }
}

for (project in projects) {
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
              url("ssh://git@bitbucket.projectcroatia.cloud/alpha/${project}.git")
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
