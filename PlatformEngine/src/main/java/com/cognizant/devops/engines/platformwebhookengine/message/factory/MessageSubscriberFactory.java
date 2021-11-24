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
package com.cognizant.devops.engines.platformwebhookengine.message.factory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

@Deprecated
public class MessageSubscriberFactory {
	private static final Logger log = LogManager.getLogger(MessageSubscriberFactory.class);
	private Connection connection;
	private static MessageSubscriberFactory instance = new MessageSubscriberFactory();

	private void initConnectionFactory() throws InsightsCustomException {
		try {
			connection = RabbitMQConnectionProvider.getConnection();
			Channel channel = connection.createChannel();
			channel.exchangeDeclare(MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE, true);
			channel.close();
		} catch (IOException e) {
			log.error("Unable to create MQ connection", e);
		} catch (TimeoutException e) {
			log.error("Unable to create MQ connection within specified time.", e);
		}
	}

	private MessageSubscriberFactory() {
		try {
			initConnectionFactory();
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
		}
	}

	public static MessageSubscriberFactory getInstance() {
		return instance;
	}

	public void registerSubscriber(String routingKey, final EngineSubscriberResponseHandler responseHandler)
			throws Exception {
		Channel channel = connection.createChannel();
		String queueName = routingKey.replace(".", "_");
		channel.queueDeclare(queueName, true, false, false, RabbitMQConnectionProvider.getQueueArguments());
		channel.queueBind(queueName, MQMessageConstants.EXCHANGE_NAME, routingKey);
		channel.basicQos(ApplicationConfigProvider.getInstance().getMessageQueue().getPrefetchCount());
		responseHandler.setChannel(channel);
		log.debug("prefetchCount {}", ApplicationConfigProvider.getInstance().getMessageQueue().getPrefetchCount());
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				responseHandler.handleDelivery(consumerTag, envelope, properties, body);
			}
		};
		channel.basicConsume(queueName, false, routingKey, consumer);
	}

	public void unregisterSubscriber(String routingKey, final EngineSubscriberResponseHandler responseHandler)
			throws IOException, TimeoutException {
		responseHandler.getChannel().basicCancel(routingKey);
		responseHandler.getChannel().close();
	}
}
