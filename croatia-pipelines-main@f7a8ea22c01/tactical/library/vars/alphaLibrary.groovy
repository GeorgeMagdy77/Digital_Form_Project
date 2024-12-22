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
    break;
    case 'release':
      pipeline {
        agent {
          kubernetes {
            inheritFrom 'microservices'
            yaml "$podYaml"
          }
        }
        environment {
          NEXUS_CREDENTIALS = credentials('nexus')
        }
        stages {
          stage('Build') {
            steps {
              sh '''
              sed -i 's,https\\://services.gradle.org/distributions/gradle-7.4.2-bin.zip,file\\:/home/jenkins/.gradle/wrapper/dists/gradle-7.4.2-bin.zip,g' gradle/wrapper/gradle-wrapper.properties
              ./gradlew clean build $GRADLE_BUILD_OPTIONS
              echo '\nnexusUrl=https://nexus.projectcroatia.cloud/repository' >> gradle.properties
              echo 'nexusRepository=alpha-releases' >> gradle.properties
              echo "nexusUsername=${NEXUS_CREDENTIALS_USR}" >> gradle.properties
              echo "nexusPassword=${NEXUS_CREDENTIALS_PSW}" >> gradle.properties
              ./gradlew publish
              '''
            }
          }
        }
      }
    break;
  }
}
