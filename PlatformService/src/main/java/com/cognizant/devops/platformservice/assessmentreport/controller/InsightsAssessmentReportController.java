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
package com.cognizant.devops.platformservice.assessmentreport.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformservice.assessmentreport.service.AssesmentReportServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/insights/report")
public class InsightsAssessmentReportController {

	private static Logger log = LogManager.getLogger(InsightsAssessmentReportController.class);

	@Autowired
	AssesmentReportServiceImpl assessmentReportService;

	@PostMapping(value = "/saveKpiDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveKpiDefinition(@RequestBody String registerkpiJson) {
		try {
			JsonObject kpiResponse = new JsonObject();
			registerkpiJson = registerkpiJson.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(registerkpiJson);
			JsonObject registerKpijson =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			int resultKpiId = assessmentReportService.saveKpiDefinition(registerKpijson);
			kpiResponse.addProperty(PlatformServiceConstants.MESSAGE, "Kpi created with Id " + resultKpiId);
			return PlatformServiceUtil.buildSuccessResponseWithData(kpiResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save KPI Setting Configuration");
		}
	}

	@PostMapping(value = "/saveContentDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveContentDefinition(@RequestBody String registerContentJson) {
		try {
			JsonObject contentResponse = new JsonObject();
			registerContentJson = registerContentJson.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(registerContentJson);
			JsonObject registerContentKPIJson =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			int contentId = assessmentReportService.saveContentDefinition(registerContentKPIJson);
			contentResponse.addProperty(PlatformServiceConstants.MESSAGE, " Content Id created " + contentId);
			return PlatformServiceUtil.buildSuccessResponseWithData(contentResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save Cantent Definition due to exception");
		}
	}

	@PostMapping(value = "/saveBulkKpiDefinition", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject saveBulkKpiDefinition(@RequestParam("file") MultipartFile file) {
		try {
			String returnMessage = assessmentReportService.uploadKPIInDatabase(file);
			return PlatformServiceUtil.buildSuccessResponseWithData(returnMessage);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save KPI Setting Configuration ");
		}
	}

	@PostMapping(value = "/saveBulkContentDefinition", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject saveBulkContentDefinition(@RequestParam("file") MultipartFile file) {
		try {
			String returnMessage = assessmentReportService.uploadContentInDatabase(file);
			return PlatformServiceUtil.buildSuccessResponseWithData(returnMessage);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save Content Setting Configuration");
		}
	}

	/* Assessment Report Controller Methods */
	@GetMapping(value = "/getSchedule", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getScheduleList() {
		List<String> scheduleList;
		try {
			scheduleList = assessmentReportService.getSchedule();
			return PlatformServiceUtil.buildSuccessResponseWithData(scheduleList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@PostMapping(value = "/saveAssessmentReport", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveAssessmentReport(@RequestBody String assessmentReport) {
		try {
			assessmentReport = assessmentReport.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(assessmentReport);
			JsonObject assessmentReportJson =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			JsonObject responseJson = assessmentReportService.saveAssessmentReport(assessmentReportJson);
			return PlatformServiceUtil
					.buildSuccessResponseWithData(responseJson);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save assessment report due to exception");
		}
	}

	@PostMapping(value = "/updateAssessmentReport", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateAssessmentReport(@RequestBody String assessmentReport) {
		try {
			assessmentReport = assessmentReport.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(assessmentReport);
			JsonObject assessmentReportJson =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			int assessmentReportId = assessmentReportService.updateAssessmentReport(assessmentReportJson);
			return PlatformServiceUtil
					.buildSuccessResponseWithData(" Assessment Report Id updated " + assessmentReportId);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to update assessment report due to exception");
		}
	}

	@PostMapping(value = "/loadAssessmentReport", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAssessmentReport(@RequestBody String UserDetail) {
		JsonArray jsonarray = new JsonArray();
		try {
			UserDetail = UserDetail.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(UserDetail);
			JsonObject userDetail = JsonUtils.parseStringAsJsonObject(validatedResponse);
			jsonarray = assessmentReportService.getAssessmentReportList(userDetail);
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@PostMapping(value = "/deleteAssessmentReport", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteReport(@RequestParam String configId) {
		String message = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(configId);
			message = assessmentReportService.deleteAssessmentReport(validatedResponse);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@PostMapping(value = "/updateAssessmentReportState", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateWebhookStatus(@RequestBody String updateReportStateJson) {
		String message = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(updateReportStateJson);
			JsonObject updateReportStateJsonValidated =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			message = assessmentReportService.updateAssessmentReportState(updateReportStateJsonValidated);
			if (message.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				return PlatformServiceUtil.buildSuccessResponse();
			} else {
				return PlatformServiceUtil.buildFailureResponse("Unable to update assessment report");
			}
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	// CONTROLLER FOR REPORT TEMPLATE CONFIG TABLE

	@PostMapping(value = "/saveReportTemplate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveReportTemplate(@RequestBody String reportTemplate) {
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(reportTemplate);
			JsonObject reportTemplateJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			int templateReportId = assessmentReportService.saveTemplateReport(reportTemplateJson);
			return PlatformServiceUtil.buildSuccessResponseWithData(" Template Report Id created " + templateReportId);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save template report due to exception");
		}
	}

	@PostMapping(value = "/setReportTemplateStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject setReportTemplateStatus(@RequestBody String reportTemplate) {
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(reportTemplate);
			JsonObject reportTemplateJson =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			String message= assessmentReportService.setReportTemplateStatus(reportTemplateJson);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil
					.buildFailureResponse("Unable to set report template status  report due to exception");
		}
	}

	@PostMapping(value = "/deleteReportTemplate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteReportTemplate(@RequestBody String deleteReportTemplateJson) {
		String message = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(deleteReportTemplateJson);
			JsonObject deleteReportTemplateJsonParsed =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			message = assessmentReportService.deleteReportTemplate(deleteReportTemplateJsonParsed);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@GetMapping(value = "/getReportTemplate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getReportTemplateList() {
		List<InsightsAssessmentReportTemplate> reportTemplateList;
		JsonArray jsonarray = new JsonArray();
		try {
			reportTemplateList = assessmentReportService.getReportTemplate();
			for (InsightsAssessmentReportTemplate reportTemplate : reportTemplateList) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.addProperty("reportId", reportTemplate.getReportId());
				jsonobject.addProperty("templateName", reportTemplate.getTemplateName());
				jsonobject.addProperty("templateType", reportTemplate.getTemplateType());
				jsonarray.add(jsonobject);
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@PostMapping(value = "/getKPIlistOfReportTemplate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getKPIlist(@RequestParam String reportId) {
		JsonArray data = null;
		try {
			data = assessmentReportService.getKPIlistOfReportTemplate(reportId);
			return PlatformServiceUtil.buildSuccessResponseWithData(data);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@PostMapping(value = "/editReportTemplate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject editReportTemplate(@RequestBody String reportTemplate) {
		try {
			reportTemplate = reportTemplate.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(reportTemplate);
			JsonObject reportTemplateJson =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			int templateReportId = assessmentReportService.editReportTemplate(reportTemplateJson);
			return PlatformServiceUtil
					.buildSuccessResponseWithData("Report Template Id updated successfully  " + templateReportId);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to edit template report due to exception");
		}
	}

	@PostMapping(value = "/uploadReportTemplate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject uploadReportTemplate(@RequestParam("file") MultipartFile file) {
		try {
			String returnMessage = assessmentReportService.uploadReportTemplate(file);
			return PlatformServiceUtil.buildSuccessResponseWithData(returnMessage);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to upload Report Template ");
		}
	}

	@PostMapping(value = "/uploadReportTemplateDesignFiles", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject uploadReportTemplateDesignFiles(@RequestParam("files") MultipartFile[] files,
			@RequestParam int reportId) {
		String returnMessage = "";
		try {
			returnMessage = assessmentReportService.uploadReportTemplateDesignFiles(files, reportId);
			return PlatformServiceUtil.buildSuccessResponseWithData(returnMessage);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to upload Report Template Design Files ");
		}
	}


	@PostMapping(value = "/setReportStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject setWorkflowStatus(@RequestBody String reportConfigJsonString) {
		String message = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(reportConfigJsonString);
			JsonObject reportConfigJson =  JsonUtils.parseStringAsJsonObject(validatedResponse);
			message = assessmentReportService.setReportStatus(reportConfigJson);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@GetMapping(value = "/getKpiCategory", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getKpiCategorylist() {
		List<String> categoryList;
		try {
			categoryList = assessmentReportService.getKpiCategory();
			return PlatformServiceUtil.buildSuccessResponseWithData(categoryList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@GetMapping(value = "/getKpiDataSource", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getKpiDataSourcelist() {
		List<String> dataSourceList;
		try {
			dataSourceList = assessmentReportService.getKpiDataSource();
			return PlatformServiceUtil.buildSuccessResponseWithData(dataSourceList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@GetMapping(value = "/getAllActiveKpiList", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllActiveKpiList() {
		try {
			List<InsightsKPIConfig> activeKPIList = assessmentReportService.getActiveKpiList();
			return PlatformServiceUtil.buildSuccessResponseWithHtmlData(activeKPIList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@PostMapping(value = "/updateKpiDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateKpiDefinition(@RequestBody String updatekpiRequest) {
		try {
			JsonObject kpiResponse = new JsonObject();
			updatekpiRequest = updatekpiRequest.replace("\n", "").replace("\r", "");
			String validatedEditResponse = ValidationUtils.validateRequestBody(updatekpiRequest);
			JsonObject updateKpijson =  JsonUtils.parseStringAsJsonObject(validatedEditResponse);
			int resultKpiId = assessmentReportService.updateKpiDefinition(updateKpijson);
			kpiResponse.addProperty(PlatformServiceConstants.MESSAGE,
					"Kpi definition updated for KpiId " + resultKpiId);
			return PlatformServiceUtil.buildSuccessResponseWithData(kpiResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to update KPI Setting Configuration");
		}
	}

	@PostMapping(value = "/deleteKpiDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteKpiDefinition(@RequestBody String deletekpiRequest) {
		try {
			JsonObject kpiResponse = new JsonObject();
			deletekpiRequest = deletekpiRequest.replace("\n", "").replace("\r", "");
			String validatedEditResponse = ValidationUtils.validateRequestBody(deletekpiRequest);
			JsonObject updateKpijson =  JsonUtils.parseStringAsJsonObject(validatedEditResponse);
			boolean isRecordDeleted = assessmentReportService.deleteKpiDefinition(updateKpijson);
			if (isRecordDeleted) {
				kpiResponse.addProperty(PlatformServiceConstants.MESSAGE,
						"Kpi definition deleted for KpiId "
								+ updateKpijson.get(AssessmentReportAndWorkflowConstants.KPIID));
				return PlatformServiceUtil.buildSuccessResponseWithData(kpiResponse);
			} else {
				kpiResponse.addProperty(PlatformServiceConstants.MESSAGE,
						"Kpi definition not deleted for KpiId "
								+ updateKpijson.get(AssessmentReportAndWorkflowConstants.KPIID));
				return PlatformServiceUtil.buildFailureResponse(kpiResponse.getAsString());
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to delete KPI Setting Configuration");
		}
	}

	@PostMapping(value = "/updateContentDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateContentDefinition(@RequestBody String updateContentRequest) {
		try {
			JsonObject kpiResponse = new JsonObject();
			updateContentRequest = updateContentRequest.replace("\n", "").replace("\r", "");
			String validatedEditResponse = ValidationUtils.validateRequestBody(updateContentRequest);
			JsonObject updateKpijson =  JsonUtils.parseStringAsJsonObject(validatedEditResponse);
			int resultContentId = assessmentReportService.updateContentDefinition(updateKpijson);
			kpiResponse.addProperty(PlatformServiceConstants.MESSAGE,
					"Content definition updated for ContentId " + resultContentId);
			return PlatformServiceUtil.buildSuccessResponseWithData(kpiResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to update KPI Setting Configuration");
		}
	}

	@GetMapping(value = "/getContentAction", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getContentAction() {
		List<String> categoryList;
		try {
			categoryList = assessmentReportService.getContentAction();
			return PlatformServiceUtil.buildSuccessResponseWithData(categoryList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@GetMapping(value = "/getAllActiveContentList", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllActiveContentList() {
		try {
			List<JsonObject> activeContentList = assessmentReportService.getAllActiveContentList();
			return PlatformServiceUtil.buildSuccessResponseWithHtmlData(activeContentList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@PostMapping(value = "/deleteContentDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteContentDefinition(@RequestBody String deleteContentRequest) {
		try {
			JsonObject kpiResponse = new JsonObject();
			deleteContentRequest = deleteContentRequest.replace("\n", "").replace("\r", "");
			String validatedEditResponse = ValidationUtils.validateRequestBody(deleteContentRequest);
			JsonObject updateKpijson =  JsonUtils.parseStringAsJsonObject(validatedEditResponse);
			boolean isRecordDeleted = assessmentReportService.deleteContentDefinition(updateKpijson);
			if (isRecordDeleted) {
				kpiResponse.addProperty(PlatformServiceConstants.MESSAGE,
						"Content definition deleted for ContentId "
								+ updateKpijson.get(AssessmentReportAndWorkflowConstants.CONTENTID));
				return PlatformServiceUtil.buildSuccessResponseWithData(kpiResponse);
			} else {
				kpiResponse.addProperty(PlatformServiceConstants.MESSAGE,
						"Content definition not deleted for ContentId "
								+ updateKpijson.get(AssessmentReportAndWorkflowConstants.CONTENTID));
				return PlatformServiceUtil.buildFailureResponse(kpiResponse.getAsString());
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to delete KPI Setting Configuration");
		}
	}
	
	@GetMapping(value = "/getAllReportTemplate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllReportTemplateList() {
		List<InsightsAssessmentReportTemplate> reportTemplateList;
		JsonArray jsonarray = new JsonArray();
		try {
			reportTemplateList = assessmentReportService.getAllReportTemplate();
			for (InsightsAssessmentReportTemplate eachTemplate : reportTemplateList) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.addProperty("reportId", eachTemplate.getReportId());
				jsonobject.addProperty("templateName", eachTemplate.getTemplateName());
				jsonobject.addProperty("description", eachTemplate.getDescription());
				jsonobject.addProperty("isActive", eachTemplate.isActive());
				jsonobject.addProperty("visualizationutil", eachTemplate.getVisualizationutil());
				jsonobject.addProperty("templateType", eachTemplate.getTemplateType());
				jsonarray.add(jsonobject);
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@PostMapping(value = "/getReportTemplateKpiDetails", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getReportTemplateKpiDetails(String reportId) {
		JsonArray kpiDetailsArray = new JsonArray();
		try {
			int reportID = Integer.parseInt(reportId);
			kpiDetailsArray = assessmentReportService.getReportTemplateKpidetails(reportID);

			return PlatformServiceUtil.buildSuccessResponseWithData(kpiDetailsArray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}	

	}

	@GetMapping(value = "/getChartType", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getChartType() {
		JsonObject vTypeList;
		try {
			vTypeList = assessmentReportService.getAllChartType();
			return PlatformServiceUtil.buildSuccessResponseWithData(vTypeList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@GetMapping(value = "/getVisualizationUtil", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getVisualizationUtil() {
		List<String> chartHandlerList;
		try {
			chartHandlerList = assessmentReportService.getVisualizationUtil();
			return PlatformServiceUtil.buildSuccessResponseWithData(chartHandlerList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}
	
	@GetMapping(value = "/getTemplateType", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getTemplateType() {
		List<String> templateTypeList;
		try {
			templateTypeList = assessmentReportService.getTemplateType();
			return PlatformServiceUtil.buildSuccessResponseWithData(templateTypeList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

}