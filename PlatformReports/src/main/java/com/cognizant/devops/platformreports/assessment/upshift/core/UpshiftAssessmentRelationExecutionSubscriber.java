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

package com.cognizant.devops.platformreports.assessment.upshift.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfig;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UpshiftAssessmentRelationExecutionSubscriber extends WorkflowTaskSubscriberHandler {

	private static Logger log = LogManager.getLogger(UpshiftAssessmentRelationExecutionSubscriber.class);
	InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	private WorkflowDAL workflowDAL = new WorkflowDAL();

	private static GraphDBHandler dbHandler = new GraphDBHandler();
	private static int numOfNodesRelated = 0;
	private UpshiftAssessmentConfigDAL upshiftAssessmentConfigDAL = new UpshiftAssessmentConfigDAL();

	public UpshiftAssessmentRelationExecutionSubscriber(String routingKey) throws IOException, TimeoutException, InsightsCustomException {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		UpshiftAssessmentConfig upshiftAssessmentConfig = null;
		try {
			long startTime = System.nanoTime();
			String incomingTaskMessage = new String(body, StandardCharsets.UTF_8);
			log.debug("Worlflow Detail ==== UpshiftAssessmentExecutionSubscriber started ... "
					+ "routing key  message handleDelivery ===== {} ", incomingTaskMessage);

			JsonObject incomingTaskMessageJson = new JsonParser().parse(incomingTaskMessage).getAsJsonObject();
			String workflowId = incomingTaskMessageJson.get("workflowId").getAsString();
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			log.debug("Worlflow Detail ==== scheduleType {} ", workflowConfig.getScheduleType());
			upshiftAssessmentConfig = upshiftAssessmentConfigDAL.fetchUpshiftAssessmentDetailsByWorkflowId(workflowId);
			createRelationship(upshiftAssessmentConfig.getUpshiftUuid());
			
			log.debug("Total nodes related: {}", numOfNodesRelated);
			updateReportStatus(upshiftAssessmentConfig, WorkflowTaskEnum.UpshiftAssessmentStatus.COMPLETED.name());
			 long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
	            log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
	            		"-",workflowConfig.getWorkflowId(),upshiftAssessmentConfig.getId(),workflowConfig.getWorkflowType(),"-","-",processingTime
	            		,"UpshiftUuid :" +upshiftAssessmentConfig.getUpshiftUuid() +
	            		"CreatedDate :" +upshiftAssessmentConfig.getCreatedDate() +
	            		"UpdatedDate :"  +upshiftAssessmentConfig.getUpdatedDate() +
	            		"fileName :" +upshiftAssessmentConfig.getFileName() +
	            		"status :" +upshiftAssessmentConfig.getStatus()+ " UpshiftAssessmentExecutionSubscriber");			
		} catch (Exception e) {
			log.error("Worlflow Detail ==== GrafanaPDFExecutionSubscriber Completed with error ", e);
			updateReportStatus(upshiftAssessmentConfig, WorkflowTaskEnum.UpshiftAssessmentStatus.ERROR.name());
			 log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
	            		"-",workflowConfig.getWorkflowId(),upshiftAssessmentConfig.getId(),workflowConfig.getWorkflowType(),"-","-",0
	            		,"UpshiftUuid :" +upshiftAssessmentConfig.getUpshiftUuid() +
	            		"CreatedDate :" +upshiftAssessmentConfig.getCreatedDate() +
	            		"UpdatedDate :"  +upshiftAssessmentConfig.getUpdatedDate() +
	            		"fileName :" +upshiftAssessmentConfig.getFileName() +
	            		"status :" +upshiftAssessmentConfig.getStatus()+ " UpshiftAssessmentExecutionSubscriber" +e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());
		}
	}

	/**
	 * Create relationship between the report nodes based on uuid, primary and parent key
	 * @param uuid 
	 * @throws InsightsCustomException
	 */
	private static void createRelationship(String uuid) throws InsightsCustomException {
		
		log.debug("Correlated upshift assessment relationships for  : {}", uuid);
		StringBuilder cypherQuery = new StringBuilder();
		cypherQuery.append("MATCH (n:INSIGHTSUPSHIFT) where NOT exists(n.correlated) and exists(n.insightsParentKey) and exists(n.insightsTime)");
		cypherQuery.append("  and n.upshiftUuid = \"").append(uuid).append("\"");
		cypherQuery.append("  WITH DISTINCT n, n.insightsParentKey as Parent limit 1000 MATCH (a:INSIGHTSUPSHIFT) where a.insightsTime=Parent MERGE (a)-[:UPSHIFT_CHILD]->(n) set n.correlated=true return n");
				
		List<NodeData> nodes;
		do {
			GraphResponse graphResponse = dbHandler.executeCypherQuery(cypherQuery.toString());
			nodes = graphResponse.getNodes();
			if(!nodes.isEmpty())
				log.debug("Correlated upshift assessment nodes : {}", nodes.size());
			numOfNodesRelated+=nodes.size();
		} while (!nodes.isEmpty());
	}

	/**
	 * Update all required properties into database
	 * @param upshiftAssessmentConfig
	 * @param status
	 */
	private void updateReportStatus(UpshiftAssessmentConfig upshiftAssessmentConfig, String status) {
		if(upshiftAssessmentConfig != null) {
			long startTime = System.nanoTime();
			upshiftAssessmentConfig.setStatus(status);
			upshiftAssessmentConfig.setWorkflowConfig(upshiftAssessmentConfig.getWorkflowConfig());
			upshiftAssessmentConfig.setUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			upshiftAssessmentConfigDAL.updateUpshiftAssessmentConfig(upshiftAssessmentConfig);
			 long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
	            log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
	            		"-",workflowConfig.getWorkflowId(),upshiftAssessmentConfig.getId(),workflowConfig.getWorkflowType(),"-","-",processingTime
	            		,"UpshiftUuid :" +upshiftAssessmentConfig.getUpshiftUuid() +
	            		"CreatedDate :" +upshiftAssessmentConfig.getCreatedDate() +
	            		"UpdatedDate :"  +upshiftAssessmentConfig.getUpdatedDate() +
	            		"fileName :" +upshiftAssessmentConfig.getFileName() +
	            		"status :" +upshiftAssessmentConfig.getStatus());
		}		
	}
}
