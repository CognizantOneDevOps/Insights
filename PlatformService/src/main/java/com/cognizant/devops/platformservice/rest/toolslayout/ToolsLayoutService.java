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
package com.cognizant.devops.platformservice.rest.toolslayout;

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

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformdal.tools.layout.ToolsLayout;
import com.cognizant.devops.platformdal.tools.layout.ToolsLayoutDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Deprecated
@RestController
@RequestMapping("/admin/toollayout")
public class ToolsLayoutService {
	private static Logger log = LogManager.getLogger(ToolsLayoutService.class.getName());

	@RequestMapping(value = "/read", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadToolsLayout(@RequestParam String category, @RequestParam String toolName) {
		ToolsLayoutDAL toolLayoutDal = new ToolsLayoutDAL();
		ToolsLayout toolLayout = toolLayoutDal.getToolLayout(toolName, category);
		if(toolLayout == null){
			return PlatformServiceUtil.buildSuccessResponse();
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(buildLayoutResponse(toolLayout));
	}
	
	@RequestMapping(value = "/readAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadAllToolsLayout() {
		ToolsLayoutDAL toolLayoutDal = new ToolsLayoutDAL();
		List<ToolsLayout> allToolLayouts = toolLayoutDal.getAllToolLayouts();
		JsonArray result = new JsonArray();
		for(ToolsLayout toolsLayout : allToolLayouts){
			result.add(buildLayoutResponse(toolsLayout));
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(result);
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
		String layoutSettings = params.get("layoutSettings");
		if (category == null || tool == null) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
		}
		if(layoutSettings == null || layoutSettings.trim().length() == 0){
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.LAYOUT_SETTINGS_MISSING);
		}
		JsonObject json = (JsonObject)new JsonParser().parse(layoutSettings);
		ToolsLayoutDAL toolLayoutDal = new ToolsLayoutDAL();
		boolean result = toolLayoutDal.saveToolLayout(tool, category, json);
		if (result) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to update agent configurations");
		}
	}
	
	private JsonObject buildLayoutResponse(ToolsLayout toolsLayout){
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("category", toolsLayout.getToolCategory());
		jsonObject.addProperty("toolName", toolsLayout.getToolName());
		jsonObject.add("layoutSettings", new JsonParser().parse(toolsLayout.getSettingsJson()));
		return jsonObject;
	}
}
