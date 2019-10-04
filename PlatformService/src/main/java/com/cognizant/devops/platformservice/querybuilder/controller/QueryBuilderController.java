/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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


package com.cognizant.devops.platformservice.querybuilder.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformdal.queryBuilder.QueryBuilderConfig;
import com.cognizant.devops.platformservice.querybuilder.service.QueryBuilderService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/blockchain/queryBuilder")
public class QueryBuilderController {
	
	private static Logger Log = LogManager.getLogger(QueryBuilderController.class);

	@Autowired
	QueryBuilderService queryBuilderService;
	
	@RequestMapping(value = "/fetchQueries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchQueries(){
		List<QueryBuilderConfig> result = null;
		try{
			result = queryBuilderService.fetchQueries();
		}catch(Exception e){
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(result);
	}
	
	@RequestMapping(value = "/createQuery", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject saveOrUpdateQuery(@RequestBody Map<String,String> queryObj){
		String message = null;
		Log.debug("object is --"+queryObj);
		try{
			for (Map.Entry<String, String> reqParamQuery : queryObj.entrySet()) {
				ValidationUtils.cleanXSS(reqParamQuery.getValue());
			}
			message = queryBuilderService.saveOrUpdateQuery(queryObj.get("reportName"), queryObj.get("frequency"), queryObj.get("subscribers"), 
					queryObj.get("fileName"), queryObj.get("queryType"), queryObj.get("user"));
		}catch(Exception e){
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@RequestMapping(value = "/deleteQuery", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject deleteQuery(@RequestBody String reportName){
		String message = null;
		try{
			String validatedResponse = ValidationUtils.validateRequestBody(reportName);
			message = queryBuilderService.deleteQuery(validatedResponse);
		}catch(Exception e){
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	@ResponseBody
	public JsonObject uploadFile(@RequestBody MultipartFile file) {
		String message = "";
		try {
			boolean checkValidFile = PlatformServiceUtil.validateFile(file.getOriginalFilename());
			boolean isValid = PlatformServiceUtil.checkFileForHTML(new String(file.getBytes()));
			if(checkValidFile && isValid){
				Path rootLocation = Paths.get(ConfigOptions.QUERY_DATA_PROCESSING_RESOLVED_PATH);
				Log.debug("File Obj -- "+file);
				String fileName = file.getOriginalFilename();
				String extension = FilenameUtils.getExtension(fileName);
				if (fileName != null && !fileName.isEmpty() && "json".equalsIgnoreCase(extension)) {
					Files.copy(file.getInputStream(),rootLocation.resolve(file.getOriginalFilename()),StandardCopyOption.REPLACE_EXISTING);
					message = "You successfully uploaded " + file.getOriginalFilename() + "!";
				}else{
					message = "Upload failed, fail has invalid format! ";
					return PlatformServiceUtil.buildFailureResponse(message);
				}
			}else {
				return PlatformServiceUtil.buildFailureResponse("Invalid file content. ");
			}
		} catch (Exception e) {
			message = "FAIL to upload " + file.getOriginalFilename() + "!";
			return PlatformServiceUtil.buildFailureResponse(message);
		}
		Log.debug("Result -- "+message);
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@RequestMapping(value = "/getFileContents", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> getFileContents(@RequestParam("path") String path) {
		ResponseEntity<byte[]>  response = null;
		try {
			boolean validPath = PlatformServiceUtil.checkValidPath(path);
			if(validPath){
				Log.debug("Path to download -- "+path);
				byte[] fileContent = Files.readAllBytes(Paths.get(new File(path).getCanonicalPath()));
				boolean isValid = PlatformServiceUtil.checkFileForHTML(new String(fileContent));
				if(isValid) {
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.parseMediaType("application/json"));
					headers.add("Access-Control-Allow-Methods", "POST");
					headers.add("Access-Control-Allow-Headers", "Content-Type");
					headers.add("Content-Disposition", "attachment; filename="+Paths.get(new File(path).getCanonicalPath()).getFileName());
					headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
					headers.add("Pragma", "no-cache");
					headers.add("Expires", "0");
					response = new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
				}else {
					throw new Exception("Invalid file content -- "+new String(fileContent));
				}
			}else{
				throw new Exception("Invalid path -- "+path);
			}
		} catch (Exception e) {
			Log.error("Error, Failed to download from " + path, e.getMessage());
		}
		return response;
	}
	
}
