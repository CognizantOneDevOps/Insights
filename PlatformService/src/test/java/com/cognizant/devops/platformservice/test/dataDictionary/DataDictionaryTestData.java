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

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataDictionaryTestData {
	GraphDBHandler graphDBHandler;

	String sourcelabel = "JIRA_TEST";
	String sourceCat = "ALM_TEST";
	String destLabel = "GIT_TEST";
	String destCat = "SCM_TEST";
	String emptylabel = "EMPTY_LABEL";
	String webhookJson = "{\"toolName\":\"GIT\",\"labelDisplay\":\"SCM:GIT:DATA\",\"webhookName\":\"git_new\",\"dataformat\":\"json\",\"mqchannel\":\"IPW_git_new\",\"responseTemplate\":\"head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at\",\"statussubscribe\":false,\"derivedOperations\":[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"\"}],\"dynamicTemplate\":\"{\\n  \\\"commits\\\":[\\n    {\\n      \\\"id\\\":\\\"commitIdDY\\\",\\n      \\\"url\\\":\\\"commitURLDY\\\",\\n      \\\"timestamp\\\":\\\"commitTimeDY\\\"\\n    }\\n  ]\\n}\",\"isUpdateRequired\":true,\"fieldUsedForUpdate\":\"id\",\"eventConfig\":\"\",\"isEventProcessing\":false}";
	JsonParser parser = new JsonParser();
	String toolName = "git";
	String agentVersion = "v5.2";
	String osversion = "Windows";
	String configDetails = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SCM.GIT.config\",\"agentCtrlQueue\":\"git_testng\"},\"publish\":{\"data\":\"SCM.GIT.DATA\",\"health\":\"SCM.GIT.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"sha\":\"commitId\",\"commit\":{\"message\":\"message\",\"author\":{\"name\":\"authorName\",\"date\":\"commitTime\"}}}},\"agentId\":\"git_testng\",\"enableBranches\":false,\"enableBrancheDeletion\":false,\"enableDataValidation\":true,\"toolCategory\":\"SCM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"enableValueArray\":false,\"useResponseTemplate\":true,\"auth\":\"base64\",\"runSchedule\":30,\"timeStampField\":\"commitTime\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"startFrom\":\"2019-03-01 15:46:33\",\"accessToken\":\"accesstoken\",\"getRepos\":\"https://api.github.com/users/USER_NAME/repos\",\"commitsBaseEndPoint\":\"https://api.github.com/repos/REPO_NAME/\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v5.2\",\"toolName\":\"GIT\"}";
	String trackingDetails = "";
	String agentId = "git_testng";
	String toolCategory = "SCM";
	public JsonObject registeredWebhookJson = getregisteredWebhookJson();
	String jiraAgentData = "{\"storyId\":\"ST-11\",\"assigneeEmail\":\"hari@cognizant.com\",\"fixVersions\":\"ACS19.0.3.1\",\"inSightsTimeX\":\"2018-09-11T03:53:10Z\",\"resolution\":\"Done\",\"assigneeID\":\"234234\",\"categoryName\":\"ALM_TEST\",\"jiraPriority\":\"Low\",\"jiraIssueType\":\"Sub-task\",\"toolName\":\"JIRA_TEST\",\"storyPoints\":\"3\",\"jiraKey\":\"LS-8782767628\",\"Priority\":\"5\",\"creationDate\":\"2018-09-11T03:53:10Z\",\"jiraStatus\":\"Backlog\",\"execId\":\"4649594f-6507-11ea-91ee-f2b3c416de74\",\"issueType\":\"Performance_Bug\",\"jiraUpdated\":\"2018-09-30T03:53:10Z\",\"sprintId\":\"ST-3\",\"authorName\":\"Tommy\",\"inSightsTime\":1536569250,\"projectName\":\"PaymentServices\",\"jiraCreator\":\"Akshay\",\"progressTimeSec\":\"1232\"}";
	String gitAgentData = "{\"jiraKey\":\"LS-8782767628\",\"repoName\":\"InsightsTest\",\"gitReponame\":\"InsightsTest\",\"gitCommiTime\":\"2018-09-11T04:20:30Z\",\"commitId\":\"CM-4083459284\",\"message\":\"This commit is associated with jira-key : LS-8782767628\",\"inSightsTimeX\":\"2018-09-11T04:20:30Z\",\"categoryName\":\"SCM_TEST\",\"gitAuthorName\":\"Prajakta\",\"execId\":\"4649594f-6507-11ea-91ee-f2b3c416de74\",\"inSightsTime\":1536639630,\"gitCommitId\":\"YWtGWquOdRZLZ6n5EgwmV9yWfk4qldfH\",\"toolName\":\"GIT_TEST\"}";
	String relationQuery = "MATCH (a:JIRA_TEST), (b:GIT_TEST) WHERE a.jiraKey = \"LS-8782767628\" AND b.jiraKey = \"LS-8782767628\" \r\n" + 
			"CREATE (a)-[r: TEST_RELATION]->(b) \r\n" + 
			"RETURN a,b ";
	
	private JsonObject getregisteredWebhookJson() {
		JsonObject json = (JsonObject) parser.parse(webhookJson);
		return json;
	}
	
	public void insertAgentDataInNeo4j(String category, String label, String data) throws InsightsCustomException {
		JsonObject agentDataJson = new JsonParser().parse(data).getAsJsonObject();
		String query = "CREATE (n:" + category + ":" + label + ":DATA {props})";
		try {
			JsonObject graphResponse = graphDBHandler.createNodesWithSingleData(agentDataJson, query);
		} catch (InsightsCustomException e) {
			throw new InsightsCustomException(e.getMessage());
		}
		
	}
	
	public void deleteAgentDataFromNeo4j(String category) throws InsightsCustomException {
		String query = "MATCH (n:" + category + ") DETACH DELETE n";
		try {
			graphDBHandler.executeCypherQuery(query);
		} catch (InsightsCustomException e) {
			throw new InsightsCustomException(e.getMessage());
		}
	}

}
