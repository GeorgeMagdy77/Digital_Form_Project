def call(Map config) {
  def pipelineType = env.PIPELINE_TYPE ? env.PIPELINE_TYPE : 'build'
  def dockerYaml = '''
spec:
  tolerations:
  - effect: NoSchedule
    key: service
    operator: Equal
    value: jenkins-agents
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: croatia-role
            operator: In
            values:
            - jenkins
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
      - --registry-mirror=https://nexus.projectcroatia.cloud:18080
      tty: true
      volumeMounts:
      - mountPath: /var/run/
        name: dind-socket
  volumes:
    - name: dind-socket
      emptyDir: {}
  '''
  def deployYaml = '''
spec:
  tolerations:
  - effect: NoSchedule
    key: service
    operator: Equal
    value: jenkins-agents
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: croatia-role
            operator: In
            values:
            - jenkins
  containers:
    - name: jnlp
      securityContext: 
        privileged: true
        runAsUser: 0

  '''
  def podYaml = config.docker ? dockerYaml : ''

  switch(pipelineType) {
    case 'build':
      pipeline {
        agent {
          kubernetes {
            inheritFrom 'microservices'
            yaml "$podYaml"
          }
        }
        environment {
          NEXUS_CREDENTIALS = credentials('nexus')
          TESTCONTAINERS_RYUK_DISABLED = 'true'
        }
        stages {
          stage('Build') {
            steps {
              sh '''
              sed -i 's,https\\://services.gradle.org/distributions/gradle-7.4.2-bin.zip,file\\:/home/jenkins/.gradle/wrapper/dists/gradle-7.4.2-bin.zip,g' gradle/wrapper/gradle-wrapper.properties
              ./gradlew clean build $GRADLE_BUILD_OPTIONS
              '''
            }
          }
          stage('Generate Allure Results') {
            steps {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                sh "./gradlew  allureReport"
              }
            }
          }
          stage('SonarQube') {
            when {
              not {
                expression { return config.skipSonar }
              }
            }
            steps {
              withSonarQubeEnv(installationName: 'croatia', credentialsId: 'sonar') {
                sh './gradlew sonarqube'
              }
            }
          }
          stage("Quality gate") {
            steps {
              waitForQualityGate abortPipeline: true
            }
          }

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
          stage('Release') {
            when {
              expression {
                return GIT_BRANCH.contains('release/')
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
    case 'release':
      pipeline {
        agent none
        environment {
          NEXUS_CREDENTIALS = credentials('nexus')
        }
        stages {
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
                NAME = config.name ? config.name : NAME
                IMAGE_TAG = GIT_BRANCH.replace('origin/', '').replace('\\', '-').replace('/', '-').replace('_', '-')
                SHORT_COMMIT = GIT_COMMIT.substring(0, 6)
                currentBuild.displayName = "${IMAGE_TAG}-${SHORT_COMMIT}"
              }
            }
          }
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
              sed -i 's,https\\://services.gradle.org/distributions/gradle-7.4.2-bin.zip,file\\:/home/jenkins/.gradle/wrapper/dists/gradle-7.4.2-bin.zip,g' gradle/wrapper/gradle-wrapper.properties
              ./gradlew clean build $GRADLE_BUILD_OPTIONS
              '''
              stash includes: 'build/**/*', name: 'build'
            }
          }    
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
              buildah login -u "$NEXUS_CREDENTIALS_USR" -p "$NEXUS_CREDENTIALS_PSW" nexus.projectcroatia.cloud:18079
              buildah bud -t "nexus.projectcroatia.cloud:18079/alpha-container-releases/${NAME}:${IMAGE_TAG}" .
              buildah tag "nexus.projectcroatia.cloud:18079/alpha-container-releases/${NAME}:${IMAGE_TAG}" "nexus.projectcroatia.cloud:18079/alpha-container-releases/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
              buildah push "nexus.projectcroatia.cloud:18079/alpha-container-releases/${NAME}:${IMAGE_TAG}"
              buildah push "nexus.projectcroatia.cloud:18079/alpha-container-releases/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
              '''
            }
          }
          stage('Package Helm') {
            agent {
              kubernetes {
                inheritFrom 'infrastructure'
              }
            }
            environment {
              NAME = "$NAME"
              IMAGE_TAG = "$IMAGE_TAG"
              SHORT_COMMIT = "$SHORT_COMMIT"
            }
            steps {
              checkout scm: BbS(
                branches: [[name: "*/main"]],
                credentialsId: 'jenkins-git',
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'alpha-microservice-chart']],
                id: 'variables',
                mirrorName: '',
                projectName: 'Alpha',
                repositoryName: 'alpha-microservice-chart',
                serverId: 'croatia',
                sshCredentialsId: 'opc-master'
              )

              script {
                def chartYaml = readYaml file: './alpha-microservice-chart/Chart.yaml'
                chartYaml.name = "$NAME"
                writeYaml file: './alpha-microservice-chart/Chart.yaml', data: chartYaml, overwrite: true

                def chartValuesFile = './alpha-microservice-chart/values.yaml'
                if (fileExists('./alpha-microservice-chart/values.yml')) {
                  chartValuesFile = './alpha-microservice-chart/values.yml'
                }

                def chartValuesYaml = readYaml file: chartValuesFile

                chartValuesYaml.image.repository = "nexus.projectcroatia.cloud:18079/alpha-container-releases/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT}"
                chartValuesYaml.hosts = ["${NAME}.apps.projectcroatia.cloud"]

                if (fileExists('./values.yaml') || fileExists('./values.yml')) {
                  def appValuesFile = './values.yaml'
                  if (fileExists('./values.yml')) {
                    appValuesFile = './values.yml'
                  }

                  def appValuesYaml = readYaml file: appValuesFile

                  chartValuesYaml = chartValuesYaml << appValuesYaml
                }

                writeYaml file: chartValuesFile, data: chartValuesYaml, overwrite: true
              }

              sh '''
              cd ./alpha-microservice-chart/
              helm repo add nexus "https://${NEXUS_CREDENTIALS_USR}:${NEXUS_CREDENTIALS_PSW}@nexus.projectcroatia.cloud/repository/alpha-charts/"
              helm package . 
              curl -u "${NEXUS_CREDENTIALS_USR}:${NEXUS_CREDENTIALS_PSW}" https://nexus.projectcroatia.cloud/repository/alpha-charts/ --upload-file $(ls | grep .tgz) -v
              rm -f $(ls | grep .tgz)
              cd ../
              '''

              script {
                def chartYaml = readYaml file: './alpha-microservice-chart/Chart.yaml'
                chartYaml.version = chartYaml.version+"-${IMAGE_TAG}-${SHORT_COMMIT}"
                writeYaml file: './alpha-microservice-chart/Chart.yaml', data: chartYaml, overwrite: true
              }

              sh '''
              cd ./alpha-microservice-chart/
              helm package .
              curl -u "${NEXUS_CREDENTIALS_USR}:${NEXUS_CREDENTIALS_PSW}" https://nexus.projectcroatia.cloud/repository/alpha-charts/ --upload-file $(ls | grep .tgz) -v
              '''
            }
          }
        }
      }
    break;
    case 'deploy':
      pipeline {
        agent {
          kubernetes {
            inheritFrom 'infrastructure'
            yaml "$deployYaml"
          }
        }
        environment {
          NEXUS_CREDENTIALS = credentials('nexus')
          ENVIRONMENT_KUBECONFIG = credentials("${params.ALPHA_ENVIRONMENT}-kubeconfig")
        }
        stages {
          stage('Setup') {
            steps {
              script {
                def jobPaths = JOB_NAME.split('/')
                SPLIT_NAME = jobPaths[jobPaths.size()-1]
                NAME = config.name ? config.name : SPLIT_NAME
                IMAGE_TAG = GIT_BRANCH.replace('origin/', '').replace('\\', '-').replace('/', '-').replace('_', '-')
                SHORT_COMMIT = GIT_COMMIT.substring(0, 6)
                NAMESPACE = config.namespace ? config.namespace : 'alpha'
                IMAGE_TAG_EXISTS = false
                VALUES_FILE = ''
                if (fileExists('./values.yaml')) {
                  VALUES_FILE = '-f ./values.yaml'
                } else if (fileExists('./values.yml')) {
                  VALUES_FILE = '-f ./values.yml'
                }
                currentBuild.displayName = "${params.ALPHA_ENVIRONMENT} - ${IMAGE_TAG}-${SHORT_COMMIT}"
              }
            }
          }
          stage('Check') {
            steps {
              script {
                IMAGE_TAGS = sh(script: "curl -q -H 'Accept: application/vnd.docker.distribution.manifest.v2+json' -H 'Content-Type: application/json' -H 'User-Agent: docker/20.10.14' https://nexus.projectcroatia.cloud:18080/v2/alpha-container-releases/${NAME}/tags/list", returnStdout: true).trim()
                IMAGE_TAGS_JSON = readJSON text: IMAGE_TAGS
                def tagList = IMAGE_TAGS_JSON.tags.join(', ')
                echo "Found tags: ${tagList}"
                echo "Checking for tag ${IMAGE_TAG}-${SHORT_COMMIT}"
                IMAGE_TAGS_JSON.tags.each{tag->
                  if (tag == "${IMAGE_TAG}-${SHORT_COMMIT}") {
                    echo "Found the tag in the list"
                    IMAGE_TAG_EXISTS = true
                  }
                }
              }
            }
          }
          stage('Build') {
            when {
              allOf {
                equals expected: false, actual: IMAGE_TAG_EXISTS
                equals expected: 'development', actual: params.ALPHA_ENVIRONMENT
              }
            }
            steps {
              build job: "/alpha/release/$SPLIT_NAME", parameters: [string(name: 'BRANCH', value: "${GIT_BRANCH}")]
              script {
                IMAGE_TAG_EXISTS = true
              }
            }
          }
          stage('Deploy') {
            environment {
              NAME = "$NAME"
              IMAGE_TAG = "$IMAGE_TAG"
              SHORT_COMMIT = "$SHORT_COMMIT"
              NAMESPACE = "$NAMESPACE"
              ALPHA_ENVIRONMENT = "${params.ALPHA_ENVIRONMENT}"
              VALUES_FILE = "$VALUES_FILE"
            }
            steps {
              script {
                if (!IMAGE_TAG_EXISTS) {
                  error("An image with the tag ${IMAGE_TAG}-${SHORT_COMMIT} does not exist, failing build")
                }
                def releasePattern = GIT_BRANCH =~ /release\//
                if (params.ALPHA_ENVIRONMENT != 'development' && !releasePattern.find()) {
                  error("higher environment deployments must use release versions")
                }
              }
              sh '''
              export KUBECONFIG=$ENVIRONMENT_KUBECONFIG
              helm repo add nexus "https://${NEXUS_CREDENTIALS_USR}:${NEXUS_CREDENTIALS_PSW}@nexus.projectcroatia.cloud/repository/alpha-charts/"
              helm -n $NAMESPACE upgrade --install $VALUES_FILE $NAME nexus/alpha-microservice-chart --set="image.repository=nexus.projectcroatia.cloud:18080/alpha-container-releases/${NAME}:${IMAGE_TAG}-${SHORT_COMMIT},hosts[0]=${NAME}.apps.${ALPHA_ENVIRONMENT}.projectcroatia.cloud"
              kubectl -n $NAMESPACE rollout restart deployment "$NAME-v1"
              kubectl -n $NAMESPACE rollout status deployment "$NAME-v1" --timeout=120s
              '''
            }
          }
        }
      }
    break;
  }
}
