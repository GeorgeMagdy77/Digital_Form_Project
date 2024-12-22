/*
orchestration job to handle the running of the android and ios child jobs

changes it's behaviour to run a publish & tagging when on the main branch
*/
multibranchPipelineJob('mobile/mobile') {
  branchSources {
    branchSource {
      source {
        BbS {
          id('croatia-mobile-app')
          credentialsId('jenkins-git')
          sshCredentialsId('opc-master')
          projectName('mob')
          repositoryName('croatia-mobile-app')
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