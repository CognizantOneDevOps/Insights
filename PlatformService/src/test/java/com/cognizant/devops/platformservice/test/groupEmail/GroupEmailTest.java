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
package com.cognizant.devops.platformservice.test.groupEmail;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.emailconfiguration.controller.InsightsEmailConfigurationController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@SuppressWarnings("unused")
@WebAppConfiguration
public class GroupEmailTest extends GroupEmailTestData {

	@Autowired
	InsightsEmailConfigurationController insightsEmailConfigurationController;

	// source=Report
	@Test(priority = 1)
	public void testSaveReportEmailConfig() {
		try {
			String expectedStatus = "success";
			JsonObject response = insightsEmailConfigurationController.saveEmailConfig(testSaveReportBatchString);
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			int emailConfigId = response.get("data").getAsInt();
			Assert.assertEquals(actualStatus, expectedStatus);

			setReportEmailConfigId(emailConfigId);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	// source = dashboard
	
	@Test(priority = 2)
	public void testSaveDashboardEmailConfig() throws InterruptedException {
		try {
			Thread.sleep(5000);
			String expectedStatus = "success";

			JsonObject response = insightsEmailConfigurationController.saveEmailConfig(testSaveDashboardBatchString);
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			int grafanaEmailConfigId = response.get("data").getAsInt();
			Assert.assertEquals(actualStatus, expectedStatus);

			setGrafanaEmailConfigId(grafanaEmailConfigId);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}


	
	@Test(priority = 3)
	public void testSaveDuplicateEmailConfig() {
		try {
			String expectedStatus = "failure";
			String expectedMessage = "GroupEmailConfiguration with the given Batch name already exists";

			JsonObject response = insightsEmailConfigurationController.saveEmailConfig(testSaveEmailWithDuplicateName);
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			String actualMessage = response.get("message").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertEquals(actualMessage, expectedMessage);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testgetAllReportEmailConfigs() {
		try {
			String expectedStatus = "success";

			JsonObject response = insightsEmailConfigurationController.getAllEmailConfigurations(sourceReport);
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			JsonArray batchArray = response.getAsJsonArray("data");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertNotNull(batchArray);
			Assert.assertTrue(batchArray.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	
	@Test(priority = 5)
	public void testgetAllGrafanaDashboardEmailConfigs() {
		try {
			String expectedStatus = "success";

			JsonObject response = insightsEmailConfigurationController
					.getAllEmailConfigurations(sourceGrafanaDashboard);
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			JsonArray batchArray = response.getAsJsonArray("data");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertNotNull(batchArray);
			Assert.assertTrue(batchArray.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 6)
	public void testgetEmailConfigsWithInvalidSource() {
		try {
			String expectedStatus = "failure";
			String expectedMessage = "Invalid Source";
			JsonObject response = insightsEmailConfigurationController.getAllEmailConfigurations(invalidSource);
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			String actualMessage = response.get("message").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertEquals(actualMessage, expectedMessage);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 7)
	public void testEditReportEmailConfig() {
		try {
			String expectedStatus = "success";

			JsonObject response = insightsEmailConfigurationController
					.updateEmailConfiguration(testEditReportBatchString);
			String actualStatus = response.get("status").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 8)
	public void testEditReportEmailConfigStatus() {
		try {
			String expectedStatus = "success";

			JsonObject response = insightsEmailConfigurationController
					.updateEmailConfigurationState(testEditBatchState);
			String actualStatus = response.get("status").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

    
    
	@Test(priority = 9)
	public void testEditGrafanaEmailConfig() {
		try {
			String expectedStatus = "success";

			JsonObject response = insightsEmailConfigurationController
					.updateEmailConfiguration(testEditGrafanaBatchString);
			String actualStatus = response.get("status").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 10)
	public void testEditEmailConfigStatus() {
		try {
			String expectedStatus = "success";

			JsonObject response = insightsEmailConfigurationController
					.updateEmailConfigurationState(testEditBatchState);
			String actualStatus = response.get("status").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 11)
	public void testEditInvalidEmailConfigStatus() {
		try {
			String expectedStatus = "failure";

			JsonObject response = insightsEmailConfigurationController
					.updateEmailConfigurationState(testEditInvalidBatchState);
			String actualStatus = response.get("status").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 12)
	public void testSaveEmailConfigWithIncorrectData() {
		try {
			String expectedStatus = "failure";

			JsonObject response = insightsEmailConfigurationController.saveEmailConfig(inValidEmailConfigString);
			String actualStatus = response.get("status").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 13)
	public void testDeleteEmailConfig() {
		try {
			String expectedStatus = "success";

			JsonObject response = insightsEmailConfigurationController.deleteEmailConfiguration(deleteEmailConfigId);
			String actualStatus = response.get("status").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 14)
	public void testDeleteEmailConfigWithInvalidId() {
		try {
			String expectedStatus = "failure";
			String expectedMessage = "Group Email Configuration not found.";

			JsonObject response = insightsEmailConfigurationController
					.deleteEmailConfiguration(deleteEmailConfigInvalidId);
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			String actualMessage = response.get("message").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertEquals(actualMessage, expectedMessage);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 15)
	public void testSaveEmailConfigWithEmptyEmaiilDetails() throws InsightsCustomException {
		try {
			String expectedStatus = "failure";
			JsonObject response = insightsEmailConfigurationController
					.saveEmailConfig(emailConfigWithEmptyEmailDetails);
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {

			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 16)
	public void testGetAllReportTitlesWithInvalidSource() throws InsightsCustomException {
		try {
			String userName = "{\"userName\":\"Test_User\"}";
			String expectedStatus = "failure";
			String expectedMessage = "Unable to retrieve report details.";
			JsonObject response = insightsEmailConfigurationController.getAllReportTitles(invalidSource, userName);

			String actualStatus = response.get("status").getAsString().replace("\"", "");
			String actualMessage = response.get("message").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertEquals(actualMessage, expectedMessage);
		} catch (AssertionError e) {

			Assert.fail(e.getMessage());
		}
	}

}
