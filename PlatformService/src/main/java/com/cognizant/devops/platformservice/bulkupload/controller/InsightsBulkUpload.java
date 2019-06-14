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

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
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
	@RequestMapping(value = "/uploadHierarchyDetails", headers = ("content-type=multipart/*"), method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject uploadHierarchyDetails(@RequestParam("file") MultipartFile file,
			@RequestParam String toolName) {
		boolean status = false;
		try {
			
				status = BulkUploadService.getInstance().createBusinessHierarchyMetaData(file);
					
			if (!status) {
				return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
			}
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

		return PlatformServiceUtil.buildSuccessResponse();

	}
}
