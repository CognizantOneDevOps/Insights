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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.UnitTestConstant;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.AccessGroupManagement.AccessGroupManagement;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.test.accessGroupManagement.GroupsAndUserTestData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GrafanaAuthenticationTest extends AbstractTestNGSpringContextTests {
	private static final Logger log = LogManager.getLogger(GrafanaAuthenticationTest.class);
	private static final String AUTHORIZATION = "authorization";

	AccessGroupManagement accessGroupManagement = new AccessGroupManagement();

	GroupsAndUserTestData groupsAndUserTestData = new GroupsAndUserTestData();
	MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	
	Map<String, String> testAuthData = new HashMap<>();

	ResultMatcher ok = MockMvcResultMatchers.status().isOk();
	String cookiesString;

	@Autowired
	private WebApplicationContext wac;

	@Resource
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Resource
	private FilterChainProxy springSecurityFilterChain;
	
	@DataProvider
	public void getData() {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + UnitTestConstant.TESTNG_TESTDATA + File.separator
				+ "grafanaAuth.json";
		JsonElement jsonData;
		try {
			jsonData = new JsonParser().parse(new FileReader(path));
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			throw new SkipException("skipped this test case as grafana auth file not found.");
		}
		testAuthData = new Gson().fromJson(jsonData, Map.class);
	}


	@BeforeClass
	public void onInit() throws InterruptedException, IOException, InsightsCustomException {

		Map<String, String> cookiesMap = null;
		try {
			getData();
			httpRequest.addHeader("Authorization", testAuthData.get(AUTHORIZATION));
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
		Map<String, String> headers = new HashMap<>();
		headers.put("Cookie", cookiesString);
		headers.put("Authorization", testAuthData.get(AUTHORIZATION));
		headers.put(HttpHeaders.ORIGIN, ApplicationConfigProvider.getInstance().getInsightsServiceURL());
		headers.put(HttpHeaders.HOST, AuthenticationUtils.getHost(null));
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
		MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
				"/user/authenticate", "", headers,false);

		ResultActions action = this.mockMvc.perform(builder.with(csrf().asHeader()));
		
		MvcResult result = action.andReturn();
		String content = result.getResponse().getContentAsString();
		log.debug("In loginWithCorrectCredentials Response  ======================= {} ",content);
		
		action.andExpect(status().isOk());
    }

	@Test(priority = 2)
	public void loginWithIncorrectCredentials() throws Exception {
		this.mockMvc = getMacMvc();
		Map<String, String> headers = new HashMap<>();
		headers.put("Cookie", cookiesString);
		headers.put("Authorization", testAuthData.get("invalid_autharization"));
		headers.put(HttpHeaders.ORIGIN, ApplicationConfigProvider.getInstance().getInsightsServiceURL());
		headers.put(HttpHeaders.HOST, AuthenticationUtils.getHost(null));
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
		MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam("/user/authenticate",
				"", headers,false);

		ResultActions action = this.mockMvc.perform(builder.with(csrf().asHeader()));
		
		MvcResult result = action.andReturn();
		String content = result.getResponse().getContentAsString();
		log.debug("In loginWithIncorrectCredentials Response  ======================= {} ",content);
		
		action.andExpect(status().is(AuthenticationUtils.UNAUTHORISE));
	}

	@Test(priority = 3)
	public void loginWithIncorrectHost() throws Exception {
		log.debug("In loginWithIncorrectHost =======================");
		this.mockMvc = getMacMvc();
		Map<String, String> headers = new HashMap<>();
		headers.put("Cookie", cookiesString);
		headers.put("Authorization", testAuthData.get(AUTHORIZATION));
		
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
		MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam("/user/authenticate",
				"", headers,true);
		
		builder.header(HttpHeaders.ORIGIN, "http://localhost55.com:8080");
		builder.header(HttpHeaders.HOST, "localhost55");

		ResultActions action = this.mockMvc.perform(builder.with(csrf().asHeader()));
		MvcResult result = action.andReturn();
		String content = result.getResponse().getContentAsString();
	    log.debug("In loginWithIncorrectHost Response  ======================= {} ",content);
	    
		action.andExpect(status().is(403));
	}

	@Test(priority = 4)
	public void loginWithIncorrectHeader() throws Exception {
		this.mockMvc = getMacMvc();
		Map<String, String> headers = new HashMap<>();
		headers.put("Cookie", cookiesString);
		headers.put("Authorization", testAuthData.get(AUTHORIZATION));
		headers.put("user", "<br></br>");
		headers.put(HttpHeaders.ORIGIN, ApplicationConfigProvider.getInstance().getInsightsServiceURL());
		headers.put(HttpHeaders.HOST, AuthenticationUtils.getHost(null));
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
		headers.put(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "*");
		MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam("/user/authenticate",
				"", headers,false);

		ResultActions action = this.mockMvc.perform(builder.with(csrf().asHeader()));
		action.andExpect(status().is(HttpServletResponse.SC_BAD_REQUEST));
	}

	private MockMvc getMacMvc() {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac)
				.addFilter(springSecurityFilterChain);
		return builder.build();
	}

	private MockHttpServletRequestBuilder mockHttpServletRequestBuilderPostWithRequestParam(String url, String content,
			Map<String, String> headers, boolean incorrectHost) {
		MockHttpServletRequestBuilder builder ;
		if(!incorrectHost) {
			builder= MockMvcRequestBuilders.post(url).header(HttpHeaders.ORIGIN,"http://localhost:8080").cookie(httpRequest.getCookies());
		}else {
			builder= MockMvcRequestBuilders.post(url).header(HttpHeaders.ORIGIN,"http://localhost55:8880").cookie(httpRequest.getCookies());
		}
		for(Map.Entry<String, String> entry : headers.entrySet()) {
			builder.header(entry.getKey(), entry.getValue());
		}
		return builder;
	}
}


