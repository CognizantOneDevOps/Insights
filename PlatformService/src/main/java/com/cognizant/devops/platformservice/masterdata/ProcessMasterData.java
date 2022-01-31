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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.masterdata.MasterDataDAL;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ProcessMasterData {

	private static Logger log = LogManager.getLogger(ProcessMasterData.class);
	private static final String JSON_FILE_EXTENSION = "json";
	MasterDataDAL masterDataDal = new MasterDataDAL();
	List<String> queryList = new ArrayList<>();
	
	public static final List<String> MASTER_FILE_LIST = Collections.unmodifiableList(Arrays.asList("agent.json","ROI_Tools.json"
			,"ScheduledTask.json","workflow.json"));

	/**
	 * Load json file for masterdata query processing
	 * 
	 * @return masterDataFileCount
	 * @throws Exception 
	 */
	public int executeMasterDataProcessing() throws Exception {
		int masterDataFileCount = 0;
		File queryFolderPath;
		
		try {
			
			queryFolderPath = new File(getClass().getClassLoader().getResource("masterdata").getFile());

			for (File masterDataFile : queryFolderPath.listFiles()) {
				if (hasJsonFileExtension(masterDataFile.getName()) &&masterDataFile.isFile() && MASTER_FILE_LIST.contains(masterDataFile.getName()) ) { // this line removes other directories/folders
						String queryJosn = new String(Files.readAllBytes(masterDataFile.toPath()));
						String validatedstr = ValidationUtils.cleanXSS(queryJosn.replace(PlatformServiceConstants.LF, PlatformServiceConstants.EMPTY).replace(PlatformServiceConstants.CR, PlatformServiceConstants.EMPTY).replace(PlatformServiceConstants.TAB, PlatformServiceConstants.EMPTY));
						queryList.add(validatedstr);
				}else {
					throw new InsightsCustomException("Not a valid master data file ");
				}
			}
			
			for (String queryJsonStr : queryList) {
				String validatedstr = ValidationUtils.cleanXSS(queryJsonStr.replace(PlatformServiceConstants.LF, PlatformServiceConstants.EMPTY).replace(PlatformServiceConstants.CR, PlatformServiceConstants.EMPTY).replace(PlatformServiceConstants.TAB, PlatformServiceConstants.EMPTY));
				processMasterDataConfiguration(validatedstr);
			}
		} catch (Exception e) {
			log.error("Error While execution master data query ",e);
			throw e;
		}
		return masterDataFileCount;
	}
	
	/**
	 * Execute Trigger statements during startup
	 * @return triggerResponse
	 * @throws InsightsCustomException 
	 * @throws Exception 
	 */
	public String executeTriggerStatement() throws InsightsCustomException {
		String response = "";
		File triggerStatementFile = new File(getClass().getClassLoader().getResource("triggerStatements.json").getFile());
		try{
			String template = new String(Files.readAllBytes(triggerStatementFile.toPath())).replace(PlatformServiceConstants.LF, PlatformServiceConstants.EMPTY).replace(PlatformServiceConstants.CR, PlatformServiceConstants.EMPTY).replace(PlatformServiceConstants.TAB, PlatformServiceConstants.EMPTY);
			String validatedstr = ValidationUtils.cleanXSS(template);
			GraphDBHandler dbHandler = new GraphDBHandler();
			response = dbHandler.executeCypherQueryRaw(validatedstr);
			log.debug(response);	
		} catch (IOException e) {
			response = "Unable to read the trigger statement file while creating UUID.";
			log.error(" IOException While executeTriggerStatement {} {} ",response, e.getMessage());
			throw new InsightsCustomException(response);
		} catch (InsightsCustomException e) {
			response = "Failed to execute the trigger query while creating UUID.";
			log.error("InsightsCustomException while  executeTriggerStatement {} {} ",response, e.getMessage());
			throw new InsightsCustomException(response);
		}  catch (Exception e) {
			response  = "Error while creating UUID trigger.";
			log.error("Exception with executeTriggerStatement {} {} ",response, e.getMessage());
			throw new InsightsCustomException(response);
		}
		return response;
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
	 * @throws Exception 
	 */
	private Boolean processMasterDataConfiguration(String queryJosn) throws Exception {
		try {	
				String validatedstr = ValidationUtils.cleanXSS(queryJosn);
				List<MasterDataQueryModel> masterDataQueryModels =  new Gson().fromJson(validatedstr,
						new TypeToken<List<MasterDataQueryModel>>() {}.getType());
				for (MasterDataQueryModel masterDataQueryModel : masterDataQueryModels) {
					processQuery(masterDataQueryModel);
				}
		} catch (FileNotFoundException e) {
			log.error(e);
			log.error("Master data file not found.", e);
		} catch (IOException e) {
			log.error(e);
			log.error("Unable to read master data file.", e);
		} catch (IllegalStateException | JsonSyntaxException ex) {
			log.error(ex);
			log.error("Error while processing string is not as per expected format {}", ex);
			return Boolean.FALSE;
		}catch (Exception ex) {
			log.error(ex);
			log.error("Exception while processing ", ex);
			throw ex ;
		}
		return Boolean.TRUE;
	}

	/**
	 * Send {@link MasterDataQueryModel} to {@link MasterDataDAL} for saving in
	 * database
	 * 
	 * @param masterDataQueryModel
	 * @throws Exception 
	 */
	private void processQuery(MasterDataQueryModel masterDataQueryModel) throws Exception {
			log.debug("masterDataQueryModel {}", masterDataQueryModel);
			masterDataDal.processMasterDataQuery(masterDataQueryModel.getSqlQuery());
	}
}