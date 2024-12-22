/**
* iosBuild is a function holding full pipelines for building and publishing an ios app
*
* *NOTICE*
* assumes that the cluster has had the 'configuration' application in ArgoCD already deployed to it
* this is essential for creating the pre-requsite service account
*
* @param: CROATIA_ENVIRONMENT the target environment to enrol
*/

/**
* call is the primary entrypoint for Jenkins
*/
def call() {
  pipeline {
    agent {
      kubernetes {
        inheritFrom 'infrastructure'
      }
    }
    // load auth tokens and endpoints for the cluster being enrolled
    environment {
      TARGET_TOKEN = credentials("${params.CROATIA_ENVIRONMENT}-deployment-token")
      TARGET_SERVER = "https://api.${params.CROATIA_ENVIRONMENT}.nrapp.cloud:6443"
      VAULT_TOKEN = credentials("vault-token")
    }
    stages {
      // capture the cluster api and service account information
      // once obtained, build json objects to call the vault apis
      // the vault apis used are to register a new authentication method, and configure them to delegate to the OIDC hosted by the
      // target cluster
      // it then adds a default Vault role to the cluster, allowing it to read everything in the 'alpha' secrets backend
      stage('Add Role') {
        steps {
          script {
            sh 'oc login --token=$TARGET_TOKEN --server=$TARGET_SERVER --insecure-skip-tls-verify=true'
            SA_SECRET = sh(script: 'kubectl -n vault get secret $(kubectl -n vault get secret --field-selector type=kubernetes.io/service-account-token -A -o jsonpath=\'{.items[?(@.metadata.annotations.kubernetes\\.io/service-account\\.name=="vault-auth")].metadata.name}\') -o yaml', returnStdout: true).trim()
            def saSecret = readYaml text: SA_SECRET

            def encodedClusterCA = saSecret.data['ca.crt']

            CLUSTER_CA = sh(script: """
            set +x
            echo ${encodedClusterCA}|base64 --decode
            set -x
            """, returnStdout: true).trim()

            TOKEN = sh(script: """
            set +x
            echo ${saSecret.data.token}|base64 --decode
            set -x
            """, returnStdout: true).trim()

            VAULT_CONFIG = [
              'token_reviewer_jwt': TOKEN,
              'kubernetes_host': TARGET_SERVER,
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
              'policies': params.CROATIA_ENVIRONMENT
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
            https://vault.apps.prod-nonpci-shared.nrapp.cloud/v1/sys/auth/${params.CROATIA_ENVIRONMENT}

          curl -k -L \
            --request POST \
            --header "X-Vault-Token: \$VAULT_TOKEN" \
            --data '@config.json' \
            https://vault.apps.prod-nonpci-shared.nrapp.cloud/v1/auth/${params.CROATIA_ENVIRONMENT}/config

          curl -k -L \
            --request PUT \
            --header "X-Vault-Token: \$VAULT_TOKEN" \
            --data '@policy.json' \
            https://vault.apps.prod-nonpci-shared.nrapp.cloud/v1/sys/policies/acl/${params.CROATIA_ENVIRONMENT}

          curl -k -L \
            --request POST \
            --header "X-Vault-Token: \$VAULT_TOKEN" \
            --data '@role.json' \
            https://vault.apps.prod-nonpci-shared.nrapp.cloud/v1/auth/${params.CROATIA_ENVIRONMENT}/role/alpha
          """
        }
      }
    }
  }
}
