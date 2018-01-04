package com.cognizant.devops.platformservice.rest.dataTagging.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformdal.hierarchy.details.HierarchyDetails;
import com.cognizant.devops.platformdal.hierarchy.details.HierarchyDetailsDAL;
import com.cognizant.devops.platformservice.rest.dataTagging.Constants.DatataggingConstants;




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
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
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
			System.out.println(details);
			hierarchyDetailsDAL.addHierarchyDetailsList(details);
			status=true;	 
		} catch (FileNotFoundException e) {
			status=false;
			LOG.debug(e);
		} catch (IOException e) {
			status=false;
			LOG.debug(e);
		}
		return status;

	}

	/*	public static void main(String[] a) throws IOException{
		DataProcessorUtil processor=new DataProcessorUtil();
		processor.readData();

	}
	 */

}
