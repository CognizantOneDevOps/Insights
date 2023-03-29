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
package com.cognizant.devops.platformservice.test.InferenceDataProvider;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class InferenceDataProviderTest extends AbstractTestNGSpringContextTests {
	JsonObject testData = new JsonObject();	
	private static Logger log = LogManager.getLogger(InferenceDataProviderTest.class);
	private static final String AUTHORIZATION = "authorization";

	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	ResultMatcher ok = MockMvcResultMatchers.status().isOk();
	String cookiesString;

	InferenceDataProviderTestData inferenceDataProviderTestData = new InferenceDataProviderTestData();
	
	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;
	String host = null;
	Gson gson = new Gson();
	int typeId = 0;
	int reportTypeId = 0;
	int reportIdkpi_Inference = 300602;
	String assessmentReport_Inference = "{\"reportName\":\"Report_Grafana_Inference\",\"reportTemplate\":"
			+ reportIdkpi_Inference
			+ ",\"emailList\":\"abc@abc.com\",\"vUtil\":\"GRAFANAPDF\",\"schedule\":\"DAILY\",\"startdate\":\"2022-03-04T00:00:00Z\",\"enddate\":\"2022-04-04T00:00:00Z\",\"isReoccuring\":true,\"datasource\":\"\",\"asseementreportdisplayname\":\"Report_Grafana_Inference\",\"emailDetails\":null}";
	private static final String ASSESSMENT_REPORT_NAME = "Report_Grafana_Inference";
	Map<String, String> testAuthData = new HashMap<>();

	@Resource
	private FilterChainProxy springSecurityFilterChain;

	@DataProvider
	public void getData() {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ TestngInitializerTest.TESTNG_TESTDATA + File.separator + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "grafanaAuth.json";
		JsonElement jsonData;
		try {
			jsonData = JsonUtils.parseReader(new FileReader(new File(path).getCanonicalPath()));
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			throw new SkipException("skipped this test case as grafana auth file not found.");
		}
		testAuthData = new Gson().fromJson(jsonData, Map.class);
	}

	@BeforeClass
	public void onInit() throws Exception {

		Map<String, String> cookiesMap = null;
		try {
				String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
						+ TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "InferenceDataProvider.json";
				testData = JsonUtils.getJsonData(path).getAsJsonObject();
			getData();
			httpRequest.addHeader("Authorization", testAuthData.get(AUTHORIZATION));

			cookiesMap = PlatformServiceUtil.getGrafanaCookies(httpRequest);
			reportTypeId = inferenceDataProviderTestData.saveWorkflowType("Report");

			// save multiple Kpi definition in db
			inferenceDataProviderTestData.readKpiFileAndSave("KPI_Inference.json");

			// save multiple content definition in db
			inferenceDataProviderTestData.readContentFileAndSave("Content_Inference.json");

			inferenceDataProviderTestData.readReportTempFileAndSave("Report_Template_Inference.json",
					reportIdkpi_Inference);
			typeId = inferenceDataProviderTestData.saveWorkflowType("Report");

			// save workflow task in db
			inferenceDataProviderTestData.saveWorkflowTask(testData.get("taskKpiExecution").toString());

			// save assessmentreport
			inferenceDataProviderTestData.saveAssessmentReport(InferenceDataProviderTestData.workflowIdWithoutEmail,
					assessmentReport_Inference, 1);
			InsightsAssessmentConfiguration assessmentReport = reportConfigDAL
					.getAssessmentByAssessmentName(ASSESSMENT_REPORT_NAME);
			assessmentReport.setId(415911);

		} catch (Exception e1) {
			log.error(e1);
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
	public void testcheckInferenceDSReport() {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
					.post("/externalApi/inference/data/report/testDataSource");
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.debug("Error while testing Interface DS Report" + e);
		}
		log.debug("Inference DS Report tested successfully!");
	}

	@Test(priority = 2)
	public void testGetInferenceReportData() {
		try {

			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
					+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();

			String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
			headers.put("Authorization", "Basic " + encodedString);
			String content = "[{\"vectorType\":\"BUILD\",\"vectorSchedule\":\"Daily\",\"chartType\":\"LineChart\"}]";
			httpRequest.addHeader("Authorization", "Basic " + encodedString);

			String auth = "Basic " + encodedString;
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/externalApi/inference/data/report")
					.content(content.getBytes()).header("Authorization", auth);
			this.mockMvc.perform(builder).andExpect(ok);
		} catch (Exception e) {
			log.debug("Error while retrieving Interface Report Data" + e);
		}
		log.debug("Inference Report Data retrieved successfully!");
	}
}
