import com.croatia.tactical.pipelines.helpers.BitbucketPaginatedAPICall

def projects = []
def repositories = BitbucketPaginatedAPICall.get('https://bitbucket.projectcroatia.cloud/rest/api/latest/projects/tdev/repos', "Bearer $BITBUCKET_TOKEN")
// At the moment(08.06.2023) there is only one repository, but this loop is left for future cases
for (repository in repositories) {
  def labels = BitbucketPaginatedAPICall.get("https://bitbucket.projectcroatia.cloud/rest/api/latest/projects/tdev/repos/${repository.slug}/labels", "Bearer $BITBUCKET_TOKEN")
  for (label in labels) {
    // check if the "transact" label is present
    if (label && label.name && label.name == 'transact') {
      projects << repository.slug
      break
    }
  }
}

// generate all in scope jobs for each located repository
for (project in projects) {
  // validate will run against all branches, should be triggered automatically by webhooks
  multibranchPipelineJob("transact/validate/${project}") {
    branchSources {
      branchSource {
        source {
          BbS {
            id(project)
            credentialsId('jenkins-git')
            sshCredentialsId('opc-master')
            projectName('TDEV')
            repositoryName(project)
            serverId('croatia')
            mirrorName('')
            traits {
              gitBranchDiscovery()
              gitTagDiscovery()
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
	
  pipelineJob("transact/deploy-development/${project}") {
    parameters {
      gitParameter {
        name('BRANCH')
        type('PT_BRANCH_TAG')
        defaultValue('origin/deploy')
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
	  choiceParam('TRANSACT_ENVIRONMENT', ['development'])
	}
    environmentVariables {
      env('PIPELINE_TYPE', 'deploy')
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
			  url("ssh://git@bitbucket.projectcroatia.cloud/tdev/${project}.git")
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
