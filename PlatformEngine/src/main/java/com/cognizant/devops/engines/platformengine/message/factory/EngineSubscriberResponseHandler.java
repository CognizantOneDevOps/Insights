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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.AWSSQSProvider;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import jakarta.ws.rs.ProcessingException;

public abstract class EngineSubscriberResponseHandler {
	private static final Logger log = LogManager.getLogger(EngineSubscriberResponseHandler.class);
	private Channel channel;
	private EngineSubscriberResponseHandler responseHandler;

	public EngineSubscriberResponseHandler(String routingKey)
			throws InterruptedException, JMSException, IOException, InsightsCustomException {
		responseHandler = this;

		MessageFactory msgFactory;
		String providerName = ApplicationConfigProvider.getInstance().getMessageQueue().getProviderName();
		if (providerName.equalsIgnoreCase("AWSSQS"))
			msgFactory = new MessageAWSSubsbriberFactory();
		else
			msgFactory = new MessageRabbitMQSubsbriberFactory();
		msgFactory.registerSubscriber(routingKey, responseHandler);
	}

	public EngineSubscriberResponseHandler() {

	}

	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties props, byte[] data)
			throws IOException {
		try {
			String message = new String(data, MQMessageConstants.MESSAGE_ENCODING);
			String routingKey = envelope.getRoutingKey();
			handleDelivery(routingKey, message);
			getChannel().basicAck(envelope.getDeliveryTag(), false);
		} catch (ProcessingException e) {
			getChannel().basicNack(envelope.getDeliveryTag(), false, true);
		} catch (Exception e) {
			getChannel().basicReject(envelope.getDeliveryTag(), false);
		}
	}

	public void onMessage(String routingKey, Message message) throws JMSException, InsightsCustomException {
		String msgBody = ((TextMessage) message).getText();
		try {
			log.debug("Received: {} ", msgBody);
			handleDelivery(routingKey, msgBody);
			message.acknowledge();
		} catch (ProcessingException | JMSException e) {
			log.error(e);
		} catch (Exception e) {
			log.error(e);
			if (ApplicationConfigProvider.getInstance().getMessageQueue().isEnableDeadLetterExchange()) {
				AWSSQSProvider.publishInDLQ(routingKey, msgBody);
				message.acknowledge();
			}
		}
	}

	public abstract void handleDelivery(String routingKey, String messageBody) throws Exception;

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void unregisterSubscriber(String routingKey) throws IOException, TimeoutException, InsightsCustomException {
		MessageSubscriberFactory.getInstance().unregisterSubscriber(routingKey, responseHandler);
	}
}
