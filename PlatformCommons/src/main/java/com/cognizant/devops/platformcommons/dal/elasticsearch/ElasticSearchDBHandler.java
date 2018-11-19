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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ElasticSearchDBHandler {

	private static final Logger log = LogManager.getLogger(ElasticSearchDBHandler.class);

	public String search(String url) {
		ClientResponse response = null;
		String result = "";
		try {
			WebResource resource = Client.create().resource(url);
			response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
					.get(ClientResponse.class);
			result = response.getEntity(String.class);
		} catch (Exception e) {
			log.error("Exception while getting data from ES", e);
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return result;
	}

	public String cloneVisualizations(String url, JsonObject data) {
		WebResource resource = Client.create().resource(url);
		resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).put(data.toString());
		return "Done";
	}

	public JsonObject loadVisualization(String sourceESUrl) {

		JsonObject result = new JsonObject();
		ClientResponse response = null;

		try {
			WebResource resource = Client.create().resource(sourceESUrl);
			response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
					.get(ClientResponse.class);
			result = new JsonParser().parse(response.getEntity(String.class)).getAsJsonObject();
		} catch (Exception e) {
			log.error("Exception while getting data from ES", e);
		} finally {
			if (response != null) {
				response.close();
			}
		}

		return result;
	}

	public JsonObject queryES(String sourceESUrl, String query) throws Exception {

		ClientResponse response = null;
		JsonObject data = null;
		try {
			WebResource resource = Client.create().resource(sourceESUrl);
			response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
					.post(ClientResponse.class, query);
			if (!((response.getStatus() == 200) || (response.getStatus() == 201) || (response.getStatus() == 404))) {
				throw new Exception("Failed to get response from ElasticSeach for query - " + query
						+ "-- HTTP response code -" + response.getStatus());
			}

			data = new JsonParser().parse(response.getEntity(String.class)).getAsJsonObject();
		} catch (Exception e) {
			log.error("Exception while getting data from ES for query - " + query, e);
			throw new Exception(e.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return data;
	}

	public static void main(String[] args) throws Exception {
		ElasticSearchDBHandler elasticSearchDBHandler = new ElasticSearchDBHandler();

		String esQuery = "{  \"query\": {    \"filtered\": {      \"query\": {        \"term\": {          \"vector\": \"build\"        }      },      \"filter\": {        \"bool\": {          \"must\": [            {\"term\":{\"aggregatedResult\":true}},{              \"range\": {                \"resultTime\": {                  \"gte\": \"1405722616070\",                  \"lte\": \"1605722616070\",                  \"format\": \"epoch_millis\"                }              }            }          ]        }      }    }  }},\"sort\": { \"resultTime\": { \"order\": \"desc\" }}";
		JsonObject jsonObj = elasticSearchDBHandler
				.queryES("http://127.0.0.1:9200/spark-jobs-conf/results/_search?size=2&filter_path=hits", esQuery);
		JsonObject rootObj = jsonObj.get("hits").getAsJsonObject();

		JsonArray array = rootObj.get("hits").getAsJsonArray();
		System.out.println(jsonObj);
		for (JsonElement element : array) {
			JsonObject output = element.getAsJsonObject().get("_source").getAsJsonObject();

			System.out.println(output.get("result"));
			System.out.println(output.get("aggregatedResult"));
			System.out.println(output.get("resultTime"));
			System.out.println(output.get("schedule"));
			System.out.println(output.get("groupByName"));
			System.out.println(output.get("vector"));
			System.out.println(output.get("name"));
			System.out.println(output.get("action"));

			// resultMap.put(kpiDefinition.getName(),
			// Long.valueOf(element.getAsJsonObject().get("avg_duration").getAsJsonObject().get("value").getAsLong()));
			/*
			 * Long avg =
			 * Long.valueOf(output.get("avg_output").getAsJsonObject().get("value").
			 * getAsLong()); String groupByValue = output.get("key").getAsString();
			 */

		}

		// System.out.println(str);
		/*
		 * JsonObject visualization = elasticSearchDBHandler.loadVisualization(
		 * "http://localhost:9200/.kibana/dashboard/_search?q=*&size=10000&from=0");
		 * String baseUrl = "http://localhost:9200/.kibana/dashboard/"; JsonArray
		 * asJsonArray =
		 * visualization.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		 * for(JsonElement vizEle : asJsonArray){ JsonObject viz =
		 * vizEle.getAsJsonObject(); elasticSearchDBHandler.cloneVisualizations(baseUrl
		 * + viz.get("_id").getAsString(), viz.get("_source").getAsJsonObject()); }
		 */

		/*
		 * for(Map.Entry<String, JsonElement> confEntry : jsonObj.entrySet()){
		 * JsonElement value = confEntry.getValue();
		 * System.out.println(confEntry.getKey()); System.out.println(value.toString());
		 * }
		 */
		/*
		 * JsonArray asJsonArray = jsonObj.get("aggregations"); for(JsonElement vizEle :
		 * asJsonArray){ JsonObject viz = vizEle.getAsJsonObject();
		 * System.out.println(viz.get("avg_jen_Duration")); }
		 */
	}
}
