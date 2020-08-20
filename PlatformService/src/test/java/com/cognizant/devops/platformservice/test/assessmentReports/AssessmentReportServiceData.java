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
package com.cognizant.devops.platformservice.test.assessmentReports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
	//JsonObject workflowTaskJson = null;
	InsightsWorkflowTask tasks = null;
	int taskID =0;
	JsonObject incorrectregisterkpiJson = null;
	JsonObject registerContentJson = null;
	JsonObject incorrectregisterContentJson = null;
	JsonObject registerkpiJson = null;
	JsonObject contentWithoutKpiJson =null;
	JsonObject reportTemplateJson = null;
	JsonObject reportTemplateWithoutKPIsJson = null;
	JsonObject reportTemplateWithoutExistingKPIDsJson = null;
	JsonObject incorrectReportTemplateJson = null;
	
	long dailyExpectedAssessmentStartDate = 0;
	long dailyExpectedAssessmentEndDate=0;
	long dailyExpectedNextRun = 0;
	JsonObject dailyAssessmentReportJson = null;
	
	long weeklyExpectedAssessmentStartDate = 0;
	long weeklyExpectedAssessmentEndDate=0;
	long weeklyExpectedNextRun = 0;
	JsonObject weeklyAssessmentReportJson = null;
	
	long monthlyExpectedAssessmentStartDate = 0;
	long monthlyExpectedAssessmentEndDate=0;
	long monthlyExpectedNextRun = 0;
	JsonObject monthlyAssessmentReportJson = null;
	
	long quarterlyExpectedAssessmentStartDate = 0;
	long quarterlyExpectedAssessmentEndDate=0;
	long quarterlyExpectedNextRun = 0;
	JsonObject quarterlyAssessmentReportJson = null;
	
	long yearlyExpectedAssessmentStartDate = 0;
	long yearlyExpectedAssessmentEndDate=0;
	long yearlyExpectedNextRun = 0;
	JsonObject yearlyAssessmentReportJson = null;
	
	long oneTimeExpectedAssessmentStartDate = 0;
	long oneTimeExpectedAssessmentEndDate=0;
	long oneTimeExpectedNextRun = 0;
	JsonObject oneTimeAssessmentReportJson = null;
	
	long biWeeklyExpectedAssessmentStartDate = 0;
	long biWeeklyExpectedAssessmentEndDate=0;
	long biWeeklyExpectedNextRun = 0;
	JsonObject biWeeklyAssessmentReportJson = null;
	
	long triWeeklyExpectedAssessmentStartDate = 0;
	long triWeeklyExpectedAssessmentEndDate=0;
	long triWeeklyExpectedNextRun = 0;
	JsonObject triWeeklyAssessmentReportJson = null;
	
	long triWeeklyExpectedAssessmentStartDateWithDataSource = 0;
	long triWeeklyExpectedAssessmentEndDateWithDataSource=0;
	long triWeeklyExpectedNextRunWithDataSource=0;
	JsonObject triWeeklyAssessmentWithDataSourceReportJson = null;
	
	JsonObject incorrectAssessmentReportJson = null;
	JsonObject updateAssessmentReportIncorrectJson = null;
	JsonObject oneTimeAssessmentReportWithStartDateGreaterThanEndDateJson = null;
	
	String deleteAssessmentReportWrongConfigId = "0";
	String reportIdForList = "610";
	
	public static List<Integer> contentIdList = new ArrayList<Integer>();
	public static List<Integer> kpiIdList = new ArrayList<Integer>();
	
	AssessmentReportServiceData() throws InsightsCustomException{
		prepareData();
	}
	
	
	void prepareData() throws InsightsCustomException {
		
		//String workflowTaskTest = "{\"description\": \"KPI_Execute_test\",\"mqChannel\": \"WORKFLOW.TASK.KPI.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber\",\"dependency\": \"100\",\"workflowType\": \"Report\"}";
		//workflowTaskJson = convertStringIntoJson(workflowTaskTest);
		//int response = workflowService.saveWorkflowTask(workflowTaskJson);
		//InsightsWorkflowTask tasks  =  workflowConfigDAL.getTaskbyTaskDescription(workflowTaskJson.get("description").getAsString());
		//taskID = tasks.getTaskId();

	String registerkpi = "{\"kpiId\":100201,\"name\":\"Total Successful Deployments\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\"}";
	registerkpiJson = convertStringIntoJson(registerkpi);
	
	String incorrectRegisterkpi = "{\"kpiId\":1001,\"name\":\"Avg all employee productivity for threshold \",\"schedule\":\"DAILY\",\"toolName\":\"PRODUCTIVITY\",\"group\":\"PRODUCTIVITY\",\"lastRunTime\":\"1586284260000\",\"neo4jQuery\":\"MATCH (n:PRODUCTIVITY) where n.completionDateEpochTime > {startTime} AND n.completionDateEpochTime < {endTime} WITH  avg(n.storyPoints*8) as StoryPoint, avg(n.authorTimeSpent) as authorTimeSpent  return   StoryPoint, authorTimeSpent, round((toFloat(StoryPoint)/authorTimeSpent)*100) as Productivity\",\"resultField\":\"Productivity\"}";
	incorrectregisterkpiJson = convertStringIntoJson(incorrectRegisterkpi);
	
	String registerContent = "{\"contentId\":10541,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments\",\"kpiId\":\"100201\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
	registerContentJson = convertStringIntoJson(registerContent);
	
	String incorrectContent = "{\"expectedTrend\":\"DOWNWARDS\",\"contentName\":\"Average Build Time\",\"kpiId\":\"1110\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"positive\":\"Average Build Time has decreased to {current:avgOutput}s from {previous:avgOutput}s \",\"negative\":\"Average Build Time has increased to {current:avgOutput}s from {previous:avgOutput}s \",\"neutral\":\"Average Build Time has remained constant to {avgOutput}s\"},\"isActive\":\"TRUE\"}";
	incorrectregisterContentJson = convertStringIntoJson(incorrectContent);
	
	String contentWithoutKpi = "{\"contentId\":10,\"expectedTrend\":\"DOWNWARDS\",\"contentName\":\"Average Build Time\",\"category\":\"COMPARISON\",\"kpiId\":\"11\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"positive\":\"Average Build Time has decreased to {current:avgOutput}s from {previous:avgOutput}s \",\"negative\":\"Average Build Time has increased to {current:avgOutput}s from {previous:avgOutput}s \",\"neutral\":\"Average Build Time has remained constant to {avgOutput}s\"},\"isActive\":\"TRUE\"}";
	contentWithoutKpiJson = convertStringIntoJson(contentWithoutKpi);
	
	

	String reportTemplate = "{\"reportId\":\"610\",\"reportName\":\"Fail Report_test\",\"description\":\"Testing\",\"isActive\":true,\"file\":\"File.json\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
	reportTemplateJson = convertStringIntoJson(reportTemplate);
	
	String reportTemplateWithoutKPIDs = "{\"reportId\":\"60020\",\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"file\": \"File1.json\",\"isActive\":true}";
	reportTemplateWithoutKPIsJson = convertStringIntoJson(reportTemplateWithoutKPIDs);
	
	String reportTemplateWithoutExistingKPIDs = "{\"reportId\":\"600330\",\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"file\": \"File1.json\",\"isActive\":true,\"kpiConfigs\":[{\"kpiId\":1,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"\"}]}]}";
	reportTemplateWithoutExistingKPIDsJson = convertStringIntoJson(reportTemplateWithoutExistingKPIDs);
	
	String incorrectReportTemplate = "{\"reportId\":\"60040\",\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"file\": \"File1.json\",\"isActive\":true}";
	incorrectReportTemplateJson = convertStringIntoJson(incorrectReportTemplate);

	String dailyAssessmentReport = "{\"reportName\":\"Daily_Deployment_test\",\"reportTemplate\":610,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	dailyExpectedAssessmentStartDate = 0;
	dailyExpectedAssessmentEndDate=0;
	dailyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
	dailyAssessmentReportJson = convertStringIntoJson(dailyAssessmentReport);
	
	String weeklyAssessmentReport = "{\"reportName\":\"Weekly_Deployment_test\",\"reportTemplate\":610,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"WEEKLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	weeklyExpectedAssessmentStartDate = 0;
	weeklyExpectedAssessmentEndDate=0;
	weeklyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "WEEKLY");
	weeklyAssessmentReportJson = convertStringIntoJson(weeklyAssessmentReport);
	
	String monthlyAssessmentReport = "{\"reportName\":\"Monthly_Deployment_test\",\"reportTemplate\":610,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"MONTHLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	monthlyExpectedAssessmentStartDate = 0;
	monthlyExpectedAssessmentEndDate=0;
	monthlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "MONTHLY");
	monthlyAssessmentReportJson = convertStringIntoJson(monthlyAssessmentReport);
	
	String quarterlyAssessmentReport = "{\"reportName\":\"Quarterly_Deployment_test\",\"reportTemplate\":610,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"QUARTERLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	quarterlyExpectedAssessmentStartDate = 0;
	quarterlyExpectedAssessmentEndDate=0;
	quarterlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "QUARTERLY");
	quarterlyAssessmentReportJson = convertStringIntoJson(quarterlyAssessmentReport);
	
	String yearlyAssessmentReport = "{\"reportName\":\"Yearly_Deployment_test\",\"reportTemplate\":610,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"YEARLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	yearlyExpectedAssessmentStartDate = 0;
	yearlyExpectedAssessmentEndDate=0;
	yearlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "YEARLY");
	yearlyAssessmentReportJson = convertStringIntoJson(yearlyAssessmentReport);
	
	String oneTimeAssessmentReport = "{\"reportName\":\"OneTime_deployment_test\",\"reportTemplate\":610,\"emailList\":\"hi@gmail.com\",\"schedule\":\"ONETIME\",\"startdate\":\"2020-07-01T00:00:00Z\",\"enddate\":\"2020-07-03T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	oneTimeExpectedAssessmentStartDate = getStartDate("2020-07-01T00:00:00Z");
	oneTimeExpectedAssessmentEndDate=getEndDate("2020-07-03T00:00:00Z");
	oneTimeExpectedNextRun=0;
	oneTimeAssessmentReportJson = convertStringIntoJson(oneTimeAssessmentReport);
	
	String biWeeklyAssessmentReport = "{\"reportName\":\"BiWeekly_deployment_test\",\"reportTemplate\":610,\"emailList\":\"dib@dib.com\",\"schedule\":\"BI_WEEKLY_SPRINT\",\"startdate\":\"2020-07-01T00:00:00Z\",\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	biWeeklyExpectedAssessmentStartDate = getStartDate("2020-07-01T00:00:00Z");
	biWeeklyExpectedAssessmentEndDate=0;
	biWeeklyExpectedNextRun = getNextRunTime(biWeeklyExpectedAssessmentStartDate, "BI_WEEKLY_SPRINT");
	biWeeklyAssessmentReportJson = convertStringIntoJson(biWeeklyAssessmentReport);

	String triWeeklyAssessmentReport = "{\"reportName\":\"Triweekly_report_deployment_test\",\"reportTemplate\":610,\"emailList\":\"dib@dib.com\",\"schedule\":\"TRI_WEEKLY_SPRINT\",\"startdate\":\"2020-06-02T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	triWeeklyExpectedAssessmentStartDate = getStartDate("2020-06-02T00:00:00Z");
	triWeeklyExpectedAssessmentEndDate=0;
	triWeeklyExpectedNextRun = getNextRunTime(triWeeklyExpectedAssessmentStartDate, "TRI_WEEKLY_SPRINT");
	triWeeklyAssessmentReportJson = convertStringIntoJson(triWeeklyAssessmentReport);

	String triWeeklyAssessmentReportWithDataSource = "{\"reportName\":\"Triweekly_report_dep_data_test\",\"reportTemplate\":610,\"emailList\":\"dib@dib.com\",\"schedule\":\"TRI_WEEKLY_SPRINT\",\"startdate\":\"2020-06-02T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	triWeeklyExpectedAssessmentStartDateWithDataSource = getStartDate("2020-06-02T00:00:00Z");
	triWeeklyExpectedAssessmentEndDateWithDataSource=0;
	triWeeklyExpectedNextRunWithDataSource=getNextRunTime(triWeeklyExpectedAssessmentStartDate, "TRI_WEEKLY_SPRINT");
	triWeeklyAssessmentWithDataSourceReportJson= convertStringIntoJson(triWeeklyAssessmentReportWithDataSource);

	String incorrectAssessmentReport = "{\"reportName\":\"Incorrect_Deployment_test\",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	incorrectAssessmentReportJson = convertStringIntoJson(incorrectAssessmentReport);
	
	String updateIncorrectAssessmentReport = "{\"isReoccuring\":true,\"emailList\":\"dasdsd\",\"id\":896}";
	updateAssessmentReportIncorrectJson = convertStringIntoJson(updateIncorrectAssessmentReport);
	
	String oneTimeAssessmentReportWithStartDateGreaterThanEndDate = "{\"reportName\":\"OneTime_deployment_StartDate_test\",\"reportTemplate\":610,\"emailList\":\"hi@gmail.com\",\"schedule\":\"ONETIME\",\"startdate\":\"2020-07-10T00:00:00Z\",\"enddate\":\"2020-07-03T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}]}";
	oneTimeAssessmentReportWithStartDateGreaterThanEndDateJson = convertStringIntoJson(oneTimeAssessmentReportWithStartDateGreaterThanEndDate);
	}

	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = parser.parse(convertregisterkpi).getAsJsonObject();
		return objectJson;
	}
	
	public long getStartDate(String date) {
		long epochStartDate = InsightsUtils.getEpochTime(date) / 1000;
		return InsightsUtils.getStartOfTheDay(epochStartDate) + 1;
	}
	
	public long getEndDate(String date) {
		long epochEndDate = InsightsUtils.getEpochTime(date) / 1000;
		return InsightsUtils.getStartOfTheDay(epochEndDate) - 1;
	}
	
	public long getNextRunTime(long date,String schedule) {
		return InsightsUtils.getNextRunTime(date,schedule, true);
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
