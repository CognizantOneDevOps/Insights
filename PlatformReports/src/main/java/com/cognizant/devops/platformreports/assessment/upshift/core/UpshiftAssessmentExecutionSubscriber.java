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

package com.cognizant.devops.platformreports.assessment.upshift.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.dataprocess.BaseDataProcessor;
import com.cognizant.devops.platformreports.assessment.upshift.handler.ReportDataProcessHandlerFactory;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UpshiftAssessmentExecutionSubscriber extends WorkflowTaskSubscriberHandler {

    private static Logger log = LogManager.getLogger(UpshiftAssessmentExecutionSubscriber.class);
    InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
    InsightsAssessmentConfigurationDTO assessmentReportDTO = null;
    private WorkflowDAL workflowDAL = new WorkflowDAL();

    public UpshiftAssessmentExecutionSubscriber(String routingKey) throws IOException, TimeoutException, InsightsCustomException {
        super(routingKey);
    }

    @Override
    public void handleTaskExecution(byte[] body) throws IOException {
        try {
        	long startTime = System.nanoTime();
            String incomingTaskMessage = new String(body, StandardCharsets.UTF_8);
            log.debug("Worlflow Detail ==== UpshiftAssessmentExecutionSubscriber started ... "
                    + "routing key  message handleDelivery ===== {} ", incomingTaskMessage);

            JsonObject incomingTaskMessageJson = new JsonParser().parse(incomingTaskMessage).getAsJsonObject();
            String workflowId = incomingTaskMessageJson.get("workflowId").getAsString();
            long executionId = incomingTaskMessageJson.get("executionId").getAsLong();
            workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
            log.debug("Worlflow Detail ==== scheduleType {} ", workflowConfig.getScheduleType());

            assessmentReportDTO = new InsightsAssessmentConfigurationDTO();
            assessmentReportDTO.setIncomingTaskMessageJson(incomingTaskMessage);
            assessmentReportDTO.setAsseementreportname(WorkflowTaskEnum.WorkflowType.UPSHIFTASSESSMENT.getValue());
            assessmentReportDTO.setExecutionId(executionId);
            assessmentReportDTO.setWorkflowId(workflowId);
            BaseDataProcessor chartHandler = ReportDataProcessHandlerFactory
                    .getDataHandler("UPSHIFTASSESSMENT");
            chartHandler.processJson(assessmentReportDTO);
            long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}"
            		,executionId,workflowId,assessmentReportDTO.getReportId(),workflowConfig.getWorkflowType(),"-","-",processingTime,
            		"Schedule :" +workflowConfig.getScheduleType() + "ReportName :" + assessmentReportDTO.getReportName() +
            		"UpshiftAssessmentExecutionSubscriber Block");
        } catch (Exception e) {
            log.error("Worlflow Detail ==== GrafanaPDFExecutionSubscriber Completed with error ", e);
            log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}"
            		,assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getReportId(),workflowConfig.getWorkflowType(),"-","-",0,
            		"Schedule :" +workflowConfig.getScheduleType() + "ReportName :" + assessmentReportDTO.getReportName() +
            		"GrafanaPDFExecutionSubscriber Completed with error" +e.getMessage());
            throw new InsightsJobFailedException(e.getMessage());
        }
    }
}
