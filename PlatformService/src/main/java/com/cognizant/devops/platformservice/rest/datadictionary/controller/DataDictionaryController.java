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

package com.cognizant.devops.platformservice.rest.datadictionary.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformservice.rest.datadictionary.service.DataDictionaryService;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/datadictionary")
public class DataDictionaryController {

	private static Logger log = LogManager.getLogger(DataDictionaryController.class);
	@Autowired
	DataDictionaryService dataDictionaryService;

	@RequestMapping(value = "/getToolsAndCategories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getToolsAndCategories() {
		return dataDictionaryService.getToolsAndCategories();
	}

	@RequestMapping(value = "/getToolProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getToolProperties(@RequestParam String labelName, @RequestParam String categoryName) {
		return dataDictionaryService.getToolProperties(labelName, categoryName);
	}
	@RequestMapping(value = "/getToolsRelationshipAndProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getToolsRelationshipAndProperties(@RequestParam String startLabelName,
			@RequestParam String startToolCategory, @RequestParam String endLabelName,
			@RequestParam String endToolCatergory) {
		return dataDictionaryService.getToolsRelationshipAndProperties(startLabelName, startToolCategory, endLabelName,
				endToolCatergory);
	}
}
