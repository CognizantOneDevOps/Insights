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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.cognizant.devops.auditservice.audit.utils.EmailUtil;
import com.cognizant.devops.platformauditing.util.PdfTableUtil;
import com.cognizant.devops.platformauditing.util.RestructureDataUtil;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.SystemStatus;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * 
 * Report generation by fetching records from NEO4J and HYPHERLEDGER.
 *
 */
@Deprecated
public class AuditLedgerReport extends AuditReportStrategy{


	private static Logger log = LogManager.getLogger(AuditLedgerReport.class);

	private static final AuditLedgerReport auditLedgerReport = new AuditLedgerReport();

	private AuditLedgerReport() {

	}

	public static AuditLedgerReport getInstance() {
		return auditLedgerReport;
	}

	@Override
	public boolean executeQuery(String fileContents, String pdfName, String subscribers) {
		log.info("---Invoking Neo and ledger DB!---");

		GraphDBHandler dbHandler = new GraphDBHandler();
		try {

			JsonParser jsonParser = new JsonParser(); 
			JsonElement jsonElements = jsonParser.parse(fileContents);
			JsonObject jsonObject = jsonElements.getAsJsonObject();
			String cypherQuery = jsonObject.get("queryname").getAsString();
			log.info("Cypher query from File = "+cypherQuery);
			if(!keywordCheck(cypherQuery)){
				GraphResponse cypherResponse = dbHandler.executeCypherQuery(cypherQuery);
				JsonObject cypherResponseJson = cypherResponse.getJson();
				log.info("cypherResponseJson = "+cypherResponseJson);
				try {
					JsonArray dataArray = cypherResponse.getJson()
							.get("results").getAsJsonArray().get(0).getAsJsonObject()
							.get("data").getAsJsonArray();

					if(dataArray.size()>0){
						log.info("Total Assets  = "+dataArray.size());
						List<JsonObject> ledgerList = new ArrayList<JsonObject>();
						for(int i=0;i<dataArray.size();i++) {
							JsonArray rowArray = dataArray.get(i).getAsJsonObject()
									.get("row").getAsJsonArray();

							for(JsonElement element : rowArray) {
								log.info("Fetching Massage data for -- "+element.getAsJsonObject());
								RestructureDataUtil restructureDataUtil = new RestructureDataUtil();
								JsonObject msgData = restructureDataUtil.masssageData(element.getAsJsonObject());
								log.info("Massage Data  ---"+msgData);

								for (Map.Entry<String, JsonElement> property : msgData.entrySet()) {
									if(property.getKey().contains("AssetID")){
										log.info("AssetID from json = "+property.getKey());
										String assetId = msgData.get(property.getKey()).getAsString();
										log.info("AssetID = "+assetId);
										String url = ApplicationConfigProvider.getInstance().getInsightsServiceURL()+"/PlatformService/traceability/getAssetHistory?assetId="+assetId;
										String ledgerresponse = SystemStatus.jerseyGetClientWithAuthentication(url, ApplicationConfigProvider.getInstance().getUserId(), ApplicationConfigProvider.getInstance().getPassword(), null);
										log.info("Ledgerresponse from couch = "+ledgerresponse);
										JsonObject jp = new JsonParser().parse(ledgerresponse).getAsJsonObject(); 
										if("success".equalsIgnoreCase(jp.get("status").getAsString())){
											
											ledgerList.add(formatAssetId(ledgerresponse, assetId));
											
										}else{
											log.info("Issue for asssetId -- "+assetId);
											log.info("-------------------------------------------------");
										}
									}
								}
							}
						}
						generateReport(pdfName, subscribers, ledgerList);
						
					}else{
						log.info("-- No Records found !!" + cypherQuery );
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					log.info("exception");
					log.error(cypherQuery + "  - Exception in getting ledger reponse -"+ ex);
					return Boolean.FALSE; 
				}

			}else{
				log.info("Aborting query from execute as it contains invalid keywords !!" + cypherQuery );
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			log.info("graph exception");
			log.error(" - query processing failed"+ e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;

	}

	private void generateReport(String pdfName, String subscribers, List<JsonObject> ledgerList)
			throws AddressException, MessagingException, IOException {
		log.info("LedgerList---"+ledgerList.size());
		
		if(ledgerList.size()>0){
			PdfTableUtil pdfTableUtil = new PdfTableUtil();
			byte[] pdfResponse = pdfTableUtil.generateLedgerReport(ledgerList, pdfName);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/pdf"));
			headers.add("Access-Control-Allow-Methods", "POST");
			headers.add("Access-Control-Allow-Headers", "Content-Type");
			headers.add("Content-Disposition", "attachment; filename="+pdfName);
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			InputStream inputStream = new ByteArrayInputStream(pdfResponse);
			sendReport(pdfName, inputStream, subscribers);
			log.info("-------------------------------------------------");
		}else{
			log.info("No Records found!");
		}
		/*if(ledgerList.size()>0){
			InputStream response = doPost(ApplicationConfigProvider.getInstance().getInsightsServiceURL()+"/PlatformAuditService/traceability/getAuditReport?pdfName="+pdfName,
					"admin", "admin", null,ledgerList);
			//log.info("pdfResponse = "+response);
			sendReport(pdfName, response, subscribers);
			log.info("-------------------------------------------------");
		}*/
	}

	/**
	 * Send Email .
	 * @param pdfName
	 * @param response
	 * @throws IOException 
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	private void sendReport(String pdfName, InputStream response, String subscribers) throws AddressException, MessagingException, IOException
	{
		try{
			EmailUtil emailutil = new EmailUtil();
			emailutil.sendEmailWithAttachment(response, pdfName, subscribers);
		}catch(Exception e){
			log.error(" -sendReport failed"+ e.getMessage());
		}finally {
			if(response!=null) {
				try {
					response.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
	}

	/**
	 * Addition of assetId key for pdf report.
	 * @param ledgerresponse
	 * @param parentAsset 
	 * @param assetId
	 * @return ledgerresponse
	 */
	private JsonObject formatAssetId(String ledgerresponse, String parentAsset) {
		JsonParser jsonParser = new JsonParser(); 
		JsonElement jsonElements = jsonParser.parse(ledgerresponse);
		JsonObject jsonObject = jsonElements.getAsJsonObject();
		
		Gson map = new Gson();
		if("success".equalsIgnoreCase(jsonObject.get("status").getAsString())){
			String assetId = "NA";
			JsonArray dataArray = jsonObject.getAsJsonArray("data");
			log.info("No of nodes = "+dataArray.size());
			for(int i=0;i<dataArray.size();i++) {
				JsonObject rowObject = jsonObject.getAsJsonArray("data").get(i).getAsJsonObject();
				for (Map.Entry<String, JsonElement> property : rowObject.entrySet()) {
					if(property.getKey().contains("AssetID")){
						log.info("AssetID from json = "+property.getKey());
						//log.info("rowObject.get(property.getKey()) = "+jsonObject.get(property.getKey()));
						assetId = rowObject.get(property.getKey()).getAsString();
						log.info("AssetID = "+assetId);
					}
				}
				rowObject.addProperty("assetID", assetId);
				rowObject.addProperty("parentAsset", parentAsset);
			}
			log.info("Formatted Inputt for pdf = "+jsonObject);
		}else{
			log.info("Invalid Ledger response");
		}
		return jsonObject;
		//Map<String, Object> map = new Gson().fromJson(jsonObject.toString(), HashMap.class);
		//return map.fromJson(jsonObject.toString(), HashMap.class);
	}




}
