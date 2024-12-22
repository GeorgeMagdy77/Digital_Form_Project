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
  def dockerYaml = '''
spec:
  tolerations:
  - effect: NoSchedule
    key: service
    operator: Equal
    value: jenkins-agents
  containers:
    - name: jnlp
      imagePullPolicy: Always
      resources: 
        limits:
          memory: 3Gi
        requests:
          memory: 2Gi
      volumeMounts:
      - mountPath: /var/run/
        name: dind-socket
    - name: docker
      image: docker:dind-rootless
      securityContext: 
        privileged: true
      resources: 
        limits:
          memory: 3Gi
        requests:
          memory: 2Gi
      command: 
      - dockerd-entrypoint.sh
      - "--insecure-registry=artifactory.apps.dev-nonpci-shared.npnbank.local"
      tty: true
      volumeMounts:
      - mountPath: /var/run/
        name: dind-socket
  volumes:
    - name: dind-socket
      emptyDir: {}
  '''
  // define pod yaml for the deploy agent pod
  def deployYaml = '''
spec:
  tolerations:
  - effect: NoSchedule
    key: service
    operator: Equal
    value: jenkins-agents
  containers:
    - name: jnlp
      securityContext: 
        privileged: true
        runAsUser: 0

  '''
  // set the yaml used by the build pod based on if docker has been requested or not
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
              sed -i 's,https\\://artifactory.apps.dev-nonpci-shared.npnbank.local/artifactory/libs-release-local/,https://nexus.apps.dev-nonpci-shared.npnbank.local/repository/libs-release-local/,g' build.gradle
              ./gradlew clean build $GRADLE_BUILD_OPTIONS
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
          // perform a dependency check on the java code
          stage ('OWASP Dependency-Check Vulnerabilities') {
            when {
              branch 'main'
            }
            steps {
              dependencyCheck additionalArguments: ''' 
                    -s "./"
                    -f "HTML" 
                    -f "XML"
                    --prettyPrint''', odcInstallation: 'OWASP-Dependency-Check'

              dependencyCheckPublisher failedNewCritical: 15, failedNewHigh: 50, failedNewLow: 100, failedNewMedium: 100, failedTotalCritical: 15, failedTotalHigh: 50, failedTotalLow: 100, failedTotalMedium: 100, pattern: 'dependency-check-report.xml', stopBuild: false, unstableNewCritical: 15, unstableNewHigh: 50, unstableNewLow: 100, unstableNewMedium: 100, unstableTotalCritical: 15, unstableTotalHigh: 50, unstableTotalLow: 100, unstableTotalMedium: 100
            }
          }
          // if the branch is main or release, then trigger a downstream release job
          stage('Release') {
            when {
              expression {
                if (GIT_BRANCH.contains('release/') || GIT_BRANCH == 'main' || GIT_BRANCH == 'origin/main') {
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
          NEXUS_REPO= 'nexus-connector.apps.dev-nonpci-shared.npnbank.local'
        }
        stages {
          // capture and setup a versioning string for the image
          stage('Setup') {
            agent {
              kubernetes {
                inheritFrom 'microservices'
              }
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
              kubernetes {
                inheritFrom 'microservices'
                yaml "$podYaml"
              }
            }
            steps {
              sh '''
              git tag -d $(git tag -l)
              sed -i 's,https\\\\://services.gradle.org/distributions/,https://nexus.apps.dev-nonpci-shared.npnbank.local/repository/gradle-packages/,g' gradle/wrapper/gradle-wrapper.properties
              sed -i 's,https\\://artifactory.apps.dev-nonpci-shared.npnbank.local/artifactory/libs-release-local/,https://nexus.apps.dev-nonpci-shared.npnbank.local/repository/libs-release-local/,g' build.gradle
              ./gradlew clean build -x test $GRADLE_BUILD_OPTIONS
              '''
              stash includes: 'build/**/*', name: 'build'
            }
          }   
          // run buildah to generate the container image, and push to artifactory 
          stage('Package') {
            agent {
              kubernetes {
                inheritFrom 'microservices'
                yaml "$deployYaml"
              }
            }
            environment {
              NAME = "$NAME"
              IMAGE_TAG = "$IMAGE_TAG"
              SHORT_COMMIT = "$SHORT_COMMIT"
            }
            steps {
              unstash 'build'
              sh '''
              buildah login -u "$NEXUS_USR" -p "$NEXUS_PASSWORD" $NEXUS_REPO
              buildah bud -t "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}" .
              buildah tag "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}" "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
              buildah push "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}"
              buildah push "${NEXUS_REPO}/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
              '''
            }
          }
          // when on main, trigger a downstream deployment to development environment to keep it in line with HEAD
          stage('Deploy Development') {
            when {
              expression {
                if (params.BRANCH == 'main' || params.BRANCH == 'origin/main') {
                  return true
                }
                return false
              }
            }
            steps {
              script {
                build job: "/alpha/deploy-development/$NAME", parameters: [string(name: 'BRANCH', value: params.BRANCH)]
              }                
            }
          }
        }
      }
    break;
    // whole pipeline for deploying to development
    case 'deploy':
      pipeline {
        agent {
          kubernetes {
            inheritFrom 'infrastructure'
          }
        }
        // load credentials for the shared s ervices cluster
        environment {
          SHARED_TOKEN = credentials('dev-nonpci-shared-deployment-token')
          SHARED_SERVER = 'https://api.dev-nonpci-shared.npnbank.local:6443'
          ARTIFACTORY = credentials('artifactory')
          DOCKER_TOKEN = credentials('artifactory-docker')
          NEXUS_USR = credentials('nexus')
          NEXUS_PASSWORD = credentials('nexus-docker')
          NEXUS_REPO= 'nexus-connector.apps.dev-nonpci-shared.npnbank.local'
        }
        stages {
          // capture and setup versioning information
          stage('Setup') {
            steps {
              script {
                def jobPaths = JOB_NAME.split('/')
                NAME = jobPaths[jobPaths.size()-1]
                IMAGE_TAG = GIT_BRANCH.replace('origin/', '').replace('\\', '-').replace('/', '-').replace('_', '-').toLowerCase()
                SHORT_COMMIT = GIT_COMMIT.substring(0, 8)
                currentBuild.displayName = "${IMAGE_TAG}-${SHORT_COMMIT}"
                APPLICATION_NAME = "${params.CROATIA_ENVIRONMENT}-$NAME"
              }
            }
          }
          // run a release job to make sure there is an associated image, this is meant to run when dpeloying a branch which would not automatically
          // trigger the release job
          stage('Build image') {
            when {
              expression {
                if (params.BRANCH != 'main' && params.BRANCH != 'origin/main') {
                  return true
                }
                return false
              }
            }
            steps {
              script {
                build job: "/alpha/release/$NAME", parameters: [string(name: 'BRANCH', value: params.BRANCH)]
              }
            }
          }
          // update the argocd application for the microservice, by updating the referenced image to the one built by the release job
          stage('Update Development') {
            steps {
              sh ' oc login --token=\$SHARED_TOKEN --server=\$SHARED_SERVER --insecure-skip-tls-verify=true'
              script {
                def values = """nameOverride: $NAME
gateway:
  port: 443
  name: https
  protocol: HTTPS
  tls:
    enabled: true
image:
  repository: ${NEXUS_REPO}/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}
host: ${NAME}.apps.${params.CROATIA_ENVIRONMENT}.npnbank.local
hosts:
- ${NAME}.apps.${params.CROATIA_ENVIRONMENT}.npnbank.local"""

                def application = [
                  'apiVersion': 'argoproj.io/v1alpha1',
                  'kind': 'Application',
                  'metadata': [
                    'labels': [
                      'component': 'alpha-microservice',
                      'environment': params.CROATIA_ENVIRONMENT
                    ],
                    'name': APPLICATION_NAME,
                    'namespace': 'argocd'
                  ],
                  'spec': [
                    'destination': [
                      'name': params.CROATIA_ENVIRONMENT,
                      'namespace': 'alpha'
                    ],
                    'project': 'alpha',
                    'syncPolicy': [
                      'automated': [
                        'prune': true
                      ]
                    ]
                  ]
                ]

                // check if the microservice has a values.yaml file, and if it does, add them to the argocd application to override 
                // the default chart values
                def hasValues = false
                if (fileExists('values.yaml') || fileExists('values.yml')) {
                  application.spec.sources = [
                    [
                      'chart': 'alpha-microservice-chart',
                      'helm': [
                        'valueFiles': ['$values/values.yaml'],
                        'values': values
                      ],
                      'repoURL': 'https://artifactory.apps.dev-nonpci-shared.npnbank.local/artifactory/helm-local',
                      'targetRevision': '0.1.19'
                    ],
                    [
                      'ref': 'values',
                      'repoURL': "ssh://git@bitbucket.projectcroatia.cloud/alpha/${NAME}.git",
                      'targetRevision': GIT_BRANCH.replace('origin/', '')
                    ]
                  ]
                } else {
                  application.spec.source = [
                    'chart': 'alpha-microservice-chart',
                    'helm': [
                      'values': values
                    ],
                    'repoURL': 'https://artifactory.apps.dev-nonpci-shared.npnbank.local/artifactory/helm-local',
                    'targetRevision': '0.1.19'
                  ]
                }

                writeYaml file: 'application.yaml', data: application
              }
              sh 'kubectl -n argocd apply -f application.yaml'
            }
          }
        }
      }
    break;
  }
}