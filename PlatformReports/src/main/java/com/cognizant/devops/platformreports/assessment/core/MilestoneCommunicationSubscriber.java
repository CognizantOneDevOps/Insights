/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.enums.MilestoneEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.cognizant.devops.platformdal.milestone.MileStoneConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfigDAL;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class MilestoneCommunicationSubscriber extends WorkflowTaskSubscriberHandler{
	
	private static Logger log = LogManager.getLogger(MilestoneCommunicationSubscriber.class.getName());
	
	MileStoneConfigDAL mileStoneConfigDAL = new MileStoneConfigDAL();

	public MilestoneCommunicationSubscriber(String routingKey) throws IOException, InsightsCustomException {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		String incomingTaskMessage = new String(body, StandardCharsets.UTF_8);
		log.debug("Worlflow Detail ==== MilestoneCommunicationSubscriber started ... "
				+ "routing key  message handleDelivery ===== {} ",incomingTaskMessage);

		JsonObject incomingTaskMessageJson = JsonUtils.parseStringAsJsonObject(incomingTaskMessage);
		String workflowId = incomingTaskMessageJson.get("workflowId").getAsString();
		String routingKey = MQMessageConstants.MILESTONE_STATUS_QUEUE.replace("_", ".");
		Channel channel;
		try {
			channel = RabbitMQConnectionProvider.getChannel(routingKey, MQMessageConstants.MILESTONE_STATUS_QUEUE, MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE);
			setChannel(channel);
			
			log.debug("prefetchCount {} for routingKey {} ",
					ApplicationConfigProvider.getInstance().getMessageQueue().getPrefetchCount(), routingKey);
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] statusbody) throws IOException {
					try {
						String incomingStatusMessage = new String(statusbody, StandardCharsets.UTF_8);
						log.debug(" ROI execution incomingStatusMessage : {}", incomingStatusMessage);
						JsonObject incomingStatusMessageJson = JsonUtils.parseStringAsJsonObject(incomingStatusMessage);
						int milestoneId = incomingStatusMessageJson.get("milestoneId").getAsInt();
						int outcomeId = incomingStatusMessageJson.get("outcomeId").getAsInt();
						String status = incomingStatusMessageJson.get("status").getAsString();
						String message = incomingStatusMessageJson.get("message").getAsString();
						mileStoneConfigDAL.updateMilestoneOutcomeStatus(milestoneId, outcomeId, status, message);
						MileStoneConfig mileStoneConfig = mileStoneConfigDAL.getMileStoneConfigById(milestoneId);
						
						boolean updateFlag = mileStoneConfig.getListOfOutcomes().stream().allMatch(outcome-> outcome.getStatus().equalsIgnoreCase("SUCCESS"));
						boolean updateErrorFlag = mileStoneConfig.getListOfOutcomes().stream().anyMatch(outcome-> outcome.getStatus().equalsIgnoreCase("ERROR"));
						if(updateFlag) {
							mileStoneConfigDAL.updateMilestoneStatus(milestoneId, MilestoneEnum.MilestoneStatus.COMPLETED.name());
						} else if(updateErrorFlag) {
							mileStoneConfigDAL.updateMilestoneStatus(milestoneId, MilestoneEnum.MilestoneStatus.ERROR.name());
						} else {
							mileStoneConfigDAL.updateMilestoneStatus(milestoneId, MilestoneEnum.MilestoneStatus.IN_PROGRESS.name());
						}
						
						getChannel().basicAck(envelope.getDeliveryTag(), false);
					} catch (Exception e) {
						log.error("Error : ", e);
					}
				}
			};
			channel.basicConsume(MQMessageConstants.MILESTONE_STATUS_QUEUE, false, routingKey, consumer);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== MilestoneCommunicationSubscriber Completed with error ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
		
		
	}

}
