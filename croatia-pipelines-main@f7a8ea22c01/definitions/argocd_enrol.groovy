/*
a job to enrol a cluster into ArgoCD

it does this by adding a secret to the ArgoCD cluster, with a service account token and the endpoint

*NOTICE*
this job assumes that a service account called 'croatia-deployment' exists on the cluster, and has cluster wide admin privileges
in addition, it assumes a credential with the aboves access token has been added to vault/Jenkins ahead of being run
*/

/*
the following sections are using Jenkins reactive choices, allowing automatic settings of parameters
this is essential for labelling the clusters in ArgoCD, so that the right applications are deployed to them
*/

// detect what type of cluster is being enrolled, is it an alpha cluster, dmz cluster (ingress/kong), or the shared services cluster
def typeScript = '''
switch(CROATIA_ENVIRONMENT) {
  case ~/.*-appdb.*/:
    return ['alpha']
  case ~/.*-dmz-ingress.*/:
    return ['dmz']
  case ~/.*-shared.*/:
    return ['shared-services']
}
'''

// detect the role of the cluster. Is it the primary environment (ie. the main environment on the cluster), or a secondary one?
def roleScript = '''
switch(CROATIA_ENVIRONMENT) {
  case ~/.*eit.*/:
    return ['secondary']
  default:
    return ['primary']
}
'''

// where an environment is a secondary one, change the target namespace for alpha deployments
def namespaceScript = '''
switch(CROATIA_ENVIRONMENT) {
  case ~/.*eit-nonpci-appdb.*/:
  case ~/.*eit-pci-appdb.*/:
    return ['alpha-eit']
  default:
    return ['alpha']
}
'''

// for secondary environments, override the API endpoint of the cluster to be the primary
def apiScript = '''
switch(CROATIA_ENVIRONMENT) {
  case ~/.*eit-nonpci-appdb.*/:
    return ['dev-nonpci-appdb']
  case ~/.*eit-pci-appdb.*/:
    return ['dev-pci-appdb']
  case ~/.*eit-dmz-ingress.*/:
    return ['dev-dmz-ingress']
  default:
    return [CROATIA_ENVIRONMENT]
}
'''

pipelineJob("argocdenrol") {
  displayName('Enrol cluster in ArgoCD')
  parameters {
    choiceParam('CROATIA_ENVIRONMENT', [
      'dev-nonpci-shared',
      'dev-nonpci-appdb',
      'dev-pci-appdb',
      'dev-dmz-ingress',
      'sit-pci-appdb',
      'sit-nonpci-appdb',
      'sit-dmz-ingress',
      'eit-nonpci-appdb',
      'eit-pci-appdb',
      'eit-dmz-ingress',
      'uat-nonpci-appdb',
      'uat-pci-appdb',
      'uat-dmz-ingress',
      'nft-nonpci-appdb',
      'nft-pci-appdb',
      'nft-dmz-ingress'
    ])
    cascadeChoiceParameter {
      name('CLUSTER_TYPE')
      choiceType('PT_SINGLE_SELECT')
      filterable(true)
      referencedParameters('CROATIA_ENVIRONMENT')
      randomName('')
      filterLength(10)
      script {
        groovyScript {
          script {
            script(typeScript)
            sandbox(true)
          }
          fallbackScript {
            script('return []')
            sandbox(true)
          }
        }
      }
    }
    cascadeChoiceParameter {
      name('CLUSTER_ROLE')
      choiceType('PT_SINGLE_SELECT')
      filterable(true)
      referencedParameters('CROATIA_ENVIRONMENT')
      randomName('')
      filterLength(10)
      script {
        groovyScript {
          script {
            script(roleScript)
            sandbox(true)
          }
          fallbackScript {
            script('return []')
            sandbox(true)
          }
        }
      }
    }
    cascadeChoiceParameter {
      name('ALPHA_NAMESPACE')
      choiceType('PT_SINGLE_SELECT')
      filterable(true)
      referencedParameters('CROATIA_ENVIRONMENT')
      randomName('')
      filterLength(10)
      script {
        groovyScript {
          script {
            script(namespaceScript)
            sandbox(true)
          }
          fallbackScript {
            script('return []')
            sandbox(true)
          }
        }
      }
    }
    cascadeChoiceParameter {
      name('CLUSTER_API')
      choiceType('PT_SINGLE_SELECT')
      filterable(true)
      referencedParameters('CROATIA_ENVIRONMENT')
      randomName('')
      filterLength(10)
      script {
        groovyScript {
          script {
            script(apiScript)
            sandbox(true)
          }
          fallbackScript {
            script('return []')
            sandbox(true)
          }
        }
      }
    }
  }
  definition {
    cps {
      sandbox(true)
      script('argocdEnrol()')
    }
  }
}
