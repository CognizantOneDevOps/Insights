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
package com.cognizant.devops.platformservice.rest.about;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;


import com.cognizant.devops.platformservice.properties.config.GetPropertyValues;
import com.google.gson.JsonObject;

import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class AboutServiceTest {

	@Test(priority=1)
	public void testLoadProperties() {
		JsonObject jsonObj = new JsonObject();
		Properties properties = null;
		try {
			properties = new GetPropertyValues().getPropValues();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Enumeration e = properties.propertyNames();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			jsonObj.addProperty(key, properties.getProperty(key));
		}
		System.out.println(" About test case with Test ng");
	}

	

}
