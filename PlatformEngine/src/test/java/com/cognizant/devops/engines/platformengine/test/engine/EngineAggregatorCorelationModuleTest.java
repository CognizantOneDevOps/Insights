/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformengine.test.engine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformengine.message.subscriber.AgentDataSubscriber;
import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.platformengine.modules.correlation.CorrelationExecutorTestData;
import com.cognizant.devops.engines.platformengine.modules.correlation.EngineCorrelatorModule;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.google.gson.JsonObject;

@Test
public class EngineAggregatorCorelationModuleTest {
	private static Logger log = LogManager.getLogger(EngineAggregatorCorelationModuleTest.class.getName());

	CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	private FileReader reader = null;
	private Properties p = null;
	CorrelationExecutorTestData correlationTestData = new CorrelationExecutorTestData();
	JsonObject testData = new JsonObject();
	
	@BeforeClass
	public void onInit() throws InterruptedException, IOException, InsightsCustomException, Exception {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
				+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "EngineAggregatorCorelationModule.json";
		testData = JsonUtils.getJsonData(path).getAsJsonObject();
		reader = new FileReader("src/test/resources/Properties.prop");
		p = new Properties();
		p.load(reader);
		
		String correlationConfig1 = testData.get("saveDataConfig").toString();
		correlationTestData.saveConfig(correlationConfig1);
		
		/*
		 * Publish Messages to Queue *
		 */
		EngineTestData.publishMessage(testData.get("gitRoutingKey").getAsString(),
		testData.get("GITTestPlayload").toString());
		EngineTestData.publishMessage(testData.get("jenkinsRoutingKey").getAsString(),
		testData.get("JENKINSTestPayload").toString());
		Thread.sleep(1000);
		
		/* Start Engine for Correlation **/
		ApplicationConfigProvider.getInstance().getCorrelations().setBatchSize(1000);
		ApplicationConfigProvider.getInstance().getCorrelations().setCorrelationFrequency(1);
		ApplicationConfigProvider.getInstance().getCorrelations().setCorrelationWindow(-1);

		/*
		 * Start Engine for Data Collection and Node Creation *
		 */
		EngineAggregatorModule em = new EngineAggregatorModule();
		em.executeJob();
		Thread.sleep(1000);
		
		log.debug("Test Data flow has been created successfully");
	}

