package com.croatia.tactical.pipelines.helpers

import groovy.json.JsonSlurper

static def get(url, authHeader, allowedCode = 200) {
  try {
    def request = new URL(url).openConnection() as HttpURLConnection
    request.setRequestMethod('GET')
    request.setDoOutput(true)
    return getResponse(request, authHeader, allowedCode)
  } catch(e) {
    println "failed whilst performing GET against $url"
    throw e
  }
}

static def getResponse(request, authHeader, allowedCode) {
  request.setRequestProperty("Accept", 'application/json')
  request.setRequestProperty("Content-Type", 'application/json')
  request.setRequestProperty("Authorization", authHeader)

  request.connect()

  def response = [:]    
  if (request.responseCode == allowedCode) {
    response = new JsonSlurper().parseText(request.inputStream.getText('UTF-8'))
  } else {
    response = new JsonSlurper().parseText(request.errorStream.getText('UTF-8'))
  }

  return response
}
