/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformmockserver;

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
	
	private  boolean isResponseJson;
	
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
	 * @return the isResponseJson
	 */
	public boolean isResponseJson() {
		return isResponseJson;
	}

	/**
	 * @param isResponseJson the isResponseJson to set
	 */
	public void setResponseJson(boolean isResponseJson) {
		this.isResponseJson = isResponseJson;
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
