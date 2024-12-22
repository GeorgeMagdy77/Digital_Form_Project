folder('agents') {
  displayName('Jenkins Agent Builds')
}

folder('alpha') {
  displayName('Alpha')
}

folder('alpha/validate') {
  displayName('Validate')
}

folder('alpha/release') {
  displayName('Release')
}

folder('alpha/deploy-development') {
  displayName('Deploy Development')
}

folder('alpha/deploy') {
  displayName('Deploy')
  properties {
    authorizationMatrix {
      permissions([
        'GROUP:Overall/Administer:jenkins-administrators',
        'GROUP:Agent/Build:jenkins-deployers',
        'GROUP:Credentials/View:jenkins-deployers',
        'GROUP:Job/Build:jenkins-deployers',
        'GROUP:Job/Cancel:jenkins-deployers',
        'GROUP:Job/Read:jenkins-deployers',
        'GROUP:Metrics/HealthCheck:jenkins-deployers',
        'GROUP:Metrics/ThreadDump:jenkins-deployers',
        'GROUP:Metrics/View:jenkins-deployers',
        'GROUP:Overall/Read:jenkins-deployers',
        'GROUP:Run/Replay:jenkins-deployers',
        'GROUP:View/Read:jenkins-deployers',
        'GROUP:Overall/Read:authenticated',
        'GROUP:Job/Read:authenticated',
        'GROUP:View/Read:authenticated',
        'GROUP:Overall/Read:jenkins-users',
        'GROUP:Job/Read:jenkins-users',
        'GROUP:View/Read:jenkins-users',
        'USER:Overall/Read:anonymous',
        'USER:Job/Read:anonymous',
        'USER:View/Read:anonymous'
      ])
      inheritanceStrategy {
        nonInheriting()
      }
    }
  }
}

folder('alpha/tester') {
  displayName('Tester')
}

folder('terraform') {
  displayName('Terraform')
}

folder('terraform/validate') {
  displayName('Validate')
}

folder('terraform/release') {
  displayName('Release')
}

folder('terraform/deploy') {
  displayName('Deploy')
}

folder('transact') {
  displayName('Transact')
}

folder('transact/validate') {
  displayName('Validate')
}

folder('transact/deploy-development') {
  displayName('Deploy Development')
}

folder('mobile') {
  displayName('Mobile')
}

folder('mobile/validate') {
  displayName('Validate')
}

folder('mobile/release') {
  displayName('Release')
}

folder('kustomize') {
  displayName('Kustomize')
}

folder('kustomize/validate') {
  displayName('Validate')
}

folder('kustomize/release') {
  displayName('Release')
}

folder('configuration') {
  displayName('Configuration')
}
