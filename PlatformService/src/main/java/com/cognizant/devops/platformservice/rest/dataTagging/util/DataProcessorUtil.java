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
	private static final Logger LOG = Logger.getLogger(DataProcessorUtil.class);
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
		try {
			csvfile = convertToFile(file);
		} catch (IOException ex) {
			LOG.debug(ex);
		}

		CSVFormat format = CSVFormat.newFormat(',').withHeader();
		boolean status = false;
		try (Reader reader = new FileReader(csvfile); CSVParser csvParser = new CSVParser(reader, format);){
			
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			List<JsonObject> gitProperties = new ArrayList<>();
			Map<String, Integer> headerMap = csvParser.getHeaderMap();
			
			dbHandler.executeCypherQuery("CREATE CONSTRAINT ON (n:METADATA) ASSERT n.id  IS UNIQUE");
			String query =  "UNWIND {props} AS properties " +
					"CREATE (n:METADATA:DATATAGGING) " +
					"SET n = properties";
			int sleepTime=500;
			int totalRecords=0;
			for (CSVRecord csvRecord : csvParser.getRecords()) {
				totalRecords++;
				JsonObject json = new JsonObject();
				for(Map.Entry<String, Integer> header : headerMap.entrySet()){
					
					json.addProperty(header.getKey(), csvRecord.get(header.getValue()));
				}			
				json.addProperty(DatataggingConstants.CREATIONDATE, Instant.now().toEpochMilli() );
				gitProperties.add(json);
				if(totalRecords > 5) {
					dbHandler.bulkCreateNodes(gitProperties, null, query);
					Thread.sleep(sleepTime);
					gitProperties = new ArrayList<>();
				}
				status=true;	

			}


		} catch (FileNotFoundException e) {
			status=false;
			LOG.debug(e);
		} catch (IOException e) {
			status=false;
			LOG.debug(e);
		} catch (GraphDBException e) {
			status=false;
			LOG.debug(e);
		} catch (InterruptedException e) {
			status=false;
			LOG.debug(e);
		}
		return status;

	}

}
