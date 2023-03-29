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

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.JsonObject;

public class DataDictionaryTestData extends AbstractTestNGSpringContextTests{
	private static final Logger log = LogManager.getLogger(DataDictionaryTestData.class);
	
	GraphDBHandler graphDBHandler;

	String sourcelabel = "JIRA_TEST";
	String sourceCat = "ALM_TEST";
	String destLabel = "GIT_TEST";
	String destCat = "SCM_TEST";
	String emptylabel = "EMPTY_LABEL";
	String toolName = "git";
	String agentVersion = "v5.2";
	String osversion = "Windows";
	String trackingDetails = "";
	String agentId = "git_testng";
	String toolCategory = "SCM";
	public JsonObject registeredWebhookJson = getregisteredWebhookJson();
	String relationQuery = "MATCH (a:JIRA_TEST), (b:GIT_TEST) WHERE a.jiraKey = \"LS-8782767628\" AND b.jiraKey = \"LS-8782767628\" \r\n" + 
			"CREATE (a)-[r: TEST_RELATION]->(b) \r\n" + 
			"RETURN a,b ";
	
	JsonObject testData = new JsonObject();
	
	private JsonObject getregisteredWebhookJson() {
		try {
		    String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
                    + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "DataDictionary.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
			
		} catch (Exception e) {
			log.error("Error preparing data at  DataDictionaryTsetData record ", e);
		}
		JsonObject json = JsonUtils.parseStringAsJsonObject(testData.get("webhookJson").toString());
		return json;
	}
	
	public void insertAgentDataInNeo4j(String category, String label, String data) throws InsightsCustomException {
		JsonObject agentDataJson = JsonUtils.parseStringAsJsonObject(data);
		String query = "CREATE (n:" + category + ":" + label + ":DATA $props)";
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
