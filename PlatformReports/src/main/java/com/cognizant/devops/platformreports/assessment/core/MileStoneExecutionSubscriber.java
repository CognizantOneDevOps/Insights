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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.cognizant.devops.platformdal.milestone.InsightsMileStoneOutcomeConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;

public class MileStoneExecutionSubscriber extends WorkflowTaskSubscriberHandler{

	private static Logger log = LogManager.getLogger(MileStoneExecutionSubscriber.class.getName());

	private WorkflowDAL workflowDAL = new WorkflowDAL();
	InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	InsightsAssessmentConfigurationDTO assessmentReportDTO = null;
	MileStoneConfigDAL mileStoneConfigDAL = new MileStoneConfigDAL();

	public MileStoneExecutionSubscriber(String routingKey) throws IOException, InsightsCustomException {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		try {
			String incomingTaskMessage = new String(body, StandardCharsets.UTF_8);
			log.debug("Worlflow Detail ==== MileStoneExecutionSubscriber started ... "
					+ "routing key  message handleDelivery ===== {} ",incomingTaskMessage);

			JsonObject incomingTaskMessageJson = JsonUtils.parseStringAsJsonObject(incomingTaskMessage);
			String workflowId = incomingTaskMessageJson.get("workflowId").getAsString();
			long executionId = incomingTaskMessageJson.get("executionId").getAsLong();
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			log.debug("Worlflow Detail ==== scheduleType {} ", workflowConfig.getScheduleType());

			assessmentReportDTO = new InsightsAssessmentConfigurationDTO();
			assessmentReportDTO.setIncomingTaskMessageJson(incomingTaskMessage);
			assessmentReportDTO.setExecutionId(executionId);
			assessmentReportDTO.setWorkflowId(workflowId);

			MileStoneConfig mileStoneConfig =  mileStoneConfigDAL.fetchMileStoneByWorkflowId(assessmentReportDTO.getWorkflowId());
			Long startDate = mileStoneConfig.getStartDate();
			if(startDate < Instant.now().getEpochSecond()) {
				mileStoneConfig.setStatus("IN_PROGRESS");
				mileStoneConfigDAL.updateMileStoneConfig(mileStoneConfig);
				List<JsonObject> listOfMilestoneJson = createToolBasedJson(mileStoneConfig);
				for(JsonObject milestone : listOfMilestoneJson) {
					publishMessageInMQ(milestone.get("queueName").getAsString(),milestone.toString());
				}
			}
		}catch (Exception e) {
			log.error("Worlflow Detail ==== MileStoneExecutionSubscriber Completed with error ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
	}

	private List<JsonObject> createToolBasedJson(MileStoneConfig mileStoneConfig) {

		List<JsonObject> config = new ArrayList<>();
		for(InsightsMileStoneOutcomeConfig milestoneOutcomeConf: mileStoneConfig.getListOfOutcomes()) {
			if(milestoneOutcomeConf.getStatus().equalsIgnoreCase("NOT_STARTED") || milestoneOutcomeConf.getStatus().equalsIgnoreCase("ERROR")) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("milestoneId",milestoneOutcomeConf.getMileStoneConfig().getId());
				jsonObject.addProperty("milestoneName",milestoneOutcomeConf.getMileStoneConfig().getMileStoneName());
				jsonObject.addProperty("startDate", milestoneOutcomeConf.getMileStoneConfig().getStartDate());
				jsonObject.addProperty("endDate",milestoneOutcomeConf.getMileStoneConfig().getEndDate());
				jsonObject.addProperty("configJson", milestoneOutcomeConf.getInsightsOutcomeTools().getConfigJson());
				jsonObject.addProperty("outcomeName", milestoneOutcomeConf.getInsightsOutcomeTools().getOutcomeName());
				jsonObject.addProperty("outcomeId", milestoneOutcomeConf.getInsightsOutcomeTools().getId());
				jsonObject.addProperty("outcomeType", milestoneOutcomeConf.getInsightsOutcomeTools().getOutcomeType());
				jsonObject.addProperty("queueName", milestoneOutcomeConf.getInsightsOutcomeTools().getInsightsTools().getAgentCommunicationQueue());
				jsonObject.addProperty("toolName", milestoneOutcomeConf.getInsightsOutcomeTools().getInsightsTools().getToolName());
				jsonObject.addProperty("statusQueue",MQMessageConstants.MILESTONE_STATUS_QUEUE);
				jsonObject.addProperty("metricUrl",milestoneOutcomeConf.getInsightsOutcomeTools().getMetricUrl());
				jsonObject.addProperty("requestParameters",milestoneOutcomeConf.getInsightsOutcomeTools().getRequestParameters());
				config.add(jsonObject);
			}
			
		}
		log.debug("Milestone json  {} ",config.toArray());
		return config;
	}


	public void publishMessageInMQ(String routingKey, String publishDataJson) throws InsightsCustomException
	{
		String queueName = routingKey.replace(".", "_");
		try (Channel channel = RabbitMQConnectionProvider.getChannel(routingKey, queueName, MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE)) {
			channel.basicPublish(MQMessageConstants.EXCHANGE_NAME, routingKey, null, publishDataJson.getBytes());
		} catch (IOException | TimeoutException e) {
			log.debug("Error while publishing message in queue");
		}
	}
	

}


