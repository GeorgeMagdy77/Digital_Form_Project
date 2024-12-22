/**
* ConventionalCommits is a class for parsing conventional commit messages from strings
*/
package com.croatia.pipelines.helpers

/**
* getScope will attempt to retrieve a scope from a conventional commit
* @param message a conventional commit message sting
* @return a scope string
*/
static def getScope(message) {
  def matcher = message =~ /\([^)]*\)/

  // if a scope exists, then clean it out and return it
  if (matcher.find()) {
    return matcher.group(0).replace('(', '').replace(')', '').toLowerCase()
  }

  // if there is no scope, return a 'blank' scope string
  return '_'
}

/**
* parseCommitMessage will parse a message and attempt to translate a conventional prefix into a version increment
* @param message a conventional commit message sting
* @return an increment level
*/
static def parseCommitMessage(message) {
  def splitMessage = message.split(':', 2)
  if (splitMessage.size() < 2) {
    return VersionIncrement.None
  }
  def conventional = splitMessage[0]

  if (conventional.substring(conventional.length() - 1).equals('!')) {
    return VersionIncrement.Major
  }

  def scopeless = conventional.split('\\(')

  switch(scopeless[0]) {
    case 'feat':
      return VersionIncrement.Minor
    case 'fix':
    case 'build':
    case 'chore':
    case 'ci':
    case 'docs':
    case 'style':
    case 'refactor':
    case 'perf':
    case 'test':
      return VersionIncrement.Patch
    break
    default:
      return VersionIncrement.None
    break
  }
}

return this
