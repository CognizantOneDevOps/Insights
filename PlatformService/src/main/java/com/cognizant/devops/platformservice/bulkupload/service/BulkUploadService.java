/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/

package com.cognizant.devops.platformservice.bulkupload.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformservice.config.PlatformServiceStatusProvider;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Service("bulkUploadService")
public class BulkUploadService implements IBulkUpload {
	private static final Logger log = LogManager.getLogger(BulkUploadService.class);
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();

	/**
	 * method performs multipart file to File conversion and does CSV file checks
	 * and calls parseCsvRecords()
	 *
	 * @param file
	 * @param toolName
	 * @param label
	 * @param insightsTimeField
	 * @param insightsTimeFormat
	 * @return boolean
	 * @throws InsightsCustomException
	 */
	@Override
	public boolean uploadDataInDatabase(MultipartFile file, String toolName, String label, String insightsTimeField,
			String insightsTimeFormat) throws InsightsCustomException {
		long filesizeMaxValue = 2097152;
		boolean status = false;
		CSVParser csvParser = null;
		Reader reader = null;
		String originalFilename = file.getOriginalFilename();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("csv")) {
				if (file.getSize() < filesizeMaxValue) {
					InputStream is = file.getInputStream();
					CSVFormat format = CSVFormat.newFormat(',').withHeader();
					try {
						reader = new InputStreamReader(is);
						csvParser = new CSVParser(reader, format);
					} catch (IOException e) {
						log.debug("Not able to read file");
					}
					status = parseCsvRecords(csvParser, label, insightsTimeField, insightsTimeFormat);
					log.debug("Final Status is: {}", status);
				} else {
					throw new InsightsCustomException("File is exceeding the size.");
				}
			} else {
				throw new InsightsCustomException("Invalid file format.");
			}
		} catch (IOException ex) {
			log.error("Exception while creating csv on server.. {} ", ex.getMessage());
			throw new InsightsCustomException("Exception while creating csv on server");
		} catch (ArrayIndexOutOfBoundsException ex) {
			log.error("Error in file. {}", ex.getMessage());
			throw new InsightsCustomException("Error in File Format");
		} catch (InsightsCustomException ex) {
			log.error("Error in csv file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		} catch (Exception ex) {
			log.error("Error in uploading csv file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (csvParser != null) {
					csvParser.close();
				}
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		return status;
	}

	/**
	 * Send records to getToolFileDetails() and store the output in neo4j database
	 *
	 * @param csvParser
	 * @param label
	 * @param insightsTimeField
	 * @param insightsTimeFormat
	 * @return boolean
	 * @throws InsightsCustomException
	 */
	private boolean parseCsvRecords(CSVParser csvParser, String label, String insightsTimeField,
			String insightsTimeFormat) throws InsightsCustomException {
		List<JsonObject> nodeProperties = new ArrayList<>();
		String query = "UNWIND $props AS properties " + "CREATE (n:" + label.toUpperCase() + ") "
				+ "SET n = properties";
		Map<String, Integer> headerMap = csvParser.getHeaderMap();
		try {
			if (headerMap.containsKey("")) {
				throw new InsightsCustomException("Error in file.");
			} else if (headerMap.containsKey(insightsTimeField)) {
				for (CSVRecord csvRecord : csvParser.getRecords()) {
					JsonObject json = getCSVRecordDetails(csvRecord, headerMap, insightsTimeField, insightsTimeFormat);
					nodeProperties.add(json);
				}
			} else {
				throw new InsightsCustomException("Insights Time Field not present in csv file");
			}
			insertDataInDatabase(nodeProperties, query);
			return true;
		} catch (Exception ex) {
			log.error("Error while parsing the .CSV records. {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
	}

	/**
	 * Method to insert the obtained JSON into Neo4j
	 * 
	 * @param dataList
	 * @param cypherQuery
	 * @return
	 * @throws InsightsCustomException
	 */
	private void insertDataInDatabase(List<JsonObject> dataList, String cypherQuery) throws InsightsCustomException {
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			List<List<JsonObject>> partitionList = partitionList(dataList, 1000);
			for (List<JsonObject> chunk : partitionList) {
				JsonObject graphResponse = dbHandler.bulkCreateNodes(chunk, cypherQuery);
				if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
						.getAsJsonArray().size() > 0) {
					throw new InsightsCustomException("Error while uploading to Neo4j");
				}
			}
		} catch (InsightsCustomException ex) {
			log.error("Neo4j is not responding {}..", ex.getMessage());
			throw new InsightsCustomException("Error while uploading to Neo4j");
		}
	}

	private <T> List<List<T>> partitionList(List<T> list, final int size) {
		List<List<T>> parts = new ArrayList<>();
		final int N = list.size();
		for (int i = 0; i < N; i += size) {
			parts.add(getPartitionSubList(list, i, size, N));
		}
		return parts;
	}

	private <T> ArrayList<T> getPartitionSubList(List<T> list, int index, int size, final int N) {
		return new ArrayList<T>(list.subList(index, Math.min(N, index + size)));
	}

	/**
	 * create json to be stored in neo4j
	 *
	 * @param record
	 * @param headerMap
	 * @param insightsTimeField
	 * @param insightsTimeFormat
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	private JsonObject getCSVRecordDetails(CSVRecord record, Map<String, Integer> headerMap, String insightsTimeField,
			String insightsTimeFormat) throws InsightsCustomException {
		JsonObject json = new JsonObject();
		String recordFieldValue = null;
		for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
			try {
				recordFieldValue = record.get(header.getValue());
				if (header.getKey().equalsIgnoreCase(insightsTimeField)) {
					if (recordFieldValue.isEmpty()) {
						throw new InsightsCustomException(
								"Null values in column " + insightsTimeField + " record  " + record);
					}
					addPropertyInJson(json, recordFieldValue, header.getKey());
					addTimePropertiesInJson(json, recordFieldValue, insightsTimeFormat);
				} else if (recordFieldValue.isEmpty()) {
					recordFieldValue = "";
					json.addProperty(header.getKey(), recordFieldValue);
				} else {
					addPropertyInJson(json, recordFieldValue, header.getKey());
				}
			} catch (InsightsCustomException ex) {
				log.error("insightsTimeFormat missing {}", ex.getMessage());
				throw new InsightsCustomException(ex.getMessage());
			}
		}
		return json;
	}

	/**
	 * Creates JSON accordingly. Saves values of properties into Numeric/String
	 * format as required.
	 * 
	 * @param json
	 * @param recordFieldValue
	 * @param key
	 * @return
	 */
	private void addPropertyInJson(JsonObject json, String recordFieldValue, String key) {
		String regex = "^([+-]?\\d*\\.?\\d*)$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(recordFieldValue);
		if (m.find() && m.group().equals(recordFieldValue)) {
			if (isInteger(recordFieldValue)) {
				json.addProperty(key, Long.parseLong(recordFieldValue));
			} else {
				json.addProperty(key, Double.parseDouble(recordFieldValue));
			}
		} else {
			json.addProperty(key, recordFieldValue);
		}
	}

	/**
	 * To check whether the record values are integer or not.
	 * 
	 * @param checkNumber
	 * @return
	 */
	private boolean isInteger(String checkNumber) {
		try {
			Integer.parseInt(checkNumber);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Check whether the time is epoch or not
	 * 
	 * @param checkNumber
	 * @return
	 */
	private boolean isEpoch(String checkNumber) {
		try {
			new BigDecimal(checkNumber);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * get Tool details in json format
	 *
	 * @return Object
	 * @throws InsightsCustomException
	 */
	@Override
	public Object getToolDetailJson() throws InsightsCustomException {
		Object config = null;
		try {
			List<InsightsConfigFiles> configFile = configFilesDAL
					.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.TOOLDETAIL.name());
			String configFileData = new String(configFile.get(0).getFileData(), StandardCharsets.UTF_8);
			Object obj = JsonUtils.parseString(configFileData);
			config = obj;
			PlatformServiceStatusProvider.getInstance().createPlatformServiceStatusNode(
					"ToolDetail.json is successfully loaded.", PlatformServiceConstants.SUCCESS);
		} catch (JsonSyntaxException ex) {
			log.error("ToolDetail.json is not formatted {}", ex.getMessage());
			PlatformServiceStatusProvider.getInstance().createPlatformServiceStatusNode(
					"ToolDetail.json is not formatted.", PlatformServiceConstants.FAILURE);
			throw new InsightsCustomException("ToolDetail.json is not formatted");
		} catch (Exception ex) {
			log.error("Error in loading ToolDetail.json {}", ex.getMessage());
			PlatformServiceStatusProvider.getInstance().createPlatformServiceStatusNode(
					"Error in loading ToolDetail.json.", PlatformServiceConstants.FAILURE);
			throw new InsightsCustomException("Could not load ToolDetail.json. Please check whether the file has been uploaded in Configuration File Management.");
		}
		return config;
	}

	/**
	 * add InsightTime and InsightTimeX to json with required format
	 *
	 * @param json
	 * @param recordFieldValue
	 * @param insightsTimeField
	 * @param insightsTimeFormat
	 * @return json
	 * @throws InsightsCustomException
	 */
	public void addTimePropertiesInJson(JsonObject json, String recordFieldValue, String insightsTimeFormat)
			throws InsightsCustomException {
		long epochTime = 0;
		String dateTimeFromEpoch = null;
		if (isEpoch(recordFieldValue)) {
			long timeStringval = new BigDecimal(recordFieldValue).longValue();
			json.addProperty(PlatformServiceConstants.INSIGHTSTIME, timeStringval);
			dateTimeFromEpoch = InsightsUtils.insightsTimeXFormatFromSeconds(timeStringval);
			json.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, dateTimeFromEpoch);
		} else if (insightsTimeFormat.isEmpty()) {
			throw new InsightsCustomException("Provide Insight Time Format for this file");
		} else {
			try {
				epochTime = InsightsUtils.getEpochTime(recordFieldValue, insightsTimeFormat);
				json.addProperty(PlatformServiceConstants.INSIGHTSTIME, epochTime);
				if (insightsTimeFormat.equals(InsightsUtils.DATE_TIME_FORMAT)) {
					json.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, recordFieldValue);
				} else {
					dateTimeFromEpoch = InsightsUtils.insightsTimeXFormatFromSeconds(epochTime);
					json.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, dateTimeFromEpoch);
				}
			} catch (InsightsCustomException ex) {
				log.error("Mismatched Timeformat {}", ex.getMessage());
				throw new InsightsCustomException("Mismatched Timeformat");
			}
		}
	}
}