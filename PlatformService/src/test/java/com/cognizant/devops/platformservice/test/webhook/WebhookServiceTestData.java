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

import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WebhookServiceTestData {

	JsonParser parser = new JsonParser();
	
	//String webhookJson = "{\"toolName\":\"GIT\",\"labelDisplay\":\"SCM:GIT:DATA\",\"webhookName\":\"git_test\",\"dataformat\":\"json\",\"mqchannel\":\"IPW_git_test\",\"responseTemplate\":\"new=new\",\"statussubscribe\":false,\"derivedOperations\":[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"timeins\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"newtime\",\"epochTime\":false,\"mappingTimeFormat\":\"newtime\"},\"webhookName\":\"git_test\"}],\"dynamicTemplate\":\"\",\"isUpdateRequired\":false,\"fieldUsedForUpdate\":\"\"}";
    String webhookJson ="{\"toolName\":\"GIT\",\"labelDisplay\":\"SCM:GIT:DATA\",\"webhookName\":\"git_new\",\"dataformat\":\"json\",\"mqchannel\":\"IPW_git_new\",\"responseTemplate\":\"head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at\",\"statussubscribe\":false,\"derivedOperations\":[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"\"}],\"dynamicTemplate\":\"{\\n  \\\"commits\\\":[\\n    {\\n      \\\"id\\\":\\\"commitIdDY\\\",\\n      \\\"url\\\":\\\"commitURLDY\\\",\\n      \\\"timestamp\\\":\\\"commitTimeDY\\\"\\n    }\\n  ]\\n}\",\"isUpdateRequired\":true,\"fieldUsedForUpdate\":\"id\"}";
	String webhookJsonIncorrectRT="{\"toolName\":\"GIT\",\"labelDisplay\":\"SCM:GIT:DATA\",\"webhookName\":\"git_new\",\"dataformat\":\"json\",\"mqchannel\":\"IPW_git_new\",\"responseTemplate\":\"head_commit.message\",\"statussubscribe\":false,\"derivedOperations\":[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"\"}],\"dynamicTemplate\":\"{\\n  \\\"commits\\\":[\\n    {\\n      \\\"id\\\":\\\"commitIdDY\\\",\\n      \\\"url\\\":\\\"commitURLDY\\\",\\n      \\\"timestamp\\\":\\\"commitTimeDY\\\"\\n    }\\n  ]\\n}\",\"isUpdateRequired\":false,\"fieldUsedForUpdate\":\"\"}";
    String webhookEmptyDT="{\"toolName\":\"GIT\",\"labelDisplay\":\"SCM:GIT:DATA\",\"webhookName\":\"git_demo\",\"dataformat\":\"json\",\"mqchannel\":\"IPW_git_demo\",\"responseTemplate\":\"head_commit.message=message,\\nhead_commit.pushed_at=pushed_at\",\"statussubscribe\":false,\"derivedOperations\":[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"\"}],\"dynamicTemplate\":\"\",\"isUpdateRequired\":false,\"fieldUsedForUpdate\":\"\"}";
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
		array = parser.parse(derivedOpsJson).getAsJsonArray();
		return array;
	}

	public void setupdateWebhook(WebHookConfig updateWebhook) {
		this.updateWebhook = updateWebhook;
	}

	public JsonArray getupdateWebhookDerivedOperationsArray() {
		Gson gson = new Gson();
		JsonArray array = new JsonArray();		
		array = (JsonArray) parser.parse(gson.toJson(this.updateWebhook.getWebhookDerivedConfig()));
		return array;
	}

	public JsonObject getWebhookStatus() {
		JsonObject objectJson = new JsonObject();
		objectJson = parser.parse(webhookStatus).getAsJsonObject();
		return objectJson;
	}	
	
	private JsonObject getregisteredWebhookJson() {
		JsonObject json = (JsonObject) parser.parse(webhookJson);	
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
		if (this.updateWebhook != null) {
			array = getupdateWebhookDerivedOperationsArray();
			registeredWebhook.add("derivedOperations", array);
		}
		
		return registeredWebhook;
	}

	private JsonObject getregisteredWebhookJsonIncorrectRT() {
		JsonObject registeredWebhook= (JsonObject) parser.parse(webhookJsonIncorrectRT);
		return registeredWebhook;
	}

	private JsonObject getregisteredWebhookJsonEmptyDTAndNodeupdate() {
		JsonObject registeredWebhook = (JsonObject) parser.parse(webhookEmptyDT);			
		return registeredWebhook;
	}
}
