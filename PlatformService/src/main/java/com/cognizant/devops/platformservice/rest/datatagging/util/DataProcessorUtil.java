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
package com.cognizant.devops.platformservice.rest.datatagging.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataProcessorUtil {
	private static final DataProcessorUtil dataProcessorUtil = new DataProcessorUtil();
	private static final Logger log = LogManager.getLogger(DataProcessorUtil.class);

	private DataProcessorUtil() {

	}

	public static DataProcessorUtil getInstance() {
		return dataProcessorUtil;
	}

	public boolean createBusinessHierarchyMetaData(MultipartFile file) throws InsightsCustomException {

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

			GraphDBHandler dbHandler = new GraphDBHandler();
			Map<String, Integer> headerMap = csvParser.getHeaderMap();
			dbHandler.executeCypherQuery("CREATE CONSTRAINT ON (n:METADATA) ASSERT n.metadata_id  IS UNIQUE");
			String query = "UNWIND {props} AS properties " + "CREATE (n:METADATA:DATATAGGING) " + "SET n = properties";
			status = parseCsvRecords(status, csvParser, dbHandler, headerMap, query);

		} catch (FileNotFoundException e) {
			log.error("File not found Exception in uploading csv file", e);
			throw new InsightsCustomException("File not found Exception in uploading csv file");
		} catch (IOException e) {
			log.error("IOException in uploading csv file", e);
			throw new InsightsCustomException("IOException in uploading csv file");
		} catch (InsightsCustomException e) {
			log.error("Duplicate record in CSV file", e);
			throw new InsightsCustomException("Duplicate record in CSV file");
		}
		return status;

	}

	private boolean parseCsvRecords(boolean status, CSVParser csvParser, GraphDBHandler dbHandler,
			Map<String, Integer> headerMap, String query)
			throws IOException,InsightsCustomException {
		List<JsonObject> nodeProperties = new ArrayList<>();
		List<String> combo = new ArrayList<>();
		getCurrentRecords(combo, dbHandler);
		int record = 0;
		for (CSVRecord csvRecord : csvParser.getRecords()) {
			JsonObject json = getHierachyDetails(csvRecord, headerMap);
			record = record + 1;
			json.addProperty(DatataggingConstants.METADATA_ID, Instant.now().getNano() + record);
			json.addProperty(DatataggingConstants.CREATIONDATE, Instant.now().toEpochMilli());
			nodeProperties.add(json);
			updateComboList(combo, json);
		}
		JsonObject graphResponse = dbHandler.bulkCreateNodes(nodeProperties, null, query);
		if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
				.getAsJsonArray().size() > 0) {
			log.error(graphResponse);
			return status;
		}

		return true;
	}

	public boolean updateHiearchyProperty(MultipartFile file) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		File csvfile = null;
		boolean status = false;
		try {
			csvfile = convertToFile(file);
		} catch (IOException e) {
			log.debug("Exception while creating csv on server", e);
			return status;
		}
		String label = "METADATA:DATATAGGING";
		CSVFormat format = CSVFormat.newFormat(',').withHeader();

		try (Reader reader = new FileReader(csvfile); CSVParser csvParser = new CSVParser(reader, format);) {
			Map<String, Integer> headerMap = csvParser.getHeaderMap();
			List<JsonObject> editList = new ArrayList<>();
			List<JsonObject> deleteList = new ArrayList<>();
			for (CSVRecord record : csvParser) {

				if (record.get(DatataggingConstants.ACTION) != null
						&& record.get(DatataggingConstants.ACTION).equalsIgnoreCase("edit")) {
					JsonObject json = getHierachyDetails(record, headerMap);
					editList.add(json);
					List<String> combo = new ArrayList<>();
					updateComboList(combo, json);
				} else if (record.get(DatataggingConstants.ACTION) != null
						&& record.get(DatataggingConstants.ACTION).equalsIgnoreCase("delete")) {
					JsonObject json = getHierachyDetails(record, headerMap);
					deleteList.add(json);
				}

			}
			if (!editList.isEmpty()) {
				status = updateMedataNodes(dbHandler, status, label, editList);
			}
			if (!deleteList.isEmpty()) {
				status = deleteMedataNodes(dbHandler, status, label, deleteList);
			}

		} catch (IOException e) {
			log.error("Exception in updating metadata", e);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage(), e);
		}
		return status;
	}

	private boolean deleteMedataNodes(GraphDBHandler dbHandler, boolean status, String label,
			List<JsonObject> deleteList) {
		String cypherQuery;
		cypherQuery = " UNWIND {props} AS properties MATCH (n :" + label + "{metadata_id:properties.metadata_id})   "
				+ " REMOVE n:" + label + "  SET n:METADATA_BACKUP  RETURN n ";
		try {
			JsonObject graphResponse = dbHandler.executeQueryWithData(cypherQuery, deleteList);
			if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
					.getAsJsonArray().size() > 0) {
				return status;
			}
			status = true;
		} catch (InsightsCustomException e) {
			log.error("Exception in deleting nodes ", e);
		}
		return status;
	}

	private boolean updateMedataNodes(GraphDBHandler dbHandler, boolean status, String label,
			List<JsonObject> editList) {
		String cypherQuery;
		cypherQuery = " UNWIND {props} AS properties MATCH (n :" + label + "{metadata_id:properties.metadata_id}) "
				+ " SET n += properties RETURN n ";
		try {
			JsonObject graphResponse = dbHandler.executeQueryWithData(cypherQuery, editList);
			if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
					.getAsJsonArray().size() > 0) {
				return status;
			}
			status = true;
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return status;
	}

	private JsonObject getHierachyDetails(CSVRecord record, Map<String, Integer> headerMap) {
		JsonObject json = new JsonObject();
		for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
			if (header.getKey() != null && !DatataggingConstants.ACTION.equalsIgnoreCase(header.getKey())) {
				if (DatataggingConstants.METADATA_ID.equalsIgnoreCase(header.getKey())
						&& (record.get(header.getValue()) != null && !record.get(header.getValue()).isEmpty())) {
					json.addProperty(header.getKey(), Integer.valueOf(record.get(header.getValue())));
				} else {
					json.addProperty(header.getKey(), record.get(header.getValue()));
				}
			}
		}
		return json;
	}

	private void getCurrentRecords(List<String> combo, GraphDBHandler dbHandler) throws InsightsCustomException {
		String cypherQuery = " MATCH (n :METADATA:DATATAGGING)  RETURN n";
		GraphResponse graphResponse = dbHandler.executeCypherQuery(cypherQuery);
		JsonArray rows = graphResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
				.getAsJsonArray();
		JsonArray asJsonArray = rows.getAsJsonArray();
		buildExistingBuToolCombinationList(combo, asJsonArray);
	}

	private void buildExistingBuToolCombinationList(List<String> combo, JsonArray array) {
		for (JsonElement element : array) {
			JsonElement jsonElement = element.getAsJsonObject().get("row").getAsJsonArray().get(0);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			combo.add(getUniqueString(jsonObject));
		}
	}

	private void updateComboList(List<String> combo, JsonObject json) throws InsightsCustomException {
		String comboStr = getUniqueString(json);

		if (combo.contains(comboStr)) {
			throw new InsightsCustomException("Duplicate Business Hierarchy..");
		}
		combo.add(comboStr);
	}

	private String getUniqueString(JsonObject jsonObject) {
		return jsonObject.get(DatataggingConstants.LEVEL1).getAsString() + "_"
				+ jsonObject.get(DatataggingConstants.LEVEL2).getAsString() + "_"
				+ jsonObject.get(DatataggingConstants.LEVEL3).getAsString() + "_"
				+ jsonObject.get(DatataggingConstants.LEVEL4).getAsString() + "_"
				+ jsonObject.get(DatataggingConstants.TOOL_NAME).getAsString();
	}

	private File convertToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());

		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multipartFile.getBytes());
		}

		return file;
	}

}