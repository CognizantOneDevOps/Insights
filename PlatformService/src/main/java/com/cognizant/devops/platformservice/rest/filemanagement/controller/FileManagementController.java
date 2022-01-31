/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.rest.filemanagement.controller;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.filemanagement.service.FileManagementServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/filemanagement")
public class FileManagementController {
	private static Logger log = LogManager.getLogger(FileManagementController.class);

	@Autowired
	FileManagementServiceImpl fileManagementService;

	@GetMapping(value = "/getFileTypes", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getFileTypeList() {
		List<String> fileTypeList;
		try {
			fileTypeList = fileManagementService.getFileType();
			return PlatformServiceUtil.buildSuccessResponseWithData(fileTypeList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@GetMapping(value = "/getModules", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getModuleList() {
		List<String> fileModuleList;
		try {
			fileModuleList = fileManagementService.getModuleList();
			return PlatformServiceUtil.buildSuccessResponseWithData(fileModuleList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@GetMapping(value = "/getConfigurationFiles", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getConfigurationFiles() {
		JsonArray jsonarray = new JsonArray();
		try {
			jsonarray = fileManagementService.getConfigurationFilesList();
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@PostMapping(value = "/uploadConfigurationFile", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveConfigurationFile(@RequestParam("file") MultipartFile file,
			@RequestParam String fileName, @RequestParam String fileType, @RequestParam String module, @RequestParam boolean isEdit) {
		try {
			String message = fileManagementService.uploadConfigurationFile(file, fileName, fileType, module, isEdit);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@PostMapping(value = "/deleteConfigFile", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteConfigurationFile(@RequestParam String fileName) {
		try {
			fileManagementService.deleteConfigurationFile(fileName);
			return PlatformServiceUtil.buildSuccessResponse();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}

	}

	@PostMapping(value = "/downloadConfigFile")
	@ResponseBody
	public ResponseEntity<byte[]> downloadConfigFile(@RequestBody String fileDetailsJsonString) {
		ResponseEntity<byte[]> response = null;
		try {
			String decodedString = new String(Base64.getDecoder().decode(fileDetailsJsonString),
					StandardCharsets.UTF_8);
			String validatedRequest = ValidationUtils.validateRequestBody(decodedString);
			JsonObject fileDetailsJson = JsonUtils.parseStringAsJsonObject(validatedRequest);
			String fileName = StringEscapeUtils.escapeHtml4(ValidationUtils.cleanXSS(fileDetailsJson.get("fileName").getAsString()));
			byte[] fileContent = fileManagementService.getFileData(fileName);
			String fileData = new String(fileContent, StandardCharsets.UTF_8).replace(PlatformServiceConstants.LF, PlatformServiceConstants.EMPTY).replace(PlatformServiceConstants.CR, PlatformServiceConstants.EMPTY).replace(PlatformServiceConstants.TAB, PlatformServiceConstants.EMPTY);
			String validatedResponse = ValidationUtils.validateResponseBody(fileData);
			String fileDetailName = fileDetailsJson.get("fileName").getAsString() + "."
					+ fileDetailsJson.get("fileType").getAsString().toLowerCase();
			String mediaType = "application/" + fileDetailsJson.get("fileType").getAsString().toLowerCase();
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			HttpHeaders headers = new HttpHeaders();
			if(mediaType.contains("json")) {
				JsonElement fileValidation = JsonUtils.parseStringAsJsonElement(validatedResponse);
				if(fileValidation == null) {
					throw new InsightsCustomException("Invalid file ");
				}
				
				byteArrayOutputStream.write(fileValidation.toString().getBytes());
			
				headers.setContentType(MediaType.parseMediaType(mediaType));
				headers.add("Access-Control-Allow-Methods", "POST");
				headers.add("Access-Control-Allow-Headers", "Content-Type");
				headers.add("Content-Disposition", "attachment; filename=" + fileDetailName);
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				headers.add("Accept-Encoding", "UTF-8");
				response = new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
			}
		} catch (Exception e) {
			log.error("Error, Failed to download file -- {} ", e.getMessage());
		}
		return response;

	}

}
