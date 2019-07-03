package com.cognizant.devops.platformservice.bulkupload.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.bulkupload.service.BulkUploadService;
import com.cognizant.devops.platformservice.rest.neo4j.GraphDBService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/bulkupload")
public class InsightsBulkUpload {
	static Logger log = LogManager.getLogger(GraphDBService.class.getName());
	 private JsonObject asJsonObject;

	@Autowired
	BulkUploadService bulkUploadService;

	@RequestMapping(value = "/uploadToolData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject uploadToolData(@RequestParam("file") MultipartFile file,
			@RequestParam String toolName, @RequestParam String label) {
		boolean status = false;
		String messageToBePassed="";
		try
		{
		
		status = bulkUploadService.createBulkUploadMetaData(file, toolName, label);
		return PlatformServiceUtil.buildSuccessResponse();
			}
	
		catch(Exception e)
		{
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@RequestMapping(value = "/getToolJson", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getToolJson()  {
		Object details = null;
		try {
			details = bulkUploadService.getToolDetailJson();
			return PlatformServiceUtil.buildSuccessResponseWithData(details);
		} 
		catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		

}
}