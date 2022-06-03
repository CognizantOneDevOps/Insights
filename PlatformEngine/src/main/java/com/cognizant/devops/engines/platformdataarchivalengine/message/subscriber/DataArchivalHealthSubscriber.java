/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

package com.cognizant.devops.engines.platformdataarchivalengine.message.subscriber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformcommons.constants.DataArchivalConstants;
import com.cognizant.devops.platformcommons.constants.EngineConstants;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.dataArchivalConfig.DataArchivalConfigDal;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class DataArchivalHealthSubscriber extends EngineSubscriberResponseHandler {
	
	private static Logger log = LogManager.getLogger(DataArchivalHealthSubscriber.class.getName());
	GraphDBHandler dbHandler = new GraphDBHandler();
	DataArchivalConfigDal dataArchivalConfigDal = new DataArchivalConfigDal();
	private Map<String,String> loggingInfo = new ConcurrentHashMap<>();

	public DataArchivalHealthSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		
		String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
		try {
			String routingKey = envelope.getRoutingKey();
			List<JsonObject> dataList = new ArrayList<>();
			List<JsonObject> failedDataList = new ArrayList<>();
			Boolean isFailure = false;
			String healthStatus = "";
			JsonElement json = JsonUtils.parseString(message);
			JsonObject messageJson = json.getAsJsonArray().get(0).getAsJsonObject();
			String agentId = messageJson.get(AgentCommonConstant.AGENTID).getAsString();
			String toolName = messageJson.get(AgentCommonConstant.TOOLNAME).getAsString();
			messageJson.addProperty(AgentCommonConstant.TOOLNAME, toolName);
			String categoryName = messageJson.get("categoryName").getAsString();
			messageJson.addProperty(AgentCommonConstant.CATEGORY, categoryName);
			loggingInfo.put(AgentCommonConstant.TOOLNAME, toolName);
			loggingInfo.put(AgentCommonConstant.CATEGORY, categoryName);
			loggingInfo.put(AgentCommonConstant.AGENTID, agentId);
			loggingInfo.put(EngineConstants.EXECID,String.valueOf( messageJson.get(EngineConstants.EXECID)));
			log.debug(" Type=DataArchival toolName={} category={} agentId={} routingKey={} execId={} Received :{} {} {}",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),"-",loggingInfo.get(EngineConstants.EXECID), consumerTag, routingKey, message);
			if (messageJson.has("status")) {
				healthStatus = messageJson.get("status").getAsString();
				if (healthStatus.equalsIgnoreCase("failure")) {
					isFailure = true;
					failedDataList.add(messageJson);
				}
			}
			dataList.add(messageJson);
			String healthLabels = ":LATEST:" + routingKey.replace(".", ":");
			createHealthNodes(dataList, agentId, healthLabels, 10, "LATEST");
			if (isFailure) {
				String failureLabels = routingKey.replace(".", ":");
				failureLabels = failureLabels.replace("HEALTH", "HEALTH_FAILURE");
				String healthFailureLabels = ":LATEST_FAILURE:" + failureLabels;
				createHealthNodes(failedDataList, agentId, healthFailureLabels, 20, "LATEST_FAILURE");
				updateErrorStateInArchivalRecord(messageJson);
			}
			getChannel().basicAck(envelope.getDeliveryTag(), false);
			log.debug(" Type=DataArchival toolName={} category={} agentId={} routingKey={} execId={} Data Archival Health message processed.",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),"-",loggingInfo.get(EngineConstants.EXECID));
		} catch (InsightsCustomException e) {
			log.error(" toolName={} category={} agentId={} execId={} Error occured in Data Archival Health Subscriber.Health message: {}",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),loggingInfo.get(EngineConstants.EXECID),message,e);
			getChannel().basicReject(envelope.getDeliveryTag(), false);
		}
	}
	
	private void updateErrorStateInArchivalRecord(JsonObject messageJson) {
		try {
			if (messageJson.has(DataArchivalConstants.ARCHIVALNAME)) {
				if (!messageJson.get(DataArchivalConstants.ARCHIVALNAME).getAsString().isEmpty()) {
					if (messageJson.has(DataArchivalConstants.TASK) && "remove_container"
							.equalsIgnoreCase(messageJson.get(DataArchivalConstants.TASK).getAsString())) {
						dataArchivalConfigDal.updateArchivalStatus(
								messageJson.get(DataArchivalConstants.ARCHIVALNAME).getAsString(),
								DataArchivalStatus.ERROR_REMOVE_CONTAINER.toString());
					} else {
						dataArchivalConfigDal.updateArchivalStatus(
								messageJson.get(DataArchivalConstants.ARCHIVALNAME).getAsString(),
								DataArchivalStatus.ERROR.toString());
					}
					log.debug(
							" Type=DataArchival toolName={} category={} agentId={} routingKey={} execId={} Updated Error state in Data Archival record.",
							loggingInfo.get(AgentCommonConstant.TOOLNAME),
							loggingInfo.get(AgentCommonConstant.CATEGORY), loggingInfo.get(AgentCommonConstant.AGENTID),
							"-", loggingInfo.get(EngineConstants.EXECID));
				} else {
					log.error(" toolName={} category={} agentId={} execId={} Archival name not provided",
							loggingInfo.get(AgentCommonConstant.TOOLNAME),
							loggingInfo.get(AgentCommonConstant.CATEGORY), loggingInfo.get(AgentCommonConstant.AGENTID),
							loggingInfo.get(EngineConstants.EXECID));
				}
			} else {
				log.error(" toolName={} category={} agentId={} execId={} Archival name property not present in message",
						loggingInfo.get(AgentCommonConstant.TOOLNAME), loggingInfo.get(AgentCommonConstant.CATEGORY),
						loggingInfo.get(AgentCommonConstant.AGENTID), loggingInfo.get(EngineConstants.EXECID));
			}
		} catch (Exception e) {
			log.error(
					" toolName={} category={} agentId={} execId={} Error occured in Data Archival Health Subscriber.Health message: {}",
					loggingInfo.get(AgentCommonConstant.TOOLNAME), loggingInfo.get(AgentCommonConstant.CATEGORY),
					loggingInfo.get(AgentCommonConstant.AGENTID), loggingInfo.get(EngineConstants.EXECID),
					e.getMessage(), e);
		}
	}
	private void createHealthNodes(List<JsonObject> dataList, String agentId, String nodeLabels, int nodeCount,
			String latestLabel) throws InsightsCustomException {
		String healthQuery;
		healthQuery = "Match";
		healthQuery = healthQuery + " (old" + nodeLabels + ")";
		healthQuery = healthQuery + " where old.agentId='" + agentId + "' or old.agentId is null";
		healthQuery = healthQuery + " OPTIONAL MATCH (old) <-[:UPDATED_TO*" + nodeCount
				+ "]-(purge)  where old.agentId='" + agentId + "'";
		healthQuery = healthQuery + " CREATE (new" + nodeLabels + " $props) ";
		healthQuery = healthQuery + " MERGE  (new)<-[r:UPDATED_TO]-(old)";
		healthQuery = healthQuery + " REMOVE old:" + latestLabel;
		healthQuery = healthQuery + " detach delete purge ";
		healthQuery = healthQuery + " return old,new";

		JsonObject graphResponse = dbHandler.executeQueryWithData(healthQuery, dataList);
		if (graphResponse.get("response").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject()
				.get("data").getAsJsonArray().size() == 0) {
			healthQuery = "";
			healthQuery = healthQuery + " CREATE (new" + nodeLabels + " $props)";
			dbHandler.executeQueryWithData(healthQuery, dataList);
		}
		if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
			log.error(" toolName={} category={} agentId={} execId={} Unable to insert health nodes for routing key: {} error {}  ",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),loggingInfo.get(EngineConstants.EXECID), nodeLabels, graphResponse);
		}
	}
}