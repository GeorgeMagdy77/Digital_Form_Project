/**
* iosBuild is a function holding full pipelines for building and publishing an ios app
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
    // use a static macos agent attached to jenkins
    agent {
      label 'macos'
    }
    /** 
    * set environment varibles relevant to the requested build
    * - API_BASE_URL:               Base URL for the Croatia apis
    * - API_TOKEN:                  The base token for interacting with the api
    * - APPSFLYER_DEV_KEY:          Development key for appsflyer
    * - APPSFLYER_IOS_APP_KEY:      Development key for appsflyer IOS (kept for compatability)
    * - MEAWALLET_USERNAME:         Username for meawallet nexus to resolve the meawallet dependency
    * - MEAWALLET_PASSWORD:         Password for meawallet nexus to resolve the meawallet dependency
    * - BUILD_ENVIRONMENT:          The build environment, should match the fastlane config file name
    * - BUILD_NUMBER:               The build number, should be passed in already incremented when publishing
    * - INSTANA_APP_KEY:            Development key for IBM Instana SDK
    * - INSTANA_REPORTING_URL:      Reporting URL for Instana SDK
    */
    environment {
      API_BASE_URL = 'api.apps.dev-dmz-ingress.npnbank.local'
      API_TOKEN = credentials('api-token-dev')
      APPSFLYER_DEV_KEY = credentials ('appsflyer-key-prod')
      APPSFLYER_IOS_APP_KEY = credentials ('appsflyer-key-ios-prod')
      MEAWALLET_USERNAME = credentials('meawallet-username')
      MEAWALLET_PASSWORD = credentials('meawallet-password')
      BUILD_ENVIRONMENT = "${params.BUILD_ENVIRONMENT}"
      BUILD_ID = "${params.BUILD_ID}"
      IS_CI_ANDROID = "false"
      INSTANA_APP_KEY = '6CRvLTC4RMKfmJKd6HiSrQ'
      INSTANA_REPORTING_URL = 'https://ibm-instana-poc.projectcroatia.cloud:446/eum/mobile'

      PATH = "/Users/almb/.rvm/rubies/ruby-2.7.8/bin:/Users/almb/.rvm/bin:/Users/almb/.sdkman/candidates/java/current/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/Library/Apple/usr/bin"
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
          
          if [ ! -f ~/.ssh/opc-master ] || [ -w ~/.ssh/opc-master ]
          then
            cp "$OPC_MASTER" ~/.ssh/opc-master
            git config --local core.sshCommand "/usr/bin/ssh -i ~/.ssh/opc-master"
            git config --local user.name "Jenkins"
            git config --local user.email jenkins@projectcroatia.cloud
          fi
          '''
        }
      }
      stage('Use correct Ruby version') {
        steps {
          sh '''
          ruby -v
          which ruby
          rm -rf ~/.npm

          curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.5/install.sh | bash

          export NVM_DIR="$HOME/.nvm"

          if [ -s "$NVM_DIR/nvm.sh" ]; then
          chmod +x "$NVM_DIR/nvm.sh"
          "$NVM_DIR/nvm.sh" | bash
          fi

          nvm list
          nvm install 18.14.0
          node -v

          nvm install --lts
          nvm use --lts


          which node
          node -v
          

          npm install --global yarn
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
          sh 'yarn react-native bundle --platform ios --dev false --minify false --entry-file index.js --bundle-output index.ios.bundle'
        }
      }
      // update all applicable versioning information, then trigger a build of the app
      stage('Build') {
        when {
          expression {
            return params.PUBLISH
          }
        }
        steps {
          withEnv(["PATH+EXTRA=/usr/bin/agvtool"]) {
            sh 'echo "Building for environment $BUILD_ENVIRONMENT with build ID $BUILD_ID"'

            sh '''
            node scripts/update-build-info.js
            bundle exec fastlane ios update_version
            bundle exec fastlane ios build "env:$BUILD_ENVIRONMENT"
            '''
          }
        }
      }
      // SKIP SonarQube, it is also ran in the Android build already
      // if toggled, publish to appcenter
      stage('Publish') {
        when {
          expression {
            return params.PUBLISH
          }
        }
        // get the appcenter deployment token from jenkins
        environment {
          IOS_APPCENTER_API_TOKEN = credentials('ios-appcenter-api-token')
        }
        steps {
          sh 'bundle exec fastlane ios deploy_appcenter "env:$BUILD_ENVIRONMENT"'
          sh 'bundle exec fastlane ios tag_build_git'
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
}
