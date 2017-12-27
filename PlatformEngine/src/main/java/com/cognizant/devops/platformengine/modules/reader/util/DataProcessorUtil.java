package com.cognizant.devops.platformengine.modules.reader.util;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

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
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		CsvSchema  schemaDet =CsvSchema.builder()
								.addColumn(DatataggingConstants.ID,CsvSchema.ColumnType.NUMBER)
								.addColumn(DatataggingConstants.LEVEL1,CsvSchema.ColumnType.STRING)
								.addColumn(DatataggingConstants.LEVEL2,CsvSchema.ColumnType.STRING)
								.addColumn(DatataggingConstants.LEVEL3,CsvSchema.ColumnType.STRING)
								.addColumn(DatataggingConstants.LEVEL4,CsvSchema.ColumnType.STRING)
								.addColumn(DatataggingConstants.TOOL_NAME,CsvSchema.ColumnType.STRING)
								.addColumn(DatataggingConstants.TOOL_PROPERTY,CsvSchema.ColumnType.STRING)
								.addColumn(DatataggingConstants.PROPERTY_VALUE,CsvSchema.ColumnType.STRING)
								.build();
		CsvMapper mapperDet = new CsvMapper();
		MappingIterator<HierarchyDetails> iterator = mapperDet.readerFor(HierarchyDetails.class).with(schemaDet.withHeader())
				  .readValues(DataProcessorUtil.class.getClassLoader().getResource(DatataggingConstants.EXCEL_FILE));
		List<HierarchyDetails> allDetails = iterator.readAll();
		List<HierarchyDetails> details = new ArrayList<HierarchyDetails>();
		for(HierarchyDetails  det:allDetails){
			StringBuilder hiearchyData= new StringBuilder();
			String appenddata="";
			if(!StringUtils.isBlank(det.getLevel_1())){
				hiearchyData.append(appenddata);
				hiearchyData.append(det.getLevel_1());
				hiearchyData.append(DatataggingConstants.COLON);
				
			}
			if(!StringUtils.isBlank(det.getLevel_2())){
				hiearchyData.append(appenddata);
				hiearchyData.append(det.getLevel_2());
				hiearchyData.append(DatataggingConstants.COLON);
				
			}
			if(!StringUtils.isBlank(det.getLevel_3())){
				hiearchyData.append(appenddata);
				hiearchyData.append(det.getLevel_3());
				hiearchyData.append(DatataggingConstants.COLON);
				
			}
			if(!StringUtils.isBlank(det.getLevel_4())){
				hiearchyData.append(appenddata);
				hiearchyData.append(det.getLevel_1());
				hiearchyData.append(DatataggingConstants.COLON);
				
			}
			det.setHierarchyName(StringUtils.removeEnd(hiearchyData.toString() ,DatataggingConstants.COLON));
			details.add(det);
		}
					hierarchyDetailsDAL.addHierarchyDetailsList(details);
				return map;
	}


/*	public static void main(String[] a) throws IOException{
		DataProcessorUtil processor=new DataProcessorUtil();
		processor.readData();

	}
*/

}
