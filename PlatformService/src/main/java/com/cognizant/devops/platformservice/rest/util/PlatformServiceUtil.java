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
package com.cognizant.devops.platformservice.rest.util;

import javax.ws.rs.core.MediaType;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class PlatformServiceUtil {
	private PlatformServiceUtil(){
		
	}
	
	public static JsonObject buildFailureResponse(String message){
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		return jsonResponse;
	}
	
	public static JsonObject buildSuccessResponseWithData(Object data){
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.add(PlatformServiceConstants.DATA, new Gson().toJsonTree(data));
		return jsonResponse;
	}
	
	public static JsonObject buildSuccessResponse(){
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		return jsonResponse;
	}
	
	public static ClientResponse publishConfigChanges(String host, int port, JsonObject requestJson) {
		WebResource resource = Client.create()
				.resource("http://"+host+":"+port+"/PlatformEngine/refreshAggregators");
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
				.type(MediaType.APPLICATION_JSON)
				.entity(requestJson.toString())
				.post(ClientResponse.class);
		return response;
	}
}
