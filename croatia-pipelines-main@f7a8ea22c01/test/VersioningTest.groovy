package com.croatia

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import com.lesfurets.jenkins.unit.BasePipelineTest

class VersioningTest extends BasePipelineTest {
  def versioningPipeline

  @BeforeEach
  void setUp() {
    super.setUp()
    versioningPipeline = loadScript("library/vars/versioning.groovy")
    helper.registerAllowedMethod('withCredentials', [Map, Closure], null)
    helper.registerAllowedMethod('sshUserPrivateKey', [Map], null)
    helper.registerAllowedMethod('sshUserPrivateKey', [Map], null)
  }

  void setupShellMocks(message) {
    helper.registerAllowedMethod('sh', [Map]) { args ->
      if (!args.returnStdout) {
        return ''
      }

      switch(args.script) {
        case 'git log --format=%H -n 1 origin/main':
          return '0a0a0aaa000a0aa0aaaaaaaa0a000000a000a0aa'
        case 'git tag -l':
          return '''release/v0.0.0
release/v0.0
release/v0'''
        case 'git show -s --format=%ci release/v0.0.0':
        case 'git show -s --format=%ci release/v0.0':
        case 'git show -s --format=%ci release/v0':
          return '2023-01-01 00:00:00 +0000'
        case 'git log --format=%H -n 1 release/v0.0.0':
        case 'git log --format=%H -n 1 release/v0.0':
        case 'git log --format=%H -n 1 release/v0':
          return '1a0a0aaa000a0aa0aaaaaaaa0a000000a000a0aa'
        case 'git rev-list release/v0.0.0..HEAD':
        case 'git rev-list release/v0.0..HEAD':
        case 'git rev-list release/v0..HEAD':
          return '2a0a0aaa000a0aa0aaaaaaaa0a000000a000a0aa'
        case 'git log --format=%s -n 1 2a0a0aaa000a0aa0aaaaaaaa0a000000a000a0aa':
          return message
        default:
          return ''
      }
    }
  }

  @Test
  void testVersionScopeless() {
    setupShellMocks('feat: mock')
    def ranTagging = false
    helper.registerAllowedMethod('sh', [String]) { script ->
      if (script.contains('git tag -f')) {
        def expected = '''
        git tag -f release/v0.1.0
        git push -f origin release/v0.1.0
        git tag -f release/v0.1    
        git push -f origin release/v0.1
        git tag -f release/v0
        git push -f origin release/v0   
        '''
        assert script.replaceAll('\\s', '').replaceAll('\n', '') == expected.replaceAll('\\s', '').replaceAll('\n', '')
        ranTagging = true
      }
    }
    versioningPipeline()

    assert ranTagging
  }

  @Test
  void testVersionScoped() {
    setupShellMocks('feat(mock): mock')
    def ranTagging = false
    helper.registerAllowedMethod('sh', [String]) { script ->
      if (script.contains('git tag -f')) {
        def expected = '''
        git tag -f release/mock/v0.1.0
        git push -f origin release/mock/v0.1.0
        git tag -f release/mock/v0.1    
        git push -f origin release/mock/v0.1
        git tag -f release/mock/v0
        git push -f origin release/mock/v0   
        '''
        assert script.replaceAll('\\s', '').replaceAll('\n', '') == expected.replaceAll('\\s', '').replaceAll('\n', '')
        ranTagging = true
      }
    }
    versioningPipeline(tagByScopes: true)

    assert ranTagging
  }
}
