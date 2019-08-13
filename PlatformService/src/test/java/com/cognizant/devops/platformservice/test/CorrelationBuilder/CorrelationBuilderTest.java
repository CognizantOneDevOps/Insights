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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.correlationbuilder.service.CorrelationBuilderServiceImpl;

@Test
public class CorrelationBuilderTest extends CorrelationBuilderTestData {

	public static final CorrelationBuilderTestData correlationBuilderTestData = new CorrelationBuilderTestData();
	public static final CorrelationBuilderServiceImpl correlationBuilderImpl = new CorrelationBuilderServiceImpl();

	@Test(priority = 1)
	public void testGetCorrelationJson() throws InsightsCustomException {

		String expectedOutcome = correlationBuilderImpl.getCorrelationJson().toString();
		Assert.assertNotNull(expectedOutcome);
		Assert.assertFalse(expectedOutcome.toString().isEmpty());
		Assert.assertTrue(expectedOutcome.length() > 0);

	}

	@Test(priority = 2)
	public void testSaveConfig() throws InsightsCustomException {
		String config = "succcess";
		CorrelationBuilderTestData test = new CorrelationBuilderTestData();
		String response = correlationBuilderImpl.saveConfig(test.configDetails);
		Assert.assertEquals(config, response);
	}

}
