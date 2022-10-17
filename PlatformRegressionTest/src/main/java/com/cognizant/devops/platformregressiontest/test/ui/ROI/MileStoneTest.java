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
package com.cognizant.devops.platformregressiontest.test.ui.ROI;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class MileStoneTest extends LoginAndSelectModule{

	private static final Logger log = LogManager.getLogger(MileStoneTest.class);
	MileStoneConfiguration milestone;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 */
	
	@BeforeTest
	public void setUp() throws InterruptedException {
		initialization();
		getData(ConfigOptionsTest.ROI_DIR + File.separator + ConfigOptionsTest.MILESTONE_TEST_DATA);
		Thread.sleep(2000);
		selectMenuOption("MileStone Config");
		milestone = new MileStoneConfiguration();
	}
	
	/**
	 * Assert true if landing page is displayed else false
	 * 
	 */
	
	@Test(priority = 1)
	public void navigateToMilestoneConfig() {
		log.info(line);
		Assert.assertTrue(milestone.navigateToMilestoneConfig(), "Landing page is displayed");
	}
	
	@Test(priority = 2)
	public void saveMileStone() throws InterruptedException {

		Assert.assertTrue(milestone.saveMileStone(),"milestone data saved successfully.");

	}
	
	@Test(priority = 3)
	public void editMileStone() throws InterruptedException {

		Assert.assertTrue(milestone.editMileStone(),"Milestone updated successfully");

	}
	
	@Test(priority = 4)
	public void validateMilestoneDetail() throws InterruptedException {

		Assert.assertTrue(milestone.validateMilestoneDetail());

	}
	
	@Test(priority = 5)
	public void saveWithInvalideData() throws InterruptedException {

		Assert.assertTrue(milestone.saveWithInvalideData());

	}
	
	@Test(priority = 6)
	public void deleteMileStone() throws InterruptedException {

		Assert.assertTrue(milestone.deleteMileStone(),"Milestone deleted successfully");

	}
	
}
