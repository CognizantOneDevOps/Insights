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
import static org.testng.Assert.ARRAY_MISMATCH_TEMPLATE;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformservice.rest.AccessGroupManagement.AccessGroupManagement;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.test.assessmentReports.AssessmentReportServiceData;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@SuppressWarnings("unused")
@WebAppConfiguration
public class GroupsAndUserTest extends GroupsAndUserTestData {
	private static final Logger log = LogManager.getLogger(GroupsAndUserTest.class);
	private static final String AUTHORIZATION = "authorization";

	@Autowired
	AccessGroupManagement accessGroupManagement;
	Map<String, String> testAuthData = new HashMap<>();
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
				+ TestngInitializerTest.TESTNG_TESTDATA + File.separator + "grafanaAuth.json";
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

	private MockHttpServletRequestBuilder mockHttpServletRequestBuilderGet(String url, String content) {
		log.debug(" cookies " + httpRequest.getCookies());
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url).cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString)
				.content(content).contentType(MediaType.APPLICATION_JSON_VALUE);
		return builder;
	}
	private MockMvc getMacMvc() {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
		return builder.build();
	}
	
	private MockHttpServletRequestBuilder mockHttpServletRequestBuilderPostWithRequestParam(String url) {

		return MockMvcRequestBuilders.post(url).cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);
	}
	
	private MockHttpServletRequestBuilder mockHttpServletRequestBuilderGetWithRequestParam(String url)
	{
		return MockMvcRequestBuilders.get(url).cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);
	}
	
	@Test(priority = 1)
	public void testCreateOrg() throws InsightsCustomException {
		try {
			deleteGrafanaOrgId(String.valueOf(getGrafanaOrgId(orgName)));
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/admin/userMgmt/createOrg?orgName=" + orgName);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			this.orgId = actual.get("orgId").getAsInt();
			setOrdId(orgId, orgName);
			Assert.assertEquals(false, actual.get("orgId").getAsString().isEmpty());
			Assert.assertEquals(true, orgId>0);
		} catch (Exception e) {
			log.error("Error while testing testCreateOrg " + e);
		}
		log.debug("Test case createOrg passed successfully ");
	}

	@Test(priority = 2)
	public void testGetOrgUsers() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/admin/userMgmt/getOrgUsers?orgId=" + orgId);

			MvcResult result = this.mockMvc.perform(builder.with(csrf().asHeader())).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(false, actual.get("data").getAsJsonArray().isEmpty());
			} catch (Exception e) {
			log.error("Error while testing getOrgUsers " + e);
		}
		log.debug("Test case getOrgUsers passed successfully ");
	}

	@Test(priority = 3)
	public void testGetOrgUsersV2WithValidOrgID() {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/admin/userMgmt/v2/getOrgUsers?orgId=" + orgId);
			MvcResult result = this.mockMvc.perform(builder.with(csrf().asHeader())).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(false, actual.get("data").getAsString().isEmpty());
			} catch (Exception e) {
			log.error("Error while testing getOrgUsers " + e);
		}
		log.debug("Test case getOrgUsers passed successfully ");
	}
	
	@Test(priority = 4)
	public void testAddNewUserInCurrentOrg() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					NewUserRoleAdmin);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			this.userId=dataJson.get("userId").getAsInt();
			Assert.assertEquals(dataJson.get("message").getAsString(), outcomeResponse);
			Assert.assertTrue("NewUser Added", userId>0);
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}
	
	@Test(priority = 5)
	public void testAddNewUserInCurrentOrgWithExistingLoginName() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					NewUserAdminUserNameExist);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			Assert.assertEquals("Username already exists", dataJson.get("message").getAsString());
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}
	
	@Test(priority = 6)
	public void testAddNewUserInCurrentOrgWithExistingEmail() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					NewUserAdminEmailExist);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			Assert.assertEquals("Email already exists", dataJson.get("message").getAsString());
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}
	
	@Test(priority = 7)
	public void testAddDuplicateUserInCurrentOrgWithSameRole() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					DuplicateUserRoleAdmin);
            MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			Assert.assertEquals("User exists in currrent org with same role", dataJson.get("message").getAsString());
			
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}
	@Test(priority = 8)
	public void testAddDuplicateUserInCurrentOrgWithDifferentRole() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					DuplicateUserRoleViewer);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			Assert.assertEquals("User exists in currrent org with different role", dataJson.get("message").getAsString().trim());
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}
	
	@Test(priority = 9)
	public void testAddNewUserInCurrentOrgWithDifferentRole() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					NewUserRoleViewer);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			this.userId=dataJson.get("userId").getAsInt();
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(dataJson.get("message").getAsString(), outcomeResponse);
			Assert.assertEquals(true, userId>0);
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}

	@Test(priority = 10)
	public void testSearchUserValidationError() throws InsightsCustomException {
		try {
			String userName=NewUserRoleViewerJson.get("name").getAsString();
			userName="&amp;{<"+userName;
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/searchUser",
					userName);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, actual.get("status").getAsString());
		} catch (Exception e) {
			log.error("Error while testing searchUser " + e);
		}
		log.debug("Test case searchUser passed successfully ");
	}
	@Test(priority = 11)
	public void testSearchNoExistingUser() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/searchUser",
					NewUserRoleViewerJson.get("name").getAsString());
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals("User Not Found", actual.get("data").toString().trim().replace("\"", ""));
		} catch (Exception e) {
			log.error("Error while testing searchUser " + e);
		}
		log.debug("Test case searchUser passed successfully ");
	}
	
	@Test(priority = 12)
	public void testSearchExistingUserWithLoginName() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/searchUser",
					NewUserRoleViewerJson.get("userName").getAsString());
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonArray dataJson = JsonUtils.parseStringAsJsonArray(data);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(false, dataJson.isEmpty());
			} catch (Exception e) {
			log.error("Error while testing searchUser " + e);
		}
		log.debug("Test case searchUser passed successfully ");
	}

	@Test(priority = 13)
	public void testAssignUserWithWrongParameter() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/assignUser",
					assignNonExistingUserDataJson.get(0).getAsJsonObject().get("userName").getAsString());
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, actual.get("status").getAsString());
			Assert.assertEquals("Request parameter is not valid.",actual.get("message").getAsString());	
		} catch (Exception e) {
			log.error("Error while testing assignUser " + e);
		}
		log.debug("Test case assignUser passed successfully ");
	}

	@Test(priority = 14)
	public void testAssignUserWithNoExisitngUser() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/assignUser",
					assignNonExistingUserData);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.FAILURE,actual.get("status").getAsString());
			Assert.assertEquals("User does not exsist." ,actual.get("message").toString().replace("\"", ""));
		} catch (Exception e) {
			log.error("Error while testing assignUser " + e);
		}
		log.debug("Test case assignUser passed successfully ");
	}

	@Test(priority = 15)
	public void testAssignExistingUserInCurrentOrg() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/assignUser",
					assignExistingUserDatatoSameOrg);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, actual.get("status").getAsString());
			String data = actual.get("message").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			Assert.assertEquals("User is already member of this organization",dataJson.get("data").getAsJsonObject().get("message").getAsString());
		} catch (Exception e) {
			log.error("Error while testing assignUser " + e);
		}
		log.debug("Test case assignUser passed successfully ");
	}
	
	@Test(priority = 16)
	public void testAssignExistingUserInDifferentOrg() throws InsightsCustomException {
		try {
			tempOrgName="TestInsightsOrg123";
			deleteGrafanaOrgId(String.valueOf(getGrafanaOrgId(tempOrgName)));
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/admin/userMgmt/createOrg?orgName=" + tempOrgName);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			tempOrgId= actual.get("orgId").getAsInt();
			setNewOrdId(tempOrgId, tempOrgName);
			
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder1 = mockHttpServletRequestBuilderPost("/accessGrpMgmt/assignUser",
					assignExistingUserDataToNewOrg);
			MvcResult result1 = this.mockMvc.perform(builder1).andReturn();
			String saveresponse1 = new String(result1.getResponse().getContentAsByteArray());
			JsonObject actual1 = JsonUtils.parseStringAsJsonObject(saveresponse1);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual1.get("status").getAsString());
			String data = actual1.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			Assert.assertEquals(false, data.toString().isEmpty());
		} 
			catch (Exception e) {
			log.error("Error while testing assignUser " + e);
		}
		log.debug("Test case assignUser passed successfully ");
	}

	 @Test(priority = 17)
	public void testEditOrganizationUser() throws InsightsCustomException {
		try {
			role = "Admin";
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			String url = "/admin/userMgmt/editOrganizationUser?orgId=" + tempOrgId + "&userId="
					+ userId + "&role=" + role;
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(url);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals("Organization user updated",actual.get("message").getAsString());
		} catch (Exception e) {
			log.error("Error while testing getOrgUsers " + e);
		}
		log.debug("Test case passed successfully ");
	}

	@Test(priority = 18)
	public void testDeleteOrganizationUser() throws InsightsCustomException {
		try {
			role = "Admin";
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			String url = "/admin/userMgmt/deleteOrganizationUser?orgId=" + tempOrgId + "&userId="
					+ userId + "&role=" + role;
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(url);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals("User removed from organization",actual.get("message").getAsString());
		} catch (Exception e) {
			log.error("Error while testing getOrgUsers " + e);
		}
		log.debug("Test case passed successfully ");
	}

	@Test(priority = 19)
	public void testGetThemePreference() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			String url = "/admin/userMgmt/getThemePreference";
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url)
					.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			Assert.assertEquals(false, actual.get("data").toString().isEmpty());
		} catch (Exception e) {
			log.error("Error while testing getGetThemePreference " + e);
		}
		log.debug("Test case passed successfully ");
	}

	@Test(priority = 20)
	public void testUpdateThemePreference() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			String content = "";
			log.debug(" cookies " + httpRequest.getCookies());
			String url = "/admin/userMgmt/updateThemePreference?themePreference="+ themePreference;
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put(url).cookie(httpRequest.getCookies())
					.content(content).header("Authorization", testAuthData.get(AUTHORIZATION))
					.header("Cookie", cookiesString).accept(MediaType.APPLICATION_JSON);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals("Preferences updated",actual.get("message").getAsString());
		} catch (Exception e) {
			log.error("Error while testing updateGetThemePreference " + e);
		}
		log.debug("Test case passed successfully ");
	}

	@Test(priority = 21)
	public void testAddNewUserInMainOrgWithAdminRole() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					NewUserRoleAdminMainOrg);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			Assert.assertEquals("Organization user updated", dataJson.get("message").getAsString());
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}
	
	@Test(priority = 22)
	public void testAddNewUserInMainOrgWithViewerRole() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					NewUserRoleAdminViewerOrg);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			String data = actual.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			this.userId=dataJson.get("id").getAsInt();
			Assert.assertEquals("User created", dataJson.get("message").getAsString());
			Assert.assertTrue("NewUser Added in main org", userId>0);
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}
	
	@Test(priority = 23)
	public void testAddExistingUserInNewOrg() throws InsightsCustomException {
		try {
			orgName1="TestOrg"+userId;
			setOrgName(orgName1);
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/admin/userMgmt/createOrg?orgName=" + orgName1);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			this.orgId = actual.get("orgId").getAsInt();
			setId(orgId,orgName1);
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder1 = mockHttpServletRequestBuilderPost("/accessGrpMgmt/addUserInOrg",
					ExisitngUserRoleOrg);
			MvcResult result1 = this.mockMvc.perform(builder1).andReturn();
			String saveresponse1 = new String(result1.getResponse().getContentAsByteArray());
			JsonObject actual1 = JsonUtils.parseStringAsJsonObject(saveresponse1);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual1.get("status").getAsString());
			String data = actual1.get("data").toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}");
			JsonObject dataJson = JsonUtils.parseStringAsJsonObject(data);
			this.userId=dataJson.get("userId").getAsInt();
			Assert.assertEquals("User added to organization", dataJson.get("message").getAsString());
			Assert.assertEquals(true, userId>0);
			} catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
		log.debug("Test case addUserInOrg passed successfully ");
	}
	
	@Test(priority = 24)
	public void testGetOrgs() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderGet("/accessGrpMgmt/getOrgs","");
			MvcResult result = this.mockMvc.perform(builder).andReturn();			
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(false, actual.get("data").getAsJsonArray().isEmpty());
	}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 25)
	public void testGetCurrentUserOrgs() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderGet("/accessGrpMgmt/getCurrentUserOrgs","");
			MvcResult result = this.mockMvc.perform(builder).andReturn();			
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(false, actual.get("data").getAsJsonArray().isEmpty());
	}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 26)
	public void testGetUser() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderGet("/accessGrpMgmt/getUser","");
			MvcResult result = this.mockMvc.perform(builder).andReturn();			
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(false, actual.get("data").isJsonNull());
	}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 27)
	public void testGetCurrentUserWithOrgs() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderGet("/accessGrpMgmt/getCurrentUserWithOrgs","");
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(false, actual.get("data").getAsJsonObject().get("userDetail").isJsonNull());
			Assert.assertEquals(false, actual.get("data").getAsJsonObject().get("orgArray").getAsJsonArray().isEmpty());
			
		}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 28)
	public void testGetGrafanaVersion() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/accessGrpMgmt/getGrafanaVersion");
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
			Assert.assertEquals(false, actual.get("data").getAsJsonObject().get("version").isJsonNull());
			
		}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 29)
	public void testLoadDashboardDataGrafana() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
				.get("/accessGrpMgmt/getDashboardsFoldersDetail").cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());
		    if (actual.get("data").getAsJsonObject().get("general").toString().contains("uid"))
		    {
		    	uid = actual.get("data").getAsJsonObject().get("general").getAsJsonArray().get(0).getAsJsonObject().get("uid").getAsString();
		    	SetUid(uid);
		    }
		}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 30)
	public void testLoadDashboardData() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
				.get("/accessGrpMgmt/dashboards").cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(true, saveresponse.contains("dashboards"));
			}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 31)
	public void testTemplateQueryResults() throws InsightsCustomException {
		try {
			JsonObject response = accessGroupManagement.getTemplateQueryResults("");
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(PlatformServiceConstants.SUCCESS,actual);
			}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 32)
	public void testGetDashboardByUID() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
