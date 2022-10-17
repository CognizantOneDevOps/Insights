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

package com.cognizant.devops.platformservice.test.accessGroupManagement;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.UnitTestConstant;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.AccessGroupManagement.AccessGroupManagement;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GroupsAndUserTest extends AbstractTestNGSpringContextTests {
	private static final Logger log = LogManager.getLogger(GroupsAndUserTest.class);
	private static final String AUTHORIZATION = "authorization";

	AccessGroupManagement accessGroupManagement = new AccessGroupManagement();

	Map<String, String> testAuthData = new HashMap<>();

	GroupsAndUserTestData groupsAndUserTestData = new GroupsAndUserTestData();
	MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	ResultMatcher ok = MockMvcResultMatchers.status().isOk();
	String cookiesString;

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Resource
	private FilterChainProxy springSecurityFilterChain;

	@DataProvider
	public void getData() {
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
		httpRequest.addHeader("Authorization", testAuthData.get(AUTHORIZATION));
		Map<String, String> cookiesMap = PlatformServiceUtil.getGrafanaCookies(httpRequest);

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

	private MockHttpServletRequestBuilder mockHttpServletRequestBuilderPost(String url, String content) {
		log.debug(" cookies " + httpRequest.getCookies());
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url).cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString)
				.content(content).contentType(MediaType.APPLICATION_JSON_VALUE);
		return builder;
	}

	private MockMvc getMacMvc() {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
		return builder.build();
	}

	private MockHttpServletRequestBuilder mockHttpServletRequestBuilderPostWithRequestParam(String url,
			String content) {

		return MockMvcRequestBuilders.post(url).cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);
	}

	@Test(priority = 1)
	public void testGetOrgUsers() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/admin/userMgmt/getOrgUsers?orgId=" + groupsAndUserTestData.orgId, "");

			this.mockMvc.perform(builder.with(csrf().asHeader())).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing getOrgUsers " + e);
		}
		log.debug("Test case getOrgUsers passed successfully ");
	}

	@Test(priority = 2)
	public void testGetOrgUsersV2() {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/admin/userMgmt/v2/getOrgUsers?orgId=" + groupsAndUserTestData.orgId, "");

			this.mockMvc.perform(builder.with(csrf().asHeader())).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing getOrgUsers " + e);
		}
		log.debug("Test case getOrgUsers passed successfully ");
	}

	@Test(priority = 3)
	public void testCreateOrg() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/admin/userMgmt/createOrg?orgName=" + groupsAndUserTestData.orgName, "");
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing testCreateOrg " + e);
		}
		log.debug("Test case createOrg passed successfully ");
	}

	@Test(priority = 4)
	public void testAddUser() throws InsightsCustomException {

		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					groupsAndUserTestData.userPropertyListAdmin);

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}

	@Test(priority = 5)
	public void testSearchUser() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/searchUser",
					groupsAndUserTestData.userName);

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing searchUser " + e);
		}
		log.debug("Test case searchUser passed successfully ");

	}

	// @Test(priority = 5)
	public void testassignUser() throws InsightsCustomException {

		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/assignUser",
					groupsAndUserTestData.assignUserData);

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing assignUser " + e);
		}
		log.debug("Test case assignUser passed successfully ");

	}

	@Test(priority = 7)
	public void testAddUserEditor() throws InsightsCustomException {

		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					groupsAndUserTestData.userPropertyListEditor);

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing assignUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");

	}

	@Test(priority = 8)
	public void testAddUserViewer() throws InsightsCustomException {

		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					groupsAndUserTestData.userPropertyListViewer);

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing assignUser " + e);
		}
		log.debug("Test case passed successfully ");
	}

	// @Test(priority = 8)
	public void testEditOrganizationUser() throws InsightsCustomException {

		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			String url = "/admin/userMgmt/editOrganizationUser?orgId=" + groupsAndUserTestData.orgId + "&userId="
					+ groupsAndUserTestData.userId + "&role=" + groupsAndUserTestData.role;
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(url, "");

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing getOrgUsers " + e);
		}
		log.debug("Test case passed successfully ");

	}

	@Test(priority = 10)
	public void testDeleteOrganizationUser() throws InsightsCustomException {

		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			String url = "/admin/userMgmt/deleteOrganizationUser?orgId=" + groupsAndUserTestData.orgId + "&userId="
					+ groupsAndUserTestData.userId + "&role=" + groupsAndUserTestData.role;
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(url, "");

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing getOrgUsers " + e);
		}
		log.debug("Test case passed successfully ");

	}

	@Test(priority = 11)
	public void testGetThemePreference() throws InsightsCustomException {

		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			String url = "/admin/userMgmt/getThemePreference";
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url)
					.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing getGetThemePreference " + e);
		}
		log.debug("Test case passed successfully ");

	}

	@Test(priority = 12)
	public void testUpdateThemePreference() throws InsightsCustomException {

		try {
			this.mockMvc = getMacMvc();
			String content = "";
			log.debug(" cookies " + httpRequest.getCookies());
			String url = "/admin/userMgmt/updateThemePreference?themePreference="
					+ groupsAndUserTestData.themePreference;
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put(url).cookie(httpRequest.getCookies())
					.content(content).header("Authorization", testAuthData.get(AUTHORIZATION))
					.header("Cookie", cookiesString).accept(MediaType.APPLICATION_JSON);
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing updateGetThemePreference " + e);
		}
		log.debug("Test case passed successfully ");

	}

}
