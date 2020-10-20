/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.auditservice.audit.report;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.cognizant.devops.auditservice.audit.utils.EmailUtil;
import com.cognizant.devops.platformauditing.util.PdfTableUtil;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Report generation by fetching records from NEO4J based on table_structure in Json file.
 *
 */
@Deprecated
public class AuditCypherReport extends AuditReportStrategy{

	private static final Logger log = LoggerFactory.getLogger(AuditCypherReport.class.getName());
	
	private static final AuditCypherReport auditCypherReport = new AuditCypherReport();

	private AuditCypherReport() {

	}

	public static AuditCypherReport getInstance() {
		return auditCypherReport;
	}
	
	/**
	 * 
	 * Execute cypher query and prepare list based on table structue.
	 * @param fileContents
	 * @return {@link Boolean}
	 */
	@Override
	public boolean executeQuery(String fileContents,String reportName, String subscribers) {
		
		log.info("--Invoking Neo DB!---");
		log.info("FileContents = " + fileContents);

		GraphDBHandler dbHandler = new GraphDBHandler();
		long queryExecutionStartTime = System.currentTimeMillis();
		HashMap<String,Integer> columnMap = new HashMap<String,Integer>();
		Set<String> headerList = new LinkedHashSet<String>();
		try {
			JsonParser jsonParser = new JsonParser(); 
			JsonElement jsonElements = jsonParser.parse(fileContents);
			JsonObject jsonObject = jsonElements.getAsJsonObject();
			String cypherQuery = jsonObject.get("queryname").getAsString();
			log.info("Cypher query from File = "+cypherQuery);
			if(!keywordCheck(cypherQuery)){
				JsonObject tableStructure = jsonObject.get("table_structure").getAsJsonObject();
				log.info("TableStructure from File = "+tableStructure);

				GraphResponse cypherResponse = dbHandler.executeCypherQuery(cypherQuery);
				//GraphResponse cypherResponse = dbHandler.executeCypherQuery("MATCH (git:SCM)--(jira:ALM) RETURN git,jira LIMIT 2");
				JsonObject cyphertResponseJson = cypherResponse.getJson();
				log.info("CyphertResponseJson = "+cyphertResponseJson);
				try {
					JsonArray dataArray = cyphertResponseJson.get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray();
					JsonArray columnArray = cyphertResponseJson.get("results").getAsJsonArray().get(0).getAsJsonObject().get("columns").getAsJsonArray();

					for(int y=0;y<columnArray.size();y++){
						log.info("Column = ",columnArray.get(y).getAsString());
						columnMap.put(columnArray.get(y).getAsString(), y);
					}

					if(cypherQuery != null){
						List<String> rowValueList = new ArrayList<String>();
						for(int j=0;j<dataArray.size();j++){
							StringBuffer propVal = new StringBuffer();
							for(Entry<String, JsonElement> fileentry : tableStructure.entrySet()){
								//headerList.add(fileentry.getKey().toUpperCase());
								String tablerow = fileentry.getValue().getAsString();
								String property =  tablerow.substring(tablerow.indexOf(".")+1,tablerow.length());
								String node = tablerow.substring(0, tablerow.indexOf("."));

								int index = columnMap.get(node);
								//log.info("index = "+index);

								JsonArray rowArray = dataArray.get(j).getAsJsonObject().get("row").getAsJsonArray();

								if(rowArray.get(index).getAsJsonObject().get(property)!= null){
									//log.info("Row = ",rowArray
									//.get(index).getAsJsonObject().get(property).getAsString());
									propVal.append(rowArray.get(index).getAsJsonObject().get(property).getAsString()).append(",");
								}else{
									propVal.append("NA").append(",");
								}
							}
							rowValueList.add(propVal.toString().substring(0, propVal.toString().length()-1));
							//log.info("propVal = ",propVal);

						}

						log.info("rowValueList = "+rowValueList);
						headerList.add("SNo");
						for(Entry<String, JsonElement> fileentry : tableStructure.entrySet()){
							headerList.add(fileentry.getKey().toUpperCase());
						}
						log.info("headerList = "+headerList);
						/*for(String a : rowValueList){
						log.info(a);
					}*/
						if(rowValueList.size()>0){
							PdfTableUtil pdfTableUtil = new PdfTableUtil();
							byte[] pdfResponse = pdfTableUtil.generateCypherReport(headerList, rowValueList, reportName);
							HttpHeaders headers = new HttpHeaders();
							headers.setContentType(MediaType.parseMediaType("application/pdf"));
							headers.add("Access-Control-Allow-Methods", "POST");
							headers.add("Access-Control-Allow-Headers", "Content-Type");
							headers.add("Content-Disposition", "attachment; filename="+reportName);
							headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
							headers.add("Pragma", "no-cache");
							headers.add("Expires", "0");
							InputStream inputStream = new ByteArrayInputStream(pdfResponse);
							EmailUtil emailUtil = new EmailUtil();
							emailUtil.sendEmailWithAttachment(inputStream, reportName, subscribers);

						}else{
							log.info("No records found!");
						}
					}else{
						log.error("Cypher query is empty.");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					log.error(cypherResponse + "  ---- Processing failed  ----", ex);
					return Boolean.FALSE; 
				}
				long queryExecutionEndTime = System.currentTimeMillis();
				log.info("queryExecutionEndTime---"+queryExecutionEndTime);
				long queryProcessingTime = (queryExecutionEndTime - queryExecutionStartTime);
				log.info("queryProcessingTime---"+queryProcessingTime);
			}else{
				log.info("Aborting query from execute as it contains invalid keywords !!" + cypherQuery );
			}
		} catch (InsightsCustomException e) {
			log.error( " - query processing failed", e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;

	}


}
