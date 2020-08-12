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
package com.cognizant.devops.platformservice.assessmentreport.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("assessmentReportService")
public class AssesmentReportServiceImpl {
	private static final Logger log = LogManager.getLogger(AssesmentReportServiceImpl.class);
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	WorkflowServiceImpl workflowService = new WorkflowServiceImpl();

	/* Kpi and Content service methods */

	/**
	 * Used to store individual KPI definition
	 * 
	 * @param registerkpiJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int saveKpiDefinition(JsonObject registerkpiJson) throws InsightsCustomException {
		int kpiId = -1;
		try {
			InsightsKPIConfig kpiConfig = new InsightsKPIConfig();
			kpiId = registerkpiJson.get("kpiId").getAsInt();
			InsightsKPIConfig kpiExistingConfig = reportConfigDAL.getKPIConfig(kpiId);
			if (kpiExistingConfig != null) {
				throw new InsightsCustomException("KPI already exists");
			}
			boolean isActive = registerkpiJson.get("isActive").getAsBoolean();
			String kpiName = registerkpiJson.get("name").getAsString();
			String dBQuery = registerkpiJson.get("DBQuery").getAsString();
			String resultField = registerkpiJson.get("resultField").getAsString();
			String group = registerkpiJson.get("group").getAsString();
			String toolName = registerkpiJson.get("toolName").getAsString();
			String dataSource = registerkpiJson.get("datasource").getAsString();
			String category = registerkpiJson.get("category").getAsString();
			kpiConfig.setKpiId(kpiId);
			kpiConfig.setActive(isActive);
			kpiConfig.setKpiName(kpiName);
			kpiConfig.setdBQuery(dBQuery);
			kpiConfig.setResultField(resultField);
			kpiConfig.setToolname(toolName);
			kpiConfig.setGroupName(group);
			kpiConfig.setDatasource(dataSource);
			kpiConfig.setCategory(category);
			reportConfigDAL.saveKpiConfig(kpiConfig);
		} catch (NullPointerException e) {
			log.error(e);
			throw new InsightsCustomException("kpi Definition does not have some mandatory field");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return kpiId;

	}

	/**
	 * Used to store individual content definition
	 * 
	 * @param registerContentJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int saveContentDefinition(JsonObject registerContentJson) throws InsightsCustomException {
		int contentId = -1;
		Gson gson = new Gson();
		try {
			InsightsContentConfig contentConfig = new InsightsContentConfig();
			int kpiId = registerContentJson.get("kpiId").getAsInt();
			contentId = registerContentJson.get("contentId").getAsInt();
			boolean isActive = registerContentJson.get("isActive").getAsBoolean();
			String contentName = registerContentJson.get("contentName").getAsString();

			String contentString = gson.toJson(registerContentJson);
			contentConfig.setContentId(contentId);
			InsightsKPIConfig kpiConfig = reportConfigDAL.getKPIConfig(kpiId);
			if (kpiConfig == null) {
				throw new InsightsCustomException("KPI not exists");
			}
			InsightsContentConfig contentDBConfig = reportConfigDAL.getContentConfig(contentId);
			if (contentDBConfig != null) {
				throw new InsightsCustomException("Content Definition already exists");
			}
			String category = kpiConfig.getCategory();
			contentConfig.setKpiConfig(kpiConfig);
			contentConfig.setActive(isActive);
			contentConfig.setContentJson(contentString);
			contentConfig.setContentName(contentName);
			contentConfig.setCategory(category);
			reportConfigDAL.saveContentConfig(contentConfig);
		} catch (NullPointerException e) {
			log.error(e);
			throw new InsightsCustomException("Content Definition does not have some mandatory field");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return contentId;
	}

	/**
	 * Used to read KPI definition from file and store it in DB
	 * 
	 * @param file
	 * @return
	 * @throws InsightsCustomException
	 */
	public String uploadKPIInDatabase(MultipartFile file) throws InsightsCustomException {
		StringBuilder kpiResultList = new StringBuilder();
		String originalFilename = file.getOriginalFilename();

		JsonParser jsonParser = new JsonParser();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("json")) {
				String kpiJson = readFileAndCreateJson(file);
				JsonArray kpiJsonArray = jsonParser.parse(kpiJson).getAsJsonArray();
				for (JsonElement jsonElement : kpiJsonArray) {
					int kpiId = jsonElement.getAsJsonObject().get("kpiId").getAsInt();
					try {
						kpiId = saveKpiDefinition(jsonElement.getAsJsonObject());
						kpiResultList.append("==== Success:KPI Id(" + kpiId + ") created.");
					} catch (Exception e) {
						kpiResultList.append(
								" ==== Error : KPI Id (" + kpiId + ") not created, with Exception " + e.getMessage());
					}
				}
			} else {
				log.error(" KPI Detail {} ", kpiResultList);
				throw new InsightsCustomException("Invalid file format.");
			}
		} catch (Exception ex) {
			log.error(" KPI Detail {} ", kpiResultList);
			log.error("Error in uploading KPI file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
		return kpiResultList.toString();
	}

	/**
	 * used to create bulk content definition in DB
	 * 
	 * @param file
	 * @return
	 * @throws InsightsCustomException
	 */
	public String uploadContentInDatabase(MultipartFile file) throws InsightsCustomException {
		StringBuilder contentResultList = new StringBuilder();
		String originalFilename = file.getOriginalFilename();
		JsonParser jsonParser = new JsonParser();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("json")) {
				String contentJson = readFileAndCreateJson(file);
				JsonArray contentJsonArray = jsonParser.parse(contentJson).getAsJsonArray();
				for (JsonElement jsonElement : contentJsonArray) {
					int contentId = jsonElement.getAsJsonObject().get("contentId").getAsInt();
					try {
						contentId = saveContentDefinition(jsonElement.getAsJsonObject());
						contentResultList.append(" ====  Suceess : Content Id(" + contentId + ") created. ");
					} catch (Exception e) {
						contentResultList.append(" ==== Error : Content Id (" + contentId
								+ ") not created, with Exception " + e.getMessage());
					}
				}
			} else {
				log.error(" Content Detail {} ", contentResultList);
				throw new InsightsCustomException("Invalid file format.");
			}
		} catch (Exception ex) {
			log.error(" Content  Detail {} ", contentResultList);
			log.error("Error in uploading Content file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
		return contentResultList.toString();
	}

	/**
	 * Read file and create Json String from it
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String readFileAndCreateJson(MultipartFile file) throws IOException {
		File configFile = convertToFile(file);
		StringBuilder json = new StringBuilder();
		if (configFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(configFile), 1024)) {
				String line;
				while ((line = reader.readLine()) != null) {
					json.append(line);
				}
			} catch (FileNotFoundException e) {
				log.error("Config file not found", e);
			} catch (IOException e) {
				log.error("Unable to read the file", e);
			}
		}
		return json.toString();
	}

	/**
	 * convert multipart to file
	 * 
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 */
	private File convertToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multipartFile.getBytes());
		}
		return file;
	}

	/**
	 * Method to save the details in ASSESSMENT_CONFIGURATION table.
	 * 
	 * @param assessmentReportJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int saveAssessmentReport(JsonObject assessmentReportJson) throws InsightsCustomException {
		int assessmentReportId;
		try {
			InsightsAssessmentConfiguration assessmentConfig = populateAssessmentReportConfiguration(
					assessmentReportJson);
			assessmentReportId = reportConfigDAL.saveInsightsAssessmentConfig(assessmentConfig);
			return assessmentReportId;
		} catch (InsightsCustomException e) {
			log.error("Error while saving the report .. {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}

	}

	/**
	 * Method to update the Active State in ASSESSMENT_CONFIGURATION table.
	 * 
	 * @param updateReportJsonValidated
	 * @return
	 * @throws InsightsCustomException
	 */
	public String updateAssessmentReportState(JsonObject updateReportJsonValidated) throws InsightsCustomException {
		try {
			int assessmentReportId = updateReportJsonValidated.get("id").getAsInt();
			Boolean state = updateReportJsonValidated.get("isActive").getAsBoolean();
			InsightsAssessmentConfiguration assessmentConfig = reportConfigDAL
					.getAssessmentByConfigId(assessmentReportId);
			InsightsWorkflowConfiguration workFlowObject = assessmentConfig.getWorkflowConfig();
			workFlowObject.setActive(state);
			assessmentConfig.setWorkflowConfig(workFlowObject);
			assessmentConfig.setActive(state);
			reportConfigDAL.updateAssessmentReportConfiguration(assessmentConfig, assessmentReportId);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while updating report status..{}", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Method to update the details in ASSESSMENT_CONFIGURATION table.
	 * 
	 * @param assessmentReportJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int updateAssessmentReport(JsonObject assessmentReportJson) throws InsightsCustomException {
		int assessmentReportId = assessmentReportJson.get("id").getAsInt();

		try {
			InsightsAssessmentConfiguration assessmentConfig = reportConfigDAL
					.getAssessmentByConfigId(assessmentReportId);
			WorkflowServiceImpl workfowserice = new WorkflowServiceImpl();
			// String workflowId = assessmentConfig.getWorkflowConfig().getWorkflowId();
			InsightsWorkflowConfiguration workFlowObject = assessmentConfig.getWorkflowConfig();
			workFlowObject.setReoccurence(assessmentReportJson.get("isReoccuring").getAsBoolean());
			// workFlowObject.setScheduleType(assessmentReportJson.get("schedule").getAsString());
			JsonArray taskArray = assessmentReportJson.get("tasklist").getAsJsonArray();
			Set<InsightsWorkflowTaskSequence> taskSequenceSet = assessmentConfig.getWorkflowConfig()
					.getTaskSequenceEntity();
			taskSequenceSet = workfowserice.setSequence(taskArray, workFlowObject);
			// workFlowObject.getTaskSequenceEntity().addAll(taskSequenceSet);
			workFlowObject.setTaskSequenceEntity(taskSequenceSet);
			assessmentConfig.setWorkflowConfig(workFlowObject);
			assessmentConfig.setEmails(assessmentReportJson.get("emailList").getAsString());
			reportConfigDAL.updateAssessmentReportConfiguration(assessmentConfig, assessmentReportId);
			return assessmentReportId;
		} catch (Exception e) {
			log.error("Error in updating the report.. {}", e);
			throw new InsightsCustomException(e.toString());
		}

	}

	/**
	 * Fetch all the reports from ASSESSMENT_CONFIGURATION table.
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonArray getAssessmentReportList() throws InsightsCustomException {
		try {
			List<InsightsAssessmentConfiguration> assessmentReportList = reportConfigDAL.getAllAssessmentConfig();
			JsonArray jsonarray = new JsonArray();
			for (InsightsAssessmentConfiguration reportTemplate : assessmentReportList) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.addProperty("configId", reportTemplate.getId());
				jsonobject.addProperty("reportName", reportTemplate.getAsseementreportname());
				long startdate = reportTemplate.getStartDate();
				InsightsAssessmentReportTemplate reporttemplate = reportTemplate.getReportTemplateEntity();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("reportId", reporttemplate.getReportId());
				jsonObject.addProperty("templateName", reporttemplate.getTemplateName());
				InsightsWorkflowConfiguration workflowConfig = reportTemplate.getWorkflowConfig();
				// JsonObject jsonObjectForWorkflow = new JsonObject();
				long lastRun = workflowConfig.getLastRun();
				long nextRun = workflowConfig.getNextRun();
				String status = workflowConfig.getStatus();
				if (status.equals(WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString()) || lastRun == 0) {
					jsonobject.addProperty("lastRun", "");
				} else {

					jsonobject.addProperty("lastRun", lastRun);
				}
				if (nextRun == 0) {
					jsonobject.addProperty("nextRun", "");
				} else {

					jsonobject.addProperty("nextRun", nextRun);
				}
				jsonobject.addProperty("schedule", workflowConfig.getScheduleType());
				jsonobject.addProperty("status", status);

				// array.add(jsonObject);
				String startDate = InsightsUtils.specficTimeFormat(startdate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				jsonobject.addProperty("isActive", reportTemplate.isActive());
				jsonobject.addProperty("emailAddress", reportTemplate.getEmails());
				jsonobject.addProperty("inputDatasource", reportTemplate.getInputDatasource());
				jsonobject.addProperty("startdate", startDate);
				jsonobject.addProperty("isReoccuring", reportTemplate.getWorkflowConfig().isReoccurence());
				if (workflowConfig.getScheduleType().equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
					String endDate = InsightsUtils.specficTimeFormat(reportTemplate.getEndDate(),
							"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					jsonobject.addProperty("enddate", endDate);
				}
				jsonobject.add("template", jsonObject);
				String workflowId = workflowConfig.getWorkflowId();
				JsonArray detailTask = new JsonArray();

				List<InsightsWorkflowTaskSequence> taskList = workflowConfigDAL
						.getAllWorkflowTaskSequenceByWorkflowId(workflowId);
				for (InsightsWorkflowTaskSequence eachTask : taskList) {
					JsonObject detailTaskJson = new JsonObject();
					detailTaskJson.addProperty("taskId", eachTask.getWorkflowTaskEntity().getTaskId());
					detailTaskJson.addProperty("description", eachTask.getWorkflowTaskEntity().getDescription());
					detailTask.add(detailTaskJson);

				}
				jsonobject.add("taskDesc", detailTask);
				jsonarray.add(jsonobject);
			}
			return jsonarray;
		} catch (Exception e) {
			log.error("Error getting all report template.. {}", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Delete the report from ASSESSMENT_CONFIGURATION table.
	 * 
	 * @param configId
	 * @return
	 * @throws InsightsCustomException
	 */
	public String deleteAssessmentReport(String configId) throws InsightsCustomException {
		try {
			int id = Integer.parseInt(configId);
			InsightsAssessmentConfiguration result = reportConfigDAL.getAssessmentConfigListByReportId(id);
			String workflowid = result.getWorkflowConfig().getWorkflowId();
			List<InsightsWorkflowExecutionHistory> historyConfig = workflowConfigDAL
					.getWorkflowExecutionHistoryByWorkflowId(workflowid);
			if (historyConfig.isEmpty()) {
				reportConfigDAL.deleteAssessmentReport(id);
			} else {
				throw new InsightsCustomException("Executions found in history");
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while deleting assesment report.{}", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	private InsightsAssessmentConfiguration populateAssessmentReportConfiguration(JsonObject assessmentReportJson)
			throws InsightsCustomException {
		int reportId = -1;
		try {
			reportId = assessmentReportJson.get("reportTemplate").getAsInt();
			InsightsAssessmentReportTemplate reportTemplate = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getActiveReportTemplateByReportId(reportId);
			if (reportTemplate == null) {
				throw new InsightsCustomException(" report template is not available for report ID: " + reportId);
			}
			String workflowType = WorkflowTaskEnum.WorkflowType.REPORT.getValue();
			long epochStartDate, epochEndDate;
			String reportName = assessmentReportJson.get("reportName").getAsString();
			boolean isActive = false;
			String schedule = assessmentReportJson.get("schedule").getAsString();
			String emailList = assessmentReportJson.get("emailList").getAsString();
			String datasource = assessmentReportJson.get("datasource").getAsString();
			boolean reoccurence = assessmentReportJson.get("isReoccuring").getAsBoolean();
			JsonElement startDateJsonObject = assessmentReportJson.get("startdate");
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
				if(epochStartDate > epochEndDate) {
					throw new InsightsCustomException("Start Date cannot be greater than End date");
				}
			} else {
				epochEndDate = 0;
			}
			String reportStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString();
			JsonArray taskList = assessmentReportJson.get("tasklist").getAsJsonArray();
			String workflowId = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_"
					+ InsightsUtils.getCurrentTimeInSeconds();
			InsightsAssessmentConfiguration assessmentConfig = new InsightsAssessmentConfiguration();
			InsightsWorkflowConfiguration workflowConfig = workflowService.saveWorkflowConfig(workflowId, isActive,
					reoccurence, schedule, reportStatus, workflowType, taskList, epochStartDate, epochEndDate);

			// Entity Setters
			assessmentConfig.setActive(isActive);
			assessmentConfig.setEmails(emailList);
			assessmentConfig.setInputDatasource(datasource);
			assessmentConfig.setAsseementreportname(reportName);
			assessmentConfig.setStartDate(epochStartDate);
			assessmentConfig.setEndDate(epochEndDate);
			assessmentConfig.setReportTemplateEntity(reportTemplate);
			assessmentConfig.setWorkflowConfig(workflowConfig);
			workflowConfig.setAssessmentConfig(assessmentConfig);
			return assessmentConfig;
		} catch (NullPointerException e) {
			log.error(e);
			throw new InsightsCustomException(" assessment report does not have some mandatory field ");
		} catch (NoResultException e) {
			log.error(e);
			throw new InsightsCustomException("Report Template not found for report id : " + reportId);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	public List<String> getSchedule() {
		List<String> listOfSchedule = new ArrayList<>();
		WorkflowTaskEnum.WorkflowSchedule jobSchedules[] = WorkflowTaskEnum.WorkflowSchedule.values();
		for (WorkflowTaskEnum.WorkflowSchedule jobSchedule : jobSchedules) {
			listOfSchedule.add(jobSchedule.toString().toUpperCase());
		}
		return listOfSchedule;
	}

	// REPORT TEMPLATE TABLE RELATED METHODS FOR SERVICE

	/**
	 * Save report template details in ASSESSEMENT_REPORT_TEMPLATE table.
	 * 
	 * @param templateReportJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int saveTemplateReport(JsonObject templateReportJson) throws InsightsCustomException {
		int reportId = -1;
		try {
			List<Integer> kpiIds = new ArrayList<>();

			Set<InsightsReportsKPIConfig> reportsKPIConfigSet = new HashSet<>();

			reportId = templateReportJson.get("reportId").getAsInt();
			InsightsAssessmentReportTemplate reportEntity = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getActiveReportTemplateByReportId(reportId);

			if (reportEntity == null) {
				String reportName = templateReportJson.get("reportName").getAsString();
				boolean isActive = templateReportJson.get("isActive").getAsBoolean();
				String description = templateReportJson.get("description").getAsString();
                String file = templateReportJson.get("file").getAsString();
				reportEntity = new InsightsAssessmentReportTemplate();
				reportEntity.setReportId(reportId);
				reportEntity.setActive(isActive);
				reportEntity.setDescription(description);
				reportEntity.setTemplateName(reportName);
				reportEntity.setFile(file);

				if (!templateReportJson.has("kpiConfigs")) {
					throw new InsightsCustomException(" no KPI config provided for report : " + reportId);
				}

				JsonArray kpiConfigArray = templateReportJson.get("kpiConfigs").getAsJsonArray();

				for (JsonElement eachKpiConfig : kpiConfigArray) {
					JsonObject KpiObject = eachKpiConfig.getAsJsonObject();
					int kpiId = KpiObject.get("kpiId").getAsInt();
					if (kpiIds.contains(kpiId)) {
						log.debug(" Kpi id already exists with the reportId " + reportId);
					} else {
						InsightsKPIConfig kpiConfig = reportConfigDAL.getKPIConfig(kpiId);
						if(kpiConfig == null) {
							throw new NoResultException("kpi Id does not exist in dB");
						}
						kpiIds.add(kpiId);
						String vConfig =(KpiObject.get("visualizationConfigs").getAsJsonArray()).toString();
						InsightsReportsKPIConfig reportsKPIConfig = new InsightsReportsKPIConfig();
						reportsKPIConfig.setKpiConfig(kpiConfig);
						reportsKPIConfig.setvConfig(vConfig);
						reportsKPIConfig.setReportTemplateEntity(reportEntity);
						reportsKPIConfigSet.add(reportsKPIConfig);
					}

				}

				reportEntity.setReportsKPIConfig(reportsKPIConfigSet);
				reportConfigDAL.saveReportConfig(reportEntity);

			} else {
				throw new InsightsCustomException(" report template already exists in database " + reportId);
			}

		} catch (NullPointerException e) {
			log.error(e);
			throw new InsightsCustomException(" report tempplate does not have some mandatory field ");
		} catch (NoResultException e) {
			log.error(e);
			throw new InsightsCustomException("Kpi id not found for attachning report : ");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return reportId;
	}

	/**
	 * Fetch all the report templates from ASSESSEMENT_REPORT_TEMPLATE table.
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public List<InsightsAssessmentReportTemplate> getReportTemplate() throws InsightsCustomException {
		try {
			return reportConfigDAL.getAllReportTemplates();
		} catch (Exception e) {
			log.error("Error getting all report template.. {}", e);
			throw new InsightsCustomException(e.toString());
		}

	}

	/**
	 * Fetch all the KPI List of the selected report template.
	 * 
	 * @param configId
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonArray getKPIlistOfReportTemplate(String configId) throws InsightsCustomException {
		try {
			int id = Integer.parseInt(configId);
			List<InsightsKPIConfig> listofkpis = reportConfigDAL.getKpiConfigByTemplateReportId(id);

			JsonArray jsonarray = new JsonArray();
			for (InsightsKPIConfig kpiDetail : listofkpis) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.addProperty("kpiId", kpiDetail.getKpiId());
				jsonobject.addProperty("kpiName", kpiDetail.getKpiName());
				jsonarray.add(jsonobject);
			}
			return jsonarray;

		} catch (Exception e) {
			log.error("Error while deleting assesment report.{}", e);
			throw new InsightsCustomException(e.toString());
		}
	}

}
