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
package com.cognizant.devops.platformworkflow.workflowtask.message.factory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQProvider;
import com.cognizant.devops.platformworkflow.workflowtask.core.InsightsStatusProvider;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class MessageRabbitMQSubsbriberFactory extends MessageFactory {
	
	private static final Logger log = LogManager.getLogger(MessageRabbitMQSubsbriberFactory.class);

	public void registerSubscriber(String routingKey, final WorkflowTaskSubscriberHandler responseHandler) throws IOException, InsightsCustomException {
		
		String queueName = routingKey.replace(".", "_");
		Channel channel = RabbitMQProvider.getConnection().createChannel();
		channel = RabbitMQProvider.initilizeChannel(channel,routingKey, queueName,MQMessageConstants.EXCHANGE_NAME,MQMessageConstants.EXCHANGE_TYPE);
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
					log.error("Unable to registerSubscriber for routingKey {} error ", routingKey, e);
					InsightsStatusProvider.getInstance().createInsightStatusNode(
							"In WorkflowTaskSubscriberHandler,Unable to registerSubscriber for routingKey " + routingKey
									+ " error " + e.getMessage(),
							PlatformServiceConstants.FAILURE);
				}
			}
		};
		channel.basicConsume(queueName, false, routingKey, consumer);
	}
	
	/**
	 * Unregister workflow task subscriber
	 * 
	 * @param routingKey
	 * @param responseHandler
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public void unregisterSubscriber(String routingKey, final WorkflowTaskSubscriberHandler responseHandler)
			throws IOException, TimeoutException {
		responseHandler.getChannel().basicCancel(routingKey);
		responseHandler.getChannel().close();
	}

	
}
