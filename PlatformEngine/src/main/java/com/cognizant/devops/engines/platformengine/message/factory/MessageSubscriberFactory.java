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
package com.cognizant.devops.engines.platformengine.message.factory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class MessageSubscriberFactory {
	private static final Logger log = LogManager.getLogger(MessageSubscriberFactory.class);
	private static MessageSubscriberFactory instance=null ;

	private MessageSubscriberFactory() {
			
	}

	public static MessageSubscriberFactory getInstance() throws InsightsCustomException {
		if(instance != null) {
			return instance;
		}else {
			instance= new MessageSubscriberFactory();
			return instance;
		}
	}

	public void registerSubscriber(String routingKey, final EngineSubscriberResponseHandler responseHandler)
			throws IOException, InsightsCustomException {
		String queueName = routingKey.replace(".", "_");
		Channel channel = RabbitMQConnectionProvider.getConnection().createChannel();
		channel = RabbitMQConnectionProvider.initilizeChannel(channel,routingKey, queueName,MQMessageConstants.EXCHANGE_NAME,MQMessageConstants.EXCHANGE_TYPE);
		responseHandler.setChannel(channel);
		log.debug("prefetchCount {} for routingKey {} ",
				ApplicationConfigProvider.getInstance().getMessageQueue().getPrefetchCount(), routingKey);
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				try {
					responseHandler.handleDelivery(consumerTag, envelope, properties, body);
				} catch (Exception e) {
					log.error("Error : ", e);
				}
			}
		};
		channel.basicConsume(queueName, false, routingKey, consumer);
	}

	public void unregisterSubscriber(String routingKey, final EngineSubscriberResponseHandler responseHandler)
			throws IOException, InsightsCustomException {
		try {
			responseHandler.getChannel().basicCancel(routingKey);
			responseHandler.getChannel().close();
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(" Error while unregistering subscriber "+e.getMessage());
		} 
	}
}