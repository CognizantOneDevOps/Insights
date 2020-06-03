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

package com.cognizant.devops.platformservice.test.grafanaAuthentication;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.AccessGroupManagement.AccessGroupManagement;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.test.accessGroupManagement.GroupsAndUserTestData;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GrafanaAuthenticationTest extends AbstractTestNGSpringContextTests {
	private static final Logger log = LogManager.getLogger(GrafanaAuthenticationTest.class);

	AccessGroupManagement accessGroupManagement = new AccessGroupManagement();

	GroupsAndUserTestData groupsAndUserTestData = new GroupsAndUserTestData();
	MockHttpServletRequest httpRequest = new MockHttpServletRequest();

	ResultMatcher ok = MockMvcResultMatchers.status().isOk();
	String cookiesString;

	@Autowired
	private WebApplicationContext wac;

	@Resource
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Resource
	private FilterChainProxy springSecurityFilterChain;


	@BeforeTest
	public void onInit() throws InterruptedException, IOException, InsightsCustomException {
		ApplicationConfigCache.loadConfigCache();

		Map<String, String> cookiesMap = null;
		try {
			httpRequest.addHeader("Authorization", GrafanaAuthenticationTestData.authorization);
			cookiesMap = PlatformServiceUtil.getGrafanaCookies(httpRequest);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		cookiesString = cookiesMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining(";"));
		log.debug(" cookiesString " + cookiesString);
		for (Map.Entry<String, String> entry : cookiesMap.entrySet()) {
			Cookie cookie = new Cookie(entry.getKey(), entry.getValue());
			cookie.setHttpOnly(true);
			cookie.setMaxAge(60 * 30);
			cookie.setPath("/");
			httpRequest.setCookies(cookie);
		}
    }

	@Test(priority = 1)
    public void loginWithCorrectCredentials() throws Exception {

		this.mockMvc = getMacMvc();
		Map<String, String> headers = new HashMap();
		headers.put("Cookie", cookiesString);
		headers.put("Authorization", GrafanaAuthenticationTestData.authorization);
		headers.put(HttpHeaders.ORIGIN, ApplicationConfigProvider.getInstance().getInsightsServiceURL());
		headers.put(HttpHeaders.HOST, AuthenticationUtils.getHost(null));
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
		MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
				"/user/authenticate", "", headers);

		ResultActions action = this.mockMvc.perform(builder.with(csrf().asHeader()));
		action.andExpect(status().isOk());
    }

	@Test(priority = 2)
	public void loginWithIncorrectCredentials() throws Exception {
		this.mockMvc = getMacMvc();
		Map<String, String> headers = new HashMap();
		headers.put("Cookie", cookiesString);
		headers.put("Authorization", GrafanaAuthenticationTestData.invalid_autharization);
		headers.put(HttpHeaders.ORIGIN, ApplicationConfigProvider.getInstance().getInsightsServiceURL());
		headers.put(HttpHeaders.HOST, AuthenticationUtils.getHost(null));
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
		MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam("/user/authenticate",
				"", headers);

		ResultActions action = this.mockMvc.perform(builder.with(csrf().asHeader()));
		action.andExpect(status().is(AuthenticationUtils.UNAUTHORISE));
	}

	@Test(priority = 3)
	public void loginWithIncorrectHost() throws Exception {
		this.mockMvc = getMacMvc();
		Map<String, String> headers = new HashMap();
		headers.put("Cookie", cookiesString);
		headers.put("Authorization", GrafanaAuthenticationTestData.authorization);
		headers.put(HttpHeaders.ORIGIN, "http://localhost55.com:8080");
		headers.put(HttpHeaders.HOST, "localhost55");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
		MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam("/user/authenticate",
				"", headers);

		ResultActions action = this.mockMvc.perform(builder.with(csrf().asHeader()));
		action.andExpect(status().is(AuthenticationUtils.INFORMATION_MISMATCH));
	}

	@Test(priority = 4)
	public void loginWithIncorrectHeader() throws Exception {
		this.mockMvc = getMacMvc();
		Map<String, String> headers = new HashMap();
		headers.put("Cookie", cookiesString);
		headers.put("Authorization", GrafanaAuthenticationTestData.authorization);
		headers.put("user", "<br></br>");
		headers.put(HttpHeaders.ORIGIN, ApplicationConfigProvider.getInstance().getInsightsServiceURL());
		headers.put(HttpHeaders.HOST, AuthenticationUtils.getHost(null));
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
		MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam("/user/authenticate",
				"", headers);

		ResultActions action = this.mockMvc.perform(builder.with(csrf().asHeader()));
		action.andExpect(status().is(HttpServletResponse.SC_BAD_REQUEST));
	}

	private MockMvc getMacMvc() {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac)
				.addFilter(springSecurityFilterChain);
		return builder.build();
	}

	private MockHttpServletRequestBuilder mockHttpServletRequestBuilderPostWithRequestParam(String url, String content,
			Map<String, String> headers) {
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url).cookie(httpRequest.getCookies());
		
		for(Map.Entry<String, String> entry : headers.entrySet()) {
			builder.header(entry.getKey(), entry.getValue());
		}
		return builder;
	}
}


