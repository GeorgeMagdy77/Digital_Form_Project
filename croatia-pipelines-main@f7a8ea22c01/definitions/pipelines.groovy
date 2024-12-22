multibranchPipelineJob('croatia-pipelines') {
  branchSources {
    branchSource {
      source {
        BbS {
          id('croatia-pipelines')
          credentialsId('jenkins-git')
          sshCredentialsId('opc-master')
          projectName('dig')
          repositoryName('croatia-pipelines')
          serverId('croatia')
          mirrorName('')
          traits {
            gitBranchDiscovery()
            //gitTagDiscovery()
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
