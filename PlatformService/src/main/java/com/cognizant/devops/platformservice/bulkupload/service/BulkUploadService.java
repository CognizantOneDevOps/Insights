/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.cognizant.devops.platformservice.bulkupload.service;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("bulkUploadService")
public class BulkUploadService {

	
	private static final BulkUploadService bulkUploadService = new BulkUploadService();
	private static final Logger log = LogManager.getLogger(BulkUploadService.class);

	private BulkUploadService() {

	}

	public static BulkUploadService getInstance() {
		return bulkUploadService;
	}

	public boolean createBulkUploadMetaData(MultipartFile file,String toolName , String label) throws InsightsCustomException {

		File csvfile = null;
		boolean status = false;
		try {
			csvfile = convertToFile(file);
		} catch (IOException ex) {
			log.debug("Exception while creating csv on server", ex);
			return status;
		}
		CSVFormat format = CSVFormat.newFormat(',').withHeader();
		try (Reader reader = new FileReader(csvfile); CSVParser csvParser = new CSVParser(reader, format);) {

			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			Map<String, Integer> headerMap = csvParser.getHeaderMap();
			
			String query = "UNWIND {props} AS properties " + "CREATE (n:"+label.toUpperCase()+":"+ toolName.toUpperCase()+":CSVDATA) " + "SET n = properties"; 
			status = parseCsvRecords(status, csvParser, dbHandler, headerMap, query);

		} catch (FileNotFoundException e) {
			log.error("File not found Exception in uploading csv file", e);
			throw new InsightsCustomException("File not found Exception in uploading csv file");
		} catch (IOException | GraphDBException e) {
			log.error("IOException in uploading csv file", e);
			throw new InsightsCustomException("IOException in uploading csv file");
		} catch (InsightsCustomException e) {
			log.error("Duplicate record in CSV file", e);
			throw new InsightsCustomException("Duplicate record in CSV file");
		}
		return status;

	}

	private boolean parseCsvRecords(boolean status, CSVParser csvParser, Neo4jDBHandler dbHandler,
			Map<String, Integer> headerMap, String query)
			throws IOException, GraphDBException, InsightsCustomException {
		List<JsonObject> nodeProperties = new ArrayList<>();
		
		for (CSVRecord csvRecord : csvParser.getRecords()) {
						
			
			JsonObject json = getToolFileDetails(csvRecord, headerMap);
			nodeProperties.add(json);
			
		}
		JsonObject graphResponse = dbHandler.bulkCreateNodes(nodeProperties, null, query);
		if (graphResponse.get("response").getAsJsonObject().get("errors")
				.getAsJsonArray().size() > 0) {
			log.error(graphResponse);
			return status;
		}

		return true;
	}



	
	

	private JsonObject getToolFileDetails(CSVRecord record, Map<String, Integer> headerMap) {
		JsonObject json = new JsonObject();
		for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
			log.debug(header);
			log.error(DatataggingConstants.METADATA_ID);
			if(DatataggingConstants.METADATA_ID.equalsIgnoreCase(header.getKey())
			&&(header.getKey() != null )) {
				log.error("HEADER"+header.getKey());
				if (record.get(header.getValue()) != null && !record.get(header.getValue()).isEmpty()) {
					json.addProperty(header.getKey(), Integer.valueOf(record.get(header.getValue())));
				} else {
					json.addProperty(header.getKey(), record.get(header.getValue()));
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
		//Path dir = Paths.get(filePath);
		String agentPath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		Path dir = Paths.get(agentPath);
		Object config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.TOOLDETAIL_TEMPLATE));
				FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {

			JsonParser parser = new JsonParser();
			Object obj = parser.parse(reader);
			//config = ((JsonArray) obj).toString();
			config=obj;
		} catch (IOException e) {
			log.error("Offline file reading issue", e);
			throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
		}
		log.error(agentPath);
		log.error("config"+config); 
		return config;
	} 

}