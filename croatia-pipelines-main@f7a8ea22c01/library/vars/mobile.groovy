/**
* mobile is a function orchestrating the android & ios builds, and pushing a git tag on release
*/

/**
* call is the primary entrypoint for Jenkins
*/
def call(Map config) {
  
  properties([disableConcurrentBuilds(abortPrevious: true)]) 
  
  pipeline {
    agent {
      kubernetes {
        inheritFrom 'mobile'
      }
    }
    options {
      buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
    }
    // TODO - add more environments
    environment {
      BUILD_ENVIRONMENT = "test"
      // This is a hack because the BUILD_ID of the parent (`mobile`) task is lower than that of the child tasks (`android`) at time of writing
      // so existing builds already have a higher build number
      BUILD_ID = "${Integer.parseInt(BUILD_ID) + 3000}"
      PUBLISH = "${GIT_BRANCH == 'origin/main' || GIT_BRANCH == 'main'}"
    }
    stages {
      stage('Build') {
        steps {
//          build job: "/mobile/ios", parameters: [
//            string(name: 'BRANCH', value: "${GIT_BRANCH}"),
//            string(name: 'BUILD_ID', value: "${BUILD_ID}"),
//            string(name: 'BUILD_ENVIRONMENT', value: "${BUILD_ENVIRONMENT}"),
//            booleanParam(name: 'PUBLISH', value: "${PUBLISH}")
//          ]

          build job: "/mobile/android", parameters: [
            string(name: 'BRANCH', value: "${GIT_BRANCH}"),
            string(name: 'BUILD_ID', value: "${BUILD_ID}"),
            string(name: 'BUILD_ENVIRONMENT', value: "${BUILD_ENVIRONMENT}"),
            booleanParam(name: 'PUBLISH', value: "${PUBLISH}")
          ]
        }
      }
    }
  }
}
