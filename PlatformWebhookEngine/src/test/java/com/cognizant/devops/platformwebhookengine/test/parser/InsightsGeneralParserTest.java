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
package com.cognizant.devops.platformwebhookengine.test.parser;

import java.util.List;

import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformwebhookengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.platformwebhookengine.parser.InsightsGeneralParser;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class InsightsGeneralParserTest extends InsightsParserTestData {
	public static final InsightsGeneralParser generalparser = new InsightsGeneralParser();
	public static final InsightsParserTestData testdata = new InsightsParserTestData();
	EngineAggregatorModule testapp = new EngineAggregatorModule();

	@Test(priority = 1)
	public void testGetToolDetailJson() throws InsightsCustomException {

		List<JsonObject> response = generalparser.parseToolData(testdata.responseTemplate, toolData, testdata.toolName,
				testdata.labelName, testdata.webhookName);
		
		String commitId = response.get(0).get("commitId").toString();
		String authorName = response.get(0).get("authorName").toString();
		String message = response.get(0).get("message").toString();
		Assert.assertEquals(commitId, testdata.commitId);
		Assert.assertEquals(authorName, testdata.authorName);
		Assert.assertEquals(message, testdata.message);

		
	}

	@Test(priority = 2, expectedExceptions = Exception.class)
	public void testGetToolDetailJsonWithExceptions() throws InsightsCustomException {
		List<JsonObject> response = generalparser.parseToolData(testdata.responseTemplate, incorrectToolData,
				testdata.toolName, testdata.labelName, "");
	}
	@Test(priority = 3)
	public void testNullJson() throws InsightsCustomException {
		List<JsonObject> nullresponse = generalparser.parseToolData(testdata.fieldNotFoundinToolData, testdata.toolData,
				testdata.toolName, testdata.labelName, testdata.webhookName);
		String responseTest = nullresponse.get(0).toString();
		responseTest = responseTest.substring(1, responseTest.length() - 1); // remove curly brackets
		Assert.assertEquals(responseTest, "");
		}

}