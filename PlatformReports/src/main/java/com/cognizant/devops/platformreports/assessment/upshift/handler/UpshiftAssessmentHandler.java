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

package com.cognizant.devops.platformreports.assessment.upshift.handler;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfig;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfigDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.dataprocess.BaseDataProcessor;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UpshiftAssessmentHandler implements BaseDataProcessor {

    private static Logger log = LogManager.getLogger(UpshiftAssessmentHandler.class);
    private static Gson gson = new Gson();
    private static JsonParser jsonParser = new JsonParser();
    private static GraphDBHandler dbHandler = new GraphDBHandler();
    private UpshiftAssessmentConfigDAL upshiftAssessmentConfigDAL = new UpshiftAssessmentConfigDAL();
    private boolean flag = true;
    private static final String QUOTATION = "\"";
    private static int numOfNodes = 0;


    /**
     * Process the stored json based on the type of data
     * @param assessmentReportDTO
     */
    @Override
    public void processJson(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
    	 JsonElement element = null ;
    	 UpshiftAssessmentConfig upshiftAssessmentConfig = upshiftAssessmentConfigDAL.fetchUpshiftAssessmentDetailsByWorkflowId(assessmentReportDTO.getWorkflowId());
        try {
        	long startTime = System.nanoTime();
            byte[] upshiftFileBytes = upshiftAssessmentConfig.getFile();
            String uuid = upshiftAssessmentConfig.getUpshiftUuid();
            String fileString = new String(upshiftFileBytes, StandardCharsets.ISO_8859_1);
            Map<?, ?> map = gson.fromJson(fileString, Map.class);
            List<JsonObject> dataList = new ArrayList<>();
            JsonObject primaryKey = new JsonObject();
            String primKey = String.valueOf(System.currentTimeMillis());
            primaryKey.addProperty("insightsTime", primKey);
            primaryKey.addProperty("toolName", "UPSHIFT");
            primaryKey.addProperty("categoryName", "INSIGHTSUPSHIFT");
            primaryKey.addProperty("jenkinsExecutionId", upshiftAssessmentConfig.getJenkinsExecId());
            dataList.add(primaryKey);
            if(createNode("DATA", dataList)) {
                flag = Boolean.FALSE;
            }

            for (Map.Entry<?, ?> entry : map.entrySet()) {

                 element = jsonParser.parse(gson.toJson(entry.getValue()));
                if (element.isJsonPrimitive() && updateNode(primKey, entry.getKey().toString(), element.getAsString())) {
                    flag = Boolean.FALSE;
                } else if (element.isJsonObject()) {
                    createObjectNode(uuid, primKey, entry.getKey().toString(), element.getAsJsonObject());
                } else if (element.isJsonArray()) {
                    parseJsonArray(uuid, primKey, entry.getKey().toString(), element.getAsJsonArray());
                }
            }
            log.debug("Completed processing of upshift report: {}", upshiftAssessmentConfig.getFileName());
            log.debug("Total nodes created: {}", numOfNodes);
            updateReportStatus(upshiftAssessmentConfig);
            long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
            		assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getReportId(),
            		upshiftAssessmentConfig.getWorkflowConfig().getWorkflowType(),"-","-",processingTime
            		,"UpshiftUuid :" +upshiftAssessmentConfig.getUpshiftUuid() +
            		"CreatedDate :" +upshiftAssessmentConfig.getCreatedDate() +
            		"UpdatedDate :"  +upshiftAssessmentConfig.getUpdatedDate() +
            		"fileName :" +upshiftAssessmentConfig.getFileName() +
            		"status :" +upshiftAssessmentConfig.getStatus()+ "Completed processing of upshift report");

        } catch (Exception e) {
            log.error(e.getStackTrace());
            log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
            		assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getReportId(),upshiftAssessmentConfig.getWorkflowConfig().getWorkflowType(),"-","-",0
            		,"UpshiftUuid :" +upshiftAssessmentConfig.getUpshiftUuid() +
            		"CreatedDate :" +upshiftAssessmentConfig.getCreatedDate() +
            		"UpdatedDate :"  +upshiftAssessmentConfig.getUpdatedDate() +
            		"fileName :" +upshiftAssessmentConfig.getFileName() +
            		"status :" +upshiftAssessmentConfig.getStatus() + element.getAsString() + e.getMessage());
            throw new InsightsJobFailedException(e.getMessage());
        }
    }

    /**
     * Update the report status in database
     * @param upshiftAssessmentConfig
     */
	private void updateReportStatus(UpshiftAssessmentConfig upshiftAssessmentConfig) {
		if(flag) {
		    updateReportStatus(upshiftAssessmentConfig, WorkflowTaskEnum.UpshiftAssessmentStatus.NODE_CREATED.name());
		}
		else {
		    updateReportStatus(upshiftAssessmentConfig, WorkflowTaskEnum.UpshiftAssessmentStatus.ERROR.name());
		}
	}

    /**
     * If data type is jsonobject, create node and process the child elements
     * @param parentId
     * @param name
     * @param uuid
     * @param input
     */
    private void createObjectNode(String uuid, String parentId, String name, JsonObject input) {
    	try {
    		String primKey = String.valueOf(System.currentTimeMillis());
    		List<JsonObject> dataList = new ArrayList<>();
    		JsonObject nodeProperties = new JsonObject();
    		nodeProperties.addProperty("insightsTime", primKey);
    		nodeProperties.addProperty("insightsParentKey", parentId);
    		nodeProperties.addProperty("insightsVSMReporttype", name);
    		nodeProperties.addProperty("toolName", "UPSHIFT");
    		nodeProperties.addProperty("upshiftUuid", uuid);
    		nodeProperties.addProperty("categoryName", "INSIGHTSUPSHIFT");
    		dataList.add(nodeProperties);
    		if(createNode(name, dataList)) {
    			flag = Boolean.FALSE;
    		}
    		for (Map.Entry<?,?> entry : input.entrySet()) {
    			JsonElement element = jsonParser.parse(gson.toJson(entry.getValue()));
    			if (element.isJsonPrimitive() && updateNode(primKey, entry.getKey().toString(), element.getAsString())) {
    				flag = Boolean.FALSE;
    			} else if (element.isJsonObject()) {
    				createObjectNode(uuid, primKey, entry.getKey().toString(), element.getAsJsonObject());
    			} else if (element.isJsonArray()){
    				parseJsonArray(uuid, primKey, entry.getKey().toString(), element.getAsJsonArray());
    			}
    		}

    	} catch (InsightsCustomException e) {
    		log.error(e.getStackTrace());
    		throw new InsightsJobFailedException(e.getMessage());
    	}

    }

    /**
     * Process data of type JsonArray according to its child elements
     * @param parentId
     * @param name
     * @param input
     * @throws InsightsCustomException
     */
    private void parseJsonArray(String uuid, String parentId, String name, JsonArray input) throws InsightsCustomException {
        if (input.getAsJsonArray().size() < 1 ){
            if(updateNode(parentId, name, input))
                flag = Boolean.FALSE;
        } else{
            JsonArray array = input.getAsJsonArray();
            if (array.get(0).isJsonPrimitive())
                checkPrimitive(parentId, name, array);
            else if (array.get(0).isJsonObject())
            	input.forEach(e -> createObjectNode(uuid, parentId, name, e.getAsJsonObject()));
        }
    }

    /**
     * Add the primitive array as a property to the node
     * @param parentId
     * @param name
     * @param array
     * @throws InsightsCustomException
     */
	private void checkPrimitive(String parentId, String name, JsonArray array) throws InsightsCustomException {
		List<String> arrayString = new ArrayList<>();
		array.forEach(e -> arrayString.add(e.getAsString()));
		if(updateNode(parentId, name, arrayString)) {
		    flag = Boolean.FALSE;
		}
	}

    /**
     * Create a new node for each jsonobject
     * @param label
     * @param dataList
     * @return
     * @throws InsightsCustomException
     */
    private static boolean createNode(String label, List<JsonObject> dataList) throws InsightsCustomException {
        StringBuilder cypherQuery = new StringBuilder("UNWIND {props} as properties CREATE (n:INSIGHTSUPSHIFT:UPSHIFT:");
        cypherQuery.append(label);
        cypherQuery.append(") SET n=properties RETURN n ");

        JsonObject graphResponse = dbHandler.executeQueryWithData(cypherQuery.toString(), dataList);

        if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
            log.error("Unable to insert upshift report nodes, error occured: {} ",graphResponse);
            log.error(dataList);
            return Boolean.TRUE;
        }
        log.debug("Successfully created node with label : {}", label);
        numOfNodes++;
        return Boolean.FALSE;

    }

    /**
     * Update a node with properties with the help of primary key generated
     * @param primary
     * @param propName
     * @param propValue
     * @return
     * @throws InsightsCustomException
     */
    private static boolean updateNode(String primary, String propName, Object propValue) throws InsightsCustomException {
        String quotes = "";
        List<String> propValue1 = new ArrayList<>();
        if(propValue instanceof String || propValue instanceof Date){
            quotes = QUOTATION;
            propValue= StringEscapeUtils.escapeJava(propValue.toString());
        }
        if(propValue instanceof List){
            List<String> tempList = (List<String>) propValue;
            tempList.forEach( s -> propValue1.add(QUOTATION + StringEscapeUtils.escapeJava(s) + QUOTATION));
            propValue= "";
        }
        StringBuilder cypherQuery = new StringBuilder("MATCH (n:INSIGHTSUPSHIFT) where n.insightsTime = \"");
        cypherQuery.append(primary);
        cypherQuery.append("\" SET n.").append(propName).append(" = ").append(quotes).append(propValue);
        if(!propValue1.isEmpty()) {
            cypherQuery.append(propValue1);
        }
        cypherQuery.append(quotes).append(" ");
        cypherQuery.append("RETURN n");
        GraphResponse graphResponse = dbHandler.executeCypherQuery(cypherQuery.toString());
        List<NodeData> nodes = graphResponse.getNodes();
        if (nodes.isEmpty()) {
            log.error("Unable to update upshift report nodes, error occured: {} ",graphResponse);
            log.error("primary key : {}",primary);
            return true;
        }
        return false;
    }

    /**
     * Update all required properties into database
     * @param upshiftAssessmentConfig
     * @param status
     */
    private void updateReportStatus(UpshiftAssessmentConfig upshiftAssessmentConfig, String status) {
    	long startTime = System.nanoTime();
        upshiftAssessmentConfig.setStatus(status);
        upshiftAssessmentConfig.setWorkflowConfig(upshiftAssessmentConfig.getWorkflowConfig());
        upshiftAssessmentConfig.setUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
        upshiftAssessmentConfigDAL.updateUpshiftAssessmentConfig(upshiftAssessmentConfig);
        long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
        		"-",upshiftAssessmentConfig.getWorkflowConfig().getWorkflowId(),"-",upshiftAssessmentConfig.getWorkflowConfig().getWorkflowType(),"-","-",processingTime
        		,"UpshiftUuid :" +upshiftAssessmentConfig.getUpshiftUuid() +
        		"CreatedDate :" +upshiftAssessmentConfig.getCreatedDate() +
        		"UpdatedDate :"  +upshiftAssessmentConfig.getUpdatedDate() +
        		"fileName :" +upshiftAssessmentConfig.getFileName() +
        		"status :" +upshiftAssessmentConfig.getStatus()+ "Completed processing of upshift report");        
    }


}
