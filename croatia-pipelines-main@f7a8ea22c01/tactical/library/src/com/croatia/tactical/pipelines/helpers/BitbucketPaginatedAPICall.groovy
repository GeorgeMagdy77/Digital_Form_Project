package com.croatia.tactical.pipelines.helpers

static def get(url, authHeader, allowedCode = 200) {
  def responses = []
  def pageResponse = RestAPICall.get(url, authHeader, allowedCode)
  for (value in pageResponse.values) {
      responses << value
  }
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
