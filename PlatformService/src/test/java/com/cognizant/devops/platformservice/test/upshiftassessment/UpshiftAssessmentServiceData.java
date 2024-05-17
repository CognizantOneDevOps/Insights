/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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

package com.cognizant.devops.platformservice.test.upshiftassessment;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

public class UpshiftAssessmentServiceData extends AbstractTestNGSpringContextTests {
	private static final Logger log = LogManager.getLogger(UpshiftAssessmentServiceData.class);
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	UpshiftAssessmentConfigDAL upshiftAssessmentConfigDAL = new UpshiftAssessmentConfigDAL();
	@Autowired
	WorkflowServiceImpl workflowService;// = new WorkflowServiceImpl();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	JsonObject testData = new JsonObject();

	int taskID = 0;
	int relationTaskID = 0;
	MultipartFile testFile, testFile1;

	void prepareAssessmentData() throws InsightsCustomException {
		try {
			String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
					+ TestngInitializerTest.TESTNG_TESTDATA + File.separator
					+ TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "UpshiftAssementService.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();

		} catch (Exception e) {
			log.error(e);
		}
		try {
			InsightsWorkflowType type = new InsightsWorkflowType();
			type.setWorkflowType(WorkflowTaskEnum.WorkflowType.UPSHIFTASSESSMENT.getValue());
			workflowConfigDAL.saveWorkflowType(type);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest workflowtype record ", e);
		}

		try {
			String workflowTaskTest = testData.get("workflowTaskTest1").toString();
			JsonObject workflowTaskJson = convertStringIntoJson(workflowTaskTest);
			int response = workflowService.saveWorkflowTask(workflowTaskJson);
			InsightsWorkflowTask tasks = workflowConfigDAL
					.getTaskbyTaskDescription(workflowTaskJson.get("description").getAsString());
			taskID = tasks.getTaskId();
		} catch (Exception e) {
			log.error("Error preparing UpshiftAssessmentServiceData task ", e);
		}

		try {
			String workflowTaskTest = testData.get("workflowTaskTest2").toString();
			JsonObject workflowTaskJson = convertStringIntoJson(workflowTaskTest);
			int response = workflowService.saveWorkflowTask(workflowTaskJson);
			InsightsWorkflowTask tasks = workflowConfigDAL
					.getTaskbyTaskDescription(workflowTaskJson.get("description").getAsString());
			relationTaskID = tasks.getTaskId();
		} catch (Exception e) {
			log.error("Error preparing UpshiftReportServiceData KPI task ", e);
		}
		File upshiftReportFile = new File(classLoader.getResource("UpshiftAssessment.json").getFile());
		try (FileInputStream input = new FileInputStream(upshiftReportFile);) {
			testFile = new MockMultipartFile("file", upshiftReportFile.getName(), "text/plain",
					IOUtils.toByteArray(input));
		} catch (Exception e) {
			log.error("Error reading test upshift Report ", e);
		}
		File upshiftReportFileXML = new File(classLoader.getResource("UpshiftAssessmentTest.xml").getFile());
		try (FileInputStream input = new FileInputStream(upshiftReportFileXML)) {
			testFile1 = new MockMultipartFile("file", upshiftReportFileXML.getName(), "text/plain",
					IOUtils.toByteArray(input));
		} catch (Exception e) {
			log.error("Error reading test upshift Report ", e);
		}
	}

	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = JsonUtils.parseStringAsJsonObject(convertregisterkpi);
		return objectJson;
	}
}
