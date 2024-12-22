def call(Map config) {
  def pipelineType = env.PIPELINE_TYPE ? env.PIPELINE_TYPE : 'build'

  switch(pipelineType) {
  case 'build':
    pipeline {
      agent { 
        label 'macos'
      }
      environment {
        API_BASE_URL = 'api-dev.apps.dev-dmz.projectcroatia.cloud'
        API_TOKEN = credentials('api-token-dev')
        APPSFLYER_DEV_KEY = credentials ('appsflyer-key-dev')
        APPSFLYER_IOS_APP_KEY = credentials ('appsflyer-key-ios-dev')
        MEAWALLET_USERNAME = credentials('meawallet-username')
        MEAWALLET_PASSWORD = credentials('meawallet-password')
        BUILD_ENVIRONMENT = "test"
      }
      stages {
        stage('Setup') {
          steps {
            script {
              BRANCH_REF = GIT_BRANCH.replace('origin/', '').replace('\\', '-').replace('/', '-').replace('_', '-')
              SHORT_COMMIT = GIT_COMMIT.substring(0, 6)
              VERSION_PREFIX = "${BRANCH_REF}-${SHORT_COMMIT}"
            }
            sh '''
            npm config set @meawallet:registry=https://nexus.ext.meawallet.com/repository/react-native-mpp/
            npm config set //nexus.ext.meawallet.com/repository/react-native-mpp/:username $MEAWALLET_USERNAME
            npm config set //nexus.ext.meawallet.com/repository/react-native-mpp/:_password $MEAWALLET_PASSWORD
            bundle install
            yarn install
            '''
          }
        }
        stage('Test') {
          steps {
            sh 'yarn test'
          }
        }
        stage('Build') {
          steps {
            sh 'bundle exec fastlane ios build "env:$BUILD_ENVIRONMENT"'
          }
        }
        stage('SonarQube') {
          when {
            expression {
              if (config && config.skipSonar) {
                return false
              }
              return true
            }
          }
          steps {
            withSonarQubeEnv(installationName: 'croatia', credentialsId: 'sonar') {
              sh '''
              echo 'sonar.projectKey=croatia-mobile-app' > sonar-project.properties
              echo 'sonar.exclusions=**/*.java,android/**,ios/**' >> sonar-project.properties
              sonar-scanner
              '''
            }
          }
        }
        stage("Quality gate") {
          steps {
            waitForQualityGate abortPipeline: true
          }
        }
      }
      post {
        always {
          catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            archiveArtifacts artifacts: 'build/reports/**/*', followSymlinks: false
            publishHTML allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'build/reports/tests/test', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: '', useWrapperFileDirectly: true
          }
        }
      }
    }
  break
  case 'release':
    pipeline {
      agent { 
        label 'macos'
      }
      environment {
        API_BASE_URL = 'api-dev.apps.dev-dmz.projectcroatia.cloud'
        API_TOKEN = credentials('api-token-dev')
        APPSFLYER_DEV_KEY = credentials ('appsflyer-key-dev')
        APPSFLYER_IOS_APP_KEY = credentials ('appsflyer-key-ios-dev')
        IOS_APPCENTER_API_TOKEN = credentials('ios-appcenter-api-token')
        MEAWALLET_USERNAME = credentials('meawallet-username')
        MEAWALLET_PASSWORD = credentials('meawallet-password')
        BUILD_ENVIRONMENT = "${params.BUILD_ENVIRONMENT}"
        OPC_MASTER = credentials('opc-master')
      }
      stages {
        stage('Setup') {
          steps {
            script {
              BRANCH_REF = GIT_BRANCH.replace('origin/', '').replace('\\', '-').replace('/', '-').replace('_', '-')
              SHORT_COMMIT = GIT_COMMIT.substring(0, 6)
              VERSION_PREFIX = "${BRANCH_REF}-${SHORT_COMMIT}"
              currentBuild.displayName = "${params.BUILD_ENVIRONMENT}-$VERSION_PREFIX"
            }
            sh '''
            npm config set @meawallet:registry=https://nexus.ext.meawallet.com/repository/react-native-mpp/
            npm config set //nexus.ext.meawallet.com/repository/react-native-mpp/:username $MEAWALLET_USERNAME
            npm config set //nexus.ext.meawallet.com/repository/react-native-mpp/:_password $MEAWALLET_PASSWORD
            bundle install
            yarn install
            '''
            sh '''
            mkdir -p ~/.ssh
            cp "$OPC_MASTER" ~/.ssh/opc-master
            git config --global init.defaultBranch main
            git init
            git config --local core.sshCommand "/usr/bin/ssh -i ~/.ssh/opc-master"
            git config --local user.name "Jenkins"
            git config --local user.email "jenkins@projectcroatia.cloud"
            git fetch
            '''
            script {
              PREV_BUILD_NUMBER = 1
              PREV_BUILD_NUMBER_STR = sh(script: 'node scripts/source-build-number.js', returnStdout: true).trim()
              if (PREV_BUILD_NUMBER_STR) {
                PREV_BUILD_NUMBER = PREV_BUILD_NUMBER_STR.isInteger() ? PREV_BUILD_NUMBER_STR as Integer : null
                if (!PREV_BUILD_NUMBER) {
                  error("previous build number found: $PREV_BUILD_NUMBER_STR was not an integer")
                }
              }
              BUILD_NUMBER = PREV_BUILD_NUMBER++
            }
          }
        }
        stage('Build') {
          environment {
            BUILD_NUMBER = "$BUILD_NUMBER"
          }
          steps {
            sh '''
            node scripts/update-build-info.js
            bundle exec fastlane ios update_version
            bundle exec fastlane ios build "env:$BUILD_ENVIRONMENT"
            bundle exec fastlane tag_build_git
            '''
          }
        }
        stage('Publish') {
          environment {
            BUILD_NUMBER = "$BUILD_NUMBER"
          }
          steps {
            sh 'bundle exec fastlane ios deploy_appcenter "env:$BUILD_ENVIRONMENT"'
          }
        }
      }
      post {
        always {
          catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            archiveArtifacts artifacts: 'build/reports/**/*', followSymlinks: false
            publishHTML allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: "build/reports/tests/$BUILD_ENVIRONMENT", reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: '', useWrapperFileDirectly: true
          }
        }
      }
    }
  break
  }
}
