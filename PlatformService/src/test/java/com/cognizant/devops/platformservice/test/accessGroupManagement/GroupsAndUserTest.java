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

import java.io.IOException;
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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.AccessGroupManagement.AccessGroupManagement;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GroupsAndUserTest extends AbstractTestNGSpringContextTests {
	private static final Logger log = LogManager.getLogger(GroupsAndUserTest.class);

	AccessGroupManagement accessGroupManagement = new AccessGroupManagement();

	GroupsAndUserTestData groupsAndUserTestData = new GroupsAndUserTestData();
	MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	ResultMatcher ok = MockMvcResultMatchers.status().isOk();
	String cookiesString;

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Resource
	private FilterChainProxy springSecurityFilterChain;

	@BeforeTest
	public void onInit() throws InterruptedException, IOException, InsightsCustomException {
		ApplicationConfigCache.loadConfigCache();
		httpRequest.addHeader("Authorization", groupsAndUserTestData.authorization);
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
				.header("Authorization", groupsAndUserTestData.authorization).header("Cookie", cookiesString)
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
				.header("Authorization", groupsAndUserTestData.authorization).header("Cookie", cookiesString);
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
		log.debug("Test case getOrgUsers pass successfully ");
	}

	@Test(priority = 2)
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
		log.debug("Test case createOrg pass successfully ");
	}

	@Test(priority = 3)
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
		log.debug("Test case addUserInOrg pass successfully ");
	}
	
	@Test(priority = 4)
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
		log.debug("Test case searchUser pass successfully ");
	
	}
	
	@Test(priority = 5)
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
		log.debug("Test case assignUser pass successfully ");
	
	}
	
	@Test(priority = 6)
	public void testAddUserEditor() throws InsightsCustomException {
	
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					groupsAndUserTestData.userPropertyListEditor);
	
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing assignUser " + e);
		}
		log.debug("Test case addUserInOrg pass successfully ");
	
	}
	
	@Test(priority = 7)
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
		log.debug("Test case pass successfully ");
	}
	
	@Test(priority = 8)
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
		log.debug("Test case pass successfully ");
	
	}
	
	@Test(priority = 9)
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
		log.debug("Test case pass successfully ");
	
	}

}
