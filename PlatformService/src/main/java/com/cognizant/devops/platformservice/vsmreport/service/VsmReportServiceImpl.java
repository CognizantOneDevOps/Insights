/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

package com.cognizant.devops.platformservice.vsmreport.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.vsmReport.VsmReportConfig;
import com.cognizant.devops.platformdal.vsmReport.VsmReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service("vsmReportService")
public class VsmReportServiceImpl implements VsmReportService {
    private static final Logger log = LogManager.getLogger(VsmReportServiceImpl.class);
    
    VsmReportConfigDAL vsmReportConfigDAL = new VsmReportConfigDAL();

    @Override
    public void saveVsmReport(String uuid, String fileName, MultipartFile file, String email) throws InsightsCustomException {
        try {
            int id = -1;
            WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
            boolean runImmediate = true;
            boolean reoccurence = false;
            boolean isActive = true;
            String workflowType = WorkflowTaskEnum.WorkflowType.VSMREPORT.getValue();
            String schedule = WorkflowTaskEnum.WorkflowSchedule.ONETIME.name();
            String workflowStatus = WorkflowTaskEnum.VsmReportStatus.NOT_STARTED.name();
            String workflowId = workflowType + "_"
                    + InsightsUtils.getCurrentTimeInSeconds();
            JsonArray taskList = new JsonArray();
            JsonArray workflowList = workflowService.getTaskList(workflowType);
            workflowList.forEach(task ->
                    taskList.add(workflowService.createTaskJson(task.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt(),
                            task.getAsJsonObject().get("dependency").getAsInt()))
            );
            JsonObject emailDetailsJson = getEmailDetails("Insights VSM Report", email);
            InsightsWorkflowConfiguration workflowConfig = workflowService.saveWorkflowConfig(workflowId, isActive,
                    reoccurence, schedule, workflowStatus, workflowType,
                    taskList, 0, emailDetailsJson, runImmediate);

            VsmReportConfig vsmReportConfig = new VsmReportConfig();
            byte[] fileBytes;
            fileBytes = file.getBytes();
            vsmReportConfig.setFileName(fileName);
            vsmReportConfig.setFile(fileBytes);
            vsmReportConfig.setEmail(email);
            vsmReportConfig.setUuid(uuid);
            vsmReportConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
            vsmReportConfig.setStatus(workflowStatus);
            vsmReportConfig.setWorkflowConfig(workflowConfig);
            id = vsmReportConfigDAL.saveVsmReport(vsmReportConfig);
            log.debug(id);
        } catch (Exception e) {
            throw new InsightsCustomException("Unable to save file");
        }
    }


    private JsonObject getEmailDetails(String subject, String email) {
        EmailConfiguration emailConfig = ApplicationConfigProvider.getInstance().getEmailConfiguration();
        JsonObject emailDetailsJson = new JsonObject();
        emailDetailsJson.addProperty("senderEmailAddress", emailConfig.getMailFrom());
        emailDetailsJson.addProperty("receiverEmailAddress", email);
        emailDetailsJson.addProperty("mailSubject", subject);
        emailDetailsJson.addProperty("mailBodyTemplate", "");
        emailDetailsJson.addProperty("receiverCCEmailAddress", "");
        emailDetailsJson.addProperty("receiverBCCEmailAddress", "");
        return emailDetailsJson;
    }
}
