/*
a job to enrol a cluster to allow it to access Vault

it does this by adding kubernetes auth directly to Vault, and delegating that endpoint to auth against the specified clusters OIDC

*NOTICE*
this job assumes that a service account called 'croatia-deployment' exists on the cluster, and has cluster wide admin privileges
in addition, it assumes a credential with the aboves access token has been added to Vault/Jenkins ahead of being run
*/
pipelineJob("vaultenrol") {
  displayName('Enrol cluster in Vault')
  parameters {
    choiceParam('CROATIA_ENVIRONMENT', ['prod-nonpci-appdb','prod-pci-appdb','prod-dmz-ingress','prod-nonpci-shared'])
  }
  definition {
    cps {
      sandbox(true)
      script('vaultEnrol()')
    }
  }
}