	@Test(priority = 1)
	public void testNeo4JData() {
		try {
			/*
			 * Test GIT node is created *
			 */
			@SuppressWarnings("rawtypes")
			Map map = EngineTestData.readNeo4JData(p.getProperty("gitDataNodeName"), "commitId");
			/* Assert on commitId */
			Assert.assertEquals(p.getProperty("gitCommitId"), map.get("commitId"));
			/* Assert on toolname */
			Assert.assertEquals(p.getProperty("gitToolName"), map.get("toolName"));
			/* Assert on categoryName */
			Assert.assertEquals(p.getProperty("gitCategoryName"), map.get("categoryName"));
	
			/*
			 * Test Jenkins node is created *
			 */
			map = EngineTestData.readNeo4JData(p.getProperty("jenkinsDataNodeName"), "scmcommitId");
			/* Assert on commitId */
			Assert.assertEquals(p.getProperty("jenkinsCommitId"), map.get("scmcommitId"));
			/* Assert on toolname */
			Assert.assertEquals(p.getProperty("jenkinsToolName"), map.get("toolName"));
			/* Assert on categoryName */
			Assert.assertEquals(p.getProperty("jenkinsCategoryName"), map.get("categoryName"));
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 2)
	public void testSaveConfig() throws InsightsCustomException {
		try {
			List<CorrelationConfiguration> correlationList = correlationConfigDAL.getActiveCorrelations();
			Assert.assertTrue(!correlationList.isEmpty());
			Thread.sleep(5000);
			EngineCorrelatorModule ecm = new EngineCorrelatorModule();
			ecm.executeCorrelation();
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 3)
	public void testCorrelation() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (a)<-[r:TEST_FROM_GIT_TO_JENKINS]-(b) where a.scmcommitId=\"CM-7569369619\" and b.commitId='CM-7569369619' return count(a) as Total";
		GraphResponse neo4jResponse;
		try {
			neo4jResponse = dbHandler.executeCypherQuery(query);
			String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");
			/* Assert on Node Relationship */
			log.debug("finalJson  {} ", finalJson);
			Assert.assertTrue(Integer.parseInt(finalJson) > 0); //"true"
		} catch (InsightsCustomException | AssertionError e) {
			log.error("InsightsCustomException : or AssertionError " + e);
		}
	}
	
	@Test(priority = 4)
	public void testDataEnrichment() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:GIT_UNTEST) where exists(n.test) return count(n) as Total";
		GraphResponse neo4jResponse;
		try {
			neo4jResponse = dbHandler.executeCypherQuery(query);
			String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");
			/* Assert on Node Relationship */
			log.debug("finalJson  {} ", finalJson);
			Assert.assertTrue(Integer.parseInt(finalJson) > 0); //"true"
		} catch (InsightsCustomException | AssertionError e) {
			log.error("InsightsCustomException : or AssertionError " + e);
		}
	}
	
	@Test(priority = 5)
	public void testDataEnrichmentForJsonResponse() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:GIT_UNTEST) where exists(n.test) return count(n) as Total";
		JsonObject jsonObject;
		try {
			jsonObject = dbHandler.executeCypherQueryForJsonResponse(query);
			String finalJson = jsonObject.get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");
			/* Assert on Node Relationship */
			log.debug("finalJson  {} ", finalJson);
			Assert.assertTrue(Integer.parseInt(finalJson) > 0); //"true"
		} catch (InsightsCustomException | AssertionError e) {
			log.error("InsightsCustomException : or AssertionError " + e);
		}
	}
	
	@Test(priority = 6)
	public void testExecuteCypherQueryMultiple() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String[] query = {"MATCH (n:GIT_UNTEST) where exists(n.test) return count(n) as Total"};
		GraphResponse neo4jResponse;
		try {
			neo4jResponse = dbHandler.executeCypherQueryMultiple(query);
			String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");
			/* Assert on Node Relationship */
			log.debug("finalJson  {} ", finalJson);
			Assert.assertTrue(Integer.parseInt(finalJson) > 0); //"true"
		} catch (InsightsCustomException | AssertionError e) {
			log.error("InsightsCustomException : or AssertionError " + e);
		}
	}
	
	@Test(priority = 7)
	public void testExecuteQueryWithData() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:GIT_UNTEST) where exists(n.test) return count(n) as Total";
		JsonObject jsonObject;
		List<JsonObject> a = new ArrayList<JsonObject>();
		a.add(testData.get("dataQuery").getAsJsonObject());
		try {
			jsonObject = dbHandler.executeQueryWithData(query, a);
			String finalJson = jsonObject.get("response").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");
			/* Assert on Node Relationship */
			log.debug("finalJson  {} ", finalJson);
			Assert.assertTrue(Integer.parseInt(finalJson) > 0); //"true"
		} catch (InsightsCustomException | AssertionError e) {
			log.error("InsightsCustomException : or AssertionError " + e);
		}
	}
	
	@Test(priority = 8)
	public void testPublishAgentData() {
		try {
			String routingKey = testData.get("jenkinsRoutingKey").getAsString();
			String data = testData.get("JENKINSTestPayload").toString();
			AgentDataSubscriber ads = new AgentDataSubscriber(routingKey);
			ads.handleDelivery(routingKey, data);
			JsonObject jsonData = testData.get("JENKINSTestPayload").getAsJsonObject();
			String value = jsonData.get("data").getAsJsonArray().get(0).getAsJsonObject().get("scmcommitId")
					.toString();
			int countOfRecords = correlationTestData.readNeo4JData("CI", value);
			Assert.assertTrue(countOfRecords > 0);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 9)
	public void testNeo4jData() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:JENKINS_UNTEST) where exists(n.scmcommitId) return count(n) as Total";
		GraphResponse neo4jResponse;
		try {
			neo4jResponse = dbHandler.executeCypherQuery(query);
			String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");
			/* Assert on Node Relationship */
			log.debug("finalJson  {} ", finalJson);
			Assert.assertTrue(Integer.parseInt(finalJson) > 0); //"true"
		} catch (InsightsCustomException | AssertionError e) {
			log.error("InsightsCustomException : or AssertionError " + e);
		}
	}
	
   @AfterClass
	public void cleanUp() {
		/* Cleaning Neo4J */
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH p=()-[r:TEST_FROM_GIT_TO_JENKINS]->() delete p";
		try {
			dbHandler.executeCypherQuery(query);
			dbHandler.executeCypherQuery("MATCH (n:JENKINS_UNTEST) DETACH DELETE n");
			dbHandler.executeCypherQuery("MATCH (n:GIT_UNTEST) DETACH DELETE n");
		} catch (InsightsCustomException e) {
			log.error("InsightsCustomException : " + e.toString());
		}
	}

}