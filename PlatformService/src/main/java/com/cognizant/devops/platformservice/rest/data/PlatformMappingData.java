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
package com.cognizant.devops.platformservice.rest.data;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.tools.layout.ToolsLayoutDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/mappingdata")
public class PlatformMappingData {
	private static Logger log = LogManager.getLogger(PlatformMappingData.class.getName());

	@RequestMapping(value = "/tools", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getToolsList() {
		ToolsLayoutDAL toolLayoutDal = new ToolsLayoutDAL();
		List<String> allToolsName = toolLayoutDal.getDistinctToolNames();
		return PlatformServiceUtil.buildSuccessResponseWithData(allToolsName);
	}
	
	@RequestMapping(value = "/toolsCategory", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getToolsCatList(@RequestParam(required = false) String toolName) {
		ToolsLayoutDAL toolLayoutDal = new ToolsLayoutDAL();
		List<String> allCatName = toolLayoutDal.getToolCategoryNames(toolName);
		return PlatformServiceUtil.buildSuccessResponseWithData(allCatName);
	}
	
	@RequestMapping(value = "/toolsField", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadToolsField(@RequestParam(required = false) String toolName) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (n:" +toolName+ ":DATA) return n limit 1";
		try { 
			GraphResponse response = dbHandler.executeCypherQuery(query);
			return PlatformServiceUtil.buildSuccessResponseWithData(response);
		} catch (GraphDBException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
	}
	//match (n:GIT) return distinct(n.git_RepoName) 
	
	@RequestMapping(value = "/toolsFieldValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadToolsFieldValue(@RequestParam(required = false) String toolName, 
			@RequestParam(required = false) String fieldName) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (n:" +toolName + ") return distinct(n." +fieldName +")";
		try { 
			GraphResponse response = dbHandler.executeCypherQuery(query);
			return PlatformServiceUtil.buildSuccessResponseWithData(response);
		} catch (GraphDBException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
	}
}
