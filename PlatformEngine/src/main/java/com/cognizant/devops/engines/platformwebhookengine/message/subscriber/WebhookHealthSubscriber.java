/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformwebhookengine.message.subscriber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformwebhookengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class WebhookHealthSubscriber extends EngineSubscriberResponseHandler {
	private static Logger log = LogManager.getLogger(WebhookHealthSubscriber.class);

	public WebhookHealthSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
		String routingKey = envelope.getRoutingKey();
		log.debug(" {}  Received  {} : {}", consumerTag, routingKey, message);
		List<JsonObject> dataList = new ArrayList<JsonObject>();
		JsonElement json = new JsonParser().parse(message);
		if (json.isJsonObject()) {
			log.debug("This is normal json object for webhook health ");
			dataList.add(json.getAsJsonObject());
		}

		try {
			if (!dataList.isEmpty()) {
				String healthLabels = ":" + routingKey.replace(".", ":");
				boolean isRecordUpdate = createHealthNodes(dbHandler, dataList, healthLabels);
				log.debug("Webhook Health Record update status ==== " + isRecordUpdate);
				if (isRecordUpdate) {
					getChannel().basicAck(envelope.getDeliveryTag(), false);
				}
			} else {
				log.error(" Data List is empty for webhook health record ");
				EngineStatusLogger.getInstance().createEngineStatusNode(
						" Data List is empty for webhook health record: " + routingKey,
						PlatformServiceConstants.FAILURE);
			}
		} catch (InsightsCustomException e) {
			log.error(e);
		}

	}

	/**
	 * @param dbHandler
	 * @param dataList
	 * @param nodeLabels
	 */
	private boolean createHealthNodes(GraphDBHandler dbHandler, List<JsonObject> dataList, String nodeLabels)
			throws InsightsCustomException {
		String cypherhealthQuery;
		boolean isRecordUpdate = Boolean.TRUE;
		// For Sequential/successive webhook health publishing
		cypherhealthQuery = "UNWIND {props} AS properties CREATE (n" + nodeLabels
				+ ") set n=properties return count(n)";

		JsonObject graphResponse = dbHandler.executeQueryWithData(cypherhealthQuery, dataList);
		if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
			isRecordUpdate = Boolean.FALSE;
			log.error("Unable to insert health nodes for routing key: {} error {}  ", nodeLabels, graphResponse);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					"Unable to insert health nodes for routing key: " + nodeLabels, PlatformServiceConstants.FAILURE);
		}
		return isRecordUpdate;
	}

}
