/*********************************************************************************
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
 *******************************************************************************/

package com.cognizant.devops.platformservice.businessmapping.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.businessmapping.service.BusinessMappingService;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/businessmapping")
public class InsightsBusinessMapping {

	static Logger log = LogManager.getLogger(InsightsBusinessMapping.class.getName());
	@Autowired
	BusinessMappingService businessMappingService;

	@RequestMapping(value = "/getAllHierarchyDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getAllHierarchyDetails() {
		return businessMappingService.getAllHierarchyDetails();
	}

	@RequestMapping(value = "/getHierarchyProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getHierarchyProperties(@RequestParam String level1, @RequestParam String level2,
			@RequestParam String level3, @RequestParam String level4) throws InsightsCustomException {
		return businessMappingService.getHierarchyProperties(level1, level2, level3, level4);
	}
	
	@RequestMapping(value = "/saveToolsMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject saveToolsMappingLabel(@RequestBody String agentMappingJson) {
		log.debug(" info mapping agent JOSN "+agentMappingJson);
		return businessMappingService.saveToolsMappingLabel(agentMappingJson);
	}
	
	@RequestMapping(value = "/getToolsMapping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getToolsMappingLabel(@RequestParam String agentName) {
		log.debug(" Tool Name "+agentName);
		return businessMappingService.getToolsMappingLabel(agentName);
	}
	
	@RequestMapping(value = "/editToolsMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject editToolsMappingLabel(@RequestBody String agentMappingJson){
		log.debug(" Edit info mapping agent JOSN "+agentMappingJson);
		return businessMappingService.editToolsMappingLabel(agentMappingJson);
	}
	
	@RequestMapping(value = "/deleteToolsMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject deleteToolsMappingLabel(@RequestParam String uuid) {
		log.debug(" delete info mapping agent JOSN "+uuid);
		return businessMappingService.deleteToolsMappingLabel(uuid);
	}

}
