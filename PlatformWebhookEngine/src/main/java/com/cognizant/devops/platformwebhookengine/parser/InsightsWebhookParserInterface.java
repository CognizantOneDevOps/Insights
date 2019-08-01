package com.cognizant.devops.platformwebhookengine.parser;

import java.util.List;

import com.google.gson.JsonObject;



public interface InsightsWebhookParserInterface {

	public List<JsonObject> parseToolData(String responseTemplate,String toolData);
}
