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
package com.cognizant.devops.engines.platformengine.message.subscriber;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.platformengine.test.engine.EngineTestData;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.google.gson.JsonObject;


/**
 * This is a test class for AgentDataSubscriber
 * 
 * @author 368419
 *
 */
@Test
public class AgentDataSubscriberTest extends AgentDataSubscriberTestData{
	private static Logger log = LogManager.getLogger(AgentDataSubscriberTest.class.getName());
	AgentDataSubscriberTest executor = null;
	JsonObject testData = new JsonObject();
	JsonObject engineAggregTestData = new JsonObject();
	private FileReader reader = null;
	private Properties p = null;
	private AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	private InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	
	/*
	 * Start Engine for Data Collection and Node Creation *
	 */
	EngineAggregatorModule em = new EngineAggregatorModule();

	@BeforeClass
	public void onInit()
			throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ TestngInitializerTest.TESTNG_TESTDATA + File.separator + TestngInitializerTest.TESTNG_PLATFORMENGINE
				+ File.separator + "AgentDataSubscriber.json";
		testData = JsonUtils.getJsonData(path).getAsJsonObject();

		String enginePath = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ TestngInitializerTest.TESTNG_TESTDATA + File.separator + TestngInitializerTest.TESTNG_PLATFORMENGINE
				+ File.separator + "EngineAggregatorCorelationModule.json";
		engineAggregTestData = JsonUtils.getJsonData(enginePath).getAsJsonObject();

		reader = new FileReader("src/test/resources/Properties.prop");
		p = new Properties();
		p.load(reader);

		/*
		 * Insert Test Data into Postgre *
		 */
		try {
			agentConfigDAL.saveAgentConfigFromUI(p.getProperty("gitAgentId"), EngineTestData.gitToolCategory,
					EngineTestData.gitLabelName, "git", engineAggregTestData.get("gitConfig").getAsJsonObject(),
					EngineTestData.agentVersion, EngineTestData.osversion, EngineTestData.updateDate, false, false);
			/******************************************************************************************/

			agentConfigDAL.saveAgentConfigFromUI(p.getProperty("jenkinsAgentId"), EngineTestData.jenkinToolCategory,
					EngineTestData.jenkinLabelName, "jenkins",
					engineAggregTestData.get("jenkinsConfig").getAsJsonObject(), EngineTestData.agentVersion,
					EngineTestData.osversion, EngineTestData.updateDate, false, false);
			/******************************************************************************************/

			// for saving business mapping data
			EngineTestData.saveToolsMappingLabel(engineAggregTestData.get("businessMapping").toString());
			
			// for saving data enrichment record
			configFilesDAL.saveConfigurationFile(EngineTestData.createDataEnrichmentData());
		} catch (Exception e) {
			log.error(e);
		}
		Thread.sleep(1000);

		/*
		 * Publish Messages to Queue *
		 */
		
		EngineTestData.publishMessage(engineAggregTestData.get("gitRoutingKey").getAsString(),
				engineAggregTestData.get("GITTestPlayloadWithRelation").toString());

		Thread.sleep(1000);


		/*
		 * Start Engine for Data Collection and Node Creation *
		 */
		
		EngineAggregatorModule em = new EngineAggregatorModule();
		em.executeJob();
		Thread.sleep(1000);

		log.debug("Test Data flow has been created successfully");
	}

	@BeforeMethod
	protected void setUp() throws Exception {
		executor = new AgentDataSubscriberTest();
	}

	@AfterMethod
	protected void tearDown() throws Exception {
	}
	
	@Test(priority = 1)
	public void testAgentData() {
		try {
			String routingKey = engineAggregTestData.get("gitRoutingKey").getAsString();
			String data = engineAggregTestData.get("GITTestPlayload").toString();
			EngineTestData.publishMessage(routingKey, data);
			AgentDataSubscriber ads = new AgentDataSubscriber(routingKey);
			ads.handleDelivery(routingKey, data);
			JsonObject jsonData = engineAggregTestData.get("GITTestPlayload").getAsJsonObject();
			String value = jsonData.get("data").getAsJsonArray().get(0).getAsJsonObject().get("gitAuthorName")
					.toString();
			int countOfRecords = readNeo4JData(gitLabel, value);
			Assert.assertTrue(countOfRecords > 0);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 2)
	public void testAgentDataWithUniqueKey() {
		try {
			String routingKey = engineAggregTestData.get("gitRoutingKey").getAsString();
			String data = engineAggregTestData.get("GITTestPlayloadWithUniqueKey").toString();
			EngineTestData.publishMessage(routingKey, data);
			AgentDataSubscriber ads = new AgentDataSubscriber(routingKey);
			ads.handleDelivery(routingKey, data);
			JsonObject jsonData = engineAggregTestData.get("GITTestPlayloadWithUniqueKey").getAsJsonObject();
			String value = jsonData.get("data").getAsJsonArray().get(0).getAsJsonObject().get("gitAuthorName")
					.toString();
			int countOfRecords = readNeo4JData(gitLabel, value);
			Assert.assertTrue(countOfRecords > 0);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 3)
	public void testAgentDataWithRelation() {
		try {
			String routingKey = engineAggregTestData.get("gitRoutingKey").getAsString();
			String data = engineAggregTestData.get("GITTestPlayloadWithRelation").toString();
			EngineTestData.publishMessage(routingKey, data);
			JsonObject jsonData = engineAggregTestData.get("GITTestPlayloadWithRelation").getAsJsonObject();
			String value = jsonData.get("data").getAsJsonArray().get(0).getAsJsonObject().get("gitAuthorName")
					.toString();
			int countOfRecords = readNeo4JData(gitLabel, value);
			Assert.assertTrue(countOfRecords > 0);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 4)
	public void testPublishData() {
		try {
			Map map = EngineTestData.readNeo4JData(p.getProperty("gitDataNodeName"), "commitId");
			Assert.assertEquals(p.getProperty("gitCommitId"), map.get("commitId"));
			Assert.assertEquals(p.getProperty("gitToolName"), map.get("toolName"));
			Assert.assertEquals(p.getProperty("gitCategoryName"), map.get("categoryName"));
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 5)
	public void testAgentHealthData() {
		try {
			String routingKey = engineAggregTestData.get("gitHealthRoutingKey").getAsString();
			String data = engineAggregTestData.get("GITHealthPlayload").toString();
			EngineTestData.publishMessage(routingKey, data);
			AgentHealthSubscriber ads = new AgentHealthSubscriber(routingKey);
			ads.handleDelivery(routingKey, data);
			JsonObject jsonData = engineAggregTestData.get("GITHealthPlayload").getAsJsonObject();
			String value = jsonData.get("data").getAsJsonArray().get(0).getAsJsonObject().get("gitAuthorName")
					.toString();
			int countOfRecords = readNeo4JData(gitLabel, value);
			Assert.assertTrue(countOfRecords > 0);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 6)
	public void testNeo4jData() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:GIT_UNTEST) where exists(n.execId) return count(n) as Total";
		GraphResponse neo4jResponse;
		try {
			neo4jResponse = dbHandler.executeCypherQuery(query);
			String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");
			/* Assert on Node Relationship */
			log.debug("finalJson  {} ", finalJson);
			Assert.assertTrue(Integer.parseInt(finalJson) > 0); 
		} catch (InsightsCustomException | AssertionError e) {
			log.error("InsightsCustomException : or AssertionError " + e);
		}
	}
	
}
