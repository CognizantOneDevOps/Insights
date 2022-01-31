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
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformcommons.core.enums.MilestoneEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformdal.milestone.MileStoneConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfigDAL;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class MilestoneStatusCommunicationSubscriber extends EngineSubscriberResponseHandler {
	
	private static Logger log = LogManager.getLogger(MilestoneStatusCommunicationSubscriber.class.getName());
	GraphDBHandler dbHandler = new GraphDBHandler();
	MileStoneConfigDAL mileStoneConfigDAL = new MileStoneConfigDAL();

	public MilestoneStatusCommunicationSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}
	
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		try {
			String incomingTaskMessage = new String(body, StandardCharsets.UTF_8);
			log.debug("MilestoneCommunicationSubscriber started ... "
					+ "routing key  message handleDelivery ===== {} ",incomingTaskMessage);

			JsonObject incomingStatusMessageJson = JsonUtils.parseStringAsJsonObject(incomingTaskMessage);
			int milestoneId = incomingStatusMessageJson.get("milestoneId").getAsInt();
			int outcomeId = incomingStatusMessageJson.get("outcomeId").getAsInt();
			String status = incomingStatusMessageJson.get("status").getAsString();
			String message = incomingStatusMessageJson.get("message").getAsString();
			mileStoneConfigDAL.updateMilestoneOutcomeStatus(milestoneId, outcomeId, status, message);
			MileStoneConfig mileStoneConfig = mileStoneConfigDAL.getMileStoneConfigById(milestoneId);
			
			boolean updateFlag = mileStoneConfig.getListOfOutcomes().stream().allMatch(outcome-> outcome.getStatus().equalsIgnoreCase(MilestoneEnum.OutcomeStatus.SUCCESS.name()));
			boolean updateErrorFlag = mileStoneConfig.getListOfOutcomes().stream().anyMatch(outcome-> outcome.getStatus().equalsIgnoreCase( MilestoneEnum.OutcomeStatus.ERROR.name()));
			if(updateFlag) {
				mileStoneConfigDAL.updateMilestoneStatus(milestoneId, MilestoneEnum.MilestoneStatus.COMPLETED.name());
			} else if(updateErrorFlag) {
				mileStoneConfigDAL.updateMilestoneStatus(milestoneId, MilestoneEnum.MilestoneStatus.ERROR.name());
			} else {
				mileStoneConfigDAL.updateMilestoneStatus(milestoneId, MilestoneEnum.MilestoneStatus.IN_PROGRESS.name());
			}
			
			getChannel().basicAck(envelope.getDeliveryTag(), false);
		} catch (Exception e) {
			log.error("Error in payload {} ",e);
			getChannel().basicReject(envelope.getDeliveryTag(), false);
		}
	}
}