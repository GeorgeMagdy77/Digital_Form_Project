// list all folders in croatia-components/jenkins/containers that will be buildable Jenkins agent container images
def containers = ['base', 'infrastructure', 'microservices', 'mobile']

// create an individual build job for each agent
for (container in containers) {
  pipelineJob("agents/${container}") {
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
    environmentVariables {
      env('IMAGE_TYPE', container)
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
              url("ssh://git@bitbucket.nrapp.cloud/dig/croatia-components.git")
            }
            extensions {
              wipeOutWorkspace()
            }
          }
        }
        scriptPath('jenkins/containers/Jenkinsfile')
      }
    }
  }
}
