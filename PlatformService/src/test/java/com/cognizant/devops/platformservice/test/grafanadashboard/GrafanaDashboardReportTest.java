/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.grafanadashboard;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.AssertJUnit;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.UnitTestConstant;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.grafanadashboard.service.GrafanaPdfServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GrafanaDashboardReportTest extends GrafanaDashboardReportData {

	private static final Logger log = LogManager.getLogger(GrafanaDashboardReportTest.class);
	private static final String AUTHORIZATION = "authorization";

	Map<String, String> testAuthData = new HashMap<>();

	MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	ResultMatcher ok = MockMvcResultMatchers.status().isOk();
	String cookiesString;

	GrafanaPdfServiceImpl grafanaPdfServiceImpl = new GrafanaPdfServiceImpl();

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@DataProvider
	public void getData() {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ UnitTestConstant.TESTNG_TESTDATA + File.separator + "grafanaAuth.json";
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
	
	private void deleteApiKeys() {
		try {
			GrafanaHandler grafanaHandler = new GrafanaHandler();

			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String responseAuthKey = grafanaHandler.grafanaGet("/api/auth/keys", headers);
			JsonArray detailsOfAPIJsonArray = new JsonParser().parse(responseAuthKey).getAsJsonArray();

			for (JsonElement jsonElement : detailsOfAPIJsonArray) {
				if (jsonElement.getAsJsonObject().has("id")
						&& jsonElement.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("pdftoken")) {
					try {
						grafanaHandler.grafanaDelete("/api/auth/keys/" + jsonElement.getAsJsonObject().get("id"),
								headers);
					} catch (Exception e) {
						log.error(" Unable to delete API token ", e);
						log.error(e);
					}
				}
			}
		} catch (Exception e) {
			log.error(" Unable to delete API token ", e);
		}
	}

	@Test(priority = 1)
	public void publishGrafanaDashboardDetails() throws InsightsCustomException {

		try {
			deleteApiKeys();
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			//String saveRequest = "{\"loadTime\":\"20\",\"title\":\"verify-maturity\",\"source\":\"PLATFORM\",\"pdfType\":[\"Dashboard\"],\"variables\":\"Product=Central,Product=Astro,Product=Mars,Product=Force,from=undefined,to=undefined\",\"dashUrl\":\"http://localhost:3000/dashboard/db/3-digital-transformation-application-view?orgId=1&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"panelUrls\":[\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=76&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=77&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=42&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=72&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=61&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=62&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=43&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=65&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=66&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=44&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=63&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=47&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=64&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=57&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=46&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=67&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=68&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=59&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=48&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\"],\"metadata\":[{\"testDB\":\"false\"}],\"email\":\"KanchiSai.Hemanth@cognizant.com\",\"senderEmailAddress\":\"KanchiSai.Hemanth@cognizant.com\",\"mailSubject\":\"3-digital-transformation-application-view\",\"mailBodyTemplate\":\"Very Good\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"range\":\"\",\"emailBody\":\"\",\"scheduleType\":\"ONETIME\",\"organisation\":\"1\",\"dashboard\":\"QFd2ks6Wk\"}";
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/dashboardReport/exportPDF/saveDashboardAsPDF", dashboardJson);
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing Save Dashboard " + e);
		}
		log.debug("Test case Save Dashboard successfully ");

	}

	@Test(priority = 2)
	public void getAllGrafanaDashboardConfigs() throws InsightsCustomException {
		try {
			deleteApiKeys();
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());

			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
					.get("/dashboardReport/exportPDF/fetchGrafanaDashboardConfigs").cookie(httpRequest.getCookies())
					.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);

			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing Fetch Dashboard Records" + e);
		}
		log.debug("Test case Fetch Dashboard successfully ");
	}

	@Test(priority = 3)
	public void updateGrafanaDashboardDetails() throws InsightsCustomException {
		try {
			deleteApiKeys();
			int id = 0;
			List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
			for (GrafanaDashboardPdfConfig g : list) {
				if (g.getTitle().equalsIgnoreCase("5-sprint-score-card")) {
					id = g.getId();
				}
			}
			JsonObject detailsJson = new JsonParser().parse(updateJson).getAsJsonObject();
			detailsJson.toString();
			detailsJson.addProperty("id", id);
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/dashboardReport/exportPDF/updateDashboardConfig", updateJson);
			grafanaPdfServiceImpl.updateGrafanaDashboardDetails(detailsJson);
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing Update Dashboard " + e);
		}
		log.debug("Test case Update Dashboard successfully ");

	}

	@Test(priority = 4)
	public void fetchGrafanaDashboardDetailsByWorkflowId() throws InsightsCustomException {
		GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
		try {
			deleteApiKeys();
			grafanaDashboardConfigDAL.fetchGrafanaDashboardDetailsByWorkflowId("GRAFANADASHBOARDPDFREPORT_1620379924");
		} catch (AssertionError e) {
			AssertJUnit.fail(e.getMessage());
		}
	}

	@Test(priority = 5)
	public void testUpdateDashboardPdfConfigStatus() throws InsightsCustomException {
		try {
			deleteApiKeys();
			int id = 0;
			List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
			for (GrafanaDashboardPdfConfig g : list) {
				if (g.getTitle().equalsIgnoreCase("verify-maturity")) {
					id = g.getId();
				}
			}
			String dashboardString = "{\"id\":" + id + ",\"status\":\"RESTART\"}";
			JsonObject detailsJson = new JsonParser().parse(dashboardString).getAsJsonObject();
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/dashboardReport/exportPDF/updateDasboardStatus", detailsJson.toString());
			String status = grafanaPdfServiceImpl.updateDashboardPdfConfigStatus(detailsJson);
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing Update Dashboard Status" + e);
		}
	}

	@Test(priority = 6)
	public void testgetEmailConfiguration() throws InsightsCustomException {
		try {
			deleteApiKeys();
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
					.get("/dashboardReport/exportPDF/getEmailConfigurationStatus");
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing  Get Email Configuration status " + e);
		}
	}


	@Test(priority = 7)
	public void saveGrafanaDashboardConfigTest() throws Exception {
		JsonObject dashboardJsonObject = convertStringIntoJson(dashboardJson);
		
		
		grafanaPdfServiceImpl.saveGrafanaDashboardConfig(dashboardJsonObject);

		List<GrafanaDashboardPdfConfig> allGrafanaDashboardConfigs = grafanaPdfServiceImpl
				.getAllGrafanaDashboardConfigs();
		int id = 0;
		for (GrafanaDashboardPdfConfig grafanaDashboardPdfConfig : allGrafanaDashboardConfigs) {
			if (grafanaDashboardPdfConfig.getTitle().equalsIgnoreCase("5-sprint-score-card")) {
				id = grafanaDashboardPdfConfig.getId();
			}
		}
		GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
		GrafanaDashboardPdfConfig outputConfig = grafanaDashboardConfigDAL .getWorkflowById(id);

		assertEquals(outputConfig.getTitle(), allGrafanaDashboardConfigs.get(0).getTitle());
	}
	
	@Test(priority = 7, expectedExceptions = InsightsCustomException.class)
	public void saveGrafanaDashboardConfigExceptionTest() throws Exception {
		JsonObject dashboardJsonObject = new JsonObject();		
		grafanaPdfServiceImpl.saveGrafanaDashboardConfig(dashboardJsonObject);
	}

	@Test(priority = 8)
	public void getAllGrafanaDashboardConfigsTest() throws InsightsCustomException {
		List<GrafanaDashboardPdfConfig> allGrafanaDashboardConfigs = grafanaPdfServiceImpl
				.getAllGrafanaDashboardConfigs();
		assertTrue(allGrafanaDashboardConfigs.size() > 0);
	}
	
	@Test(priority = 9)
	public void updateGrafanaDashboardDetailsTest() throws InsightsCustomException {
		List<GrafanaDashboardPdfConfig> allGrafanaDashboardConfigs = grafanaPdfServiceImpl
				.getAllGrafanaDashboardConfigs();
		int id = 0;
		for (GrafanaDashboardPdfConfig grafanaDashboardPdfConfig : allGrafanaDashboardConfigs) {
			if (grafanaDashboardPdfConfig.getTitle().equalsIgnoreCase("5-sprint-score-card")) {
				id = grafanaDashboardPdfConfig.getId();
			}
		}
		
		JsonObject dashObject = (JsonObject) new JsonParser().parse(updateJson);
		dashObject.addProperty("id", id);
		grafanaPdfServiceImpl.updateGrafanaDashboardDetails(dashObject);
		List<GrafanaDashboardPdfConfig> updateDashboardPdfConfigs = grafanaPdfServiceImpl
				.getAllGrafanaDashboardConfigs();
		assertEquals("5-sprint-score-card-updated", updateDashboardPdfConfigs.get(0).getTitle());

	}
	
	@Test(priority = 10, expectedExceptions = InsightsCustomException.class)
	public void updateGrafanaDashboardDetailsExceptionTest() throws InsightsCustomException {
		
		int id = 0;
		JsonObject dashObject = (JsonObject) new JsonParser().parse(updateJson);
		dashObject.addProperty("id", id);
		grafanaPdfServiceImpl.updateGrafanaDashboardDetails(dashObject);
	}
	

	@Test(priority = 11)
	public void updateDashboardPdfConfigStatusTest() throws InsightsCustomException {
		int id = 0;
		List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
		for (GrafanaDashboardPdfConfig g : list) {
			if (g.getTitle().equalsIgnoreCase("5-sprint-score-card-updated")) {
				id = g.getId();
			}
		}
		String dashboardString = "{\"id\":" + id + ",\"status\":\"RESTART\"}";
		JsonObject detailsJson = new JsonParser().parse(dashboardString).getAsJsonObject();
		
		String status = grafanaPdfServiceImpl.updateDashboardPdfConfigStatus(detailsJson);
		
		assertEquals(status, PlatformServiceConstants.SUCCESS);
		
	}
	
	@Test(priority = 12, expectedExceptions = InsightsCustomException.class)
	public void updateDashboardPdfConfigStatusExceptionTest() throws InsightsCustomException {
		int id = 0;
		String dashboardString = "{\"id\":" + id + ",\"status\":\"RESTART\"}";
		JsonObject detailsJson = new JsonParser().parse(dashboardString).getAsJsonObject();
		grafanaPdfServiceImpl.updateDashboardPdfConfigStatus(detailsJson);
	}
	
	@Test(priority = 13)
	public void setDashboardActiveStatusTest() throws InsightsCustomException {
		int id = 0;
		List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
		for (GrafanaDashboardPdfConfig g : list) {
			if (g.getTitle().equalsIgnoreCase("5-sprint-score-card-updated")) {
				id = g.getId();
			}
		}
		String dashboardJsonString = "{\"id\":" + id  + ", \"isActive\": \"false\"}";
		JsonObject dashboardJson = new JsonParser().parse(dashboardJsonString).getAsJsonObject();
		String status = grafanaPdfServiceImpl.setDashboardActiveState(dashboardJson);
		assertEquals(status, PlatformServiceConstants.SUCCESS);
	}
	
	@Test(priority = 14, expectedExceptions = InsightsCustomException.class)
	public void setDashboardActiveStatusExceptionTest() throws InsightsCustomException {
		int id = 0;
		String dashboardJsonString = "{\"id\":" + id  + ", \"isActive\": \"false\"}";
		JsonObject dashboardJson = new JsonParser().parse(dashboardJsonString).getAsJsonObject();
		String status = grafanaPdfServiceImpl.setDashboardActiveState(dashboardJson);
	}
	
	@Test(priority = 15)
	public void deleteGrafanaDashboardDetailsTest() throws InsightsCustomException {
		int id = 0;
		List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
		for (GrafanaDashboardPdfConfig g : list) {
			if (g.getTitle().equalsIgnoreCase("5-sprint-score-card-updated")) {
				id = g.getId();
			}
		}
		grafanaPdfServiceImpl.deleteGrafanaDashboardDetails(id);
		
		List<GrafanaDashboardPdfConfig> allGrafanaDashboardConfigs = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
		assertEquals(allGrafanaDashboardConfigs.size(), 0);
	}
	
	@Test(priority = 16)
	public void deleteGrafanaDashboardDetails() throws InsightsCustomException {
		try {
			deleteApiKeys();
			
			this.mockMvc = getMacMvc();

			log.debug(" cookies " + httpRequest.getCookies());

			int id = 0;
			List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
			for (GrafanaDashboardPdfConfig g : list) {
				if (g.getTitle().equalsIgnoreCase("5-sprint-score-card-updated")) {
					id = g.getId();
					String url = "/dashboardReport/exportPDF/deleteDashboardConfig?id=" + id;

					MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(url, "");

					this.mockMvc.perform(builder).andExpect(ok);
				}
			}

		} catch (Exception e) {
			log.error("Error while testing Delete Dashboard " + e);
		}
		log.debug("Test case Deleted Dashboard successfully ");

	}
	
	
}
