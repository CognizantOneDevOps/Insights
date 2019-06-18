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
	//private JsonObject asJsonObject;
	
	@RequestMapping(value = "/uploadToolData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject uploadToolData(@RequestParam("file") MultipartFile file, @RequestParam String toolName ) throws InsightsCustomException {
		boolean status = false;
		
		status = BulkUploadService.getInstance().createBulkUploadMetaData(file,toolName);
		
        log.error(file);
        log.error(status);
        log.error(PlatformServiceUtil.buildSuccessResponse());
		return PlatformServiceUtil.buildSuccessResponse();

	}
	
	@RequestMapping(value = "/getToolJson", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getToolJson() throws IOException, InsightsCustomException {
		Object details = null;
		try {
			details = BulkUploadService.getInstance().getToolDetailJson();
			log.debug("FILE"+details);
		}catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		log.error(PlatformServiceUtil.buildSuccessResponseWithData(details));
		return PlatformServiceUtil.buildSuccessResponseWithData(details);
		//return PlatformServiceUtil.buildSuccessResponseWithData(details);
	}
	
	
}