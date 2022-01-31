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
package com.cognizant.devops.platformservice.test.webhook;

import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WebhookServiceTestData {

	
	//String webhookJson = "{\"toolName\":\"GIT\",\"labelDisplay\":\"SCM:GIT:DATA\",\"webhookName\":\"git_test\",\"dataformat\":\"json\",\"mqchannel\":\"IPW_git_test\",\"responseTemplate\":\"new=new\",\"statussubscribe\":false,\"derivedOperations\":[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"timeins\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"newtime\",\"epochTime\":false,\"mappingTimeFormat\":\"newtime\"},\"webhookName\":\"git_test\"}],\"dynamicTemplate\":\"\",\"isUpdateRequired\":false,\"fieldUsedForUpdate\":\"\"}";
	String webhookJson ="{\r\n  \"toolName\":\"GIT\",\r\n  \"labelDisplay\":\"SCM:GIT:DATA\",\r\n  \"webhookName\":\"git_test\",\r\n  \"dataformat\":\"json\",\r\n  \"mqchannel\":\"IPW_git_test\",  \"responseTemplate\":\"head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at\",\r\n  \"statussubscribe\":false,\r\n  \"derivedOperations\":[\r\n    {\r\n      \"wid\":-1,\r\n      \"operationName\":\"insightsTimex\",\r\n      \"operationFields\":{\r\n        \"timeField\":\"pushed_at\",\r\n        \"epochTime\":true,\r\n        \"timeFormat\":\"\"\r\n      },\r\n      \"webhookName\":\"\"\r\n    }\r\n  ],\r\n  \"dynamicTemplate\":\"{\\n  \\\"commits\\\":[\\n    {\\n      \\\"id\\\":\\\"commitIdDY\\\",\\n      \\\"url\\\":\\\"commitURLDY\\\",\\n      \\\"timestamp\\\":\\\"commitTimeDY\\\"\\n    }\\n  ]\\n}\",\r\n  \"isUpdateRequired\":true,\r\n  \"fieldUsedForUpdate\":\"id\",\r\n  \"eventConfig\":\"\",\r\n  \"isEventProcessing\":false\r\n}";
	String webhookJsonIncorrectRT="{\"toolName\":\"GIT\",\"labelDisplay\":\"SCM:GIT:DATA\",\"webhookName\":\"git_new\",\"dataformat\":\"json\",\"mqchannel\":\"IPW_git_new\",\"responseTemplate\":\"head_commit.message\",\"statussubscribe\":false,\"derivedOperations\":[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"\"}],\"dynamicTemplate\":\"{\\n  \\\"commits\\\":[\\n    {\\n      \\\"id\\\":\\\"commitIdDY\\\",\\n      \\\"url\\\":\\\"commitURLDY\\\",\\n      \\\"timestamp\\\":\\\"commitTimeDY\\\"\\n    }\\n  ]\\n}\",\"isUpdateRequired\":false,\"fieldUsedForUpdate\":\"\"}";
	String webhookEmptyDT="{\r\n  \"toolName\":\"GIT\",\r\n  \"labelDisplay\":\"SCM:GIT:DATA\",\r\n  \"webhookName\":\"git_demo\",\r\n  \"dataformat\":\"json\",\r\n  \"mqchannel\":\"IPW_git_demo\",\r\n  \"responseTemplate\":\"head_commit.message=message, head_commit.pushed_at=pushed_at\",\r\n  \"statussubscribe\":false,\r\n  \"derivedOperations\":[\r\n    {\r\n      \"wid\":-1,\r\n      \"operationName\":\"insightsTimex\",\r\n      \"operationFields\":{\r\n        \"timeField\":\"pushed_at\",\r\n        \"epochTime\":true,\r\n        \"timeFormat\":\"\"\r\n      },\r\n      \"webhookName\":\"\"\r\n    }\r\n  ],\r\n  \"dynamicTemplate\":\"\",\r\n  \"isUpdateRequired\":false,\r\n  \"fieldUsedForUpdate\":\"\",\r\n  \"eventConfig\":\"\",\r\n  \"isEventProcessing\":false\r\n}";
	String webhookname = "git_new";
	String toolName = "GIT";
	String labelDisplay = "SCM:GIT:DATA";
	String labelNewDisplay = "SCM:GIT_update11:DATA";
	String dataformat = "json";
	String mqchannel = "IPW_testNG_webhook_test_new";
	Boolean subscribestatus = true;
	String webhookStatus = "{\"webhookName\":\"git_new\",\"statussubscribe\":false}";
	String responseTemplate = "head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at";
	String dynamicTemplate = "{\"commits\":[{\"id\":\"iD\",\"message\":\"message\",\"timestamp\":\"timestamp\",\"url\":\"URL\"}]}";
	String derivedOpsJson = "[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"testNG_webhook_test_new\"},{\"wid\":-1,\"operationName\":\"dataEnrichment\",\"operationFields\":{\"sourceProperty\":\"message\",\"keyPattern\":\"-\",\"targetProperty\":\"messageEnrichExtractwb2\"},\"webhookName\":\"testNG_webhook_test_new\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"commitTime\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ssXXX\"},\"webhookName\":\"testNG_webhook_test_new\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"updated_at\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ss'Z'\"},\"webhookName\":\"testNG_webhook_test_new\"}]";
	String fieldUsedForUpdate = "iD";
	Boolean isUpdateRequired = true;
	WebHookConfig updateWebhook = null;
	
	public JsonArray derivedOperationsArray = getderivedOperationsJSONArray();
	public JsonObject registeredWebhookJson = getregisteredWebhookJson();
	public JsonObject registeredWebhookJsonIncorrectRT = getregisteredWebhookJsonIncorrectRT();
	public JsonObject registeredWebhookJsonEmptyDTAndNodeupdate = getregisteredWebhookJsonEmptyDTAndNodeupdate();
	JsonArray array = new JsonArray();

	private JsonArray getderivedOperationsJSONArray() {
		JsonArray array = new JsonArray();
		array = JsonUtils.parseStringAsJsonArray(derivedOpsJson);
		return array;
	}

	public void setupdateWebhook(WebHookConfig updateWebhook) {
		this.updateWebhook = updateWebhook;
	}

	public JsonArray getupdateWebhookDerivedOperationsArray() {
		Gson gson = new Gson();
		JsonArray array = new JsonArray();		
		array = JsonUtils.parseStringAsJsonArray(gson.toJson(this.updateWebhook.getWebhookDerivedConfig()));
		return array;
	}

	public JsonObject getWebhookStatus() {
		JsonObject objectJson = new JsonObject();
		objectJson = JsonUtils.parseStringAsJsonObject(webhookStatus);
		return objectJson;
	}	
	
	private JsonObject getregisteredWebhookJson() {
		JsonObject json = JsonUtils.parseStringAsJsonObject(webhookJson);	
		return json;
	}

	public JsonObject getregisteredWebhookJsonUpdate() {
		JsonObject registeredWebhook = new JsonObject();
		registeredWebhook.addProperty("webhookName", webhookname);
		registeredWebhook.addProperty("toolName", toolName);
		registeredWebhook.addProperty("labelDisplay", labelNewDisplay);
		registeredWebhook.addProperty("dataformat", dataformat);
		registeredWebhook.addProperty("mqchannel", mqchannel);
		registeredWebhook.addProperty("statussubscribe", subscribestatus);
		registeredWebhook.addProperty("webhookStatus", webhookStatus);
		registeredWebhook.addProperty("responseTemplate","");
		registeredWebhook.addProperty("dynamicTemplate", dynamicTemplate);
		registeredWebhook.addProperty("isUpdateRequired", isUpdateRequired);
		registeredWebhook.addProperty("fieldUsedForUpdate", fieldUsedForUpdate);		
		registeredWebhook.addProperty("eventConfig", "");
		registeredWebhook.addProperty("isEventProcessing", false);
		if (this.updateWebhook != null) {
			array = getupdateWebhookDerivedOperationsArray();
			registeredWebhook.add("derivedOperations", array);
		}
		
		return registeredWebhook;
	}

	private JsonObject getregisteredWebhookJsonIncorrectRT() {
		JsonObject registeredWebhook= JsonUtils.parseStringAsJsonObject(webhookJsonIncorrectRT);
		return registeredWebhook;
	}

	private JsonObject getregisteredWebhookJsonEmptyDTAndNodeupdate() {
		JsonObject registeredWebhook =JsonUtils.parseStringAsJsonObject(webhookEmptyDT);			
		return registeredWebhook;
	}
}
