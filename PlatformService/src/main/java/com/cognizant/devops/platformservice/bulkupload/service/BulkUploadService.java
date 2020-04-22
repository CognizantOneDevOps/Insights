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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javassist.expr.Instanceof;

@Service("bulkUploadService")
public class BulkUploadService implements IBulkUpload {
	private static final Logger log = LogManager.getLogger(BulkUploadService.class);

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
	public boolean uploadDataInDatabase(MultipartFile file, String toolName, String label, String insightsTimeField,
			String insightsTimeFormat) throws InsightsCustomException {
		File csvfile = null;
		long filesizeMaxValue = 2097152;
		boolean status = false;
		CSVParser csvParser = null;
		Reader reader = null;
		String originalFilename = file.getOriginalFilename();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("csv")) {
				if (file.getSize() < filesizeMaxValue) {
					csvfile = convertToFile(file);
					CSVFormat format = CSVFormat.newFormat(',').withHeader();
					reader = new FileReader(csvfile);
					csvParser = new CSVParser(reader, format);
					status = parseCsvRecords(csvParser, label, insightsTimeField, insightsTimeFormat);
					log.debug("Final Status is:", status);
				} else {
					throw new InsightsCustomException("File is exceeding the size.");
				}
			} else {
				throw new InsightsCustomException("Invalid file format.");
			}
		} catch (IOException ex) {
			log.debug("Exception while creating csv on server", ex.getMessage());
			throw new InsightsCustomException("Exception while creating csv on server");
		} catch (ArrayIndexOutOfBoundsException ex) {
			log.error("Error in file.", ex.getMessage());
			throw new InsightsCustomException("Error in File Format");
		} catch (Exception ex) {
			log.error("Error in uploading csv file", ex.getMessage());
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
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "UNWIND {props} AS properties " + "CREATE (n:" + label.toUpperCase() + ") "
				+ "SET n = properties";
		Map<String, Integer> headerMap = csvParser.getHeaderMap();
		try {
			if (headerMap.containsKey(insightsTimeField)) {
				for (CSVRecord csvRecord : csvParser.getRecords()) {
					JsonObject json = getToolFileDetails(csvRecord, headerMap, insightsTimeField, insightsTimeFormat);
					nodeProperties.add(json);
				}
			} else {
				throw new InsightsCustomException("Insights Time Field not present in csv file");
			}
			JsonObject graphResponse = dbHandler.bulkCreateNodes(nodeProperties, null, query);
			if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
					.getAsJsonArray().size() > 0) {
				throw new InsightsCustomException("Error while uploading to Neo4j");
			} else {
				return true;
			}
		} catch (Exception ex) {
			log.error("Error in file.", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		}
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
	private JsonObject getToolFileDetails(CSVRecord record, Map<String, Integer> headerMap, String insightsTimeField,
			String insightsTimeFormat) throws InsightsCustomException {
		JsonObject json = new JsonObject();
		String recordFieldValue = null;
		for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
			if (header.getKey() != null) {
				try {
					recordFieldValue = record.get(header.getValue());
					if (header.getKey().equalsIgnoreCase(insightsTimeField)) {
						json = addTimeField(json, recordFieldValue, insightsTimeField, insightsTimeFormat);
					} else {
						if (recordFieldValue.isEmpty()) {
							recordFieldValue = "";
						}
						json.addProperty(header.getKey(), recordFieldValue);
					}
				} catch (Exception ex) {
					log.error("Error in file.", ex.getMessage());
					throw new InsightsCustomException(ex.getMessage());
				}
			}
		}
		return json;
	}

	/**
	 * convert multipart file to file
	 *
	 * @param multipartFile
	 * @return File
	 * @throws IOException
	 */
	private File convertToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multipartFile.getBytes());
		}
		return file;
	}

	/**
	 * get Tool details in json format
	 *
	 * @return Object
	 * @throws InsightsCustomException
	 */
	public Object getToolDetailJson() throws InsightsCustomException {

		String agentPath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		Path dir = Paths.get(agentPath);
		Object config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.TOOLDETAIL_TEMPLATE));
				FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(reader);
			config = obj;
		} catch (IOException ex) {
			log.error("Offline file reading issue", ex.getMessage());
			throw new InsightsCustomException("Offline file reading issue -" + ex.getMessage());
		} catch (Exception ex) {
			log.error("Error in reading csv file", ex.getMessage());
			throw new InsightsCustomException("Error in reading csv file");
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
	public JsonObject addTimeField(JsonObject json, String recordFieldValue, String insightsTimeField,
			String insightsTimeFormat) throws InsightsCustomException {
		long epochTime = 0;
		String dateTimeFromEpoch = null;
		if (!recordFieldValue.isEmpty()) {
			if (!recordFieldValue.contains("T")) {
				if (!recordFieldValue.contains("E")) {
					json.addProperty(PlatformServiceConstants.INSIGHTSTIME, recordFieldValue);
					dateTimeFromEpoch = InsightsUtils.insightsTimeXFormat(Long.parseLong(recordFieldValue));
				} else {
					long timeStringval = new BigDecimal(recordFieldValue).longValue();
					json.addProperty(PlatformServiceConstants.INSIGHTSTIME, timeStringval);
					dateTimeFromEpoch = InsightsUtils.insightsTimeXFormat(timeStringval);
				}
				json.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, dateTimeFromEpoch);
			} else {
				if (!insightsTimeFormat.isEmpty()) {
					epochTime = InsightsUtils.getEpochTime(recordFieldValue, insightsTimeFormat);
					json.addProperty(PlatformServiceConstants.INSIGHTSTIME, epochTime);
					if (insightsTimeFormat.equals(InsightsUtils.DATE_TIME_FORMAT)) {
						json.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, recordFieldValue);
					} else {
						dateTimeFromEpoch = InsightsUtils.insightsTimeXFormat(epochTime);
						json.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, dateTimeFromEpoch);
					}
				} else {
					throw new InsightsCustomException("Provide Insight Time Format for this file");
				}
			}
		} else {
			throw new InsightsCustomException("Null values in column " + insightsTimeField);
		}
		return json;
	}
}