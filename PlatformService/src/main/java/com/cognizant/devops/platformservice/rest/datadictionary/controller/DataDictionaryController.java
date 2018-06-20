package com.cognizant.devops.platformservice.rest.datadictionary.controller;

import org.apache.log4j.Logger;
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

	private static Logger log = Logger.getLogger(DataDictionaryController.class);
	@Autowired
	DataDictionaryService dataDictionaryService;

	@RequestMapping(value = "/getToolsAndCategories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getToolsAndCategories() {
		return dataDictionaryService.getToolsAndCategories();
	}

	@RequestMapping(value = "/getToolProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getToolProperties(@RequestParam String toolName, @RequestParam String categoryName) {
		return dataDictionaryService.getToolProperties(toolName, categoryName);
	}

	@RequestMapping(value = "/getToolsRelationshipAndProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getToolsRelationshipAndProperties(@RequestParam String startToolName,
			@RequestParam String startToolCategory, @RequestParam String endToolName,
			@RequestParam String endToolCatergory) {
		return dataDictionaryService.getToolsRelationshipAndProperties(startToolName, startToolCategory, endToolName,
				endToolCatergory);
	}
}
