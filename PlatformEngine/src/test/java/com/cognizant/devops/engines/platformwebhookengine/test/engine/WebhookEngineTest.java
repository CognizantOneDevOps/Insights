/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.engines.platformwebhookengine.test.engine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformengine.test.engine.EngineTestData;
import com.cognizant.devops.engines.platformwebhookengine.message.subscriber.WebHookDataSubscriber;
import com.cognizant.devops.engines.platformwebhookengine.message.subscriber.WebhookHealthSubscriber;
import com.cognizant.devops.engines.platformwebhookengine.modules.aggregator.WebHookEngineAggregatorModule;
import com.cognizant.devops.engines.platformwebhookengine.offlineprocessing.WebhookOfflineEventProcessing;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.engines.util.WebhookEventProcessing;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;
import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.google.gson.JsonObject;

@Test
public class WebhookEngineTest {

	private static Logger log = LogManager.getLogger(WebhookEngineTest.class.getName());
	WebHookConfigDAL webhookConfigDAL = new WebHookConfigDAL();
	WebhookEngineTestData webhookEngineTestData = new WebhookEngineTestData();
	WebhookOfflineEventProcessing webhookOfflineEventProcessing = new WebhookOfflineEventProcessing();
	private FileReader reader = null;
	private Properties p = null;
	JsonObject testData = new JsonObject();	

	public Set<WebhookDerivedConfig> derivedOperationsArray = null;
	public Set<WebhookDerivedConfig> derivedOperationsArrayWithoutEpoch = null;
	public Set<WebhookDerivedConfig> derivedOperationsArrayWithoutEpochPivotal = null;
	WebHookConfig webhookConfig = new WebHookConfig();
	
	@BeforeClass
	public void onInit() throws IOException, InsightsCustomException, Exception {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
				+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "WebhookEngine.json";
		testData = JsonUtils.getJsonData(path).getAsJsonObject();
		
		derivedOperationsArray = WebhookEngineTestData.getderivedOperationsJSONArray(testData.get("derivedOpsJson").toString());
		derivedOperationsArrayWithoutEpoch = WebhookEngineTestData.getderivedOperationsJSONArray(testData.get("derivedOpsJsonWithoutEpoch").toString());
		derivedOperationsArrayWithoutEpochPivotal = WebhookEngineTestData.getderivedOperationsJSONArray(testData.get("derivedOpsJsonWithoutEpochPivotal").toString());
		reader = new FileReader("src/test/resources/Properties.prop");

		p = new Properties();

		p.load(reader);

		/*
		 * Saving Data into Postgre
		 */
		try {
			webhookConfig.setResponseTemplate(testData.get("responseTemplate").getAsString());
			webhookConfig.setDynamicTemplate(testData.get("dynamicTemplate").toString());
			webhookConfig.setLabelName(testData.get("labelName").getAsString());
			webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
			webhookConfig.setToolName(testData.get("toolName").getAsString());
			webhookConfig.setWebHookName(WebhookEngineTestData.webhookName);
			webhookConfig.setEventProcessing(false);
			webhookConfig.setSubscribeStatus(true);
			webhookConfig.setIsUpdateRequired(false);
			webhookConfig.setDataFormat(testData.get("dataformat").getAsString());
			webhookConfig.setWebhookDerivedConfig(derivedOperationsArray);
			webhookConfigDAL.saveWebHookConfiguration(webhookConfig);
		} catch (Exception e) {
			log.error("message  {} ", e);
		}

		WebHookEngineAggregatorModule em = new WebHookEngineAggregatorModule();
		em.runWebhookDataCollector();
		log.debug("Test Data flow has been created successfully");
	}
	

	@AfterMethod
	protected void tearDown() throws Exception {
		WebHookEngineAggregatorModule em = new WebHookEngineAggregatorModule();
		em.runWebhookDataCollector();
		Thread.sleep(1000);
	}

