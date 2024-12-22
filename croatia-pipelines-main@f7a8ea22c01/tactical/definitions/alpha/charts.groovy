pipelineJob('alpha/alpha-microservice-chart') {
  definition {
    cpsScm {
      scm {
        BbS {
          id('alpha-microservice-chart')
          credentialsId('jenkins-git')
          sshCredentialsId('opc-master')
          projectName('Alpha')
          repositoryName('alpha-microservice-chart')
          serverId('croatia')
          mirrorName('')
          gitTool('')
          branches {
            branchSpec {
              name('main')
            }
          }
        }
      }
    }
  }
  triggers {
    BitbucketWebhookTriggerImpl {
      refTrigger(true)
      pullRequestTrigger(false)
    }
  }
}
