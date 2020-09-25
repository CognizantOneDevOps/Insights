/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformwebhookengine.modules.aggregator.WebHookEngineAggregatorModule;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;

public class WebhookEngineTest {

	private static Logger log = LogManager.getLogger(WebhookEngineTest.class.getName());
	WebHookConfigDAL webhookConfigDAL = new WebHookConfigDAL();
	WebhookEngineTestData webhookEngineTestData = new WebhookEngineTestData();
	private FileReader reader = null;
	private Properties p = null;

	@BeforeTest
	public void onInit() throws IOException {
		ApplicationConfigCache.loadConfigCache();

		reader = new FileReader("src/test/resources/Properties.prop");

		p = new Properties();

		p.load(reader);

		log.debug("Test Data flow has been created successfully");
	}

	@Test(priority = 1)
	public void testPublishDataToMQ() throws IOException, TimeoutException, InterruptedException {
		/*
		 * Push Data to MQ *
		 */
		webhookEngineTestData.publishMessage(webhookEngineTestData.mqChannel, webhookEngineTestData.labelName,
				webhookEngineTestData.toolData);

		/*
		 * Publish Health message to q 
		 * */
		webhookEngineTestData.publishMessage(webhookEngineTestData.WEBHOOK_SUBSCRIBER_HEALTH_QUEUE,
				webhookEngineTestData.WEBHOOK_SUBSCRIBER_HEALTH_ROUTING_KEY,
				webhookEngineTestData.healthWebhookData);
		Thread.sleep(1000);
	}

	@Test(priority = 2)
	public void testPushDataToPostgre() throws InsightsCustomException, InterruptedException {
		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(webhookEngineTestData.responseTemplate);
		webhookConfig.setDynamicTemplate(webhookEngineTestData.dynamicTemplate);
		webhookConfig.setLabelName(webhookEngineTestData.labelName);
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(webhookEngineTestData.toolName);
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
		webhookConfig.setDataFormat(p.getProperty("dataFormat"));
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArray);
		webhookConfigDAL.saveWebHookConfiguration(webhookConfig);

		List<WebHookConfig> activeWebhookList = webhookConfigDAL.getAllActiveWebHookConfigurations();
		Assert.assertTrue(!activeWebhookList.isEmpty());
		Thread.sleep(1000);
	}

	@Test(priority = 3)
	public void testEngineData() {
		WebHookEngineAggregatorModule em = new WebHookEngineAggregatorModule();
		em.run();
	}

	@Test(priority = 4)
	public void testEngineDataWithUpdate()
			throws IOException, TimeoutException, InterruptedException, InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(webhookEngineTestData.responseTemplate);
		webhookConfig.setDynamicTemplate(webhookEngineTestData.dynamicTemplate);
		webhookConfig.setLabelName(webhookEngineTestData.labelName);
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(webhookEngineTestData.toolName);
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(true);
		webhookConfig.setFieldUsedForUpdate(webhookEngineTestData.fieldUsedForUpdate);
		webhookConfig.setDataFormat(p.getProperty("dataFormat"));
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArray);
		webhookConfigDAL.updateWebHookConfiguration(webhookConfig);
		webhookEngineTestData.publishMessage(webhookEngineTestData.mqChannel, webhookEngineTestData.labelName,
				webhookEngineTestData.toolData);
		Thread.sleep(1000);
		WebHookEngineAggregatorModule em = new WebHookEngineAggregatorModule();
		em.run();
		Thread.sleep(1000);
	}

	@Test(priority = 5)
	public void testEngineDataDerivedOperationWithoutEpoch()
			throws IOException, TimeoutException, InterruptedException, InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(webhookEngineTestData.responseTemplate);
		webhookConfig.setDynamicTemplate(webhookEngineTestData.dynamicTemplate);
		webhookConfig.setLabelName(webhookEngineTestData.labelName);
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(webhookEngineTestData.toolName);
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(true);
		webhookConfig.setFieldUsedForUpdate(webhookEngineTestData.fieldUsedForUpdate);
		webhookConfig.setDataFormat(p.getProperty("dataFormat"));
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArrayWithoutEpoch);
		webhookConfigDAL.updateWebHookConfiguration(webhookConfig);
		webhookEngineTestData.publishMessage(webhookEngineTestData.mqChannel, webhookEngineTestData.labelName,
				webhookEngineTestData.toolData);
		Thread.sleep(1000);
		WebHookEngineAggregatorModule em = new WebHookEngineAggregatorModule();
		em.run();
		Thread.sleep(1000);
	}

	@Test(priority = 6)
	public void testNeo4JData() {
		/*
		 * Test GIT node is created *
		 */
		Map map = WebhookEngineTestData.readNeo4JData(webhookEngineTestData.labelName, p.getProperty("compareFlag"));
		/* Assert on commitId */
		Assert.assertEquals(webhookEngineTestData.commitId, map.get("commitId"));
		/* Assert on toolname */
		Assert.assertEquals(p.getProperty("toolName"), map.get("toolName"));
		/* Assert on categoryName */
		Assert.assertEquals(p.getProperty("webhookName"), map.get("webhookName"));
	}

	@Test(priority = 7)
	public void testEngineReRunAfterSpecifcTime() throws InterruptedException {
		WebHookEngineAggregatorModule em = new WebHookEngineAggregatorModule();
		em.run();
		Thread.sleep(1000);
	}

	/*@Test(priority = 8)
	public void testEngineAgreegateException() throws Exception {
		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(webhookEngineTestData.responseTemplate);
		webhookConfig.setDynamicTemplate(webhookEngineTestData.dynamicTemplate);
		webhookConfig.setToolName(webhookEngineTestData.toolName);
		webhookConfig.setWebHookName(webhookEngineTestData.webhookNameException);
		webhookConfig.setMQChannel("");
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
		webhookConfig.setFieldUsedForUpdate(webhookEngineTestData.fieldUsedForUpdate);
		webhookConfig.setDataFormat(p.getProperty("dataFormat"));
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArray);
		webhookConfigDAL.saveWebHookConfiguration(webhookConfig);
		WebHookEngineAggregatorModule em = new WebHookEngineAggregatorModule();
		em.run();
	}*/

	@AfterTest
	public void cleanUp() {

		// Cleaning Postgre 
		webhookConfigDAL.deleteWebhookConfigurations(p.getProperty("webhookName"));
		//webhookConfigDAL.deleteWebhookConfigurations(webhookEngineTestData.webhookNameException);
		// Cleaning Neo4J 
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (p:" + webhookEngineTestData.labelName + ") where p.webhookName="
				+ webhookEngineTestData.webhookName + " delete p";
		String queryDeleteHeathData = "MATCH (p:" + webhookEngineTestData.WEBHOOK_SUBSCRIBER_HEALTH_ROUTING_KEY
				+ ") where p.instanceName="
				+ webhookEngineTestData.healthMessageInstanceName + " delete p";
		try {
			dbHandler.executeCypherQuery(query);
			dbHandler.executeCypherQuery(queryDeleteHeathData);
		} catch (InsightsCustomException e) {
			log.error("InsightsCustomException : " + e.toString());
		}
	}

}