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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.multipart.MultipartFile;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.assessmentreport.service.GrafanaUtilities;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AssessmentReportServiceData extends AbstractTestNGSpringContextTests {
	private static final Logger log = LogManager.getLogger(AssessmentReportServiceData.class);
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	@Autowired
    GrafanaUtilities GrafanaUtilities;
	@Autowired
	WorkflowServiceImpl workflowService;
	@Autowired
	private HttpServletRequest httpRequest;
	
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	File kpiFile = new File(classLoader.getResource("KPIDefination.json").getFile());
	File kpiFileNoData = new File(classLoader.getResource("KPIDefinationNoData.json").getFile());
	File configFile = new File(classLoader.getResource("ContentsConfiguration.json").getFile());
	File kpiFileTxt = new File(classLoader.getResource("KPIDefination.txt").getFile());
	File configFileTxt = new File(classLoader.getResource("ContentsConfiguration.txt").getFile());
	File emptyFile = new File(classLoader.getResource("EmptyFile.json").getFile());
	File templateJsonFile = new File(classLoader.getResource("report_template_upload_test.json").getFile());
	File tableJson = new File(classLoader.getResource("table.json").getFile());
	
	InsightsWorkflowTask tasks = null;
	JsonObject incorrectregisterkpiJson = null;
	JsonObject registerContentJson = null;
	JsonObject incorrectregisterContentJson = null;
	JsonObject registerkpiJson = null;
	JsonObject registerROIkpiJson = null;
	JsonObject contentWithoutKpiJson = null;
	JsonObject reportTemplateJson = null;
	JsonObject reportTemplateROIJson = null;
	JsonObject reportTemplateWithoutKPIsJson = null;
	JsonObject reportTemplateWithoutExistingKPIDsJson = null;
	JsonObject incorrectReportTemplateJson = null;
	JsonObject registerGrafanakpiJson = null;
	JsonObject registerGrafanaContentJson = null;
	JsonObject grafanaPDFreportTemplateJson = null;
	
	//variables for code coverage methods
	String updatekpiString = "{\"kpiId\":100201,\"name\":\"TotalSuccessfulDeploymentsUpdate\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH(n:RUNDECK:DATA)WHEREn.SPKstartTime>{startTime}andn.SPKstartTime<{endTime}andn.SPKvector='DEPLOYMENT'andn.SPKstatus='Success'RETURNcount(n.SPKstatus)astotalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}" ;
	String updatekpiStringFail = "{\"kpiId\":124332,\"name\":\"TotalSuccessfulDeploymentsUpdate\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH(n:RUNDECK:DATA)WHEREn.SPKstartTime>{startTime}andn.SPKstartTime<{endTime}andn.SPKvector='DEPLOYMENT'andn.SPKstatus='Success'RETURNcount(n.SPKstatus)astotalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}" ;
	String updatekpiStringValidation = "&amp;{<\"kpiId\":100201,\"name\":\"TotalSuccessfulDeploymentsUpdate\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH(n:RUNDECK:DATA)WHEREn.SPKstartTime>{startTime}andn.SPKstartTime<{endTime}andn.SPKvector='DEPLOYMENT'andn.SPKstatus='Success'RETURNcount(n.SPKstatus)astotalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}" ;
	
	String deleteContentRequestValidation = "&amp;{<\"contentId\":10541}";
	String deleteKpiString = "{\"kpiId\":\"100201\"}";
	String deleteKpiStringFail = "{\"kpiId\":\"124332\"}";
	String deleteKpiStringFailException = "{\"kpiId\":\"1002011111111\"}";
	String deleteKpiStringValidation = "&amp;{<\"kpiId\":\"100201\"}";
	
	String updateContentString = "{\"contentId\":10541,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments Update\",\"kpiId\":\"100201\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
	String updateContentString1 = "{\"contentId\":105411111,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments Update\",\"kpiId\":\"100201\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
	String updateContentString2 = "{\"contentId\":105411111,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments Update\",\"kpiId\":\"123456\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
	String updateContentStringValidation = "&amp;{<\"contentId\":10541,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments Update\",\"kpiId\":\"100201\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
	String reportTemplateEdit ="";
	String reportTemplateDuplicate="";
	String reportTemplateEditMissingReportId="";
	String retryJson="";
	String retryJsonValidation="";
	private static final String ORG_NAME_PREFIX = "Report Org. ";
	private static final String NAME = "name";
	private static final String PDFTOKEN = "pdftoken";
	private static final String BASICAUTH = "Basic ";
	JsonObject grafanaAssessmentReportJson = null;
	public static final String REPORT_SOURCE = "Report";
	
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
	JsonObject oneTimeAssessmentROIReportJson = null;

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
	String deleteAssessmentReportWrongConfigId = "0";
	int reportIdForList = 0;
	int reportIdForROI = 0;
	int grafanaReportId = 0;
	int taskID = 0;
	int pdftaskID = 0;
	int emailtaskID = 0;
	String reportIdString="";
	String reportIdROIString="";
	int assessmentid =0;
	String registerkpi = "{\"kpiId\":100201,\"name\":\"Total Successful Deployments\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}";

	String registerROIkpi = "{\"kpiId\":100202,\"name\":\"Total Successful Deployments\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}";
	
	String incorrectRegisterkpi = "{\"kpiId\":1001,\"name\":\"Avg all employee productivity for threshold \",\"schedule\":\"DAILY\",\"toolName\":\"PRODUCTIVITY\",\"group\":\"PRODUCTIVITY\",\"lastRunTime\":\"1586284260000\",\"neo4jQuery\":\"MATCH (n:PRODUCTIVITY) where n.completionDateEpochTime > {startTime} AND n.completionDateEpochTime < {endTime} WITH  avg(n.storyPoints*8) as StoryPoint, avg(n.authorTimeSpent) as authorTimeSpent  return   StoryPoint, authorTimeSpent, round((toFloat(StoryPoint)/authorTimeSpent)*100) as Productivity\",\"resultField\":\"Productivity\"}";
	
	String registerSecondkpi = "{\"kpiId\":100144,\"name\":\"Minimum Deployment Time\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE  n.SPKstartTime >= {startTime} and n.SPKstartTime <= {endTime} and n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN COALESCE(Min(toInt(n.SPKduration)),0) as MinDeploymentTime\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"MinDeploymentTime\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}";

	String registerkpiValidation = "&amp;{<\"kpiId\":100201,\"name\":\"Total Successful Deployments\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}";

	String reportTemplateSave = "{\"reportName\":\"report_template_save\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";

	String editReportTemplate = "{\"reportName\":\"report_template_save\",\"reportId\":\"reportIdData\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]},{\"kpiId\":100144,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";	
	
	String registerContent = "{\"contentId\":10541,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments\",\"kpiId\":\"100201\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
	
	String incorrectContent = "{\"expectedTrend\":\"DOWNWARDS\",\"contentName\":\"Average Build Time\",\"kpiId\":\"1110\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"positive\":\"Average Build Time has decreased to {current:avgOutput}s from {previous:avgOutput}s \",\"negative\":\"Average Build Time has increased to {current:avgOutput}s from {previous:avgOutput}s \",\"neutral\":\"Average Build Time has remained constant to {avgOutput}s\"},\"isActive\":\"TRUE\"}";
	
	String contentWithoutKpi = "{\"contentId\":10,\"expectedTrend\":\"DOWNWARDS\",\"contentName\":\"Average Build Time\",\"category\":\"COMPARISON\",\"kpiId\":\"11\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"positive\":\"Average Build Time has decreased to {current:avgOutput}s from {previous:avgOutput}s \",\"negative\":\"Average Build Time has increased to {current:avgOutput}s from {previous:avgOutput}s \",\"neutral\":\"Average Build Time has remained constant to {avgOutput}s\"},\"isActive\":\"TRUE\"}";
	
	String registerContentValiation = "&amp;{<\"contentId\":10541,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments\",\"kpiId\":\"100201\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
	
	String reportTemplate = "{\"reportName\":\"Fail_Report_test\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
	
	String reportTemplateEditValidation = "&amp;{<\"reportName\":\"Fail_Report_test123\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";

	String reportTemplateValidation = "&amp;{<\"reportName\":\"Fail_Report_test\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";

	String reportTemplateROI = "{\"reportName\":\"ROI_ReportTest\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"templateType\":\"ROITemplate\",\"kpiConfigs\":[{\"kpiId\":100202,\"visualizationConfigs\":[{\"vId\":\"101\",\"vQuery\":\"Query\"}]}]}";
	
	String reportTemplateROIValidation = "&amp;{<\"reportName\":\"ROI_ReportTest\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"templateType\":\"ROITemplate\",\"kpiConfigs\":[{\"kpiId\":100202,\"visualizationConfigs\":[{\"vId\":\"101\",\"vQuery\":\"Query\"}]}]}";
	
	String reportTemplateWithoutKPIDs = "{\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"visualizationutil\":\"FUSION\",\"isActive\":true}";
	
	String reportTemplateWithoutExistingKPIDs = "{\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"isActive\":true,\"visualizationutil\":\"Fusion\",\"kpiConfigs\":[{\"kpiId\":1,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"\"}]}]}";
	
	String incorrectReportTemplate = "{\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"isActive\":true,\"visualizationutil\":\"FUSION\"}";
	
	String grafanaReportTemplate = "{\"reportName\":\"Grafana_PDFReport_test12344\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"GRAFANAPDF\",\"kpiConfigs\":[{\"kpiId\":200202,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"MATCH (n:KPI:RESULTS) where n.assessmentId = {assessmentId} and n.kpiId={kpiId} RETURN  n.`ISSUE API` as `ISSUE API`, n.`TOOL NAME` as `TOOL NAME`, n.`STATUS` as `STATUS`, n.`Key` as `Key`\",\"vType\":\"table_200202\"}]}]}";
	
	File file = new File(classLoader.getResource("report_template_upload_test.json").getFile()); 
	
	public static List<Integer> contentIdList = new ArrayList<Integer>();
	public static List<Integer> kpiIdList = new ArrayList<Integer>();
	
	String dailyAssessmentReport="";
	String AssessmentReportValidation="";
	String weeklyAssessmentReport=""; 
    String monthlyAssessmentReport=""; 
	String quarterlyAssessmentReport="";
	String yearlyAssessmentReport="";
	String oneTimeAssessmentReport=""; 
	String biWeeklyAssessmentReport="";
	String triWeeklyAssessmentReport="";
	String triWeeklyAssessmentReportWithDataSource="";
	String incorrectAssessmentReport = "{\"reportName\":\"Incorrect_Deployment_test\",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
			+ taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
	
	String setReportTemplateStatusWrongId = "{\"reportName\":\"report_template_status\",\"reportId\":" + 1234567 + ",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
	
	String updateIncorrectAssessmentReport = "{\"isReoccuring\":true,\"emailList\":\"dasdsd\",\"id\":896}";
	String oneTimeAssessmentReportWithStartDateGreaterThanEndDate=""; 
	String dailyEmailAssessmentReport=""; 
	String dailywithoutEmailAssessmentReport=""; 
	String dailyEmailCcAssessmentReport=""; 
	String dailyEmailBCCAssessmentReport=""; 
	String dailyRestartAssessmentReportStr=""; 
	String dailyRunImmediateAssessmentReportStr="";
	String deleteReportTemplate="";
	String deleteROIReportTemplate="";
	String setWorkflowStatusValidation="";
	String saveAssessmentReportWithInvalidReportTemplate="";
	String deleteUploadedReportTemplate="";
	String setReportTemplateStatus=""; 
	String setReportTemplateStatusValidation="";
	String oneTimeAssessmentROIReport=""; 
	String updateAssessmentReportState="";
	String updateAssessmentReportStateFail="";
	String updateAssessmentReport="";
	String updateGrafanaAssessmentReport="";
	String grafanaAssessmentReport="";
	String deleteGrafanaReportTemplate="";
	String sampleDashboard="{\"dashboard\":{\"id\":null,\"uid\":null,\"title\":\"Report_send_email_service_test1\",\"tags\":[],\"panels\":[{\"datasource\":\"Neo4j Data Source\",\"description\":\"\",\"fieldConfig\":{\"defaults\":{\"color\":{\"mode\":\"thresholds\"},\"custom\":{\"align\":\"auto\",\"displayMode\":\"auto\"},\"mappings\":[],\"thresholds\":{\"mode\":\"absolute\",\"steps\":[{\"color\":\"blue\",\"value\":null}]}},\"overrides\":[]},\"gridPos\":{\"h\":9,\"w\":12,\"x\":0,\"y\":18},\"id\":1,\"options\":{\"showHeader\":true},\"pluginVersion\":\"8.1.3\",\"targets\":[{\"cache\":false,\"cacheType\":false,\"cacheValue\":false,\"constant\":6.5,\"fixTime\":false,\"graph\":false,\"queryText\":\"MATCH (n:KPI:RESULTS) where n.assessmentId = {assessmentId} and n.kpiId=200202 RETURN  n.`ISSUE API` as `ISSUE API`, n.`TOOL NAME` as `TOOL NAME`, n.`STATUS` as `STATUS`, n.`Key` as `Key`\",\"raw\":false,\"refId\":\"A\",\"stats\":false,\"table\":true,\"timeseries\":false,\"varTime\":false}],\"title\":\"Total Successful Deployments\",\"type\":\"table\"},{\"datasource\":\"Neo4j Data Source\",\"fieldConfig\":{\"defaults\":{\"color\":{\"mode\":\"thresholds\"},\"custom\":{\"align\":\"auto\",\"displayMode\":\"color-text\",\"filterable\":false},\"mappings\":[],\"thresholds\":{\"mode\":\"absolute\",\"steps\":[{\"color\":\"green\",\"value\":null},{\"color\":\"red\",\"value\":80}]}},\"overrides\":[]},\"gridPos\":{\"h\":9,\"w\":12,\"x\":12,\"y\":0},\"id\":2,\"options\":{\"showHeader\":false},\"pluginVersion\":\"8.1.3\",\"targets\":[{\"cache\":false,\"cacheType\":false,\"cacheValue\":false,\"constant\":6.5,\"fixTime\":false,\"graph\":false,\"queryText\":\"MATCH (n:CONTENT_RESULT) where n.assessmentReportName= 'Report_send_email_service_test1' and n.kpiId= 200202 with distinct max(n.executionId) as latestexecutionId Match (b:CONTENT_RESULT) where b.executionId =latestexecutionId and b.kpiId= 200202 RETURN b.inferenceText as Text order by b.executionId, b.kpiId desc\",\"raw\":false,\"refId\":\"A\",\"stats\":false,\"table\":true,\"timeseries\":false,\"varTime\":false}],\"title\":\"Observation of Total Successful Deployments\",\"transparent\":true,\"type\":\"table\"}],\"timepicker\":{},\"templating\":{\"list\":[]},\"timezone\":\"browser\",\"schemaVersion\":16,\"version\":0},\"folderId\":0,\"overwrite\":false}";
	JsonObject sampleDashboardJson = convertStringIntoJson(sampleDashboard);
	String username="Test_User ";
	
	public void setReportId(String reportIdString) {
		this.reportIdString = reportIdString;
		this.reportTemplateEdit = "{\"reportName\":\"Fail_Report_test123\",\"description\":\"Testing\",\"reportId\":"+reportIdString+",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
		this.reportTemplateDuplicate = "{\"reportName\":\"Fail_Report_test123\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
		
		this.reportTemplateEditMissingReportId = "{\"reportName\":\"Fail_Report_test123\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";

		this.dailyAssessmentReport = "{\"reportName\":\"Daily_Deployment_test\",\"reportTemplate\":" + reportIdString+ ",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org.\",\"userName\":\"Test_User \"}";;
		
		this.AssessmentReportValidation = "&amp;{<\"reportName\":\"Daily_Deployment_test\",\"reportTemplate\":" + reportIdString+ ",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org.\",\"userName\":\"Test_User \"}";;
			
		this.weeklyAssessmentReport ="{\"reportName\":\"Weekly_Deployment_test\",\"reportTemplate\":" +reportIdString+ ",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"WEEKLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.monthlyAssessmentReport = "{\"reportName\":\"Monthly_Deployment_test\",\"reportTemplate\":"+reportIdString+ ",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"MONTHLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
		        + taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.quarterlyAssessmentReport = "{\"reportName\":\"Quarterly_Deployment_test\",\"reportTemplate\":"+ reportIdString+ ",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"QUARTERLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.yearlyAssessmentReport ="{\"reportName\":\"Yearly_Deployment_test\",\"reportTemplate\":"+ reportIdString+ ",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"YEARLY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.oneTimeAssessmentReport = "{\"reportName\":\"OneTime_deployment_test\",\"reportTemplate\":"+ reportIdString+ ",\"emailList\":\"hi@gmail.com\",\"schedule\":\"ONETIME\",\"startdate\":\"2020-07-01T00:00:00Z\",\"enddate\":\"2020-07-03T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.biWeeklyAssessmentReport = "{\"reportName\":\"BiWeekly_deployment_test\",\"reportTemplate\":"+ reportIdString+ ",\"emailList\":\"dib@dib.com\",\"schedule\":\"BI_WEEKLY_SPRINT\",\"startdate\":\"2020-07-01T00:00:00Z\",\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.triWeeklyAssessmentReport ="{\"reportName\":\"Triweekly_report_deployment_test\",\"reportTemplate\":"+ reportIdString+ ",\"emailList\":\"dib@dib.com\",\"schedule\":\"TRI_WEEKLY_SPRINT\",\"startdate\":\"2020-06-02T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.triWeeklyAssessmentReportWithDataSource = "{\"reportName\":\"Triweekly_report_dep_data_test\",\"reportTemplate\":"+ reportIdString+ ",\"emailList\":\"dib@dib.com\",\"schedule\":\"TRI_WEEKLY_SPRINT\",\"startdate\":\"2020-06-02T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.oneTimeAssessmentReportWithStartDateGreaterThanEndDate = "{\"reportName\":\"OneTime_deployment_StartDate_test\",\"reportTemplate\":" + reportIdString + ",\"emailList\":\"hi@gmail.com\",\"schedule\":\"ONETIME\",\"startdate\":\"2020-07-10T00:00:00Z\",\"enddate\":\"2020-07-03T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0}],\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.dailyEmailAssessmentReport = "{\"reportName\":\"Report_send_email_service_test\",\"asseementreportdisplayname\":\"Report email\",\"reportTemplate\":" + reportIdString + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"demo123@abc.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.dailywithoutEmailAssessmentReport = "{\"reportName\":\"Email_without_details_service_test\",\"asseementreportdisplayname\":\"Report_test\",\"reportTemplate\":" + reportIdString + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID+ ",\"sequence\":1}],\"emailDetails\":null,\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.dailyEmailCcAssessmentReport = "{\"reportName\":\"Report_send_email_CC_service_test\",\"asseementreportdisplayname\":\"Report email\",\"reportTemplate\":" + reportIdString + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"\",\"receiverCCEmailAddress\":\"demo123@abc.com\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.dailyEmailBCCAssessmentReport = "{\"reportName\":\"Report_send_email_BCC_service_test\",\"asseementreportdisplayname\":\"Report email\",\"reportTemplate\":" + reportIdString + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"\",\"receiverCCEmailAddress\":\"demo123@abc.com\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.dailyRestartAssessmentReportStr = "{\"reportName\":\"Report_restart_service_test\",\"asseementreportdisplayname\":\"Report restart\",\"reportTemplate\":" + reportIdString + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"demo123@abc.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.dailyRunImmediateAssessmentReportStr = "{\"reportName\":\"Report_RunImmediate_service_test\",\"asseementreportdisplayname\":\"Report RunImmediate\",\"reportTemplate\":" + reportIdString + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"demo123@abc.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.setReportTemplateStatus = "{\"reportName\":\"report_template_status\",\"reportId\":" + reportIdString + ",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
		
		this.setReportTemplateStatusValidation = "&amp;{<\"reportName\":\"report_template_status\",\"reportId\":" + reportIdString + ",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
		
		this.deleteReportTemplate= "{\"reportId\"=" +reportIdString+ ",\"isActive\":true\"}";
		
		this.setWorkflowStatusValidation = "{<\"reportName&amp;\":\"Report_restart_service_test\",\"asseementreportdisplayname\":\"Report restart\",\"reportTemplate\":" + reportIdString + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"demo123@abc.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		this.saveAssessmentReportWithInvalidReportTemplate = "{\"reportName\":\"Report_send_email_BCC_service_test\",\"asseementreportdisplayname\":\"Report email\",\"reportTemplate\":" + reportIdString+"21" + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"\",\"receiverCCEmailAddress\":\"demo123@abc.com\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		assessmentReportDataInit();	
	}
	
	public void deleteUploadedReportTemplate(int id) {
	this.deleteUploadedReportTemplate= "{\"reportId\"=" +id+ ",\"isActive\":true\"}";
	}
	
	public void getGrafanaAssessmentReport(int id) {
		this.grafanaAssessmentReport = "{\"reportName\":\"Report_send_email_service_test1\",\"asseementreportdisplayname\":\"Report email1\",\"reportTemplate\":" + id + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID+ ",\"sequence\":2}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"dakor.barua@cognizant.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		grafanaAssessmentReportJson = convertStringIntoJson(grafanaAssessmentReport);
		this.deleteGrafanaReportTemplate= "{\"reportId\"=" +id+ ",\"isActive\":true\"}";	
	}
	
	public void setOneTimeAssessmentROIReport(String reportIdROIString) {
		this.reportIdROIString=reportIdROIString;
		this.oneTimeAssessmentROIReport = "{\"reportName\":\"ROI_OneTime_deployment_test\",\"reportTemplate\":"+ reportIdROIString+ ", \"milestoneId\": 12220 ,\"emailList\":\"hi@gmail.com\",\"schedule\":\"ONETIME\",\"startdate\":\"2020-07-01T00:00:00Z\",\"enddate\":\"2020-07-03T00:00:00Z\",\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		this.deleteROIReportTemplate= "{\"reportId\"=" +reportIdROIString+ ",\"isActive\":true\"}";
	}
	
	public void getInfoAssessmentReport(int id) {
		updateAssessmentReport = "{\"isReoccuring\":true,\"emailList\":\"dasdsd\",\"id\":"+ id+ ",\"tasklist\":[{\"taskId\":1,\"description\":\"KPI_Execute_service_test\"},{\"taskId\":2,\"description\":\"PDF_Execute_service_test\"},{\"taskId\":3,\"description\":\"Email_Execute_service_test\"}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"\",\"receiverCCEmailAddress\":\"demo123@abc.com\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
	}
	
	public void setRetryStatusJson(int configId) {
		retryJson = "{\"configId\":" + configId + ",\"status\":\"RESTART\"}";
		retryJsonValidation = "{\"configIdd\":" + configId + ",\"status\":\"RESTART\"}";
		
	}
	public void getInfoAssessmentReportState(int id) {
		updateAssessmentReportState= "{\"id\"=" +id+ ",\"isActive\":true\"}";
		updateAssessmentReportStateFail= "{\"idd\"=" +id+ ",\"isActive\":true}";
	}
	
	public void getInfoGrafanaAssessmentReport(int id) {
		updateAssessmentReport = "{\"isReoccuring\":true,\"emailList\":\"dasdsd\",\"id\":"+ id+ ",\"tasklist\":[{\"taskId\":1,\"description\":\"KPI_Execute_service_test\"},{\"taskId\":2,\"description\":\"PDF_Execute_service_test\"},{\"taskId\":3,\"description\":\"Email_Execute_service_test\"}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"\",\"receiverCCEmailAddress\":\"demo123@abc.com\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		updateGrafanaAssessmentReport = updateAssessmentReport;		
	}
	
	void prepareAssessmentData() throws InsightsCustomException {
		try {
			InsightsWorkflowType type = new InsightsWorkflowType();
			type.setWorkflowType(WorkflowTaskEnum.WorkflowType.REPORT.getValue());
			workflowConfigDAL.saveWorkflowType(type);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest workflowtype record ", e);
		}
		
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

		String registerkpi = "{\"kpiId\":100201,\"name\":\"Total Successful Deployments\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}";
		registerkpiJson = convertStringIntoJson(registerkpi);
		
		String registerROIkpi = "{\"kpiId\":100202,\"name\":\"Total Successful Deployments\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}";
		registerROIkpiJson = convertStringIntoJson(registerROIkpi);

		String incorrectRegisterkpi = "{\"kpiId\":1001,\"name\":\"Avg all employee productivity for threshold \",\"schedule\":\"DAILY\",\"toolName\":\"PRODUCTIVITY\",\"group\":\"PRODUCTIVITY\",\"lastRunTime\":\"1586284260000\",\"neo4jQuery\":\"MATCH (n:PRODUCTIVITY) where n.completionDateEpochTime > {startTime} AND n.completionDateEpochTime < {endTime} WITH  avg(n.storyPoints*8) as StoryPoint, avg(n.authorTimeSpent) as authorTimeSpent  return   StoryPoint, authorTimeSpent, round((toFloat(StoryPoint)/authorTimeSpent)*100) as Productivity\",\"resultField\":\"Productivity\"}";
		incorrectregisterkpiJson = convertStringIntoJson(incorrectRegisterkpi);

		String registerContent = "{\"contentId\":10541,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments\",\"kpiId\":\"100201\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
		registerContentJson = convertStringIntoJson(registerContent);

		String incorrectContent = "{\"expectedTrend\":\"DOWNWARDS\",\"contentName\":\"Average Build Time\",\"kpiId\":\"1110\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"positive\":\"Average Build Time has decreased to {current:avgOutput}s from {previous:avgOutput}s \",\"negative\":\"Average Build Time has increased to {current:avgOutput}s from {previous:avgOutput}s \",\"neutral\":\"Average Build Time has remained constant to {avgOutput}s\"},\"isActive\":\"TRUE\"}";
		incorrectregisterContentJson = convertStringIntoJson(incorrectContent);

		String contentWithoutKpi = "{\"contentId\":10,\"expectedTrend\":\"DOWNWARDS\",\"contentName\":\"Average Build Time\",\"category\":\"COMPARISON\",\"kpiId\":\"11\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"positive\":\"Average Build Time has decreased to {current:avgOutput}s from {previous:avgOutput}s \",\"negative\":\"Average Build Time has increased to {current:avgOutput}s from {previous:avgOutput}s \",\"neutral\":\"Average Build Time has remained constant to {avgOutput}s\"},\"isActive\":\"TRUE\"}";
		contentWithoutKpiJson = convertStringIntoJson(contentWithoutKpi);

		String reportTemplate = "{\"reportName\":\"Fail_Report_test\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
		reportTemplateJson = convertStringIntoJson(reportTemplate);
		
		String reportTemplateROI = "{\"reportName\":\"ROI_ReportTest\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"templateType\":\"ROITemplate\",\"kpiConfigs\":[{\"kpiId\":100202,\"visualizationConfigs\":[{\"vId\":\"101\",\"vQuery\":\"Query\"}]}]}";
		reportTemplateROIJson = convertStringIntoJson(reportTemplateROI);
		
		String registerGrafanakpi = "{\"kpiId\":200202,\"name\":\"Total Successful Deployments\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\",\"outputDatasource\":\"NEO4J\",\"usecase\":\"\"}";
		registerGrafanakpiJson = convertStringIntoJson(registerGrafanakpi);
		
		String registerGrafanaContent = "{\"contentId\":50022,\"expectedTrend\":\"UPWARDS\",\"contentName\":\"Total successful Deployments\",\"kpiId\":\"200202\",\"noOfResult\":2,\"threshold\":0,\"message\":{\"contentMessage\":\"Successful Deployments captured in last run were {totalDeploymentCount}\"},\"isActive\":\"TRUE\"}";
		registerGrafanaContentJson = convertStringIntoJson(registerGrafanaContent);
		
		String grafanaReportTemplate = "{\"reportName\":\"Grafana_PDFReport_test\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"GRAFANAPDF\",\"kpiConfigs\":[{\"kpiId\":200202,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"MATCH (n:KPI:RESULTS) where n.assessmentId = {assessmentId} and n.kpiId={kpiId} RETURN  n.`ISSUE API` as `ISSUE API`, n.`TOOL NAME` as `TOOL NAME`, n.`STATUS` as `STATUS`, n.`Key` as `Key`\",\"vType\":\"table_200202\"}]}]}";
		grafanaPDFreportTemplateJson = convertStringIntoJson(grafanaReportTemplate);
		
		String reportTemplateWithoutKPIDs = "{\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"visualizationutil\":\"FUSION\",\"isActive\":true}";
		reportTemplateWithoutKPIsJson = convertStringIntoJson(reportTemplateWithoutKPIDs);

		String reportTemplateWithoutExistingKPIDs = "{\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"isActive\":true,\"visualizationutil\":\"Fusion\",\"kpiConfigs\":[{\"kpiId\":1,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"\"}]}]}";
		reportTemplateWithoutExistingKPIDsJson = convertStringIntoJson(reportTemplateWithoutExistingKPIDs);

		String incorrectReportTemplate = "{\"reportName\":\"Productivity_test\",\"description\":\"Backend Team\",\"isActive\":true,\"visualizationutil\":\"FUSION\"}";
		incorrectReportTemplateJson = convertStringIntoJson(incorrectReportTemplate);
	}

	public void assessmentReportDataInit() {
		dailyExpectedAssessmentStartDate = 0L;
		dailyExpectedAssessmentEndDate = 0L;
		dailyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailyAssessmentReportJson = convertStringIntoJson(dailyAssessmentReport);

		weeklyExpectedAssessmentStartDate = 0L;
		weeklyExpectedAssessmentEndDate = 0L;
		weeklyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "WEEKLY");
		weeklyAssessmentReportJson = convertStringIntoJson(weeklyAssessmentReport);

		monthlyExpectedAssessmentStartDate =0L;
		monthlyExpectedAssessmentEndDate = 0L;
		monthlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "MONTHLY");
		monthlyAssessmentReportJson = convertStringIntoJson(monthlyAssessmentReport);

		quarterlyExpectedAssessmentStartDate = 0L;
		quarterlyExpectedAssessmentEndDate = 0L;
		quarterlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "QUARTERLY");
		quarterlyAssessmentReportJson = convertStringIntoJson(quarterlyAssessmentReport);

		yearlyExpectedAssessmentStartDate = 0L;
		yearlyExpectedAssessmentEndDate = 0L;
		yearlyExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "YEARLY");
		yearlyAssessmentReportJson = convertStringIntoJson(yearlyAssessmentReport);

		oneTimeExpectedAssessmentStartDate = getStartDate("2020-07-01T00:00:00Z");
		oneTimeExpectedAssessmentEndDate = getEndDate("2020-07-03T00:00:00Z");
		oneTimeExpectedNextRun = 0L;
		oneTimeAssessmentReportJson = convertStringIntoJson(oneTimeAssessmentReport);
		oneTimeAssessmentROIReportJson = convertStringIntoJson(oneTimeAssessmentROIReport);

		biWeeklyExpectedAssessmentStartDate = getStartDate("2020-07-01T00:00:00Z");
		biWeeklyExpectedAssessmentEndDate = 0L;
		biWeeklyExpectedNextRun = getNextRunTime(biWeeklyExpectedAssessmentStartDate, "BI_WEEKLY_SPRINT");
		biWeeklyAssessmentReportJson = convertStringIntoJson(biWeeklyAssessmentReport);

		triWeeklyExpectedAssessmentStartDate = getStartDate("2020-06-02T00:00:00Z");
		triWeeklyExpectedAssessmentEndDate = 0L;
		triWeeklyExpectedNextRun = getNextRunTime(triWeeklyExpectedAssessmentStartDate, "TRI_WEEKLY_SPRINT");
		triWeeklyAssessmentReportJson = convertStringIntoJson(triWeeklyAssessmentReport);

		triWeeklyExpectedAssessmentStartDateWithDataSource = getStartDate("2020-06-02T00:00:00Z");
		triWeeklyExpectedAssessmentEndDateWithDataSource = 0L;
		triWeeklyExpectedNextRunWithDataSource = getNextRunTime(triWeeklyExpectedAssessmentStartDate,
				"TRI_WEEKLY_SPRINT");
		triWeeklyAssessmentWithDataSourceReportJson = convertStringIntoJson(triWeeklyAssessmentReportWithDataSource);

		incorrectAssessmentReportJson = convertStringIntoJson(incorrectAssessmentReport);

		updateIncorrectAssessmentReport = "{\"isReoccuring\":true,\"emailList\":\"dasdsd\",\"id\":896}";
		updateAssessmentReportIncorrectJson = convertStringIntoJson(updateIncorrectAssessmentReport);

		oneTimeAssessmentReportWithStartDateGreaterThanEndDateJson = convertStringIntoJson(
				oneTimeAssessmentReportWithStartDateGreaterThanEndDate);

		dailyEmailExpectedAssessmentStartDate = 0L;
		dailyEmailExpectedAssessmentEndDate = 0L;
		dailyEmailExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailyEmailAssessmentReportJson = convertStringIntoJson(dailyEmailAssessmentReport);

		dailyRestartAssessmentReport = convertStringIntoJson(dailyRestartAssessmentReportStr);
		
		dailyRunImmediateAssessmentReport = convertStringIntoJson(dailyRunImmediateAssessmentReportStr);
		
		dailywithoutEmailExpectedAssessmentStartDate = 0L;
		dailywithoutEmailExpectedAssessmentEndDate = 0L;
		dailywithoutEmailExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailywithoutEmailAssessmentReportJson = convertStringIntoJson(dailywithoutEmailAssessmentReport);
		
		String dailywithoutEmaildetailsAssessmentReport = "{\"reportName\":\"Email_without_email_details_service_test\",\"asseementreportdisplayname\":\"Email details not present\",\"reportTemplate\":" + reportIdForList + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID + ",\"sequence\":1},{\"taskId\":" + emailtaskID
				+ ",\"sequence\":2}],\"emailDetails\":null,\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		dailywithoutEmaildetailsAssessmentReportJson = convertStringIntoJson(dailywithoutEmaildetailsAssessmentReport);
		
		String dailywithoutEmailtaskAssessmentReport = "{\"reportName\":\"Report_without_email_task_service_test\",\"asseementreportdisplayname\":\"Report email task not present\",\"reportTemplate\":" + reportIdForList + ",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":false,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0},{\"taskId\":" + pdftaskID
				+ ",\"sequence\":1}],\"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail\",\"receiverEmailAddress\":\"demo123@abc.com\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"},\"emailList\":\"abcd@abcd.com\",\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		
		dailywithoutEmailtaskAssessmentReportJson = convertStringIntoJson(dailywithoutEmailtaskAssessmentReport);

		dailyEmailCCExpectedAssessmentStartDate = 0L;
		dailyEmailCCExpectedAssessmentEndDate = 0L;
		dailyEmailCCExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailyEmailCCAssessmentReportJson = convertStringIntoJson(dailyEmailCcAssessmentReport);

		dailyEmailBCCExpectedAssessmentStartDate = 0L;
		dailyEmailBCCExpectedAssessmentEndDate = 0L;
		dailyEmailBCCExpectedNextRun = getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), "DAILY");
		dailyEmailBCCAssessmentReportJson = convertStringIntoJson(dailyEmailBCCAssessmentReport);
	}

	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = JsonUtils.parseStringAsJsonObject(convertregisterkpi);
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
		JsonArray array = JsonUtils.parseStringAsJsonArray(filecontent);
		for (JsonElement element : array) {
			int kpiId = element.getAsJsonObject().get("kpiId").getAsInt();
			kpiIdList.add(kpiId);
		}
	}

	public void readFileAndgetContentIdList(String filename) throws IOException {
		File File = new File(classLoader.getResource(filename).getFile());
		String filecontent = new String(Files.readAllBytes(File.toPath()));
		JsonArray array =JsonUtils.parseStringAsJsonArray(filecontent);
		for (JsonElement element : array) {
			int kpiId = element.getAsJsonObject().get("contentId").getAsInt();
			contentIdList.add(kpiId);
		}
	}
	String[] templateDesignFilesArray = {"report_template_save.json", "report_template_save.html", "style.css", "image.webp"};
	String[] templateDesignFilesArrayError = {"reportTemplateError.xml"};
	
	public MultipartFile[] readReportTemplateDesignFiles(int size, String[] templateDesignFilesArray) throws IOException {
		MultipartFile[] files = new MultipartFile[size];
		int i = 0;
		for (String eachFile : templateDesignFilesArray) {
			File file = new File(classLoader.getResource("report_template_save/"+eachFile).getFile());
			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
					IOUtils.toByteArray(input));
			files[i] = multipartFile;
			i++;
		}
		return files;	
	}
	
	public int getGrafanaOrgId(String userName) throws InsightsCustomException{
		String orgName = ORG_NAME_PREFIX + userName;
		int orgId = -1;
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
				+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
		String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
		headers.put(AuthenticationUtils.AUTH_HEADER_KEY, BASICAUTH + encodedString);
		
		// check if organization exists
		String orgResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.API_ORGS + "name/" + orgName, headers);

		if (!orgResponse.contains("id")) {
			JsonObject request = new JsonObject();
			request.addProperty(NAME, orgName);
			orgResponse = grafanaHandler.grafanaPost(PlatformServiceConstants.API_ORGS, request, headers);
			JsonObject orgResponseJson = JsonUtils.parseStringAsJsonObject(orgResponse);
			orgId = orgResponseJson.get("orgId").getAsInt();
		} else {
			JsonObject orgResponseJson = JsonUtils.parseStringAsJsonObject(orgResponse);
			orgId = orgResponseJson.get("id").getAsInt();
		}
		return orgId;
	}
	
	public void deleteGrafanaOrgId(String id) throws InsightsCustomException{
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
				+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
		String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
		headers.put(AuthenticationUtils.AUTH_HEADER_KEY, BASICAUTH + encodedString);
		grafanaHandler.grafanaDelete(PlatformServiceConstants.API_ORGS + id, headers);
	 }
	
	 public String grafanaDelete(String path, Map<String, String> headers) throws InsightsCustomException{
		String response = null;
		String GRAFANA_END_POINT = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
		try {
			String url = GRAFANA_END_POINT + path;
			response = RestApiHandler.doDelete(url, headers);
		} catch (InsightsCustomException e) {
			throw e;
		}
		return response;
	}
	
	public JsonObject deleteGrafanaAPIToken(int orgId) throws InsightsCustomException {
		String response = null;
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
				+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
		String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
		headers.put(AuthenticationUtils.AUTH_HEADER_KEY, "Basic " + encodedString);
		headers.put("x-grafana-org-id", String.valueOf(orgId));

		//check if API key exists in Grafana
		String id="";
		boolean keyExists = false;
		String getApiKeyResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.API_AUTH_KEYS, headers);
		JsonArray getApiKeyResponseObj = JsonUtils.parseStringAsJsonArray(getApiKeyResponse);
		for(JsonElement item: getApiKeyResponseObj) {
			if(item.getAsJsonObject().get(NAME).getAsString().equalsIgnoreCase(PDFTOKEN)) {
				keyExists = true;
				id = item.getAsJsonObject().get("id").getAsString();
			}
		}
		if(keyExists) {	
			String path = PlatformServiceConstants.API_AUTH_KEYS;
			 path = path + "/" +id;
			 String GRAFANA_END_POINT = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
			 try {
				String url = GRAFANA_END_POINT + path;
				response = RestApiHandler.doDelete(url, headers);
			 } catch (InsightsCustomException e) {
				return PlatformServiceUtil.buildFailureResponse(e.getMessage());
			}
		}
	  return PlatformServiceUtil.buildSuccessResponseWithData(response);
	
	}
}
