/**
 * 
 */
package com.cognizant.devops.platformtests;

/**
 * @author 668284
 *
 */
public class MockRequest {
	
	private String path;
	
	private String response;

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}
	
}
