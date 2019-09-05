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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("bulkUploadService")
public class BulkUploadService implements IBulkUpload {
	private static final Logger log = LogManager.getLogger(BulkUploadService.class);

	public boolean uploadDataInDatabase(MultipartFile file, String toolName, String label)
			throws InsightsCustomException, IOException {
		File csvfile = null;
		long filesizeMaxValue = 2097152;
		boolean status = false;
		String originalFilename = file.getOriginalFilename();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("csv")) {
				if (file.getSize() < filesizeMaxValue) {
					csvfile = convertToFile(file);
					CSVFormat format = CSVFormat.newFormat(',').withHeader();
					Reader reader = new FileReader(csvfile);
					CSVParser csvParser = new CSVParser(reader, format);
					Neo4jDBHandler dbHandler = new Neo4jDBHandler();
					Map<String, Integer> headerMap = csvParser.getHeaderMap();
					String query = "UNWIND {props} AS properties " + "CREATE (n:" + label.toUpperCase() + ") "
							+ "SET n = properties";
					status = parseCsvRecords(csvParser, dbHandler, headerMap, query);
				} else {
					throw new InsightsCustomException("File is exceeding the size.");
				}
			} else {
				throw new InsightsCustomException("Invalid file format.");
			}
		} catch (IOException ex) {
			log.debug("Exception while creating csv on server", ex.getMessage());
			throw new InsightsCustomException("Exception while creating csv on server");
		} catch (InsightsCustomException ex) {
			log.error("Error in file.", ex);
			throw new InsightsCustomException("Error in File Format");
		} catch (ArrayIndexOutOfBoundsException e) {
			log.error("Error in file.", e.getMessage());
			throw new InsightsCustomException("Error in File Format");
		} catch (Exception e) {
			status = false;
			log.error("Error in uploading csv file", e.getMessage());
			throw new InsightsCustomException("Error in uploading csv file");
		}
		return status;
	}

	private boolean parseCsvRecords(CSVParser csvParser, Neo4jDBHandler dbHandler, Map<String, Integer> headerMap,
			String query)
			throws IOException, GraphDBException, InsightsCustomException, ArrayIndexOutOfBoundsException {
		List<JsonObject> nodeProperties = new ArrayList<>();
		for (CSVRecord csvRecord : csvParser.getRecords()) {
			try {
				JsonObject json = getToolFileDetails(csvRecord, headerMap);
				nodeProperties.add(json);
			} catch (InsightsCustomException ex) {
				log.error("Error in file.", ex);
				throw new InsightsCustomException("Error in File Format");
			} catch (ArrayIndexOutOfBoundsException ex) {
				log.error("Error in file.", ex);
				throw new InsightsCustomException("Error in File Format");
			} catch (Exception e) {
				log.error(e);
				throw new InsightsCustomException(e.getMessage());
			}
		}
		JsonObject graphResponse = dbHandler.bulkCreateNodes(nodeProperties, null, query);
		if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
				.getAsJsonArray().size() > 0) {
			return false;
		} else {
			return true;
		}
	}

	private JsonObject getToolFileDetails(CSVRecord record, Map<String, Integer> headerMap)
			throws InsightsCustomException {
		JsonObject json = new JsonObject();
		for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
			if (header.getKey() != null) {
				try {
					json.addProperty(header.getKey(), record.get(header.getValue()));
				} catch (ArrayIndexOutOfBoundsException ex) {
					log.error("Error in file.", ex.getMessage());
					throw new InsightsCustomException("Error in File Format");
				} catch (Exception e) {
					log.error("Error " + e + " at Header Key..." + header.getKey());
					throw new InsightsCustomException("Error " + e + " at Header Key..." + header.getKey());
				}
			}
		}
		return json;
	}

	private File convertToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multipartFile.getBytes());
		}
		return file;
	}

	public Object getToolDetailJson() throws InsightsCustomException {
		// TODO Auto-generated method stub
		
		String agentPath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		Path dir = Paths.get(agentPath);
		Object config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.TOOLDETAIL_TEMPLATE));
				FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(reader);
			config = obj;
		} catch (IOException e) {
			log.error("Offline file reading issue", e.getMessage());
			throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
		} catch (Exception e) {
			log.error("Error in reading csv file", e.getMessage());
			throw new InsightsCustomException("Error in reading csv file");
		}
		return config;
	}
}