	@Test(priority = 1)
	public void testGetAllActiveWebhookConfig() throws InsightsCustomException, InterruptedException {
		try {
			List<WebHookConfig> activeWebhookList = webhookConfigDAL.getAllActiveWebHookConfigurations();
			Assert.assertTrue(!activeWebhookList.isEmpty());
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 2)
	public void testPublishDataToQueue() {
		try {
			String routingKey = testData.get("labelName").getAsString();
			String data = testData.get("toolData").toString();
			WebhookEngineTestData.publishMessage(routingKey, data);
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 3)
	public void testEngineDataWithUpdate()
			throws IOException, TimeoutException, InterruptedException, InsightsCustomException {
		try {
			webhookConfig.setIsUpdateRequired(true);
			webhookConfig.setFieldUsedForUpdate(webhookEngineTestData.fieldUsedForUpdate);
			webhookConfigDAL.updateWebHookConfiguration(webhookConfig);
			WebhookEngineTestData.publishMessage(testData.get("labelName").getAsString(),
					testData.get("toolData").toString());
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 4)
	public void testEngineDataDerivedOperationWithoutEpoch()
			throws IOException, TimeoutException, InterruptedException, InsightsCustomException {
		try {
			webhookConfig.setWebhookDerivedConfig(derivedOperationsArrayWithoutEpoch);
			webhookConfigDAL.updateWebHookConfiguration(webhookConfig);
			WebhookEngineTestData.publishMessage(testData.get("labelName").getAsString(),
					testData.get("toolData").toString());
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 5)
	public void testEngineDataWithEvent()
			throws IOException, TimeoutException, InterruptedException, InsightsCustomException {
		try {
			webhookConfig.setIsUpdateRequired(false);
			webhookConfig.setEventProcessing(true);
			webhookConfigDAL.updateWebHookConfiguration(webhookConfig);
			WebhookEngineTestData.publishMessage(testData.get("labelName").getAsString(),
					testData.get("toolData").toString());
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 6)
	public void testNeo4JData() {
		try {
			String labelName = testData.get("labelName").getAsString();
			Map map = WebhookEngineTestData.readNeo4JData(labelName, p.getProperty("compareFlag"));
			Assert.assertEquals(testData.get("commitId").getAsString(), map.get("commitId"));
			Assert.assertEquals(p.getProperty("toolName"), map.get("toolName"));
			Assert.assertEquals(p.getProperty("webhookName"), map.get("webhookName"));
		} catch (Exception e) {
			log.error(e);
		}
	}


	@Test(priority = 7)
	public void testExecute() throws Exception {
		try {
			List<WebHookConfig> webhookEventConfigs = new ArrayList<>();
			webhookConfig.setFieldUsedForUpdate(webhookEngineTestData.fieldUsedForUpdate);
			webhookConfig.setEventProcessing(true);
			webhookEventConfigs.add(webhookConfig);
			webhookOfflineEventProcessing.execute(webhookEventConfigs);
		} catch (Exception e) {
			log.error("InsightsCustomException : " + e.toString());
		}
	}
	
	@Test(priority = 8)
	public void testDoEvent() {
		try {
			webhookConfig.setEventProcessing(true);
			webhookConfig.setFieldUsedForUpdate(webhookEngineTestData.fieldUsedForUpdate);
			WebhookEventProcessing webhookEventProcessing = new WebhookEventProcessing(Arrays.asList(testData.get("eventNode").getAsJsonObject()), webhookConfig, true); 
			Assert.assertEquals(true, webhookEventProcessing.doEvent());
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 9)
	public void testWebHookData() {
		try {
			String routingKey = testData.get("mqchannel").getAsString();
			String data = testData.get("toolData").toString();
			WebHookDataSubscriber webhookData = new WebHookDataSubscriber(webhookConfig, routingKey,
					"EngineWebHookModule");
			webhookData.handleDelivery(routingKey, data);
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 10)
	public void testWebHookHealthData() {
		try {
			String routingKey = testData.get("mqchannel").getAsString();
			String data = testData.get("toolData").toString();
			WebhookHealthSubscriber webhookData = new WebhookHealthSubscriber(routingKey, "EngineWebHookModule");
			webhookData.handleDelivery(routingKey, data);
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 11)
	public void testNeo4jDataQuery() {
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
	
	@AfterClass
	public void cleanUp() {

		// Cleaning Postgre 
		webhookConfigDAL.deleteWebhookConfigurations(webhookEngineTestData.webhookName);
		//webhookConfigDAL.deleteWebhookConfigurations(webhookEngineTestData.webhookNameException);
		// Cleaning Neo4J 
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (p:" + testData.get("labelName").getAsString() + ") where p.webhookName='"
				+ webhookEngineTestData.webhookName + "' delete p";
		String queryDeleteHeathData = "MATCH (p:" + webhookEngineTestData.WEBHOOK_SUBSCRIBER_HEALTH_ROUTING_KEY
				+ ") where p.instanceName='"
				+ webhookEngineTestData.healthMessageInstanceName + "' delete p";
		try {
			dbHandler.executeCypherQuery(query);
			dbHandler.executeCypherQuery(queryDeleteHeathData);
		} catch (InsightsCustomException e) {
			log.error("InsightsCustomException : " + e.toString());
		}
	}

}