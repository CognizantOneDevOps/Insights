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
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformservice.assessmentreport.service.AssesmentReportServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
			JsonParser parser = new JsonParser();
			JsonObject registerKpijson = (JsonObject) parser.parse(validatedResponse);
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
			JsonParser parser = new JsonParser();
			JsonObject registerContentKPIJson = (JsonObject) parser.parse(validatedResponse);
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
			JsonParser parser = new JsonParser();
			JsonObject assessmentReportJson = (JsonObject) parser.parse(validatedResponse);
			int assessmentReportId = assessmentReportService.saveAssessmentReport(assessmentReportJson);
			return PlatformServiceUtil
					.buildSuccessResponseWithData(" Assessment Report Id created " + assessmentReportId);
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
			JsonParser parser = new JsonParser();
			JsonObject assessmentReportJson = (JsonObject) parser.parse(validatedResponse);
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

	@GetMapping(value = "/loadAssessmentReport", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAssessmentReport() {
		JsonArray jsonarray = new JsonArray();
		try {
			jsonarray = assessmentReportService.getAssessmentReportList();
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@PostMapping(value = "/deleteAssessmentReport", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteReport(@RequestParam String configId) {
		String message = null;
		try {
			message = assessmentReportService.deleteAssessmentReport(configId);
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
			JsonParser parser = new JsonParser();
			JsonObject updateReportStateJsonValidated = (JsonObject) parser.parse(validatedResponse);
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
			reportTemplate = reportTemplate.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(reportTemplate);
			JsonParser parser = new JsonParser();
			JsonObject reportTemplateJson = (JsonObject) parser.parse(validatedResponse);
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

	@PostMapping(value = "/setReportStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject setWorkflowStatus(@RequestBody String reportConfigJsonString) {
		String message = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(reportConfigJsonString);
			JsonParser parser = new JsonParser();
			JsonObject reportConfigJson = (JsonObject) parser.parse(validatedResponse);
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
			JsonParser parser = new JsonParser();
			JsonObject updateKpijson = (JsonObject) parser.parse(validatedEditResponse);
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
			JsonParser parser = new JsonParser();
			JsonObject updateKpijson = (JsonObject) parser.parse(validatedEditResponse);
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
			JsonParser parser = new JsonParser();
			JsonObject updateKpijson = (JsonObject) parser.parse(validatedEditResponse);
			int resultKpiId = assessmentReportService.updateContentDefinition(updateKpijson);
			kpiResponse.addProperty(PlatformServiceConstants.MESSAGE,
					"Content definition updated for KpiId " + resultKpiId);
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
			JsonParser parser = new JsonParser();
			JsonObject updateKpijson = (JsonObject) parser.parse(validatedEditResponse);
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

}