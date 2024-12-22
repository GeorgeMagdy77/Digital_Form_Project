pipelineJob("alpha/alpha-template-app") {
  displayName('alpha-template-app')
  parameters {
      stringParam('appName', '', 'New application name')
      stringParam('description', '', 'Description of the service to be deployed')
      stringParam('synopsis', '', 'Synopsis')
      choiceParam('template', ['javaApp','javaLibrary'])

  }

  definition {
    cps {
      sandbox(true)
      script('templateapp()')
    }
  }
}