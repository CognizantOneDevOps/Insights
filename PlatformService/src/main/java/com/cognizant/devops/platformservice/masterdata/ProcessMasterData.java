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
package com.cognizant.devops.platformservice.masterdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ResourceUtils;

import com.cognizant.devops.platformdal.masterdata.MasterDataDAL;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ProcessMasterData {

	private static Logger log = LogManager.getLogger(ProcessMasterData.class);
	private static final String JSON_FILE_EXTENSION = "json";
	MasterDataDAL masterDataDal = new MasterDataDAL();

	/**
	 * Load json file for masterdata query processing
	 * 
	 * @return masterDataFileCount
	 */
	public int executeMasterDataProcessing() {
		int masterDataFileCount = 0;
		File queryFolderPath;
		try {
			queryFolderPath = ResourceUtils.getFile("classpath:masterdata");
			File[] masterDataFiles = queryFolderPath.listFiles();
			if (masterDataFiles == null) {
				return masterDataFileCount;
			}
			for (File masterDataFile : masterDataFiles) {
				log.debug("masterDataFile {} ", masterDataFile.getName());
				if (masterDataFile.isFile()) { // this line removes other directories/folders
					String masterDataFileName = masterDataFile.getName();
					if (hasJsonFileExtension(masterDataFileName)) {
						masterDataFileCount++;
						processMasterDataConfiguration(masterDataFile);
					}
				}
			}
		} catch (FileNotFoundException e) {
			log.error("File not found");
		}
		return masterDataFileCount;
	}

	/**
	 * Check if file has json extension or not
	 * 
	 * @param masterDataFileName
	 * @return
	 */
	private boolean hasJsonFileExtension(String masterDataFileName) {
		if (masterDataFileName != null && !masterDataFileName.isEmpty()) {
			String extension = FilenameUtils.getExtension(masterDataFileName);
			if (JSON_FILE_EXTENSION.equalsIgnoreCase(extension)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Extract {@link MasterDataQueryModel} using FileReader
	 * 
	 * @param masterDataFile
	 * @return
	 */
	private Boolean processMasterDataConfiguration(File masterDataFile) {
		try {
			try (BufferedReader reader = new BufferedReader(new FileReader(masterDataFile))) {
				MasterDataQueryModel[] masterDataQueryModelArray = new Gson().fromJson(reader,
						MasterDataQueryModel[].class);
				List<MasterDataQueryModel> masterDataQueryModels = Arrays.asList(masterDataQueryModelArray);
				for (MasterDataQueryModel masterDataQueryModel : masterDataQueryModels) {
					processQuery(masterDataQueryModel);
				}
			}
		} catch (FileNotFoundException e) {
			log.error("Master data file not found.", e);
		} catch (IOException e) {
			log.error("Unable to read master data file.", e);
		} catch (IllegalStateException | JsonSyntaxException ex) {
			log.error(masterDataFile.getName(), "{} file is not as per expected format ", ex);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * Send {@link MasterDataQueryModel} to {@link MasterDataDAL} for saving in
	 * database
	 * 
	 * @param masterDataQueryModel
	 */
	private void processQuery(MasterDataQueryModel masterDataQueryModel) {
		log.debug("masterDataQueryModel {}", masterDataQueryModel);
		try {
			masterDataDal.processMasterDataQuery(masterDataQueryModel.getSqlQuery());
		} catch (Exception e) {
			log.error("SQL exception occured while inserting the master data", e);
		}

	}
}