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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.datadictionary.service.DataDictionaryServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/datadictionary")
public class DataDictionaryController {

	@Autowired
	DataDictionaryServiceImpl dataDictionaryService;

	@GetMapping(value = "/getToolsAndCategories", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getToolsAndCategories() {
		return dataDictionaryService.getToolsAndCategories();
	}

	@GetMapping(value = "/getToolProperties", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getToolProperties(@RequestParam String labelName, @RequestParam String categoryName) {
		JsonObject keysArrayJson;
		try {
		if (!ValidationUtils.checkLabelNameString(labelName)&&!ValidationUtils.checkLabelNameString(categoryName)) {
			keysArrayJson= dataDictionaryService.getToolProperties(labelName, categoryName);}
		else {
					throw new InsightsCustomException("Invalid LabelName or toolcategory.");
				}
		}
		catch(Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return keysArrayJson;
		 
	}

	@GetMapping(value = "/getToolsRelationshipAndProperties", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getToolsRelationshipAndProperties(@RequestParam String startLabelName,
			@RequestParam String startToolCategory, @RequestParam String endLabelName,
			@RequestParam String endToolCatergory) {
		JsonObject toolsRealtionJson;
		try {
		if (!ValidationUtils.checkLabelNameString(startLabelName)&&!ValidationUtils.checkLabelNameString(endLabelName)&&!ValidationUtils.checkLabelNameString(startToolCategory)&&!ValidationUtils.checkLabelNameString(endToolCatergory)) {
			toolsRealtionJson= dataDictionaryService.getToolsRelationshipAndProperties(startLabelName, startToolCategory, endLabelName,
				endToolCatergory);}
		else {
					throw new InsightsCustomException("Invalid LabelName or toolcategory.");
				}
		}
		catch(Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return toolsRealtionJson;
	}
}
