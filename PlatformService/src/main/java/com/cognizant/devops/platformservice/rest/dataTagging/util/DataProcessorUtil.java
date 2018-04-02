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
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.google.gson.JsonObject;


public class DataProcessorUtil  {
	private static final DataProcessorUtil dataProcessorUtil = new DataProcessorUtil();
	private static final Logger log = Logger.getLogger(DataProcessorUtil.class);
	private DataProcessorUtil() {

	}

	public static DataProcessorUtil getInstance() {
		return dataProcessorUtil;
	}

	private File convertToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		if(file.createNewFile()) { 
			try(FileOutputStream fos = new FileOutputStream(file) ){ 
				fos.write(multipartFile.getBytes());
			}
		}
		return file;
	}

	public  boolean readData(MultipartFile file)    {

		File csvfile =null;
		boolean status = false;
		try {
			csvfile = convertToFile(file);
		} catch (IOException ex) {
			log.debug(ex);
		}
		CSVFormat format = CSVFormat.newFormat(',').withHeader();
		try (Reader reader = new FileReader(csvfile); CSVParser csvParser = new CSVParser(reader, format);){

			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			Map<String, Integer> headerMap = csvParser.getHeaderMap();
			dbHandler.executeCypherQuery("CREATE CONSTRAINT ON (n:METADATA) ASSERT n.metadata_id  IS UNIQUE");
			String query =  "UNWIND {props} AS properties " +
					"CREATE (n:METADATA:DATATAGGING) " +
					"SET n = properties";
			status = parseCsvRecords(status, csvParser, dbHandler, headerMap, query);

		} catch (FileNotFoundException e) {
			log.error("File not found Exception in uploading csv file" , e);
		} catch (IOException  | GraphDBException  |InterruptedException e ) {
			log.error("IOException in uploading csv file" , e);
		}
		return status;

	}

	private boolean parseCsvRecords(boolean status, CSVParser csvParser, Neo4jDBHandler dbHandler,
			Map<String, Integer> headerMap, String query) throws IOException, GraphDBException, InterruptedException {
		List<JsonObject> gitProperties = new ArrayList<>();
		int sleepTime=500;
		int size = 0;
		int totalSize = 0;
		int bulkRecordCnt=10;
		for (CSVRecord csvRecord : csvParser.getRecords()) {
			size += 1;
			JsonObject json = new JsonObject();
			for(Map.Entry<String, Integer> header : headerMap.entrySet()){
				if(header.getKey()!= null){
					json.addProperty(header.getKey(), csvRecord.get(header.getValue()));
				}
			}			
			json.addProperty(DatataggingConstants.CREATIONDATE, Instant.now().toEpochMilli() );
			gitProperties.add(json);
			if(size == bulkRecordCnt ) {
				totalSize += size;
				size = 0;
				JsonObject graphResponse = dbHandler.bulkCreateNodes(gitProperties, null, query);
				if(graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS).getAsJsonArray().size() > 0){
					log.error(graphResponse);
					status=false;
					if(totalSize >= csvParser.getRecords().size()) {
						break;
					}
				}
				Thread.sleep(sleepTime);
				gitProperties = new ArrayList<>();
			}

			status=true;
		}
		return status;
	}

	public boolean updateHiearchyProperty(MultipartFile file) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		File csvfile =null;
		boolean status = false;
		try {
			csvfile = convertToFile(file);
		} catch (IOException e) {
			log.error("IOException in converting file",e);
		}
		String label = "METADATA:DATATAGGING";
		CSVFormat format = CSVFormat.newFormat(',').withHeader();

		try (Reader reader = new FileReader(csvfile); CSVParser csvParser = new CSVParser(reader, format);){
			Map<String, Integer> headerMap = csvParser.getHeaderMap();
			List<JsonObject>  editList=new ArrayList<>();
			List<JsonObject>  deleteList=new ArrayList<>();
			for (CSVRecord record : csvParser) { 

				if( record.get(DatataggingConstants.ACTION) != null &&  record.get(DatataggingConstants.ACTION).equals("edit")){
					JsonObject json = new JsonObject();
					for(Map.Entry<String, Integer> header : headerMap.entrySet()){
						if(header.getKey() != null){
							json.addProperty(header.getKey(), record.get(header.getValue()));

						}
					}
					editList.add(json);
				}else if(record.get(DatataggingConstants.ACTION) != null &&  record.get(DatataggingConstants.ACTION).equals("delete") ){
					JsonObject json = new JsonObject();
					for(Map.Entry<String, Integer> header : headerMap.entrySet()){
						if(header.getKey() != null){
							json.addProperty(header.getKey(), record.get(header.getValue()));

						}
					}
					deleteList.add(json);
				}

			}
			if(!editList.isEmpty() ){
				status = updateMedataNodes(dbHandler, status, label, editList);
			}	
			if( !deleteList.isEmpty()){
				status = deleteMedataNodes(dbHandler, status, label, deleteList);
			}

		} catch (IOException e) {
			log.error("Exception in updating metadata" , e);
		} 
		return status;
	}

	private boolean deleteMedataNodes(Neo4jDBHandler dbHandler, boolean status, String label,
			List<JsonObject> deleteList) {
		String cypherQuery;
		cypherQuery = " UNWIND {props} AS properties MATCH (n :"+label+"{metadata_id:properties.metadata_id})   "
				+ " REMOVE n:"+label+"  SET n:METADATA_BACKUP  RETURN n ";
		try {
			JsonObject graphResponse = dbHandler.executeQueryWithData(cypherQuery,deleteList);
			if(graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS).getAsJsonArray().size() > 0){
				return status;
			}
			status = true;
		} catch (GraphDBException e) {
			log.error("Exception in deleting nodes ",e);
		}
		return status;
	}

	private boolean updateMedataNodes(Neo4jDBHandler dbHandler, boolean status, String label, List<JsonObject> editList) {
		String cypherQuery;
		cypherQuery = " UNWIND {props} AS properties MATCH (n :"+label+"{metadata_id:properties.metadata_id}) "
				+ " SET n += {props} RETURN n ";
		try {
			JsonObject graphResponse = dbHandler.executeQueryWithData(cypherQuery,editList);
			if(graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS).getAsJsonArray().size() > 0){
				return status;
			}
			status = true;
		} catch (GraphDBException e) {
			log.error(e);
		}
		return status;
	}

}