/**
* alphaAdapter is a function holding full pipelines for validation, release, and deploy of alpha microservices
* any implementing pipeline should have at least one environment varaible defined
* this variable is 'PIPELINE_TYPE', and will determine what type of pipeline will be executed
* the allowed options for this variable are the following
* - build: used for a validation/build pipelne
* - release: used for a release pipeline to generate an image
* @param: BRANCH a git branch to run the release against
* - deploy: used for a deployment pipeline
* @param: BRANCH a git branch to run the release against
* @param: CROATIA_ENVIRONMENT the target environment to deploy to
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
  // the core switch which will set what type of pipeline will be executed
  switch(pipelineType) {
    // the whole pipeline for build & validation
    case 'build':
      pipeline {
        // use the podYaml spec for the agent to enable docker in docker if needed
        agent {
          label 'prod-vm-agent'
        }
        options {
          buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
        }
        // globally change the test containers behaviour to make sure to load images from artifactory
        environment {
          TESTCONTAINERS_RYUK_DISABLED = 'true'
          TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX = 'artifactory.apps.prod-nonpci-shared.nrapp.cloud/docker/'
        }
        stages {
          // perform a gradle build
          stage('Build') {
            steps {
              sh '''
              sed -i 's,https\\\\://services.gradle.org/distributions/,https://nexus.apps.prod-nonpci-shared.nrapp.cloud/repository/gradle-packages/,g' gradle/wrapper/gradle-wrapper.properties
              sed -i 's,https://artifactory.apps.dev-nonpci-shared.npnbank.local/artifactory/libs-release-local/,https://nexus.apps.prod-nonpci-shared.nrapp.cloud/repository/libs-release-local/,g' build.gradle
              ./gradlew clean build $GRADLE_BUILD_OPTIONS --parallel
              '''
            }
          }
          // generate an allure report to upload to jenkins
          stage('Generate Allure Results') {
            steps {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                sh "./gradlew allureReport"
              }
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
          //    withSonarQubeEnv(installationName: 'croatia', credentialsId: 'sonar') {
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
          // perform a dependency check on the java code
          stage ('OWASP Dependency-Check Vulnerabilities') {
            steps {
              dependencyCheck additionalArguments: ''' 
                    -s "./"
                    -f "HTML" 
                    -f "XML"
                    --prettyPrint''', odcInstallation: 'OWASP-Dependency-Check'

              dependencyCheckPublisher failedNewCritical: 15, failedNewHigh: 50, failedNewLow: 100, failedNewMedium: 100, failedTotalCritical: 15, failedTotalHigh: 50, failedTotalLow: 100, failedTotalMedium: 100, pattern: 'dependency-check-report.xml', stopBuild: false, unstableNewCritical: 15, unstableNewHigh: 50, unstableNewLow: 100, unstableNewMedium: 100, unstableTotalCritical: 15, unstableTotalHigh: 50, unstableTotalLow: 100, unstableTotalMedium: 100
            }
          }
          // if the branch is release/prod , then trigger a downstream release job
          stage('Release') {
            when {
              expression {
                if (GIT_BRANCH.contains('release/')) {
                  return true
                }
                return false
              }
            }
            steps {
              script {
                def jobPaths = JOB_NAME.split('/')
                SPLIT_NAME = jobPaths[jobPaths.size()-2]
                build job: "/alpha/release/$SPLIT_NAME", wait: false, parameters: [string(name: 'BRANCH', value: "${GIT_BRANCH}")]
              }
            }
          }
        }
        // attempt to upload all allure and test results from previous steps, will never fail the build on failure
        post {
          always {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
              archiveArtifacts artifacts: 'build/reports/**/*', followSymlinks: false
              publishHTML allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'build/reports/tests/test', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: '', useWrapperFileDirectly: true
            }
            allure([
              includeProperties: false,
              jdk: '',
              properties: [],
              reportBuildPolicy: 'ALWAYS',
              results: [[path: 'target/allure-results']]
            ])
          }
        }
      }
    break;
    // the whole pipeline for release, so building and publishing a container image
    case 'release':
      pipeline {
        agent none
        // load credentials for artifactory
        environment {
          ARTIFACTORY = credentials('artifactory')
          DOCKER_TOKEN = credentials('artifactory-docker')
          NEXUS_USR = credentials('nexus')
          NEXUS_PASSWORD = credentials('nexus-docker')
          NEXUS_REPO = 'nexus-connector.apps.prod-nonpci-shared.nrapp.cloud'
          NEXUS_DR_REPO = 'nexus-connector.apps.dr-nonpci-appdb.nrapp.cloud'
        }
        stages {
          // capture and setup a versioning string for the image
          stage('Setup') {
            agent {
              label 'prod-vm-agent'
            }
            steps {
              script {
                def jobPaths = JOB_NAME.split('/')
                NAME = jobPaths[jobPaths.size()-1]
                IMAGE_TAG = GIT_BRANCH.replace('origin/', '').replace('\\', '-').replace('/', '-').replace('_', '-').toLowerCase()
                SHORT_COMMIT = GIT_COMMIT.substring(0, 8)
                currentBuild.displayName = "${IMAGE_TAG}-${SHORT_COMMIT}"
              }
            }
          }
          // build the code
          stage('Build') {
            agent {
              label 'prod-vm-agent'
            }
            steps {
              sh '''
              git tag -d $(git tag -l)
              sed -i 's,https\\\\://services.gradle.org/distributions/,https://nexus.apps.prod-nonpci-shared.nrapp.cloud/repository/gradle-packages/,g' gradle/wrapper/gradle-wrapper.properties
              sed -i 's,https://artifactory.apps.dev-nonpci-shared.npnbank.local/artifactory/libs-release-local/,https://nexus.apps.prod-nonpci-shared.nrapp.cloud/repository/libs-release-local/,g' build.gradle
              ./gradlew clean build $GRADLE_BUILD_OPTIONS --parallel
              '''
              stash includes: 'build/**/*', name: 'build'
            }
          }   
          // run buildah to generate the container image, and push to artifactory 
          stage('Package') {
            agent {
              label 'prod-vm-agent'
            }
            environment {
              NAME = "$NAME"
              IMAGE_TAG = "$IMAGE_TAG"
              SHORT_COMMIT = "$SHORT_COMMIT"
            }
            steps {
              unstash 'build'
              sh '''
              docker login -u "$NEXUS_USR" -p "$NEXUS_PASSWORD" $NEXUS_REPO
              docker build -t "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}" .
              docker tag "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}" "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
              docker push "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}"
              docker push "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
              docker login -u "$NEXUS_USR" -p "$NEXUS_PASSWORD" $NEXUS_DR_REPO
              docker tag "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}" "${NEXUS_DR_REPO}/${NAME}:${IMAGE_TAG}"
              docker tag "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}" "${NEXUS_DR_REPO}/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
              docker push "${NEXUS_DR_REPO}/${NAME}:${IMAGE_TAG}"
              docker push "${NEXUS_DR_REPO}/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
              '''
            }
          }
        }
      }
    break;
  }
}
// edit done
