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
package com.cognizant.devops.platformcommons.dal.neo4j;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * 
 * 
 *
 */
public class DocumentParser {
	private static Logger log = LogManager.getLogger(DocumentParser.class);

	/**
	 * Method not used anymore
	 * 
	 * @param jsonElement
	 * @param nodeDataList
	 * @param nodeData
	 * @param property
	 */
	private void processGraphDBJson(JsonElement jsonElement, List<NodeData> nodeDataList, NodeData nodeData,
			String property) {
		if(jsonElement.isJsonNull()){
			log.error("Json Element is null");
		}else if(jsonElement.isJsonArray()){
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			for (JsonElement jsonArrayElement : jsonArray) {
				processGraphDBJson(jsonArrayElement, nodeDataList, nodeData, property);
			}
		} else if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				if (entry.getKey().equals("row")) {
					nodeData = new NodeData();
					nodeDataList.add(nodeData);
					boolean graph = jsonObject.has(ConfigOptions.GRAPH);
					if (graph) {
						JsonArray graphNodes = jsonObject.get(ConfigOptions.GRAPH).getAsJsonObject().get("nodes").getAsJsonArray();
						if (graphNodes.size() > 0) {
							JsonArray labels = graphNodes.get(0).getAsJsonObject().get("labels").getAsJsonArray();
							for (JsonElement label : labels) {
								nodeData.getLabels().add(label.getAsString());
							}
						}
					}
				}
				if (!entry.getKey().equals(ConfigOptions.GRAPH)) {
					processGraphDBJson(entry.getValue(), nodeDataList, nodeData, entry.getKey());
				}
			}
		} else if (jsonElement.isJsonPrimitive() && nodeData != null) {
			nodeData.setProperty(property, jsonElement.getAsString());
		}
	}

	/**
	 * Process GraphDB return data
	 * 
	 * @param jsonData
	 * @return GraphResponse
	 * @throws InsightsCustomException
	 */
	public GraphResponse processGraphDBNode(String jsonData) throws InsightsCustomException {
		try {
			GraphResponse response = new GraphResponse();
			List<NodeData> nodeDataList = response.getNodes();
			JsonElement parsedJson = new JsonParser().parse(jsonData);
			response.setJson(parsedJson.getAsJsonObject());
			processGraphDBJson(parsedJson, nodeDataList, null, "");
			return response;
		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}
	}

}
