/**
 * 
 */
package com.cognizant.devops.platformtests;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

/**
 * @author 668284
 *
 */
public class MockRequest {
	
	private String path;
	
	private List<JsonObject> response;
	
	private Map<String,String> parameters;  

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
	public List<JsonObject> getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(List<JsonObject> response) {
		this.response = response;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
}