//			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderGetWithRequestParam(
//					"/accessGrpMgmt/getDashboardByUid?uuid=OPSYF6S4z&orgId=1");
			
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderGetWithRequestParam(
					"/accessGrpMgmt/getDashboardByUid?uuid="+uid+"&orgId=1");
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
		}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 33)
	public void testGetDashboardByDBUID() throws InsightsCustomException {
		try {
			JsonObject response = accessGroupManagement.getDashboardByDBUid("OPSYF6S4z",1);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual);
		}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 34)
	public void testGetDashboardByOrgID() throws InsightsCustomException {
		try {
			JsonObject response = accessGroupManagement.getDashboardByOrg(1);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual);
		}catch (Exception e) {
		log.error("Error while testing testAddUser " + e);
	}
	}
	
	@Test(priority = 35)
	public void testSwitchUserOrg() throws InsightsCustomException {
		try {  
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(
					"/accessGrpMgmt/switchUserOrg?orgId=1");
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual.get("status").getAsString());			
		}catch (Exception e) {
			log.error("Error while testing testAddUser " + e);
		}
	}
	
	@Test(priority = 36)
	public void cleanUp() {
		try { 
		deleteGrafanaOrgId(String.valueOf(getGrafanaOrgId(orgName)));
		deleteGrafanaOrgId(String.valueOf(getGrafanaOrgId(orgName1)));
		}catch(Exception e) {
			log.error("Error while CleanUp " + e);
		}
	}
}