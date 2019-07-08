package com.cognizant.devops.platformauditing.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface InsightsAudit {

	public String getAssetHistory(String primaryId);
	
	public String getAssetInfo(String assetId);

	public String getAllAssets(String startDate, String endDate, String toolName);

	public boolean insertToolData(JsonObject input);

	public boolean insertChangeLogData(JsonObject input);

	public boolean insertJiraNode(JsonObject input, JsonArray changelogArray);

	public JsonObject getProcessFlow();

}
