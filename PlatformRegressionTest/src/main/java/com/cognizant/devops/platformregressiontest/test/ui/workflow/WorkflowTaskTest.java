/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.workflow;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.WorkflowDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.WorkflowTakDataModel;

/**
 * @author Tharunaa
 * 
 *         Class contains the test cases for Health Check Module
 *
 */
public class WorkflowTaskTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(WorkflowTaskTest.class);

	WorkflowTaskConfiguration clickAllActionButton;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 */
	@BeforeTest
	public void setUp() {
		initialization();
		getData(ConfigOptionsTest.WORKFLOW_TASK + File.separator + ConfigOptionsTest.WORKFLOW_TASK_JSON_FILE);
		driver.findElement(By.xpath("//a[@title='Task Management']")).click();
		selectMenuOption("Workflow Task Management");
		clickAllActionButton = new WorkflowTaskConfiguration();
	}

	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 * 
	 * @throws InterruptedException
	 */
	@BeforeMethod
	public void beforeMethod() throws InterruptedException {
		Thread.sleep(1000);
	}

	/**
	 * Assert true if landing page displayed successfully
	 */
	@Test(priority = 1)
	public void navigateToWorkflowLandingPage() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToWorkFlowLandingPage(), "Landing page is displayed");
	}

	/**
	 * Assert true if workflow saved successfully
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 2, enabled = true, dataProvider = "workFlowDataProvider", dataProviderClass = WorkflowDataProvider.class)
	public void saveWorkflow(WorkflowTakDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.saveWorkflow(data), "WorkflowTask saved successfully");
	}

	/**
	 * Mandatory field validation
	 */
	@Test(priority = 3, enabled = true, dataProvider = "workFlowDataProvider", dataProviderClass = WorkflowDataProvider.class)
	public void validateMandatoryFields(WorkflowTakDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.validateMandatoryFields(data), "WorkflowTask validated successfully");
	}

	/**
	 * Update workflow
	 */
	@Test(priority = 4, enabled = true, dataProvider = "updateWorkFlowDataProvider", dataProviderClass = WorkflowDataProvider.class)
	public void updateWorkflow(WorkflowTakDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.updateWorkflow(data), "WorkflowTask updated successfully");
	}

	/**
	 * Delete workflow
	 */
	@Test(priority = 5, enabled = true, dataProvider = "workFlowDataProvider", dataProviderClass = WorkflowDataProvider.class)
	public void deleteWorkflow(WorkflowTakDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.deleteWorkflow(data), "WorkflowTask deleted successfully");
	}

	/**
	 * This method will be executed just after any function/method with @Test
	 * annotation ends.
	 */
	@AfterMethod
	public void afterMethod() {
		log.info(line);
	}

}
