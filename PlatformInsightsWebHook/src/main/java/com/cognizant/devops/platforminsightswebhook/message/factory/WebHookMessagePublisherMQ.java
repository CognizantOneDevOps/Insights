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
package com.cognizant.devops.platforminsightswebhook.message.factory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cognizant.devops.platforminsightswebhook.application.AppProperties;
import com.cognizant.devops.platforminsightswebhook.config.WebHookConstants;
import com.cognizant.devops.platforminsightswebhook.message.core.SubscriberStatusLogger;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component("webhookmessagepublisherMQ")
public class WebHookMessagePublisherMQ extends WebhookMessageFactory {

	private static Logger LOG = LogManager.getLogger(WebHookMessagePublisherMQ.class);
	private static ConnectionFactory factory;
	private static Connection connection;
	Channel channel;
	private static String exchangeName;
	private static String routingKey;
	Map<String, Channel> mqMappingMap = new HashMap<>(0);

	static WebHookMessagePublisherMQ webhookmessagepublisher;

	@Override
	public void initializeConnection() throws Exception {
		LOG.debug(
				" In initilizeMq ======== host = {} port = {} user = {} passcode = {} exchangeName= {} enableDeadLetterExchange = {}",
				AppProperties.mqHost, AppProperties.port, AppProperties.mqUser, AppProperties.mqPassword,
				AppProperties.mqExchangeName, AppProperties.enableDeadLetterExchange);
		try {
			exchangeName = AppProperties.mqExchangeName;
			routingKey = WebHookConstants.WEBHOOK_EVENTDATA;
			factory = new ConnectionFactory();
			factory.setHost(AppProperties.mqHost);
			factory.setUsername(AppProperties.mqUser);
			factory.setPassword(AppProperties.mqPassword);
			factory.setPort(AppProperties.port);
			connection = factory.newConnection();
			if (AppProperties.enableDeadLetterExchange) {
				declareDeadLetterExchange(connection);
			}
			SubscriberStatusLogger.getInstance().createSubsriberStatusNode(
					" Instance Name " + AppProperties.instanceName + " : Connection with Rabbit Mq for host "
							+ AppProperties.mqHost + " established successfully. ",
					WebHookConstants.SUCCESS);
		} catch (Exception e) {
			LOG.error("Error while initilize mq ", e);
			SubscriberStatusLogger.getInstance().createSubsriberStatusNode(
					"Platform Webhook Subscriber : problem in connection with Rabbit Mq host " + AppProperties.mqHost,
					WebHookConstants.FAILURE);
			throw e;

		}

	}

	private static void declareDeadLetterExchange(Connection connection) {
		try (Channel channelForDeadLetter = connection.createChannel()) {
			channelForDeadLetter.exchangeDeclare(WebHookConstants.RECOVER_EXCHANGE_NAME,
					WebHookConstants.RECOVER_EXCHANGE_TYPE, true);
			channelForDeadLetter.queueDeclare(WebHookConstants.RECOVER_QUEUE, true, false, false, null);
			channelForDeadLetter.queueBind(WebHookConstants.RECOVER_QUEUE, WebHookConstants.RECOVER_EXCHANGE_NAME,
					WebHookConstants.RECOVER_ROUNTINGKEY_QUEUE);
		} catch (Exception e) {
			LOG.error("Error while initilize mq declareDeadLetterExchange Queue ", e);
			SubscriberStatusLogger.getInstance().createSubsriberStatusNode(
					"Error while initilize mq declareDeadLetterExchange Queue " + AppProperties.mqHost,
					WebHookConstants.FAILURE);
		}
	}

	@Override
	public void publishEventAction(String data, String webHookMqChannelName) throws Exception {
		LOG.debug(" Inside publishEventAction ==== {}", connection);
		try {

			if (!connection.isOpen()) {
				LOG.debug(" Connection is not open " + " connection{} ", connection.isOpen());
				initializeConnection();
			}
			if (mqMappingMap.containsKey(webHookMqChannelName)) {
				channel = mqMappingMap.get(webHookMqChannelName);
				channel.basicPublish(exchangeName, webHookMqChannelName, null, data.getBytes());
				LOG.debug(" data published in queue {}", webHookMqChannelName);
			} else {
				channel = connection.createChannel();
				Map<String, Object> args = new HashMap<>();

				if (AppProperties.enableDeadLetterExchange) {
					args.put(WebHookConstants.RECOVER_EXCHANGE_PROPERTY, WebHookConstants.RECOVER_EXCHANGE_NAME);
				}

				channel.exchangeDeclare(exchangeName, WebHookConstants.EXCHANGE_TYPE, true);
				channel.queueDeclare(webHookMqChannelName, true, false, false, args);
				channel.queueBind(webHookMqChannelName, exchangeName, webHookMqChannelName);
				channel.basicPublish(exchangeName, webHookMqChannelName, null, data.getBytes());
				LOG.debug(" data published first time in queue  {}", webHookMqChannelName);
				mqMappingMap.put(webHookMqChannelName, channel);
			}
		} catch (Exception e) {
			LOG.error("Error while publishEventAction ", e);
			throw e;
		}
	}

	@Override
	public void publishHealthData(String data, String healthQueueName) throws TimeoutException, IOException {
		LOG.debug(" Inside publishHealthData ==== ");
		try {
			channel = connection.createChannel();
			String healthRoutingKey = healthQueueName.replace("_", ".");
			Map<String, Object> args = new HashMap<>();
			if (AppProperties.enableDeadLetterExchange) {
				args.put(WebHookConstants.RECOVER_EXCHANGE_PROPERTY, WebHookConstants.RECOVER_EXCHANGE_NAME);
			}

			channel.exchangeDeclare(exchangeName, WebHookConstants.EXCHANGE_TYPE, true);
			channel.queueDeclare(healthQueueName, true, false, false, args);
			channel.queueBind(healthQueueName, exchangeName, healthRoutingKey);
			channel.basicPublish(exchangeName, healthRoutingKey, null, data.getBytes());
			LOG.debug(" Health data published first time in queue {}", healthQueueName);
		} catch (Exception e) {
			LOG.error("Error while publishEventAction ", e);
		} finally {
			channel.close();
		}
	}

	public void releaseMqConnetion() {
		try {
			LOG.info(" In releaseMqConnetion ");
			for (Map.Entry<String, Channel> entry : mqMappingMap.entrySet()) {
				Channel channelCls = entry.getValue();
				if (channelCls != null && connection != null) {
					channelCls.close();
				}
			}
		} catch (IOException | TimeoutException e) {
			LOG.error(e.getMessage());
		}
	}

	public void purgeQueue(String queueName) {
		try {
			channel = connection.createChannel();
			channel.queuePurge(queueName);
		} catch (Exception e) {
			LOG.error("Error while purgeQueue ", e);
		}
	}

}