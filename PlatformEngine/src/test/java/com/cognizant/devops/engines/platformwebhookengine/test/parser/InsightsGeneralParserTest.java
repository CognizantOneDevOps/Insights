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
package com.cognizant.devops.engines.platformwebhookengine.test.parser;

import java.util.List;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.engines.platformwebhookengine.modules.aggregator.WebHookEngineAggregatorModule;
import com.cognizant.devops.engines.platformwebhookengine.parser.InsightsGeneralParser;
import com.cognizant.devops.engines.platformwebhookengine.test.engine.WebhookEngineTestData;
import com.google.gson.JsonObject;

@Test

public class InsightsGeneralParserTest extends InsightsParserTestData {
	public static final InsightsGeneralParser generalparser = new InsightsGeneralParser();
	public static final InsightsParserTestData testdata = new InsightsParserTestData();
	WebHookEngineAggregatorModule testapp = new WebHookEngineAggregatorModule();
	WebhookEngineTestData webhookEngineTestData = new WebhookEngineTestData();

	@Test(priority = 1)
	public void testGetToolDetailJson() throws InsightsCustomException {

		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(webhookEngineTestData.responseTemplate);
		webhookConfig.setDynamicTemplate(webhookEngineTestData.dynamicTemplate);
		webhookConfig.setLabelName(webhookEngineTestData.labelName);
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(webhookEngineTestData.toolName);
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArray);
		List<JsonObject> response = generalparser.parseToolData(webhookConfig, webhookEngineTestData.toolData);

		String commitId = response.get(0).get("commitId").getAsString();
		String authorName = response.get(0).get("authorName").toString();
		String message = response.get(0).get("message").toString();
		Assert.assertEquals(commitId, webhookEngineTestData.commitId);
		Assert.assertEquals(authorName, webhookEngineTestData.authorName);
		Assert.assertEquals(message, webhookEngineTestData.message);

	}

	@Test(priority = 2, expectedExceptions = Exception.class)
	public void testGetToolDetailJsonWithExceptions() throws InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(webhookEngineTestData.responseTemplate);
		webhookConfig.setDynamicTemplate(webhookEngineTestData.dynamicTemplate);
		webhookConfig.setLabelName(webhookEngineTestData.labelName);
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(webhookEngineTestData.toolName);
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArray);
		List<JsonObject> response = generalparser.parseToolData(webhookConfig, incorrectToolData);
		Assert.assertFalse(response.isEmpty());
	}

	@Test(priority = 3)
	public void testUnmatchedResponseTemplate() throws InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(testdata.fieldNotFoundinToolData);
		webhookConfig.setDynamicTemplate(testdata.emptyDynamicTemplate);
		webhookConfig.setLabelName(webhookEngineTestData.labelName);
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(webhookEngineTestData.toolName);
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArray);
		List<JsonObject> nullresponse = generalparser.parseToolData(webhookConfig, webhookEngineTestData.toolData);
		String responseTest = nullresponse.toString();
		responseTest = responseTest.substring(1, responseTest.length() - 1); // remove curly brackets
		Assert.assertEquals(responseTest, "");
	}

	@Test(priority = 4)
	public void testEmptyDynamicTemplate() throws InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(webhookEngineTestData.responseTemplate);
		webhookConfig.setDynamicTemplate(testdata.emptyDynamicTemplate);
		webhookConfig.setLabelName(webhookEngineTestData.labelName);
		webhookConfig.setMQChannel(webhookEngineTestData.mqChannel);
		webhookConfig.setToolName(webhookEngineTestData.toolName);
		webhookConfig.setWebHookName(webhookEngineTestData.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(webhookEngineTestData.isUpdateRequired);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArray);
		List<JsonObject> nullresponse = generalparser.parseToolData(webhookConfig, webhookEngineTestData.toolData);
		String responseTest = nullresponse.toString();
		responseTest = responseTest.substring(1, responseTest.length() - 1); // remove curly brackets
		Assert.assertFalse(responseTest.isEmpty());
	}

	@Test(priority = 5)
	public void testDynamicTemplateWithArray() throws InsightsCustomException {
		WebHookConfig webhookConfig = new WebHookConfig();
		webhookConfig.setResponseTemplate(testdata.responseTemplateForArray);
		webhookConfig.setDynamicTemplate(testdata.dynamicTemplateWithArray);
		webhookConfig.setLabelName(testdata.labelName);
		webhookConfig.setMQChannel(testdata.mqChannel);
		webhookConfig.setToolName(testdata.toolName);
		webhookConfig.setWebHookName(testdata.webhookName);
		webhookConfig.setSubscribeStatus(true);
		webhookConfig.setIsUpdateRequired(true);
		webhookConfig.setFieldUsedForUpdate(testdata.fieldUsedForUpdate);
		webhookConfig.setDataFormat(webhookEngineTestData.dataFormat);
		webhookConfig.setWebhookDerivedConfig(webhookEngineTestData.derivedOperationsArrayWithoutEpochPivotal);
		List<JsonObject> nullresponse = generalparser.parseToolData(webhookConfig,
				testdata.toolDataWithArray);
		String responseTest = nullresponse.toString();
		responseTest = responseTest.substring(1, responseTest.length() - 1); // remove curly brackets
		Assert.assertFalse(responseTest.isEmpty());
	}


}