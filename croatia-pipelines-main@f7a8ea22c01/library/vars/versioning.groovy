/**
* versioning is a helper function to setup git push credentials, and semantically version a repository, based on a commit history
* derived from conventional commits
*/

import com.croatia.pipelines.helpers.SemanticVersion
import com.croatia.pipelines.helpers.ConventionalCommits
import com.croatia.pipelines.helpers.VersionIncrement

/**
* call is the primary entrypoint for Jenkins
* @param config is a map holiding configuration options from the implementing pipeline/Jenkinsfile
* config options
* - tagByScopes: a bool to state if the version tags should respect the conventional commit scopes from the message
*/
def call(Map args = [:]) {
  def tagByScopes = args.containsKey('tagByScopes') ? args.tagByScopes : false

  withCredentials([sshUserPrivateKey(credentialsId: 'opc-master', keyFileVariable: 'OPC_MASTER')]) {
    sh '''
    mkdir -p ~/.ssh
    cp "\$OPC_MASTER" ~/.ssh/opc-master
    git config --local core.sshCommand "/usr/bin/ssh -i ~/.ssh/opc-master"
    git config --local user.name "Jenkins"
    git config --local user.email "jenkins@projectcroatia.cloud"
    git fetch --all --tags
    git checkout $GIT_COMMIT
    '''
  }
  def headCommit = sh(script: 'git log --format=%H -n 1 origin/main', returnStdout: true).trim()
  def tags = sh(script: "git tag -l", returnStdout: true).trim().split('\n')

  def currentVersions = [:]

  for (tag in tags) {
    if (tag.contains('release/')) {
      def scope = '_'
      def semverStr = ''

      def scoplessMatch = tag =~ /release\/v(\d(\.\d)+)/
      def scopedMatch = tag =~ /release\/[a-zA-Z0-9-_]+\/v(\d(\.\d)+)/
      if (scoplessMatch.find()) {
        semverStr = tag.replace('release/v', '')
      } else if (scopedMatch.find()) {
        semverStr = tag.replace('release/', '')
        def versionScope = semverStr.split('/v')
        if (tagByScopes) {
          scope = versionScope[0].toLowerCase()
        }
        semverStr = versionScope[1]
      } else {
        continue
      }

      def semver = new SemanticVersion()
      semver.parse(semverStr)

      if (!currentVersions.containsKey(scope) || semver.isHigher(currentVersions[scope])) {
        currentVersions[scope] = [version: semver, tag: tag]
      }
    }
  }

  def latestTag = null
  def latestDate = null

  currentVersions.each{currentVersion -> 
    def commitDate = sh(script: "git show -s --format=%ci ${currentVersion.value.tag}", returnStdout: true).trim()
    def date = Date.parse("yyy-MM-dd hh:mm:ss Z", commitDate)

    if (!latestDate || date.compareTo(latestDate) > 0) {
      latestTag = currentVersion.value.tag
      latestDate = date
    }
  }
  def newCommits = ''
  if (currentVersions.keySet().size() > 0) {
    def commit = sh(script: "git log --format=%H -n 1 ${latestTag}", returnStdout: true).trim()
    if (commit != headCommit) {
      newCommits = sh(script: "git rev-list ${latestTag}..HEAD", returnStdout: true).trim().split('\n')
    }
  } else {
    newCommits = sh(script: "git rev-list HEAD", returnStdout: true).trim().split('\n')
  }

  def increments = [:]

  for (newCommit in newCommits) {  
    def message = sh(script: "git log --format=%s -n 1 $newCommit", returnStdout: true).trim()
    def inc = ConventionalCommits.parseCommitMessage(message)
    def scope = '_'

    if (tagByScopes) {
      scope = ConventionalCommits.getScope(message)
    }
    
    switch(inc) {
      case VersionIncrement.Major:
        increments[scope] = VersionIncrement.Major
      break
      case VersionIncrement.Minor:
        if (!increments.containsKey(scope) || increments[scope] != VersionIncrement.Major) {
          increments[scope] = VersionIncrement.Minor
        }
      break
      case VersionIncrement.Patch:
        if (!increments.containsKey(scope) || (increments[scope] != VersionIncrement.Major && increments[scope] != VersionIncrement.Minor)) {
          increments[scope] = VersionIncrement.Patch
        }
      break
    }
  }

  increments.each{increment ->
    def currentVersion = new SemanticVersion()
    if (currentVersions.containsKey(increment.key)) {
      currentVersion = currentVersions[increment.key].version
    }

    currentVersion.increment(increment.value)
    
    def prefixTag = "release/"
    if (increment.key != '_') {
      if (!(increment.key =~ /\s/)) {
        prefixTag = "${prefixTag}${increment.key}/"
      }
    }
    
    releaseTag = "${prefixTag}v${currentVersion.print()}"

    minorTag = "${prefixTag}v${currentVersion.major}.${currentVersion.minor}"
    majorTag = "${prefixTag}v${currentVersion.major}"

    sh """
    git tag -f ${releaseTag}
    git push -f origin ${releaseTag}
    git tag -f ${minorTag}
    git push -f origin ${minorTag}
    git tag -f ${majorTag}
    git push -f origin ${majorTag}
    """
  }
}
