pipelineJob("vaultenrol") {
  displayName('Enrol cluster in Vault')
  parameters {
    choiceParam('ALPHA_ENVIRONMENT', ['development', 'dev-cit', 'san-sandbox', 'san-kong'])
  }
  definition {
    cps {
      sandbox(true)
      script('vaultEnrol()')
    }
  }
}
