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
package com.cognizant.devops.platformservice.rest.datatagging;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformdal.entity.definition.EntityDefinition;
import com.cognizant.devops.platformdal.entity.definition.EntityDefinitionDAL;
import com.cognizant.devops.platformservice.rest.neo4j.GraphDBService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/dataTagging")
public class EntityDefinitionService {
	static Logger log = LogManager.getLogger(GraphDBService.class.getName());

	@RequestMapping(value = "/addEntityDefinition", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject addEntityDefinition(@RequestParam int rowId, @RequestParam String levelName, @RequestParam String entityName) {
		EntityDefinitionDAL entityDefinitionDAL = new EntityDefinitionDAL();
		boolean status = entityDefinitionDAL.saveEntityDefinition(rowId, levelName, entityName);
		if (status) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to add Entity Definition for the request");
		}
	}

	@RequestMapping(value = "/removeEntityDefinition", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject removeEntityDefinition(@RequestParam String levelName,
			@RequestParam String entityName) {
		EntityDefinitionDAL entityDefinitionDAL = new EntityDefinitionDAL();
		return PlatformServiceUtil
				.buildSuccessResponseWithData(entityDefinitionDAL.deleteEntityDefinition(levelName, entityName));
	}

	@RequestMapping(value = "/fetchEntityDefinition", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchEntityDefinition(@RequestParam String levelName,
			@RequestParam String entityName) {
		EntityDefinitionDAL entityDefinitionDAL = new EntityDefinitionDAL();
		EntityDefinition results = entityDefinitionDAL.getEntityDefinition(levelName, entityName);
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}

	@RequestMapping(value = "/fetchAllEntityDefinition", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchAllEntityDefinition() {
		EntityDefinitionDAL entityDefinitionDAL = new EntityDefinitionDAL();
		List<EntityDefinition> results = entityDefinitionDAL.fetchAllEntityDefination();
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}

	//Hierarchy Mapping
	/*@RequestMapping(value = "/addHierarchyMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject addHierarchyMapping(@RequestParam int rowId, @RequestParam String hierarchyName, @RequestParam String orgName, @RequestParam int orgId) {
		HierarchyMappingDAL hierarchyMappingDAL = new HierarchyMappingDAL();
		boolean status = hierarchyMappingDAL.saveHierarchyMapping(rowId, hierarchyName, orgName, orgId);
		if (status) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to add Entity Definition for the request");
		}
	}

	@RequestMapping(value = "/removeHierarchyMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject removeHierarchyMapping(@RequestParam String hierarchyName,
			@RequestParam String orgName) {
		HierarchyMappingDAL hierarchyMappingDAL = new HierarchyMappingDAL();
		return PlatformServiceUtil
				.buildSuccessResponseWithData(hierarchyMappingDAL.deleteHierarchyMapping(hierarchyName, orgName));
	}

	@RequestMapping(value = "/fetchHierarchyMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchHierarchyMapping(@RequestParam String hierarchyName) {
		HierarchyMappingDAL hierarchyMappingDAL = new HierarchyMappingDAL();
		List<String> hierarchyList = hierarchyMappingDAL.getHierarchyMapping(hierarchyName);
		return PlatformServiceUtil.buildSuccessResponseWithData(hierarchyList);
	}

	@RequestMapping(value = "/fetchAllHierarchyMapping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchAllHierarchyMapping() {
		HierarchyMappingDAL hierarchyMappingDAL = new HierarchyMappingDAL();
		List<HierarchyMapping> results = hierarchyMappingDAL.fetchAllHierarchyMapping();
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}*/

}
