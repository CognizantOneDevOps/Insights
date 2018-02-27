package com.cognizant.devops.platformservice.rest.dataTagging.util;

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
import com.cognizant.devops.platformservice.rest.dataTagging.Constants.DatataggingConstants;
import com.google.gson.JsonObject;


public class DataProcessorUtil  {
	private static final DataProcessorUtil dataProcessorUtil = new DataProcessorUtil();
	private static final Logger log = Logger.getLogger(DataProcessorUtil.class);
	private DataProcessorUtil() {

	}

	public static DataProcessorUtil getInstance() {
		return dataProcessorUtil;
	}

	private File convertToFile(MultipartFile multipartFile) throws IOException, FileNotFoundException {
		File file = new File(multipartFile.getOriginalFilename());
		file.createNewFile(); 
		FileOutputStream fos = new FileOutputStream(file); 
		fos.write(multipartFile.getBytes());
		fos.close();
		return file;
	}

	public  boolean readData(MultipartFile file)   {
		File csvfile =null;
		boolean status = false;
		try {
			csvfile = convertToFile(file);
		} catch (IOException ex) {
			status=true;
			log.debug(ex);
		}

		CSVFormat format = CSVFormat.newFormat(',').withHeader();

		try (Reader reader = new FileReader(csvfile); CSVParser csvParser = new CSVParser(reader, format);){

			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			List<JsonObject> gitProperties = new ArrayList<>();
			Map<String, Integer> headerMap = csvParser.getHeaderMap();

			dbHandler.executeCypherQuery("CREATE CONSTRAINT ON (n:METADATA) ASSERT n.id  IS UNIQUE");
			String query =  "UNWIND {props} AS properties " +
					"CREATE (n:METADATA:DATATAGGING) " +
					"SET n = properties";
			int sleepTime=500;
			int size = 0;
			int totalSize = 0;
			int bulkRecordCnt=10;
			for (CSVRecord csvRecord : csvParser.getRecords()) {
				size += 1;
				//totalRecords++;
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
					if(graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0){
						log.error(graphResponse);
						status=false;
						if(totalSize >= csvParser.getRecords().size()) {
							break;
						}
					}
					Thread.sleep(sleepTime);
					gitProperties = new ArrayList<>();
				}
				/*if(totalSize >= totalRecords) {

					break;
				}*/
				status=true;
			}

		} catch (FileNotFoundException e) {
			status=false;
			log.debug(e);
		} catch (IOException e) {
			status=false;
			log.debug(e);
		} catch (GraphDBException e) {
			status=false;
			log.debug(e);
		} catch (InterruptedException e) {
			status=false;
			log.debug(e);
		}
		return status;

	}

	public boolean updateHiearchyProperty(MultipartFile file) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		File csvfile =null;
		boolean status = false;
		try {
			csvfile = convertToFile(file);
		} catch (IOException ex) {
			status=true;
			log.debug(ex);
		}
		String label = "METADATA:DATATAGGING";
		CSVFormat format = CSVFormat.newFormat(',').withHeader();

		try (Reader reader = new FileReader(csvfile); CSVParser csvParser = new CSVParser(reader, format);){
			Map<String, Integer> headerMap = csvParser.getHeaderMap();
			String cypherQuery = null;
			for (CSVRecord record : csvParser) { 
				JsonObject json = new JsonObject();
				List<JsonObject>  list=new ArrayList<JsonObject>();

				if( record.get("Action") != null &&  record.get("Action").equals("edit")){
					
					for(Map.Entry<String, Integer> header : headerMap.entrySet()){
						if(header.getKey() != null){
							json.addProperty(header.getKey(), record.get(header.getValue()));

						}
					}
					list.add(json);
					cypherQuery = "MATCH (n :"+label+"{id:'"+record.get("id")+"'}" + ")  SET n += {props} RETURN n ";
					try {
						JsonObject graphResponse = dbHandler.executeQueryWithData(cypherQuery,list);
						if(graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0){
							status = false;
							break;
						}
					} catch (GraphDBException e) {
						status = false;
						log.debug(e);
					}
					status = true;

				}else if(record.get("Action") != null &&  record.get("Action").equals("delete") ){

					cypherQuery = "MATCH (n :"+label+"{id:'"+record.get("id")+"'}" + ")   REMOVE n:"+label+"  SET n:METADATA_BACKUP  RETURN n ";
					try {
						dbHandler.executeCypherQuery(cypherQuery);
						
					} catch (GraphDBException e) {
						status = false;
						log.debug(e);
					}

					status = true;

				}



			}

		} catch (IOException e) {
			log.debug(e);
		} 
		return status;
	}

}
