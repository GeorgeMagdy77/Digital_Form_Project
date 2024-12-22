/**
* androidBuild is a function holding full pipelines for building and publishing an android app
* @param: BRANCH a git branch to run the job against
* @param: BUILD_ENVIRONMENT a string name of the build environment
* @param: BUILD_ID build ID of the parent (`mobile`) pipeline so we have consistent IDs for all child tasks
* @param: PUBLISH a bool to state if the output should be published to appcenter
*/

/**
* call is the primary entrypoint for Jenkins
* @param config is a map holiding configuration options from the implementing pipeline/Jenkinsfile
* config options
* - skipSonar: a bool to toogle to true if sonar steps should be skipped entirely 
*/
def call(Map config) {
  pipeline {
    // use the mobile agent built specifically for android
    agent {
      kubernetes {
        inheritFrom 'mobile'
      }
    }
    /** 
    * set environment variables relevant to the requested build
    * - ANDROID_KEYSTORE:           The location of a keystore holding required certificates, loaded from jenkins
    * - API_BASE_URL:               Base URL for the Croatia apis
    * - API_TOKEN:                  The base token for interacting with the api
    * - APPSFLYER_DEV_KEY:          Development key for appsflyer
    * - APPSFLYER_IOS_APP_KEY:      Development key for appsflyer IOS (kept for compatability)
    * - ANDROID_KEYSTORE_PASSWORD:  Required password for the loaded keystore
    * - MEAWALLET_USERNAME:         Username for meawallet nexus to resolve the meawallet dependency
    * - MEAWALLET_PASSWORD:         Password for meawallet nexus to resolve the meawallet dependency
    * - BUILD_ENVIRONMENT:          The build environment, should match the fastlane config file name
    * - BUILD_ID:                   Build ID of parent pipeline
    * - IS_CI_ANDROID               If this build is a pipeline build for Android, set this to "true", otherwise don't set.
    * - INSTANA_APP_KEY:            Development key for IBM Instana SDK
    * - INSTANA_REPORTING_URL:      Reporting URL for Instana SDK
    */
    environment {
      ANDROID_KEYSTORE = credentials('android-keystore')
      API_BASE_URL = 'api.apps.prod-dmz-ingress.nrapp.cloud'
      API_TOKEN = credentials('api-token-prod')
      APPSFLYER_DEV_KEY = credentials ('appsflyer-key-prod')
      APPSFLYER_IOS_APP_KEY = credentials ('appsflyer-key-ios-prod')
      ANDROID_KEYSTORE_PASSWORD = credentials('android-keystore-password')
      MEAWALLET_USERNAME = credentials('meawallet-username')
      MEAWALLET_PASSWORD = credentials('meawallet-password')
      BUILD_ENVIRONMENT = "${params.BUILD_ENVIRONMENT}"
      BUILD_ID = "${params.BUILD_ID}"
      IS_CI_ANDROID = "true"
      INSTANA_APP_KEY = '6CRvLTC4RMKfmJKd6HiSrQ'
      INSTANA_REPORTING_URL = 'https://ibm-instana-poc.projectcroatia.cloud:446/eum/mobile'
    }
    stages {
      stage('Enable Bitbucket access') {
        environment {
          OPC_MASTER = credentials('opc-master')
        }
        steps {
          script {
            currentBuild.displayName = "${params.BUILD_ENVIRONMENT}-${params.BUILD_ID}"
          }
          sh '''
          mkdir -p ~/.ssh
          cp "$OPC_MASTER" ~/.ssh/opc-master
          git config --local core.sshCommand "/usr/bin/ssh -i ~/.ssh/opc-master"
          git config --local user.name "Jenkins"
          git config --local user.email jenkins@projectcroatia.cloud
          '''
        }
      }
      // capture version information, and setup npm & keystores ready for the build
      stage('Setup') {
        steps {
          sh '''
          npm config set @meawallet:registry=https://nexus.ext.meawallet.com/repository/react-native-mpp/
          npm config set //nexus.ext.meawallet.com/repository/react-native-mpp/:username $MEAWALLET_USERNAME
          npm config set //nexus.ext.meawallet.com/repository/react-native-mpp/:_password $MEAWALLET_PASSWORD
          mv $ANDROID_KEYSTORE ./android/keystores/test.keystore
          bundle install
          yarn install
          '''
        }
      }
      // run any unit tests
      stage('Test') {
        steps {
          sh 'yarn test'
        }
      }
      // run ESLint on all changed files
      stage('ESLint') {
        steps {
          sh '''
          DIFFED_FILES_TO_LINT=$(git diff origin/main... --name-only -- "*.ts" "*.tsx" "*.js" "*.jsx")
          yarn lint:ci --no-error-on-unmatched-pattern $DIFFED_FILES_TO_LINT
          '''
        }
      }
      // compiles all javascript code into the release bundle. running this step instead of "Build" saves
      // a significant amount of time since all Java compilation is skipped
      stage('Build dry run') {
        when {
          expression {
            return !params.PUBLISH
          }
        }
        steps {
          sh 'echo "Dry-run build for environment $BUILD_ENVIRONMENT with build ID $BUILD_ID"'
          sh 'yarn react-native bundle --platform android --dev false --minify false --entry-file index.js --bundle-output index.android.bundle'
        }
      }
      // update all applicable versioning information, then trigger a build of the app. this step generates a
      // complete binary that can be used to install on a device
      stage('Build') {
        when {
          expression {
            return params.PUBLISH
          }
        }
        steps {
          sh 'echo "Building for environment $BUILD_ENVIRONMENT with build ID $BUILD_ID"'

          sh '''
          node scripts/update-build-info.js
          bundle exec fastlane android update_version
          bundle exec fastlane android build "env:$BUILD_ENVIRONMENT"
          '''
        }
      }
      // run sonarqube and upload the results
      stage('SonarQube') {
        steps {
          withSonarQubeEnv(installationName: 'croatia', credentialsId: 'sonar') {
            sh 'echo "sonar.branch.name=$GIT_BRANCH" >> sonar-project.properties'
            sh 'echo "sonar.projectKey=croatia-mobile-app" >> sonar-project.properties'
            sh 'echo "sonar.exclusions=**/*.java,android/**,ios/**" >> sonar-project.properties'
            sh 'sonar-scanner'
          }
        }
      }
      // check there's a passing quality gate against the build
      stage("Quality gate") {
        when {
          expression {
            return params.PUBLISH
          }
        }
        steps {
          waitForQualityGate abortPipeline: true
        }
      }
      // if toggled, publish to appcenter
      stage('Publish') {
        when {
          expression {
            return params.PUBLISH
          }
        }
        // get the appcenter deployment token from jenkins
        environment {
          ANDROID_APPCENTER_API_TOKEN = credentials('android-appcenter-api-token')
        }
        steps {
          sh 'bundle exec fastlane android deploy_appcenter "env:$BUILD_ENVIRONMENT"'
          sh 'bundle exec fastlane android tag_build_git'
          sh 'bundle exec fastlane android upload_browserstack "env:$BUILD_ENVIRONMENT"'
        }
      }
    }
    // try to upload test report files to jenkins
    post {
      always {
        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
          archiveArtifacts artifacts: 'build/reports/**/*', followSymlinks: false
          publishHTML allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'build/reports/tests/test', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: '', useWrapperFileDirectly: true
        }
      }
    }
  }
}
