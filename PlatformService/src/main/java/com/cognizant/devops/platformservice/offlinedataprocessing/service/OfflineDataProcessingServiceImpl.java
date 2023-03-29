/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.offlinedataprocessing.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfigDAL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service("offlineDataProcessingService")
public class OfflineDataProcessingServiceImpl implements OfflineDataProcessingService {

	private static final Logger log = LogManager.getLogger(OfflineDataProcessingServiceImpl.class);
	private static final String DATE_TIME_FORMAT = "yyyy/MM/dd hh:mm a";
	InsightsOfflineConfigDAL insightsOfflineConfigDAL = new InsightsOfflineConfigDAL();

	/**
	 * Used to read Offline Data definition from file and store it in DB
	 * 
	 * @param file
	 * @return
	 * @throws InsightsCustomException
	 */
	@Override
	public String saveOfflineDataInDatabase(MultipartFile file) throws InsightsCustomException {
		String returnMessage = "";
		String originalFilename = StringEscapeUtils.escapeHtml(ValidationUtils.cleanXSS(file.getOriginalFilename()));
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("json")) {
				String offlineJson = readMultipartFileAndCreateJson(file);
				JsonArray offlineJsonArray = JsonUtils.parseStringAsJsonArray(offlineJson);
				returnMessage = saveBulkOfflineDefinition(offlineJsonArray);
			} else {
				log.error("Invalid file format. ");
				throw new InsightsCustomException("Invalid Offline Data file format.");
			}
		} catch (Exception ex) {
			log.error("Error in uploading Offline Data Config file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
		return returnMessage;
	}

	/**
	 * Read multipart file and create Json String from it
	 * 
	 * @param file
	 * @return String
	 * @throws InsightsCustomException
	 */
	private String readMultipartFileAndCreateJson(MultipartFile file) throws InsightsCustomException {
		try {
			InputStream inputStream = file.getInputStream();
			StringBuilder json = new StringBuilder();
			new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(json::append);

			return json.toString();
		} catch (Exception e) {
			log.error("Error while reading file {} ", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}

	}

	/**
	 * Method to save Offline Data during Bulk save
	 * 
	 * @param offlineResultList
	 * @return
	 * @throws InsightsCustomException
	 */
	public String saveBulkOfflineDefinition(JsonArray offlineJsonArray) throws InsightsCustomException {
		String returnMessage;
		int totalOfflineRecord = offlineJsonArray.size();
		JsonArray successMessageArray = new JsonArray();
		JsonArray errorMessageArray = new JsonArray();
		if (totalOfflineRecord > 0) {
			for (JsonElement jsonElement : offlineJsonArray) {
				String queryName = jsonElement.getAsJsonObject().get("queryName").getAsString();
				try {
					JsonObject registerOfflineJson = jsonElement.getAsJsonObject();
					if(offlineValidation(registerOfflineJson)) {
						queryName = saveOfflineDefinition(registerOfflineJson);
						successMessageArray.add(queryName);
					} else {
						errorMessageArray.add(queryName);
					}
				} catch (Exception e) {
					log.error("Error : Query Name {} not created, with Exception {}", queryName, e.getMessage());
					errorMessageArray.add(queryName);
				}
			}
			if (successMessageArray.size() == totalOfflineRecord) {
				returnMessage = "All Offline Data records are inserted successfully. ";
			} else if (errorMessageArray.size() == totalOfflineRecord) {
				returnMessage = "All Offline Data records are not inserted, Please check Platform Service log for more detail. ";
			} else {
				returnMessage = "Number of Offline Data inserted successfully are " + successMessageArray.size()
						+ " and not inserted are " + errorMessageArray
						+ ", Please check Platform Service log for more detail.";
			}
		} else {
			returnMessage = "No Offline Data definition found in request json";
		}
		return returnMessage;
	}
	
	/**
	 * Method to validate Offline Data during Bulk save
	 * 
	 * @param offlineResultList
	 * @return
	 * @throws InsightsCustomException
	 */
	public boolean offlineValidation(JsonObject registerOfflineJson) {
		Pattern pattern = Pattern.compile("INDEX", Pattern.CASE_INSENSITIVE);
		if (registerOfflineJson.get("queryName") == null
				|| registerOfflineJson.get("queryName").getAsString().isEmpty()) {
			return false;
		} else if (registerOfflineJson.get("toolName") == null
				|| registerOfflineJson.get("toolName").getAsString().isEmpty()) {
			String queryName = registerOfflineJson.get("queryName").getAsString();
			log.error("Error in tool name for query - {}", queryName);
			return false;
		} else if (registerOfflineJson.get("cronSchedule") == null
				|| registerOfflineJson.get("cronSchedule").getAsString().isEmpty()) {
			String queryName = registerOfflineJson.get("queryName").getAsString();
			log.error("Error in cronSchedule for query - {}", queryName);
			return false;
		} else if (registerOfflineJson.get("cypherQuery").getAsString().isEmpty()
				|| pattern.matcher(registerOfflineJson.get("cypherQuery").getAsString()).find()) {
			String queryName = registerOfflineJson.get("queryName").getAsString();
			log.error("Error in cypherQuery for query - {}", queryName);
			return false;
		}
		return true;
	}

	/**
	 * Used to store individual Offline Data definition
	 * 
	 * @param registerOfflineJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public String saveOfflineDefinition(JsonObject registerOfflineJson) throws InsightsCustomException {
		String queryName = "-1";
		try {
			InsightsOfflineConfig offlineConfig = new InsightsOfflineConfig();
			queryName = registerOfflineJson.get("queryName").getAsString();
			InsightsOfflineConfig offlineConfigData = insightsOfflineConfigDAL.getOfflineDataConfig(queryName);
			if (offlineConfigData != null) {
				throw new InsightsCustomException("Offline Data already exists");
			}
			String cypherQuery = registerOfflineJson.get("cypherQuery").getAsString();
			String toolName = registerOfflineJson.get("toolName").getAsString();
			Long lastRunTime = 0l;
			if(!registerOfflineJson.get("lastExecutionTime").getAsString().isEmpty()) {
				String lastExecutionTime = registerOfflineJson.get("lastExecutionTime").getAsString();
				lastRunTime=convertToEpochSeconds(lastExecutionTime, DATE_TIME_FORMAT);
				
			}

			String cronSchedule = registerOfflineJson.get("cronSchedule").getAsString();
			if(!cronSchedule.isEmpty() && !org.quartz.CronExpression.isValidExpression(cronSchedule)) {
				throw new InsightsCustomException("Cron Expression is invalid");
			}

			offlineConfig.setIsActive(true);
			offlineConfig.setQueryName(queryName);
			offlineConfig.setCypherQuery(cypherQuery);
			offlineConfig.setToolName(toolName);
			offlineConfig.setCronSchedule(cronSchedule);
			offlineConfig.setLastRunTime(lastRunTime);
			offlineConfig.setRetryCount(0);
			offlineConfig.setQueryProcessingTime(null);
			offlineConfig.setStatus(null);
			offlineConfig.setMessage(null);

			insightsOfflineConfigDAL.saveOfflineDataConfig(offlineConfig);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return queryName;

	}

	/**
	 * Used to update individual Offline definition
	 * 
	 * @param registerOfflieJson
	 * @return queryName
	 * @throws InsightsCustomException
	 */
	public String updateOfflineDefinition(JsonObject updateOfflineJson) throws InsightsCustomException {
		String queryName = "-1";
		try {
			queryName = updateOfflineJson.get("queryName").getAsString();
			InsightsOfflineConfig offlineExistingConfig = insightsOfflineConfigDAL.getOfflineDataConfig(queryName);
			if (offlineExistingConfig == null) {
				throw new InsightsCustomException("While update, Offline definition not exists");
			} else {
				String cypherQuery = updateOfflineJson.get("cypherQuery").getAsString();
				String cronSchedule = updateOfflineJson.get("cronSchedule").getAsString();
				if (!org.quartz.CronExpression.isValidExpression(cronSchedule)) {
					throw new InsightsCustomException("Cron Expression is invalid!");
				}
				offlineExistingConfig.setCronSchedule(cronSchedule);
				offlineExistingConfig.setCypherQuery(cypherQuery);
				insightsOfflineConfigDAL.updateOfflineConfig(offlineExistingConfig);
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return queryName;
	}

	/**
	 * Used to update individual Offline definition status
	 * 
	 * @param registerOfflieJson
	 * @return
	 * @throws InsightsCustomException
	 */
	@Override
	public void updateOfflineConfigStatus(JsonObject configJson) throws InsightsCustomException {
		try {
			String queryName = configJson.get("queryName").getAsString();
			InsightsOfflineConfig offlineConfig = insightsOfflineConfigDAL.getOfflineDataConfig(queryName);
			offlineConfig.setIsActive(configJson.get("isActive").getAsBoolean());
			insightsOfflineConfigDAL.updateOfflineConfig(offlineConfig);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(" Error while updating outcome status " + e.getMessage());
		}
	}

	/**
	 * Used to delete individual Offline definition
	 * 
	 * @param deleteOfflineJson
	 * @return boolean
	 * @throws InsightsCustomException
	 */
	public boolean deleteOfflineDefinition(JsonObject deleteOfflineJson) throws InsightsCustomException {
		String queryName = "-1";
		boolean isRecordDeleted = Boolean.FALSE;
		try {
			queryName = deleteOfflineJson.get("queryName").getAsString();
			InsightsOfflineConfig offlineExistingConfig = insightsOfflineConfigDAL.getOfflineDataConfig(queryName);
			if (offlineExistingConfig != null) {
				insightsOfflineConfigDAL.deleteOfflinebyQueryName(queryName);
				isRecordDeleted = Boolean.TRUE;
			} else {
				throw new InsightsCustomException("Offline definition not exists");
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return isRecordDeleted;
	}

	/**
	 * API use to return all Offline Data list
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public List<InsightsOfflineConfig> getOfflineDataList() throws InsightsCustomException {
		List<InsightsOfflineConfig> offlineConfigList = new ArrayList<>();
		try {
			offlineConfigList = insightsOfflineConfigDAL.getAllOfflineConfig();
			return offlineConfigList;
		} catch (Exception e) {
			log.error("Error getting all OfflineDataList...", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Used to get Epoch time (sec) from date
	 * 
	 * @return long
	 * @throws InsightsCustomException
	 */
	private static long convertToEpochSeconds(String datetime, String dateFormat) throws InsightsCustomException {
		try {
			return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern(dateFormat))
					.atZone(InsightsUtils.zoneIdUTC).toInstant().getEpochSecond();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}
	
}
