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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.model.DataEnrichmentModel;


/**
 * This is a test class for OfflineDataProcessingExecutor
 * 
 * @author 368419
 *
 */
public class OfflineDataProcessingExecutorTest {
	
	OfflineDataProcessingExecutor executor = null;

	@BeforeMethod
	protected void setUp() throws Exception {
		ApplicationConfigCache.loadConfigCache();
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
    @Test
	public void testExecuteOfflineProcessingNegative() {
    	AssertJUnit.assertNotSame(6, executor.executeOfflineProcessing());		
	}
    
    /**
     * Positive test case for hasJsonFileExtension() method
     * Checks whether input file has json extension or not
     * 
     */
    @Test
	public void testHasJsonFileExtension() {
    	String fileName = "data-enrichment.JSON";
    	Boolean hasJsonFileExtension = executor.hasJsonFileExtension(fileName);
    	assertTrue(hasJsonFileExtension);
    }   
   
    
    /*
     * Negative test case for hasJsonFileExtension() method
     * Checks whether input file has json extension or not
     * 
     */
    @Test
	public void testHasJsonFileExtensionNegative() {
    	String fileName = "neo4j_import_json.py";
    	Boolean hasJsonFileExtension = executor.hasJsonFileExtension(fileName);
    	AssertJUnit.assertEquals(Boolean.FALSE, hasJsonFileExtension);
    }
    
    /*
     * Positive test case for processOfflineConfiguration() method
     */
    @Test
	public void testProcessOfflineConfiguration() {
    	File configFile = new File(ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR + ConfigOptions.DATA_ENRICHMENT_TEMPLATE);
    	Boolean resultFlag = executor.processOfflineConfiguration(configFile);
    	assertTrue(resultFlag);    	
    }
    
    /**
     * Negative test case for processOfflineConfiguration() method
     */
   /* @Test
	public void testProcessOfflineConfigurationNegative() {
    	File configFile = new File(ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR + "data-enrichment - Copy (3).json");
    	Boolean resultFlag = executor.processOfflineConfiguration(configFile);
    	assertFalse(resultFlag);    	
    }*/
    
   /**
     * Positive test case for updateLastExecutionTime() method
     */
    @Test
	public void testUpdateLastExecutionTime() {
    	DataEnrichmentModel dataEnrichmentModel = new DataEnrichmentModel();
    	dataEnrichmentModel.setLastExecutionTime("2018/07/16 12:53 PM");
    	DataEnrichmentModel resultModel = executor.updateLastExecutionTime(dataEnrichmentModel);
    	String currentTime = InsightsUtils.getLocalDateTime("yyyy/MM/dd hh:mm a");    	
    	AssertJUnit.assertEquals(currentTime, resultModel.getLastExecutionTime());    	
    }
    
    /**
     * Negative test case for updateLastExecutionTime() method
     */
    @Test
	public void testUpdateLastExecutionTimeNegative() {
    	DataEnrichmentModel dataEnrichmentModel = new DataEnrichmentModel();
    	dataEnrichmentModel.setLastExecutionTime("2018/07/16 12:53 PM");
    	DataEnrichmentModel resultModel = executor.updateLastExecutionTime(dataEnrichmentModel);
    	String randomTime = "2018/07/16 05:50 PM";
    	AssertJUnit.assertNotSame(randomTime, resultModel.getLastExecutionTime());    	
    }
    
    /**
     * Positive test case for executeCypherQuery() method
     */
    @Test
	public void testExecuteCypherQuery() {
    	String cypherQuery = "MATCH (n:ALM) where not exists (n.processed) with n limit 100 set n.processed=true return count(n)";
    	DataEnrichmentModel model = new DataEnrichmentModel();
    	Boolean resultFlag = executor.executeCypherQuery(cypherQuery, model);
    	assertTrue(resultFlag);    	
    }
    
    /**
     * Negative test case for executeCypherQuery() method
     * 
     */
    @Test
	public void testExecuteCypherQueryNegative() {
    	String cypherQuery = "MATCH (n:ALM) where not exists (n.processed) with n limit 100 set n.processed=true return n";
    	DataEnrichmentModel model = new DataEnrichmentModel();
    	Boolean resultFlag = executor.executeCypherQuery(cypherQuery, model);
    	assertFalse(resultFlag);    
    }
    
  /**
     * Positive test case for isQueryScheduledToRun() method
     */
    @Test
	public void testIsQueryScheduledToRun() {
    	Long runSchedule = 720L;
    	String lastRunTime = "2018/07/16 05:50 PM";
    	assertTrue(executor.isQueryScheduledToRun(runSchedule, lastRunTime, null));   			
    }
    
  /**
     * Negative test case for isQueryScheduledToRun() method
     */
  /*  @Test
	public void testIsQueryScheduledToRunNegative() {
    	Long runSchedule = 720L;
    	String lastRunTime = "2018/07/17 04:50 PM";
    	assertFalse(executor.isQueryScheduledToRun(runSchedule, lastRunTime, null));   			    	
    }*/

}
