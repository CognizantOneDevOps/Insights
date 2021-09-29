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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
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
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.UnitTestConstant;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformservice.grafanadashboard.controller.GrafanaDashboardReportController;
import com.cognizant.devops.platformservice.grafanadashboard.service.GrafanaPdfServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformservice.grafanadashboard.service.GrafanaPdfServiceImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GrafanaDashboardReportTest extends GrafanaDashboardReportData{
	
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
	public void publishGrafanaDashboardDetails() throws InsightsCustomException {

		try {
            GrafanaHandler grafanaHandler = new GrafanaHandler();
            
            Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
            String responseAuthKey = grafanaHandler.grafanaGet( "/api/auth/keys", headers);
                   JsonArray detailsOfAPIJsonArray = new JsonParser().parse(responseAuthKey).getAsJsonArray();
            
                   for (JsonElement jsonElement : detailsOfAPIJsonArray) {
                          if(jsonElement.getAsJsonObject().has("id") && jsonElement.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("pdftoken")) {
                                try {
                                       grafanaHandler.grafanaDelete( "/api/auth/keys/" + jsonElement.getAsJsonObject().get("id"), headers);
                                } catch (Exception e) {
                                      log.error(" Unable to delete API token ",e);
                                      log.error(e);
                                }
                          }
                   }
			this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			String saveRequest = "{\"loadTime\":\"20\",\"title\":\"verify-maturity\",\"source\":\"PLATFORM\",\"pdfType\":[\"Dashboard\"],\"variables\":\"Product=Central,Product=Astro,Product=Mars,Product=Force,from=undefined,to=undefined\",\"dashUrl\":\"http://localhost:3000/dashboard/db/3-digital-transformation-application-view?orgId=1&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"panelUrls\":[\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=76&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=77&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=42&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=72&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=61&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=62&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=43&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=65&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=66&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=44&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=63&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=47&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=64&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=57&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=46&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=67&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=68&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=59&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=48&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\"],\"metadata\":[{\"testDB\":\"false\"}],\"email\":\"KanchiSai.Hemanth@cognizant.com\",\"senderEmailAddress\":\"KanchiSai.Hemanth@cognizant.com\",\"mailSubject\":\"3-digital-transformation-application-view\",\"mailBodyTemplate\":\"Very Good\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"range\":\"\",\"emailBody\":\"\",\"scheduleType\":\"ONETIME\",\"organisation\":\"1\",\"dashboard\":\"QFd2ks6Wk\"}";		
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/dashboardReport/exportPDF/getDashboardAsPDF",saveRequest);
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.error("Error while testing Save Dashboard " + e);
		}
		log.debug("Test case Save Dashboard successfully ");

	}
	
	@Test(priority = 2)
    public void getAllGrafanaDashboardConfigs() throws InsightsCustomException {
        try {
            GrafanaHandler grafanaHandler = new GrafanaHandler();
            
            Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
            String responseAuthKey = grafanaHandler.grafanaGet( "/api/auth/keys", headers);
                   JsonArray detailsOfAPIJsonArray = new JsonParser().parse(responseAuthKey).getAsJsonArray();
            
                   for (JsonElement jsonElement : detailsOfAPIJsonArray) {
                          if(jsonElement.getAsJsonObject().has("id") && jsonElement.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("pdftoken")) {
                                try {
                                       grafanaHandler.grafanaDelete( "/api/auth/keys/" + jsonElement.getAsJsonObject().get("id"), headers);
                                } catch (Exception e) {
                                      log.error(" Unable to delete API token ",e);
                                      log.error(e);
                                }
                          }
                   }
        	this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/dashboardReport/exportPDF/fetchGrafanaDashboardConfigs").cookie(httpRequest.getCookies())
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
            GrafanaHandler grafanaHandler = new GrafanaHandler();
            
            Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
            String responseAuthKey = grafanaHandler.grafanaGet( "/api/auth/keys", headers);
                   JsonArray detailsOfAPIJsonArray = new JsonParser().parse(responseAuthKey).getAsJsonArray();
            
                   for (JsonElement jsonElement : detailsOfAPIJsonArray) {
                          if(jsonElement.getAsJsonObject().has("id") && jsonElement.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("pdftoken")) {
                                try {
                                       grafanaHandler.grafanaDelete( "/api/auth/keys/" + jsonElement.getAsJsonObject().get("id"), headers);
                                } catch (Exception e) {
                                      log.error(" Unable to delete API token ",e);
                                      log.error(e);
                                }
                          }
                   }
        	String updateRequest = "{\"loadTime\":\"25\",\"title\":\"verify-maturity\",\"source\":\"PLATFORM\",\"pdfType\":[\"Dashboard\"],\"variables\":\"Product=Central,Product=Astro,Product=Mars,Product=Force,from=undefined,to=undefined\",\"dashUrl\":\"http://localhost:3000/dashboard/db/3-digital-transformation-application-view?orgId=1&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"panelUrls\":[\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=76&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=77&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=42&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=72&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=61&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=62&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=43&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=65&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=66&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=44&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=63&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=47&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=64&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=57&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=46&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=67&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=68&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=59&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\",\"http://localhost:3000/d/QFd2ks6Wk/3-digital-transformation-application-view?viewPanel=48&var-Product=Central&var-Product=Astro&var-Product=Mars&var-Product=Force&from=now-5y&to=now\"],\"metadata\":[{\"testDB\":\"false\"}],\"email\":\"KanchiSai.Hemanth@cognizant.com\",\"senderEmailAddress\":\"KanchiSai.Hemanth@cognizant.com\",\"mailSubject\":\"3-digital-transformation-application-view\",\"mailBodyTemplate\":\"Very Good\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"range\":\"\",\"emailBody\":\"\",\"scheduleType\":\"ONETIME\",\"organisation\":\"1\",\"dashboard\":\"QFd2ks6Wk\"}";		
        	int id =0;
            List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
            for(GrafanaDashboardPdfConfig g: list){
                if(g.getTitle().equalsIgnoreCase("verify-maturity")) {
                id= g.getId();
            }
            }
            JsonObject detailsJson = new JsonParser().parse(updateRequest).getAsJsonObject();
        	detailsJson.toString();
            detailsJson.addProperty("id", id);
            this.mockMvc = getMacMvc();
			log.debug(" cookies " + httpRequest.getCookies());
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/dashboardReport/exportPDF/updateDashboardConfig",updateRequest);
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
           GrafanaHandler grafanaHandler = new GrafanaHandler();
            
            Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
            String responseAuthKey = grafanaHandler.grafanaGet( "/api/auth/keys", headers);
                   JsonArray detailsOfAPIJsonArray = new JsonParser().parse(responseAuthKey).getAsJsonArray();
            
                   for (JsonElement jsonElement : detailsOfAPIJsonArray) {
                          if(jsonElement.getAsJsonObject().has("id") && jsonElement.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("pdftoken")) {
                                try {
                                       grafanaHandler.grafanaDelete( "/api/auth/keys/" + jsonElement.getAsJsonObject().get("id"), headers);
                                } catch (Exception e) {
                                      log.error(" Unable to delete API token ",e);
                                      log.error(e);
                                }
                          }
                   }
        	grafanaDashboardConfigDAL.fetchGrafanaDashboardDetailsByWorkflowId("GRAFANADASHBOARDPDFREPORT_1620379924");
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }
    }
	
	
	@Test(priority = 5)
    public void deleteGrafanaDashboardDetails() throws InsightsCustomException {
		try {
            GrafanaHandler grafanaHandler = new GrafanaHandler();
            
            Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
            String responseAuthKey = grafanaHandler.grafanaGet( "/api/auth/keys", headers);
                   JsonArray detailsOfAPIJsonArray = new JsonParser().parse(responseAuthKey).getAsJsonArray();
            
                   for (JsonElement jsonElement : detailsOfAPIJsonArray) {
                          if(jsonElement.getAsJsonObject().has("id") && jsonElement.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("pdftoken")) {
                                try {
                                       grafanaHandler.grafanaDelete( "/api/auth/keys/" + jsonElement.getAsJsonObject().get("id"), headers);
                                } catch (Exception e) {
                                      log.error(" Unable to delete API token ",e);
                                      log.error(e);
                                }
                          }
                   }
			this.mockMvc = getMacMvc();
			
			log.debug(" cookies " + httpRequest.getCookies());
			
			 int id =0;
		        List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
		        for(GrafanaDashboardPdfConfig g: list){
		            if(g.getTitle().equalsIgnoreCase("verify-maturity")) {
		            id= g.getId();
		            String url = "/dashboardReport/exportPDF/deleteDashboardConfig?id=" + id;

		            MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPostWithRequestParam(url, "");
		   	
		            this.mockMvc.perform(builder).andExpect(ok);
		            }
		   }       

          }catch (Exception e) {
			log.error("Error while testing Delete Dashboard " + e);
		}
		log.debug("Test case Deleted Dashboard successfully ");

	}
}
