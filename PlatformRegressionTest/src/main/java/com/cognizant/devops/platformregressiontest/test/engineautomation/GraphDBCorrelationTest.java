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
package com.cognizant.devops.platformregressiontest.test.engineautomation;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

public class GraphDBCorrelationTest extends LoginAndSelectModule {
	private static final Logger log = LogManager.getLogger(GraphDBCorrelationTest.class);
	GraphdbCorrelationConfiguration clickAllActionButton;
	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 * @throws InterruptedException
	 * @throws IOException 
	 */
	@BeforeTest
	public void setUp() throws InterruptedException {
		getData(ConfigOptionsTest.ENGINE_AUTO_DIR + File.separator + ConfigOptionsTest.CONFIGURATION_JSON_FILE);
		clickAllActionButton = new GraphdbCorrelationConfiguration();
	}

	/**
	 * Assert true if Agent data is present in Neo4j
	 * @throws InterruptedException 
	 */
	@Test(priority = 1)
	public void verifyAgentDataInGraphDB() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.testAgentLabelNode(), "Agent data is present in Neo4j");
	}
	
	/**
	 * Assert true if correlation data is present
	 * @throws InterruptedException 
	 */
	@Test(priority = 2)
	public void verifyCorrelationDataInGraphDB() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.testCorrelation(), "Correlation data is present in Neo4j");
	}
	
	/**
	 * Assert true if Data enrichment data is present
	 * @throws InterruptedException 
	 */
	@Test(priority = 3)
	public void verifyTraceabilityDataInGraphDB() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.testTraceability(), "Traceability data is present in Neo4j");
	}
	
	/**
	 * Assert true if Data enrichment data is present
	 * @throws InterruptedException 
	 */
	@Test(priority = 4)
	public void verifyDataEnrichmentInGraphDB() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.testDataEnrichment(), "Data Enrichment is present in Neo4j");
	}
	
	/**
	 * Assert true if agent data is present
	 * @throws InsightsCustomException 
	 */
	@Test(priority = 5)
	public void verifyAgentInPostgres() throws InsightsCustomException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.testRegisterAgentInDatabase(), "agent is present in postgres");
	}
}
