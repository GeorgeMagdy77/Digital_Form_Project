def call() {
  pipeline {
    agent {
      kubernetes {
        inheritFrom 'infrastructure'
      }
    }
    environment {
      ENVIRONMENT_KUBECONFIG = credentials("${params.ALPHA_ENVIRONMENT}-kubeconfig")
      ARGOCD_KUBECONFIG = credentials("san-sandbox-kubeconfig")
    }
    stages {
      stage('Source') {
        steps {
          writeFile file: 'serviceaccount.yaml', text: '''apiVersion: v1
kind: ServiceAccount
metadata:
  name: croatia-deployment
  namespace: default
'''
          writeFile file: 'clusterrolebinding.yaml', text: '''apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: croatia-deployment
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: croatia-deployment
  namespace: default
'''
          sh 'KUBECONFIG="$ENVIRONMENT_KUBECONFIG" kubectl apply -f serviceaccount.yaml'
          sh 'KUBECONFIG="$ENVIRONMENT_KUBECONFIG" kubectl apply -f clusterrolebinding.yaml'

          script {
            def kubeconfig = readYaml file: ENVIRONMENT_KUBECONFIG
            CLUSTER_SERVER = sh(script: "echo ${kubeconfig.clusters[0].cluster.server}", returnStdout: true).trim().replace('\n', '')

            CLUSTER_CA = kubeconfig.clusters[0].cluster['certificate-authority-data']

            SA_YAML = sh(script: 'KUBECONFIG="$ENVIRONMENT_KUBECONFIG" kubectl -n default get sa croatia-deployment -o yaml', returnStdout: true).trim()
            def saYaml = readYaml text: SA_YAML

            SA_SECRET = sh(script: "KUBECONFIG=\"\$ENVIRONMENT_KUBECONFIG\" kubectl -n default get secret ${saYaml.secrets[0].name} -o yaml", returnStdout: true).trim()
            def saSecret = readYaml text: SA_SECRET

            TOKEN = sh(script: """
            set +x
            echo ${saSecret.data.token}|base64 --decode
            set -x
            """, returnStdout: true).trim()

            SECRET_PAYLOAD = sh(script: """
            set +x
            echo '{\\\"bearerToken\\\":\\\"$TOKEN\\\",\\\"tlsClientConfig\\\":{\\\"insecure\\\":false,\\\"caData\\\":\\\"$CLUSTER_CA\\\"}}'
            set -x
            """, returnStdout: true).trim().replace('\n', '')
          }
        }
      }
      stage('Target') {
        steps {
          sh """
          export KUBECONFIG="\$ARGOCD_KUBECONFIG"
          set +x 
          kubectl -n argocd create secret generic cluster-${params.ALPHA_ENVIRONMENT} --from-literal=config=$SECRET_PAYLOAD --from-literal=name=${params.ALPHA_ENVIRONMENT} --from-literal=server=$CLUSTER_SERVER
          set -x
          kubectl -n argocd label secret cluster-${params.ALPHA_ENVIRONMENT} "argocd.argoproj.io/secret-type=cluster"
          kubectl -n argocd label secret cluster-${params.ALPHA_ENVIRONMENT} "argo-cluster-type=${params.CLUSTER_TYPE}"
          """       
        }
      }
    }
  }
}
