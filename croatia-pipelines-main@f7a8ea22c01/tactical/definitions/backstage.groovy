pipelineJob("backstage") {
  displayName('Build & Publish Backstage')
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
            url("ssh://git@bitbucket.projectcroatia.cloud/dig/croatia-backstage.git")
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
