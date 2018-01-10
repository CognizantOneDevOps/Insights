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
		boolean status = false;
		try (	Reader reader = new FileReader(csvfile);  
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
						.withHeader(DatataggingConstants.ID,
								DatataggingConstants.LEVEL1, 
								DatataggingConstants.LEVEL2, 
								DatataggingConstants.LEVEL3,
								DatataggingConstants.LEVEL4,
								DatataggingConstants.TOOL_NAME,
								DatataggingConstants.TOOL_PROPERTY,
								DatataggingConstants.PROPERTY_VALUE)
						.withSkipHeaderRecord()
						);
				){
			/*HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
			List<HierarchyDetails> details = new ArrayList<HierarchyDetails>();
						for (CSVRecord csvRecord : csvParser.getRecords()) {
				HierarchyDetails heirarchyDetails=new HierarchyDetails();
				heirarchyDetails.setId(Integer.parseInt(csvRecord.get(DatataggingConstants.ID)));
				heirarchyDetails.setLevel_1(csvRecord.get(DatataggingConstants.LEVEL1));
				heirarchyDetails.setLevel_2(csvRecord.get(DatataggingConstants.LEVEL2));
				heirarchyDetails.setLevel_3(csvRecord.get(DatataggingConstants.LEVEL3));
				heirarchyDetails.setLevel_4(csvRecord.get(DatataggingConstants.LEVEL4));
				heirarchyDetails.setToolName(csvRecord.get(DatataggingConstants.TOOL_NAME));
				heirarchyDetails.setToolProperty(csvRecord.get(DatataggingConstants.TOOL_PROPERTY));
				heirarchyDetails.setPropertyValue(csvRecord.get(DatataggingConstants.PROPERTY_VALUE));

				StringBuilder hiearchyData= new StringBuilder();
				String appenddata="";
				if(!StringUtils.isBlank(csvRecord.get(DatataggingConstants.LEVEL1))){
					hiearchyData.append(appenddata);
					hiearchyData.append(csvRecord.get(DatataggingConstants.LEVEL1));
					hiearchyData.append(DatataggingConstants.COLON);

				}
				if(!StringUtils.isBlank(csvRecord.get(DatataggingConstants.LEVEL2))){
					hiearchyData.append(appenddata);
					hiearchyData.append(csvRecord.get(DatataggingConstants.LEVEL2));
					hiearchyData.append(DatataggingConstants.COLON);

				}
				if(!StringUtils.isBlank(csvRecord.get(DatataggingConstants.LEVEL3))){
					hiearchyData.append(appenddata);
					hiearchyData.append(csvRecord.get(DatataggingConstants.LEVEL3));
					hiearchyData.append(DatataggingConstants.COLON);

				}
				if(!StringUtils.isBlank(csvRecord.get(DatataggingConstants.LEVEL4))){
					hiearchyData.append(appenddata);
					hiearchyData.append(csvRecord.get(DatataggingConstants.LEVEL4));
					hiearchyData.append(DatataggingConstants.COLON);

				}
				heirarchyDetails.setHierarchyName(StringUtils.removeEnd(hiearchyData.toString() ,DatataggingConstants.COLON));
				details.add(heirarchyDetails);
			}
			hierarchyDetailsDAL.addHierarchyDetailsList(details);
			 */			
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			List<JsonObject> gitProperties = new ArrayList<>();
			String query = "UNWIND {props} AS properties " +
					"CREATE (n:METADATA:DATATAGGING) " +
					"SET n = properties";
			dbHandler.executeCypherQuery("CREATE CONSTRAINT ON (n:METADATA) ASSERT n.id  IS UNIQUE");
			int sleepTime=500;
			int totalRecords=0;
			for (CSVRecord csvRecord : csvParser.getRecords()) { 
				totalRecords++;
				JsonObject values1 = new JsonObject();
				values1.addProperty(DatataggingConstants.ID, csvRecord.get(DatataggingConstants.ID));
				values1.addProperty(DatataggingConstants.LEVEL1, csvRecord.get(DatataggingConstants.LEVEL1));
				values1.addProperty(DatataggingConstants.LEVEL2, csvRecord.get(DatataggingConstants.LEVEL2));
				values1.addProperty(DatataggingConstants.LEVEL3, csvRecord.get(DatataggingConstants.LEVEL3));
				values1.addProperty(DatataggingConstants.LEVEL4, csvRecord.get(DatataggingConstants.LEVEL4));
				values1.addProperty(DatataggingConstants.TOOL_NAME,csvRecord.get(DatataggingConstants.TOOL_NAME));
				values1.addProperty(DatataggingConstants.TOOL_PROPERTY, csvRecord.get(DatataggingConstants.TOOL_PROPERTY));
				values1.addProperty(DatataggingConstants.PROPERTY_VALUE, csvRecord.get(DatataggingConstants.PROPERTY_VALUE));
				values1.addProperty("creationDate", Instant.now().toEpochMilli() );
				gitProperties.add(values1);

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
