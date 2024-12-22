/**
* BitbucketPaginatedAPICall is a class for helping to orchestate and run paginated get requests to Bitbucket
*/
package com.croatia.pipelines.helpers

/**
* get method will call bitbucket with any url and auth headers to json based rest api
* @param url the rest api url to call
* @param authHeader an auth header to use when calling the api
* @param allowedCode is the allowed response code form the endpoint
* @return a json object with the response from the api
*/
static def get(url, authHeader, allowedCode = 200) {
  def responses = []
  def pageResponse = RestAPICall.get(url, authHeader, allowedCode)
  for (value in pageResponse.values) {
      responses << value
  }
  // paginating in bitbucket is handled by the 'nextPageStart' property
  // isPaging will only toggle to false once the above property no longer exists
  // failures on the rest call will cause a panic, and break the loop regardless
  def isPaging = true
  if (!pageResponse.limit) {
    isPaging = false
  }
  while (isPaging) {
    def pageUrl = "${url}?start=${pageResponse.nextPageStart}"
    pageResponse = RestAPICall.get(pageUrl, authHeader, allowedCode)
    for (value in pageResponse.values) {
        responses << value
    }
    if (pageResponse.isLastPage) {
      isPaging = false
    }
  }
  return responses
}
