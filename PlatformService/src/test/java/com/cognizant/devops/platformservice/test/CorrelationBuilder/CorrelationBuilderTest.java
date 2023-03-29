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
package com.cognizant.devops.platformservice.test.CorrelationBuilder;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;
import com.cognizant.devops.platformservice.correlationbuilder.controller.InsightsCorrelationBuilder;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@WebAppConfiguration
public class CorrelationBuilderTest extends CorrelationBuilderTestData {
	JsonObject testData = new JsonObject();
	private static Logger log = LogManager.getLogger(CorrelationBuilderTestData.class);

	
	@Autowired
	InsightsCorrelationBuilder CorrelationBuilderController;
	
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
			String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
					+ TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "CorrelationBuilder.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
		} catch (Exception e) {
			log.error("message", e);
		}
	}
	
	@Test(priority = 1)
	public void testSaveConfig() throws InsightsCustomException {
		try {
			JsonObject response = CorrelationBuilderController.saveConfig(testData.get("saveDataConfig").toString());
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	   }
	}

	@Test(priority = 2)
	public void testGetAllCorrelations() throws InsightsCustomException {
		try {
			JsonObject response = CorrelationBuilderController.getCorrelationJson();
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(response.get("data").getAsJsonArray().isJsonNull(), false);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	   }
	}

	@Test(priority = 3)
	public void testUpdateCorrelationStatus() throws InsightsCustomException {
		try {
			JsonObject response = CorrelationBuilderController.updateCorrelation(testData.get("UpdateConfigDetails").toString());
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	      }
	}
	
	@Test(priority = 4)
	public void testDeleteCorrelation() throws InsightsCustomException {
		try {
			JsonObject response = CorrelationBuilderController.deleteCorrelation(testData.get("DeleteConfigDetails").toString());
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	     }
	}
	
	@Test(priority = 5)
	public void testSaveConfigNoSourceDestinationLabel() throws InsightsCustomException {
		try {
			JsonObject response = CorrelationBuilderController.saveConfig(testData.get("saveDataConfigSourceDestinationLabelNull").toString());
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	   }
	}
	
	@Test(priority = 6)
	public void testSaveConfigValidationError() throws InsightsCustomException {
		try {
			JsonObject response = CorrelationBuilderController.saveConfig("&amp;"+testData.get("saveDataConfigValidationError").toString());
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	   }
	}
	
	@Test(priority = 7)
	public void testUpdateCorrelationStatusValidationError() throws InsightsCustomException {
		try {
			CorrelationBuilderController.updateCorrelation("&amp;"+testData.get("UpdateConfigDetailsValidationError").toString());
		}catch (Exception e) {
			Assert.assertEquals(e.toString().contains("Invalid request"), true);
		}
	}
	
	@Test(priority = 8)
	public void testDeleteCorrelationValidationError() throws InsightsCustomException {
		try {
			CorrelationBuilderController.deleteCorrelation("&amp;"+testData.get("DeleteConfigDetailsValidationError").toString());
		}catch (Exception e) {
			Assert.assertEquals(e.toString().contains("Invalid request"), true);
		}
	}
	
	@AfterClass
	public void cleanUP() throws IOException {
		String resetConfigData = "[{\"destination\":{\"toolName\":\"JENKINS\",\"toolCategory\":\"CI\",\"fields\":[\"scmcommitId\"]},\"source\":{\"toolName\":\"GIT\",\"toolCategory\":\"SCM\",\"fields\":[\"commitId\"]},\"relationName\":\"TEST_FROM_GIT_TO_JENKINS\"}]";
		CorrelationBuilderTestData.resetConfig(resetConfigData);

	}
}