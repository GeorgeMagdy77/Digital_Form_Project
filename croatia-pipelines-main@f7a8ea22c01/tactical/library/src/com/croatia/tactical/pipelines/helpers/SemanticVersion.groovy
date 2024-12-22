package com.croatia.tactical.pipelines.helpers

import groovy.transform.Field

@Field
def major = 0
@Field
def minor = 0
@Field
def patch = 0
@Field
def suffix = ''

def parse(version) {
  def fields = version.split('\\.', 3)
    
  if (fields.size() > 2 && fields[2]) {
    def patchStr = fields[2]
    if (patchStr.contains('-')) {
      def patchStrs = patchStr.split('-', 2)
      patchStr = patchStrs[0]
      suffix = patchStr[1]
    }
    patch = patchStr.toInteger()
  }
  if (fields.size() > 1 && fields[1]) {
    minor = fields[1].toInteger()
  }
  if (fields[0]) {
    major = fields[0].toInteger()
  }
}

def print() {
  def pSuffix = patch
  if (suffix) {
    pSuffix = "${pSuffix}-${suffix}"
  }
  return "${major}.${minor}.${pSuffix}"
}

def isHigher(compare) {
  if (compare.major > major) {
    return false
  }
  if (compare.minor > minor) {
    return false
  }
  if (compare.patch > patch) {
    return false
  }
  return true
}

def increment(inc ) {
  switch(inc) {
    case VersionIncrement.Major:
      major++
      minor = 0
      patch = 0
      suffix = ''
    break
    case VersionIncrement.Minor:
      this.minor++
      patch = 0
      suffix = ''
    break
    case VersionIncrement.Patch:
      this.patch++
      suffix = ''
    break
  }
}

return this
