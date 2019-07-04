package com.cognizant.devops.platformservice.bulkupload.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;

public interface IBulkUpload {
	public Object getToolDetailJson() throws InsightsCustomException;
	public boolean uploadDataInDatabase(MultipartFile file, String toolName, String label)
			throws InsightsCustomException, IOException;
}
