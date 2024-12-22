/*
runs a validate and publish for the android application

not designed to be run manually, and is expected to be called via the main 'mobile' pipeline
*/
pipelineJob('mobile/android') {
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
    choiceParam('BUILD_ENVIRONMENT', ['test','dev', 'eit', 'prod'])
    stringParam('BUILD_NUMBER', '1')
    booleanParam('PUBLISH')
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
            url("ssh://git@bitbucket.projectcroatia.cloud/MOB/croatia-mobile-app.git")
          }
          extensions {
            wipeOutWorkspace()
          }
        }
      }
      scriptPath('android/Jenkinsfile')
    }
  }
}
