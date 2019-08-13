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

public class WebhookServiceTestData {

	
	String webhookname = "git_webhook";
	String toolName = "GIT";
	String labelDisplay = "SCM:GIT:DATA";
	String dataformat = "json";
	String mqchannel = "IPW_git_webhook";
	Boolean subscribestatus = true;
	String responseTemplate = "head_commit.timestamp=commitTime,head_commit.author.name=authorName";
}
