package com.cognizant.devops.platformengine.test.engine;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.platformengine.modules.correlation.EngineCorrelatorModule;

public class EngineAggregatorCorelationModuleTest {

	private static Logger log = LogManager.getLogger(EngineAggregatorCorelationModuleTest.class.getName());

	private AgentConfigDAL agentConfigDAL = new AgentConfigDAL();

	
	@BeforeTest
	public void onInit() throws InterruptedException {

		ApplicationConfigCache.loadConfigCache();
		/*
		 * Insert Test Data into Postgre * 
		 */

		agentConfigDAL.saveAgentConfigFromUI("GITTEST8800", EngineTestData.gitToolCategory, "git",
				EngineTestData.getJsonObject(EngineTestData.gitConfig), EngineTestData.agentVersion,
				EngineTestData.osversion, EngineTestData.updateDate);
	/******************************************************************************************/

		agentConfigDAL.saveAgentConfigFromUI("JENKINSTEST8800", EngineTestData.jenkinToolCategory, "jenkins",
				EngineTestData.getJsonObject(EngineTestData.jenkinsConfig), EngineTestData.agentVersion,
				EngineTestData.osversion, EngineTestData.updateDate);
				
		 Thread.sleep(1000);
		
			/*
			 * Publish Messages to MQ * 
			 */
		 		 
		 EngineTestData.publishMessage(EngineTestData.gitQueueName,EngineTestData.gitRoutingKey,EngineTestData.rabbitMQGITTestPlayload);

		 EngineTestData.publishMessage(EngineTestData.jenkinQueueName,EngineTestData.jenkinsRoutingKey,EngineTestData.rabbitMQJENKINSTestPayload);

		 Thread.sleep(1000);
		 
			/*
			 * Start Engine for Data Collection and Node Creation * 
			 */
		 
		 EngineAggregatorModule em= new EngineAggregatorModule(); em.run();
		 
		 Thread.sleep(1000);
		 
		 /*
		  * Start Engine for Correlation * 
		  */
		 
		 EngineCorrelatorModule ecm = new EngineCorrelatorModule();ecm.run();
		 
		 log.debug("Test Data flow has been created successfully");
	}

	@Test(priority=1)
	public void testNeo4JData() {
		/*
		 * Test GIT node is created	 * 
		 */		
		@SuppressWarnings("rawtypes")
		Map map = EngineTestData.readNeo4JData("SCM:GIT:DATA", "commitId");
		/* Assert on commitId */
		AssertJUnit.assertEquals("CommitId", "CM-7569369619", map.get("commitId"));
		/* Assert on toolname */
		AssertJUnit.assertEquals("toolName", "GIT", map.get("toolName"));
		/* Assert on categoryName */
		AssertJUnit.assertEquals("categoryName", "SCM", map.get("categoryName"));

	/*
	 * Test Jenkins node is created	 * 
	 */
	
		map = EngineTestData.readNeo4JData("CI:JENKINS:DATA", "scmcommitId");
		/* Assert on commitId */
		AssertJUnit.assertEquals("scmcommitId", "CM-7569369619", map.get("scmcommitId"));
		/* Assert on toolname */
		AssertJUnit.assertEquals("toolName", "JENKINS", map.get("toolName"));
		/* Assert on categoryName */
		AssertJUnit.assertEquals("categoryName", "CI", map.get("categoryName"));

	}
	
	@Test(priority=2)
	public void testCorrelation() {
		
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (p:DATA {toolName:'GIT'}), (q:DATA {toolName:'JENKINS'}) RETURN EXISTS( (p)-[:TEST_FROM_GIT_TO_JENKINS]->(q))";
		GraphResponse neo4jResponse;
		try {

		neo4jResponse = dbHandler.executeCypherQuery(query);
		
		String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");
		
		/* Assert on Node Relationship */

		AssertJUnit.assertEquals("true",finalJson.toString());

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
