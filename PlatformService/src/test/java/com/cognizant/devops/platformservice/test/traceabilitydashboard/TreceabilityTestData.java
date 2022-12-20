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

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.google.gson.JsonObject;

public class TreceabilityTestData extends AbstractTestNGSpringContextTests {
	CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
			.withCache("traceability",
					CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
							ResourcePoolsBuilder.newResourcePoolsBuilder().heap(30, EntryUnit.ENTRIES).offheap(10,
									MemoryUnit.MB)))
			.build();
	GraphDBHandler graphDBHandler = new GraphDBHandler();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	InsightsConfigFiles config=new InsightsConfigFiles();
	String toolName="JIRA";
	String fieldName="key";
	String fieldVal ="JiraDemo123";
	String type = "Epic";
	String incorrectFieldVal ="incorrect";
	String cacheKey=toolName+fieldName+fieldVal;
	String jiraAgentData = "{\"storyId\":\"ST-11\",\"assigneeEmail\":\"demo123@gmail.com\",\"fixVersions\":\"ACS19.0.3.1\",\"inSightsTimeX\":\"2018-09-11T03:53:10Z\",\"resolution\":\"Done\",\"assigneeID\":\"234234\",\"categoryName\":\"ALM_TRACEABILITY\",\"jiraPriority\":\"Low\",\"jiraIssueType\":\"Sub-task\",\"toolName\":\"JIRA_TRACEABILITY\",\"storyPoints\":\"3\",\"jiraKey\":\"LS-8782767628\",\"Priority\":\"5\",\"creationDate\":\"2018-09-11T03:53:10Z\",\"jiraStatus\":\"Backlog\",\"execId\":\"4649594f-6507-11ea-91ee-f2b3c416de74\",\"issueType\":\"Performance_Bug\",\"jiraUpdated\":\"2018-09-30T03:53:10Z\",\"sprintId\":\"ST-3\",\"authorName\":\"Tommy\",\"inSightsTime\":1536569250,\"projectName\":\"PaymentServices\",\"jiraCreator\":\"Akshay\",\"progressTimeSec\":\"1232\"}";
	String gitAgentData = "{\"jiraKey\":\"LS-8782767628\",\"repoName\":\"InsightsTest\",\"gitReponame\":\"InsightsTest\",\"gitCommiTime\":\"2018-09-11T04:20:30Z\",\"commitId\":\"CM-4083459284\",\"message\":\"This commit is associated with jira-key : LS-8782767628\",\"inSightsTimeX\":\"2018-09-11T04:20:30Z\",\"categoryName\":\"SCM_TRACEABILITY\",\"gitAuthorName\":\"Prajakta\",\"execId\":\"4649594f-6507-11ea-91ee-f2b3c416de74\",\"inSightsTime\":1536639630,\"gitCommitId\":\"YWtGWquOdRZLZ6n5EgwmV9yWfk4qldfH\",\"toolName\":\"GIT_TRACEABILITY\"}";
	String[] operation={"PROPERTY_COUNT","COUNT","PERCENTAGE","DISTINCT_COUNT","SUMM"}; 
	String data = "{\"uuid\":\"faf7cd5c-92b2-11eb-9c8f-06aa9eb01291\",\"order\":\"2\",\"searchKey\":\"key\",\"issueKey\":\"PS-65\",\"property\":\"ALM\",\"epicKey\":\"JiraDemo1234\",\"issueType\":\"Story\",\"toolName\":\"JIRA\",\"toolstatus\":\"Done\",\"author\":\"Adam\",\"projectName\":\"PaymentServices\",\"timestamp\":\"1612781985\",\"priority\":\"High\",\"inSightsTimeX\":\"2021-02-08T10:59:45Z\",\"count\":21}";
	String dataWithoutEpicIssueType = "{\"uuid\":\"faf7cd5c-92b2-11eb-9c8f-06aa9eb01291\",\"order\":\"2\",\"searchKey\":\"key\",\"issueKey\":\"PS-65\",\"property\":\"ALM\",\"epicKey\":\"JiraDemo1234\",\"issueType\":\"Epic\",\"toolName\":\"JIRA\",\"toolstatus\":\"Done\",\"author\":\"Adam\",\"projectName\":\"PaymentServices\",\"timestamp\":\"1612781985\",\"priority\":\"High\",\"inSightsTimeX\":\"2021-02-08T10:59:45Z\",\"count\":21}";
	
	public void getOperationData(String actual, String replace) {
		List<InsightsConfigFiles> configFile = configFilesDAL.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.TRACEABILITY.name());
		String configFileData = new String(configFile.get(0).getFileData(), StandardCharsets.UTF_8);
		configFileData = configFileData.replace(actual,replace);
		config=configFile.get(0);
		config.setFileData(configFileData.getBytes());
		configFilesDAL.updateConfigurationFile(config);
	}
	
	public void getConfigData(String propertyRemove) {
		List<InsightsConfigFiles> configFile = configFilesDAL.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.TRACEABILITY.name());
		String configFileData = new String(configFile.get(0).getFileData(), StandardCharsets.UTF_8);
		JsonObject obj = convertStringIntoJson(configFileData);
		obj.get("JIRA").getAsJsonObject().remove(propertyRemove);
		obj.get("JIRA").getAsJsonObject().addProperty(propertyRemove, "EPIC");
		configFileData = obj.toString();
		config=configFile.get(0);
		config.setFileData(configFileData.getBytes());
		configFilesDAL.updateConfigurationFile(config);
	}

	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = JsonUtils.parseStringAsJsonObject(convertregisterkpi);
		return objectJson;
	}
	
	public void insertNodeInNeo4j() throws InsightsCustomException{
		String cypher = "CREATE (JiraDemo:ALM:DATA:DUMMY:JIRA:LATEST{resolutionDateEpoch: 1612762055, resolutionDate: \"2021-02-08T10:57:35\", reporter: \"Tom\", priority:\"High\", inSightsTimeX: \"2021-02-08T10:57:35Z\",\r\n"
				+ "uuid: \"e6119cc0-92b2-11eb-9c8f-06aa9eb01291\",\r\n"
				+ "categoryName: \"ALM\",\r\n"
				+ "execId: \"a4c28ade-923f-11eb-bf14-ddb856c2582a\",\r\n"
				+ "issueType: \"Epic\",\r\n"
				+ "lastUpdated: \"2021-02-08T10:57:35\",\r\n"
				+ "createdDate: \"2021-01-12T10:09:26\",\r\n"
				+ "blockchainProcessedFlag: true,\r\n"
				+ "inSightsTime: 1612781855,\r\n"
				+ "category: \"ALM\",\r\n"
				+ "projectName: \"PaymentServices\",\r\n"
				+ "lastUpdatedEpoch: 1612762055,\r\n"
				+ "key: \"JiraDemo123\",\r\n"
				+ "toolName: \"JIRA\",\r\n"
				+ "status: \"Done\"})";
		
		String cypher1 = "CREATE (JiraDemo1:ALM:DATA:DUMMY:DUMMYDATA:JIRA:LATEST:RAW{\r\n"
				+ "uuid: \"faf7cd5c-92b2-11eb-9c8f-06aa9eb01291\",\r\n"
				+ "issueKey:\"PS-65\",\r\n"
				+ "property:\"ALM\",\r\n"
				+ "epicKey:\"JiraDemo1234\",\r\n"
				+ "issueType:\"Story\",\r\n"
				+ "toolName:\"JIRA\",\r\n"
				+ "toolstatus:\"Done\",\r\n"
				+ "author:\"Adam\",\r\n"
				+ "projectName:\"PaymentServices\",\r\n"
				+ "timestamp:\"1612781985\",\r\n"
				+ "priority:\"High\",\r\n"
				+ "inSightsTimeX:\"2021-02-08T10:59:45Z\",\r\n"
				+ "count:\"21\"\r\n"
				+ "})";
		executeCypherQuery(cypher);
		executeCypherQuery(cypher1);
	}
	
	public void deleteNodeInNeo4j() throws InsightsCustomException{
		String cypher = "MATCH (n:ALM:JIRA:DATA) where n.key IN [\"JiraDemo123\"] DELETE n";
		executeCypherQuery(cypher);
		String cypher1 = "MATCH (n:ALM:JIRA:DATA) where n.epicKey IN [\"JiraDemo1234\"] DELETE n";
		executeCypherQuery(cypher1);
	}
	
   public JsonObject executeCypherQuery(String query) throws InsightsCustomException {
		GraphDBHandler dbHandler = new GraphDBHandler();
		JsonObject neo4jResponse = dbHandler.executeCypherQueryForJsonResponse(query);
		return neo4jResponse;
	}
}