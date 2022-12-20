/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.TaskManagement;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
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
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.UnitTestConstant;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class TaskManagementServiceTest extends TaskManagementTestData  {

	private static final Logger log = LogManager.getLogger(TaskManagementServiceTest.class);
	MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	ResultMatcher ok = MockMvcResultMatchers.status().isOk();
	Map<String, String> testAuthData = new HashMap<>();

	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
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
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url).cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString)
				.content(content).contentType(MediaType.APPLICATION_JSON_VALUE);
		return builder;
	}
	
	private MockMvc getMacMvc() {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
		return builder.build();
	}
	
	private MockHttpServletRequestBuilder mockHttpServletRequestBuilderGet(String url) {
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url).cookie(httpRequest.getCookies())
				.header("Authorization", testAuthData.get(AUTHORIZATION)).header("Cookie", cookiesString);
		return builder;
	}
	
	@Test(priority = 1)
	public void testSaveTaskDefinitionRecordValidationError() throws Exception {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/admin/scheduletaskmanagement/saveOrEditTaskDefinition",saveTaskJsonValidation); 
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, actual.get("status").getAsString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} 
	}
	
	@Test(priority = 2)
	public void testSaveTaskDefinitionRecordInvalidSchedule() throws Exception {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/admin/scheduletaskmanagement/saveOrEditTaskDefinition",saveTaskJsonInvalidSchedule); 
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, actual.get("status").getAsString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} 
	}

	@Test(priority = 3)
	public void testSaveTaskDefinitionRecord() throws InsightsCustomException {
		try {
			log.debug(" cookies {} data {} " ,httpRequest.getCookies(),saveTaskJson);
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/admin/scheduletaskmanagement/saveOrEditTaskDefinition",saveTaskJson); 
			
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			log.debug(" webResponse  status  ============= {} ======  saveresponse {}  ",result.getResponse().getStatus(), saveresponse);
			JsonObject saveTaskConfigjson = JsonUtils.parseStringAsJsonObject(saveresponse);
			
			if (saveresponse.contains("status")) {
				MockHttpServletRequestBuilder builderAllTaskList  =  mockHttpServletRequestBuilderGet("/admin/scheduletaskmanagement/getAllTaskDetail"); 
				MvcResult resultAllTask =  this.mockMvc.perform(builderAllTaskList).andReturn();
				String allTaskResponse = new String(resultAllTask.getResponse().getContentAsByteArray());
				JsonObject taskConfigjson = JsonUtils.parseStringAsJsonObject(allTaskResponse);
				log.debug(" webResponse  status  ============= {} ====== {} ",resultAllTask.getResponse().getStatus(),taskConfigjson);
				List<JsonObject> taskList = gson.fromJson(taskConfigjson.get("data"),
						new TypeToken<List<JsonObject>>() {
						}.getType());
				Assert.assertEquals(200, result.getResponse().getStatus());
				Assert.assertTrue(taskList.stream().anyMatch(taskJson -> taskJson.get("componentName").getAsString()
						.equalsIgnoreCase(componentNameString)));
				Assert.assertTrue(saveTaskConfigjson.get("status").getAsString()
						.equalsIgnoreCase(PlatformServiceConstants.SUCCESS));
			} else {
				Assert.fail(" Save task configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testGetAllTaskList() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builderAllTaskList  =  mockHttpServletRequestBuilderGet("/admin/scheduletaskmanagement/getAllTaskDetail"); 
			MvcResult resultAllTask =  this.mockMvc.perform(builderAllTaskList).andReturn();
			String allTaskResponse = new String(resultAllTask.getResponse().getContentAsByteArray());
			JsonObject taskConfigjson = JsonUtils.parseStringAsJsonObject(allTaskResponse);
			Assert.assertNotNull(taskConfigjson);
			if (taskConfigjson.has("data")) {
				List<JsonObject> taskList = gson.fromJson(taskConfigjson.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				log.debug(" getServerConfigStatus retun  {} ",taskList.size());
				Assert.assertTrue(taskList.size() > 0);		
				Assert.assertEquals(200, resultAllTask.getResponse().getStatus());
			}else {
				Assert.fail(" No task configure in system ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 5)
	public void testEditTaskDefinitionRecord() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/admin/scheduletaskmanagement/saveOrEditTaskDefinition",editTaskJson); 
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String editresponse = new String(result.getResponse().getContentAsByteArray());
			log.debug(" webResponse  status  ============= {} ======  editresponse {}  ",result.getResponse().getStatus(), editresponse);
			JsonObject editTaskConfigjson = JsonUtils.parseStringAsJsonObject(editresponse);
			Assert.assertNotNull(editTaskConfigjson);
			if (editTaskConfigjson.has("status")) {
				MockHttpServletRequestBuilder builderAllTaskList  =  mockHttpServletRequestBuilderGet("/admin/scheduletaskmanagement/getAllTaskDetail"); 
				MvcResult resultAllTask =  this.mockMvc.perform(builderAllTaskList).andReturn();
				String allTaskResponse = new String(resultAllTask.getResponse().getContentAsByteArray());
				JsonObject taskConfigjson = JsonUtils.parseStringAsJsonObject(allTaskResponse);
				log.debug(" webResponse  status  ============= {} ====== {} ",resultAllTask.getResponse().getStatus(),taskConfigjson);
				List<JsonObject> taskList = gson.fromJson(taskConfigjson.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertTrue(taskList.stream().anyMatch(taskJson -> 
					(taskJson.get("componentName").getAsString().equalsIgnoreCase(componentNameString)&&
						taskJson.get("schedule").getAsString().equalsIgnoreCase("0 */12 * ? * *")
						)));
				Assert.assertTrue(editTaskConfigjson.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));		
			}else {
				Assert.fail(" Save task configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 6)
	public void testStatusUpdateWrongData() throws Exception {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/admin/scheduletaskmanagement/statusUpdateForTaskDefinition",statusUpdateInvalidData); 
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, actual.get("status").getAsString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} 
	}
		
	@Test(priority = 7)
	public void testStatusupdateTaskDefinitionRecord() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/admin/scheduletaskmanagement/statusUpdateForTaskDefinition",statusUpdate); 
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String editresponse = new String(result.getResponse().getContentAsByteArray());
			log.debug(" webResponse  status  ============= {} ======  editresponse {}  ",result.getResponse().getStatus(), editresponse);
			JsonObject statusUpdateTaskConfigjson = JsonUtils.parseStringAsJsonObject(editresponse);
			Assert.assertNotNull(statusUpdateTaskConfigjson);
			if (statusUpdateTaskConfigjson.has("status")) {
				MockHttpServletRequestBuilder builderAllTaskList  =  mockHttpServletRequestBuilderGet("/admin/scheduletaskmanagement/getAllTaskDetail"); 
				MvcResult resultAllTask =  this.mockMvc.perform(builderAllTaskList).andReturn();
				String allTaskResponse = new String(resultAllTask.getResponse().getContentAsByteArray());
				JsonObject taskConfigjson = JsonUtils.parseStringAsJsonObject(allTaskResponse);
				log.debug(" webResponse  status  ============= {} ====== {} ",resultAllTask.getResponse().getStatus(),taskConfigjson);
				List<JsonObject> taskList = gson.fromJson(taskConfigjson.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertTrue(taskList.stream().anyMatch(taskJson -> 
				(taskJson.get("componentName").getAsString().equalsIgnoreCase(componentNameString)&&
					taskJson.get("action").getAsString().equalsIgnoreCase("RESCHEDULE")
					)));				
				Assert.assertTrue(statusUpdateTaskConfigjson.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));		
			}else {
				Assert.fail(" Save task configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 8)
	public void testTaskHistoryDefinitionRecord() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/admin/scheduletaskmanagement/getTaskHistoryDetail",taskHistoryRequest); 
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String editresponse = new String(result.getResponse().getContentAsByteArray());
			log.debug(" webResponse  status  ============= {} ======  editresponse {}  ",result.getResponse().getStatus(), editresponse);
			JsonObject detailTaskHistoryConfigjson = JsonUtils.parseStringAsJsonObject(editresponse);
			Assert.assertNotNull(detailTaskHistoryConfigjson);
			if (detailTaskHistoryConfigjson.has("status")) {
				List<JsonObject> taskHistoryList = gson.fromJson(detailTaskHistoryConfigjson.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertArrayEquals(taskHistoryList.toArray(), new Object[0]);	
			}else {
				Assert.fail(" get task hsitory configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 9)
	public void testDeleteTaskDefinitionInvalidData() throws Exception {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost("/admin/scheduletaskmanagement/deleteTaskDefinition",deleteTaskJsonInvalidData); 
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String saveresponse = new String(result.getResponse().getContentAsByteArray());
			JsonObject actual = JsonUtils.parseStringAsJsonObject(saveresponse);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, actual.get("status").getAsString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} 
	}
	
	@Test(priority = 9)
	public void testDeleteTaskDefinitionRecord() throws InsightsCustomException {
		try {
			this.mockMvc = getMacMvc();
			MockHttpServletRequestBuilder builder = mockHttpServletRequestBuilderPost(
					"/admin/scheduletaskmanagement/deleteTaskDefinition",deleteTaskJson); 
			MvcResult result = this.mockMvc.perform(builder).andReturn();
			String editresponse = new String(result.getResponse().getContentAsByteArray());
			log.debug(" webResponse  status  ============= {} ======  editresponse {}  ",result.getResponse().getStatus(), editresponse);
			JsonObject deleteTaskStatus = JsonUtils.parseStringAsJsonObject(editresponse);
			Assert.assertNotNull(deleteTaskStatus);
			if (deleteTaskStatus.has("status")) {
				Assert.assertTrue(deleteTaskStatus.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));
				MockHttpServletRequestBuilder builderAllTaskList  =  mockHttpServletRequestBuilderGet("/admin/scheduletaskmanagement/getAllTaskDetail"); 
				MvcResult resultAllTask =  this.mockMvc.perform(builderAllTaskList).andReturn();
				String allTaskResponse = new String(resultAllTask.getResponse().getContentAsByteArray());
				JsonObject taskConfigjson = JsonUtils.parseStringAsJsonObject(allTaskResponse);
				log.debug(" webResponse  status  ============= {} ====== {} ",resultAllTask.getResponse().getStatus(),taskConfigjson);
				
				List<JsonObject> taskList = gson.fromJson(taskConfigjson.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertFalse(taskList.stream().anyMatch(taskJson -> taskJson.get("componentName").getAsString().equalsIgnoreCase(componentNameString)));
			}else {
				Assert.fail(" delete task configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}