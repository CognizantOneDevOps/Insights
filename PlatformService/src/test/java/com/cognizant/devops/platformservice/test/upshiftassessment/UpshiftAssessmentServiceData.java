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

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

public class UpshiftAssessmentServiceData {
    private static final Logger log = LogManager.getLogger(UpshiftAssessmentServiceData.class);
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    JsonParser parser = new JsonParser();
    UpshiftAssessmentConfigDAL upshiftAssessmentConfigDAL = new UpshiftAssessmentConfigDAL();
    WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
    WorkflowDAL workflowConfigDAL = new WorkflowDAL();

    int taskID = 0;
    int relationTaskID = 0;
    MultipartFile testFile;

    void prepareAssessmentData() throws InsightsCustomException {
        try {
            InsightsWorkflowType type = new InsightsWorkflowType();
            type.setWorkflowType(WorkflowTaskEnum.WorkflowType.UPSHIFTASSESSMENT.getValue());
            workflowConfigDAL.saveWorkflowType(type);
        } catch (Exception e) {
            log.error("Error preparing data at WorkflowServiceTest workflowtype record ", e);
        }

        try {
            String workflowTaskTest = "{\n" +
                    "\"description\":\"UPSHIFTNODE_Execute\",\n" +
                    "\"mqChannel\":\"WORKFLOW.TASK.UPSHIFTREPORT.EXCECUTION\",\n" +
                    "\"componentName\":\"com.cognizant.devops.platformreports.assessment.upshift.core.UpshiftAssessmentExecutionSubscriber\",\n" +
                    "\"dependency\":\"0\",\n" +
                    "\"workflowType\":\"UPSHIFTASSESSMENT\"\n" +
                    "}";
            JsonObject workflowTaskJson = convertStringIntoJson(workflowTaskTest);
            int response = workflowService.saveWorkflowTask(workflowTaskJson);
            InsightsWorkflowTask tasks = workflowConfigDAL
                    .getTaskbyTaskDescription(workflowTaskJson.get("description").getAsString());
            taskID = tasks.getTaskId();
        } catch (Exception e) {
            log.error("Error preparing UpshiftAssessmentServiceData task ", e);
        }

        try {
            String workflowTaskTest = "{\n" +
                    "\"description\":\"UPSHIFTRELATION_Execute\",\n" +
                    "\"mqChannel\":\"WORKFLOW.TASK.UPSHIFTRELATION.EXCECUTION\",\n" +
                    "\"componentName\":\"com.cognizant.devops.platformreports.assessment.upshift.core.UpshiftAssessmentRelationExecutionSubscriber\",\n" +
                    "\"dependency\":\"1\",\n" +
                    "\"workflowType\":\"UPSHIFTASSESSMENT\"\n" +
                    "}\n";
            JsonObject workflowTaskJson = convertStringIntoJson(workflowTaskTest);
            int response = workflowService.saveWorkflowTask(workflowTaskJson);
            InsightsWorkflowTask tasks = workflowConfigDAL
                    .getTaskbyTaskDescription(workflowTaskJson.get("description").getAsString());
            relationTaskID = tasks.getTaskId();
        } catch (Exception e) {
            log.error("Error preparing UpshiftReportServiceData KPI task ", e);
        }
        try {
            File upshiftReportFile = new File(classLoader.getResource("UpshiftAssessment.json").getFile());
            FileInputStream input = new FileInputStream(upshiftReportFile);
            testFile = new MockMultipartFile("file",
                    upshiftReportFile.getName(), "text/plain", IOUtils.toByteArray(input));
        }catch (Exception e){
            log.error("Error reading test upshift Report ", e);
        }
    }

    public JsonObject convertStringIntoJson(String convertregisterkpi) {
        JsonObject objectJson = new JsonObject();
        objectJson = parser.parse(convertregisterkpi).getAsJsonObject();
        return objectJson;
    }
}
