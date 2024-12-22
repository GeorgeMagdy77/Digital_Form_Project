def call(Map config) {
  def pipelineType = env.PIPELINE_TYPE ? env.PIPELINE_TYPE : 'validate'

  switch(pipelineType) {
    case 'validate':
      pipeline {
        agent {
          kubernetes {
            inheritFrom 'infrastructure'
          }
        }

        options {
          disableConcurrentBuilds()
          ansiColor('xterm')
        }

        environment {
          OCI_CONFIG = credentials('oci-config')
        }

        stages {
          stage('Plan') {
            steps {
              checkout scm: BbS(branches: [[name: "*/main"]], credentialsId: 'jenkins-git', extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'croatia-variables']], id: 'variables', mirrorName: '', projectName: 'DigitalPlatform', repositoryName: 'croatia-variables', serverId: 'croatia', sshCredentialsId: 'opc-master')
              sh '''
              terraform init -backend=false
              terraform validate
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
          }
        }

        options {
          disableConcurrentBuilds()
          ansiColor('xterm')
        }

        environment {
          OCI_CONFIG = credentials('oci-config')
        }

        stages {
          stage('Setup') {
            steps {
              withCredentials([sshUserPrivateKey(credentialsId: 'oci-key', keyFileVariable: 'OCI_KEY')]) {
                checkout scm: BbS(branches: [[name: "*/main"]], credentialsId: 'jenkins-git', extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'croatia-variables']], id: 'variables', mirrorName: '', projectName: 'DigitalPlatform', repositoryName: 'croatia-variables', serverId: 'croatia', sshCredentialsId: 'opc-master')
                sh '''
                mkdir -p ~/.ssh
                cp "$OCI_KEY" ~/.ssh/oci-key
                mkdir -p ~/.oci
                cp "$OCI_CONFIG" ~/.oci/config
                '''
              }
              script {
                def jobPaths = JOB_NAME.split('/')
                SPLIT_NAME = jobPaths[jobPaths.size()-1]
                NAME = config.name ? config.name : SPLIT_NAME

                BACKEND_CONFIG = './croatia-variables/backends'
                if (params.ENVIRONMENT) {
                  BACKEND_CONFIG = BACKEND_CONFIG + "/${params.ENVIRONMENT}"
                }
                if (params.CLUSTER) {
                  BACKEND_CONFIG = BACKEND_CONFIG + "/${params.CLUSTER}"
                }
                BACKEND_CONFIG = BACKEND_CONFIG + "${NAME}/backend.tfvars"

                VAR_FILES = ''
                if (fileExists('./croatia-variables/variables.tfvars')) {
                  VAR_FILES = '-var-file=./croatia-variables/variables.tfvars'
                }
                if (fileExists("./croatia-variables/${params.ENVIRONMENT}/variables.tfvars")) {
                  VAR_FILES = VAR_FILES + ' ' + "-var-file=./croatia-variables/${params.ENVIRONMENT}/variables.tfvars"
                }
                if (config.useLayer && fileExists("./croatia-variables/${params.ENVIRONMENT}/${params.CLUSTER}/variables.tfvars")) {
                  VAR_FILES = VAR_FILES + ' ' + "-var-file=./croatia-variables/${params.ENVIRONMENT}/${params.CLUSTER}/variables.tfvars"
                }
              }
            }        
          }
          stage('Init') {
            steps {
              sh "terraform init -backend-config=$BACKEND_CONFIG"
            }
          }
          stage('Plan') {
            steps {
              sh "terraform plan $VAR_FILES --out ${NAME}.plan"
            }
          }
          stage('Apply') {
            steps {
              input 'Is the plan ok to run?'
              sh "terraform apply ${NAME}.plan"
            }
          }
        }
      }
    break;
  }
}
