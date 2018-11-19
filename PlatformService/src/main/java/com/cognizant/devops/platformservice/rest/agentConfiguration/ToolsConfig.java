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
package com.cognizant.devops.platformservice.rest.agentConfiguration;

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
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformservice.rest.graph.ToolsConfigUtil;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Deprecated
@RestController
@RequestMapping("/admin/toolsConfig")
public class ToolsConfig {
	private static Logger log = LogManager.getLogger(ToolsConfig.class.getName());

	/**
	 * Avoid instantiations inside loops - Created JsonParser object outside of loop
	 * @param category
	 * @param toolName
	 * @return
	 */
	@RequestMapping(value = "/read", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadToolsConfig(@RequestParam(required = false) String category,
			@RequestParam(required = false) String toolName) {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> results = agentConfigDAL.getAgentConfigurations(toolName, category);
		JsonArray response = new JsonArray();
		JsonParser parser = new JsonParser();
		for(AgentConfig agentConfig : results){
			JsonObject data = new JsonObject();
			JsonObject agentJson = (JsonObject) parser.parse(agentConfig.getAgentJson());
			JsonArray userInputArray = (JsonArray)agentJson.get(NamedNodeAttribute.USER_INPUT);
			for(JsonElement input : userInputArray){
				data.add(input.getAsString(), agentJson.get(input.getAsString()));
			}
			data.addProperty("category", category);
			data.addProperty("toolName", toolName);
			response.add(data);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}
	
	/**
	 * Avoid instantiations inside loops - Created JsonParser object outside of loop
	 * @return
	 */
	@RequestMapping(value = "/readAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadAllToolsConfig() {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> results = agentConfigDAL.getAllAgentConfigurations();
		JsonArray response = new JsonArray();
		JsonParser parser = new JsonParser();
		for(AgentConfig agentConfig : results){
			JsonObject data = new JsonObject();
			JsonObject agentJson = (JsonObject) parser.parse(agentConfig.getAgentJson());
			JsonArray userInputArray = (JsonArray)agentJson.get(NamedNodeAttribute.USER_INPUT);
			for(JsonElement input : userInputArray){
				data.add(input.getAsString(), agentJson.get(input.getAsString()));
			}
			response.add(data);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
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
		boolean isDataUpdateSupported = false;
		String dataUpdateSupported = params.get("dataUpdateSupported");
		if(dataUpdateSupported != null){
			isDataUpdateSupported = Boolean.valueOf(params.get("dataUpdateSupported"));
		}		
		String uniqueKey = params.get("uniqueKey");
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
		try {
			agentId = Integer.valueOf(params.get("agentId"));
		} catch (Exception e) {
			log.error("agentId parameter is not present.", e);
		}
		JsonObject agentJsonData = new JsonObject();
		List<String> userInputs = new ArrayList<String>();
		for (Map.Entry<String, String> paramEntry : params.entrySet()) {
			agentJsonData.addProperty(paramEntry.getKey(), paramEntry.getValue());
			userInputs.add(paramEntry.getKey());
		}
		agentJsonData.add(NamedNodeAttribute.USER_INPUT, ToolsConfigUtil.createUserInputs(userInputs));
		agentJsonData.add("subscribe", ToolsConfigUtil.createSubscriberConfig(category, tool, agentId));
		agentJsonData.add("publish", ToolsConfigUtil.createPublisherConfig(category, tool));
		agentJsonData.add("mqConfig", ToolsConfigUtil.createMqConfig("iSight"));
		agentJsonData.add("communication", ToolsConfigUtil.createCommunicationConfig("REST"));
		//agentJsonData.addProperty("insightsTimeZone", ApplicationConfigProvider.getInstance().getInsightsTimeZone());
		int runSchedule = 30;
		try {
			runSchedule = Integer.valueOf(params.get("runSchedule"));
		} catch (Exception e) {
			log.error("runSchedule parameter is not present.", e);
		}
		agentJsonData.addProperty("runSchedule", runSchedule);

		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		boolean result = agentConfigDAL.saveAgentConfigurationData(agentId, tool, category, agentJsonData, isDataUpdateSupported, uniqueKey);
		if (result) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to update agent configurations");
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject deleteToolsConfig(@RequestParam String category, @RequestParam String toolName,
			@RequestParam(required = false) int agentId) {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		return PlatformServiceUtil
				.buildSuccessResponseWithData(agentConfigDAL.deleteAgentConfigurations(toolName, category, agentId));
	}

	
	/**
	 * Added the fix for Close the outermost stream ASAP CAST finding. closed fielReader promptly
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/toolsConfig", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getToolsConfig(){
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

	@RequestMapping(value = "/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonElement downloadToolsConfig(@RequestParam String category, @RequestParam String toolName,
			@RequestParam int agentId) {
		//JsonObject configJson = new JsonObject();
		if (category == null || toolName == null) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
		}
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		AgentConfig downloadAgentConfigurations = agentConfigDAL.downloadAgentConfigurations(toolName, category, agentId);
		JsonObject configJson = (JsonObject)new JsonParser().parse(downloadAgentConfigurations.getAgentJson());
		return AgentUtils.buildAgentConfig(configJson);
	}
}
