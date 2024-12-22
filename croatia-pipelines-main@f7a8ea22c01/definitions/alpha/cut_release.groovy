pipelineJob("alpha/cut-release") {
  displayName('Cut Release')
  parameters {
    booleanParam('RUN_ALL', false, 'Should all repositories be cut? Unless versions are overridden, the release will cut against the main branch of all microservice repositories')
    stringParam('DEFAULT_VERSION', 'HEAD', 'When running all, this version will be used to generate the new branch')
    choiceParam('RELEASE_TYPE', ['release', 'hotfix'])
    stringParam('RELEASE', '', 'Provide the current build cycle, ie. BC1')
    textParam('REPOSITORY_VERSIONS', '', '''A line separated list of repositories and commits/versions to deploy. Must adhere to repository:commit format
For example:
example-repo-1:0aa000a0a0a
example-repo-2:1bb111b1b1b''')
  }
  definition {
    cps {
      sandbox(true)
      script('cutRelease()')
    }
  }
}
