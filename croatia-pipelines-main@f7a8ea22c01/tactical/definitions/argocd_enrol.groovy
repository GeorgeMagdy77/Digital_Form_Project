pipelineJob("argocdenrol") {
  displayName('Enrol cluster in ArgoCD')
  parameters {
    choiceParam('ALPHA_ENVIRONMENT', ['development', 'dev-cit', 'san-sandbox', 'san-kong'])
    choiceParam('CLUSTER_TYPE', ['alpha', 'shared-services'])
  }
  definition {
    cps {
      sandbox(true)
      script('argocdEnrol()')
    }
  }
}
