/**
* kustomize is a function holding full pipelines for validation and release a kustomize repository
* any implementing pipeline should have at least one environment varaible defined
* this variable is 'PIPELINE_TYPE', and will determine what type of pipeline will be executed
* the allowed options for this variable are the following
* - validate: used for a validation pipelne
* - release: used for a release pipeline to generate an image
* @param: BRANCH a git branch to run the release against
*/

/**
* call is the primary entrypoint for Jenkins
* @param config is a map holiding configuration options from the implementing pipeline/Jenkinsfile
*/
def call(Map config) {
  // capture the pipeline type via an environmnt variable
  // default to the validation pipeline if not set
  def pipelineType = env.PIPELINE_TYPE ? env.PIPELINE_TYPE : 'validate'

  // the core switch which will set what type of pipeline will be executed
  switch(pipelineType) {
    // the whole pipeline for build & validation
  case 'validate':
    pipeline {
      agent {
        kubernetes {
          inheritFrom 'infrastructure'
        }
      }
      stages {
        /** 
        * run the validation steps, this setup will perform a series of checks against every yaml file is can find
        * - check that kustomize itself does not flag any errors (apply dry run)
        * - run kubeconform to check that the requested object type adheres to an expected schema (where applicable)
        * - run kube-score to check and report on the security adherence of files in the pipeline, using known best practices
        *
        * currently both kustomize and kubeconform will fail the build if they exit with anything other than a code 0
        * kube-score will report results to the console, but will not fail the build at present
        */
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
