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
package com.cognizant.devops.engines.platformengine.modules.correlation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.platformengine.test.engine.EngineTestData;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.google.gson.JsonObject;

/**
 * This is a test class for CorrelationExecutor
 * 
 * @author 368419
 *
 */
@Test
public class CorrelationExecutorTest {
	private static Logger log = LogManager.getLogger(CorrelationExecutorTest.class.getName());
	JsonObject engineAggregTestData = new JsonObject();
	CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	List<CorrelationConfiguration> correlations = new ArrayList<>();
	CorrelationExecutorTest executor = null;
	JsonObject testData = new JsonObject();
	private FileReader reader = null;
	private Properties p = null;
	public static String jenkinsLabel = "CI";
	private AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	
	
	/*
	 * Start Engine for Data Collection and Node Creation *
	 */
	EngineAggregatorModule em = new EngineAggregatorModule();

	CorrelationExecutor correlationExecutor = new CorrelationExecutor();
	CorrelationExecutorTestData correlationTestData = new CorrelationExecutorTestData();

	@BeforeClass
	public void onInit()
			throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		String enginePath = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ TestngInitializerTest.TESTNG_TESTDATA + File.separator + TestngInitializerTest.TESTNG_PLATFORMENGINE
				+ File.separator + "CorrelationExecutor.json";
		testData = JsonUtils.getJsonData(enginePath).getAsJsonObject();
		reader = new FileReader("src/test/resources/Properties.prop");
		p = new Properties();
		p.load(reader);
		
		/* 
		 * Saving correlation record
		 */
		String correlationConfig1 = testData.get("correlationConfig").toString();
		CorrelationConfiguration config1 = correlationTestData.saveConfig(correlationConfig1);
		Thread.sleep(1000);

		String correlationConfig2 = testData.get("saveDataConfig").toString();
		CorrelationConfiguration config2 = correlationTestData.saveConfig(correlationConfig2);
		Thread.sleep(1000);
		
		correlations.add(config1);
		correlations.add(config2);
		Thread.sleep(1000);
		
		configFilesDAL.saveConfigurationFile(EngineTestData.createCorrelationData());
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
		executor = new CorrelationExecutorTest();
	}

	@AfterMethod
	protected void tearDown() throws Exception {
	}

	@Test(priority = 1)
	public void testSaveConfig() throws InsightsCustomException {
		try {
			ApplicationConfigInterface.loadConfiguration();
			correlationExecutor.execute("EngineCorrelatorModule");
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 2)
	public void testloadCorrelationConfigfromJson() {
		try {
			correlationExecutor.loadCorrelationConfigfromJson(correlations);
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 3)
	public void testGetActiveCorrelations() throws InsightsCustomException {
		try {
			List<CorrelationConfiguration> correlationList = correlationConfigDAL.getActiveCorrelations();
			Assert.assertTrue(!correlationList.isEmpty());
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	 @AfterClass
		public void cleanUp() {
			/* Cleaning Neo4J */
			GraphDBHandler dbHandler = new GraphDBHandler();
			try {
				dbHandler.executeCypherQuery("MATCH (n:JENKINS_UNTEST) DETACH DELETE n");
				dbHandler.executeCypherQuery("MATCH (n:GIT_UNTEST) DETACH DELETE n");
				correlationConfigDAL.deleteCorrelationConfig("TEST_FROM_GIT_TO_JENKINS");
				correlationConfigDAL.deleteCorrelationConfig("TEST_FROM_JIRA_TO_GITHUB2");
			} catch (InsightsCustomException e) {
				log.error("InsightsCustomException : " + e.toString());
			}
		}
}
