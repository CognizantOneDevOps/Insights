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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.security.config.grafana.GrafanaUserDetailsUtil;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GrafanaUserDetailsTest {

	GrafanaUserDetailsUtil grafanaUser = new GrafanaUserDetailsUtil();
	GrafanaUserDetailsTestData userDetailsTestData = new GrafanaUserDetailsTestData();

	@Test(priority = 1)
	public void testGetUserDetails() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setCookies(userDetailsTestData.cookies);
		request.addHeader("Accept", userDetailsTestData.accept);
		request.addHeader("Authorization", userDetailsTestData.authorization);
		request.addHeader("Content-Type", userDetailsTestData.contentType);
		request.addHeader("Origin", userDetailsTestData.origin);
		request.addHeader("Referer", userDetailsTestData.referer);
		request.addHeader("XSRF-TOKEN", userDetailsTestData.XSRFTOKEN);

		UserDetails Actualrespone = GrafanaUserDetailsUtil.getUserDetails(request);

	}

	@Test(priority = 2)
	public void testGetUserDetailsExceptions() throws InsightsCustomException {

		MockHttpServletRequest request = new MockHttpServletRequest();

		request.addHeader("Accept", userDetailsTestData.accept);
		request.addHeader("Authorization", userDetailsTestData.authorizationException);
		request.addHeader("Content-Type", userDetailsTestData.contentTypeException);
		request.addHeader("Origin", userDetailsTestData.origin);
		request.addHeader("Referer", userDetailsTestData.referer);

		UserDetails ActualresponeExceptions = GrafanaUserDetailsUtil.getUserDetails(request);

	}

}
