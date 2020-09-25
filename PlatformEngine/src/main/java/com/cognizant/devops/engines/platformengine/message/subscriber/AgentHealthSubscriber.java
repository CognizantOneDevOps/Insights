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
package com.cognizant.devops.engines.platformengine.message.subscriber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class AgentHealthSubscriber extends EngineSubscriberResponseHandler {
	private static Logger log = LogManager.getLogger(AgentHealthSubscriber.class.getName());
	GraphDBHandler dbHandler = new GraphDBHandler();

	public AgentHealthSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		try {
			String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
			String routingKey = envelope.getRoutingKey();
			log.debug(consumerTag + " [x] Received '" + routingKey + "':'" + message + "'");
			List<String> labels = Arrays.asList(routingKey.split(MQMessageConstants.ROUTING_KEY_SEPERATOR));
			List<JsonObject> dataList = new ArrayList<JsonObject>();
			List<JsonObject> failedDataList = new ArrayList<JsonObject>();
			JsonElement json = new JsonParser().parse(message);
			String agentId = "";
			String toolName = "";
			String categoryName = "";
			Boolean isFailure = false;
			if (json.isJsonArray()) {
				JsonArray asJsonArray = json.getAsJsonArray();
				for (JsonElement e : asJsonArray) {
					if (e.isJsonObject()) {
						JsonObject jsonObject = e.getAsJsonObject();
						if (jsonObject.has("agentId")) {
							agentId = jsonObject.get("agentId").getAsString();
						}
						if (jsonObject.has("toolName")) {
							toolName = jsonObject.get("toolName").getAsString();
							jsonObject.addProperty("toolName", toolName);
							categoryName = jsonObject.get("categoryName").getAsString();
							jsonObject.addProperty("category", categoryName);
						}

						else {
							if (labels.size() > 1) {
								jsonObject.addProperty("category", labels.get(0));
								jsonObject.addProperty("toolName", labels.get(1));
							}
						}
						/**
						 * If health message status is failure, create a separate node for
						 * HEALTH_FAILURE HEALTH_FAILURE node will contain latest 20 error messages
						 */
						String healthStatus = "";
						if (jsonObject.has("status")) {
							healthStatus = jsonObject.get("status").getAsString();
							if (healthStatus.equalsIgnoreCase("failure")) {
								isFailure = true;
								failedDataList.add(jsonObject);
							}
						}
						dataList.add(jsonObject);
					}
				}
				String healthLabels = ":LATEST:" + routingKey.replace(".", ":");
				createHealthNodes(dbHandler, routingKey, dataList, agentId, healthLabels, 10, "LATEST");

				if (isFailure) {
					String failureLabels = routingKey.replace(".", ":");
					failureLabels = failureLabels.replace("HEALTH", "HEALTH_FAILURE");
					String healthFailureLabels = ":LATEST_FAILURE:" + failureLabels;
					createHealthNodes(dbHandler, routingKey, failedDataList, agentId, healthFailureLabels, 20,
							"LATEST_FAILURE");
				}

				getChannel().basicAck(envelope.getDeliveryTag(), false);

			}
		} catch (InsightsCustomException e) {
			log.error(e);
		}
	}

	/**
	 * @param dbHandler
	 * @param routingKey
	 * @param dataList
	 * @param agentId
	 * @param nodeLabels
	 * @throws InsightsCustomException
	 */
	private void createHealthNodes(GraphDBHandler dbHandler, String routingKey, List<JsonObject> dataList,
			String agentId, String nodeLabels, int nodeCount, String latestLabel) throws InsightsCustomException {
		String healthQuery;
		// For Sequential/successive agent health publishing where agentId is not null
		if (!agentId.equalsIgnoreCase("")) {
			healthQuery = "Match";
			healthQuery = healthQuery + " (old" + nodeLabels + ")";
			healthQuery = healthQuery + " where old.agentId='" + agentId + "' or old.agentId is null";
			healthQuery = healthQuery + " OPTIONAL MATCH (old) <-[:UPDATED_TO*" + nodeCount
					+ "]-(purge)  where old.agentId='" + agentId + "'";
			healthQuery = healthQuery + " CREATE (new" + nodeLabels + " {props}) ";
			healthQuery = healthQuery + " MERGE  (new)<-[r:UPDATED_TO]-(old)";
			healthQuery = healthQuery + " REMOVE old:" + latestLabel;
			healthQuery = healthQuery + " detach delete purge ";
			healthQuery = healthQuery + " return old,new";

		}
		// For first time agent health publishing when agentId is null
		else {
			healthQuery = "Match (old" + nodeLabels + ")";
			healthQuery = healthQuery + " OPTIONAL MATCH (old) <-[:UPDATED_TO*" + nodeCount + "]-(purge) ";
			healthQuery = healthQuery + " CREATE (new" + nodeLabels + " {props})";
			healthQuery = healthQuery + " MERGE  (new)<-[r:UPDATED_TO]-(old)";
			healthQuery = healthQuery + " REMOVE old:" + latestLabel;
			healthQuery = healthQuery + " detach delete purge ";
			healthQuery = healthQuery + " return old,new";
		}

		JsonObject graphResponse = dbHandler.executeQueryWithData(healthQuery, dataList);
		if (graphResponse.get("response").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject()
				.get("data").getAsJsonArray().size() == 0) {
			healthQuery = "";
			healthQuery = healthQuery + " CREATE (new" + nodeLabels + " {props})";
			JsonObject graphResponse1 = dbHandler.executeQueryWithData(healthQuery, dataList);
		}
		if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
			log.error("Unable to insert health nodes for routing key: " + routingKey + ", error occured: "
					+ graphResponse);
			log.error(dataList);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					"Unable to insert health nodes for routing key: " + routingKey, PlatformServiceConstants.FAILURE);
		}
	}
}
