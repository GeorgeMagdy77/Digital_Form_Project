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
      stages {
        stage('Validate') {
          steps {
            sh '''
            set +e
            exitCode=0
            rootDir=$(pwd)
            configDirs=$(find . -type d -not -path "*/charts/*" -not -path "*/.git/*" -not -path "./.git")

            for configDir in $configDirs ; do
              cd $configDir

              if [ -e kustomization.yaml ] || [ -e kustomization.yml ]; then
                echo "$configDir" >>$rootDir/kubeconform-results.txt

                kubectl kustomize . --enable-helm --load-restrictor LoadRestrictionsNone > /dev/null

                if [ $? -ne 0 ]; then
                  exitCode=1

                  kubectl kustomize . --enable-helm --load-restrictor LoadRestrictionsNone >>$rootDir/kubeconform-results.txt 2>&1
                else
                  kubectl kustomize . --enable-helm --load-restrictor LoadRestrictionsNone | \
                  kubeconform \
                  -ignore-missing-schemas \
                  -kubernetes-version 4.1.0 \
                  -schema-location default \
                  -schema-location 'https://raw.githubusercontent.com/garethr/openshift-json-schema/master/{{ .NormalizedKubernetesVersion }}-standalone{{ .StrictSuffix }}/{{ .ResourceKind }}.json' \
                  -skip Image,Route \
                  >>$rootDir/kubeconform-results.txt 2>&1

                  if [ $? -ne 0 ]; then
                    exitCode=1
                  else
                    echo "PASS" >>$rootDir/kubeconform-results.txt
                  fi

                  echo "" >>$rootDir/kubeconform-results.txt
                fi

                kubectl kustomize . --enable-helm --load-restrictor LoadRestrictionsNone | kube-score score --ignore-test pod-networkpolicy - >>$rootDir/kube-score-results.txt 2>&1
              fi

              cd $rootDir
            done

            cat $rootDir/kubeconform-results.txt
            cat $rootDir/kube-score-results.txt
              
            set -e
            exit $exitCode
            '''
          }
        }
        stage('Release') {
          when {
            equals expected: 'main', actual: GIT_BRANCH
          }
          steps {
            script {
                def jobPaths = JOB_NAME.split('/')
                NAME = jobPaths[jobPaths.size()-2]
            }
            build job: "kustomize/release/$NAME"
          }
        }
      }
    }
  break;
  case 'release':
    pipeline {
      agent {
        kubernetes {
          inheritFrom 'default'
        }
      }
      stages {
        stage('Release') {
          steps {
            script {
              versioning tagByScopes: true
            }
          }
        }
      }
    }
  break;
  }
}
