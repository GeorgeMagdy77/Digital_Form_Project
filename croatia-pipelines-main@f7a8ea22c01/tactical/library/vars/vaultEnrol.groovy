def call() {
  pipeline {
    agent {
      kubernetes {
        inheritFrom 'infrastructure'
      }
    }
    environment {
      ENVIRONMENT_KUBECONFIG = credentials("${params.ALPHA_ENVIRONMENT}-kubeconfig")
      VAULT_TOKEN = credentials("vault-token")
    }
    stages {
      stage('Add Role') {
        steps {
          script {
              def kubeconfig = readYaml file: ENVIRONMENT_KUBECONFIG
              CLUSTER_SERVER = sh(script: "echo ${kubeconfig.clusters[0].cluster.server}", returnStdout: true).trim().replace('\n', '')

              CLUSTER_CA = sh(script: 'KUBECONFIG="$ENVIRONMENT_KUBECONFIG" kubectl config view --raw --minify --flatten --output=\'jsonpath={.clusters[].cluster.certificate-authority-data}\' | base64 --decode', returnStdout: true).trim()

              SA_YAML = sh(script: 'KUBECONFIG="$ENVIRONMENT_KUBECONFIG" kubectl -n vault get sa vault-auth -o yaml', returnStdout: true).trim()
              def saYaml = readYaml text: SA_YAML

              SA_SECRET = sh(script: "KUBECONFIG=\"\$ENVIRONMENT_KUBECONFIG\" kubectl -n vault get secret ${saYaml.secrets[0].name} -o yaml", returnStdout: true).trim()
              def saSecret = readYaml text: SA_SECRET

              TOKEN = sh(script: """
              set +x
              echo ${saSecret.data.token}|base64 --decode
              set -x
              """, returnStdout: true).trim()

              VAULT_CONFIG = [
                'token_reviewer_jwt': TOKEN,
                'kubernetes_host': CLUSTER_SERVER,
                'kubernetes_ca_cert': CLUSTER_CA
              ]

              POLICY = [
                'policy': '''
                path "alpha/*" {
                  capabilities = ["read"]
                }
                '''
              ]

              ROLE = [
                'bound_service_account_names': '*', 
                'bound_service_account_namespaces': '*', 
                'policies': params.ALPHA_ENVIRONMENT
              ]
          }

          writeJSON file: 'config.json', json: VAULT_CONFIG
          writeJSON file: 'policy.json', json: POLICY
          writeJSON file: 'role.json', json: ROLE

          sh """
          curl -k -L \
            --header "X-Vault-Token: \$VAULT_TOKEN" \
            --request POST \
            --data '{"type": "kubernetes"}' \
            https://san-sandbox-vault.vault.svc.cluster.local:8200/v1/sys/auth/${params.ALPHA_ENVIRONMENT}

          curl -k -L \
            --request POST \
            --header "X-Vault-Token: \$VAULT_TOKEN" \
            --data '@config.json' \
            https://san-sandbox-vault.vault.svc.cluster.local:8200/v1/auth/${params.ALPHA_ENVIRONMENT}/config

          curl -k -L \
            --request PUT \
            --header "X-Vault-Token: \$VAULT_TOKEN" \
            --data '@policy.json' \
            https://san-sandbox-vault.vault.svc.cluster.local:8200/v1/sys/policies/acl/${params.ALPHA_ENVIRONMENT}

          curl -k -L \
            --request POST \
            --header "X-Vault-Token: \$VAULT_TOKEN" \
            --data '@role.json' \
            https://san-sandbox-vault.vault.svc.cluster.local:8200/v1/auth/${params.ALPHA_ENVIRONMENT}/role/alpha
          """
        }
      }
    }
  }
}
