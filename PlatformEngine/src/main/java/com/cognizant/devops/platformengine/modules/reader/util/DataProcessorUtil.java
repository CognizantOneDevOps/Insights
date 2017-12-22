package com.cognizant.devops.platformengine.modules.reader.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cognizant.devops.platformdal.hierarchy.details.HierarchyDetails;
import com.cognizant.devops.platformdal.hierarchy.details.HierarchyDetailsDAL;
import com.cognizant.devops.platformengine.modules.reader.Constants.DatataggingConstants;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;



public class DataProcessorUtil {

	private static final DataProcessorUtil dataProcessorUtil = new DataProcessorUtil();

	private DataProcessorUtil() {

	}

	public static DataProcessorUtil getInstance() {
		return dataProcessorUtil;
	}

	public  Map<String,List<String>> readData() throws  IOException {
		Map<String,List<String>> map=new HashMap<String,List<String>>();
		CsvMapper mapper = new CsvMapper();
		CsvSchema schema = CsvSchema.emptySchema().withHeader(); 
		MappingIterator<Map<String,String>> it = mapper.readerFor(Map.class)
				.with(schema)
				.readValues(DataProcessorUtil.class.getClassLoader().getResource(DatataggingConstants.EXCEL_FILE));
		List<HierarchyDetails> hiearchyList=new ArrayList<HierarchyDetails>();
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		while (it.hasNext()) {
			HierarchyDetails hierarchyDetails = new HierarchyDetails();
			Map<String,String> rowAsMap = it.next();
			for (Map.Entry<String, String> entry : rowAsMap.entrySet()) {
				String key = entry.getKey();
				if(key.equals(DatataggingConstants.TOOLNAME)){


				}else if(key.equals(DatataggingConstants.LEVEL1)){
					hierarchyDetails.setLevel_1(entry.getValue());
				}else if(key.equals(DatataggingConstants.LEVEL2)){
					hierarchyDetails.setLevel_1(entry.getValue());
				}else if(key.equals(DatataggingConstants.LEVEL3)){
					hierarchyDetails.setLevel_1(entry.getValue());
				}else if(key.equals(DatataggingConstants.LEVEL4)){
					hierarchyDetails.setLevel_1(entry.getValue());
				}else if(key.equals(DatataggingConstants.PROPERTY)){
					hierarchyDetails.setProperty(entry.getValue());
				}else if(key.equals(DatataggingConstants.VALUE)){
					hierarchyDetails.setValue(entry.getValue());
				}
			}
			hiearchyList.add(hierarchyDetails);
		}

		hierarchyDetailsDAL.addHierarchyDetailsList(hiearchyList);
		return map;
	}


	/*public static void main(String[] a) throws IOException{
		DataProcessorUtil processor=new DataProcessorUtil();
		processor.readData();

	}*/


}
