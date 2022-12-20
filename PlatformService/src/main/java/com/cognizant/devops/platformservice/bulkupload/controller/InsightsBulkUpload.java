/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.bulkupload.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.bulkupload.service.IBulkUpload;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/bulkupload")
public class InsightsBulkUpload {
	static Logger log = LogManager.getLogger(InsightsBulkUpload.class.getName());

	@Autowired
	IBulkUpload bulkUploadService;
	/**
	 * entry point to upload Bulk Data
	 *
	 * @param file
	 * @param toolName
	 * @param label
	 * @param insightsTimeField
	 * @param insightsTimeFormat
	 * @return ResponseBody
	 * @throws IOException
	 */
	@PostMapping(value = "/uploadToolData", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject uploadToolData(@RequestParam("file") MultipartFile file,
			@RequestParam String toolName, @RequestParam String label, @RequestParam String insightsTimeField,
			@RequestParam String insightsTimeFormat) throws IOException {
		boolean status = false;
		try {
			status = bulkUploadService.uploadDataInDatabase(file, toolName, label, insightsTimeField,
					insightsTimeFormat);
			log.debug(" Upload tool data done {} successfully for tool  {} ", status, toolName);
			return PlatformServiceUtil.buildSuccessResponse();
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	/**
	 * entry point to get Tool Details
	 *
	 * @return ResponseBody
	 */
	@GetMapping(value = "/getToolJson", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getToolJson() {
		Object details = null;
		try {
			details = bulkUploadService.getToolDetailJson();
			return PlatformServiceUtil.buildSuccessResponseWithData(details);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
}