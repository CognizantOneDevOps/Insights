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
package com.cognizant.devops.platformservice.rest.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.agent.AgentUtils;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.constants.NamedNodeAttribute;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/admin/tools")
public class ToolsConfigurationService {
	private static Logger log = LogManager.getLogger(ToolsConfigurationService.class.getName());

	/**
	 * Avoid instantiations inside loops - Created JsonParser object outside of loop
	 * Pending for data object, but this is needed
	 * @param category
	 * @param toolName
	 * @return
	 */
	@RequestMapping(value = "/read", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadToolsConfig(@RequestParam(required = false) String category,
			@RequestParam(required = false) String toolName) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		StringBuffer label = new StringBuffer(":AGENT");
		if (toolName != null && toolName.trim().length() > 0) {
			label.append(":").append(toolName);
		}
		if (category != null && category.trim().length() > 0) {
			label.append(":").append(category);
		}
		String query = "MATCH (n" + label.toString() + ") return n";
		try {
			GraphResponse response = dbHandler.executeCypherQuery(query);
			JsonObject responseData = new JsonObject();
			JsonArray nodes = new JsonArray();
			responseData.add("data", nodes);
			JsonParser parser = new JsonParser();
			for (NodeData node : response.getNodes()) {
				String userInputStr = node.getProperty(NamedNodeAttribute.USER_INPUT);
				JsonArray userInputArray = parser.parse(userInputStr).getAsJsonArray();
				JsonObject data = new JsonObject();
				for (JsonElement input : userInputArray) {
					data.addProperty(input.getAsString(), node.getProperty(input.getAsString()));
				}
				nodes.add(data);
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(responseData);
		} catch (GraphDBException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
		// return
		// PlatformServiceUtil.buildFailureResponse(ErrorMessage.UNEXPECTED_ERROR);
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject deleteToolsConfig(@RequestParam String category,
														@RequestParam String toolName,
														@RequestParam(required = false) String agentId) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		StringBuffer label = new StringBuffer(":AGENT");
		label.append(":").append(toolName);
		label.append(":").append(category);
		String query = null;
		if(agentId != null){
			query = "MATCH (n" + label.toString() + " {agentId : '"+agentId+"' }) detach delete n";
		}else{
			query = "MATCH (n" + label.toString() + ") detach delete n";
		}
		try {
			GraphResponse response = dbHandler.executeCypherQuery(query);
			return PlatformServiceUtil.buildSuccessResponse();
		} catch (GraphDBException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
		// return
		// PlatformServiceUtil.buildFailureResponse(ErrorMessage.UNEXPECTED_ERROR);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject updateToolsConfig(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			if (entry.getValue().length > 0) {
				params.put(entry.getKey(), entry.getValue()[0]);
			}
		}
		String category = params.get("category");
		String tool = params.get("toolName");
		if (category == null || tool == null) {
			Scanner s;
			try {
				ServletInputStream inputStream = request.getInputStream();
				s = new Scanner(inputStream, "UTF-8");
				s.useDelimiter("\\A");
				String jsonStr = s.hasNext() ? s.next() : "";
				JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
				category = json.get("category").getAsString();
				tool = json.get("toolName").getAsString();
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					params.put(entry.getKey(), entry.getValue().getAsString());
				}
				inputStream.close();
				s.close();
			} catch (Exception e1) {
				log.error(e1);
				return PlatformServiceUtil.buildFailureResponse(ErrorMessage.UNEXPECTED_ERROR);
			}
		}

		if (category == null || tool == null) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
		}
		int agentId = 1;
		try{
			agentId = Integer.valueOf(params.get("agentId"));
		}catch(Exception e){
			log.error(e);
		}
		StringBuffer cypher = new StringBuffer();
		cypher.append("MERGE (n:AGENT:").append(category).append(":").append(tool);
		cypher.append(" { ");
		cypher.append("category: '").append(category).append("',");
		cypher.append("toolName: '").append(tool).append("',");
		cypher.append("agentId: ").append(agentId);
		cypher.append(" }) ");
		cypher.append(" SET ");
		List<String> userInputs = new ArrayList<String>();
		for (Map.Entry<String, String> paramEntry : params.entrySet()) {
			cypher.append("n." + paramEntry.getKey()).append(" = '").append(paramEntry.getValue()).append("',");
			userInputs.add(paramEntry.getKey());
		}
		cypher.append("n.").append(NamedNodeAttribute.USER_INPUT).append(" = '")
				.append(ToolsConfigUtil.createUserInputs(userInputs).toString()).append("',");
		// cypher.append("n.userInputs =
		// '").append(ToolsConfigUtil.createUserInputs(userInputs).toString()).append("',");
		cypher.append("n.subscribe = '").append(ToolsConfigUtil.createSubscriberConfig(category, tool, 1).toString())
				.append("',");
		cypher.append("n.publish = '").append(ToolsConfigUtil.createPublisherConfig(category, tool).toString())
				.append("',");
		cypher.append("n.mqConfig = '").append(ToolsConfigUtil.createMqConfig("iSight").toString()).append("',");
		cypher.append("n.communication = '").append(ToolsConfigUtil.createCommunicationConfig("REST").toString())
				.append("',");
		cypher.append("n.runSchedule = 30").append(",");
		cypher.append("n.engine_ack = false");
		cypher.append(" return n ");

		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			GraphResponse response = dbHandler.executeCypherQuery(cypher.toString());
			if (response.getNodes().size() > 0) {
				// String localAddr = request.getLocalAddr();
				// int localPort = request.getLocalPort();
				// PlatformServiceUtil.publishConfigChanges(localAddr,
				// localPort, loadConfig(category, tool));
				return PlatformServiceUtil.buildSuccessResponse();
			}
		} catch (GraphDBException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
		return PlatformServiceUtil.buildFailureResponse(ErrorMessage.UNEXPECTED_ERROR);
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String downloadToolsConfig(@RequestParam String category, @RequestParam String tool) {
		JsonObject configJson = new JsonObject();
		if (category == null || tool == null) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED)
					.toString();
		}
		JsonObject configResp = loadConfig(category, tool);
		if( null != configResp.get(PlatformServiceConstants.DATA) ){
			configJson = configResp.get(PlatformServiceConstants.DATA).getAsJsonObject();
		}else{
			configJson = configResp;
		}
		return new GsonBuilder().setPrettyPrinting().create().toJson(configJson);
		// return loadConfig(category, tool);
	}

	/**
	 * Added the fix for Close the outermost stream ASAP CAST finding. closed fielReader promptly
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/toolsConfig", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getToolsConfig() {
		File configFile = new File(ConfigOptions.TOOLS_CONFIG_FILE_RESOLVED_PATH);
		JsonObject toolsJsonObj = new JsonObject();
		if (!configFile.exists()) {
			URL resource = ApplicationConfigCache.class.getClassLoader().getResource(ConfigOptions.TOOLS_CONFIG_FILE);
			if (resource != null) {
				configFile = new File(resource.getFile());
			}
		}
		try(FileReader fileReader = new FileReader(configFile)){
			JsonElement jsonElement = new JsonParser().parse(fileReader);
			toolsJsonObj = jsonElement.getAsJsonObject();
		} catch (FileNotFoundException e) {
			log.error("Unable to find tools config file: "+ConfigOptions.TOOLS_CONFIG_FILE_RESOLVED_PATH, e);
		} catch (IOException e) {
			log.error(e);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(toolsJsonObj);
	}

	private JsonObject loadConfig(String category, String tool) {
		try {
			JsonObject config = AgentUtils.buildAgentConfig(category, tool);
			return PlatformServiceUtil.buildSuccessResponseWithData(config);
		} catch (GraphDBException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
	}
}
