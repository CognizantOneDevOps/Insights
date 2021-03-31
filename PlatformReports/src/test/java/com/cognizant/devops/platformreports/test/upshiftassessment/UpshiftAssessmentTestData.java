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


package com.cognizant.devops.platformreports.test.upshiftassessment;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfig;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfigDAL;
import com.cognizant.devops.platformdal.workflow.*;
import com.cognizant.devops.platformreports.assessment.upshift.core.UpshiftAssessmentExecutionSubscriber;
import com.cognizant.devops.platformreports.assessment.upshift.core.UpshiftAssessmentRelationExecutionSubscriber;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowDataHandler;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class UpshiftAssessmentTestData {
    private static Logger log = LogManager.getLogger(UpshiftAssessmentTestData.class.getName());
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    WorkflowDAL workflowDAL = new WorkflowDAL();
    UpshiftAssessmentConfigDAL upshiftAssessmentConfigDAL = new UpshiftAssessmentConfigDAL();

    String createNodeWorkflow = "{\n" +
            "\"description\":\"UPSHIFTNODE_Execute\",\n" +
            "\"mqChannel\":\"WORKFLOW.TASK.UPSHIFTREPORT.EXCECUTION\",\n" +
            "\"componentName\":\"com.cognizant.devops.platformreports.assessment.upshift.core.UpshiftAssessmentExecutionSubscriber\",\n" +
            "\"dependency\":\"0\",\n" +
            "\"workflowType\":\"UPSHIFTASSESSMENT\"\n" +
            "}";
    String createRelationshipWorkflow = "{\n" +
            "\"description\":\"UPSHIFTRELATION_Execute\",\n" +
            "\"mqChannel\":\"WORKFLOW.TASK.UPSHIFTRELATION.EXCECUTION\",\n" +
            "\"componentName\":\"com.cognizant.devops.platformreports.assessment.upshift.core.UpshiftAssessmentRelationExecutionSubscriber\",\n" +
            "\"dependency\":\"1\",\n" +
            "\"workflowType\":\"UPSHIFTASSESSMENT\"\n" +
            "}\n";

    String mqChannelNodeCreation = "WORKFLOW.TASK.UPSHIFTREPORT.EXCECUTION";
    String mqChannelRelationCreation = "WORKFLOW.TASK.UPSHIFTRELATION.EXCECUTION";

    String workflowId;

    public void readUpshiftAssessmentFileAndSave(String fileName, String executionId) throws Exception {
        try {
            UpshiftAssessmentConfig upshiftAssessmentConfig = new UpshiftAssessmentConfig();
            int id = -1;
            //WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
            boolean runImmediate = true;
            boolean reoccurence = false;
            boolean isActive = true;
            String email = "dummy@test.com";
            //Path path = Paths.get(String.valueOf(classLoader.getResource(fileName)));
            File vsmReportFile = new File(classLoader.getResource(fileName).getFile());
            FileInputStream input = new FileInputStream(vsmReportFile);
            byte[] fileBytes = new byte[(int) vsmReportFile.length()];
            input.read(fileBytes);
            String fileString = new String(fileBytes, StandardCharsets.ISO_8859_1);
            Map<?, ?> map = new Gson().fromJson(fileString, Map.class);
            String upshiftUuid = map.get("upshiftUuid").toString();
            String workflowType = WorkflowTaskEnum.WorkflowType.UPSHIFTASSESSMENT.getValue();
            String schedule = WorkflowTaskEnum.WorkflowSchedule.ONETIME.name();
            String workflowStatus = WorkflowTaskEnum.UpshiftAssessmentStatus.NOT_STARTED.name();
            workflowId = workflowType + "_"
                    + InsightsUtils.getCurrentTimeInSeconds();
            JsonArray taskList = new JsonArray();
            JsonArray workflowList = getTaskList(workflowType);
            workflowList.forEach(task ->
                    taskList.add(createTaskJson(task.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt(),
                            task.getAsJsonObject().get("dependency").getAsInt()))
            );
            JsonObject emailDetailsJson = getEmailDetails("Insights VSM Report", email);
            InsightsWorkflowConfiguration workflowConfig = saveWorkflowConfig(workflowId, isActive,
                    reoccurence, schedule, workflowStatus, workflowType,
                    taskList, 0, emailDetailsJson, runImmediate);
            upshiftAssessmentConfig.setFileName(fileName);
            upshiftAssessmentConfig.setFile(fileBytes);
            upshiftAssessmentConfig.setEmail(email);
            upshiftAssessmentConfig.setUpshiftUuid(upshiftUuid);
            upshiftAssessmentConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
            upshiftAssessmentConfig.setStatus(workflowStatus);
            upshiftAssessmentConfig.setJenkinsExecId(executionId);
            upshiftAssessmentConfig.setWorkflowConfig(workflowConfig);
            id = upshiftAssessmentConfigDAL.saveUpshiftAssessment(upshiftAssessmentConfig);
            log.debug(id);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public int saveWorkflowTask(String task) {
        JsonObject taskJson = new JsonParser().parse(task).getAsJsonObject();
        InsightsWorkflowTask taskConfig = new InsightsWorkflowTask();
        int taskId = -1;
        try {
            InsightsWorkflowTask existingTask = workflowDAL
                    .getTaskbyTaskDescription(taskJson.get("description").getAsString());
            if (existingTask == null) {
                String description = taskJson.get("description").getAsString();
                String mqChannel = taskJson.get("mqChannel").getAsString();
                String componentName = taskJson.get("componentName").getAsString();
                int dependency = taskJson.get("dependency").getAsInt();
                String workflowType = taskJson.get("workflowType").getAsString();
                taskConfig.setDescription(description);
                taskConfig.setMqChannel(mqChannel);
                taskConfig.setCompnentName(componentName);
                taskConfig.setDependency(dependency);
                InsightsWorkflowType workflowTypeEntity = new InsightsWorkflowType();
                workflowTypeEntity.setWorkflowType(workflowType);
                taskConfig.setWorkflowType(workflowTypeEntity);
                taskId = workflowDAL.saveInsightsWorkflowTaskConfig(taskConfig);

            }
            log.debug("Task id: {}", taskId);
        } catch (Exception e) {
            log.error(e);
        }
        return taskId;
    }

    public int getTaskId(String mqChannel) {
        int taskId = -1;
        try {
            taskId = workflowDAL.getTaskId(mqChannel);
        } catch (Exception e) {
            log.error(e);
        }
        return taskId;

    }

    public void initializeTask() {
        try {
            Map<Integer, WorkflowTaskSubscriberHandler> registry = new HashMap<>(0);
            WorkflowTaskSubscriberHandler testNodeCreation = new UpshiftAssessmentExecutionSubscriber(mqChannelNodeCreation);
            registry.put(getTaskId(mqChannelNodeCreation), testNodeCreation);
            WorkflowTaskSubscriberHandler testRelationCreation = new UpshiftAssessmentRelationExecutionSubscriber(mqChannelRelationCreation);
            registry.put(getTaskId(mqChannelRelationCreation), testRelationCreation);
            WorkflowDataHandler.setRegistry(registry);

            Thread.sleep(1000);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private JsonArray getTaskList(String workflowType) throws InsightsCustomException {
        try {

            List<InsightsWorkflowTask> listofTasks = workflowDAL.getTaskLists(workflowType);
            JsonArray jsonarray = new JsonArray();
            for (InsightsWorkflowTask taskDetail : listofTasks) {
                JsonObject jsonobject = new JsonObject();
                jsonobject.addProperty(AssessmentReportAndWorkflowConstants.TASK_ID, taskDetail.getTaskId());
                jsonobject.addProperty("description", taskDetail.getDescription());
                jsonobject.addProperty("dependency", taskDetail.getDependency());
                jsonarray.add(jsonobject);
            }
            return jsonarray;

        } catch (Exception e) {
            log.error("Error while deleting assesment report", e);
            throw new InsightsCustomException(e.toString());
        }
    }

    private JsonObject createTaskJson(int taskId, int sequence) {
        JsonObject taskJson = new JsonObject();
        taskJson.addProperty(AssessmentReportAndWorkflowConstants.TASK_ID, taskId);
        taskJson.addProperty("sequence", sequence);
        return taskJson;
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

    private InsightsWorkflowConfiguration saveWorkflowConfig(String workflowId, boolean isActive, boolean reoccurence,
                                                             String schedule, String reportStatus, String workflowType, JsonArray taskList, long startdate,
                                                             JsonObject emailDetails, boolean runImmediate) throws InsightsCustomException {
        InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowByWorkflowId(workflowId);
        if (workflowConfig != null) {
            throw new InsightsCustomException("Workflow already exists for with assessment report id "
                    + workflowConfig.getAssessmentConfig().getId());
        }

        workflowConfig = new InsightsWorkflowConfiguration();
        workflowConfig.setWorkflowId(workflowId);
        workflowConfig.setActive(isActive);
        if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
            workflowConfig.setNextRun(0L);
        } else if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.BI_WEEKLY_SPRINT.toString())
                || schedule.equals(WorkflowTaskEnum.WorkflowSchedule.TRI_WEEKLY_SPRINT.toString())) {
            workflowConfig.setNextRun(InsightsUtils.getNextRunTime(startdate, schedule, true));
        } else {
            workflowConfig
                    .setNextRun(InsightsUtils.getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), schedule, true));
        }
        workflowConfig.setLastRun(0L);
        workflowConfig.setReoccurence(reoccurence);
        workflowConfig.setScheduleType(schedule);
        workflowConfig.setStatus(reportStatus);
        workflowConfig.setWorkflowType(workflowType);
        workflowConfig.setRunImmediate(runImmediate);
        Set<InsightsWorkflowTaskSequence> sequneceEntitySet = setSequence(taskList, workflowConfig);
        // Attach TaskSequence to workflow
        workflowConfig.setTaskSequenceEntity(sequneceEntitySet);
        if (emailDetails != null) {
            InsightsEmailTemplates emailTemplateConfig = createEmailTemplateObject(emailDetails, workflowConfig);
            workflowConfig.setEmailConfig(emailTemplateConfig);
        }
        return workflowConfig;

    }

    private Set<InsightsWorkflowTaskSequence> setSequence(JsonArray taskList,
                                                          InsightsWorkflowConfiguration workflowConfig) throws InsightsCustomException {
        Set<InsightsWorkflowTaskSequence> sequneceEntitySet = new HashSet<>();
        try {
            Set<InsightsWorkflowTaskSequence> taskSequenceSet = workflowConfig.getTaskSequenceEntity();
            if (!taskSequenceSet.isEmpty()) {
                workflowDAL.deleteWorkflowTaskSequence(workflowConfig.getWorkflowId());
            }
            ArrayList<Integer> sortedTask = new ArrayList<>();
            taskList.forEach(taskObj -> sortedTask.add(taskObj.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt()));
            @SuppressWarnings("unchecked")

            /*
             * make a clone of list as sortedTask list will be iterated so same list can not
             * used to get next element
             */
                    ArrayList<Integer> taskListClone = (ArrayList<Integer>) sortedTask.clone();

            int sequenceNo = 1;
            int nextTask = -1;

            ListIterator<Integer> listIterator = sortedTask.listIterator();
            while (listIterator.hasNext()) {

                int taskId = listIterator.next();
                int nextIndex = listIterator.nextIndex();
                if (nextIndex == taskListClone.size()) {
                    nextTask = -1;
                } else {
                    nextTask = taskListClone.get(nextIndex);
                }
                InsightsWorkflowTask taskEntity = workflowDAL.getTaskByTaskId(taskId);
                InsightsWorkflowTaskSequence taskSequenceEntity = new InsightsWorkflowTaskSequence();
                // Attach each task to sequence
                taskSequenceEntity.setWorkflowTaskEntity(taskEntity);
                taskSequenceEntity.setWorkflowConfig(workflowConfig);
                taskSequenceEntity.setSequence(sequenceNo);
                taskSequenceEntity.setNextTask(nextTask);
                sequneceEntitySet.add(taskSequenceEntity);
                sequenceNo++;

            }

            return sequneceEntitySet;
        } catch (Exception e) {
            throw new InsightsCustomException("Something went wrong while attaching task to workflow");
        }
    }

    public InsightsEmailTemplates createEmailTemplateObject(JsonObject emailDetails,
                                                            InsightsWorkflowConfiguration workflowConfig) {
        InsightsEmailTemplates emailTemplateConfig = workflowConfig.getEmailConfig();
        if (emailTemplateConfig == null) {
            emailTemplateConfig = new InsightsEmailTemplates();
        }
        String mailBody = emailDetails.get("mailBodyTemplate").getAsString();
        mailBody = mailBody.replace("#", "<").replace("~", ">");
        emailTemplateConfig.setMailFrom(emailDetails.get("senderEmailAddress").getAsString());
        if (!emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVEREMAILADDRESS).getAsString().isEmpty()) {
            emailTemplateConfig.setMailTo(emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVEREMAILADDRESS).getAsString());
        } else {
            emailTemplateConfig.setMailTo(null);
        }
        if (!emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVERCCEMAILADDRESS).getAsString().isEmpty()) {
            emailTemplateConfig.setMailCC(emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVERCCEMAILADDRESS).getAsString());
        } else {
            emailTemplateConfig.setMailCC(null);
        }
        if (!emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVERBCCEMAILADDRESS).getAsString().isEmpty()) {
            emailTemplateConfig.setMailBCC(emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVERBCCEMAILADDRESS).getAsString());
        } else {
            emailTemplateConfig.setMailBCC(null);
        }
        emailTemplateConfig.setSubject(emailDetails.get("mailSubject").getAsString());
        emailTemplateConfig.setMailBody(mailBody);
        emailTemplateConfig.setWorkflowConfig(workflowConfig);
        return emailTemplateConfig;
    }

}

