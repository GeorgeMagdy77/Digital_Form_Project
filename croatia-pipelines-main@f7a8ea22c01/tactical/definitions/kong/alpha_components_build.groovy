def projects = [
  'alpha-kong-configuration'
]

for (project in projects) {
  multibranchPipelineJob("alpha/${project}") {
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
              headWildcardFilter {
                includes('main feature/*')
                excludes('task/* bugfix/* hotfix/*')
              }
            }
          }
        }
      }
    }
//    triggers {
//      BitbucketWebhookMultibranchTrigger {
//        refTrigger(true)
//        pullRequestTrigger(false)
//      }
//    }
  }
}
