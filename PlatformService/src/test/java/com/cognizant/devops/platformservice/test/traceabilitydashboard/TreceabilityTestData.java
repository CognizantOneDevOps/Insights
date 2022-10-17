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
package com.cognizant.devops.platformservice.test.traceabilitydashboard;

import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;

public class TreceabilityTestData {
	GraphDBHandler graphDBHandler;
	
	public static String toolName="JIRA_TRACEABILITY";
	public static String fieldName="jiraKey";
	public static String fieldVal ="LS-8782767628";
	public static String incorrectFieldVal ="incorrect";
	public static String cacheKey=toolName+fieldName+fieldVal;
	String jiralabel = "JIRA_TRACEABILITY";
	String jiraCat = "ALM_TRACEABILITY";
	String gitLabel = "GIT_TRACEABILITY";
	String gitCat = "SCM_TRACEABILITY";
	String jiraAgentData = "{\"storyId\":\"ST-11\",\"assigneeEmail\":\"demo123@gmail.com\",\"fixVersions\":\"ACS19.0.3.1\",\"inSightsTimeX\":\"2018-09-11T03:53:10Z\",\"resolution\":\"Done\",\"assigneeID\":\"234234\",\"categoryName\":\"ALM_TRACEABILITY\",\"jiraPriority\":\"Low\",\"jiraIssueType\":\"Sub-task\",\"toolName\":\"JIRA_TRACEABILITY\",\"storyPoints\":\"3\",\"jiraKey\":\"LS-8782767628\",\"Priority\":\"5\",\"creationDate\":\"2018-09-11T03:53:10Z\",\"jiraStatus\":\"Backlog\",\"execId\":\"4649594f-6507-11ea-91ee-f2b3c416de74\",\"issueType\":\"Performance_Bug\",\"jiraUpdated\":\"2018-09-30T03:53:10Z\",\"sprintId\":\"ST-3\",\"authorName\":\"Tommy\",\"inSightsTime\":1536569250,\"projectName\":\"PaymentServices\",\"jiraCreator\":\"Akshay\",\"progressTimeSec\":\"1232\"}";
	String gitAgentData = "{\"jiraKey\":\"LS-8782767628\",\"repoName\":\"InsightsTest\",\"gitReponame\":\"InsightsTest\",\"gitCommiTime\":\"2018-09-11T04:20:30Z\",\"commitId\":\"CM-4083459284\",\"message\":\"This commit is associated with jira-key : LS-8782767628\",\"inSightsTimeX\":\"2018-09-11T04:20:30Z\",\"categoryName\":\"SCM_TRACEABILITY\",\"gitAuthorName\":\"Prajakta\",\"execId\":\"4649594f-6507-11ea-91ee-f2b3c416de74\",\"inSightsTime\":1536639630,\"gitCommitId\":\"YWtGWquOdRZLZ6n5EgwmV9yWfk4qldfH\",\"toolName\":\"GIT_TRACEABILITY\"}";
	
	public void insertDataInNeo4j(String category, String label, String data) throws InsightsCustomException {
		JsonObject agentDataJson =JsonUtils.parseStringAsJsonObject(data);
		String query = "CREATE (n:" + category + ":" + label + ":DATA $props)";
		try {
			JsonObject graphResponse = graphDBHandler.createNodesWithSingleData(agentDataJson, query);
		} catch (InsightsCustomException e) {
			throw new InsightsCustomException(e.getMessage());
		}
		
	}
	
	public void deleteDataFromNeo4j(String category) throws InsightsCustomException {
		String query = "MATCH (n:" + category + ") DETACH DELETE n";
		try {
			graphDBHandler.executeCypherQuery(query);
		} catch (InsightsCustomException e) {
			throw new InsightsCustomException(e.getMessage());
		}
	}

}
