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
package com.cognizant.devops.engines.platformengine.message.subscriber;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformengine.modules.aggregator.BusinessMappingData;
import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.OfflineDataProcessingExecutor;
import com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.model.DataEnrichmentModel;
import com.cognizant.devops.engines.platformengine.test.engine.EngineAggregatorCorelationModuleTest;
import com.cognizant.devops.engines.platformengine.test.engine.EngineTestData;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;


/**
 * This is a test class for OfflineDataProcessingExecutor
 * 
 * @author 368419
 *
 */
@Test
public class AgentHealthSubscriberTest {
	final int NUM_OF_JSON_FILES = 6;
	final String JSON_FILE_NAME = "data-enrichment.JSON";
	final String INCORRECT_JSON_FILE_NAME = "data-enrichment.py";
	final String FILE_TYPE = "Json";
	final String LOCAL_DATE_FORMAT = "yyyy/MM/dd hh:mm a";
	final String EXECUTION_DATE_TIME = "2018/07/16 12:53 PM";
	String WRONG_EXECUTION_TIME = "2018/07/16 05:50 PM";
	public static final String  DATA_ENRICHMENT_TEMPLATE_ERROR = "data-enrichment-error.json";
	public static final String  DATA_ENRICHMENT_TEMPLATE_NO_QUERY = "data-enrichment-NoCypherQry.json";
	String filePath = ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR + ConfigOptions.DATA_ENRICHMENT_TEMPLATE;
	String filePathError = ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR + DATA_ENRICHMENT_TEMPLATE_ERROR;
	String filePathNoQuery = ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR + DATA_ENRICHMENT_TEMPLATE_NO_QUERY;
	String CYPHERQUERY = "cypherQuery";
	String CYPHERQUERYNEGATIVECASE = "cypherQueryNegativeCase";
	String RUNSCHEDULE = "720";
	String LASTRUNTIME = "2018/07/16 05:50 PM";
	String CRON = "0 9 * * 1 ?";
	String WRONG_CRON = "0 9 * * 1";
	AgentHealthSubscriberTest executor = null;
	JsonObject testData = new JsonObject();
	
	JsonObject engineAggregTestData = new JsonObject();
	private FileReader reader = null;
	private Properties p = null;
	private AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	private static Logger log = LogManager.getLogger(AgentHealthSubscriberTest.class.getName());
	/*
	 * Start Engine for Data Collection and Node Creation *
	 */
	EngineAggregatorModule em = new EngineAggregatorModule();
	
	@BeforeClass
	public void onInit() throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
				+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "AgentDataSubscriber.json";
		testData = JsonUtils.getJsonData(path).getAsJsonObject();
		
		String enginePath = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
				+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "EngineAggregatorCorelationModule.json";
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
					EngineTestData.agentVersion, EngineTestData.osversion, EngineTestData.updateDate, false,false);
			/******************************************************************************************/

			agentConfigDAL.saveAgentConfigFromUI(p.getProperty("jenkinsAgentId"), EngineTestData.jenkinToolCategory,
					EngineTestData.jenkinLabelName, "jenkins",
					engineAggregTestData.get("jenkinsConfig").getAsJsonObject(), EngineTestData.agentVersion,
					EngineTestData.osversion, EngineTestData.updateDate, false,false);

			CorrelationConfiguration saveCorrelationJson = EngineTestData
					.loadCorrelation(engineAggregTestData.get("saveDataConfig").toString());

			correlationConfigDAL.saveCorrelationConfig(saveCorrelationJson);
			
			//for saving data enrichment record
			configFilesDAL.saveConfigurationFile(EngineTestData.createDataEnrichmentData());
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		Thread.sleep(1000);

		/*
		 * Publish Messages to MQ *
		 */
		EngineTestData.publishMessage(engineAggregTestData.get("gitQueueName").getAsString(), engineAggregTestData.get("gitRoutingKey").getAsString(),
				engineAggregTestData.get("rabbitMQGITTestPlayload").toString());
		EngineTestData.publishMessage(engineAggregTestData.get("jenkinQueueName").getAsString(), engineAggregTestData.get("jenkinsRoutingKey").getAsString(),
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

		log.debug("Test Data flow has been created successfully");
	}
	
	@BeforeMethod
	protected void setUp() throws Exception {
		executor = new AgentHealthSubscriberTest();
	}

	@AfterMethod
	protected void tearDown() throws Exception {
	}
	
}
