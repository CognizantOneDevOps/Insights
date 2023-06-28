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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ReportChartCollection;
import com.cognizant.devops.platformcommons.constants.ReportStatusConstants;
import com.cognizant.devops.platformcommons.core.enums.ContentConfigEnum;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.core.enums.KpiConfigEnum;
import com.cognizant.devops.platformcommons.core.enums.ReportTemplateTypeEnum;
import com.cognizant.devops.platformcommons.core.enums.VisualizationUtilEnum;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
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
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.saml.ResourceLoaderService;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service("assessmentReportService")
public class AssesmentReportServiceImpl {

	private static final Logger log = LogManager.getLogger(AssesmentReportServiceImpl.class);
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();

	@Autowired
	GrafanaUtilities grafanaUtilities;
	
	@Autowired
	ResourceLoaderService resourceLoaderService;

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
		String usecase = "";
		String outputDatasource = "";
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
			String category = registerkpiJson.get(AssessmentReportAndWorkflowConstants.CATEGORY).getAsString();
			if (registerkpiJson.has(AssessmentReportAndWorkflowConstants.USECASE)) {
				usecase = registerkpiJson.get(AssessmentReportAndWorkflowConstants.USECASE).getAsString();
			}
			if (registerkpiJson.has(AssessmentReportAndWorkflowConstants.OUTPUTDATASOURCE)) {
				outputDatasource = registerkpiJson.get(AssessmentReportAndWorkflowConstants.OUTPUTDATASOURCE)
						.getAsString();
			}
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
			kpiConfig.setUsecase(usecase);
			kpiConfig.setOutputDatasource(outputDatasource);
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
				String category = updatekpiJson.get(AssessmentReportAndWorkflowConstants.CATEGORY).getAsString();
				String usecase = updatekpiJson.get("usecase").getAsString();
				String outputDatasource = updatekpiJson.get(AssessmentReportAndWorkflowConstants.OUTPUTDATASOURCE)
						.getAsString();
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
				kpiExistingConfig.setUsecase(usecase);
				kpiExistingConfig.setOutputDatasource(outputDatasource);
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
			String contentName = registerContentJson.get(AssessmentReportAndWorkflowConstants.CONTENT_NAME)
					.getAsString();

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
			String contentName = updateContentJson.get(AssessmentReportAndWorkflowConstants.CONTENT_NAME).getAsString();
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
		String originalFilename = StringEscapeUtils.escapeHtml(ValidationUtils.cleanXSS(file.getOriginalFilename()));
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("json")) {
				String kpiJson = readMultipartFileAndCreateJson(file);
				JsonArray kpiJsonArray =  JsonUtils.parseStringAsJsonArray(kpiJson);
				returnMessage = saveBulkKpiDefinition(kpiJsonArray);
			} else {
				log.error("Invalid file format. ");
				throw new InsightsCustomException("Invalid kpi file format.");
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
		String originalFilename = StringEscapeUtils.escapeHtml(ValidationUtils.cleanXSS(file.getOriginalFilename()));
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("json")) {
				String contentJson = readMultipartFileAndCreateJson(file);
				JsonArray contentJsonArray =  JsonUtils.parseStringAsJsonArray(contentJson);
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
	 * Read multipart file and create Json String from it
	 * 
	 * @param file
	 * @return String
	 * @throws InsightsCustomException
	 */
	private String readMultipartFileAndCreateJson(MultipartFile file) throws InsightsCustomException {
		try {
			InputStream inputStream = file.getInputStream();
			StringBuilder json = new StringBuilder();
			new BufferedReader(new InputStreamReader(inputStream)).lines()
	        .forEach(json::append);
			
			return json.toString();
		} catch (Exception e) {
			log.error("Error while reading file {} ", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		
	}

	/**
	 * Method to save the details in ASSESSMENT_CONFIGURATION table.
	 * 
	 * @param assessmentReportJson
	 * @return int
	 * @throws InsightsCustomException
	 */
	public JsonObject saveAssessmentReport(JsonObject assessmentReportJson) throws InsightsCustomException {
		JsonObject responseJson = null;
		try {
			responseJson = populateAssessmentReportConfiguration(assessmentReportJson);
			
			return responseJson;
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
			Set<InsightsWorkflowTaskSequence> taskSequenceSetPrevious = workFlowObject.getTaskSequenceEntity();
			
			if(assessmentConfig.getReportTemplateEntity().getVisualizationutil().equalsIgnoreCase(AssessmentReportAndWorkflowConstants.GRAFANAPDF)) {
				Set<Integer> previousTaskId = taskSequenceSetPrevious.stream()
						.map(s -> s.getWorkflowTaskEntity().getTaskId()).collect(Collectors.toSet());
				Set<Integer> requestTaskId = new HashSet<>();
					taskArray.forEach(taskObj -> requestTaskId
						.add(taskObj.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt()));
				Set<Integer> requestDiffTask = ValidationUtils.differenceOfSet(previousTaskId, requestTaskId);
				log.debug(" previousTaskId {} requestTaskId {} requestDiffTask {}  ", previousTaskId, requestTaskId, requestDiffTask);
				if(!requestDiffTask.isEmpty()) {
					throw new InsightsCustomException(" For Grafana PDF type Report Template, Modifying Task is not allowed.");
				}
			}
			
			Set<InsightsWorkflowTaskSequence> taskSequenceSet = workfowserice.setSequence(taskArray, workFlowObject);
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
			String username = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.USERNAME).getAsString();
			String orgname = assessmentReportJson.get(AssessmentReportAndWorkflowConstants.ORGNAME).getAsString();
			assessmentConfig.setUserName(username);
			assessmentConfig.setOrgName(orgname);
			assessmentConfig.setWorkflowConfig(workFlowObject);
			reportConfigDAL.updateAssessmentReportConfiguration(assessmentConfig, assessmentReportId);			
			return assessmentReportId;
		} catch (Exception e) {
			log.error("Error in updating the report.", e);
			throw new InsightsCustomException(e.getMessage());
		}

	}

	/**
	 * Fetch all the reports from ASSESSMENT_CONFIGURATION table.
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonArray getAssessmentReportList(JsonObject userDetail) throws InsightsCustomException {
		try {
			String userName = userDetail.get("userName").getAsString();
			List<InsightsAssessmentConfiguration> assessmentReportList = reportConfigDAL.getAllAssessmentConfig();
			JsonArray jsonarray = new JsonArray();
			for (InsightsAssessmentConfiguration assessmentReport : assessmentReportList) {
				if (userName.equals(assessmentReport.getUserName()) || assessmentReport.getUserName() == null) {
					JsonObject assessmentJson = createAssessmentReportJsonForUI(assessmentReport);
					jsonarray.add(assessmentJson);
				}
			}
			return jsonarray;
		} catch (Exception e) {
			log.error("Error getting all assessment report template list ..", e);
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
		reportTemplateJsonObject.addProperty("templateType", reporttemplate.getTemplateType());
		jsonobject.add("template", reportTemplateJsonObject);
		jsonobject.addProperty(ReportStatusConstants.MILESTONE_ID, (assessmentReport.getMilestoneId()!= null ?assessmentReport.getMilestoneId():null));

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
		String startDate = InsightsUtils.insightsTimeXFormat(startdate * 1000);
		jsonobject.addProperty(AssessmentReportAndWorkflowConstants.STARTDATE, startDate);
		if (workflowConfig.getScheduleType().equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
			String endDate = InsightsUtils.insightsTimeXFormat(assessmentReport.getEndDate() * 1000);
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
			detailTaskJson.addProperty(AssessmentReportAndWorkflowConstants.TASK_DESCRIPTION,
					eachTask.getWorkflowTaskEntity().getDescription());
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

			if (result == null) {
				throw new InsightsCustomException("Report not found.");
			}
			long lastRun = result.getWorkflowConfig().getLastRun();
			long diff = InsightsUtils.getDifferenceFromLastRunTime(lastRun);
			if (lastRun == 0 || diff >= 100) {
				// delete dashboard from Grafana 
				String visualizationutil = result.getReportTemplateEntity().getVisualizationutil();	
				if (visualizationutil.equalsIgnoreCase(VisualizationUtilEnum.VisualizationUtil.GRAFANAPDF.name())
						&& result.getAdditionalDetail() != null && !result.getAdditionalDetail().equalsIgnoreCase("{}")) {
					JsonObject dashboardConfigJson = JsonUtils.parseStringAsJsonObject(result.getAdditionalDetail());
					grafanaUtilities.deleteDashboardFromGrafana(dashboardConfigJson);
				}

				// delete assessment report from database
				reportConfigDAL.deleteAssessmentReport(result);
			} else {
				throw new InsightsCustomException("This report is not 100 days old");
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
	private JsonObject populateAssessmentReportConfiguration(JsonObject assessmentReportJson)
			throws InsightsCustomException {
		int reportId = -1;
		JsonObject emailDetails = null;
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
			
			InsightsAssessmentConfiguration assessmentReport = reportConfigDAL
					.getAssessmentByAssessmentName(reportName);
			if (assessmentReport != null) {
				throw new InsightsCustomException("Assessment Report with the given Report name already exists");
			}
			String emailList = assessmentReportJson.get("emailList").getAsString();
			boolean isActive = false;
			int milestoneId = assessmentReportJson.has(ReportStatusConstants.MILESTONE_ID)?assessmentReportJson.get(ReportStatusConstants.MILESTONE_ID).getAsInt():0;
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
			epochStartDate = setEpochStartDate(startDateJsonObject);		
			JsonElement endDateJsonObject = assessmentReportJson.get("enddate");
			epochEndDate = setEpochEndDate(endDateJsonObject,epochStartDate,schedule);			
			String reportStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString();
			JsonArray taskList = assessmentReportJson.get("tasklist").getAsJsonArray();
			String username = assessmentReportJson.get("userName").getAsString();
			String orgname = assessmentReportJson.get("orgName").getAsString();
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
			assessmentConfig.setUserName(username);
			assessmentConfig.setOrgName(orgname);
			assessmentConfig.setMilestoneId(milestoneId);
			workflowConfig.setAssessmentConfig(assessmentConfig);
			
			boolean reportHasPDFTask = false;
			for(JsonElement item: taskList) {
				InsightsWorkflowTask taskConfig = workflowConfigDAL.getTaskByTaskId(item.getAsJsonObject().get("taskId").getAsInt());
				if(taskConfig.getCompnentName().contains("PDFExecutionSubscriber")) {
					reportHasPDFTask = true;
				}
			} 
			return getResponseJson(assessmentConfig,username, reportName, reportTemplate,reportHasPDFTask,schedule,epochStartDate,epochEndDate);
			
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}
	
	private long setEpochStartDate(JsonElement startDateJsonObject) {
		long epochStartDate;
		if (startDateJsonObject.isJsonNull()) {
			epochStartDate = 0;
		} else {
			epochStartDate = InsightsUtils.getEpochTime(startDateJsonObject.getAsString()) / 1000;
			epochStartDate = InsightsUtils.getStartOfTheDay(epochStartDate) + 1;

		}
		return epochStartDate;
	}
	
	private long setEpochEndDate(JsonElement endDateJsonObject, long epochStartDate, String schedule) throws InsightsCustomException {
		
		long epochEndDate;
		
		if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) { 
		
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
		
		return epochEndDate;
	}
	
	private JsonObject getResponseJson(InsightsAssessmentConfiguration assessmentConfig, String username,
			String reportName, InsightsAssessmentReportTemplate reportTemplate, boolean reportHasPDFTask, String schedule, long epochStartDate, long epochEndDate) throws InsightsCustomException {
		
		JsonObject responseJson = new JsonObject();
		JsonObject dashboardPdfObj = new JsonObject();
		
		String vUtil = assessmentConfig.getReportTemplateEntity().getVisualizationutil();
		if (vUtil.equalsIgnoreCase(AssessmentReportAndWorkflowConstants.GRAFANAPDF) && reportHasPDFTask){
			String response = saveDashbaordInGrafana(username, reportName, reportTemplate  );
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
		responseJson.addProperty("assessmentReportId", assessmentReportId);
		
		return responseJson;
		
		
	}

	private String saveDashbaordInGrafana(String userName, String reportName, InsightsAssessmentReportTemplate reportTemplate) throws InsightsCustomException {

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
			log.debug(" requestOrg {} ", requestOrg);
			responseorg = grafanaUtilities.createOrgAndSaveDashboardInGrafana(requestOrg, userName);
			log.debug(" responseorg {} ", responseorg);
			
		} catch (Exception e) {
			log.error("Error while saving dashboard in Grafana.", e);
			throw new InsightsCustomException(e.getMessage());
		}

		return responseorg;
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
	 * Save report template details in database table.
	 * 
	 * @param templateReportJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int saveTemplateReport(JsonObject templateReportJson) throws InsightsCustomException {
		int reportId = -1;
		try {
			reportId = (int) (System.currentTimeMillis() / 1000);
			String reportName = templateReportJson.get(AssessmentReportAndWorkflowConstants.REPORTNAME).getAsString();
			InsightsAssessmentReportTemplate reportEntity = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getActiveReportTemplateByName(reportName);

			if (reportEntity == null) {
				reportEntity = new InsightsAssessmentReportTemplate();
				saveReportConfig(templateReportJson, reportEntity, reportId);
			} else {
				throw new InsightsCustomException(
						" Report template already exists in database " + reportEntity.getReportId());
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return reportId;
	}

	/**
	 * Edit report template details in database table.
	 * 
	 * @param templateReportJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int editReportTemplate(JsonObject templateReportJson) throws InsightsCustomException {
		int reportId = -1;
		try {

			reportId = editReportConfig(templateReportJson);
		} catch (NoResultException e) {
			log.error(e);
			throw new InsightsCustomException("Report template not found ");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return reportId;
	}

	/**
	 * This files used to upload Report template in DB
	 * 
	 * @param file
	 * @return
	 * @throws InsightsCustomException
	 */
	public String uploadReportTemplate(MultipartFile file) throws InsightsCustomException {
		String returnMessage = "";
		String originalFilename = file.getOriginalFilename();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("json")) {
				String reportTemplateStr = readMultipartFileAndCreateJson(file);
				JsonObject reportTemplateJson =  JsonUtils.parseStringAsJsonObject(reportTemplateStr);
				int reportId = saveTemplateReport(reportTemplateJson);
				returnMessage = "Report Template Id created " + reportId;
			} else {
				log.error("Invalid Report Template file format. ");
				throw new InsightsCustomException("Invalid Report Template file format.");
			}
		} catch (Exception ex) {
			log.error("Error in Report Template file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
		return returnMessage;
	}

	/**
	 * Upload uploadReport Template Design Files
	 * 
	 * @param file
	 * @return
	 * @throws InsightsCustomException
	 */
	public String uploadReportTemplateDesignFiles(MultipartFile[] files, int reportId) throws InsightsCustomException {
		String returnMessage = "";
		try {
			InsightsAssessmentReportTemplate reportEntity = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getReportTemplateByReportId(reportId);
			if (reportEntity == null) {
				throw new InsightsCustomException(" Report template not exists in database " + reportId);
			} else {

				validateAndUploadReportTemplate(files,reportEntity,reportId);				
				returnMessage = "File uploaded";
			}
		} catch (Exception ex) {
			log.error("Error in Report Template files upload {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
		return returnMessage;
	}

	private void validateAndUploadReportTemplate(MultipartFile[] files, 
			InsightsAssessmentReportTemplate reportEntity, int reportId) throws IOException, InsightsCustomException {
		
		for (MultipartFile multipartfile : Arrays.asList(files)) {
			String fileName = multipartfile.getOriginalFilename();
			String fileExt = FilenameUtils.getExtension(fileName);
			if (AssessmentReportAndWorkflowConstants.validReportTemplateFileExtention
					.contains(fileExt.toLowerCase())) {
				boolean fileCheck = PlatformServiceUtil.validateFile(fileName);
				if (fileCheck) {
					String fileType = PlatformServiceUtil.getFileType(multipartfile.getOriginalFilename());
					InputStream is = multipartfile.getInputStream();
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				    int nRead;
				    byte[] data = new byte[4];
				    while ((nRead = is.read(data, 0, data.length)) != -1) {
				        buffer.write(data, 0, nRead);
				    }
				    buffer.flush();
					uploadReportTemplateFile(reportId, reportEntity, fileType, multipartfile.getOriginalFilename(), buffer.toByteArray());
				} else {
					log.error("File validation failed {}", multipartfile.getOriginalFilename());
					throw new InsightsCustomException(
							"File validation failed " + multipartfile.getOriginalFilename());
				}
			} else {
				log.error("Wrong file format for {}", multipartfile.getOriginalFilename());
				throw new InsightsCustomException(
						"Wrong file format for" + multipartfile.getOriginalFilename());
			}
		}
		
	}
	
	/**
	 * Method to upload Report Template file
	 * 
	 * @param reportId
	 * @param reportEntity
	 * @param fileType
	 * @param file
	 * @throws IOException
	 */
	private void uploadReportTemplateFile(int reportId, InsightsAssessmentReportTemplate reportEntity, String fileType,
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

	/**
	 * This method used to make template active or inactive
	 * 
	 * @param reportTemplateJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public String setReportTemplateStatus(JsonObject reportTemplateJson) throws InsightsCustomException {
		String message = "";
		try {
			int reportId = reportTemplateJson.get(AssessmentReportAndWorkflowConstants.REPORTID).getAsInt();
			InsightsAssessmentReportTemplate reportEntity = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getReportTemplateByReportId(reportId);
			if (reportEntity == null) {
				throw new InsightsCustomException(" Report template not exists in database " + reportId);
			} else {
				Boolean state = reportTemplateJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
				reportEntity.setActive(state);
				reportConfigDAL.updateReportConfig(reportEntity);
				message = "Report Template Id " + reportId + " status changed successfully ";
			}
		} catch (NoResultException e) {
			log.error(e);
			throw new InsightsCustomException("Report template not found ");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return message;
	}

	/**
	 * Delete the report template.
	 * 
	 * @param configId
	 * @return
	 * @throws InsightsCustomException
	 */
	public String deleteReportTemplate(JsonObject deleteReportTemplateJson) throws InsightsCustomException {
		String message = "";
		try {
			int reportId = deleteReportTemplateJson.get(AssessmentReportAndWorkflowConstants.REPORTID).getAsInt();
			reportConfigDAL.deleteReportTemplatebyReportID(reportId);
			message = "Template Report Id  " + reportId + " deleted successfully ";

		} catch (NoResultException e) {
			log.error(e);
			throw new InsightsCustomException("Report template not exists");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return message;
	}

	/**
	 * Method to save Report Template Configuration
	 * 
	 * @param templateReportJson
	 * @param reportEntity
	 * @param reportId
	 * @throws InsightsCustomException
	 * @throws IOException 
	 */
	public void saveReportConfig(JsonObject templateReportJson, InsightsAssessmentReportTemplate reportEntity,
			int reportId) throws InsightsCustomException {
		try {
			List<Integer> kpiIds = new ArrayList<>();
			Set<InsightsReportsKPIConfig> reportsKPIConfigSet = new LinkedHashSet<>();
			String reportName = templateReportJson.get(AssessmentReportAndWorkflowConstants.REPORTNAME).getAsString();
			boolean isActive = templateReportJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
			String description = templateReportJson.get("description").getAsString();
			String visualizationutil = templateReportJson.get("visualizationutil").getAsString();
			String templateType = (templateReportJson.has(ReportStatusConstants.TEMPLATE_TYPE))?templateReportJson.get(ReportStatusConstants.TEMPLATE_TYPE).getAsString():"Others";
			reportEntity.setReportId(reportId);
			reportEntity.setActive(isActive);
			reportEntity.setDescription(description);
			reportEntity.setTemplateName(reportName);
			reportEntity.setFile(reportName);
			reportEntity.setVisualizationutil(visualizationutil);
			reportEntity.setTemplateType(templateType);
			
			if(visualizationutil.equalsIgnoreCase(AssessmentReportAndWorkflowConstants.GRAFANAPDF)) {
				JsonObject dashboardTemplateJson = createDashboardJson(templateReportJson);
				byte[] dashboardTemplatebytes = dashboardTemplateJson.toString().getBytes();
				uploadReportTemplateFile(reportId, reportEntity, "JSON", "dashboardTemplate.json",dashboardTemplatebytes);
			}
			

			if (!templateReportJson.has(AssessmentReportAndWorkflowConstants.KPICONFIGS)) {
				throw new InsightsCustomException(" no KPI config provided for report : " + reportId);
			}

			JsonArray kpiConfigArray = templateReportJson.get(AssessmentReportAndWorkflowConstants.KPICONFIGS)
					.getAsJsonArray();

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
					String vConfig = (kpiObject.get(AssessmentReportAndWorkflowConstants.VISUALIZATIONCONFIGS).getAsJsonArray()).toString();
					InsightsReportsKPIConfig reportsKPIConfig = new InsightsReportsKPIConfig();
					reportsKPIConfig.setKpiConfig(kpiConfig);
					reportsKPIConfig.setvConfig(vConfig);
					reportsKPIConfig.setReportTemplateEntity(reportEntity);
					reportsKPIConfigSet.add(reportsKPIConfig);
				}
			}
			
			
			reportEntity.setReportsKPIConfig(reportsKPIConfigSet);
			reportConfigDAL.saveReportConfig(reportEntity);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		

	}

	private JsonObject createDashboardJson(JsonObject templateReportJson) throws InsightsCustomException {
		JsonObject dashboardTemplateJson = null;
		try {
			dashboardTemplateJson = fetchTemplateJson("Dashboard.json");
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
	
	
	public JsonObject fetchTemplateJson(String filename) throws InsightsCustomException {
		JsonObject templateJson = null;
		try {
			Resource resource = resourceLoaderService.getResource("classpath:dashboardandpaneltemplate/" + filename);
			InputStream fileInputStream = resource.getInputStream();
			String template = new String(fileInputStream.readAllBytes(), MQMessageConstants.MESSAGE_ENCODING);
			templateJson = JsonUtils.parseStringAsJsonObject(template);
		} catch (Exception e) {
			log.error("Error while fetching template JSON ====", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return templateJson;
	}
	
	public JsonObject preparePanelJson(String vType, String vQuery, String title, int id) throws InsightsCustomException {
		JsonObject panelTemplateJson = null;
		try {
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

	/**
	 * Method to edit Report Template Configuration
	 * 
	 * @param templateReportJson
	 * @param reportEntity
	 * @param reportId
	 * @throws InsightsCustomException
	 */
	public int editReportConfig(JsonObject templateReportJson) throws InsightsCustomException {

		InsightsAssessmentReportTemplate reportEntity = new InsightsAssessmentReportTemplate();
		List<Integer> kpiIds = new ArrayList<>();
		Set<InsightsReportsKPIConfig> reportsKPIConfigSet = new LinkedHashSet<>();
		int reportId = templateReportJson.get(AssessmentReportAndWorkflowConstants.REPORTID).getAsInt();
		String reportName = templateReportJson.get(AssessmentReportAndWorkflowConstants.REPORTNAME).getAsString();
		boolean isActive = templateReportJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
		String description = templateReportJson.get("description").getAsString();
		String visualizationutil = templateReportJson.get("visualizationutil").getAsString();
		String templateType = (templateReportJson.has(ReportStatusConstants.TEMPLATE_TYPE))?templateReportJson.get(ReportStatusConstants.TEMPLATE_TYPE).getAsString():"Others";
		
		reportEntity.setReportId(reportId);
		reportEntity.setActive(isActive);
		reportEntity.setDescription(description);
		reportEntity.setTemplateName(reportName);
		reportEntity.setFile(reportName);
		reportEntity.setVisualizationutil(visualizationutil);
		reportEntity.setTemplateType(templateType);
		
		if(visualizationutil.equalsIgnoreCase(AssessmentReportAndWorkflowConstants.GRAFANAPDF)) {
			JsonObject dashboardTemplateJson = createDashboardJson(templateReportJson);
			byte[] dashboardTemplatebytes = dashboardTemplateJson.toString().getBytes();
			uploadReportTemplateFile(reportId, reportEntity, "JSON", "dashboardTemplate.json",dashboardTemplatebytes);
		}

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
		reportConfigDAL.updateReportTemplate(reportEntity);
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
		try {
			kpiconfigList = reportConfigDAL.getAllActiveContentList();
			kpiconfigList.stream().forEach(contentDBData -> {
				JsonObject contentData =  JsonUtils.parseStringAsJsonObject(contentDBData.getContentJson());
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
		int contentId = -1;
		boolean isRecordDeleted = Boolean.FALSE;
		try {
			contentId = deleteContentJson.get(AssessmentReportAndWorkflowConstants.CONTENTID).getAsInt();
			InsightsContentConfig contentExistingConfig = reportConfigDAL.getContentConfig(contentId);
			if (contentExistingConfig == null) {
				throw new InsightsCustomException("Content definition not exists");
			} else {
				List<InsightsReportsKPIConfig> reportKPIList = reportConfigDAL
						.getActiveReportTemplateByKPIId(contentExistingConfig.getKpiConfig().getId());
				if (reportKPIList.isEmpty()) {
					reportConfigDAL.deleteContentbyContentID(contentId);
					isRecordDeleted = Boolean.TRUE;
				} else {
					throw new InsightsCustomException("Content definition attached to report template");
				}
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return isRecordDeleted;
	}

	/**
	 * method to fetch all report templates from ASSESSEMENT_REPORT_TEMPLATE table.
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public List<InsightsAssessmentReportTemplate> getAllReportTemplate() throws InsightsCustomException {
		try {
			return reportConfigDAL.getAllReportTemplatesList();
		} catch (Exception e) {
			log.error("Error getting all report template..", e);
			throw new InsightsCustomException(e.toString());
		}

	}

	/**
	 * method use to fetch KPI details of report template
	 * 
	 * @param
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonArray getReportTemplateKpidetails(int reportId) throws InsightsCustomException {
		List<InsightsReportsKPIConfig> kpiDetailsList = new ArrayList<>();
		JsonArray jsonArray = new JsonArray();
		try {
			kpiDetailsList = reportConfigDAL.getTemplateKpiDetailsByReportId(reportId);
			if (!kpiDetailsList.isEmpty()) {
				for (InsightsReportsKPIConfig eachRecord : kpiDetailsList) {
					JsonObject eachObject = new JsonObject();
	        		eachObject.addProperty("kpiId", eachRecord.getKpiConfig().getKpiId());
	        		JsonArray vConfigobj =  JsonUtils.parseStringAsJsonArray(eachRecord.getvConfig());
	        		if(vConfigobj.size() == 0) {
	        			eachObject.addProperty(AssessmentReportAndWorkflowConstants.VTYPE, "");
		        		eachObject.addProperty(AssessmentReportAndWorkflowConstants.VQUERY, "");
	        		}  else {	        			
	        			eachObject.addProperty(AssessmentReportAndWorkflowConstants.VQUERY, vConfigobj.get(0).getAsJsonObject().get(AssessmentReportAndWorkflowConstants.VQUERY).getAsString());
	        			eachObject.addProperty(AssessmentReportAndWorkflowConstants.VTYPE, vConfigobj.get(0).getAsJsonObject().get(AssessmentReportAndWorkflowConstants.VTYPE).getAsString());
	        		}
	        		jsonArray.add(eachObject);
				}
			}
			return jsonArray;
		} catch (Exception e) {
			log.error("Error while getting kpi details of report template ...", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Method to get Visualization types
	 * 
	 * @return List<String>
	 * @throws InsightsCustomException
	 */
	public JsonObject getAllChartType() throws InsightsCustomException {
		try {
			JsonObject chartTypesJson = new JsonObject();
			List<String> vTypeList = new LinkedList<>(ReportChartCollection.getSingleSeriesCharts());
			vTypeList.addAll(ReportChartCollection.getCommonCharts());
			vTypeList.addAll(ReportChartCollection.getSingleValueCharts());
			vTypeList.add("table");
			Gson gson = new Gson();
			chartTypesJson.add("vTypes", gson.toJsonTree(vTypeList).getAsJsonArray());
			JsonArray grafanaChartsList = new JsonArray();
			List<InsightsConfigFiles> configFile = configFilesDAL
					.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.GRAFANA_PDF_TEMPLATE.name());
			configFile.forEach(x -> grafanaChartsList.add(x.getFileName()));
			chartTypesJson.add("grafanaCharts", grafanaChartsList);
			return chartTypesJson;
		} catch (Exception e) {
			log.error("Error while getting chart types ... ", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Method to get Visualization util
	 * 
	 * @return List<String>
	 */
	public List<String> getVisualizationUtil() {
		List<String> chartHandlerList = new ArrayList<>();
		VisualizationUtilEnum.VisualizationUtil[] visualizationUtil = VisualizationUtilEnum.VisualizationUtil.values();
		for (VisualizationUtilEnum.VisualizationUtil eachHandler : visualizationUtil) {
			chartHandlerList.add(eachHandler.toString().toUpperCase());
		}
		return chartHandlerList;
	}
	
	/**
	 * Method to get Report template Type
	 * 
	 * @return List<String>
	 */
	public List<String> getTemplateType() {
		List<String> templateTypeList = new ArrayList<>();
		ReportTemplateTypeEnum.ReportTemplateType[] templateType = ReportTemplateTypeEnum.ReportTemplateType.values();
		for (ReportTemplateTypeEnum.ReportTemplateType eachHandler : templateType) {
			templateTypeList.add(eachHandler.toString());
		}
		return templateTypeList;
	}
	
	/**
	 * Method to refresh Grafana Org Token
	 * 
	 */
	public String refreshGrafanaOrgToken(int orgId) throws InsightsCustomException {
		try {
			grafanaUtilities.refreshGrafanaToken(orgId);
			return "Token refreshed successfully";
		} catch (Exception e) {
			log.error("Error while refreshing grafana token ... ", e);
			throw new InsightsCustomException(e.toString());
		}
	}

}
