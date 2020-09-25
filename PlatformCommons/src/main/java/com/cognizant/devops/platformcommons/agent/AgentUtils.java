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
package com.cognizant.devops.platformcommons.agent;

import java.util.Map;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class AgentUtils {
	private AgentUtils(){
		
	}
	public static JsonObject buildAgentConfig(String category, String tool) throws InsightsCustomException{
		StringBuffer cypher = new StringBuffer();
		cypher.append("MATCH (n:AGENT:").append(category).append(":").append(tool).append(") return n");
		
		GraphDBHandler dbHandler = new GraphDBHandler();
		GraphResponse response = dbHandler.executeCypherQuery(cypher.toString());
		if(response.getNodes().size() > 0){
			JsonObject config = buildAgentConfigFromNodeData(response.getNodes().get(0));
			return config;
		}
		return new JsonObject();
	}

	public static JsonObject buildAgentConfigFromNodeData(NodeData nodeData) {
		Map<String, String> propertyMap = nodeData.getPropertyMap();
		JsonObject config = new JsonObject();
		JsonParser parser = new JsonParser();
		for(Map.Entry<String, String> entry : propertyMap.entrySet()){
			try{
				config.add(entry.getKey(), parser.parse(entry.getValue()));
			}catch(JsonSyntaxException ex){
				config.addProperty(entry.getKey(), entry.getValue());
			}
		}
		config.remove("userInput");
		config.remove("engine_ack");
		return config;
	}
	
	public static JsonObject buildAgentConfig(JsonObject config) {
		config.remove("agentId");
		config.remove("category");
		config.remove("toolName");
		config.remove("userInput");
		config.remove("selectedAuthMtd");
		config.remove("uniqueKey");
		config.remove("dataUpdateSupported");
		return config;
	}
}
