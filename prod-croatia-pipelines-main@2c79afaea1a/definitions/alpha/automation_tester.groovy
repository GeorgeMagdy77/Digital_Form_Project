/*
a scheduled job to run automation tests against environments
*/
// define any and all in scope environments within here. These must match the names of the configurations in the testing repository
def envs = ['prod']

// create separate jobs for each environment
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
           	  headRegexFilter {                 
                regex("release/\\d+_.+")  
              }              
            }
          }
        }
      }
    }
  // TODO - re-enable triggers once tactical is decomissioned
  // triggers {
  //   BitbucketWebhookMultibranchTrigger {
  //     refTrigger(true)
  //     pullRequestTrigger(false)
  //     }
  //   }
  }
}
