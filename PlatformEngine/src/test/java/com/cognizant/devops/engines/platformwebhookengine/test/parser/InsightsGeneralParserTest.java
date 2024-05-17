/*******************************************************************************
 *  * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformwebhookengine.test.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.cognizant.devops.engines.platformwebhookengine.modules.aggregator.WebHookEngineAggregatorModule;
import com.cognizant.devops.engines.platformwebhookengine.parser.InsightsGeneralParser;
import com.cognizant.devops.engines.platformwebhookengine.test.engine.WebhookEngineTest;
import com.cognizant.devops.engines.platformwebhookengine.test.engine.WebhookEngineTestData;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.google.gson.JsonObject;

@Test
public class InsightsGeneralParserTest extends InsightsParserTestData {
	private static Logger log = LogManager.getLogger(InsightsGeneralParserTest.class.getName());
	public static final InsightsGeneralParser generalparser = new InsightsGeneralParser();
	public static final InsightsParserTestData parsertestdata = new InsightsParserTestData();
	WebHookEngineAggregatorModule testapp = new WebHookEngineAggregatorModule();
	WebhookEngineTestData webhookEngineTestData = new WebhookEngineTestData();
	JsonObject parserTestData = new JsonObject();
	JsonObject testData = new JsonObject();
	public Set<WebhookDerivedConfig> derivedOperationsArray = null;
	public Set<WebhookDerivedConfig> derivedOperationsArrayWithoutEpoch = null;
	public Set<WebhookDerivedConfig> derivedOperationsArrayWithoutEpochPivotal = null;
	
	@BeforeClass
	public void onInit() throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		// save data archival agent
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
				+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "InsightsGeneralParser.json";
		String path2 = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
				+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "WebhookEngine.json";
		parserTestData = JsonUtils.getJsonData(path).getAsJsonObject();
		testData = JsonUtils.getJsonData(path2).getAsJsonObject();
		derivedOperationsArray = WebhookEngineTestData.getderivedOperationsJSONArray(testData.get("derivedOpsJson").toString());
		derivedOperationsArrayWithoutEpoch = WebhookEngineTestData.getderivedOperationsJSONArray(testData.get("derivedOpsJsonWithoutEpoch").toString());
		derivedOperationsArrayWithoutEpochPivotal = WebhookEngineTestData.getderivedOperationsJSONArray(testData.get("derivedOpsJsonWithoutEpochPivotal").toString());
	}
	
	@Test(priority = 1)
	public void testGetToolDetailJson() throws InsightsCustomException {
		try {
			WebHookConfig webhookConfig = new WebHookConfig();
			WebhookEngineTest webhookEngineTest = new WebhookEngineTest();
			webhookConfig.setResponseTemplate(testData.get("responseTemplate").getAsString());
			webhookConfig.setDynamicTemplate(testData.get("dynamicTemplate").toString());
			webhookConfig.setLabelName(testData.get("labelName").getAsString());
			webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
			webhookConfig.setToolName(testData.get("toolName").getAsString());
			webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
			webhookConfig.setSubscribeStatus(true);
			webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
			webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
			webhookConfig.setWebhookDerivedConfig(derivedOperationsArray);
			List<JsonObject> response = generalparser.parseToolData(webhookConfig, testData.get("toolData").toString());

			String commitId = response.get(0).get("commitId").getAsString();
			String authorName = response.get(0).get("authorName").toString();
			String message = response.get(0).get("message").toString();
			Assert.assertEquals(commitId, testData.get("commitId").getAsString());
//			Assert.assertEquals(authorName, testData.get("authorName").getAsString());
//			Assert.assertEquals(message, testData.get("message").getAsString());
		}catch(Exception e) {
			log.error(e);
		}

	}

	@Test(priority = 2)
	public void testGetToolDetailJsonWithExceptions() throws InsightsCustomException {
		try {
			WebHookConfig webhookConfig = new WebHookConfig();
			WebhookEngineTest webhookEngineTest = new WebhookEngineTest();
			webhookConfig.setResponseTemplate(testData.get("responseTemplate").getAsString());
			webhookConfig.setDynamicTemplate(testData.get("dynamicTemplate").toString());
			webhookConfig.setLabelName(testData.get("labelName").getAsString());
			webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
			webhookConfig.setToolName(testData.get("toolName").getAsString());
			webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
			webhookConfig.setSubscribeStatus(true);
			webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
			webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
			webhookConfig.setWebhookDerivedConfig(derivedOperationsArray);
			generalparser.parseToolData(webhookConfig, incorrectToolData);
		}
		catch(Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 3)
	public void testGetToolDetailJsonWithEmptyResponseTemplate() throws InsightsCustomException {
		try {
			WebHookConfig webhookConfig = new WebHookConfig();
			WebhookEngineTest webhookEngineTest = new WebhookEngineTest();
			webhookConfig.setResponseTemplate(webhookEngineTestData.emptyResponseTemplate);
			webhookConfig.setDynamicTemplate(testData.get("dynamicTemplate").toString());
			webhookConfig.setLabelName(testData.get("labelName").getAsString());
			webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
			webhookConfig.setToolName(testData.get("toolName").getAsString());
			webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
			webhookConfig.setSubscribeStatus(true);
			webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
			webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
			webhookConfig.setWebhookDerivedConfig(derivedOperationsArray);
			List<JsonObject> response = generalparser.parseToolData(webhookConfig, testData.get("toolData").toString());
			String webhookName = response.get(0).get("webhookName").getAsString();
			Assert.assertEquals(webhookName, webhookEngineTestData.webhookName);
		}
		catch(Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 4)
	public void testUnmatchedResponseTemplate() throws InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		WebhookEngineTest webhookEngineTest = new WebhookEngineTest();
		webhookConfig.setResponseTemplate(parserTestData.get("fieldNotFoundinToolData").getAsString());
		webhookConfig.setDynamicTemplate(parsertestdata.emptyDynamicTemplate);
		webhookConfig.setLabelName(testData.get("labelName").getAsString());
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(testData.get("toolName").getAsString());
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(derivedOperationsArray);
		List<JsonObject> nullresponse = generalparser.parseToolData(webhookConfig, testData.get("toolData").toString());
		String responseTest = nullresponse.toString();
		responseTest = responseTest.substring(1, responseTest.length() - 1); // remove curly brackets
		Assert.assertEquals(responseTest, "");
	}

	@Test(priority = 5)
	public void testEmptyDynamicTemplate() throws InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		WebhookEngineTest webhookEngineTest = new WebhookEngineTest();
		webhookConfig.setResponseTemplate(testData.get("responseTemplate").getAsString());
		webhookConfig.setDynamicTemplate(parsertestdata.emptyDynamicTemplate);
		webhookConfig.setLabelName(testData.get("labelName").getAsString());
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(testData.get("toolName").getAsString());
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(derivedOperationsArray);
		List<JsonObject> nullresponse = generalparser.parseToolData(webhookConfig, testData.get("toolData").toString());
		String responseTest = nullresponse.toString();
		responseTest = responseTest.substring(1, responseTest.length() - 1); // remove curly brackets
		Assert.assertFalse(responseTest.isEmpty());
	}

	@Test(priority = 6)
	public void testDynamicTemplateWithArray() throws InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		WebhookEngineTest webhookEngineTest = new WebhookEngineTest();
		webhookConfig.setResponseTemplate(parserTestData.get("responseTemplateForArray").getAsString());
		webhookConfig.setDynamicTemplate(parserTestData.get("dynamicTemplateWithArray").toString());
		webhookConfig.setLabelName(parserTestData.get("labelName").getAsString());
		webhookConfig.setMQChannel(parserTestData.get("mqChannel").getAsString());
		webhookConfig.setToolName(parsertestdata.toolName);
		webhookConfig.setWebHookName(parserTestData.get("webhookName").getAsString());
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(true);
		webhookConfig.setFieldUsedForUpdate(parsertestdata.fieldUsedForUpdate);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(derivedOperationsArrayWithoutEpochPivotal);
		List<JsonObject> nullresponse = generalparser.parseToolData(webhookConfig,
				parserTestData.get("toolDataWithArray").toString());
		String responseTest = nullresponse.toString();
		responseTest = responseTest.substring(1, responseTest.length() - 1); // remove curly brackets
		Assert.assertFalse(responseTest.isEmpty());
	}
	
	@Test(priority = 7)
	public void testDynamicTemplateWithArrayEpochFalse() throws InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		WebhookEngineTest webhookEngineTest = new WebhookEngineTest();
		webhookConfig.setResponseTemplate(parserTestData.get("responseTemplateForArray").getAsString());
		webhookConfig.setDynamicTemplate(parserTestData.get("dynamicTemplateWithArray").toString());
		webhookConfig.setLabelName(parserTestData.get("labelName").getAsString());
		webhookConfig.setMQChannel(parserTestData.get("mqChannel").getAsString());
		webhookConfig.setToolName(parsertestdata.toolName);
		webhookConfig.setWebHookName(parserTestData.get("webhookName").getAsString());
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(true);
		webhookConfig.setFieldUsedForUpdate(parsertestdata.fieldUsedForUpdate);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(derivedOperationsArrayWithoutEpoch);
		List<JsonObject> nullresponse = generalparser.parseToolData(webhookConfig,
				parserTestData.get("toolDataWithArray").toString());
		String responseTest = nullresponse.toString();
		responseTest = responseTest.substring(1, responseTest.length() - 1); // remove curly brackets
		Assert.assertFalse(responseTest.isEmpty());
	}


}