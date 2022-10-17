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
package com.cognizant.devops.platformservice.test.grafanaUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.UnitTestConstant;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.grafana.GrafanaUserDetailsUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GrafanaUserDetailsTest {
	private static final String AUTHORIZATION = "authorization";

	GrafanaUserDetailsTestData userDetailsTestData = new GrafanaUserDetailsTestData();

	Map<String, String> testAuthData = new HashMap<>();

	@DataProvider
	public void getData() throws FileNotFoundException {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ UnitTestConstant.TESTNG_TESTDATA + File.separator + "grafanaAuth.json";
		JsonElement jsonData;
		try {
			jsonData = JsonUtils.parseReader(new FileReader(new File(path).getCanonicalPath()));
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			throw new SkipException("skipped this test case as grafana auth file not found.");
		}
		testAuthData = new Gson().fromJson(jsonData, Map.class);
	}

	@BeforeClass
	public void onInit() throws InterruptedException, IOException, InsightsCustomException {
		getData();

	}

	@Test(priority = 1)
	public void testGetUserDetails() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setCookies(userDetailsTestData.cookies);
		request.addHeader("Accept", userDetailsTestData.accept);
		request.addHeader("Authorization", testAuthData.get(AUTHORIZATION));
		request.addHeader("Content-Type", userDetailsTestData.contentType);
		request.addHeader("Origin", userDetailsTestData.origin);
		request.addHeader("Referer", userDetailsTestData.referer);
		request.addHeader("XSRF-TOKEN", testAuthData.get("XSRFTOKEN"));

		UserDetails Actualrespone = GrafanaUserDetailsUtil.getUserDetails(request);
		Assert.assertTrue(Actualrespone.getUsername().equals(testAuthData.get("username")));
	}

	@Test(priority = 2, expectedExceptions = RuntimeException.class)
	public void testGetUserDetailsExceptions() throws InsightsCustomException {

		MockHttpServletRequest request = new MockHttpServletRequest();

		request.addHeader("Accept", userDetailsTestData.accept);
		request.addHeader("Authorization", userDetailsTestData.authorizationException);
		request.addHeader("Content-Type", userDetailsTestData.contentTypeException);
		request.addHeader("Origin", userDetailsTestData.origin);
		request.addHeader("Referer", userDetailsTestData.referer);

		UserDetails ActualresponeExceptions = GrafanaUserDetailsUtil.getUserDetails(request);
		Assert.assertNull(ActualresponeExceptions);

	}

	@Test(priority = 3)
	public void testGetUserDetailsWithToken() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setCookies(userDetailsTestData.cookies);
		request.addHeader("Accept", userDetailsTestData.accept);
		request.addHeader("Authorization", testAuthData.get(AUTHORIZATION));
		request.addHeader("Content-Type", userDetailsTestData.contentType);
		request.addHeader("Origin", userDetailsTestData.origin);
		request.addHeader("Referer", userDetailsTestData.referer);
		request.addHeader("XSRF-TOKEN", testAuthData.get("XSRFTOKEN"));

		String token = AuthenticationUtils.extractAndValidateAuthToken(request);

		UserDetails actualResponse = GrafanaUserDetailsUtil.getUserDetails(token);
		Assert.assertTrue(actualResponse.getUsername().equals(testAuthData.get("username")));

	}
	
	@Test(priority = 4)
	public void testGetUserDetailsWithInvalidToken() throws Exception {

		UserDetails actualResponse = GrafanaUserDetailsUtil.getUserDetails(userDetailsTestData.invalidToken);
		Assert.assertNull(actualResponse);
	}

}
