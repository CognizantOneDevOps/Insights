/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformroi.subscriber;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.enums.MilestoneEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.cognizant.devops.platformdal.milestone.InsightsMileStoneOutcomeConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfigDAL;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;

public class MilestoneExecutor {

	private static Logger log = LogManager.getLogger(MilestoneExecutor.class);
	MileStoneConfigDAL mileStoneConfigDAL = new MileStoneConfigDAL();

	public void executeMilestone()throws InsightsCustomException {
		try {
			log.debug("Milestone Executor started ... ");

			List<MileStoneConfig> mileStoneConfigList =  mileStoneConfigDAL.fetchMileStoneByStatus();
			
			for (MileStoneConfig mileStoneConfig : mileStoneConfigList) {
				publishMilestoneDetail(mileStoneConfig);
			}
			log.debug(" Milestone Executor completed successfully ");
		}catch (Exception e) {
			log.error("MileStoneExecutionSubscriber Completed with error ", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	private void publishMilestoneDetail(MileStoneConfig mileStoneConfig) {
		try {
			Long startDate = mileStoneConfig.getStartDate();
			if(startDate < Instant.now().getEpochSecond()) {
				mileStoneConfig.setStatus("IN_PROGRESS");
				mileStoneConfigDAL.updateMileStoneConfig(mileStoneConfig);
				createToolBasedJson(mileStoneConfig);
			}
		} catch (Exception e) {
			log.error(" Error while publishing milestone message ",e);
			log.error(e);
		}
	}
	
	private void createToolBasedJson(MileStoneConfig mileStoneConfig) {
		for(InsightsMileStoneOutcomeConfig outcome: mileStoneConfig.getListOfOutcomes()) {
			if(outcome.getStatus().equalsIgnoreCase(MilestoneEnum.OutcomeStatus.NOT_STARTED.name()) || outcome.getStatus().equalsIgnoreCase(MilestoneEnum.OutcomeStatus.RESTART.name())) {
				try {
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("milestoneId",outcome.getMileStoneConfig().getId());
					jsonObject.addProperty("milestoneName",outcome.getMileStoneConfig().getMileStoneName());
					jsonObject.addProperty("startDate", outcome.getMileStoneConfig().getStartDate());
					jsonObject.addProperty("endDate",outcome.getMileStoneConfig().getEndDate());
					jsonObject.addProperty("configJson", outcome.getInsightsOutcomeTools().getConfigJson());
					jsonObject.addProperty("outcomeName", outcome.getInsightsOutcomeTools().getOutcomeName());
					jsonObject.addProperty("outcomeId", outcome.getInsightsOutcomeTools().getId());
					jsonObject.addProperty("outcomeType", outcome.getInsightsOutcomeTools().getOutcomeType());
					jsonObject.addProperty("queueName", outcome.getInsightsOutcomeTools().getInsightsTools().getAgentCommunicationQueue());
					jsonObject.addProperty("toolName", outcome.getInsightsOutcomeTools().getInsightsTools().getToolName());
					jsonObject.addProperty("statusQueue",MQMessageConstants.MILESTONE_STATUS_QUEUE);
					jsonObject.addProperty("metricUrl",outcome.getInsightsOutcomeTools().getMetricUrl());
					jsonObject.addProperty("requestParameters",outcome.getInsightsOutcomeTools().getRequestParameters());
					publishMessageInMQ(outcome.getInsightsOutcomeTools().getInsightsTools().getAgentCommunicationQueue(),jsonObject.toString());
					mileStoneConfigDAL.updateMilestoneOutcomeStatus(outcome.getMileStoneConfig().getId(), outcome.getInsightsOutcomeTools().getId(), 
							MilestoneEnum.OutcomeStatus.OUTCOME_SENT_TO_AGENT.name(), MilestoneEnum.OutcomeStatus.OUTCOME_SENT_TO_AGENT.getValue());
				} catch (InsightsCustomException e) {
					log.error(" Error while publishing outcome name as {} ",outcome.getInsightsOutcomeTools().getOutcomeName());
				}
			}
		}
		log.debug("Milestone data publish successfully  ");
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
