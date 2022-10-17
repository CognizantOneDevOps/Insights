/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.scheduletask;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class ScheduleTaskTest extends LoginAndSelectModule{

	private static final Logger log = LogManager.getLogger(ScheduleTaskTest.class);
	ScheduleTaskConfiguration scheduleTask;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 */
	
	@BeforeTest
	public void setUp() throws InterruptedException {
		initialization();
		getData(ConfigOptionsTest.SCHEDULETASK_DIR + File.separator + ConfigOptionsTest.SCHEDULETASK_TEST_DATA);
		selectMenuOption("Schedule Task Management");
		scheduleTask = new ScheduleTaskConfiguration();
	}

	@Test(priority = 1)
	public void navigateToScheduleTaskManagement() {
		log.info(line);
		Assert.assertTrue(scheduleTask.navigateToScheduleTaskManagement(), "Landing page is displayed");
	}
	
	@Test(priority = 2)
	public void addScheduleTask() throws InterruptedException {

		Assert.assertTrue(scheduleTask.addScheduleTask(),"schedule task saved successfully.");

	}
	
	@Test(priority = 3)
	public void editMileStone() throws InterruptedException {

		Assert.assertTrue(scheduleTask.editScheduleTask(),"Task updated successfully");

	}
	
	@Test(priority = 4)
	public void validateStop() throws InterruptedException {

		Assert.assertTrue(scheduleTask.validateStop());

	}
	
	@Test(priority = 5)
	public void validateStart() throws InterruptedException {

		Assert.assertTrue(scheduleTask.validateStart());

	}
	
	@Test(priority = 6)
	public void validateExecutionHistory() throws InterruptedException {

		Assert.assertTrue(scheduleTask.validateExecutionHistory());

	}
	
	@Test(priority = 7)
	public void addInvalideScheduleTask() throws InterruptedException {

		Assert.assertTrue(scheduleTask.addInvalideScheduleTask());

	}
	
	@Test(priority = 8)
	public void deleteTask() throws InterruptedException {

		Assert.assertTrue(scheduleTask.deleteTask(),"Task deleted successfully");

	}
}
