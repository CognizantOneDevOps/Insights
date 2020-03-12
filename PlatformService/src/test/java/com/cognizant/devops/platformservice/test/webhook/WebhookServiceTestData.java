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
	
	String webhookname = "testNG_webhook_test_new";
	String toolName = "GIT";
	String labelDisplay = "SCM:GIT:DATA";
	String labelNewDisplay = "SCM:GIT_update:DATA";
	String dataformat = "json";
	String mqchannel = "IPW_testNG_webhook_test_new";
	Boolean subscribestatus = true;
	String webhookStarus = "{\"webhookName\":\"testNG_webhook_test_new\",\"statussubscribe\":false}";
	String responseTemplate = "head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at";
	String derivedOpsJson = "[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"GIT_65_WEBHOOK2\"},{\"wid\":-1,\"operationName\":\"dataEnrichment\",\"operationFields\":{\"sourceProperty\":\"message\",\"keyPattern\":\"-\",\"targetProperty\":\"messageEnrichExtractwb2\"},\"webhookName\":\"GIT_65_WEBHOOK2\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"commitTime\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ssXXX\"},\"webhookName\":\"GIT_65_WEBHOOK2\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"updated_at\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ss'Z'\"},\"webhookName\":\"GIT_65_WEBHOOK2\"}]";
	WebHookConfig updateWebhook = null;
	
	public JsonArray derivedOperationsArray = getderivedOperationsJSONArray();

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
		objectJson = parser.parse(webhookStarus).getAsJsonObject();
		return objectJson;
	}

}
