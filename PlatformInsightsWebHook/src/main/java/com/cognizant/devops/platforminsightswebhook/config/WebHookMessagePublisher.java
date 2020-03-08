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
package com.cognizant.devops.platforminsightswebhook.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cognizant.devops.platforminsightswebhook.application.AppProperties;
import com.cognizant.devops.platforminsightswebhook.message.core.SubscriberStatusLogger;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component("webhookmessagepublisher")
public class WebHookMessagePublisher {

	private static Logger LOG = LogManager.getLogger(WebHookMessagePublisher.class);
	private ConnectionFactory factory;
	private Connection connection;
	String exchangeName;
	String routingKey;
	Map<String, Channel> mqMappingMap = new HashMap<String, Channel>(0);

	public static WebHookMessagePublisher webhookmessagepublisher;

	public static WebHookMessagePublisher getInstance() {
		if (webhookmessagepublisher == null) {
			webhookmessagepublisher = new WebHookMessagePublisher();
		}
		return webhookmessagepublisher;
	}

	private WebHookMessagePublisher() {

	}

	public void initilizeMq() throws Exception {
		LOG.debug(" In initilizeMq ======== host = {} user = {} passcode = {} exchangeName= {} ", AppProperties.mqHost,
				AppProperties.mqUser, AppProperties.mqPassword, AppProperties.mqExchangeName);
		try {
			this.exchangeName = AppProperties.mqExchangeName;
			this.routingKey = WebHookConstants.WEBHOOK_EVENTDATA;
			factory = new ConnectionFactory();
			factory.setHost(AppProperties.mqHost);
			factory.setUsername(AppProperties.mqUser);
			factory.setPassword(AppProperties.mqPassword);
			connection = factory.newConnection();
			SubscriberStatusLogger.getInstance()
					.createSubsriberStatusNode(
							" Instance Name " + AppProperties.instanceName
							+ " : Connection with Rabbit Mq for host "
							+ AppProperties.mqHost + " established successfully. ", WebHookConstants.SUCCESS);
		} catch (Exception e) {
			LOG.error("Error while initilize mq " + e);
			SubscriberStatusLogger.getInstance().createSubsriberStatusNode(
					"Platform Webhook Subscriber : problem in connection with Rabbit Mq host " + AppProperties.mqHost,
					WebHookConstants.FAILURE);
			throw e;

		}

	}

	public void publishEventAction(byte[] data, String webHookMqChannelName) throws Exception {
		LOG.debug(" Inside publishEventAction ==== " + connection);
		try {

			if (!connection.isOpen()) {
				LOG.debug(" Connection is not open " + " connection " + connection.isOpen());
				initilizeMq();
			}
			Channel channel;
			if (mqMappingMap.containsKey(webHookMqChannelName)) {
				channel = mqMappingMap.get(webHookMqChannelName);
				channel.basicPublish(exchangeName, webHookMqChannelName, null, data);
				LOG.debug(" data published in queue " + webHookMqChannelName);
			} else {
				channel = connection.createChannel();
				channel.exchangeDeclare(exchangeName, WebHookConstants.EXCHANGE_TYPE, true);
				channel.queueDeclare(webHookMqChannelName, true, false, false, null);
				channel.queueBind(webHookMqChannelName, exchangeName, webHookMqChannelName);
				channel.basicPublish(exchangeName, webHookMqChannelName, null, data);
				LOG.debug(" data published first time in queue " + webHookMqChannelName);
				mqMappingMap.put(webHookMqChannelName, channel);
			}
		} catch (Exception e) {
			LOG.error("Error while publishEventAction " + e);
			throw e;
		}
	}

	public void publishHealthData(byte[] data, String webHookHealthqueueName, String webHookHealthRoutingKey)
			throws TimeoutException, IOException {
		LOG.debug(" Inside publishHealthData ==== ");
		try {
			Channel channel;
			channel = connection.createChannel();
			channel.exchangeDeclare(exchangeName, WebHookConstants.EXCHANGE_TYPE, true);
			channel.queueDeclare(webHookHealthqueueName, true, false, false, null);
			channel.queueBind(webHookHealthqueueName, exchangeName, webHookHealthRoutingKey);
			channel.basicPublish(exchangeName, webHookHealthRoutingKey, null, data);
			LOG.debug(" Health data published first time in queue " + webHookHealthRoutingKey);
		} catch (Exception e) {
			LOG.error("Error while publishEventAction " + e);
		}
	}

	public void releaseMqConnetion() {
		try {
			LOG.info(" In releaseMqConnetion ");
			for (Map.Entry<String, Channel> entry : mqMappingMap.entrySet()) {
				Channel channel = entry.getValue();
				if (channel != null && connection != null) {
					channel.close();
				}
			}
			if (connection != null) {
				connection.close();
			}
		} catch (IOException e) {
			LOG.error(e.getMessage());
		} catch (TimeoutException e) {
			LOG.error(e.getMessage());
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	public void purgeQueue(String queueName) {
		try {
			Channel channel;
			channel = connection.createChannel();
			channel.queuePurge(queueName);
		} catch (Exception e) {
			LOG.error("Error while purgeQueue " + e);
		}
	}
}
