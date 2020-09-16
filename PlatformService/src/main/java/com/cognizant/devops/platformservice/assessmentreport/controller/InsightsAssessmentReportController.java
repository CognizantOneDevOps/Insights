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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
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
			registerkpiJson = registerkpiJson.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(registerkpiJson);
			JsonParser parser = new JsonParser();
			JsonObject registerKpijson = (JsonObject) parser.parse(validatedResponse);
			int resultKpiId = assessmentReportService.saveKpiDefinition(registerKpijson);
			return PlatformServiceUtil.buildSuccessResponseWithData(" Kpi created with Id " + resultKpiId);
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
			registerContentJson = registerContentJson.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(registerContentJson);
			JsonParser parser = new JsonParser();
			JsonObject registerContentKPIJson = (JsonObject) parser.parse(validatedResponse);
			int contentId = assessmentReportService.saveContentDefinition(registerContentKPIJson);
			return PlatformServiceUtil.buildSuccessResponseWithData(" Content Id created " + contentId);
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
			String resultKpiIdResponse = assessmentReportService.uploadKPIInDatabase(file);
			return PlatformServiceUtil.buildSuccessResponseWithData(" Kpis details are  " + resultKpiIdResponse);
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
			String resultContentResponse = assessmentReportService.uploadContentInDatabase(file);
			return PlatformServiceUtil.buildSuccessResponseWithData(" Contents details are " + resultContentResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save Content Setting Configuration");
		}
	}

	/* Assessment Report Controller Methods */
	@RequestMapping(value = "/getSchedule", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
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

	@RequestMapping(value = "/loadAssessmentReport", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAssessmentReport() {
		JsonArray jsonarray = new JsonArray();
		try {
			jsonarray = assessmentReportService.getAssessmentReportList();
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@RequestMapping(value = "/deleteAssessmentReport", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteReport(@RequestParam String configId) {
		String message = null;
		try {
			message = assessmentReportService.deleteAssessmentReport(configId);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@RequestMapping(value = "/updateAssessmentReportState", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateWebhookStatus(@RequestBody String updateReportStateJson) {
		String message = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(updateReportStateJson);
			JsonParser parser = new JsonParser();
			JsonObject updateReportStateJsonValidated = (JsonObject) parser.parse(validatedResponse);
			message = assessmentReportService.updateAssessmentReportState(updateReportStateJsonValidated);
			return PlatformServiceUtil.buildSuccessResponse();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	// CONTROLLER FOR REPORT TEMPLATE CONFIG TABLE

	@RequestMapping(value = "/saveReportTemplate", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
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

	@RequestMapping(value = "/getReportTemplate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
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

	@RequestMapping(value = "/getKPIlistOfReportTemplate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getKPIlist(@RequestParam String reportId) {
		JsonArray data = null;
		try {
			data = assessmentReportService.getKPIlistOfReportTemplate(reportId);
			return PlatformServiceUtil.buildSuccessResponseWithData(data);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@RequestMapping(value = "/setReportStatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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

}
