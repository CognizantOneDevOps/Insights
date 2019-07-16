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

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component("webhookmessagepublisher")
public class WebHookMessagePublisher {

	private static Logger LOG = LogManager.getLogger(WebHookMessagePublisher.class);
	private ConnectionFactory factory;
	private Channel channel;
	private Connection connection;
	String exchangeName;
	String routingKey;
	Map<String, String> mqMappingMap = new HashMap<String, String>(0);


	public WebHookMessagePublisher() {
		this.exchangeName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentExchange();
		this.routingKey = "WEBHOOK_EVENTDATA";
	}

	public void initilizeMq() {
		try {
			factory = new ConnectionFactory();
			factory.setHost(ApplicationConfigProvider.getInstance().getMessageQueue().getHost());
			factory.setUsername(ApplicationConfigProvider.getInstance().getMessageQueue().getUser());
			factory.setPassword(ApplicationConfigProvider.getInstance().getMessageQueue().getPassword());
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(exchangeName, WebHookConstants.EXCHANGE_TYPE, true);
		} catch (Exception e) {
			LOG.error("Error while initilize mq " + e.getMessage());
		}

	}

	public void publishEventAction(byte[] data, String webHookName) throws TimeoutException, IOException {
		/*if (channel == null) {
			initilizeMq();
		}*/
		if (channel != null) {
			channel.queueDeclare(webHookName, true, false, false, null);
			channel.queueBind(webHookName, exchangeName, webHookName);
			channel.basicPublish(exchangeName, webHookName, null, data);
		}
	}

	public void releaseMqConnetion() {
		try {
			if (channel != null && connection != null) {
				channel.close();
				connection.close();
			}
		} catch (IOException e) {
			LOG.error(e.getMessage());
		} catch (TimeoutException e) {
			LOG.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}
