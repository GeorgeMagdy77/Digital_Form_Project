/**
* backstage is a deployment pipeline for the Backstage application in use
* @param: BRANCH a git branch to run the deploy against
*/

/**
* call is the primary entrypoint for Jenkins
*/
def call() {
  pipeline {
    agent {
      kubernetes {
        inheritFrom 'microservices'
        yaml '''
  spec:
    containers:
      - name: jnlp
        securityContext: 
          privileged: true
          runAsUser: 0
  '''
      }
    }

    options {
      disableConcurrentBuilds()
    }

    // load credentials for artifactory & the shared services cluster
    environment {
      ARTIFACTORY = credentials('artifactory')
      DOCKER_TOKEN = credentials('artifactory-docker')
      SHARED_TOKEN = credentials('dev-nonpci-shared-deployment-token')
      SHARED_SERVER = 'https://api.dev-nonpci-shared.npnbank.local:6443'
    }
    stages {
      // build the node application, and then generate the container image with the applicable node code, then push to artifactory
      stage('Build') {
        environment {
          NAME = "$name"
        }
        steps {
          script {
            IMAGE_TAG = GIT_BRANCH.replace('origin/', '').replace('\\', '-').replace('/', '-').replace('_', '-').toLowerCase()
            SHORT_COMMIT = GIT_COMMIT.substring(0, 8)
            currentBuild.displayName = "${IMAGE_TAG}-${SHORT_COMMIT}"
          }
          sh """
          buildah login -u "\$ARTIFACTORY_USR" -p "\$DOCKER_TOKEN" artifactory.apps.dev-nonpci-shared.npnbank.local
          
          npm i
          yarn install --frozen-lockfile
          yarn tsc
          yarn build:backend
          
          buildah bud -t "artifactory.apps.dev-nonpci-shared.npnbank.local/docker-local/backstage:${IMAGE_TAG}" .
          buildah tag "artifactory.apps.dev-nonpci-shared.npnbank.local/docker-local/backstage:${IMAGE_TAG}" "artifactory.apps.dev-nonpci-shared.npnbank.local/docker-local/backstage:${IMAGE_TAG}-${SHORT_COMMIT}"
          buildah tag "artifactory.apps.dev-nonpci-shared.npnbank.local/docker-local/backstage:${IMAGE_TAG}" "artifactory.apps.dev-nonpci-shared.npnbank.local/docker-local/backstage:latest"
          buildah push "artifactory.apps.dev-nonpci-shared.npnbank.local/docker-local/backstage:${IMAGE_TAG}"
          buildah push "artifactory.apps.dev-nonpci-shared.npnbank.local/docker-local/backstage:${IMAGE_TAG}-${SHORT_COMMIT}"
          buildah push "artifactory.apps.dev-nonpci-shared.npnbank.local/docker-local/backstage:latest"
          """
        }
      }
      // update the live instance in shared services by forceably bouncing the pod running backstage
      stage('Update') {
        agent {
          kubernetes {
            inheritFrom 'infrastructure'
          }
        }
        steps {
          sh '''
          oc login --token=\$SHARED_TOKEN --server=\$SHARED_SERVER --insecure-skip-tls-verify=true
          kubectl -n backstage rollout restart deployment backstage
          kubectl -n backstage rollout status deployment backstage --timeout=120s
          '''
        }
      }
    }
    post {
      always {
        sh('buildah logout artifactory.apps.dev-nonpci-shared.npnbank.local')
      }
    }
  }
}