package com.croatia

import groovy.io.FileType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.MemoryJobManagement

import javaposse.jobdsl.dsl.helpers.workflow.BranchSourcesContext
import javaposse.jobdsl.dsl.helpers.BuildParametersContext
import javaposse.jobdsl.dsl.helpers.triggers.MultibranchWorkflowTriggerContext
import javaposse.jobdsl.dsl.helpers.triggers.TriggerContext
import javaposse.jobdsl.dsl.helpers.ScmContext
import javaposse.jobdsl.dsl.helpers.properties.PropertiesContext
import javaposse.jobdsl.dsl.helpers.properties.FolderPropertiesContext

class JobScriptsSpecAlternative extends Specification {

  MemoryJobManagement jobManagement = Spy(MemoryJobManagement)

  @Shared
  private File outputDir = new File('./build/debug-xml')

  def setupSpec() {
    outputDir.deleteDir()
  }

  def setup() {
    stubGeneratedDslCall PropertiesContext, 'authorizationMatrix'
    stubGeneratedDslCall FolderPropertiesContext, 'authorizationMatrix'
    stubGeneratedDslCall BranchSourcesContext, 'branchSource'
    stubGeneratedDslCall BuildParametersContext, 'gitParameter'
    stubGeneratedDslCall BuildParametersContext, 'booleanParam'
    stubGeneratedDslCall BuildParametersContext, 'cascadeChoiceParameter'
    stubGeneratedDslCall MultibranchWorkflowTriggerContext, 'BitbucketWebhookMultibranchTrigger'
    stubGeneratedDslCall TriggerContext, 'BitbucketWebhookTriggerImpl'
    _ * jobManagement.callExtension('BbS', _, ScmContext, *_) >> new Node(null, 'git(\'example\')')
  }

  @Unroll
  void 'test script #file.name'(File file) {
    when:
    new DslScriptLoader(jobManagement).runScript(getScriptTextWithProperties(file.text))
    writeItems(outputDir)

    then:
    noExceptionThrown()

    where:
    file << getJobFiles()
  }

  private void stubGeneratedDslCall(Class contextType, String methodName) {
    _ * jobManagement.callExtension(methodName, _, contextType, *_) >> JobManagement.NO_VALUE
  }

  private void writeItems(File outputDir) {
    jobManagement.savedConfigs.each { String name, String xml ->
      writeFile(new File(outputDir, 'jobs'), name, xml)
    }
    jobManagement.savedViews.each { String name, String xml ->
      writeFile(new File(outputDir, 'views'), name, xml)
    }
  }

  static List<File> getJobFiles() {
    List<File> files = []
    new File('definitions').eachFileRecurse(FileType.FILES) {
      if (it.name.endsWith('.groovy')) {
        files << it
      }
    }
    files
  }

  static void writeFile(File dir, String name, String xml) {
    List tokens = name.split('/')
    File folderDir = tokens[0..<-1].inject(dir) { File tokenDir, String token ->
      new File(tokenDir, token)
    }
    folderDir.mkdirs()

    File xmlFile = new File(folderDir, "${tokens[-1]}.xml")
    xmlFile.text = xml
  }

  static String getScriptTextWithProperties(String text) {
    def scriptText = 'BITBUCKET_TOKEN = \'\'\n' + text
    return scriptText
  }
}
