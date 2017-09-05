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
package com.cognizant.devops.platformcommons.dal.elasticsearch;

import javax.ws.rs.core.MediaType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ElasticSearchDBHandler {
	public String search(String url){
		WebResource resource = Client.create()
				.resource(url);
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
				.type(MediaType.APPLICATION_JSON)
				.get(ClientResponse.class);
		return response.getEntity(String.class);
	}
	
	public String cloneVisualizations(String url, JsonObject data){
		WebResource resource = Client.create()
				.resource(url);
		resource.accept( MediaType.APPLICATION_JSON )
				.type(MediaType.APPLICATION_JSON).put(data.toString());
		return "Done";
	}
	
	public JsonObject loadVisualization(String sourceESUrl){
		WebResource resource = Client.create()
				.resource(sourceESUrl);
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
				.type(MediaType.APPLICATION_JSON)
				.get(ClientResponse.class);
		return new JsonParser().parse(response.getEntity(String.class)).getAsJsonObject();
	}
	
	public static void main(String[] args){
		ElasticSearchDBHandler elasticSearchDBHandler = new ElasticSearchDBHandler();
		JsonObject visualization = elasticSearchDBHandler.loadVisualization("http://localhost:9200/.kibana/dashboard/_search?q=*&size=10000&from=0");
		String baseUrl = "http://localhost:9200/.kibana/dashboard/";
		JsonArray asJsonArray = visualization.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		for(JsonElement vizEle : asJsonArray){
			JsonObject viz = vizEle.getAsJsonObject();
			elasticSearchDBHandler.cloneVisualizations(baseUrl + viz.get("_id").getAsString(), viz.get("_source").getAsJsonObject());
		}
	}
}
