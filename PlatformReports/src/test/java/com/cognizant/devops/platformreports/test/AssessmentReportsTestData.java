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
package com.cognizant.devops.platformreports.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportTemplateConfigFiles;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.core.PDFExecutionSubscriber;
import com.cognizant.devops.platformreports.assessment.core.ReportEmailSubscriber;
import com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber;
import com.cognizant.devops.platformreports.assessment.core.SystemNotificationDetailSubscriber;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowDataHandler;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AssessmentReportsTestData {
	private static Logger log = LogManager.getLogger(AssessmentReportsTestData.class.getName());

	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowDAL workflowDAL = new WorkflowDAL();
	
	int reportIdProdRT = 300600;
	int reportIdSonarRT = 300601;
	int reportIdkpiRT = 300602;
	int reportIdkpisRT = 300603;
	int workflowTypeId = 0;
	boolean deleteWorkflowType = false;
	String fusionExportUrl = null;
	String smtpHostServer = null;

	String taskKpiExecution = "{\"description\":\"TEST.REPORT_KPI_Execute\",\"mqChannel\":\"TEST.WORKFLOW.TASK.KPI.EXCECUTION\",\"componentName\":\"com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber\",\"dependency\":1,\"workflowType\":\"Report\"}";
	String taskPDFExecution = "{\"description\":\"TEST.REPORT_PDF_Execute\",\"mqChannel\":\"TEST.WORKFLOW.TASK.PDF.EXCECUTION\",\"componentName\":\"com.cognizant.devops.platformreports.assessment.core.PDFExecutionSubscriber\",\"dependency\":2,\"workflowType\":\"Report\"}";
	String taskEmailExecution = "{\"description\":\"TEST.REPORT_EMAIL_Execute\",\"mqChannel\":\"TEST.WORKFLOW.TASK.EMAIL.EXCECUTION\",\"componentName\":\"com.cognizant.devops.platformreports.assessment.core.ReportEmailSubscriber\",\"dependency\":3,\"workflowType\":\"Report\"}";
	String taskSystemHealthNotificationExecution = "{\"description\":\"TEST.SystemNotification_Execute\",\"mqChannel\":\"TEST.WORKFLOW.SYSTEM_TASK.SYSTEMNOTIFICATION.EXECUTION\",\"componentName\":\"com.cognizant.devops.platformreports.assessment.core.SystemNotificationDetailSubscriber\",\"dependency\":100,\"workflowType\":\"SYSTEM\"}";
	String taskSystemEmailNotificationExecution = "{\"description\":\"TEST.Email_Execute\",\"mqChannel\":\"TEST.WORKFLOW.SYSTEM_TASK.EMAIL.EXECUTION\",\"componentName\":\"com.cognizant.devops.platformreports.assessment.core.ReportEmailSubscriber\",\"dependency\":101,\"workflowType\":\"SYSTEM\"}";
	
	String reportTemplatekpi = "{\"reportName\":\"Testing_fail\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100127,\"visualizationConfigs\":[{\"vType\":\"mscolumn2d_100127\",\"vQuery\":\"MATCH (n:KPI:RESULTS) where n.reportId = 602 and n.kpiId=127 RETURN n.SPKendTime as SPKendTime , n.MaxBuildTime as MaxBuildTime LIMIT 5\"}]}]}";
	String reportTemplatekpis = "{\"reportName\":\"Testing_fail_queries\",\"description\":\"Testing_queries\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100161,\"visualizationConfigs\":[{\"vType\":\"100161_line\",\"vQuery\":\"\"}]},{\"kpiId\":100153,\"visualizationConfigs\":[{\"vType\":\"100153_line\",\"vQuery\":\"\"}]}]}";

	JsonObject reportTemplateJson = JsonUtils.parseStringAsJsonObject(reportTemplatekpi);
	JsonObject reportTemplateKpisJson = JsonUtils.parseStringAsJsonObject(reportTemplatekpis);

	String assessmentReportWithEmail = "{\"reportName\":\"report_Email_test10002154\",\"reportTemplate\":" + reportIdProdRT + ",\"emailList\":\"abc@abc.com\",\"schedule\":\"BI_WEEKLY_SPRINT\",\"startdate\":\"2020-05-12T00:00:00Z\",\"isReoccuring\":true,\"datasource\":\"\",\"emailDetails\": {\"senderEmailAddress\":\"abc@abc.com\",\"receiverEmailAddress\":\"abcd@abcd.com\",\"receiverCCEmailAddress\":\"sb@sb.com\",\"receiverBCCEmailAddress\":\"sb@sb.com\",\"mailSubject\":\"Sub_mail\",\"mailBodyTemplate\":\"sending a mail for report\"},\"asseementreportdisplayname\":\"Report_test\"}";
	String assessmentReportWithoutEmail = "{\"reportName\":\"report_test100021548\",\"reportTemplate\":" + reportIdProdRT + ",\"emailList\":\"abc@abc.com\",\"schedule\":\"BI_WEEKLY_SPRINT\",\"startdate\":\"2020-05-12T00:00:00Z\",\"isReoccuring\":true,\"datasource\":\"\",\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
	String assessmentReportFail = "{\"reportName\":\"report_test_Sonar100064032\",\"asseementreportdisplayname\":\"ReportWeek\",\"reportTemplate\":" + reportIdSonarRT + ",\"emailList\":\"abc@abc.com\",\"schedule\":\"QUARTERLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
	String assessmentReportWrongkpi = "{\"reportName\":\"report_test_10083556935\",\"asseementreportdisplayname\":\"ReportWeek\",\"reportTemplate\":" + reportIdkpiRT + ",\"emailList\":\"abc@abc.com\",\"schedule\":\"MONTHLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
	String assessmentReportWrongkpis = "{\"reportName\":\"report_test_10083563542\",\"asseementreportdisplayname\":\"ReportWeek\",\"reportTemplate\":" + reportIdkpisRT + ",\"emailList\":\"abc@abc.com\",\"schedule\":\"MONTHLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
	String assessmentReport = "{\"reportName\":\"report_test100021547\",\"reportTemplate\":" + reportIdProdRT + ",\"emailList\":\"abc@abc.com\",\"schedule\":\"BI_WEEKLY_SPRINT\",\"startdate\":\"2020-05-12T00:00:00Z\",\"isReoccuring\":true,\"datasource\":\"\",\"emailDetails\":null,\"asseementreportdisplayname\":\"Report_test\"}";

	String mqChannelKpiExecution = "TEST.WORKFLOW.TASK.KPI.EXCECUTION";
	String mqChannelPDFExecution = "TEST.WORKFLOW.TASK.PDF.EXCECUTION";
	String mqChannelEmailExecution = "TEST.WORKFLOW.TASK.EMAIL.EXCECUTION";
	String mqChannelSystemHealthNotificationExecution = "TEST.WORKFLOW.SYSTEM_TASK.SYSTEMNOTIFICATION.EXECUTION";
	String mqChannelSystemEmailExecution = "TEST.WORKFLOW.SYSTEM_TASK.EMAIL.EXECUTION";
	
	public static String workflowIdProd = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000567276";
	public static String workflowIdWithEmail = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000567999";
	public static String workflowIdWithoutEmail = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000568296";
	public static String workflowIdFail = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000640327";
	public static String workflowIdWrongkpi = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000835535";
	public static String workflowIdWrongkpis = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "1000083563542";
    public static String healthNotificationWorkflowId = WorkflowTaskEnum.WorkflowType.SYSTEM.getValue() + "_" + "HealthNotificationTest";
	public static long nextRunDaily;
	public static long nextRunBiWeekly;

	String querySonar = "MATCH (n:SONAR:DATA) where n.SPKstartTime > 1593561600 and n.SPKstartTime < 1596239999 and n.SPKstatus='Success' RETURN  COALESCE(Avg(toInt(n.SPKcomplexity)),0) as AvgComplexityCoverage";
	String queryJira = "MATCH (n:JIRA:DATA) WHERE n.SPKstartTime > 1593561600 and n.SPKstartTime < 1596239999 and n.SPKissueType='Bug' and n.SPKstatus='Closed'  RETURN count(n) as ClosedDefect";
	String queryJenkins = "MATCH (n:JENKINS:DATA) WHERE n.SPKstartTime > 1593561600 and n.SPKstartTime < 1596239999 and n.SPKvector = 'BUILD' and n.SPKstatus='Success' RETURN COALESCE(Max(toInt(n.SPKduration)),0) as MaxBuildTime";
	String queryJiraAvg = "MATCH (n:JIRA:DATA) WHERE n.SPKstartTime > 1593561600 and n.SPKstartTime < 1596239999 and n.SPKissueType='Bug' and n.SPKstatus='Closed'  RETURN COALESCE(Avg(toInt(n.SPKduration)),0) as AvgDefectCompletionTime";

	public static List<Integer> reportIdList = new ArrayList<>();
	public static List<Integer> taskidList = new ArrayList<>();
	public static List<Integer> contentIdList = new ArrayList<>();
	public static List<Integer> kpiIdList = new ArrayList<>();
	
	String[] templateDesignFilesArray = {"REPORT_SONAR_JENKINS_PROD.json", "REPORT_SONAR_JENKINS_PROD.html", "style.css", "image.webp"};

	public void readKpiFileAndSave(String fileName) throws Exception {
		try {
			File kpiFile = new File(classLoader.getResource(fileName).getFile());
			String kpiJson = new String(Files.readAllBytes(kpiFile.toPath()));
			JsonArray kpiArray = JsonUtils.parseStringAsJsonArray(kpiJson);
			for (JsonElement element : kpiArray) {
				int kpiId = saveKpiDefinition(element.getAsJsonObject());
				kpiIdList.add(kpiId);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void readContentFileAndSave(String fileName) throws Exception {
		try {
			File contentFile = new File(classLoader.getResource(fileName).getFile());
			String contentJson = new String(Files.readAllBytes(contentFile.toPath()));
			JsonArray contentArray = JsonUtils.parseStringAsJsonArray(contentJson);
			for (JsonElement element : contentArray) {
				int contentId = saveContentDefinition(element.getAsJsonObject());
				contentIdList.add(contentId);

			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public int readReportTempFileAndSave(String fileName, int reportID) throws IOException {
		int reportId = 0;
		try {
			File reportTempFile = new File(classLoader.getResource(fileName).getFile());
			String reportTempJson = new String(Files.readAllBytes(reportTempFile.toPath()));
			reportId = saveReportTemplate(reportTempJson, reportID);
			reportIdList.add(reportId);
		} catch (IOException e) {
			log.error(e);
		}
		return reportId;
	}

	public String readNeo4jData(String query) {
		log.debug(" query executed for Assessment report {} ", query);
		GraphDBHandler dbHandler = new GraphDBHandler();
		GraphResponse neo4jResponse;
		String finalJson = null;
		try {
			neo4jResponse = dbHandler.executeCypherQuery(query);
			log.debug(" Assessment report  neo4jResponse  {} ", neo4jResponse.getJson());
			JsonArray data = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray();
			if (data.size() > 0) {
				finalJson = data.get(0).getAsJsonObject().get("row").toString().replace("[", "").replace("]", "");
			}

		} catch (Exception e) {
			log.error(e);
			return finalJson;
		}
		// String [] StringArray= finalJson.split(",", 2);
		return finalJson;

	}

	public int saveKpiDefinition(JsonObject kpiJson) {
		InsightsKPIConfig kpiConfig = new InsightsKPIConfig();
		int kpiId = kpiJson.get("kpiId").getAsInt();
		try {
			InsightsKPIConfig existingConfig = reportConfigDAL.getKPIConfig(kpiId);
			if (existingConfig == null) {
				boolean isActive = kpiJson.get("isActive").getAsBoolean();
				String kpiName = kpiJson.get("name").getAsString();
				String dBQuery = kpiJson.get("DBQuery").getAsString();
				String resultField = kpiJson.get("resultField").getAsString();
				String group = kpiJson.get("group").getAsString();
				String toolName = kpiJson.get("toolName").getAsString();
				String dataSource = kpiJson.get("datasource").getAsString();
				String category = kpiJson.get("category").getAsString();
				String usecase = kpiJson.get("usecase").getAsString();
				String outputDatasource = kpiJson.get("outputDatasource").getAsString();
				kpiConfig.setKpiId(kpiId);
				kpiConfig.setActive(isActive);
				kpiConfig.setKpiName(kpiName);
				kpiConfig.setdBQuery(dBQuery);
				kpiConfig.setResultField(resultField);
				kpiConfig.setToolname(toolName);
				kpiConfig.setGroupName(group);
				kpiConfig.setDatasource(dataSource);
				kpiConfig.setCategory(category);
				kpiConfig.setOutputDatasource(outputDatasource);
				kpiConfig.setUsecase(usecase);
				reportConfigDAL.saveKpiConfig(kpiConfig);
			}
		} catch (Exception e) {
			log.error(e);
		}
		return kpiId;
	}

	public int saveContentDefinition(JsonObject contentJson) throws InsightsCustomException {
		InsightsContentConfig contentConfig = new InsightsContentConfig();
		Gson gson = new Gson();
		int kpiId = contentJson.get("kpiId").getAsInt();
		int contentId = contentJson.get("contentId").getAsInt();
		try {
			InsightsContentConfig existingContentConfig = reportConfigDAL.getContentConfig(contentId);
			if (existingContentConfig == null) {
				boolean contentisActive = contentJson.get("isActive").getAsBoolean();
				String contentName = contentJson.get("contentName").getAsString();
				String contentString = gson.toJson(contentJson);
				contentConfig.setContentId(contentId);
				InsightsKPIConfig kpiConfig = reportConfigDAL.getKPIConfig(kpiId);
				String contentCategory = kpiConfig.getCategory();
				contentConfig.setKpiConfig(kpiConfig);
				contentConfig.setActive(contentisActive);
				contentConfig.setContentJson(contentString);
				contentConfig.setContentName(contentName);
				contentConfig.setCategory(contentCategory);
				reportConfigDAL.saveContentConfig(contentConfig);
			}
		} catch (Exception e) {
			log.error(e);
		}
		return contentId;
	}

	public int saveWorkflowTask(String task) {
		JsonObject taskJson = JsonUtils.parseStringAsJsonObject(task);
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
			taskidList.add(taskId);
		} catch (Exception e) {
			log.error(e);
		}
		return taskId;
	}

	public int saveReportTemplate(String reportTemplate, int reportID) {
		int reportId = 0;
		JsonObject reportJson = JsonUtils.parseStringAsJsonObject(reportTemplate);
		Set<InsightsReportsKPIConfig> reportsKPIConfigSet = new LinkedHashSet<>();
		try {
			//reportId = (int) (System.currentTimeMillis() / 1000);
			reportId = reportID;
			String reportName = reportJson.get("reportName").getAsString();
			InsightsAssessmentReportTemplate reportEntity = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getActiveReportTemplateByName(reportName);
			if (reportEntity == null) {
				boolean isActive = reportJson.get("isActive").getAsBoolean();
				String description = reportJson.get("description").getAsString();
				String visualizationutil = reportJson.get("visualizationutil").getAsString();
				reportEntity = new InsightsAssessmentReportTemplate();
				reportEntity.setReportId(reportId);
				reportEntity.setActive(isActive);
				reportEntity.setDescription(description);
				reportEntity.setTemplateName(reportName);
				reportEntity.setFile(reportName);
				reportEntity.setVisualizationutil(visualizationutil);
				JsonArray kpiConfigArray = reportJson.get("kpiConfigs").getAsJsonArray();
				for (JsonElement eachKpiConfig : kpiConfigArray) {
					JsonObject KpiObject = eachKpiConfig.getAsJsonObject();
					int kpiId = KpiObject.get("kpiId").getAsInt();
					String vConfig = (KpiObject.get("visualizationConfigs").getAsJsonArray()).toString();
					InsightsKPIConfig kpiConfig = reportConfigDAL.getKPIConfig(kpiId);
					if (kpiConfig != null) {
						InsightsReportsKPIConfig reportsKPIConfig = new InsightsReportsKPIConfig();
						reportsKPIConfig.setKpiConfig(kpiConfig);
						reportsKPIConfig.setvConfig(vConfig);
						reportsKPIConfig.setReportTemplateEntity(reportEntity);
						reportsKPIConfigSet.add(reportsKPIConfig);
					}

				}
				reportEntity.setReportsKPIConfig(reportsKPIConfigSet);
				reportConfigDAL.saveReportConfig(reportEntity);
			}
		} catch (Exception e) {
			log.error(e);
		}
		return reportId;

	}

	public String saveAssessmentReport(String workflowid, String assessmentReport, int noOftask)
			throws InsightsCustomException {
		try {
			JsonObject emailDetails = null;
			JsonObject assessmentReportJson = addTask(assessmentReport, noOftask);
			int reportId = assessmentReportJson.get("reportTemplate").getAsInt();
			InsightsAssessmentReportTemplate reportTemplate = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getActiveReportTemplateByReportId(reportId);
			String workflowType = WorkflowTaskEnum.WorkflowType.REPORT.getValue();
			String reportName = assessmentReportJson.get("reportName").getAsString();
			boolean isActive = true;
			String schedule = assessmentReportJson.get("schedule").getAsString();
			String emailList = assessmentReportJson.get("emailList").getAsString();
			String datasource = assessmentReportJson.get("datasource").getAsString();
			boolean reoccurence = assessmentReportJson.get("isReoccuring").getAsBoolean();
			boolean runImmediate = Boolean.FALSE;
			String asseementreportdisplayname = assessmentReportJson.get("asseementreportdisplayname").getAsString();
			if (!assessmentReportJson.get("emailDetails").isJsonNull()) {
				emailDetails = assessmentReportJson.get("emailDetails").getAsJsonObject();
			}
			long epochStartDate = 0;
			long epochEndDate = 0;
			JsonElement startDateJsonObject = assessmentReportJson.get("startdate");
			if (startDateJsonObject.isJsonNull()) {
				epochStartDate = 0;
			} else {
				epochStartDate = InsightsUtils.getEpochTime(startDateJsonObject.getAsString()) / 1000;
				epochStartDate = InsightsUtils.getStartOfTheDay(epochStartDate) + 1;

			}
			String reportStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString();
			JsonArray taskList = assessmentReportJson.get("tasklist").getAsJsonArray();
//		workflowId = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_"
//				+ InsightsUtils.getCurrentTimeInSeconds();

			InsightsAssessmentConfiguration assessmentConfig = new InsightsAssessmentConfiguration();
			InsightsWorkflowConfiguration workflowConfig = saveWorkflowConfig(workflowid, isActive, reoccurence,
					schedule, reportStatus, workflowType, taskList, epochStartDate, emailDetails,
					runImmediate);
			assessmentConfig.setActive(isActive);
			assessmentConfig.setEmails(emailList);
			assessmentConfig.setInputDatasource(datasource);
			assessmentConfig.setAsseementreportname(reportName);
			assessmentConfig.setStartDate(epochStartDate);
			assessmentConfig.setEndDate(epochEndDate);
			assessmentConfig.setReportTemplateEntity(reportTemplate);
			assessmentConfig.setAsseementReportDisplayName(asseementreportdisplayname);
			assessmentConfig.setWorkflowConfig(workflowConfig);
			workflowConfig.setAssessmentConfig(assessmentConfig);
			reportConfigDAL.saveInsightsAssessmentConfig(assessmentConfig);
		} catch (Exception e) {
			log.error(e);
		}
		return workflowid;
	}

	public InsightsWorkflowConfiguration saveWorkflowConfig(String workflowId, boolean isActive, boolean reoccurence,
			String schedule, String reportStatus, String workflowType, JsonArray taskList, long startdate,
			JsonObject emailDetails, boolean runImmediate) throws InsightsCustomException {
		InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
		workflowConfig.setWorkflowId(workflowId);
		workflowConfig.setActive(isActive);
		try {
			if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
				workflowConfig.setNextRun(0L);
			} else if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.BI_WEEKLY_SPRINT.toString())
					|| schedule.equals(WorkflowTaskEnum.WorkflowSchedule.TRI_WEEKLY_SPRINT.toString())) {
				nextRunBiWeekly = InsightsUtils.getNextRunTime(startdate, schedule, true);
				workflowConfig.setNextRun(nextRunBiWeekly);
			} else {
				nextRunDaily = InsightsUtils.getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), schedule, true);
				workflowConfig.setNextRun(nextRunDaily);
			}
			// workflowConfig.setNextRun(nextRun);
			workflowConfig.setLastRun(0L);
			workflowConfig.setReoccurence(reoccurence);
			workflowConfig.setScheduleType(schedule);
			workflowConfig.setStatus(reportStatus);
			workflowConfig.setWorkflowType(workflowType);
			workflowConfig.setRunImmediate(runImmediate);
			Set<InsightsWorkflowTaskSequence> sequneceEntitySet = setSequence(taskList, workflowConfig);
			workflowConfig.setTaskSequenceEntity(sequneceEntitySet);
			if (emailDetails != null) {
				InsightsEmailTemplates emailTemplateConfig = createEmailTemplateObject(emailDetails, workflowConfig);
				workflowConfig.setEmailConfig(emailTemplateConfig);
			}
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return workflowConfig;
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
		if (!emailDetails.get("receiverEmailAddress").getAsString().isEmpty()) {
			emailTemplateConfig.setMailTo(emailDetails.get("receiverEmailAddress").getAsString());
		} else {
			emailTemplateConfig.setMailTo(null);
		}
		if (!emailDetails.get("receiverCCEmailAddress").getAsString().isEmpty()) {
			emailTemplateConfig.setMailCC(emailDetails.get("receiverCCEmailAddress").getAsString());
		} else {
			emailTemplateConfig.setMailCC(null);
		}
		if (!emailDetails.get("receiverBCCEmailAddress").getAsString().isEmpty()) {
			emailTemplateConfig.setMailBCC(emailDetails.get("receiverBCCEmailAddress").getAsString());
		} else {
			emailTemplateConfig.setMailBCC(null);
		}
		emailTemplateConfig.setSubject(emailDetails.get("mailSubject").getAsString());
		emailTemplateConfig.setMailBody(mailBody);
		emailTemplateConfig.setWorkflowConfig(workflowConfig);
		return emailTemplateConfig;
	}

	public Set<InsightsWorkflowTaskSequence> setSequence(JsonArray taskList,
			InsightsWorkflowConfiguration workflowConfig) throws InsightsCustomException {
		Set<InsightsWorkflowTaskSequence> sequneceEntitySet = new HashSet<>();
		try {
			Set<InsightsWorkflowTaskSequence> taskSequenceSet = workflowConfig.getTaskSequenceEntity();
			if (!taskSequenceSet.isEmpty()) {
				workflowDAL.deleteWorkflowTaskSequence(workflowConfig.getWorkflowId());
			}
			ArrayList<Integer> sortedTask = new ArrayList<>();
			taskList.forEach(taskObj -> sortedTask.add(taskObj.getAsJsonObject().get("taskId").getAsInt()));
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

	public JsonObject addTask(String assessmentReport, int noOftask) {
		JsonObject assessmentReportJson = JsonUtils.parseStringAsJsonObject(assessmentReport);
		List<Integer> taskIdList = new ArrayList<Integer>();
		taskIdList.add(getTaskId(mqChannelKpiExecution));
		taskIdList.add(getTaskId(mqChannelPDFExecution));
		taskIdList.add(getTaskId(mqChannelEmailExecution));
		JsonArray tasklist = new JsonArray();
		for (int i = 0; i < noOftask; i++) {
			JsonObject task = new JsonObject();
			task.addProperty("taskId", taskIdList.get(i));
			task.addProperty("sequence", i);
			tasklist.add(task);
		}
		assessmentReportJson.add("tasklist", tasklist);
		return assessmentReportJson;
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
			WorkflowTaskSubscriberHandler testKpisubscriberobject = new ReportKPISubscriber(mqChannelKpiExecution);
			registry.put(getTaskId(mqChannelKpiExecution), testKpisubscriberobject);
			WorkflowTaskSubscriberHandler testPDFsubscriberobject = new PDFExecutionSubscriber(mqChannelPDFExecution);
			registry.put(getTaskId(mqChannelPDFExecution), testPDFsubscriberobject);
			WorkflowTaskSubscriberHandler testEmailsubscriberobject = new ReportEmailSubscriber(
					mqChannelEmailExecution);
			registry.put(getTaskId(mqChannelEmailExecution), testEmailsubscriberobject);
			WorkflowTaskSubscriberHandler testSystemHealthNotificationsubscriberobject = new SystemNotificationDetailSubscriber(
					mqChannelSystemHealthNotificationExecution);
			registry.put(getTaskId(mqChannelSystemHealthNotificationExecution), testSystemHealthNotificationsubscriberobject);
			WorkflowTaskSubscriberHandler testSystemEmailsubscriberobject = new SystemNotificationDetailSubscriber(
					mqChannelSystemEmailExecution);
			registry.put(getTaskId(mqChannelSystemEmailExecution), testSystemEmailsubscriberobject);
			WorkflowDataHandler.setRegistry(registry);

			Thread.sleep(1000);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void deleteExecutionHistory(String workflowId) {
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL
				.getWorkflowExecutionHistoryByWorkflowId(workflowId);
		if (executionHistory.size() > 0) {
			for (InsightsWorkflowExecutionHistory eachExecutionRecord : executionHistory) {
				if (eachExecutionRecord.getWorkflowConfig().getWorkflowId().equalsIgnoreCase(workflowId)) {
					workflowDAL.deleteExecutionHistory(eachExecutionRecord.getId());
				}
			}
		}
	}

	public void delete(String workflowId) {
		try {
			deleteExecutionHistory(workflowId);
			InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			int id = workflowConfig.getAssessmentConfig().getId();
			workflowDAL.deleteEmailExecutionHistoryByWorkflowId(workflowConfig.getWorkflowId());
			workflowDAL.deleteWorkflowTaskSequence(workflowConfig.getWorkflowId());
			reportConfigDAL.deleteAssessmentReport(id);
			workflowDAL.deleteEmailTemplateByWorkflowId(workflowConfig.getWorkflowId());
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	public void deleteWorkflowConfig(String workflowId) {
		try {
			deleteExecutionHistory(workflowId);
			InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			workflowDAL.deleteEmailExecutionHistoryByWorkflowId(workflowConfig.getWorkflowId());
			workflowDAL.deleteWorkflowTaskSequence(workflowConfig.getWorkflowId());
			workflowDAL.deleteEmailTemplateByWorkflowId(workflowConfig.getWorkflowId());
			workflowDAL.deleteWorkflowConfig(workflowId);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void updateCorrectKpiQuery(int kpiId, String query) {
		try {
			InsightsKPIConfig existingConfig = reportConfigDAL.getKPIConfig(kpiId);
			existingConfig.setdBQuery(query);
			reportConfigDAL.updateKpiConfig(existingConfig);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	public int saveWorkflowType(String workflowtype) {
		int typeId = 0;
		try {
			InsightsWorkflowType workflowTypeObj = workflowDAL
					.getWorkflowType(workflowtype);
			if (workflowTypeObj == null) {
				InsightsWorkflowType type = new InsightsWorkflowType();
				type.setWorkflowType(workflowtype);
				typeId = workflowDAL.saveWorkflowType(type);
			} else {
				typeId = workflowTypeObj.getId();
			}
		} catch (Exception e) {
			log.error(e);
		}
		return typeId;
	}
	
	public void saveHealthNotificationWorkflowConfig() {
		try {
			Long epochStartDate = 0L;
			boolean isActive = true;
			String schedule = WorkflowTaskEnum.WorkflowSchedule.DAILY.toString();
			boolean reoccurence = true;
			boolean runImmediate = true;
			String reportStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString();
			String workflowType = WorkflowTaskEnum.WorkflowType.SYSTEM.toString();
			JsonArray taskList = new JsonArray();
			taskList.add(createTaskJson(getTaskId(mqChannelSystemHealthNotificationExecution), 0));
			taskList.add(createTaskJson(getTaskId(mqChannelSystemEmailExecution), 1));
			JsonObject emailDetails = getEmailDetails();
			InsightsWorkflowConfiguration saveWorkflowConfig = saveWorkflowConfig(healthNotificationWorkflowId,
					isActive, reoccurence, schedule, reportStatus, workflowType, taskList, epochStartDate, emailDetails,
					runImmediate);
			workflowDAL.saveInsightsWorkflowConfig(saveWorkflowConfig);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	public JsonObject createTaskJson(int taskId, int sequence) {
		JsonObject taskJson = new JsonObject();
		taskJson.addProperty("taskId", taskId);
		taskJson.addProperty("sequence", sequence);
		return taskJson;
	}

	public JsonObject getEmailDetails() {
		EmailConfiguration emailConfig = ApplicationConfigProvider.getInstance().getEmailConfiguration();
		JsonObject emailDetailsJson = new JsonObject();
		emailDetailsJson.addProperty("senderEmailAddress", emailConfig.getMailFrom());
		emailDetailsJson.addProperty("receiverEmailAddress", emailConfig.getSystemNotificationSubscriber());
		emailDetailsJson.addProperty("mailSubject", emailConfig.getSubject());
		emailDetailsJson.addProperty("mailBodyTemplate", "");
		emailDetailsJson.addProperty("receiverCCEmailAddress", "");
		emailDetailsJson.addProperty("receiverBCCEmailAddress", "");
		return emailDetailsJson;
	}
	
	public String uploadReportTemplateDesignFiles(int reportId) throws InsightsCustomException {
		String returnMessage = "";
		try {
			InsightsAssessmentReportTemplate reportEntity = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getReportTemplateByReportId(reportId);
			if (reportEntity == null) {
				throw new InsightsCustomException(" Report template not exists in database " + reportId);
			} else {

				for (String eachFile : templateDesignFilesArray) {
					String fileType = FilenameUtils.getExtension(eachFile).toUpperCase();
					File file = new File(classLoader.getResource("Report_SONAR_JENKINS_PROD/"+eachFile).getFile());
					InsightsReportTemplateConfigFiles templateFile = reportConfigDAL.getReportTemplateConfigFileByFileNameAndReportId(file.getName(),reportId);
					if(templateFile==null) {
					InsightsReportTemplateConfigFiles record = new InsightsReportTemplateConfigFiles();
					record.setFileName(file.getName());
					record.setFileData(FileUtils.readFileToByteArray(file));
					record.setFileType(fileType);
					record.setReportId(reportId);
					reportConfigDAL.saveReportTemplateConfigFiles(record);
					}
					else {
						templateFile.setFileData(FileUtils.readFileToByteArray(file));
						reportConfigDAL.updateReportTemplateConfigFiles(templateFile);						
					}
					
				}
				returnMessage = "File uploaded";
			}
		} catch (Exception ex) {
			log.error("Error in Report Template files upload {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		} 
		return returnMessage;
	}
	
	
}
