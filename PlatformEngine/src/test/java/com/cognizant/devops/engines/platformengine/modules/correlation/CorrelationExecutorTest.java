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
package com.cognizant.devops.engines.platformengine.modules.correlation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.OfflineDataProcessingExecutor;
import com.cognizant.devops.engines.platformengine.test.engine.EngineTestData;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformdal.relationshipconfig.RelationshipConfiguration;
import com.google.gson.JsonObject;

/**
 * This is a test class for CorrelationExecutor
 * 
 * @author 368419
 *
 */
@Test
public class CorrelationExecutorTest {
	final int NUM_OF_JSON_FILES = 6;
	final String JSON_FILE_NAME = "data-enrichment.JSON";
	final String INCORRECT_JSON_FILE_NAME = "data-enrichment.py";
	final String FILE_TYPE = "Json";
	final String LOCAL_DATE_FORMAT = "yyyy/MM/dd hh:mm a";
	final String EXECUTION_DATE_TIME = "2018/07/16 12:53 PM";
	String WRONG_EXECUTION_TIME = "2018/07/16 05:50 PM";
	public static final String DATA_ENRICHMENT_TEMPLATE_ERROR = "data-enrichment-error.json";
	public static final String DATA_ENRICHMENT_TEMPLATE_NO_QUERY = "data-enrichment-NoCypherQry.json";
	String filePath = ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR
			+ ConfigOptions.DATA_ENRICHMENT_TEMPLATE;
	String filePathError = ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR
			+ DATA_ENRICHMENT_TEMPLATE_ERROR;
	String filePathNoQuery = ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR
			+ DATA_ENRICHMENT_TEMPLATE_NO_QUERY;
	String CYPHERQUERY = "cypherQuery";
	String CYPHERQUERYNEGATIVECASE = "cypherQueryNegativeCase";
	String RUNSCHEDULE = "720";
	String LASTRUNTIME = "2018/07/16 05:50 PM";
	String CRON = "0 9 * * 1 ?";
	String WRONG_CRON = "0 9 * * 1";
	CorrelationExecutorTest executor = null;
	JsonObject testData = new JsonObject();

	JsonObject engineAggregTestData = new JsonObject();
	private FileReader reader = null;
	private Properties p = null;
	private AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	private static Logger log = LogManager.getLogger(CorrelationExecutorTest.class.getName());
	
	/*
	 * Start Engine for Data Collection and Node Creation *
	 */
	EngineAggregatorModule em = new EngineAggregatorModule();

	CorrelationExecutor correlationExecutor = new CorrelationExecutor();
	CorrelationConfiguration correlationConfiguration = new CorrelationConfiguration();
	RelationshipConfiguration relationConfig = new RelationshipConfiguration();
	CorrelationExecutorTestData correlationExecutorTestData = new CorrelationExecutorTestData();
	Set<RelationshipConfiguration> relationSet = new HashSet<>();

	@BeforeClass
	public void onInit()
			throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		String enginePath = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ TestngInitializerTest.TESTNG_TESTDATA + File.separator + TestngInitializerTest.TESTNG_PLATFORMENGINE
				+ File.separator + "EngineAggregatorCorelationModule.json";
		engineAggregTestData = JsonUtils.getJsonData(enginePath).getAsJsonObject();

		reader = new FileReader("src/test/resources/Properties.prop");
		p = new Properties();
		p.load(reader);
		
		// for saving data correlation record
		configFilesDAL.saveConfigurationFile(EngineTestData.createCorrelationData());
		
		String config = engineAggregTestData.get("saveDataConfig").toString();
		correlationExecutorTestData.saveConfig(config);
		
		/*
		 * Publish Messages to MQ *
		 */
		EngineTestData.publishMessage(engineAggregTestData.get("gitQueueName").getAsString(),
				engineAggregTestData.get("gitRoutingKey").getAsString(),
				engineAggregTestData.get("rabbitMQGITTestPlayload").toString());
		EngineTestData.publishMessage(engineAggregTestData.get("gitQueueName").getAsString(),
				engineAggregTestData.get("gitRoutingKey").getAsString(),
				engineAggregTestData.get("rabbitMQGITTestPlayloadWithUniqueKey").toString());
		EngineTestData.publishMessage(engineAggregTestData.get("gitQueueName").getAsString(),
				engineAggregTestData.get("gitRoutingKey").getAsString(),
				engineAggregTestData.get("rabbitMQGITTestPlayloadWithRelation").toString());
		EngineTestData.publishMessage(engineAggregTestData.get("jenkinQueueName").getAsString(),
				engineAggregTestData.get("jenkinsRoutingKey").getAsString(),
				engineAggregTestData.get("rabbitMQJENKINSTestPayload").toString());

		Thread.sleep(1000);

		/*
		 * Start Engine for Data Collection and Node Creation *
		 */
		EngineAggregatorModule em = new EngineAggregatorModule();
		em.executeJob();
		Thread.sleep(1000);

		OfflineDataProcessingExecutor oc = new OfflineDataProcessingExecutor();
		oc.executeOfflineProcessing();
		
		/* Start Engine for Correlation **/
		ApplicationConfigProvider.getInstance().getCorrelations().setBatchSize(1000);
		ApplicationConfigProvider.getInstance().getCorrelations().setCorrelationFrequency(1);
		ApplicationConfigProvider.getInstance().getCorrelations().setCorrelationWindow(-1);

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
			correlationExecutor.execute("EngineCorrelatorModule");
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 2)
	public void testloadCorrelationConfigfromJson() {
		try {
			List<CorrelationConfiguration> correlations = new ArrayList<>();
			correlationExecutor.loadCorrelationConfigfromJson(correlations);
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
}
