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
package com.cognizant.devops.engines.platformengine.test.engine;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.platformengine.modules.correlation.EngineCorrelatorModule;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;

public class EngineAggregatorCorelationModuleTest {
	private static Logger log = LogManager.getLogger(EngineAggregatorCorelationModuleTest.class.getName());

	private AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();

	private FileReader reader = null;

	private Properties p = null;

	@BeforeTest
	public void onInit() throws InterruptedException, IOException {

		ApplicationConfigCache.loadConfigCache();

		reader = new FileReader("src/test/resources/Properties.prop");

		p = new Properties();

		p.load(reader);

		/*
		 * Insert Test Data into Postgre *
		 */


		try {
			agentConfigDAL.saveAgentConfigFromUI(p.getProperty("gitAgentId"), EngineTestData.gitToolCategory,
					EngineTestData.gitLabelName, "git", EngineTestData.getJsonObject(EngineTestData.gitConfig),
					EngineTestData.agentVersion, EngineTestData.osversion, EngineTestData.updateDate, false);
			/******************************************************************************************/

			agentConfigDAL.saveAgentConfigFromUI(p.getProperty("jenkinsAgentId"), EngineTestData.jenkinToolCategory,
					EngineTestData.jenkinLabelName, "jenkins",
					EngineTestData.getJsonObject(EngineTestData.jenkinsConfig), EngineTestData.agentVersion,
					EngineTestData.osversion, EngineTestData.updateDate, false);

			CorrelationConfiguration saveCorrelationJson = EngineTestData
					.loadCorrelation(EngineTestData.saveDataConfig);

			correlationConfigDAL.saveCorrelationConfig(saveCorrelationJson);
		} catch (InsightsCustomException e) {
			log.error(e);
		}

		Thread.sleep(1000);

		/*
		 * Publish Messages to MQ *
		 */

		EngineTestData.publishMessage(EngineTestData.gitQueueName, EngineTestData.gitRoutingKey,
				EngineTestData.rabbitMQGITTestPlayload);
		EngineTestData.publishMessage(EngineTestData.jenkinQueueName, EngineTestData.jenkinsRoutingKey,
				EngineTestData.rabbitMQJENKINSTestPayload);
		Thread.sleep(1000);

		/*
		 * Start Engine for Data Collection and Node Creation *
		 */

		EngineAggregatorModule em = new EngineAggregatorModule();
		em.run();
		Thread.sleep(1000);

		/* Start Engine for Correlation **/

		ApplicationConfigProvider.getInstance().getCorrelations().setBatchSize(1000);
		ApplicationConfigProvider.getInstance().getCorrelations().setCorrelationFrequency(1);
		ApplicationConfigProvider.getInstance().getCorrelations().setCorrelationWindow(-1);

		log.debug("Test Data flow has been created successfully");
	}

	@Test(priority = 1)
	public void testNeo4JData() {
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

	}

	@Test(priority = 2)
	public void testSaveConfig() throws InsightsCustomException {
		try {

			List<CorrelationConfiguration> correlationList = correlationConfigDAL.getActiveCorrelations();
			Assert.assertTrue(!correlationList.isEmpty());
			Thread.sleep(5000);

			EngineCorrelatorModule ecm = new EngineCorrelatorModule();
			ecm.run();
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

   @AfterTest
	public void cleanUp() {

		/* Cleaning Postgre */

		try {
			agentConfigDAL.deleteAgentConfigurations("JENKINSTEST8800");
			agentConfigDAL.deleteAgentConfigurations("GITTEST8800");
			correlationConfigDAL.deleteCorrelationConfig("TEST_FROM_GIT_TO_JENKINS");
		} catch (InsightsCustomException e1) {
			log.error(e1);
		}

		ApplicationConfigProvider.getInstance().getCorrelations().setBatchSize(2000);
		ApplicationConfigProvider.getInstance().getCorrelations().setCorrelationFrequency(3);
		ApplicationConfigProvider.getInstance().getCorrelations().setCorrelationWindow(48);

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