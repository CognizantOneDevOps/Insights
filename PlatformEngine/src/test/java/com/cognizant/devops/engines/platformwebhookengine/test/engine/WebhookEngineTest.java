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
package com.cognizant.devops.engines.platformwebhookengine.test.engine;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;
import com.cognizant.devops.engines.platformwebhookengine.modules.aggregator.WebHookEngineAggregatorModule;

public class WebhookEngineTest {

	private static Logger log = LogManager.getLogger(WebhookEngineTest.class.getName());
	WebHookConfigDAL webhookConfigDAL= new WebHookConfigDAL();
	WebHookConfig webhookConfig = new WebHookConfig();
    private FileReader reader=null;
	private Properties p =null;
		
	@BeforeTest
	public void onInit() throws IOException
	{
		ApplicationConfigCache.loadConfigCache();
		
		reader=new FileReader("src/test/resources/Properties.prop");  
	      
		p=new Properties();  
		
		p.load(reader);		
		
		log.debug("Test Data flow has been created successfully");
	}
	
	@Test (priority=2)
	public void testPublishDataToMQ() throws IOException, TimeoutException, InterruptedException
	{
		/*
		 * Push Data to MQ *
		 */
		EngineTestData.publishMessage(EngineTestData.mqChannel, EngineTestData.labelName,
		EngineTestData.toolData);
		Thread.sleep(1000);
	}
	
	@Test(priority=1)
	public void testPushDataToPostgre() throws InsightsCustomException, InterruptedException
	{
	    webhookConfig.setResponseTemplate(EngineTestData.responseTemplate);
		webhookConfig.setLabelName(EngineTestData.labelName);
		webhookConfig.setMQChannel(EngineTestData.mqChannel);
		webhookConfig.setToolName(EngineTestData.toolName);
		webhookConfig.setWebHookName(EngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setDataFormat(p.getProperty("dataFormat"));
		webhookConfigDAL.saveWebHookConfiguration(webhookConfig);
		Thread.sleep(1000);
	}	
	@Test(priority=3)
	public void testEngine()
	{
		WebHookEngineAggregatorModule em = new WebHookEngineAggregatorModule();
		em.run();
	}
	
	@Test(priority = 4)
	public void testNeo4JData() {
		/*
		 * Test GIT node is created *
		 */
		@SuppressWarnings("rawtypes")
		Map map = EngineTestData.readNeo4JData(p.getProperty("nodeName"), p.getProperty("compareFlag"));
		/* Assert on commitId */
		Assert.assertEquals(p.getProperty("gitwebhookCommitId"), map.get("commitId"));
		/* Assert on toolname */
		Assert.assertEquals(p.getProperty("toolName"), map.get("toolName"));
		/* Assert on categoryName */
		Assert.assertEquals(p.getProperty("webhookName"), map.get("webhookName"));
	}
	
	
	
	
	@AfterTest
	public void cleanUp() {

		/* Cleaning Postgre */
		webhookConfigDAL.deleteWebhookConfigurations(p.getProperty("webhookName"));
		/* Cleaning Neo4J */
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (p:DATA) where p.webhookName='git_demo' delete p";
		try {
			dbHandler.executeCypherQuery(query);
		} catch (GraphDBException e) {
			log.error("GraphDBException : " + e.toString());
		}
	}

}