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
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.platformengine.modules.correlation.EngineCorrelatorModule;

public class EngineAggregatorCorelationModuleTest {
	private static Logger log = LogManager.getLogger(EngineAggregatorCorelationModuleTest.class.getName());

	private AgentConfigDAL agentConfigDAL = new AgentConfigDAL();

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


		agentConfigDAL.saveAgentConfigFromUI(p.getProperty("gitAgentId"), EngineTestData.gitToolCategory,
				EngineTestData.gitLabelName, "git", EngineTestData.getJsonObject(EngineTestData.gitConfig),
				EngineTestData.agentVersion, EngineTestData.osversion, EngineTestData.updateDate, false);
		/******************************************************************************************/

		agentConfigDAL.saveAgentConfigFromUI(p.getProperty("jenkinsAgentId"), EngineTestData.jenkinToolCategory,
				EngineTestData.jenkinLabelName, "jenkins", EngineTestData.getJsonObject(EngineTestData.jenkinsConfig),
				EngineTestData.agentVersion, EngineTestData.osversion, EngineTestData.updateDate, false);

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

		EngineCorrelatorModule ecm = new EngineCorrelatorModule();
		ecm.run();

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
	public void testCorrelation() {

		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (p:DATA {commitId:'CM-7569369619'}), (q:DATA {scmcommitId:'CM-7569369619'}) RETURN distinct  EXISTS( (p)-[:TEST_FROM_GIT_TO_JENKINS]->(q))";
		GraphResponse neo4jResponse;
		try {

			neo4jResponse = dbHandler.executeCypherQuery(query);

			String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");

			/* Assert on Node Relationship */

			Assert.assertEquals("true", finalJson.toString());

		} catch (GraphDBException e) {

			log.error("GraphDBException : " + e.toString());
		}
	}

   @AfterTest
	public void cleanUp() {

		/* Cleaning Postgre */

		agentConfigDAL.deleteAgentConfigurations("JENKINSTEST8800");
		agentConfigDAL.deleteAgentConfigurations("GITTEST8800");

		/* Cleaning Neo4J */

		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH p=()-[r:TEST_FROM_GIT_TO_JENKINS]->() delete p";
		try {

			dbHandler.executeCypherQuery(query);

		} catch (GraphDBException e) {

			log.error("GraphDBException : " + e.toString());
		}

	}

}