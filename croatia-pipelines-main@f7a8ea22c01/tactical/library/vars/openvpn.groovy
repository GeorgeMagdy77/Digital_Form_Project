def call(Map config) {
  pipeline {
    agent {
      kubernetes {
        inheritFrom 'infrastructure'
      }
    }
    stages {
      stage('Create') {
        steps {
          script {
            currentBuild.displayName = "${params.EMAIL_PREFIX}${params.EMAIL_DOMAIN}"
          }
          withCredentials([sshUserPrivateKey(credentialsId: 'opc-master', keyFileVariable: 'OPC_MASTER')]) {
            sh """
            ssh -o StrictHostKeyChecking=no -i "\$OPC_MASTER" ubuntu@10.0.0.35<< EOF 
            ./create-openvpn-client.sh ${params.EMAIL_PREFIX}
            exit
            EOF
            """
            sh "scp -o StrictHostKeyChecking=no -i \"\$OPC_MASTER\" ubuntu@10.0.0.35:/home/ubuntu/client-configs/files/${params.EMAIL_PREFIX}.ovpn ./${params.EMAIL_PREFIX}.ovpn"
          }
          emailext to: "${params.EMAIL_PREFIX}${params.EMAIL_DOMAIN}",
            attachmentsPattern: '*ovpn',
            from: 'jenkins@projectcroatia.cloud',
            body: '''You have been granted access to the Project Croatia VPN instance.

  Please make sure you have insalled the OpenVPN connect application for your operating system.
  This can be found in your organisations software catalog, or via the following official links:
  - https://openvpn.net/client-connect-vpn-for-windows/
  - https://openvpn.net/client-connect-vpn-for-mac-os/

  Attached is your user configuration file for OpenVPN. Details on how to import this profile can be found at: https://openvpn.net/cloud-docs/user-imports-received-profile-into-connect-client/
            ''',
            subject: "Croatia VPN Client Configuration",
            saveOutput: false
        }
      }
    }
  }
}
