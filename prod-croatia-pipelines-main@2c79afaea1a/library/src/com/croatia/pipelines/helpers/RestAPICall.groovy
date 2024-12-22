/**
* RestAPICall is a class to get and parse json responses from a rest api endpoint
*/
package com.croatia.pipelines.helpers

import groovy.json.JsonSlurperClassic

/**
* get is a basic implementation of an http get request
* @param url the rest api url to call
* @param authHeader an auth header to use when calling the api
* @param allowedCode is the allowed response code form the endpoint
* @return a json object with the response from the api
*/
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

/**
* getResponse will perform the actual http call, and run the parsing on the response
* @param request a url get request
* @param authHeader an auth header to use when calling the api
* @param allowedCode is the allowed response code form the endpoint
* @return a json object with the response from the api
*/
static def getResponse(request, authHeader, allowedCode) {
  request.setRequestProperty("Accept", 'application/json')
  request.setRequestProperty("Content-Type", 'application/json')
  request.setRequestProperty("Authorization", authHeader)

  request.connect()

  def response = [:]    
  if (request.responseCode == allowedCode) {
    response = new JsonSlurperClassic().parseText(request.inputStream.getText('UTF-8'))
  } else {
    response = new JsonSlurperClassic().parseText(request.errorStream.getText('UTF-8'))
  }

  return response
}
