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

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;

public class WebhookServiceTestData extends AbstractTestNGSpringContextTests{
	
	String webhookname = "git_test";
	String toolName = "GIT";
	String labelDisplay = "SCM:GIT:DATA";
	String labelNewDisplay = "SCM:GIT_update11:DATA";
	String dataformat = "json";
	String mqchannel = "IPW_testNG_webhook_test_new";
	Boolean subscribestatus = true;
	String responseTemplate = "head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at";
	String fieldUsedForUpdate = "iD";
	Boolean isUpdateRequired = true;
	WebHookConfig updateWebhook = null;
	String webhookJson ="{\r\n  \"toolName\":\"GIT\",\r\n  \"labelDisplay\":\"SCM:GIT:DATA\",\r\n  \"webhookName\":\"git_test\",\r\n  \"dataformat\":\"json\",\r\n  \"mqchannel\":\"IPW_git_test\",  \"responseTemplate\":\"head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at\",\r\n  \"statussubscribe\":false,\r\n  \"derivedOperations\":[\r\n    {\r\n      \"wid\":-1,\r\n      \"operationName\":\"insightsTimex\",\r\n      \"operationFields\":{\r\n        \"timeField\":\"pushed_at\",\r\n        \"epochTime\":true,\r\n        \"timeFormat\":\"\"\r\n      },\r\n      \"webhookName\":\"\"\r\n    }\r\n  ],\r\n  \"dynamicTemplate\":\"{\\n  \\\"commits\\\":[\\n    {\\n      \\\"id\\\":\\\"commitIdDY\\\",\\n      \\\"url\\\":\\\"commitURLDY\\\",\\n      \\\"timestamp\\\":\\\"commitTimeDY\\\"\\n    }\\n  ]\\n}\",\r\n  \"isUpdateRequired\":true,\r\n  \"fieldUsedForUpdate\":\"id\",\r\n  \"eventConfig\":\"\",\r\n  \"isEventProcessing\":false\r\n}";
	String webhookUpdateJson ="{\r\n  \"toolName\":\"GIT\",\r\n  \"labelDisplay\":\"SCM:GIT_update11:DATA\",\r\n  \"webhookName\":\"git_test\",\r\n  \"dataformat\":\"json\",\r\n  \"mqchannel\":\"IPW_git_test\",  \"responseTemplate\":\"head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at\",\r\n  \"statussubscribe\":false,\r\n  \"derivedOperations\":[\r\n    {\r\n      \"wid\":-1,\r\n      \"operationName\":\"insightsTimex\",\r\n      \"operationFields\":{\r\n        \"timeField\":\"pushed_at\",\r\n        \"epochTime\":true,\r\n        \"timeFormat\":\"\"\r\n      },\r\n      \"webhookName\":\"\"\r\n    }\r\n  ],\r\n  \"dynamicTemplate\":\"{\\n  \\\"commits\\\":[\\n    {\\n      \\\"id\\\":\\\"commitIdDY\\\",\\n      \\\"url\\\":\\\"commitURLDY\\\",\\n      \\\"timestamp\\\":\\\"commitTimeDY\\\"\\n    }\\n  ]\\n}\",\r\n  \"isUpdateRequired\":true,\r\n  \"fieldUsedForUpdate\":\"id\",\r\n  \"eventConfig\":\"\",\r\n  \"isEventProcessing\":false\r\n}";
	}
