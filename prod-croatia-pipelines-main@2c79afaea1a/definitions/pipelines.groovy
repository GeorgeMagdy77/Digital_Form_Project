multibranchPipelineJob('prod-croatia-pipelines') {
  branchSources {
    branchSource {
      source {
        BbS {
          id('prod-croatia-pipelines')
          credentialsId('jenkins-git')
          sshCredentialsId('opc-master')
          projectName('dig')
          repositoryName('prod-croatia-pipelines')
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
