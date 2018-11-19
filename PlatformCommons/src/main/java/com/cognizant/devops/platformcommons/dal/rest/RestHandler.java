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
package com.cognizant.devops.platformcommons.dal.rest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class RestHandler {
	private static Logger log = LogManager.getLogger(RestHandler.class.getName());
		
	private RestHandler(){
		
	}
	
	static{
		try {
	         Field methodsField = HttpURLConnection.class.getDeclaredField("methods");
	         methodsField.setAccessible(true);
	         // get the methods field modifiers
	         Field modifiersField = Field.class.getDeclaredField("modifiers");
	         // bypass the "private" modifier 
	         modifiersField.setAccessible(true);
	         // remove the "final" modifier
	         modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);
	         /* valid HTTP methods */
	         String[] methods = {
	                    "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE", "PATCH"
	         };
	         // set the new methods - including patch
	         methodsField.set(null, methods);
	     } catch (Exception e) {
	    	 log.error(e);
	     }
	}
	
	public static ClientResponse doGet(String url, JsonElement requestJson, Map<String, String> headers){
		return getRequestBuilder(url, requestJson, headers).get(ClientResponse.class);
	}
	
	public static ClientResponse doPost(String url, JsonElement requestJson, Map<String, String> headers){
		return getRequestBuilder(url, requestJson, headers).post(ClientResponse.class);
	}
	
	public static ClientResponse doDelete(String url, JsonElement requestJson, Map<String, String> headers){
		return getRequestBuilder(url, requestJson, headers).delete(ClientResponse.class);
	}
	
	public static ClientResponse doPatch(String url, JsonElement requestJson, Map<String, String> headers){
		return getRequestBuilder(url, requestJson, headers).method("PATCH", ClientResponse.class);
	}

	private static Builder getRequestBuilder(String url, JsonElement requestJson, Map<String, String> headers) {
		Client client = Client.create();
		//client.addFilter(new LoggingFilter(System.out));
		WebResource resource = client.resource(url);
		Builder requestBuilder = resource.accept(MediaType.APPLICATION_JSON);
		if(headers != null && headers.size() > 0){
			for(Map.Entry<String, String> entry : headers.entrySet()){
				requestBuilder = requestBuilder.header(entry.getKey(), entry.getValue());
			}
		}
		requestBuilder = requestBuilder.type(MediaType.APPLICATION_JSON);
		if(requestJson != null){
			requestBuilder = requestBuilder.entity(requestJson.toString());
		}
		return requestBuilder;
	}
}
