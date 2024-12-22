/**
* argocdEnrol is a function enrolling a cluster into ArgoCD
*
* *NOTICE*
* this job assumes that a service account called 'croatia-deployment' exists on the cluster, and has cluster wide admin privileges
* in addition, it assumes a credential with the aboves access token has been added to vault/Jenkins ahead of being run
*
* @param: CLUSTER_API string api endpoint for the cluster
* @param: CROATIA_ENVIRONMENT string name of the target cluster
* @param: CLUSTER_TYPE string of what the cluster type is, ie. alpha, dmz, shared-services, determines what applications are enrolled
* @param: CLUSTER_ROLE string of primary or secondary to determine if the environment is a primary or secondary (deployed in same cluster as a primary) one
* @param: ALPHA_NAMESPACE a string of the target namespace to deploy alpha components to
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
    // load auth tokens for both shared services, and the cluster being enrolled
    // requires that a valid deployment service account has been setup in the target, and that jenkins has the auth token for that user already set up
    environment {
      TARGET_TOKEN = credentials("${params.CLUSTER_API}-deployment-token")
      SHARED_TOKEN = credentials('prod-nonpci-shared-deployment-token')
      TARGET_SERVER = "https://api.${params.CLUSTER_API}.nrapp.cloud:6443"
      SHARED_SERVER = 'https://api.prod-nonpci-shared.nrapp.cloud:6443'
    }
    stages {
      // capture login & connection information for the service account 
      stage('Source') {
        steps {
          script {
            sh 'oc login --token=$TARGET_TOKEN --server=$TARGET_SERVER --insecure-skip-tls-verify=true --namespace=default'
            SA_SECRET = sh(script: 'kubectl get secret $(kubectl get secret --field-selector type=kubernetes.io/service-account-token -A -o jsonpath=\'{.items[?(@.metadata.annotations.kubernetes\\.io/service-account\\.name=="croatia-deployment")].metadata.name}\') -o yaml', returnStdout: true).trim()
            def saSecret = readYaml text: SA_SECRET

            CLUSTER_CA = saSecret.data['ca.crt']

            SECRET_PAYLOAD = sh(script: """
            set +x
            echo '{\\\"bearerToken\\\":\\\"\$TARGET_TOKEN\\\",\\\"tlsClientConfig\\\":{\\\"insecure\\\":false,\\\"caData\\\":\\\"$CLUSTER_CA\\\"}}'
            set -x
            """, returnStdout: true).trim().replace('\n', '')
          }
        }
      }
      // login to shares services, and build a kubernetes secret representing a cluster in ArgoCD, with the appropriate labels and metadata
      stage('Target') {
        steps {
          sh """
          oc login --token=\$SHARED_TOKEN --server=\$SHARED_SERVER --insecure-skip-tls-verify=true
          set +x 
          kubectl -n argocd create secret generic cluster-${params.CROATIA_ENVIRONMENT} --from-literal=config=$SECRET_PAYLOAD --from-literal=name=${params.CROATIA_ENVIRONMENT} --from-literal=server=$TARGET_SERVER
          set -x
          kubectl -n argocd label secret cluster-${params.CROATIA_ENVIRONMENT} "argocd.argoproj.io/secret-type=cluster"
          kubectl -n argocd label secret cluster-${params.CROATIA_ENVIRONMENT} "argo-cluster-project=croatia"
          kubectl -n argocd label secret cluster-${params.CROATIA_ENVIRONMENT} "argo-cluster-type=${params.CLUSTER_TYPE}"
          kubectl -n argocd label secret cluster-${params.CROATIA_ENVIRONMENT} "argo-cluster-role=${params.CLUSTER_ROLE}"
          """     
          script {
            if (params.CLUSTER_TYPE == 'alpha') {
              sh "kubectl -n argocd label secret cluster-${params.CROATIA_ENVIRONMENT} \"alpha-namespace=${params.ALPHA_NAMESPACE}\""
            }
          }
        }
      }
    }
  }
}
