/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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

package com.cognizant.devops.platformservice.workflow.external.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/externalApi/insights")
public class InsightsWorkflowExternalController {
	
	private static Logger log = LogManager.getLogger(InsightsWorkflowExternalController.class);
	private static final String POST = "POST";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String NO_CACHE = "no-cache";
	
	@Autowired
	WorkflowServiceImpl workflowService;
	
	
	@PostMapping(value = "/downloadDashboardReportPDF")
	@ResponseBody
	public ResponseEntity<Object> getDashboardReportPDF(@RequestBody String reportTitle) {
		ResponseEntity<Object> response = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(reportTitle);
			JsonObject pdfDetailsJson = workflowService.preparePdfDetailsJson(validatedResponse);
			byte[] fileContent = workflowService.getReportPDF(pdfDetailsJson);
			if(fileContent !=  null) {			
				String pdfName = pdfDetailsJson.get("pdfName").getAsString() + ".pdf";
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_PDF_VALUE));
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, POST);
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, CONTENT_TYPE);
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdfName);
				headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
				headers.add(HttpHeaders.PRAGMA, NO_CACHE);
				headers.add(HttpHeaders.EXPIRES, "0");
				response = new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
			} else {
				response = new ResponseEntity<>("Error while generating the PDF: PDF not found!", HttpStatus.BAD_REQUEST );
			}
		} catch (InsightsCustomException e) {
			log.error("Error, Failed to download pdf -- {} ", e.getMessage());
			response = new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR );
		}
		return response;
	}
}
