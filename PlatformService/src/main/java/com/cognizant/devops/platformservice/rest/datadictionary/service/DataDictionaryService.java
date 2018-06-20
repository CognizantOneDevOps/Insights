package com.cognizant.devops.platformservice.rest.datadictionary.service;

import com.google.gson.JsonObject;

public interface DataDictionaryService {
	public JsonObject getToolsAndCategories();
	public JsonObject getToolProperties(String toolName, String categoryName);
	public JsonObject getToolsRelationshipAndProperties(String startToolName, String startToolCategory, String endToolName, String endToolCatergory);
}
