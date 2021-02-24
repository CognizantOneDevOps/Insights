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

package com.cognizant.devops.platformreports.assessment.vsm.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.vsmReport.VsmReportConfig;
import com.cognizant.devops.platformdal.vsmReport.VsmReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VsmReportRelationExecutionSubscriber extends WorkflowTaskSubscriberHandler {

	private static Logger log = LogManager.getLogger(VsmReportRelationExecutionSubscriber.class);
	InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	private WorkflowDAL workflowDAL = new WorkflowDAL();

	private static GraphDBHandler dbHandler = new GraphDBHandler();
	private static int numOfNodesRelated = 0;
	private VsmReportConfigDAL vsmReportConfigDAL = new VsmReportConfigDAL();

	public VsmReportRelationExecutionSubscriber(String routingKey) throws IOException {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		VsmReportConfig vsmReportConfig = null;
		try {
			String incomingTaskMessage = new String(body, StandardCharsets.UTF_8);
			log.debug("Worlflow Detail ==== VsmReportExecutionSubscriber started ... "
					+ "routing key  message handleDelivery ===== {} ", incomingTaskMessage);

			JsonObject incomingTaskMessageJson = new JsonParser().parse(incomingTaskMessage).getAsJsonObject();
			String workflowId = incomingTaskMessageJson.get("workflowId").getAsString();
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			log.debug("Worlflow Detail ==== scheduleType {} ", workflowConfig.getScheduleType());
			vsmReportConfig = vsmReportConfigDAL.fetchGrafanaDashboardDetailsByWorkflowId(workflowId);
			createRelationship(vsmReportConfig.getUuid());
			
			log.debug("Total nodes related: {}", numOfNodesRelated);
			updateReportStatus(vsmReportConfig, WorkflowTaskEnum.VsmReportStatus.COMPLETED.name());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== GrafanaPDFExecutionSubscriber Completed with error ", e);
			updateReportStatus(vsmReportConfig, WorkflowTaskEnum.VsmReportStatus.ERROR.name());
			throw new InsightsJobFailedException(e.getMessage());
		}
	}

	/**
	 * Create relationship between the report nodes based on uuid, primary and parent key
	 * @param uuid 
	 * @throws InsightsCustomException
	 */
	private static void createRelationship(String uuid) throws InsightsCustomException {
		
		log.debug("Correlated Vsm report relationships for  : {}", uuid);
		StringBuilder cypherQuery = new StringBuilder();
		cypherQuery.append("MATCH (n:INSIGHTSVSMREPORT) where NOT exists(n.correlated) and exists(n.insightsParentKey) and exists(n.insightsPrimaryKey)");
		cypherQuery.append("  and n.vsm_uuid = \"").append(uuid).append("\"");
		cypherQuery.append("  WITH DISTINCT n, n.insightsParentKey as Parent limit 1000 MATCH (a:INSIGHTSVSMREPORT) where a.insightsPrimaryKey=Parent MERGE (a)-[:VSMREPORT_CHILD]->(n) set n.correlated=true return n");
				
		List<NodeData> nodes;
		do {
			GraphResponse graphResponse = dbHandler.executeCypherQuery(cypherQuery.toString());
			nodes = graphResponse.getNodes();
			if(!nodes.isEmpty())
				log.debug("Correlated Vsm report nodes : {}", nodes.size());
			numOfNodesRelated+=nodes.size();
		} while (!nodes.isEmpty());
	}

	/**
	 * Update all required properties into database
	 * @param vsmReportConfig
	 * @param status
	 */
	private void updateReportStatus(VsmReportConfig vsmReportConfig, String status) {
		if(vsmReportConfig!= null) {
			vsmReportConfig.setStatus(status);
			vsmReportConfig.setWorkflowConfig(vsmReportConfig.getWorkflowConfig());
			vsmReportConfig.setUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			vsmReportConfigDAL.updateVsmReportConfig(vsmReportConfig);
		}
	}
}
