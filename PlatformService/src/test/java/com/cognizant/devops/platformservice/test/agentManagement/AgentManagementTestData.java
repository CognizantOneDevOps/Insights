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
package com.cognizant.devops.platformservice.test.agentManagement;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class AgentManagementTestData {

	String toolName = "git";
	
	String agentVersion = "v5.2";
	String osversion = "Windows";
	String configDetails = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SCM.GIT.config\",\"agentCtrlQueue\":\"git_testng\"},\"publish\":{\"data\":\"SCM.GIT.DATA\",\"health\":\"SCM.GIT.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"sha\":\"commitId\",\"commit\":{\"message\":\"message\",\"author\":{\"name\":\"authorName\",\"date\":\"commitTime\"}}}},\"agentId\":\"git_testng\",\"enableBranches\":false,\"enableBrancheDeletion\":false,\"enableDataValidation\":true,\"toolCategory\":\"SCM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"enableValueArray\":false,\"useResponseTemplate\":true,\"auth\":\"base64\",\"runSchedule\":30,\"timeStampField\":\"commitTime\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"startFrom\":\"2019-03-01 15:46:33\",\"accessToken\":\"accesstoken\",\"getRepos\":\"https://api.github.com/users/USER_NAME/repos\",\"commitsBaseEndPoint\":\"https://api.github.com/repos/REPO_NAME/\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v5.2\",\"toolName\":\"GIT\"}";
	String configDetailsWithSameIDs = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SCM.GIT.config\",\"agentCtrlQueue\":\"git\"},\"publish\":{\"data\":\"SCM.GIT.DATA\",\"health\":\"SCM.GIT.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"sha\":\"commitId\",\"commit\":{\"message\":\"message\",\"author\":{\"name\":\"authorName\",\"date\":\"commitTime\"}}}},\"agentId\":\"git\",\"enableBranches\":false,\"enableBrancheDeletion\":false,\"enableDataValidation\":true,\"toolCategory\":\"SCM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"enableValueArray\":false,\"useResponseTemplate\":true,\"auth\":\"base64\",\"runSchedule\":30,\"timeStampField\":\"commitTime\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"startFrom\":\"2019-03-01 15:46:33\",\"accessToken\":\"accesstoken\",\"getRepos\":\"https://api.github.com/users/USER_NAME/repos\",\"commitsBaseEndPoint\":\"https://api.github.com/repos/REPO_NAME/\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v5.2\",\"toolName\":\"GIT\"}";
	String configDetailsWithInvalidDataLabel = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SCM.GIT.config\",\"agentCtrlQueue\":\"git_testng\"},\"publish\":{\"data\":\"SCM:GIT:DATA\",\"health\":\"SCM.GIT.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"sha\":\"commitId\",\"commit\":{\"message\":\"message\",\"author\":{\"name\":\"authorName\",\"date\":\"commitTime\"}}}},\"agentId\":\"git_testng\",\"enableBranches\":false,\"enableBrancheDeletion\":false,\"enableDataValidation\":true,\"toolCategory\":\"SCM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"enableValueArray\":false,\"useResponseTemplate\":true,\"auth\":\"base64\",\"runSchedule\":30,\"timeStampField\":\"commitTime\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"startFrom\":\"2019-03-01 15:46:33\",\"accessToken\":\"accesstoken\",\"getRepos\":\"https://api.github.com/users/USER_NAME/repos\",\"commitsBaseEndPoint\":\"https://api.github.com/repos/REPO_NAME/\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v5.2\",\"toolName\":\"GIT\"}";
	String configDetailsWithInvalidHealthLabel = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SCM.GIT.config\",\"agentCtrlQueue\":\"git_testng\"},\"publish\":{\"data\":\"SCM.GIT.DATA\",\"health\":\"SCM.GIT,HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"sha\":\"commitId\",\"commit\":{\"message\":\"message\",\"author\":{\"name\":\"authorName\",\"date\":\"commitTime\"}}}},\"agentId\":\"git_testng\",\"enableBranches\":false,\"enableBrancheDeletion\":false,\"enableDataValidation\":true,\"toolCategory\":\"SCM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"enableValueArray\":false,\"useResponseTemplate\":true,\"auth\":\"base64\",\"runSchedule\":30,\"timeStampField\":\"commitTime\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"startFrom\":\"2019-03-01 15:46:33\",\"accessToken\":\"accesstoken\",\"getRepos\":\"https://api.github.com/users/USER_NAME/repos\",\"commitsBaseEndPoint\":\"https://api.github.com/repos/REPO_NAME/\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v5.2\",\"toolName\":\"GIT\"}";

	String trackingDetails = "";
	
	Date updateDate = Timestamp.valueOf(LocalDateTime.now());
	
	String agentId = "git_testng";
	String toolCategory = "SCM";
	
}