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

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.ContentConfigEnum;
import com.cognizant.devops.platformcommons.core.enums.KpiConfigEnum;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
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
			kpiId = registerkpiJson.get(AssessmentReportAndWorkflowConstants.KPIID).getAsInt();
			InsightsKPIConfig kpiExistingConfig = reportConfigDAL.getKPIConfig(kpiId);
			if (kpiExistingConfig != null) {
				throw new InsightsCustomException("KPI already exists");
			}
			boolean isActive = registerkpiJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
			String kpiName = registerkpiJson.get("name").getAsString();
			String dBQuery = registerkpiJson.get("DBQuery").getAsString();
			String resultField = registerkpiJson.get("resultField").getAsString();
			String group = registerkpiJson.get("group").getAsString();
			String toolName = registerkpiJson.get("toolName").getAsString();
			String dataSource = registerkpiJson.get(AssessmentReportAndWorkflowConstants.DATASOURCE).getAsString();
			String category = registerkpiJson.get("category").getAsString();
			dBQuery = dBQuery.replace("#", "<").replace("~", ">");
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
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return kpiId;

	}

	/**
	 * Used to update individual KPI definition
	 * 
	 * @param registerkpiJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int updateKpiDefinition(JsonObject updatekpiJson) throws InsightsCustomException {
		int kpiId = -1;
		try {
			kpiId = updatekpiJson.get(AssessmentReportAndWorkflowConstants.KPIID).getAsInt();
			InsightsKPIConfig kpiExistingConfig = reportConfigDAL.getKPIConfig(kpiId);
			if (kpiExistingConfig == null) {
				throw new InsightsCustomException("While update, KPI definition not exists");
			} else {
				boolean isActive = updatekpiJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
				String kpiName = updatekpiJson.get("name").getAsString();
				String dBQuery = updatekpiJson.get("DBQuery").getAsString();
				String resultField = updatekpiJson.get("resultField").getAsString();
				String group = updatekpiJson.get("group").getAsString();
				String toolName = updatekpiJson.get("toolName").getAsString();
				String dataSource = updatekpiJson.get(AssessmentReportAndWorkflowConstants.DATASOURCE).getAsString();
				String category = updatekpiJson.get("category").getAsString();
				dBQuery = dBQuery.replace("#", "<").replace("~", ">");
				kpiExistingConfig.setKpiId(kpiId);
				kpiExistingConfig.setActive(isActive);
				kpiExistingConfig.setKpiName(kpiName);
				kpiExistingConfig.setdBQuery(dBQuery);
				kpiExistingConfig.setResultField(resultField);
				kpiExistingConfig.setToolname(toolName);
				kpiExistingConfig.setGroupName(group);
				kpiExistingConfig.setDatasource(dataSource);
				kpiExistingConfig.setCategory(category);
				reportConfigDAL.updateKpiConfig(kpiExistingConfig);
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return kpiId;
	}

	/**
	 * Used to delete individual KPI definition
	 * 
	 * @param registerkpiJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public boolean deleteKpiDefinition(JsonObject deletekpiJson) throws InsightsCustomException {
		int kpiId = -1;
		boolean isRecordDeleted = Boolean.FALSE;
		try {
			kpiId = deletekpiJson.get(AssessmentReportAndWorkflowConstants.KPIID).getAsInt();
			InsightsKPIConfig kpiExistingConfig = reportConfigDAL.getKPIConfig(kpiId);
			if (kpiExistingConfig == null) {
				throw new InsightsCustomException("KPI definition not exists");
			} else {
				List<InsightsReportsKPIConfig> reportKPIList = reportConfigDAL.getActiveReportTemplateByKPIId(kpiId);
				if (reportKPIList.isEmpty()) {
					reportConfigDAL.deleteContentbyKPIID(kpiId);
					reportConfigDAL.deleteKPIbyKpiID(kpiId);
					isRecordDeleted = Boolean.TRUE;
				} else {
					throw new InsightsCustomException("KPI definition attached to report template");
				}
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return isRecordDeleted;
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
			int kpiId = registerContentJson.get(AssessmentReportAndWorkflowConstants.KPIID).getAsInt();
			contentId = registerContentJson.get(AssessmentReportAndWorkflowConstants.CONTENTID).getAsInt();
			boolean isActive = registerContentJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
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
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return contentId;
	}

	/**
	 * Used to store update content definition
	 * 
	 * @param registerContentJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int updateContentDefinition(JsonObject updateContentJson) throws InsightsCustomException {
		int contentId = -1;
		Gson gson = new Gson();
		try {
			InsightsContentConfig contentConfig = new InsightsContentConfig();
			int kpiId = updateContentJson.get(AssessmentReportAndWorkflowConstants.KPIID).getAsInt();
			contentId = updateContentJson.get(AssessmentReportAndWorkflowConstants.CONTENTID).getAsInt();
			boolean isActive = updateContentJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
			String contentName = updateContentJson.get("contentName").getAsString();
			String contentString = gson.toJson(updateContentJson);
			contentConfig.setContentId(contentId);
			InsightsKPIConfig kpiConfig = reportConfigDAL.getKPIConfig(kpiId);
			if (kpiConfig == null) {
				throw new InsightsCustomException("KPI defination not exists");
			}
			InsightsContentConfig contentDBConfig = reportConfigDAL.getContentConfig(contentId);
			if (contentDBConfig == null) {
				throw new InsightsCustomException("Content Definition does not  exists");
			} else {
				String category = kpiConfig.getCategory();
				contentDBConfig.setKpiConfig(kpiConfig);
				contentDBConfig.setActive(isActive);
				contentDBConfig.setContentJson(contentString);
				contentDBConfig.setContentName(contentName);
				contentDBConfig.setCategory(category);
				reportConfigDAL.updateContentConfig(contentDBConfig);
			}
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
		String returnMessage = "";
		String originalFilename = file.getOriginalFilename();
		JsonParser jsonParser = new JsonParser();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("json")) {
				String kpiJson = readFileAndCreateJson(file);
				JsonArray kpiJsonArray = jsonParser.parse(kpiJson).getAsJsonArray();
				returnMessage = saveBulkKpiDefinition(kpiJsonArray);
			} else {
				log.error("Invalid file format. ");
				throw new InsightsCustomException("Invalid file format.");
			}
		} catch (Exception ex) {
			log.error("Error in uploading KPI file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
		return returnMessage;
	}

	/**
	 * Method to save Kpi during Bulk save
	 * 
	 * @param jsonElement
	 * @param kpiResultList
	 * @return
	 */
	public String saveBulkKpiDefinition(JsonArray kpiJsonArray) {
		String returnMessage;
		int totalKpiRecord = kpiJsonArray.size();
		JsonArray successMessageArray = new JsonArray();
		JsonArray errorMessageArray = new JsonArray();
		if (totalKpiRecord > 0) {
			for (JsonElement jsonElement : kpiJsonArray) {
				int kpiId = jsonElement.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.KPIID).getAsInt();
				try {
					kpiId = saveKpiDefinition(jsonElement.getAsJsonObject());
					successMessageArray.add(kpiId);
				} catch (Exception e) {
					log.error("Error : KPI Id {} not created, with Exception {}", kpiId, e.getMessage());
					errorMessageArray.add(kpiId);
				}
			}
			if (successMessageArray.size() == totalKpiRecord) {
				returnMessage = "All KPI records are inserted successfully. ";
			} else if (errorMessageArray.size() == totalKpiRecord) {
				returnMessage = "All KPI records are not inserted, Please check Platform Service log for more detail. ";
			} else {
				returnMessage = "Number of KPI inserted successfully are " + successMessageArray.size()
						+ " and not inserted are " + errorMessageArray.size()
						+ ", Please check Platform Service log for more detail.";
			}
		} else {
			returnMessage = "No KPI defination found in request json";
		}
		return returnMessage;
	}

	/**
	 * used to create bulk content definition in DB
	 * 
	 * @param file
	 * @return
	 * @throws InsightsCustomException
	 */
	public String uploadContentInDatabase(MultipartFile file) throws InsightsCustomException {
		String returnMessage = "";
		String originalFilename = file.getOriginalFilename();
		JsonParser jsonParser = new JsonParser();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("json")) {
				String contentJson = readFileAndCreateJson(file);
				JsonArray contentJsonArray = jsonParser.parse(contentJson).getAsJsonArray();
				returnMessage = saveBulkContentDefinition(contentJsonArray);
			} else {
				log.error("Invalid file format.");
				throw new InsightsCustomException("Invalid file format.");
			}
		} catch (Exception ex) {
			log.error("Error in uploading Content file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
		return returnMessage;
	}

	/**
	 * Method to save Content during bulk save
	 * 
	 * @param jsonElement
	 * @param contentResultList
	 * @return StringBuilder
	 */
	public String saveBulkContentDefinition(JsonArray contentJsonArray) {
		String returnMessage;
		int totalContentRecord = contentJsonArray.size();
		JsonArray successMessageArray = new JsonArray();
		JsonArray errorMessageArray = new JsonArray();
		if (totalContentRecord > 0) {
			for (JsonElement jsonElement : contentJsonArray) {
				int contentId = jsonElement.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.CONTENTID)
						.getAsInt();
				try {
					contentId = saveContentDefinition(jsonElement.getAsJsonObject());
					successMessageArray.add(contentId);
				} catch (Exception e) {
					log.error("Error : Content Id {} not created, with Exception {}", contentId, e.getMessage());
					errorMessageArray.add(contentId);
				}
			}
			if (successMessageArray.size() == totalContentRecord) {
				returnMessage = "All Content records are inserted successfully.";
			} else if (errorMessageArray.size() == totalContentRecord) {
				returnMessage = "All Content records are not inserted, Please check Platform Service log for more detail.";
			} else {
				returnMessage = "Number of Content inserted successfully are " + successMessageArray.size()
						+ " and not inserted are " + errorMessageArray.size()
						+ ", Please check Platform Service log for more detail.";
			}
		} else {
			returnMessage = "No Content defination found in request json";
		}
		return returnMessage;
	}

	/**
	 * Read file and create Json String from it
	 * 
	 * @param file
	 * @return String
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
	 * @return int
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
	 * @return String
	 * @throws InsightsCustomException
	 */
	public String updateAssessmentReportState(JsonObject updateReportJsonValidated) throws InsightsCustomException {
		try {
			int assessmentReportId = updateReportJsonValidated.get("id").getAsInt();
			Boolean state = updateReportJsonValidated.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
			InsightsAssessmentConfiguration assessmentConfig = reportConfigDAL
					.getAssessmentByConfigId(assessmentReportId);
			InsightsWorkflowConfiguration workFlowObject = assessmentConfig.getWorkflowConfig();
			workFlowObject.setActive(state);
			assessmentConfig.setWorkflowConfig(workFlowObject);
			assessmentConfig.setActive(state);
			reportConfigDAL.updateAssessmentReportConfiguration(assessmentConfig, assessmentReportId);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while updating report status.", e);
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
		JsonObject emailDetails = null;
		try {
			InsightsAssessmentConfiguration assessmentConfig = reportConfigDAL
					.getAssessmentByConfigId(assessmentReportId);
			WorkflowServiceImpl workfowserice = new WorkflowServiceImpl();
			InsightsWorkflowConfiguration workFlowObject = assessmentConfig.getWorkflowConfig();
			workFlowObject.setReoccurence(
					assessmentReportJson.get(AssessmentReportAndWorkflowConstants.ISREOCCURING).getAsBoolean());
			JsonArray taskArray = assessmentReportJson.get("tasklist").getAsJsonArray();
			Set<InsightsWorkflowTaskSequence> taskSequenceSet = assessmentConfig.getWorkflowConfig()
					.getTaskSequenceEntity();
			taskSequenceSet = workfowserice.setSequence(taskArray, workFlowObject);
			workFlowObject.setTaskSequenceEntity(taskSequenceSet);
			if (!assessmentReportJson.get(AssessmentReportAndWorkflowConstants.EMAILDETAILS).isJsonNull()) {
				emailDetails = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.EMAILDETAILS)
						.getAsJsonObject();
				InsightsEmailTemplates emailTemplateConfig = workflowService.createEmailTemplateObject(emailDetails,
						workFlowObject);
				workFlowObject.setEmailConfig(emailTemplateConfig);
			} else {
				workFlowObject.setEmailConfig(null);
			}
			assessmentConfig.setWorkflowConfig(workFlowObject);
			reportConfigDAL.updateAssessmentReportConfiguration(assessmentConfig, assessmentReportId);
			return assessmentReportId;
		} catch (Exception e) {
			log.error("Error in updating the report.", e);
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
			for (InsightsAssessmentConfiguration assessmentReport : assessmentReportList) {
				JsonObject assessmentJson = createAssessmentReportJsonForUI(assessmentReport);
				jsonarray.add(assessmentJson);
			}
			return jsonarray;
		} catch (Exception e) {
			log.error("Error getting all report template..", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Method to create AssessmentReport JsonObject
	 * 
	 * @param assessmentReport
	 * @return JsonObject
	 */
	public JsonObject createAssessmentReportJsonForUI(InsightsAssessmentConfiguration assessmentReport) {
		JsonObject jsonobject = new JsonObject();
		jsonobject.addProperty("configId", assessmentReport.getId());
		jsonobject.addProperty(AssessmentReportAndWorkflowConstants.REPORTNAME,
				assessmentReport.getAsseementreportname());
		jsonobject.addProperty("asseementreportdisplayname", assessmentReport.getAsseementReportDisplayName());
		jsonobject.addProperty(AssessmentReportAndWorkflowConstants.ISACTIVE, assessmentReport.isActive());
		jsonobject.addProperty("inputDatasource", assessmentReport.getInputDatasource());
		jsonobject.addProperty(AssessmentReportAndWorkflowConstants.ISREOCCURING,
				assessmentReport.getWorkflowConfig().isReoccurence());

		InsightsAssessmentReportTemplate reporttemplate = assessmentReport.getReportTemplateEntity();
		JsonObject reportTemplateJsonObject = new JsonObject();
		reportTemplateJsonObject.addProperty("reportId", reporttemplate.getReportId());
		reportTemplateJsonObject.addProperty("templateName", reporttemplate.getTemplateName());
		jsonobject.add("template", reportTemplateJsonObject);

		InsightsWorkflowConfiguration workflowConfig = assessmentReport.getWorkflowConfig();
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
		jsonobject.addProperty(AssessmentReportAndWorkflowConstants.STATUS, status);
		long startdate = assessmentReport.getStartDate();
		String startDate = InsightsUtils.specficTimeFormat(startdate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		jsonobject.addProperty(AssessmentReportAndWorkflowConstants.STARTDATE, startDate);
		if (workflowConfig.getScheduleType().equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
			String endDate = InsightsUtils.specficTimeFormat(assessmentReport.getEndDate(),
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			jsonobject.addProperty("enddate", endDate);
		}
		jsonobject.addProperty(AssessmentReportAndWorkflowConstants.STARTDATE, startDate);
		String workflowId = workflowConfig.getWorkflowId();
		jsonobject.addProperty("workflowId", workflowId);
		jsonobject.addProperty(AssessmentReportAndWorkflowConstants.RUNIMMEDIATE, workflowConfig.isRunImmediate());
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
		InsightsEmailTemplates emailConfig = workflowConfig.getEmailConfig();
		JsonObject emailDetails = loadEmailDetailsForUI(emailConfig);
		jsonobject.add(AssessmentReportAndWorkflowConstants.EMAILDETAILS, emailDetails);
		return jsonobject;
	}

	/**
	 * Method to create Email details object for UI
	 * 
	 * @param emailConfig
	 * @return JsonObject
	 */
	public JsonObject loadEmailDetailsForUI(InsightsEmailTemplates emailConfig) {
		JsonObject emailDetails = new JsonObject();
		if (emailConfig != null) {
			String mailBody = emailConfig.getMailBody();
			mailBody = mailBody.replace("<", "#").replace(">", "~");
			emailDetails.addProperty("senderEmailAddress", emailConfig.getMailFrom());
			if (emailConfig.getMailTo() != null) {
				emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVEREMAILADDRESS,
						emailConfig.getMailTo());
			} else {
				emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVEREMAILADDRESS, "");
			}
			if (emailConfig.getMailCC() != null) {
				emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVERCCEMAILADDRESS,
						emailConfig.getMailCC());
			} else {
				emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVERCCEMAILADDRESS, "");
			}
			if (emailConfig.getMailBCC() != null) {
				emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVERBCCEMAILADDRESS,
						emailConfig.getMailBCC());
			} else {
				emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVERBCCEMAILADDRESS, "");
			}
			emailDetails.addProperty("mailSubject", emailConfig.getSubject());
			emailDetails.addProperty("mailBodyTemplate", mailBody);
		} else {
			emailDetails.addProperty("senderEmailAddress", "");
			emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVEREMAILADDRESS, "");
			emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVERCCEMAILADDRESS, "");
			emailDetails.addProperty(AssessmentReportAndWorkflowConstants.RECEIVERBCCEMAILADDRESS, "");
			emailDetails.addProperty("mailSubject", "");
			emailDetails.addProperty("mailBodyTemplate", "");
		}
		return emailDetails;
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
			log.error("Error while deleting assesment report.", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Method to save workflow configuration and create assessment report object
	 * 
	 * @param assessmentReportJson
	 * @return
	 * @throws InsightsCustomException
	 */
	private InsightsAssessmentConfiguration populateAssessmentReportConfiguration(JsonObject assessmentReportJson)
			throws InsightsCustomException {
		int reportId = -1;
		JsonObject emailDetails = null;
		try {
			log.debug(" Assessment Json to be saved {} ", assessmentReportJson);
			reportId = assessmentReportJson.get("reportTemplate").getAsInt();
			InsightsAssessmentReportTemplate reportTemplate = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getActiveReportTemplateByReportId(reportId);
			if (reportTemplate == null) {
				throw new InsightsCustomException(" report template is not available for report ID: " + reportId);
			}
			String workflowType = WorkflowTaskEnum.WorkflowType.REPORT.getValue();
			long epochStartDate;
			long epochEndDate;
			String reportName = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.REPORTNAME).getAsString();
			InsightsAssessmentConfiguration assessmentReport = reportConfigDAL
					.getAssessmentByAssessmentName(reportName);
			if (assessmentReport != null) {
				throw new InsightsCustomException("Assessment Report with the given Report name already exists");
			}
			String emailList = assessmentReportJson.get("emailList").getAsString();
			boolean isActive = false;
			String schedule = assessmentReportJson.get("schedule").getAsString();
			String datasource = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.DATASOURCE).getAsString();
			boolean reoccurence = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.ISREOCCURING)
					.getAsBoolean();
			boolean runImmediate = false;
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
			String workflowId = WorkflowTaskEnum.WorkflowType.REPORT.getValue() + "_"
					+ InsightsUtils.getCurrentTimeInSeconds();
			InsightsAssessmentConfiguration assessmentConfig = new InsightsAssessmentConfiguration();
			InsightsWorkflowConfiguration workflowConfig = workflowService.saveWorkflowConfig(workflowId, isActive,
					reoccurence, schedule, reportStatus, workflowType, taskList, epochStartDate, emailDetails,
					runImmediate);
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
			workflowConfig.setAssessmentConfig(assessmentConfig);
			return assessmentConfig;
		} catch (NoResultException e) {
			log.error(e);
			throw new InsightsCustomException("Report Template not found for report id : " + reportId);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Method to get Schedule
	 * 
	 * @return List<String>
	 */
	public List<String> getSchedule() {
		List<String> listOfSchedule = new ArrayList<>();
		WorkflowTaskEnum.WorkflowSchedule[] jobSchedules = WorkflowTaskEnum.WorkflowSchedule.values();
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
			reportId = templateReportJson.get("reportId").getAsInt();
			InsightsAssessmentReportTemplate reportEntity = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getActiveReportTemplateByReportId(reportId);

			if (reportEntity == null) {
				reportEntity = new InsightsAssessmentReportTemplate();
				saveReportConfig(templateReportJson, reportEntity, reportId);
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
	 * Method to save Report Template Configuration
	 * 
	 * @param templateReportJson
	 * @param reportEntity
	 * @param reportId
	 * @throws InsightsCustomException
	 */
	public void saveReportConfig(JsonObject templateReportJson, InsightsAssessmentReportTemplate reportEntity,
			int reportId) throws InsightsCustomException {
		List<Integer> kpiIds = new ArrayList<>();
		Set<InsightsReportsKPIConfig> reportsKPIConfigSet = new HashSet<>();
		String reportName = templateReportJson.get(AssessmentReportAndWorkflowConstants.REPORTNAME).getAsString();
		boolean isActive = templateReportJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
		String description = templateReportJson.get("description").getAsString();
		String file = templateReportJson.get("file").getAsString();
		String visualizationutil = templateReportJson.get("visualizationutil").getAsString();
		reportEntity.setReportId(reportId);
		reportEntity.setActive(isActive);
		reportEntity.setDescription(description);
		reportEntity.setTemplateName(reportName);
		reportEntity.setFile(file);
		reportEntity.setVisualizationutil(visualizationutil);

		if (!templateReportJson.has("kpiConfigs")) {
			throw new InsightsCustomException(" no KPI config provided for report : " + reportId);
		}

		JsonArray kpiConfigArray = templateReportJson.get("kpiConfigs").getAsJsonArray();

		for (JsonElement eachKpiConfig : kpiConfigArray) {
			JsonObject kpiObject = eachKpiConfig.getAsJsonObject();
			int kpiId = kpiObject.get(AssessmentReportAndWorkflowConstants.KPIID).getAsInt();
			if (kpiIds.contains(kpiId)) {
				log.debug(" Kpi id already exists with the reportId {} ", reportId);
			} else {
				InsightsKPIConfig kpiConfig = reportConfigDAL.getKPIConfig(kpiId);
				if (kpiConfig == null) {
					throw new NoResultException("kpi Id does not exist in dB");
				}
				kpiIds.add(kpiId);
				String vConfig = (kpiObject.get("visualizationConfigs").getAsJsonArray()).toString();
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
			log.error("Error getting all report template..", e);
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
				jsonobject.addProperty(AssessmentReportAndWorkflowConstants.KPIID, kpiDetail.getKpiId());
				jsonobject.addProperty("kpiName", kpiDetail.getKpiName());
				jsonarray.add(jsonobject);
			}
			return jsonarray;

		} catch (Exception e) {
			log.error("Error while deleting assesment report.", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Set the status to RETRY
	 * 
	 * @param configId
	 * @return
	 * @throws InsightsCustomException
	 */
	public String setReportStatus(JsonObject reportConfigJson) throws InsightsCustomException {
		try {
			int assessmentReportId = reportConfigJson.get("configId").getAsInt();
			InsightsAssessmentConfiguration assessmentConfig = reportConfigDAL
					.getAssessmentByConfigId(assessmentReportId);
			InsightsWorkflowConfiguration workFlowObject = assessmentConfig.getWorkflowConfig();
			if (reportConfigJson.has(AssessmentReportAndWorkflowConstants.STATUS)) {
				String status = reportConfigJson.get(AssessmentReportAndWorkflowConstants.STATUS).getAsString();
				workFlowObject.setStatus(status);
			} else if (reportConfigJson.has(AssessmentReportAndWorkflowConstants.RUNIMMEDIATE)) {
				boolean runImmediate = reportConfigJson.get(AssessmentReportAndWorkflowConstants.RUNIMMEDIATE)
						.getAsBoolean();
				workFlowObject.setRunImmediate(runImmediate);
				assessmentConfig.setActive(true);
				workFlowObject.setActive(true);
			}
			assessmentConfig.setWorkflowConfig(workFlowObject);
			reportConfigDAL.updateAssessmentReportConfiguration(assessmentConfig, assessmentReportId);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while updating report status.", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Method use to return KPI category
	 * 
	 * @return
	 */
	public List<String> getKpiCategory() {
		List<String> listOfCategory = new ArrayList<>();
		KpiConfigEnum.KpiCategory[] categories = KpiConfigEnum.KpiCategory.values();
		for (KpiConfigEnum.KpiCategory category : categories) {
			listOfCategory.add(category.toString().toUpperCase());
		}
		return listOfCategory;
	}

	/**
	 * Method use to return KPI datasource
	 * 
	 * @return
	 */
	public List<String> getKpiDataSource() {
		List<String> listOfDatasource = new ArrayList<>();
		KpiConfigEnum.KpiDatasourse[] datasources = KpiConfigEnum.KpiDatasourse.values();
		for (KpiConfigEnum.KpiDatasourse kpidatasource : datasources) {
			listOfDatasource.add(kpidatasource.toString().toUpperCase());
		}
		return listOfDatasource;
	}

	/**
	 * API use to return all active KPI list
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public List<InsightsKPIConfig> getActiveKpiList() throws InsightsCustomException {
		List<InsightsKPIConfig> kpiconfigList = new ArrayList<>();
		try {
			kpiconfigList = reportConfigDAL.getAllActiveKpiConfig();
			return kpiconfigList;
		} catch (Exception e) {
			log.error("Error getting all kpiList...", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Method use to return Content Action
	 * 
	 * @return
	 */
	public List<String> getContentAction() {
		List<String> listOfCategory = new ArrayList<>();
		ContentConfigEnum.ExecutionActions[] categories = ContentConfigEnum.ExecutionActions.values();
		for (ContentConfigEnum.ExecutionActions category : categories) {
			listOfCategory.add(category.toString().toUpperCase());
		}
		return listOfCategory;
	}

	/**
	 * API use to return all active Content list
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public List<JsonObject> getAllActiveContentList() throws InsightsCustomException {
		List<InsightsContentConfig> kpiconfigList = new ArrayList<>();
		List<JsonObject> contentCustomList = new ArrayList<>();
		JsonParser jsonParser = new JsonParser();
		try {
			kpiconfigList = reportConfigDAL.getAllActiveContentList();
			kpiconfigList.stream().forEach(contentDBData -> {
				JsonObject contentData = jsonParser.parse(contentDBData.getContentJson()).getAsJsonObject();
				contentData.addProperty("kpiName", contentDBData.getKpiConfig().getKpiName());
				contentData.addProperty("contentId", contentDBData.getContentId());
				contentData.addProperty("contentName", contentDBData.getContentName());
				contentData.addProperty("kpiId", contentDBData.getKpiConfig().getKpiId());
				contentData.addProperty("category", contentDBData.getKpiConfig().getCategory());

				contentCustomList.add(contentData);
			});
			return contentCustomList;
		} catch (Exception e) {
			log.error("Error getting all kpiList...", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Used to delete individual Content definition
	 * 
	 * @param registerkpiJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public boolean deleteContentDefinition(JsonObject deleteContentJson) throws InsightsCustomException {
		int ContentId = -1;
		boolean isRecordDeleted = Boolean.FALSE;
		try {
			ContentId = deleteContentJson.get(AssessmentReportAndWorkflowConstants.CONTENTID).getAsInt();
			InsightsContentConfig contentExistingConfig = reportConfigDAL.getContentConfig(ContentId);
			if (contentExistingConfig == null) {
				throw new InsightsCustomException("Content definition not exists");
			} else {
				List<InsightsReportsKPIConfig> reportKPIList = reportConfigDAL
						.getActiveReportTemplateByKPIId(contentExistingConfig.getKpiConfig().getId());
				if (reportKPIList.isEmpty()) {
					reportConfigDAL.deleteContentbyContentID(ContentId);
					isRecordDeleted = Boolean.TRUE;
				} else {
					throw new InsightsCustomException("Content definition attached to report template");
				}

				isRecordDeleted = Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return isRecordDeleted;
	}
}
