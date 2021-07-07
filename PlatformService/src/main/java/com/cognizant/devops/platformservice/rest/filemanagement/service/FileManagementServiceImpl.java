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
package com.cognizant.devops.platformservice.rest.filemanagement.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.traceabilitydashboard.service.TraceabilityDashboardServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service("fileManagementService")
public class FileManagementServiceImpl {

	private static final Logger log = LogManager.getLogger(FileManagementServiceImpl.class);
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();

	/**
	 * Method to fetch File Types
	 * 
	 * @return List<String>
	 */
	public List<String> getFileType() {
		List<String> listOfFileType = new ArrayList<>();
		FileDetailsEnum.ConfigurationFileType[] fileTypes = FileDetailsEnum.ConfigurationFileType.values();
		for (FileDetailsEnum.ConfigurationFileType fileType : fileTypes) {
			listOfFileType.add(fileType.toString());
		}
		return listOfFileType;
	}

	/**
	 * Method to fetch Module List
	 * 
	 * @return  List<String>
	 */
	public List<String> getModuleList() {
		List<String> listOfModule = new ArrayList<>();
		FileDetailsEnum.FileModule[] modules = FileDetailsEnum.FileModule.values();
		for (FileDetailsEnum.FileModule module : modules) {
			listOfModule.add(module.toString());
		}
		return listOfModule;
	}

	/**
	 * Method to fetch all Configuration files
	 * 
	 * @return JsonArray
	 * @throws InsightsCustomException
	 */
	public JsonArray getConfigurationFilesList() throws InsightsCustomException {
		try {
			List<InsightsConfigFiles> configFiles = configFilesDAL.getAllConfigurationFiles();
			JsonArray configFilesArray = new JsonArray();
			for (InsightsConfigFiles configFile : configFiles) {
				JsonObject configFileJson = new JsonObject();
				configFileJson.addProperty("fileName", configFile.getFileName());
				configFileJson.addProperty("fileType", configFile.getFileType());
				configFileJson.addProperty("fileModule", configFile.getFileModule());
				configFilesArray.add(configFileJson);
			}
			return configFilesArray;
		} catch (Exception e) {
			log.error("Error getting configuration files list ..", e);
			throw new InsightsCustomException(e.getMessage());
		}

	}

	/**
	 * Method to upload Configuration file
	 * 
	 * @param multipartfile
	 * @param fileName
	 * @param fileType
	 * @param module
	 * @return String
	 * @throws InsightsCustomException
	 */
	public String uploadConfigurationFile(MultipartFile multipartfile, String fileName, String fileType, String module, boolean isEdit)
			throws InsightsCustomException {
		try {
			InsightsConfigFiles configFileRecord = configFilesDAL.getConfigurationFile(fileName);
			File file = PlatformServiceUtil.convertToFile(multipartfile);
			if (configFileRecord == null) {
				List<InsightsConfigFiles> configFiles = configFilesDAL.getAllConfigurationFilesForModule(module);
				FileDetailsEnum.FileModule[] modules = FileDetailsEnum.FileModule.values();
				int moduleValue = 0;
				for (FileDetailsEnum.FileModule mod : modules) {
					if (module.equals(mod.toString())) {
						moduleValue = mod.getValue();
						break;
					}
				}
				if (configFiles.size() < moduleValue || moduleValue == -1) {
					InsightsConfigFiles configFile = new InsightsConfigFiles();
					configFile.setFileName(fileName);
					configFile.setFileType(fileType);
					configFile.setFileModule(module);
					configFile.setFileData(FileUtils.readFileToByteArray(file));
					configFilesDAL.saveConfigurationFile(configFile);
				} else {
					throw new InsightsCustomException("One file upload allowed for " + module+". Please delete the existing file to upload a new file.");
				}
			} else if(isEdit) {
				configFileRecord.setFileName(fileName);
				configFileRecord.setFileType(fileType);
				configFileRecord.setFileModule(module);
				configFileRecord.setFileData(FileUtils.readFileToByteArray(file));
				configFilesDAL.updateConfigurationFile(configFileRecord);
			} else {
				throw new InsightsCustomException("File already exists in database.");
			}
			// used to clean up TRACEABILITY cache 
			if(module.equalsIgnoreCase(FileDetailsEnum.FileModule.TRACEABILITY.toString())) {
				log.debug(" Traceability Json updated, clear all value from cache ");
				TraceabilityDashboardServiceImpl traceabilityObj = new TraceabilityDashboardServiceImpl();
				traceabilityObj.clearAllCacheValue();
			}
			return "File uploaded";

		} catch (Exception e) {
			log.error("Error saving configuration file", e);
			throw new InsightsCustomException(e.getMessage());
		}

	}

	/**
	 * Method to delete Configuration file
	 * 
	 * @param fileName
	 * @return String
	 * @throws InsightsCustomException
	 */
	public String deleteConfigurationFile(String fileName) throws InsightsCustomException {
		try {
			return configFilesDAL.deleteConfigurationFile(fileName);
		} catch (Exception e) {
			log.error("Error while deleting archived data {} ", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}

	}

	/**
	 * Method to get File Data
	 * 
	 * @param fileDetailsJson
	 * @return byte[]
	 * @throws InsightsCustomException
	 */
	public byte[] getFileData(JsonObject fileDetailsJson) throws InsightsCustomException {
		byte[] fileContent = null;
		try {
			InsightsConfigFiles configFile = configFilesDAL
					.getConfigurationFile(fileDetailsJson.get("fileName").getAsString());
			if (configFile != null) {
				fileContent = configFile.getFileData();
			} else {
				throw new InsightsCustomException("File not found");
			}
		} catch (Exception e) {
			log.error("Error while downloading file", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return fileContent;
	}

}
