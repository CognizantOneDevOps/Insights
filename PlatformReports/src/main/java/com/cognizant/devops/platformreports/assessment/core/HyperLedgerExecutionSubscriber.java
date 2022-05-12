/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformreports.assessment.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.cognizant.devops.platformauditing.util.AuditServiceUtil;
import com.cognizant.devops.platformauditing.util.RestructureDataUtil;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportsKPIConfig;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.dal.ReportPDFVisualizationHandlerFactory;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.pdf.BasePDFProcessor;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowtask.utils.MQMessageConstants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class HyperLedgerExecutionSubscriber extends WorkflowTaskSubscriberHandler{

	private static Logger log = LogManager.getLogger(HyperLedgerExecutionSubscriber.class.getName());
	
	private static final String ASSET_ID = "AssetID";
	
	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	private InsightsAuditImpl insightsAuditImpl = new InsightsAuditImpl();
	InsightsAssessmentConfigurationDTO assessmentReportDTO = null;

	public HyperLedgerExecutionSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		try {
			String incomingTaskMessage = new String(body, MQMessageConstants.MESSAGE_ENCODING);
			log.debug("Worlflow Detail ==== HyperLedgerExecutionSubscriber started ... "
					+ "routing key  message handleDelivery ===== {} ",incomingTaskMessage);
			assessmentReportDTO = new InsightsAssessmentConfigurationDTO();
			assessmentReportDTO.setIncomingTaskMessageJson(incomingTaskMessage);
			JsonObject incomingTaskMessageJson =JsonUtils.parseStringAsJsonObject(incomingTaskMessage);
			String workflowId = incomingTaskMessageJson.get("workflowId").getAsString();
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			log.debug("Worlflow Detail ==== scheduleType {} ", workflowConfig.getScheduleType());

			long startDate = 0;
			long endDate = 0;
			long startDateEpoch = 0;
			if (workflowConfig.getScheduleType().equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.name())) {
				startDate = workflowConfig.getAssessmentConfig().getStartDate();
				endDate = workflowConfig.getAssessmentConfig().getEndDate();				
			} else {
				startDateEpoch = workflowConfig.getNextRun();
				startDate = InsightsUtils.getStartFromTime(startDateEpoch, workflowConfig.getScheduleType()) - 1;
				endDate = startDateEpoch;
			}
			log.debug("Worlflow Detail ==== startDate & endDate in Epoch {} {}", startDate, endDate);
			String ledgerStartDate = InsightsUtils.epochToDateFormat(startDate, "yyyy-MM-dd");
			String ledgerEndDate = InsightsUtils.epochToDateFormat(endDate, "yyyy-MM-dd");
			log.debug("Worlflow Detail ==== ledgerStartDate & ledgerEndDate {} {}", ledgerStartDate, ledgerEndDate);
			prepareAssestmentDTO(incomingTaskMessageJson);
			List<JsonObject> ledgerList = fetchLedgerRecords(ledgerStartDate, ledgerEndDate);
			assessmentReportDTO.setLedgerRecords(ledgerList);
			BasePDFProcessor chartHandler = ReportPDFVisualizationHandlerFactory
					.getChartHandler(assessmentReportDTO.getVisualizationutil());
			chartHandler.generatePDF(assessmentReportDTO);
			
			
			log.debug("Worlflow Detail ==== HyperLedgerExecutionSubscriber Completed  {} ", incomingTaskMessageJson);			
		} catch (InsightsJobFailedException ijfe) {
			log.error("Worlflow Detail ==== HyperLedgerExecutionSubscriber Completed with error ", ijfe);
			statusLog = ijfe.getMessage();
			throw ijfe;
		} catch (Exception e) {
			log.error("Worlflow Detail ==== HyperLedgerExecutionSubscriber Completed with error ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
	}

	/**
	 * @param incomingTaskMessageJson
	 * @param ledgerStartDate
	 * @param ledgerEndDate
	 * @return
	 * @throws InsightsCustomException 
	 * @throws JsonSyntaxException
	 */
	private List<JsonObject> fetchLedgerRecords(String ledgerStartDate,String ledgerEndDate) throws InsightsCustomException{
		String toolName;
		String dbQuery;
		String assets = null;
		List<JsonObject> ledgerList = new ArrayList<>();
		
		Set<InsightsReportsKPIConfig> kpiconfig = workflowConfig.getAssessmentConfig().getReportTemplateEntity().getReportsKPIConfig();
		for (InsightsReportsKPIConfig insightsReportsKPIConfig : kpiconfig) {
			toolName = insightsReportsKPIConfig.getKpiConfig().getToolname();
			dbQuery = insightsReportsKPIConfig.getKpiConfig().getdBQuery();
			if(dbQuery.isEmpty()) {
				assets = insightsAuditImpl.getAllAssets(ledgerStartDate,ledgerEndDate, toolName);
				log.debug("Worlflow Detail ==== result {} ", assets);
				JsonElement msgData = JsonUtils.parseString(assets);
				JsonArray msgArray = msgData.getAsJsonObject().get("msg").getAsJsonArray();
				fetchAssetHistory(msgArray, ledgerList, false);
				
			}else {
				getLedgerRecordsByAssetID(dbQuery, ledgerList);
			}
		}
		return ledgerList;
	}

	/**
	 * If KPi Contains dbQuery fetch records from neo4j then get asset history for each records returned by neo4j.
	 * @param dbQuery
	 * @param ledgerList 
	 * @throws InsightsCustomException
	 */
	private void getLedgerRecordsByAssetID(String dbQuery, List<JsonObject> ledgerList) throws InsightsCustomException {
		GraphDBHandler dbHandler = new GraphDBHandler();
		GraphResponse cypherResponse = dbHandler.executeCypherQuery(dbQuery);
		JsonArray dataArray = cypherResponse.getJson()
				.get("results").getAsJsonArray().get(0).getAsJsonObject()
				.get("data").getAsJsonArray();
		for(int i=0;i<dataArray.size();i++) {
			JsonArray rowArray = dataArray.get(i).getAsJsonObject()
					.get("row").getAsJsonArray();

			for(JsonElement element : rowArray) {
				log.debug("Fetching Massage data for == {} ",element.getAsJsonObject());
				RestructureDataUtil restructureDataUtil = new RestructureDataUtil();
				JsonObject msgData = restructureDataUtil.massageData(element.getAsJsonObject());
				log.debug("Massaged Data  === {}",msgData);

				for (Map.Entry<String, JsonElement> property : msgData.entrySet()) {
					if(property.getKey().contains(ASSET_ID)){
						String assetId = msgData.get(property.getKey()).getAsString();
						log.debug("AssetID === {} ",assetId);
						String ledgerresponse = insightsAuditImpl.getAssetHistory(assetId);
						log.debug("Ledgerresponse from couch === {} ",ledgerresponse);
						JsonElement data = JsonUtils.parseString(ledgerresponse);
						JsonArray msgArray = data.getAsJsonObject().get("msg").getAsJsonArray();
						fetchAssetHistory(msgArray, ledgerList, true);
					}
				}
			}
		}
	}
	
	private void prepareAssestmentDTO(JsonObject incomingTaskMessage)
	{

		String workflowId = incomingTaskMessage.get("workflowId").getAsString();
		long executionId = incomingTaskMessage.get("executionId").getAsLong();
		workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
		
		assessmentReportDTO.setAsseementreportname(workflowConfig.getAssessmentConfig().getAsseementreportname());
		assessmentReportDTO.setConfigId(workflowConfig.getAssessmentConfig().getId());
		assessmentReportDTO.setExecutionId(executionId);
		assessmentReportDTO.setWorkflowId(workflowId);
		assessmentReportDTO.setReportId(workflowConfig.getAssessmentConfig().getReportTemplateEntity().getReportId());
		assessmentReportDTO
				.setReportName(workflowConfig.getAssessmentConfig().getReportTemplateEntity().getTemplateName());
		assessmentReportDTO.setReportFilePath(workflowConfig.getAssessmentConfig().getReportTemplateEntity().getFile());
		assessmentReportDTO
				.setAsseementreportdisplayname(workflowConfig.getAssessmentConfig().getAsseementReportDisplayName());
		assessmentReportDTO.setVisualizationutil(workflowConfig.getAssessmentConfig().getReportTemplateEntity().getVisualizationutil());

	}

	/**
	 * Fetch AssetHistory for each assetId.
	 * @param msgArray
	 * @param ledgerList
	 * @param byTool 
	 */
	private void fetchAssetHistory(JsonArray msgArray, List<JsonObject> ledgerList, boolean byTool) {
		log.debug("Worlflow Detail ==== fetchAssetHistory ledgerList {} ",ledgerList);
		for(JsonElement element : msgArray) {
			JsonObject assetObj = byTool ? element.getAsJsonObject().get("Value").getAsJsonObject():element.getAsJsonObject();
			for (Map.Entry<String, JsonElement> property : assetObj.entrySet()) {
				if(property.getKey().contains(ASSET_ID)){
					String assetId = assetObj.get(property.getKey()).getAsString();
					log.debug("Worlflow Detail ==== fetchAssetHistory assetId {} ",assetId);
					String assetHistory = insightsAuditImpl.getAssetHistory(assetId);
					JsonObject parsedHistory = AuditServiceUtil.parseHistory(assetHistory);
					 if (parsedHistory.getAsJsonPrimitive("statusCode").getAsString().equals("200")) {
						JsonObject parsedResponse = buildSuccessResponseWithData(parsedHistory.getAsJsonArray("data"));
						ledgerList.add(formatAssetId(parsedResponse.toString(), assetId));
					 }else{
						log.debug("fetchAssetHistory ==== Issue for asssetId {} ",assetId);
					 }
				}
			}
		}
		
	}
	
	/**
	 * Convert couch response to Insights common response.
	 * @param data
	 * @return
	 */
	public static JsonObject buildSuccessResponseWithData(Object data) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.add(PlatformServiceConstants.DATA, new Gson().toJsonTree(data));
		return ValidationUtils.replaceHTMLContentFormString(jsonResponse);
	}
	
	public void savePDFFile(File extractedPdfFile, byte[] pdfResponse) {
		try(FileOutputStream outStream = new FileOutputStream(extractedPdfFile)) {
			 outStream.write(pdfResponse);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while saving pdf ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
	}
	/* Addition of assetId key for pdf report.
	 * @param ledgerresponse
	 * @param parentAsset 
	 * @param assetId
	 * @return ledgerresponse
	 */
	private JsonObject formatAssetId(String ledgerresponse, String parentAsset) {
		JsonElement jsonElements = JsonUtils.parseString(ledgerresponse);
		JsonObject jsonObject = jsonElements.getAsJsonObject();
		if(!jsonObject.get("data").isJsonNull() && "success".equalsIgnoreCase(jsonObject.get("status").getAsString())){
			String assetId = "NA";
			JsonArray dataArray = jsonObject.get("data").getAsJsonArray();
			for(int i=0;i<dataArray.size();i++) {
				JsonObject rowObject = jsonObject.getAsJsonArray("data").get(i).getAsJsonObject();
				for (Map.Entry<String, JsonElement> property : rowObject.entrySet()) {
					if(property.getKey().contains(ASSET_ID)){
						assetId = rowObject.get(property.getKey()).getAsString();
					}
				}
				rowObject.addProperty("assetID", assetId);
				rowObject.addProperty("parentAsset", parentAsset);
			}
			log.debug("Formatted Inputt for pdf === {} ",jsonObject);
		}else{
			log.debug("Invalid Ledger response");
		}
		return jsonObject;
	}

}
