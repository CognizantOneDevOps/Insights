/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.agentManagement;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.agentmanagement.controller.InsightsAgentConfiguration;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minidev.json.JSONObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@WebAppConfiguration
public class AgentManagementTest extends AgentManagementTestData {

	public static final AgentManagementTestData agentManagementTestData = new AgentManagementTestData();
	public static final AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
	private static Logger log = LogManager.getLogger(AgentManagementTestData.class);
	Gson gson = new Gson();
	@Autowired
	InsightsAgentConfiguration insightsAgentConfiguration;
	

	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
			prepareOfflineAgent(version);
		} catch (Exception e) {
			log.error("message", e);
		}
	}

	@Test(priority = 2)
	public void testGetSystemAvailableAgentListForOfflineRegistration_C() throws InsightsCustomException {

		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		JsonObject response = insightsAgentConfiguration.getSystemAvailableAgentList();

		JsonObject dataJson = response.getAsJsonObject("data");
		JsonArray toolName = response.getAsJsonObject("data").get(version).getAsJsonArray();
		Assert.assertTrue(dataJson.has(version));
		Assert.assertTrue(toolName.size() > 0);

	}

	@Test(priority = 4)
	public void testGetToolRawConfigFileForOfflineAgentRegistration() throws InsightsCustomException {
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);

		JsonObject configJson = insightsAgentConfiguration.getToolRawConfigFile(version, gitTool, false, typeAgent);

		String trimJson = configJson.get("data").getAsString();
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(trimJson.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		Assert.assertNotNull(json);
		Assert.assertEquals(json.get("toolCategory").getAsString(), "SCM");
	}

	@Test(priority = 5)
	public void testRegisterAgent() throws InsightsCustomException {
		String expectedOutcome = "success";

		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetails);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 6)
	public void testRegisterAgentWithDuplicateId() throws InsightsCustomException {
		String expectedOutcome = "Agent Id already exsits.";

		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetails);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);

		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 7)
	public void testAgentIDandToolNamenotequal() throws InsightsCustomException {
		String expectedOutcome = "Agent Id and Tool name cannot be the same.";

		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsWithSameIDs);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);

		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 8)
	public void testRegisterAgentWithInvalidDataLabelName() throws InsightsCustomException {
		String expectedOutcome = "Invalid data label Name, it should contain only alphanumeric character,underscore & dot";

		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsWithInvalidDataLabel);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);

		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 9)
	public void testRegisterAgentWithInvalidHealthLabelName() throws InsightsCustomException {
		String expectedOutcome = "Invalid health label Name, it should contain only alphanumeric character,underscore & dot";

		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsWithInvalidHealthLabel);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);

		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 10)
	public void testRegisterAgentInDatabase() throws InsightsCustomException {

		JsonObject registeredAgents = insightsAgentConfiguration.getRegisteredAgentsNew();
		String expectedOutcome = "success";
		String actual = registeredAgents.get("status").toString().replaceAll("\"", "");

		if (actual.equalsIgnoreCase(expectedOutcome)) {
			List<JsonObject> versionList = gson.fromJson(registeredAgents.get("data"),
					new TypeToken<List<JsonObject>>() {
					}.getType());
			for (int i = 0; i < versionList.size(); i++) {
				JsonObject jsonObj = versionList.get(i);
				String toolName = jsonObj.get("toolName").getAsString();
				Assert.assertTrue((toolName.equalsIgnoreCase("git")) || (toolName.equalsIgnoreCase("github2"))
						|| (toolName.equalsIgnoreCase("newrelic")) || (toolName.equalsIgnoreCase("jira")));
				break;
			}
		}
	}

	@Test(priority = 11)
	public void testGetRegisteredAgents() throws InsightsCustomException {

		JsonObject response = insightsAgentConfiguration.getRegisteredAgents();
		List<JsonObject> versionList = gson.fromJson(response.get("data"), new TypeToken<List<JsonObject>>() {
		}.getType());
		for (int i = 0; i < versionList.size(); i++) {
			JsonObject jsonObj = versionList.get(i);
			String toolName = jsonObj.get("toolName").getAsString();
			Assert.assertTrue((toolName.equalsIgnoreCase("git")) || (toolName.equalsIgnoreCase("github2"))
					|| (toolName.equalsIgnoreCase("newrelic")) || (toolName.equalsIgnoreCase("jira")));
		}
	}

	@Test(priority = 12)
	public void testStartStopAgentForStartAction() throws InsightsCustomException {

		String action = "START";
		String expectedOutput = "success";

		JsonObject response = insightsAgentConfiguration.startStopAgent(agentId, gitTool, osversion, action);
		Assert.assertNotNull(response);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutput);
	}

	@Test(priority = 13)
	public void testStartStopAgentForStopAction() throws InsightsCustomException {

		String action = "STOP";
		String expectedOutput = "success";

		JsonObject response = insightsAgentConfiguration.startStopAgent(agentId, gitTool, osversion, action);
		Assert.assertNotNull(response);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutput);
	}

	@Test(priority = 14)
	public void testStartStopAgentForNoAction() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "success";

		JsonObject response = insightsAgentConfiguration.startStopAgent(agentId, gitTool, osversion, action);
		Assert.assertNotNull(response);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutput);
	}

	@Test(priority = 15)
	public void testStartStopAgentForLinux() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "LINUX";
		String expectedOutput = "success";

		JsonObject response = insightsAgentConfiguration.startStopAgent(agentId, gitTool, osversion, action);
		Assert.assertNotNull(response);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutput);
	}

	@Test(priority = 16)
	public void testStartStopAgentForException() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "success";

		JsonObject response = insightsAgentConfiguration.startStopAgent(agentId, gitTool, osversion, action);
		Assert.assertNotNull(response);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutput);
	}

	@Test(priority = 17)
	public void getAgentDetails() throws InsightsCustomException {

		JsonObject agentDetailsJson = insightsAgentConfiguration.getAgentDetails(agentId);// agentKey
		Assert.assertNotNull(agentDetailsJson.getAsJsonObject("data").get("agentKey").getAsString());
		Assert.assertNotNull(agentDetailsJson.get("data"));
		Assert.assertEquals(agentDetailsJson.getAsJsonObject("data").get("toolCategory").getAsString(), "SCM");
		Assert.assertEquals(agentDetailsJson.getAsJsonObject("data").get("agentKey").getAsString(), agentId);

	}

	@Test(priority = 18)
	public void getAgentDetailsForException() throws InsightsCustomException {

		String expectedOutput = "failure";

		JsonObject agentDetailsJson = insightsAgentConfiguration.getAgentDetails(agentIdNotExist);
		String actual = agentDetailsJson.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutput);
	}

	@Test(priority = 20)
	public void testUpdateAgent() throws InsightsCustomException {

		String expectedOutcome = "success";
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);

		JSONObject obj = new JSONObject();

		obj.put("agentId", agentId);
		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsToUpdateAgent);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.updateAgent(jsonobj);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@Test(priority = 21)
	public void testUpdateAgentVersion() throws InsightsCustomException {
		String expectedOutcome = "success";

		JSONObject obj = new JSONObject();

		obj.put("agentId", agentId);
		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsToUpdateAgent);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.updateAgent(jsonobj);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 22)
	public void testUninstallAgent() throws InsightsCustomException {

		String expectedOutcome = "success";

		JsonObject response = insightsAgentConfiguration.uninstallAgent(agentId, gitTool, osversion);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 23)
	public void testUninstallAgentForException() throws InsightsCustomException {

		String expectedOutcome = "failure";

		JsonObject response = insightsAgentConfiguration.uninstallAgent("12345fghj", gitTool, osversion);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	// webhook testcases

	@Test(priority = 24)
	public void testGetToolRawConfigFileForOfflineWebhhokRegistration() throws InsightsCustomException {

		JsonObject configJson = insightsAgentConfiguration.getToolRawConfigFile(version, gitTool, true, "Webhook");

		String trimJson = configJson.get("data").getAsString();
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(trimJson.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		Assert.assertNotNull(json);
		Assert.assertEquals(json.get("toolCategory").getAsString(), "SCM");
	}

	@Test(priority = 25)
	public void testRegisterWebhook() throws InsightsCustomException, IOException {

		String expectedOutcome = "success";

		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", webhookConfigDetails);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", true);
		obj.put("type", "Webhook");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 26)
	public void testRegisterWebhookWithDuplicateId() throws InsightsCustomException {

		String expectedOutcome = "Agent Id already exsits.";

		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", webhookConfigDetails);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", true);
		obj.put("type", "Webhook");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 27)
	public void testRegisterWebhookWithInvalidDataLabelName() throws InsightsCustomException {

		String expectedOutcome = "Invalid data label Name, it should contain only alphanumeric character,underscore & dot";
		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", webhookConfigDetailsWithInvalidDataLabel);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", true);
		obj.put("type", "Webhook");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 28)
	public void testRegisterWebhookWithInvalidHealthLabelName() throws InsightsCustomException {

		String expectedOutcome = "Invalid health label Name, it should contain only alphanumeric character,underscore & dot";

		JSONObject obj = new JSONObject();

		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", webhookConfigDetailsWithInvalidHealthLabel);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", true);
		obj.put("type", "Webhook");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@Test(priority = 29)
	public void testRegisterIncorrectWebhookAgent() throws InsightsCustomException {

		String expectedOutcome = "failure";
		String toolname = "neo4jarchival";
		JsonObject configJson = insightsAgentConfiguration.getToolRawConfigFile(version, toolname, true, "Webhook");
		String actual = configJson.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@Test(priority = 30)
	public void testRegisterWithInvalidAgentId() throws InsightsCustomException {
		String expectedOutcome = "Agent Id has to be Alpha numeric with '_' as special character";

		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);

		JSONObject obj = new JSONObject();

		obj.put("toolName", gitTool);
		obj.put("agentVersion", version);
		obj.put("osversion", osversion);
		obj.put("configJson", ConfigDetailsWithInvalidAgentId);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 31)
	public void testUpdateOfflineWebhook() throws InsightsCustomException {

		String expectedOutcome = "success";
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);

		JSONObject obj = new JSONObject();

		obj.put("agentId", webhookAgentId);
		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsToUpdateWebhook);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", true);
		obj.put("type", "Webhook");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.updateAgent(jsonobj);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(expectedOutcome, actual);
	}

	@Test(priority = 32)
	public void testUninstallWebhookAgent() throws InsightsCustomException {
		String expectedOutcome = "success";
		JsonObject response = insightsAgentConfiguration.uninstallAgent(webhookAgentId, gitTool, osversion);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	// ROI TestCases
	@Test(priority = 33)
	public void testGetToolRawConfigFileForOfflineROIRegistration() throws InsightsCustomException {
		try {
			log.debug("dbconnection  {}", ApplicationConfigProvider.getInstance().getPostgre().getInsightsDBUrl());
			prepareROIAgentToolData();
			Thread.sleep(5000);

			JsonObject configJson = insightsAgentConfiguration.getToolRawConfigFile(version, ROITool, false, typeROI);
			log.debug(configJson);
			String trimJson = configJson.get("data").getAsString();
			Gson gson = new Gson();
			JsonElement jsonElement = gson.fromJson(trimJson.trim(), JsonElement.class);
			JsonObject json = jsonElement.getAsJsonObject();
			Assert.assertNotNull(json);
			Assert.assertEquals(json.get("toolCategory").getAsString(), "ROI");
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
	}

	@Test(priority = 34)
	public void testRegisterROIAgent() throws InsightsCustomException {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		String expectedOutcome = "success";

		JSONObject obj = new JSONObject();

		obj.put("toolName", ROITool);
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsForROIAgent);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "ROIAgent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@Test(priority = 35)
	public void testRegisterROIAgentWithDuplicateID() throws InsightsCustomException {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		String expectedOutcome = "Agent Id already exsits.";

		JSONObject obj = new JSONObject();

		obj.put("toolName", ROITool);
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsForROIAgent);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "ROIAgent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 36)
	public void testRegisterROIAgentWithInvalidDataLabel() throws InsightsCustomException {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		String expectedOutcome = "Invalid data label Name, it should contain only alphanumeric character,underscore & dot";

		JSONObject obj = new JSONObject();

		obj.put("toolName", ROITool);
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsForROIAgentWithInvalidDataLabel);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "ROIAgent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);

	}

	@Test(priority = 37)
	public void testRegisterROIAgentWithInvalidHealthLabel() throws InsightsCustomException {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		String expectedOutcome = "Invalid health label Name, it should contain only alphanumeric character,underscore & dot";

		JSONObject obj = new JSONObject();

		obj.put("toolName", ROITool);
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsForROIAgentWithInvalidHealthLabel);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "ROIAgent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);
		String actual = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@Test(priority = 38)
	public void testUninstallROIAgent() throws InsightsCustomException {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		String expectedOutcome = "success";

		JsonObject response = insightsAgentConfiguration.uninstallAgent(ROIAgentId, ROITool, osversion);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(expectedOutcome, actual);
	}

	@Test(priority = 39)
	public void testUpdateAgentWithEmptyAgentId() throws InsightsCustomException {
		String expectedOutcome = "failure";
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);

		JSONObject obj = new JSONObject();

		obj.put("agentId", "");
		obj.put("toolName", "git");
		obj.put("agentVersion", "v9.1");
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsToUpdateAgent);
		obj.put("trackingDetails", trackingDetails);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.updateAgent(jsonobj);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@Test(priority = 40)
	public void testGetAvailableAgentTags() throws InsightsCustomException {
		String expectedOutcome = "success";
		JsonObject response = insightsAgentConfiguration.getAvailableAgentTags();

		String dataJson = response.getAsJsonObject("data").toString();
		String actual = response.get("status").toString().replaceAll("\"", "");

		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertTrue(dataJson.contains(version));
		Assert.assertTrue(dataJson.contains("9.2"));
	}

	@Test(priority = 41)
	public void testDownloadAgentPackage() throws InsightsCustomException {
		String folderPath;
		String agentVersion = "v9.6";
		try {
			folderPath = new File(ApplicationConfigProvider.getInstance().getAgentDetails().getOfflineAgentPath()
					+ File.separator + agentVersion).getCanonicalPath();
			File offlineAgentFolder = new File(folderPath);
			if (offlineAgentFolder.exists()) {
				FileUtils.deleteDirectory(offlineAgentFolder);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		String expectedOutcome = "success";
		String expectedOutcomeMessage = "Downloaded agents package - " + agentVersion;

		JsonObject response = insightsAgentConfiguration.downloadAgentPackage(agentVersion);

		String actual = response.get("status").toString().replaceAll("\"", "");
		String actualMessage = response.get("data").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertEquals(actualMessage, expectedOutcomeMessage);
	}

	@Test(priority = 42)
	public void testDownloadAgentPackageException() throws InsightsCustomException {
		String agentVersion = "v9.6";
		String expectedOutcome = "failure";
		String expectedOutcomeMessage = agentVersion + " - Package already exist!";
		JsonObject response = insightsAgentConfiguration.downloadAgentPackage(agentVersion);

		String actual = response.get("status").toString().replaceAll("\"", "");
		String actualMessage = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertEquals(actualMessage, expectedOutcomeMessage);
	}

	@Test(priority = 43)
	public void testGetToolRawConfigFileForOfflineAgentRegistrationWithTrackingDetails()
			throws InsightsCustomException {
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);

		JsonObject configJson = insightsAgentConfiguration.getToolRawConfigFile(version, gitTool, false, typeAgent);

		String trimJson = configJson.get("data").getAsString();
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(trimJson.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		Assert.assertNotNull(json);
		Assert.assertEquals(json.get("toolCategory").getAsString(), "SCM");
	}

	@Test(priority = 44)
	public void testRegisterAgentWithTrackingDetails() throws InsightsCustomException {
		String expectedOutcome = "success";
		String oldVersion = "v9.1";

		JSONObject obj = new JSONObject();

		obj.put("toolName", gitTool);
		obj.put("agentVersion", oldVersion);
		obj.put("osversion", osversion);
		obj.put("configJson", configDetailsForTracking);
		obj.put("trackingDetails", tracking);
		obj.put("vault", false);
		obj.put("isWebhook", false);
		obj.put("type", "Agent");

		String jsonobj = obj.toString();
		JsonObject response = insightsAgentConfiguration.registerAgentV2(jsonobj);

		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@Test(priority = 45)
	public void testUninstallAgentWithTrackingDetails() throws InsightsCustomException {
		String expectedOutcome = "success";

		JsonObject response = insightsAgentConfiguration.uninstallAgent(agentIdTrackingDetails, gitTool, osversion);
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@AfterClass
	public void cleanUp() throws InsightsCustomException, IOException {

	}

}
