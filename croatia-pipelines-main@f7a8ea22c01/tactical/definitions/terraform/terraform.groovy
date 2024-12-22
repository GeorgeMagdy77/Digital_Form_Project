def repos = [
  'pc-platform-bootstrap' : [
    environment : false,
    cluster : false
  ],
  'pc-platform-networking' : [
    environment : false,
    cluster : false
  ],
  'pc-platform-sharedservices' : [
    environment : true,
    cluster : true
  ],
  'pc-platform' : [
    environment : true,
    cluster : false
  ],
  'pc-platform-components' : [
    environment : true,
    cluster : true
  ],
]

def environments = ['development', 'sandbox']

def environmentScript = '''
switch(ENVIRONMENT) {
  case ~/.*development.*/:
    return ['development', 'dmz', 'cit', 'cit-dmz']
  case ~/.*sandbox.*/:
    return ['sandbox', 'kong']
}
'''

for (repo in repos) {
  multibranchPipelineJob("terraform/validate/${repo.key}") {
    branchSources {
      branchSource {
        source {
          BbS {
            id(repo.key)
            credentialsId('jenkins-git')
            sshCredentialsId('opc-master')
            projectName('dig')
            repositoryName(repo.key)
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

  pipelineJob("terraform/deploy/${repo.key}") {
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
      if (repo.value.environment) {
        choiceParam('ENVIRONMENT', environments)
        activeChoiceReactiveParam('CLUSTER') {
          choiceType('SINGLE_SELECT')
          filterable()
          groovyScript {
            script(environmentScript)
          }
          referencedParameter('ENVIRONMENT')
        }
      }
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
              url("ssh://git@bitbucket.projectcroatia.cloud/dig/${repo.key}.git")
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
