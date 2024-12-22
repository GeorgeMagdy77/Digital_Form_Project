def envs = ['dev', 'eit']

for (environment in envs) {
  multibranchPipelineJob("alpha/tester/${environment}") {
    branchSources {
      branchSource {
        source {
          BbS {
            id("tester")
            credentialsId('jenkins-git')
            sshCredentialsId('opc-master')
            projectName('Alpha')
            repositoryName("alpha-automation-testing")
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
}