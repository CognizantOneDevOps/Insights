/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformwebhookengine.test.engine;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.AWSSQSProvider;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQProvider;
import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class WebhookEngineTestData {

	/*
	 * Webhook test data
	 */
	private static Logger LOG = LogManager.getLogger(WebhookEngineTestData.class);
	public String emptyResponseTemplate = null;
	public String dataFormat = "JSON";
	public final String WEBHOOK_SUBSCRIBER_HEALTH_QUEUE = "WEBHOOKSUBSCRIBER_HEALTH";
	public final String WEBHOOK_SUBSCRIBER_HEALTH_ROUTING_KEY = WEBHOOK_SUBSCRIBER_HEALTH_QUEUE.replace("_", ".");
	public String healthMessageInstanceName = "localWebhookApp_PlatformInsightsWebHook_testData";
	public String mqChannel = "IPW_git_demo";
	public static String webhookName = "git_test";
	public String webhookNameException = "git_testException";
	public Boolean isUpdateRequired = Boolean.FALSE;
	public String fieldUsedForUpdate = "commitId";
	public DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	public LocalDateTime now = LocalDateTime.now();
	public String expectedOutput = "{\"commitTime\":\"2019-09-16T04:52:25Z\",\"authorName\":\"Insights_test\",\"insightsTime\":"
			+ ZonedDateTime.now().toInstant().toEpochMilli()
			+ ",\"webhookName\":\"git_demo\",\"commitId\":\"86ef096bb924674a69cd2198e2964b76aa75d88b\",\"source\":\"webhook\",\"message\":\"Update Hello\",\"inSightsTimeX\":\""
			+ dtf.format(now) + "\",\"labelName\":\"SCM:GIT76:DATA\",\"toollName\":\"GIT\"}";

	public static Set<WebhookDerivedConfig> getderivedOperationsJSONArray(String derivedOpsJsonLocal) {
		Set<WebhookDerivedConfig> setWebhookDerivedConfigs = new HashSet<WebhookDerivedConfig>();
		JsonArray array = new JsonArray();
		array = JsonUtils.parseStringAsJsonArray(derivedOpsJsonLocal);
		for (JsonElement webhookDerivedConfigJson : array) {
			WebhookDerivedConfig webhookDerivedConfig = new WebhookDerivedConfig();
			JsonObject receivedObject = webhookDerivedConfigJson.getAsJsonObject();
			int wid = receivedObject.get("wid").getAsInt();
			webhookDerivedConfig.setOperationName(receivedObject.get("operationName").getAsString());
			webhookDerivedConfig.setOperationFields(receivedObject.get("operationFields").toString());
			webhookDerivedConfig.setWebhookName(webhookName);
			if (wid != -1) {
				webhookDerivedConfig.setWid(wid);
			}
			setWebhookDerivedConfigs.add(webhookDerivedConfig);
		}
		return setWebhookDerivedConfigs;
	}

	@SuppressWarnings("unused")
	public void publishMessage(String queueName, String routingKey, String playload)
			throws IOException, TimeoutException, InsightsCustomException {
		String exchangeName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentExchange();
		Connection connection = null;
		Channel channel = null;
		connection = RabbitMQProvider.getConnection();
		channel = connection.createChannel();
		String message = new GsonBuilder().disableHtmlEscaping().create().toJson(playload);
		message = message.substring(1, message.length() - 1).replace("\\", "");
		DeclareOk exchangeResp;
		try {
			exchangeResp = channel.exchangeDeclarePassive(exchangeName);
			channel.queueDeclare(queueName, true, false, false, RabbitMQProvider.getQueueArguments());
			channel.queueBind(queueName, exchangeName, routingKey);
			channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
		} catch (IOException e) {

			/*
			 * if exception raised means exchange doesen't exists creating exchange in next
			 * block
			 */

			try {
				channel.exchangeDeclare(exchangeName, MQMessageConstants.EXCHANGE_TYPE);
				channel.queueDeclare(queueName, true, false, false, RabbitMQProvider.getQueueArguments());
				channel.queueBind(queueName, exchangeName, routingKey);
				channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
			} catch (IOException e1) {
				LOG.error(e1);
			}

		}

	}

	@SuppressWarnings("rawtypes")
	public static Map readNeo4JData(String nodeName, String compareFlag) {

		Map map = null;
		ObjectMapper mapper = new ObjectMapper();
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:" + nodeName + ") where n." + compareFlag
				+ "='8ea6c42b96d5c0ffdaf3622720450e2d5def75e6' return n";
		GraphResponse neo4jResponse;
		try {
			neo4jResponse = dbHandler.executeCypherQuery(query);
			JsonElement tooldataObject = neo4jResponse.getJson().get("results").getAsJsonArray().get(0)
					.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("row");

			String finalJson = tooldataObject.toString().replace("[", "").replace("]", "");
			map = mapper.readValue(finalJson, Map.class);
		} catch (Exception e) {
			LOG.error(e);
		}
		return map;
	}

	public static void publishMessage(String routingKey, String data) {
		try {
			if (ApplicationConfigProvider.getInstance().getMessageQueue().getProviderName().equalsIgnoreCase("AWSSQS"))
				AWSSQSProvider.publish(routingKey, data);
			else
				publishRMQMessage(routingKey, data);
		} catch (InsightsCustomException | JMSException e) {
			LOG.error(e.getMessage());
		}
	}

	public static String publishRMQMessage(String routingKey, String data) {
		String result = null;
		try {
			RabbitMQProvider.publish(routingKey, data);
			result = "Message published";
		} catch (InsightsCustomException | IOException | TimeoutException e) {
			LOG.error(e.getMessage());
		}
		return result;
	}
}
