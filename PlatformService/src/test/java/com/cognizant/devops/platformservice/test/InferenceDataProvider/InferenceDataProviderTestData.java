/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.InferenceDataProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
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
import org.testng.annotations.BeforeClass;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
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
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class InferenceDataProviderTestData {
	private static Logger log = LogManager.getLogger(InferenceDataProviderTest.class);
	
    static JsonObject testData = new JsonObject();
	
	@BeforeClass
	public void beforeMethod() throws InsightsCustomException {
		try {
			String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
                    + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "InferenceDataProvider.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
			
		} catch (Exception e) {
			log.error("Error preparing data at InferenceDataProviderTestData record ", e);
		}
	}
	
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowDAL workflowDAL = new WorkflowDAL();
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
	Map<String, String> testAuthData = new HashMap<>();
	Map<String, String> headers = new HashMap<>();
	int reportIdProdRT = 300600;
	int reportIdSonarRT = 300601;
	
	int reportIdkpisRT = 300603;
	int workflowTypeId = 0;
	boolean deleteWorkflowType = false;
	String grafanaPDFExportUrl = null;
	String smtpHostServer = null;
	int id;
	String taskSystemEmailNotificationExecution = "";
	String assessmentReportWithEmail="";
	String host = null;
	Gson gson = new Gson();
	int typeId = 0;
	int reportTypeId = 0;

	String mqChannelKpiExecution = "TEST.WORKFLOW.TASK.KPI.EXCECUTION";
	String mqChannelPDFExecution = "TEST.WORKFLOW.TASK.PDF.EXCECUTION";
	String mqChannelEmailExecution = "TEST.WORKFLOW.TASK.EMAIL.EXCECUTION";
	String mqChannelSystemHealthNotificationExecution = "TEST.WORKFLOW.SYSTEM_TASK.SYSTEMNOTIFICATION.EXECUTION";
	String mqChannelSystemEmailExecution = "TEST.WORKFLOW.SYSTEM_TASK.EMAIL.EXECUTION";

	public static String workflowIdProd = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000567276";
	public static String workflowIdWithEmail = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000567999";
	public static String workflowIdWithoutEmail = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000568296";
	public static String workflowIdFail = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10000640327";
	public static String workflowIdWrongkpi = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "10083556935";
	public static String workflowIdWrongkpis = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_" + "1000083563542";
    public static String healthNotificationWorkflowId = WorkflowTaskEnum.WorkflowType.SYSTEM.getValue() + "_" + "HealthNotificationTest";
	public static long nextRunDaily;
	public static long nextRunBiWeekly;
	private static final String NAME = "name";
	private static final String PDFTOKEN = "Testpdftoken";
	private static final String ADMIN = "Admin";
	private static final String ROLE = "role";

	String querySonar = "MATCH (n:SONAR:DATA) where n.SPKstartTime > 1593561600 and n.SPKstartTime < 1596239999 and n.SPKstatus='Success' RETURN  COALESCE(Avg(toInt(n.SPKcomplexity)),0) as AvgComplexityCoverage";
	String queryJira = "MATCH (n:JIRA:DATA) WHERE n.SPKstartTime > 1593561600 and n.SPKstartTime < 1596239999 and n.SPKissueType='Bug' and n.SPKstatus='Closed'  RETURN count(n) as ClosedDefect";
	String queryJenkins = "MATCH (n:JENKINS:DATA) WHERE n.SPKstartTime > 1593561600 and n.SPKstartTime < 1596239999 and n.SPKvector = 'BUILD' and n.SPKstatus='Success' RETURN COALESCE(Max(toInt(n.SPKduration)),0) as MaxBuildTime";
	String queryJiraAvg = "MATCH (n:JIRA:DATA) WHERE n.SPKstartTime > 1593561600 and n.SPKstartTime < 1596239999 and n.SPKissueType='Bug' and n.SPKstatus='Closed'  RETURN COALESCE(Avg(toInt(n.SPKduration)),0) as AvgDefectCompletionTime";

	public static List<Integer> reportIdList = new ArrayList<>();
	public static List<Integer> taskidList = new ArrayList<>();
	public static List<Integer> contentIdList = new ArrayList<>();
	public static List<Integer> kpiIdList = new ArrayList<>();
	String username;
	String credential;
	
	String[] templateDesignFilesArray = {"/Insights/PlatformReports/src/main/resources/templates/Configs/Report_Grafana_Inference/Report_Grafana_Inference_RT.json", "/Insights/PlatformReports/src/main/resources/templates/Configs/REPORT_TEMPLATE_JSON_AND_HTML_FOR_GENERAL_KPIS/REPORT_SONAR_JENKINS_PROD.html", "/Insights/PlatformReports/src/main/resources/templates/Configs/REPORT_ALL_CAT/style.css", "/Insights/PlatformReports/src/main/resources/templates/Configs/REPORT_ALL_CAT/image.webp"};

	
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
	
	public void uploadReportTemplateFile(int reportId, InsightsAssessmentReportTemplate reportEntity, String fileType,
			String filename, byte[] file) {
		InsightsReportTemplateConfigFiles templateFile = reportConfigDAL
				.getReportTemplateConfigFileByFileNameAndReportId(filename,
						reportEntity.getReportId());
		if (templateFile == null) {
			InsightsReportTemplateConfigFiles record = new InsightsReportTemplateConfigFiles();
			record.setFileName(filename);
			record.setFileData(file);
			record.setFileType(fileType);
			record.setReportId(reportId);
			reportConfigDAL.saveReportTemplateConfigFiles(record);
		} else {
			templateFile.setFileData(file);
			reportConfigDAL.updateReportTemplateConfigFiles(templateFile);
		}
	}
	public JsonObject fetchTemplateJson(String filename) throws InsightsCustomException {
		JsonObject templateJson = null;
		try {
			File templateFile = new File(getClass().getClassLoader().getResource(filename).getFile());
			String template = new String(Files.readAllBytes(templateFile.toPath()));
			templateJson = JsonUtils.parseStringAsJsonObject(template);
		} catch (Exception e) {
			log.error("Error while fetching template JSON ====", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return templateJson;
	}
	public static InsightsConfigFiles createConfigFileData() {
		InsightsConfigFiles configFile = new InsightsConfigFiles();
		configFile.setFileName("Table");
		configFile.setFileType("JSON");
		configFile.setFileModule("GRAFANA_PDF_TEMPLATE");
		configFile.setFileData(testData.get("tableData").toString().getBytes());
		return configFile;
	}

	public JsonObject preparePanelJson(String vType, String vQuery, String title, int id) throws InsightsCustomException {
			JsonObject panelTemplateJson = null;
			try {
				InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
				byte[] panelTemplateByte = configFilesDAL.getConfigurationFile(vType).getFileData();
				String template = new String(panelTemplateByte);
				panelTemplateJson = JsonUtils.parseStringAsJsonObject(template);
				panelTemplateJson.addProperty("title",title);
				panelTemplateJson.addProperty("id",id);
				panelTemplateJson.addProperty("datasource", "Neo4j Data Source");
				panelTemplateJson.getAsJsonArray(AssessmentReportAndWorkflowConstants.TARGETS).get(0).getAsJsonObject()
				.addProperty(AssessmentReportAndWorkflowConstants.QUERYTEXT, vQuery);
				
			} catch (Exception e) {
				log.error("Error while preparing panel JSON ====", e);
				throw new InsightsCustomException(e.getMessage());
			}
			return panelTemplateJson;
		}
	
	public JsonObject prepareContentPanelJson(String vType, String title, boolean isContentEmpty, int kpiId, int id ) throws InsightsCustomException {
			JsonObject panelTemplateJson = null;
			String vQuery = "";
			try {
				panelTemplateJson = fetchTemplateJson(vType+".json");
				panelTemplateJson.addProperty("title",title);
				panelTemplateJson.addProperty("id",id);
				panelTemplateJson.addProperty("datasource", "Neo4j Data Source");
				if(!isContentEmpty) {
					vQuery = panelTemplateJson.getAsJsonArray("targets").get(0).getAsJsonObject().get("queryText").getAsString(); 
					vQuery = vQuery.replace("{kpiId}", String.valueOf(kpiId));
				}
				panelTemplateJson.getAsJsonArray("targets").get(0).getAsJsonObject().addProperty("queryText", vQuery);
				
			} catch (Exception e) {
				log.error("Error while preparing panel JSON ====", e);
				throw new InsightsCustomException(e.getMessage());
			}
			return panelTemplateJson;
		}
	public JsonObject createDashboardJson(JsonObject templateReportJson) throws InsightsCustomException {
		JsonObject dashboardTemplateJson = null;
		try {
			dashboardTemplateJson = fetchTemplateJson("Report_Template_Inference.json");
			JsonArray kpiConfigArray = templateReportJson.get(AssessmentReportAndWorkflowConstants.KPICONFIGS)
					.getAsJsonArray();
			JsonArray panelsArray = new JsonArray();
			int panelId = 0 ;
			for (JsonElement eachKpiConfig : kpiConfigArray) {
				JsonObject kpiObject = eachKpiConfig.getAsJsonObject();
				int kpiId = kpiObject.get(AssessmentReportAndWorkflowConstants.KPIID).getAsInt();
				List<InsightsContentConfig> contentList = reportConfigDAL.getActiveContentConfigByKPIId(kpiId);
				
				// Add panel for KPI 
				panelId = panelId + 1; 
				JsonArray vConfigs = kpiObject.get("visualizationConfigs").getAsJsonArray();
				String vtype = vConfigs.get(0).getAsJsonObject().get(AssessmentReportAndWorkflowConstants.VTYPE).getAsString();
				vtype = vtype.substring(0, vtype.lastIndexOf('_')); 
				String vQuery = vConfigs.get(0).getAsJsonObject().get(AssessmentReportAndWorkflowConstants.VQUERY).getAsString();
				vQuery = vQuery.replace("{kpiId}", String.valueOf(kpiId));
				InsightsKPIConfig kpiConfig = reportConfigDAL.getKPIConfig(kpiId);
				JsonObject panelTemplateJson = preparePanelJson(vtype, vQuery, kpiConfig.getKpiName(), panelId);
				panelsArray.add(panelTemplateJson);
				
				// Add panel for content 
				panelId = panelId + 1; 
				vtype = "content";
				String panelName = "Observation of ".concat(kpiConfig.getKpiName());
				boolean isContentEmpty = contentList.isEmpty();
				
				JsonObject panelContentTemplateJson = prepareContentPanelJson(vtype, panelName, isContentEmpty ,kpiId, panelId);
				panelsArray.add(panelContentTemplateJson);
			}
			
			dashboardTemplateJson.get("dashboard").getAsJsonObject().add("panels", panelsArray);
			log.debug("dashboard json ==={}", dashboardTemplateJson);
			
		} catch (Exception e) {
			log.error("Error while preparing dashboard JSON ====", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return dashboardTemplateJson;
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
				if(visualizationutil.equalsIgnoreCase(AssessmentReportAndWorkflowConstants.GRAFANAPDF)) {
					JsonObject dashboardTemplateJson = createDashboardJson(reportJson);
					byte[] dashboardTemplatebytes = dashboardTemplateJson.toString().getBytes();
					uploadReportTemplateFile(reportId, reportEntity, "JSON", "dashboardTemplate.json",dashboardTemplatebytes);
				}
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
	
	public void populateDataWithEmail() {
		String From= testAuthData.get("From");
		String To= testAuthData.get("TO");
		assessmentReportWithEmail = "{\"reportName\":\"report_Email_test10002154\",\"reportTemplate\":" + reportIdProdRT + ",\"emailList\":\"demo123@gmail.com\",\"schedule\":\"BI_WEEKLY_SPRINT\",\"startdate\":\"2020-05-12T00:00:00Z\",\"isReoccuring\":true,\"datasource\":\"\",\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\": {\"senderEmailAddress\":\""+ From +"\",\"receiverEmailAddress\":\""+ To +"\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"Sub_mail\",\"mailBodyTemplate\":\"sending a mail for report\"}}";	
	}
	
	public Map<String, String> getGrafanaHeaders(int orgId) {
		GrafanaOrgToken grafanaOrgToken = grafanaDashboardConfigDAL.getTokenByOrgId(orgId);
		String token = "Bearer "+AES256Cryptor.decrypt(grafanaOrgToken.getApiKey(), AssessmentReportAndWorkflowConstants.GRAFANA_PDF_TOKEN_SIGNING_KEY);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", token);
		return headers;
	}
	public String saveDashbaordInGrafana(InsightsAssessmentReportTemplate reportTemplate,String reportName) throws InsightsCustomException {
		String responseorg = null ;
		try {
			List<InsightsReportTemplateConfigFiles> records = reportConfigDAL.getReportTemplateConfigFileByReportId(reportTemplate.getReportId());

			JsonObject requestOrg = new JsonObject();

			for (InsightsReportTemplateConfigFiles insightsReportTemplateConfigFiles : records) {
				if (insightsReportTemplateConfigFiles.getFileName().equalsIgnoreCase(AssessmentReportAndWorkflowConstants.DASHBOARDTEMPLATEJSON)) {
					String dashboardJson = new String(insightsReportTemplateConfigFiles.getFileData());
					requestOrg = JsonUtils.parseStringAsJsonObject(dashboardJson);
				}
			}

			requestOrg.get(AssessmentReportAndWorkflowConstants.DASHBOARD).getAsJsonObject().addProperty(AssessmentReportAndWorkflowConstants.TITLE, reportName);
			JsonArray panelArray = requestOrg.get("dashboard").getAsJsonObject().get("panels").getAsJsonArray();
			for (JsonElement jsonElement : panelArray) {
				String vQuery = jsonElement.getAsJsonObject().getAsJsonArray(AssessmentReportAndWorkflowConstants.TARGETS).get(0).getAsJsonObject()
						.get(AssessmentReportAndWorkflowConstants.QUERYTEXT).getAsString();
				vQuery = vQuery.replace("{assessmentReportName}", "'"+reportName+"'");
				jsonElement.getAsJsonObject().getAsJsonArray(AssessmentReportAndWorkflowConstants.TARGETS).get(0).getAsJsonObject()
						.addProperty(AssessmentReportAndWorkflowConstants.QUERYTEXT, vQuery);
			}
			responseorg = createDashboardInGrafana(requestOrg);
			log.debug(" responseorg {} ", responseorg);
			
		} catch (Exception e) {
			log.error("Error while saving dashboard in Grafana.", e);
			throw new InsightsCustomException(e.getMessage());
		}

		return responseorg;
	}
	public String createDashboardInGrafana(JsonObject requestDashboardObj) throws InsightsCustomException, Exception {
		int orgId =Integer.parseInt(testAuthData.get("org_id"));
		JsonObject dashboardApiResponseObj = null;
		try {
			String title = requestDashboardObj.get("dashboard").getAsJsonObject().get("title").getAsString();
			String dashboardApiResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.SEARCH_DASHBOARD_PATH + title , getGrafanaHeaders(orgId));
			if(dashboardApiResponse.equalsIgnoreCase("[]")) {
				dashboardApiResponse = grafanaHandler.grafanaPost(PlatformServiceConstants.API_DASHBOARD_PATH, requestDashboardObj, getGrafanaHeaders(orgId));
				log.debug(" dashboardApiResponse {} ", dashboardApiResponse);
				dashboardApiResponseObj = JsonUtils.parseStringAsJsonObject(dashboardApiResponse);
			} else {
				dashboardApiResponseObj = JsonUtils.parseStringAsJsonArray(dashboardApiResponse).get(0).getAsJsonObject();
			}
			dashboardApiResponseObj.addProperty("orgId", orgId);
		} catch (Exception e) {
			log.error("Error while creating dashboard.", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return dashboardApiResponseObj.toString();
	}
	
	public void generateGrafanaToken() throws InsightsCustomException {
		try {				
			int orgId = Integer.parseInt(testAuthData.get("org_id"));
			GrafanaOrgToken grafanaOrgToken = new GrafanaOrgToken();
			//httpRequest.addHeader("Authorization", testAuthData.get(AUTHORIZATION));
			headers.put("Content-Type", "application/json");
			log.debug(testAuthData);
			username=testAuthData.get("username");
			credential=testAuthData.get("password");
			String basicAuth = username+":"+credential;
			String basicAuthHeader =  "Basic " + new String(Base64.getEncoder().encode(basicAuth.getBytes()));
			headers.put("Authorization",basicAuthHeader);
			headers.put("x-grafana-org-id", String.valueOf(orgId));
			boolean keyExists = false;
			String getApiKeyResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.API_AUTH_KEYS, headers);
			JsonArray getApiKeyResponseObj = JsonUtils.parseStringAsJsonArray(getApiKeyResponse);
			for(JsonElement item: getApiKeyResponseObj) {
				if(item.getAsJsonObject().get(NAME).getAsString().equalsIgnoreCase(PDFTOKEN)) {
					keyExists = true;
					id=item.getAsJsonObject().get("id").getAsInt();
				}
			}
			if(keyExists) {
				grafanaHandler.grafanaDelete("/api/auth/keys/" + id ,
						headers);
			}
			JsonObject json = new JsonObject();
			json.addProperty(NAME, PDFTOKEN);
			json.addProperty(ROLE, ADMIN);
			String response = grafanaHandler.grafanaPost(PlatformServiceConstants.API_AUTH_KEYS,json, headers);
			JsonObject apiObj = JsonUtils.parseStringAsJsonObject(response);
			grafanaOrgToken.setOrgId(orgId);
			grafanaOrgToken.setApiKey(AES256Cryptor.encrypt(apiObj.get("key").getAsString(), AssessmentReportAndWorkflowConstants.GRAFANA_PDF_TOKEN_SIGNING_KEY));		
			grafanaDashboardConfigDAL.saveGrafanaOrgToken(grafanaOrgToken);
					 
		}catch (Exception e) {
			log.error("Unable to generate Grafana token  {}", e.getMessage());
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	public String saveAssessmentReport(String workflowid, String assessmentReport, int noOftask)
			throws InsightsCustomException {
		int reportId = -1;
		JsonObject emailDetails = null;
		JsonObject responseJson = new JsonObject();
		JsonObject assessmentReportJson = addTask(assessmentReport, noOftask);
		try {
			log.debug(" Assessment Json to be saved {} ", assessmentReportJson);
			reportId = assessmentReportJson.get("reportTemplate").getAsInt();
			String reportName = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.REPORTNAME).getAsString();
			InsightsAssessmentReportTemplate reportTemplate = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getActiveReportTemplateByReportId(reportId);
			if (reportTemplate == null) {
				throw new InsightsCustomException(" report template is not available for report ID: " + reportId);
			} 
			
			String workflowType = WorkflowTaskEnum.WorkflowType.REPORT.getValue();
			long epochStartDate;
			long epochEndDate;
			
			String emailList = assessmentReportJson.get("emailList").getAsString();
			boolean isActive = true;
			String schedule = assessmentReportJson.get("schedule").getAsString();
			String datasource = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.DATASOURCE).getAsString();
			boolean reoccurence = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.ISREOCCURING)
					.getAsBoolean();
			boolean runImmediate = Boolean.FALSE;
			String asseementreportdisplayname = assessmentReportJson.get("asseementreportdisplayname").getAsString();
			if (!assessmentReportJson.get(AssessmentReportAndWorkflowConstants.EMAILDETAILS).isJsonNull()) {
				emailDetails = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.EMAILDETAILS)
						.getAsJsonObject();
			}
			JsonElement startDateJsonObject = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.STARTDATE);
			if (startDateJsonObject.isJsonNull()) {
				epochStartDate = 0;
			} else {
				epochStartDate = InsightsUtils.getEpochTime(startDateJsonObject.getAsString()) / 1000;
				epochStartDate = InsightsUtils.getStartOfTheDay(epochStartDate) + 1;
	
			}
			if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
				JsonElement endDateJsonObject = assessmentReportJson.get("enddate");
				if (endDateJsonObject.isJsonNull()) {
					epochEndDate = 0;
				} else {
					epochEndDate = InsightsUtils.getEpochTime(endDateJsonObject.getAsString()) / 1000;
					epochEndDate = InsightsUtils.getStartOfTheDay(epochEndDate) - 1;
				}
				if (epochStartDate > epochEndDate) {
					throw new InsightsCustomException("Start Date cannot be greater than End date");
				}
			} else {
				epochEndDate = 0;
			}
			String reportStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString();
			JsonArray taskList = assessmentReportJson.get("tasklist").getAsJsonArray();
			String username = "admin";
			String orgname = "testgroup";
			InsightsAssessmentConfiguration assessmentConfig = new InsightsAssessmentConfiguration();
			InsightsWorkflowConfiguration workflowConfig = saveWorkflowConfig(workflowid, isActive, reoccurence,
					schedule, reportStatus, workflowType, taskList, epochStartDate, emailDetails,
					runImmediate);
	     	JsonObject dashboardPdfObj = new JsonObject();
		
			// Entity Setters
			assessmentConfig.setActive(isActive);
			assessmentConfig.setInputDatasource(datasource);
			assessmentConfig.setAsseementreportname(reportName);
			assessmentConfig.setEmails(emailList);
			assessmentConfig.setStartDate(epochStartDate);
			assessmentConfig.setEndDate(epochEndDate);
			assessmentConfig.setReportTemplateEntity(reportTemplate);
			assessmentConfig.setWorkflowConfig(workflowConfig);
			assessmentConfig.setAsseementReportDisplayName(asseementreportdisplayname);
			assessmentConfig.setUserName(username);
			assessmentConfig.setOrgName(orgname);
			workflowConfig.setAssessmentConfig(assessmentConfig);
			
			String vUtil = assessmentConfig.getReportTemplateEntity().getVisualizationutil();
			if (vUtil.equalsIgnoreCase(AssessmentReportAndWorkflowConstants.GRAFANAPDF)){
				String response = saveDashbaordInGrafana(reportTemplate,reportName);
				if(response !=  null) {
					JsonObject responseObj = JsonUtils.parseStringAsJsonObject(response);
					String dashboardUrlStr = responseObj.get("url").getAsString();
					String orgId = responseObj.get("orgId").getAsString();
					String dateParam  = "";
					if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
						dateParam = "&from="+epochStartDate*1000+"&to="+epochEndDate*1000;
					}else {
						dateParam = "&from="+WorkflowTaskEnum.GrafanaPDFScheduleMapping.valueOf(schedule).getValue()+"&to=now";
					}
					dashboardUrlStr = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint().concat(dashboardUrlStr.substring(dashboardUrlStr.indexOf("/d"))) +"?orgId="+ orgId+dateParam;
					log.debug(" dashboardUrlStr {} ",dashboardUrlStr);
					dashboardPdfObj.addProperty("dashUrl", dashboardUrlStr);
					dashboardPdfObj.addProperty("workflowId", assessmentConfig.getWorkflowConfig().getWorkflowId());
					dashboardPdfObj.addProperty("variables", "");
					dashboardPdfObj.addProperty(AssessmentReportAndWorkflowConstants.DASHBOARD, responseObj.get("uid").getAsString());
					dashboardPdfObj.addProperty("theme", "dark");
					dashboardPdfObj.addProperty("pdfType", "Dashboard");
					dashboardPdfObj.addProperty(AssessmentReportAndWorkflowConstants.TITLE, assessmentConfig.getAsseementReportDisplayName());
					dashboardPdfObj.addProperty("source", "PLATFORM");
					dashboardPdfObj.addProperty("loadTime", "90");
					dashboardPdfObj.addProperty("organisation", orgId);
					responseJson.addProperty("dashboardUrl", dashboardUrlStr);
				} else {
					responseJson.addProperty("dashboardResponse", "unable to save dashboard in Grafana.");
				}
			}
			
			log.debug(" dashboardPdfObj {} ",dashboardPdfObj);
			
			assessmentConfig.setAdditionalDetail(dashboardPdfObj.toString());
			int assessmentReportId = reportConfigDAL.saveInsightsAssessmentConfig(assessmentConfig);
			//workflowDAL.saveInsightsWorkflowConfig(workflowConfig);
			responseJson.addProperty("assessmentReportId", assessmentReportId);
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