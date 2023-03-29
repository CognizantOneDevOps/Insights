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
package com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.model.DataEnrichmentModel;
import com.cognizant.devops.engines.platformengine.test.engine.EngineTestData;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.google.gson.JsonObject;


/**
 * This is a test class for OfflineDataProcessingExecutor
 * 
 * @author 368419
 *
 */
@Test
public class OfflineDataProcessingExecutorTest {
	private static final Logger log = LogManager.getLogger(OfflineDataProcessingExecutorTest.class.getName());

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
	OfflineDataProcessingExecutor executor = null;
	JsonObject offlineTestData = new JsonObject();	

	
	@BeforeClass
	public void onInit() throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
				+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "OfflineDataProvider.json";
		offlineTestData = JsonUtils.getJsonData(path).getAsJsonObject();
	}
	
	@BeforeMethod
	protected void setUp() throws Exception {
		executor = new OfflineDataProcessingExecutor();
	}

	@AfterMethod
	protected void tearDown() throws Exception {
	}
	
    /**
     * Positive test case for executeOfflineProcessing()
     * Checks no. of json files inside  data-enrichment folder
     **/
  /*  @Test
	public void testExecuteOfflineProcessing() {
    	AssertJUnit.assertEquals(5, executor.executeOfflineProcessing());		
	}
    */
    /**
     * Negative test case for executeOfflineProcessing()
     * Checks no. of json files inside  data-enrichment folder
     */
    @Test(priority = 1)
	public void testExecuteOfflineProcessingNegative() {
    	AssertJUnit.assertNotSame(NUM_OF_JSON_FILES, executor.executeOfflineProcessing());		
	}
    
    /**
     * Positive test case for hasJsonFileExtension() method
     * Checks whether input file has json extension or not
     * 
     */
    @Test(priority = 2)
	public void testHasJsonFileExtension() {    	
    	boolean hasJsonFileExtension = executor.hasJsonFileExtension(JSON_FILE_NAME);
    	assertTrue(hasJsonFileExtension);
    }   
   
    
    /*
     * Negative test case for hasJsonFileExtension() method
     * Checks whether input file has json extension or not
     * 
     */
    @Test(priority = 3)
	public void testHasJsonFileExtensionNegative() {
    	boolean hasJsonFileExtension = executor.hasJsonFileExtension(INCORRECT_JSON_FILE_NAME);
    	AssertJUnit.assertEquals(false, hasJsonFileExtension);
    }
    
    /*
     * Positive test case for processOfflineConfiguration() method
     */
    
    @Test(priority = 4)
    public void testProcessOfflineConfiguration() {
		try {
			InsightsConfigFiles configFile = EngineTestData.createDataEnrichmentDataFile(filePath);
			Boolean resultFlag = executor.processOfflineConfiguration(configFile);
			assertTrue(resultFlag);
		} catch (Exception e) {
			log.error(e);
		}
    }
    
    /**
     * Negative test case for processOfflineConfiguration() method
     */
    @Test(priority = 5)
    public void testProcessOfflineConfigurationIllegalStateException() {
		try {
			InsightsConfigFiles configFile = EngineTestData.createDataEnrichmentDataFile(filePathError);
			Boolean resultFlag = executor.processOfflineConfiguration(configFile);
			assertFalse(resultFlag);
		} catch (Exception e) {
			log.error(e);
		}
    }
    
    /**
     * Negative test case for processOfflineConfiguration() method
     */
    @Test(priority = 6)
    public void testProcessOfflineConfigurationNoQuery() {
		try {
			InsightsConfigFiles configFile = EngineTestData.createDataEnrichmentDataFile(filePathNoQuery);
			Boolean resultFlag = executor.processOfflineConfiguration(configFile);
			assertTrue(resultFlag);
		} catch (Exception e) {
			log.error(e);
		}
    }
    
   /**
     * Positive test case for updateLastExecutionTime() method
     */
    @Test(priority = 7)
	public void testUpdateLastExecutionTime() {
    	DataEnrichmentModel dataEnrichmentModel = new DataEnrichmentModel();
    	dataEnrichmentModel.setLastExecutionTime(EXECUTION_DATE_TIME);
    	DataEnrichmentModel resultModel = executor.updateLastExecutionTime(dataEnrichmentModel);
    	String currentTime = InsightsUtils.getLocalDateTime(LOCAL_DATE_FORMAT);    	
    	AssertJUnit.assertEquals(currentTime, resultModel.getLastExecutionTime());    	
    }
    
    /**
     * Negative test case for updateLastExecutionTime() method
     */
    @Test(priority = 8)
	public void testUpdateLastExecutionTimeNegative() {
    	DataEnrichmentModel dataEnrichmentModel = new DataEnrichmentModel();
    	dataEnrichmentModel.setLastExecutionTime(EXECUTION_DATE_TIME);
    	DataEnrichmentModel resultModel = executor.updateLastExecutionTime(dataEnrichmentModel);
    	AssertJUnit.assertNotSame(WRONG_EXECUTION_TIME, resultModel.getLastExecutionTime());    	
    }
    
    /**
     * Positive test case for executeCypherQuery() method
     */
    @Test(priority = 9)
	public void testExecuteCypherQuery() {
    	DataEnrichmentModel model = new DataEnrichmentModel();
    	boolean resultFlag = executor.executeCypherQuery(offlineTestData.get(CYPHERQUERY).getAsString(), model);
    	assertTrue(resultFlag);    	
    }
    
    /**
     * Negative test case for executeCypherQuery() method
     * 
     */
    @Test(priority = 10)
	public void testExecuteCypherQueryNegative() {
    	DataEnrichmentModel model = new DataEnrichmentModel();
    	boolean resultFlag = executor.executeCypherQuery(offlineTestData.get(CYPHERQUERYNEGATIVECASE).getAsString(), model);
    	assertFalse(resultFlag);    
    }
    
  /**
     * Positive test case for isQueryScheduledToRun() method
     */
    @Test(priority = 11)
	public void testIsQueryScheduledToRun() {
    	assertTrue(executor.isQueryScheduledToRun(Long.valueOf(RUNSCHEDULE), LASTRUNTIME, null));   			
    }
    
  /**
     * Negative test case for isQueryScheduledToRun() method
     */
    @Test(priority = 12)
	public void testIsQueryScheduledToRunNegative() {
    	assertTrue(executor.isQueryScheduledToRun(Long.valueOf(RUNSCHEDULE), LASTRUNTIME, CRON));   			    	
    }
    
    @Test(priority = 13)
	public void testIsQueryScheduledToRunNoRunSchedule() {
    	assertTrue(executor.isQueryScheduledToRun(null, LASTRUNTIME, CRON));   			    	
    }

    @Test(priority = 14)
	public void testIsQueryScheduledToRunWrongCronData() {
    	assertFalse(executor.isQueryScheduledToRun(Long.valueOf(RUNSCHEDULE), LASTRUNTIME, WRONG_CRON));   			    	
    }
    
    @Test(priority = 15)
	public void testIsQueryScheduledToRunNoLastRun() {
    	assertTrue(executor.isQueryScheduledToRun(Long.valueOf(RUNSCHEDULE), null, null));   			    	
    }
    
    @Test(priority = 16)
	public void testIsQueryScheduledToRunNoLastRunForDateTime() {
    	assertFalse(executor.isQueryScheduledToRun(Long.valueOf(RUNSCHEDULE), null, WRONG_CRON));   			    	
    }
}
