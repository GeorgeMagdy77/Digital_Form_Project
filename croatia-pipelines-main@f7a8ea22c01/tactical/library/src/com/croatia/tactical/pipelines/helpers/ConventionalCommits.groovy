package com.croatia.tactical.pipelines.helpers

static def getScope(message) {
  def matcher = message =~ /\([^)]*\)/

  if (matcher.find()) {
    return matcher.group(0).replace('(', '').replace(')', '').toLowerCase()
  }

  return '_'
}

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
