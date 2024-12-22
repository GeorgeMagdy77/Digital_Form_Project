def call() {
 pipeline {
  agent {
    kubernetes {
      inheritFrom 'microservices'
    }
  }
  environment {
    BITBUCKET_CREDS = credentials('jenkins-git')
    OPC_MASTER = credentials('opc-master')
  }

  stages {
    stage('Build') {
      steps {
        checkout scm: BbS(branches: [[name: "*/main"]], credentialsId: 'jenkins-git', extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'template-app']], id: 'templateapp', mirrorName: '', projectName: 'Alpha', repositoryName: 'alpha-template-app', serverId: 'croatia', sshCredentialsId: 'opc-master')
        sh """
        echo "Building from source code"
        cd template-app && ./gradlew build
        java -jar build/libs/alpha-template-app-0.1-all.jar --output application --template ${params.template} --application ${params.appName} --group croatia --synopsis ${params.synopsis} --description ${params.description}
        """
        input 'Ready to push the build to remote repository?'
      }
    }
    stage('Push') {
      steps {
        sh """
        if [ ! -d "template-app/application" ] 
        then
          echo "Build failed" 
          exit 999
        fi
        cd template-app/application
        echo "======== Creating Repo ========"
        echo "Creating Bitbucket Repository"
        curl --request POST \
             --url "https://bitbucket.projectcroatia.cloud/rest/api/latest/projects/Alpha/repos" \
             -u ${BITBUCKET_CREDS_USR}:${BITBUCKET_CREDS_PSW} \
             --header 'Accept: application/json' \
             --header 'Content-Type: application/json' \
             -d '{"is_private": true, "scmId": "git", "project": {"key": "ALPHA"}, "slug": "${params.appName}","name": "${params.appName}"}'

        echo "======== Creating new branch and pushing initial commit  ========"
        mkdir -p ~/.ssh
        cp "\$OPC_MASTER" ~/.ssh/opc-master
        git config --global init.defaultBranch main
        git init
        git config --local core.sshCommand "/usr/bin/ssh -i ~/.ssh/opc-master"
        git config --local user.name "Jenkins"
        git config --local user.email "jenkins@projectcroatia.cloud"
        git checkout -b main
        git add .
        git commit -m "Initial commit"
        git push --set-upstream ssh://git@bitbucket.projectcroatia.cloud/alpha/${params.appName}.git main
        """
        }
    }
  }
 }
}
