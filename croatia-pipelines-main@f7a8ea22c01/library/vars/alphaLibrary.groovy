/**
* alphaLibary is a function holding full pipelines for validation and release of a java code library
* any implementing pipeline should have at least one environment varaible defined
* this variable is 'PIPELINE_TYPE', and will determine what type of pipeline will be executed
* the allowed options for this variable are the following
* - build: used for a validation/build pipelne
* - release: used for a release pipeline to generate an image
* @param: BRANCH a git branch to run the release against
*/

/**
* call is the primary entrypoint for Jenkins
* @param config is a map holiding configuration options from the implementing pipeline/Jenkinsfile
* config options
* - docker: a bool to specify if a docker in docker container should be attached
* - skipSonar: a bool to toogle to true if sonar steps should be skipped entirely 
*/
def call(Map config) {
  // capture the pipeline type via an environmnt variable
  // default to the build/validation pipeline if not set
  def pipelineType = env.PIPELINE_TYPE ? env.PIPELINE_TYPE : 'build'
  // define pod yaml for docker in docker declaratively, not used by every pipeline
  def dockerYaml = '''
spec:
  tolerations:
  - effect: NoSchedule
    key: service
    operator: Equal
    value: jenkins-agents
  containers:
    - name: jnlp
      volumeMounts:
      - mountPath: /var/run/
        name: dind-socket
    - name: docker
      image: docker:dind-rootless
      securityContext: 
        privileged: true
      command: 
      - dockerd-entrypoint.sh
      tty: true
      volumeMounts:
      - mountPath: /var/run/
        name: dind-socket
  volumes:
    - name: dind-socket
      emptyDir: {}
  '''
  // define pod yaml for the deploy agent pod
  def podYaml = config.docker ? dockerYaml : ''

  // the core switch which will set what type of pipeline will be executed
  switch(pipelineType) {
    // the whole pipeline for build & validation
    case 'build':
      pipeline {
        // use the podYaml spec for the agent to enable docker in docker if needed
        agent {
          kubernetes {
            inheritFrom 'microservices'
            yaml "$podYaml"
          }
        }
        options {
          buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
        }
        // globally change the test containers behaviour to make sure to load images from artifactory
        environment {
          TESTCONTAINERS_RYUK_DISABLED = 'true'
          TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX = 'artifactory.apps.dev-nonpci-shared.npnbank.local/docker/'
        }
        stages {
          // perform a gradle build
          stage('Build') {
            steps {
              sh '''
              sed -i 's,https\\\\://services.gradle.org/distributions/,https://nexus.apps.dev-nonpci-shared.npnbank.local/repository/gradle-packages/,g' gradle/wrapper/gradle-wrapper.properties
              ./gradlew clean build $GRADLE_BUILD_OPTIONS
              '''
            }
          }
          // run sonarqube via gradle
          //stage('SonarQube') {
          //  when {
          //    not {
          //      expression { return config.skipSonar }
          //    }
          //  }
          //  steps {
          //    withSonarQubeEnv(installationName: 'croatia', credentialsId: 'sonarnew') {
          //      sh './gradlew sonarqube'
          //    }
          //  }
          //}
          //// check the quality gate in jenkins, and fail pipeline on a failing gate
          //stage("Quality gate") {
          //  steps {
          //    waitForQualityGate abortPipeline: true
          //  }
          //}
        }
        // attempt to upload all test results from previous steps, will never fail the build on failure
        post {
          always {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
              archiveArtifacts artifacts: 'build/reports/**/*', followSymlinks: false
              publishHTML allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'build/reports/tests/test', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: '', useWrapperFileDirectly: true
            }
          }
        }
      }
    break;
    // the whole pipeline for release, so building and publishing a jar for the library
    case 'release':
      pipeline {
        agent {
          kubernetes {
            inheritFrom 'microservices'
            yaml "$podYaml"
          }
        }
        // load credentials for artifactory
        environment {
          ARTIFACTORY = credentials('artifactory')
          NEXUS_USR = credentials('nexus')
          NEXUS_PSW = credentials('nexus-docker')
        }
        // build the code, set the location for the publish, then publish the built jar via gradle
        stages {
          stage('Build') {
            steps {
              sh '''
              sed -i 's,https\\://artifactory.apps.dev-nonpci-shared.npnbank.local/artifactory/libs-release-local/,https://nexus.apps.dev-nonpci-shared.npnbank.local/repository/libs-release-local/,g' build.gradle
              sed -i 's,https\\\\://services.gradle.org/distributions/,https://nexus.apps.dev-nonpci-shared.npnbank.local/repository/gradle-packages/,g' gradle/wrapper/gradle-wrapper.properties
              ./gradlew clean build $GRADLE_BUILD_OPTIONS
              echo '\nnexusUrl=https://nexus.apps.dev-nonpci-shared.npnbank.local/repository' >> gradle.properties
              echo '\nnexusRepository=libs-release-local' >> gradle.properties
              echo "nexusUsername=${NEXUS_USR}" >> gradle.properties
              echo "nexusPassword=${NEXUS_PSW}" >> gradle.properties
              ./gradlew publish
              '''
            }
          }
        }
      }
    break;
  }
}