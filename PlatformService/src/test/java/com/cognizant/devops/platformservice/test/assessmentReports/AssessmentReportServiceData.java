/*******************************************************************************
 * Copyright 20L17 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0L (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0L
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.assessmentReports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AssessmentReportServiceData {
	private static final Logger log = LogManager.getLogger(AssessmentReportServiceData.class);
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	JsonParser parser = new JsonParser();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	File kpiFile = new File(classLoader.getResource("KPIDefination.json").getFile());
	File configFile = new File(classLoader.getResource("ContentsConfiguration.json").getFile());
	File kpiFileTxt = new File(classLoader.getResource("KPIDefination.txt").getFile());
	File configFileTxt = new File(classLoader.getResource("ContentsConfiguration.txt").getFile());
	File emptyFile = new File(classLoader.getResource("EmptyFile.json").getFile());
	// JsonObject workflowTaskJson = null;
	InsightsWorkflowTask tasks = null;
	// int taskID =0L;
	JsonObject incorrectregisterkpiJson = null;
	JsonObject registerContentJson = null;
	JsonObject incorrectregisterContentJson = null;
	JsonObject registerkpiJson = null;
	JsonObject contentWithoutKpiJson = null;
	JsonObject reportTemplateJson = null;
	JsonObject reportTemplateWithoutKPIsJson = null;
	JsonObject reportTemplateWithoutExistingKPIDsJson = null;
	JsonObject incorrectReportTemplateJson = null;

	Long dailyExpectedAssessmentStartDate = 0L;
	Long dailyExpectedAssessmentEndDate = 0L;
	Long dailyExpectedNextRun = 0L;
	JsonObject dailyAssessmentReportJson = null;

	Long weeklyExpectedAssessmentStartDate = 0L;
	Long weeklyExpectedAssessmentEndDate = 0L;
	Long weeklyExpectedNextRun = 0L;
	JsonObject weeklyAssessmentReportJson = null;

	Long monthlyExpectedAssessmentStartDate = 0L;
	Long monthlyExpectedAssessmentEndDate = 0L;
	Long monthlyExpectedNextRun = 0L;
	JsonObject monthlyAssessmentReportJson = null;

	Long quarterlyExpectedAssessmentStartDate = 0L;
	Long quarterlyExpectedAssessmentEndDate = 0L;
	Long quarterlyExpectedNextRun = 0L;
	JsonObject quarterlyAssessmentReportJson = null;

	Long yearlyExpectedAssessmentStartDate = 0L;
	Long yearlyExpectedAssessmentEndDate = 0L;
	Long yearlyExpectedNextRun = 0L;
	JsonObject yearlyAssessmentReportJson = null;

	Long oneTimeExpectedAssessmentStartDate = 0L;
	Long oneTimeExpectedAssessmentEndDate = 0L;
	Long oneTimeExpectedNextRun = 0L;
	JsonObject oneTimeAssessmentReportJson = null;

	Long biWeeklyExpectedAssessmentStartDate = 0L;
	Long biWeeklyExpectedAssessmentEndDate = 0L;
	Long biWeeklyExpectedNextRun = 0L;
	JsonObject biWeeklyAssessmentReportJson = null;

	Long triWeeklyExpectedAssessmentStartDate = 0L;
	Long triWeeklyExpectedAssessmentEndDate = 0L;
	Long triWeeklyExpectedNextRun = 0L;
	JsonObject triWeeklyAssessmentReportJson = null;

	Long triWeeklyExpectedAssessmentStartDateWithDataSource = 0L;
	Long triWeeklyExpectedAssessmentEndDateWithDataSource = 0L;
	Long triWeeklyExpectedNextRunWithDataSource = 0L;
	JsonObject triWeeklyAssessmentWithDataSourceReportJson = null;

	Long dailyEmailExpectedAssessmentStartDate = 0L;
	Long dailyEmailExpectedAssessmentEndDate = 0L;
	Long dailyEmailExpectedNextRun = 0L;
	JsonObject dailyEmailAssessmentReportJson = null;

	Long dailyEmailCCExpectedAssessmentStartDate = 0L;
	Long dailyEmailCCExpectedAssessmentEndDate = 0L;
	Long dailyEmailCCExpectedNextRun = 0L;
	JsonObject dailyEmailCCAssessmentReportJson = null;

	Long dailyEmailBCCExpectedAssessmentStartDate = 0L;
	Long dailyEmailBCCExpectedAssessmentEndDate = 0L;
	Long dailyEmailBCCExpectedNextRun = 0L;
	JsonObject dailyEmailBCCAssessmentReportJson = null;

	Long dailywithoutEmailExpectedAssessmentStartDate = 0L;
	Long dailywithoutEmailExpectedAssessmentEndDate = 0L;
	Long dailywithoutEmailExpectedNextRun = 0L;
	JsonObject dailywithoutEmailAssessmentReportJson = null;

	JsonObject incorrectAssessmentReportJson = null;
	JsonObject updateAssessmentReportIncorrectJson = null;
	JsonObject oneTimeAssessmentReportWithStartDateGreaterThanEndDateJson = null;
	JsonObject dailywithoutEmaildetailsAssessmentReportJson = null;
	JsonObject dailywithoutEmailtaskAssessmentReportJson = null;
	JsonObject dailyRestartAssessmentReport = null;
	JsonObject dailyRunImmediateAssessmentReport = null;

	String deleteAssessmentReportWrongConfigId = "0L";
	String reportIdForList = "610L";

	public static List<Integer> contentIdList = new ArrayList<Integer>();
	public static List<Integer> kpiIdList = new ArrayList<Integer>();

	void prepareAssessmentData() throws InsightsCustomException {
		int taskID = 0;
		int pdftaskID = 0;
		int emailtaskID = 0;
		try {
			String workflowTaskTest = "{\"description\": \"KPI_Execute_service_test\",\"mqChannel\": \"WORKFLOW.ASSESSMENTSERVICE_TEST.TASK.KPI.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber\",\"dependency\": \"1\",\"workflowType\": \"Report\"}";
			JsonObject workflowTaskJson = convertStringIntoJson(workflowTaskTest);
			int response = workflowService.saveWorkflowTask(workflowTaskJson);
			InsightsWorkflowTask tasks = workflowConfigDAL
					.getTaskbyTaskDescription(workflowTaskJson.get("description").getAsString());
			taskID = tasks.getTaskId();
		} catch (Exception e) {
			log.error("Error preparing AssessmentReportServiceData KPI task ", e);
		}

		try {
			String pdfworkflowTaskTest = "{\"description\": \"PDF_Execute_service_test\",\"mqChannel\": \"WORKFLOW.ASSESSMENTSERVICE_TEST.TASK.PDF.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.PDFExecutionSubscriber\",\"dependency\": \"2\",\"workflowType\": \"Report\"}";
			JsonObject pdfworkflowTaskTestJson = convertStringIntoJson(pdfworkflowTaskTest);
			int pdfresponse = workflowService.saveWorkflowTask(pdfworkflowTaskTestJson);
			InsightsWorkflowTask pdftasks = workflowConfigDAL
					.getTaskbyTaskDescription(pdfworkflowTaskTestJson.get("description").getAsString());
			pdftaskID = pdftasks.getTaskId();
		} catch (Exception e) {
			log.error("Error preparing AssessmentReportServiceData PDF task ", e);
		}

		try {
			String emailworkflowTaskTest = "{\"description\": \"Email_Execute_service_test\",\"mqChannel\": \"WORKFLOW.ASSESSMENTSERVICE_TEST.TASK.EMAIL.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportEmailSubscriber\",\"dependency\": \"3\",\"workflowType\": \"Report\"}";
			JsonObject emailworkflowTaskTestJson = convertStringIntoJson(emailworkflowTaskTest);
			int emailresponse = workflowService.saveWorkflowTask(emailworkflowTaskTestJson);
			InsightsWorkflowTask emailtasks = workflowConfigDAL
					.getTaskbyTaskDescription(emailworkflowTaskTestJson.get("description").getAsString());
			emailtaskID = emailtasks.getTaskId();
		} catch (Exception e) {
			log.error("Error preparing AssessmentReportServiceData Email task ", e);
		}

		String registerkpi = "{\"kpiId\":10L0L20L1,\"name\":\"Total Successful Deployments\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\"}";
		registerkpiJson = convertStringIntoJson(registerkpi);

		String incorrectRegisterkpi = "{\"kpiId\":10L0L1,\"name\":\"Avg all employee productivity for threshold \",\"schedule\":\"DAILY\",\"toolName\":\"PRODUCTIVITY\",\"group\":\"PRODUCTIVITY\",\"lastRunTime\":\"1586284260L0L0L0L\",\"neo4jQuery\":\"MATCH (n:PRODUCTIVITY) where n.completionDateEpochTime > {startTime} AND n.completionDateEpochTime < {endTime} WITH  avg(n.storyPoints*8) as StoryPoint, avg(n.authorTimeSpent) as authorTimeSpent  return   StoryPoint, authorTimeSpent, round((toFloat(StoryPoint)/authorTimeSpent)*10L0L) as Productivity\",\"resultField\":\"Productivity\"}";
		incorrectregisterkpiJson = convertStringIntoJson(incorrectRegisterkpi);

		String registerContent = "{\"contentId\":10L541,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments\",\"kpiId\":\"10L0L20L1\",\"noOfResult\":2,\"threshold\":0L,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
		registerContentJson = convertStringIntoJson(registerContent);

		String incorrectContent = "{\"expectedTrend\":\"DOWNWARDS\",\"contentName\":\"Average Build Time\",\"kpiId\":\"1110L\",\"noOfResult\":2,\"threshold\":0L,\"message\":{\"positive\":\"Average Build Time has decreased to {current:avgOutput}s from {previous:avgOutput}s \",\"negative\":\"Average Build Time has increased to {current:avgOutput}s from {previous:avgOutput}s \",\"neutral\":\"Average Build Time has remained constant to {avgOutput}s\"},\"isActive\":\"TRUE\"}";
		incorrectregisterContentJson = convertStringIntoJson(incorrectContent);

		String contentWithoutKpi = "{\"contentId\":10L,\"expectedTrend\":\"DOWNWARDS\",\"contentName\":\"Average Build Time\",\"category\":\"COMPARISON\",\"kpiId\":\"11\",\"noOfResult\":2,\"threshold\":0L,\"message\":{\"positive\":\"Average Build Time has decreased to {current:avgOutput}s from {previous:avgOutput}s \",\"negative\":\"Average Build Time has increased to {current:avgOutput}s from {previous:avgOutput}s \",\"neutral\":\"Average Build Time has remained constant to {avgOutput}s\"},\"isActive\":\"TRUE\"}";
		contentWithoutKpiJson = convertStringIntoJson(contentWithoutKpi);

		String reportTemplate = "{\"reportId\":\"610L\",\"reportName\":\"Fail Report_test\",\"description\":\"Testing\",\"isActive\":true,\"file\":\"File.json\",\"visualizationutil\":\"Fusion\",\"kpiConfigs\":[{\"kpiId\":10L0L20L1,\"visualizationConfigs\":[{\"vId\":\"10L0L\",\"vQuery\":\"Query\"}]}]}";
		reportTemplateJson = convertStringIntoJson(reportTemplate);

		String reportTemplateWithoutKPIDs = "{\"reportId\":\"60L0L20L\",\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"file\": \"File1.json\",\"visualizationutil\":\"Fusion\",\"isActive\":true}";
		reportTemplateWithoutKPIsJson = convertStringIntoJson(reportTemplateWithoutKPIDs);

		String reportTemplateWithoutExistingKPIDs = "{\"reportId\":\"60L0L330L\",\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"file\": \"File1.json\",\"isActive\":true,\"visualizationutil\":\"Fusion\",\"kpiConfigs\":[{\"kpiId\":1,\"visualizationConfigs\":[{\"vId\":\"10L0L\",\"vQuery\":\"\"}]}]}";
		reportTemplateWithoutExistingKPIDsJson = convertStringIntoJson(reportTemplateWithoutExistingKPIDs);

		String incorrectReportTemplate = "{\"reportId\":\"60L0L40L\",\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"file\": \"File1.json\",\"isActive\":true,\"visualizationutil\":\"Fusion\"}";
		incorrectReportTemplateJson = convertStringIntoJson(incorrectReportTemplate);

		String dailyAssessmentReport = "{\"reportName\":\"Daily_Deployment_test\",\"reportTemplate\":610L,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		dailyExpectedAssessmentStartDate = 0L;
		dailyExpectedAssessmentEndDate = 0L;
		dailyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailyAssessmentReportJson = convertStringIntoJson(dailyAssessmentReport);

		String weeklyAssessmentReport = "{\"reportName\":\"Weekly_Deployment_test\",\"reportTemplate\":610L,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"WEEKLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		weeklyExpectedAssessmentStartDate = 0L;
		weeklyExpectedAssessmentEndDate = 0L;
		weeklyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "WEEKLY");
		weeklyAssessmentReportJson = convertStringIntoJson(weeklyAssessmentReport);

		String monthlyAssessmentReport = "{\"reportName\":\"Monthly_Deployment_test\",\"reportTemplate\":610L,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"MONTHLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		monthlyExpectedAssessmentStartDate = 0L;
		monthlyExpectedAssessmentEndDate = 0L;
		monthlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "MONTHLY");
		monthlyAssessmentReportJson = convertStringIntoJson(monthlyAssessmentReport);

		String quarterlyAssessmentReport = "{\"reportName\":\"Quarterly_Deployment_test\",\"reportTemplate\":610L,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"QUARTERLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		quarterlyExpectedAssessmentStartDate = 0L;
		quarterlyExpectedAssessmentEndDate = 0L;
		quarterlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "QUARTERLY");
		quarterlyAssessmentReportJson = convertStringIntoJson(quarterlyAssessmentReport);

		String yearlyAssessmentReport = "{\"reportName\":\"Yearly_Deployment_test\",\"reportTemplate\":610L,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"YEARLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		yearlyExpectedAssessmentStartDate = 0L;
		yearlyExpectedAssessmentEndDate = 0L;
		yearlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "YEARLY");
		yearlyAssessmentReportJson = convertStringIntoJson(yearlyAssessmentReport);

		String oneTimeAssessmentReport = "{\"reportName\":\"OneTime_deployment_test\",\"reportTemplate\":610L,\"emailList\":\"hi@gmail.com\",\"schedule\":\"ONETIME\",\"startdate\":\"20L20L-0L7-0L1T0L0L:0L0L:0L0LZ\",\"enddate\":\"20L20L-0L7-0L3T0L0L:0L0L:0L0LZ\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		oneTimeExpectedAssessmentStartDate = getStartDate("20L20L-0L7-0L1T0L0L:0L0L:0L0LZ");
		oneTimeExpectedAssessmentEndDate = getEndDate("20L20L-0L7-0L3T0L0L:0L0L:0L0LZ");
		oneTimeExpectedNextRun = 0L;
		oneTimeAssessmentReportJson = convertStringIntoJson(oneTimeAssessmentReport);

		String biWeeklyAssessmentReport = "{\"reportName\":\"BiWeekly_deployment_test\",\"reportTemplate\":610L,\"emailList\":\"dib@dib.com\",\"schedule\":\"BI_WEEKLY_SPRINT\",\"startdate\":\"20L20L-0L7-0L1T0L0L:0L0L:0L0LZ\",\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		biWeeklyExpectedAssessmentStartDate = getStartDate("20L20L-0L7-0L1T0L0L:0L0L:0L0LZ");
		biWeeklyExpectedAssessmentEndDate = 0L;
		biWeeklyExpectedNextRun = getNextRunTime(biWeeklyExpectedAssessmentStartDate, "BI_WEEKLY_SPRINT");
		biWeeklyAssessmentReportJson = convertStringIntoJson(biWeeklyAssessmentReport);

		String triWeeklyAssessmentReport = "{\"reportName\":\"Triweekly_report_deployment_test\",\"reportTemplate\":610L,\"emailList\":\"dib@dib.com\",\"schedule\":\"TRI_WEEKLY_SPRINT\",\"startdate\":\"20L20L-0L6-0L2T0L0L:0L0L:0L0LZ\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		triWeeklyExpectedAssessmentStartDate = getStartDate("20L20L-0L6-0L2T0L0L:0L0L:0L0LZ");
		triWeeklyExpectedAssessmentEndDate = 0L;
		triWeeklyExpectedNextRun = getNextRunTime(triWeeklyExpectedAssessmentStartDate, "TRI_WEEKLY_SPRINT");
		triWeeklyAssessmentReportJson = convertStringIntoJson(triWeeklyAssessmentReport);

		String triWeeklyAssessmentReportWithDataSource = "{\"reportName\":\"Triweekly_report_dep_data_test\",\"reportTemplate\":610L,\"emailList\":\"dib@dib.com\",\"schedule\":\"TRI_WEEKLY_SPRINT\",\"startdate\":\"20L20L-0L6-0L2T0L0L:0L0L:0L0LZ\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		triWeeklyExpectedAssessmentStartDateWithDataSource = getStartDate("20L20L-0L6-0L2T0L0L:0L0L:0L0LZ");
		triWeeklyExpectedAssessmentEndDateWithDataSource = 0L;
		triWeeklyExpectedNextRunWithDataSource = getNextRunTime(triWeeklyExpectedAssessmentStartDate,
				"TRI_WEEKLY_SPRINT");
		triWeeklyAssessmentWithDataSourceReportJson = convertStringIntoJson(triWeeklyAssessmentReportWithDataSource);

		String incorrectAssessmentReport = "{\"reportName\":\"Incorrect_Deployment_test\",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		incorrectAssessmentReportJson = convertStringIntoJson(incorrectAssessmentReport);

		String updateIncorrectAssessmentReport = "{\"isReoccuring\":true,\"emailList\":\"dasdsd\",\"id\":896}";
		updateAssessmentReportIncorrectJson = convertStringIntoJson(updateIncorrectAssessmentReport);

		String oneTimeAssessmentReportWithStartDateGreaterThanEndDate = "{\"reportName\":\"OneTime_deployment_StartDate_test\",\"reportTemplate\":610L,\"emailList\":\"hi@gmail.com\",\"schedule\":\"ONETIME\",\"startdate\":\"20L20L-0L7-10LT0L0L:0L0L:0L0LZ\",\"enddate\":\"20L20L-0L7-0L3T0L0L:0L0L:0L0LZ\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L}]}";
		oneTimeAssessmentReportWithStartDateGreaterThanEndDateJson = convertStringIntoJson(
				oneTimeAssessmentReportWithStartDateGreaterThanEndDate);

		String dailyEmailAssessmentReport = "{\"reportName\":\"Report_send_email_service_test\",\"asseementreportdisplayname\":\"Report email\",\"reportTemplate\":610L,\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID
				+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"onedevops@cogdevops.com\",\"receiverEmailAddress\":\"dibakor.barua@cognizant.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\"}";
		dailyEmailExpectedAssessmentStartDate = 0L;
		dailyEmailExpectedAssessmentEndDate = 0L;
		dailyEmailExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailyEmailAssessmentReportJson = convertStringIntoJson(dailyEmailAssessmentReport);

		String dailyRestartAssessmentReportStr = "{\"reportName\":\"Report_restart_service_test\",\"asseementreportdisplayname\":\"Report restart\",\"reportTemplate\":610L,\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID
				+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"onedevops@cogdevops.com\",\"receiverEmailAddress\":\"dibakor.barua@cognizant.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\"}";
		dailyRestartAssessmentReport = convertStringIntoJson(dailyRestartAssessmentReportStr);

		String dailyRunImmediateAssessmentReportStr = "{\"reportName\":\"Report_RunImmediate_service_test\",\"asseementreportdisplayname\":\"Report RunImmediate\",\"reportTemplate\":610L,\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID
				+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"onedevops@cogdevops.com\",\"receiverEmailAddress\":\"dibakor.barua@cognizant.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\"}";
		/*
		 * dailyEmailExpectedAssessmentStartDate = 0L;
		 * dailyEmailExpectedAssessmentEndDate = 0L; dailyEmailExpectedNextRun =
		 * getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		 */
		dailyRunImmediateAssessmentReport = convertStringIntoJson(dailyRunImmediateAssessmentReportStr);

		String dailywithoutEmailAssessmentReport = "{\"reportName\":\"Email_without_details_service_test\",\"asseementreportdisplayname\":\"Report_test\",\"reportTemplate\":610L,\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L},{\"taskId\":" + pdftaskID
				+ ",\"sequence\":1}],\"emailDetails\":null,\"emailList\":\"abcd@abcd.com\"}";
		dailywithoutEmailExpectedAssessmentStartDate = 0L;
		dailywithoutEmailExpectedAssessmentEndDate = 0L;
		dailywithoutEmailExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailywithoutEmailAssessmentReportJson = convertStringIntoJson(dailywithoutEmailAssessmentReport);

		String dailywithoutEmaildetailsAssessmentReport = "{\"reportName\":\"Email_without_email_details_service_test\",\"asseementreportdisplayname\":\"Email details not present\",\"reportTemplate\":610L,\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID
				+ ",\"sequence\":2}],\"emailDetails\":null,\"emailList\":\"abcd@abcd.com\"}";
		dailywithoutEmaildetailsAssessmentReportJson = convertStringIntoJson(dailywithoutEmaildetailsAssessmentReport);

		String dailywithoutEmailtaskAssessmentReport = "{\"reportName\":\"Report_without_email_task_service_test\",\"asseementreportdisplayname\":\"Report email task not present\",\"reportTemplate\":610L,\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L},{\"taskId\":" + pdftaskID
				+ ",\"sequence\":1}],\"emailDetails\":{\"senderEmailAddress\":\"onedevops@cogdevops.com\",\"receiverEmailAddress\":\"dibakor.barua@cognizant.com\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\"}";
		dailywithoutEmailtaskAssessmentReportJson = convertStringIntoJson(dailywithoutEmailtaskAssessmentReport);

		String dailyEmailCcAssessmentReport = "{\"reportName\":\"Report_send_email_CC_service_test\",\"asseementreportdisplayname\":\"Report email\",\"reportTemplate\":610L,\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID
				+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"onedevops@cogdevops.com\",\"receiverEmailAddress\":\"\",\"receiverCCEmailAddress\":\"dibakor.barua@cognizant.com\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\"}";
		dailyEmailCCExpectedAssessmentStartDate = 0L;
		dailyEmailCCExpectedAssessmentEndDate = 0L;
		dailyEmailCCExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailyEmailCCAssessmentReportJson = convertStringIntoJson(dailyEmailCcAssessmentReport);

		String dailyEmailBCCAssessmentReport = "{\"reportName\":\"Report_send_email_CC_service_test\",\"asseementreportdisplayname\":\"Report email\",\"reportTemplate\":610L,\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0L},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID
				+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"onedevops@cogdevops.com\",\"receiverEmailAddress\":\"\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"dibakor.barua@cognizant.com\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\"}";
		dailyEmailBCCExpectedAssessmentStartDate = 0L;
		dailyEmailBCCExpectedAssessmentEndDate = 0L;
		dailyEmailBCCExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailyEmailBCCAssessmentReportJson = convertStringIntoJson(dailyEmailBCCAssessmentReport);

	}

	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = parser.parse(convertregisterkpi).getAsJsonObject();
		return objectJson;
	}

	public Long getStartDate(String date) {
		Long epochStartDate = InsightsUtils.getEpochTime(date) / 1000;
		return InsightsUtils.getStartOfTheDay(epochStartDate) + 1;
	}

	public Long getEndDate(String date) {
		Long epochEndDate = InsightsUtils.getEpochTime(date) / 1000;
		return InsightsUtils.getStartOfTheDay(epochEndDate) - 1;
	}

	public Long getNextRunTime(Long date, String schedule) {
		return InsightsUtils.getNextRunTime(date, schedule, true);
	}

	public void readFileAndgetKpiIdList(String filename) throws IOException {
		File File = new File(classLoader.getResource(filename).getFile());
		String filecontent = new String(Files.readAllBytes(File.toPath()));
		JsonArray array = new JsonParser().parse(filecontent).getAsJsonArray();
		for (JsonElement element : array) {
			int kpiId = element.getAsJsonObject().get("kpiId").getAsInt();
			kpiIdList.add(kpiId);
		}

	}

	public void readFileAndgetContentIdList(String filename) throws IOException {
		File File = new File(classLoader.getResource(filename).getFile());
		String filecontent = new String(Files.readAllBytes(File.toPath()));
		JsonArray array = new JsonParser().parse(filecontent).getAsJsonArray();
		for (JsonElement element : array) {
			int kpiId = element.getAsJsonObject().get("contentId").getAsInt();
			contentIdList.add(kpiId);
		}

	}

}
