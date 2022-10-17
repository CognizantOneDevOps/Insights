/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.healthStatus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.UnitTestConstant;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.health.service.HealthStatusServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class HealthStatusTest extends AbstractTestNGSpringContextTests {

	private static Logger log = LogManager.getLogger(HealthStatusTest.class);
	private static final String AUTHORIZATION = "authorization";

	MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	ResultMatcher ok = MockMvcResultMatchers.status().isOk();
	String cookiesString;
	Map<String, String> testAuthData = new HashMap<>();
	
	@Autowired
	HealthStatusServiceImpl healthStatusServiceImpl;
	
	HealthStatusTestData healthStatusTestData = new HealthStatusTestData();

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

	String host = null;
	Gson gson = new Gson();

	@Resource
	private FilterChainProxy springSecurityFilterChain;

	@DataProvider
	public void getData() {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + UnitTestConstant.TESTNG_TESTDATA + File.separator
				+ "grafanaAuth.json";
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

		Map<String, String> cookiesMap = null;
		try {
			getData();
			httpRequest.addHeader("Authorization", testAuthData.get(AUTHORIZATION));
			cookiesMap = PlatformServiceUtil.getGrafanaCookies(httpRequest);
		} catch (Exception e1) {
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

	private MockMvc getMacMvc() {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
		return builder.build();
	}

	
	@Test(priority = 1)
	public void testGetHealthStatus() throws InsightsCustomException{
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admin/health/globalHealth");
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.debug("Error while testing Get Health Status" +e);
		}
		log.debug("Components health and status retrieved successfully!");
	}
	
	@Test(priority = 2)
	public void testGetAgentsHealthStatus() throws InsightsCustomException{
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admin/health/globalAgentsHealth");
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing Get Agents Health Status" +e);
		}
		log.debug("Agent health and status retrieved successfully!");
	}
	
	@Test(priority = 3)
	public void testGetDetailHealth() throws InsightsCustomException{
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admin/health/detailHealth").param("category", healthStatusTestData.categoryGit).param("tool", healthStatusTestData.toolName).param("agentId", healthStatusTestData.agentId).accept(MediaType.APPLICATION_JSON);
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing Get Detail Health Status" +e);
		}
		log.debug("Agents health details retrieved successfully!");
	}
	
	@Test(priority = 4)
	public void testGetAgentFailureDetails() throws InsightsCustomException{
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/admin/health/getAgentFailureDetails").param("category", healthStatusTestData.categoryGit).param("tool", healthStatusTestData.toolName).param("agentId", healthStatusTestData.failedAgentId).accept(MediaType.APPLICATION_JSON);
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing Get Agent Failure Detail Status" +e);
		}
		log.debug("Agents health failure details retrieved successfully!");
	}
	
	@AfterClass
	public void cleanUp() throws InsightsCustomException, IOException {
		
	}
	
}