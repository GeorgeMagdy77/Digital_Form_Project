pipelineJob("openvpn") {
  displayName('Create OpenVPN User Client Config')
  parameters {
    stringParam('EMAIL_PREFIX', '', 'The email of the user before the @ domain')
    choiceParam('EMAIL_DOMAIN', [
      '@deloitte.co.uk',
      '@deloittece.com',
      '@deloitte.lu',
      '@deloittedigital.se',
      '@deloitte.nl',
      '@ejada.com',
      '@deloitte.com',
      '@alrajhibank.com.sa'
    ])
  }
  definition {
    cps {
      sandbox(true)
      script('openvpn()')
    }
  }
}
