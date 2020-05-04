/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.dataDictionary;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataDictionaryTestData {

	String sourcelabel = "STORY_BKP";
	String sourceCat = "ALM";
	String destLabel = "GITINSIGHTS";
	String destCat = "SCM";
	String emptylabel = "EMPTY_LABEL";
	String webhookJson = "{\"toolName\":\"GIT\",\"labelDisplay\":\"SCM:GIT:DATA\",\"webhookName\":\"git_new\",\"dataformat\":\"json\",\"mqchannel\":\"IPW_git_new\",\"responseTemplate\":\"head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at\",\"statussubscribe\":false,\"derivedOperations\":[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"\"}],\"dynamicTemplate\":\"{\\n  \\\"commits\\\":[\\n    {\\n      \\\"id\\\":\\\"commitIdDY\\\",\\n      \\\"url\\\":\\\"commitURLDY\\\",\\n      \\\"timestamp\\\":\\\"commitTimeDY\\\"\\n    }\\n  ]\\n}\",\"isUpdateRequired\":true,\"fieldUsedForUpdate\":\"id\"}";
	JsonParser parser = new JsonParser();
	String toolName = "git";
	String agentVersion = "v5.2";
	String osversion = "Windows";
	String configDetails = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SCM.GIT.config\",\"agentCtrlQueue\":\"git_testng\"},\"publish\":{\"data\":\"SCM.GIT.DATA\",\"health\":\"SCM.GIT.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"sha\":\"commitId\",\"commit\":{\"message\":\"message\",\"author\":{\"name\":\"authorName\",\"date\":\"commitTime\"}}}},\"agentId\":\"git_testng\",\"enableBranches\":false,\"enableBrancheDeletion\":false,\"enableDataValidation\":true,\"toolCategory\":\"SCM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"enableValueArray\":false,\"useResponseTemplate\":true,\"auth\":\"base64\",\"runSchedule\":30,\"timeStampField\":\"commitTime\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"startFrom\":\"2019-03-01 15:46:33\",\"accessToken\":\"accesstoken\",\"getRepos\":\"https://api.github.com/users/USER_NAME/repos\",\"commitsBaseEndPoint\":\"https://api.github.com/repos/REPO_NAME/\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v5.2\",\"toolName\":\"GIT\"}";
	String trackingDetails = "";
	String agentId = "git_testng";
	String toolCategory = "SCM";
	public JsonObject registeredWebhookJson = getregisteredWebhookJson();

	private JsonObject getregisteredWebhookJson() {
		JsonObject json = (JsonObject) parser.parse(webhookJson);
		return json;
	}

}
