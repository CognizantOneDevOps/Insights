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
	
	/**
	 * Method not used anymore
	 * 
	 * @param jsonElement
	 * @param nodeDataList
	 * @param nodeData
	 * @param property
	 */
	private void processGraphDBJson(JsonElement jsonElement, List<NodeData> nodeDataList,  NodeData nodeData, String property){
		//{"results":[{"columns":["n"],"data":[{"row":[{"name":"Test me"}]},{"row":[{"name":"Test me"}]}]}],"errors":[]}
		if(jsonElement.isJsonNull()){
			return;
		}else if(jsonElement.isJsonArray()){
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			for(JsonElement jsonArrayElement : jsonArray){
				processGraphDBJson(jsonArrayElement, nodeDataList, nodeData, property);
			}
		}else if(jsonElement.isJsonObject()){
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()){
				if(entry.getKey().equals("row")){
					nodeData = new NodeData();
					nodeDataList.add(nodeData);
					boolean graph = jsonObject.has("graph");
					if(graph){
						JsonArray graphNodes = jsonObject.get("graph").getAsJsonObject().get("nodes").getAsJsonArray();
						if(graphNodes.size() > 0){
							JsonArray labels = graphNodes.get(0).getAsJsonObject().get("labels").getAsJsonArray();
							for(JsonElement label : labels){
								nodeData.getLabels().add(label.getAsString());
							}
						}
					}
				}
				if(!entry.getKey().equals("graph")){
					processGraphDBJson(entry.getValue(), nodeDataList, nodeData, entry.getKey());
				}
			}
		}else if(jsonElement.isJsonPrimitive() && nodeData != null){
			nodeData.setProperty(property, jsonElement.getAsString());
		}
	}
	
	/**
	 * Process GraphDB return data
	 * 
	 * @param jsonData
	 * @return GraphResponse
	 */
	public GraphResponse processGraphDBNode(String jsonData){
		GraphResponse response = new GraphResponse();
		List<NodeData> nodeDataList = response.getNodes();
		JsonElement parsedJson = new JsonParser().parse(jsonData);
		response.setJson(parsedJson.getAsJsonObject());
		processGraphDBJson(parsedJson, nodeDataList, null, "");
		return response;
	}
	
	/**
	 * 
	 * 
	 * @param jsonData
	 * @param xpath
	 * @return JsonElement
	 */
	
	/* public JsonElement parseJsonResponse(JsonElement jsonData, String xpath){
		if(xpath.contains(".")){
			String[] split = xpath.split("\\.");
			JsonElement jsonElement = null;
			if(jsonData.isJsonArray()){
				JsonArray asJsonArray = jsonData.getAsJsonArray();
				if(asJsonArray.size() > 0){
					JsonArray jsonDataArray = new JsonArray();
					for(JsonElement item : jsonData.getAsJsonArray()){
						jsonDataArray.add(parseJsonResponse(item, xpath));
					}
					return jsonDataArray;
				}else{
					return new JsonPrimitive("");
				}
			}else{
				jsonElement = jsonData.getAsJsonObject().get(split[0]);
			}
			xpath = xpath.replace(split[0]+".", "");
			return parseJsonResponse(jsonElement, xpath);
		}else{
			//Base case. Return the object.
			if(jsonData.isJsonObject()){
				JsonObject jsonObject = jsonData.getAsJsonObject();
				JsonElement jsonElement = jsonObject.get(xpath);
				return jsonElement;
			}else if(jsonData.isJsonArray()){
				JsonArray asJsonArray = jsonData.getAsJsonArray();
				if(asJsonArray.size() > 0){
					JsonArray jsonDataArray = new JsonArray();
					for(JsonElement item : jsonData.getAsJsonArray()){
						jsonDataArray.add(parseJsonResponse(item, xpath));
					}
					return jsonDataArray;
				}else{
					return new JsonPrimitive("");
				}
			}
		}
		return null;
	}*/

	/*public static void main(String[] args){
		StringBuilder content = new StringBuilder();
		try {
			BufferedReader read = new BufferedReader(new FileReader(new File("D:\\VishalGanjare\\DevOpsPlatform\\Thinking\\Rundeck\\graphResponse.txt")));
			String line = "";
			while((line = read.readLine()) != null){
				content.append(line);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		JsonParser parser = new JsonParser();
		JsonElement jsonData = parser.parse(content.toString());
		DocumentParser docParser = new DocumentParser();
		List<NodeData> nodeDataList = new ArrayList<NodeData>();
		docParser.processGraphDBJson(jsonData, nodeDataList, null, "");
		//JsonElement parseJsonResponse = docParser.parseJsonResponse(jsonData, "executions.job.href");
		//System.out.println(parseJsonResponse.toString());
	}*/
	
	/*public static void main(String[] args){
		DocumentParser documentParser = new DocumentParser();
		JsonParser jsonParser = new JsonParser();
		String json = "{\"results\":[{\"columns\":[\"n\"],\"data\":[{\"row\":[{\"name\":\"Test me\"}]},{\"row\":[{\"name\":\"Test me\"}]},{\"row\":[{\"name\":\"Vishal1\",\"company\":\"cts1\"}]},{\"row\":[{\"name\":\"Vishal\",\"company\":\"cts\"}]}]}],\"errors\":[]}";
		JsonElement jsonElement = jsonParser.parse(json);
		List<NodeData> nodeDataList = new ArrayList<NodeData>();
		documentParser.processGraphDBNode(jsonElement);
	}*/
}
