/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.mq.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.MessageQueueDataModel;
import com.cognizant.devops.platformcommons.constants.CommonsAndDALConstants;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;

public class RabbitMQConnectionProvider {

	private static final Logger log = LogManager.getLogger(RabbitMQConnectionProvider.class);
	private static Connection connection = null;

	public static Connection getConnection() throws InsightsCustomException {
		try {
			if(connection != null) {
				return connection;
			}
			MessageQueueDataModel messageQueueConfig = ApplicationConfigProvider.getInstance().getMessageQueue();
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(messageQueueConfig.getHost());
			factory.setUsername(messageQueueConfig.getUser());
			factory.setPassword(messageQueueConfig.getPassword());
			factory.setPort(messageQueueConfig.getPort());
			connection=factory.newConnection();
			if(ApplicationConfigProvider.getInstance().getMessageQueue().isEnableDeadLetterExchange()) {
				declareDeadLetterExchange(connection);
			}
			return connection;
		} catch (IOException e) {
			log.error("Unable to create MQ connection", e);
			throw new InsightsCustomException("Unable to create MQ connection" + e);
		} catch (TimeoutException e) {
			log.error("Unable to create MQ connection within specified time.", e);
			throw new InsightsCustomException("Unable to create MQ connection within specified time." + e);
		} catch (Exception e) {
			log.error(CommonsAndDALConstants.RABBIT_MQ_EXCEPTION, e);
			throw new InsightsCustomException(CommonsAndDALConstants.RABBIT_MQ_EXCEPTION + e);
		}
	}
	private static void declareDeadLetterExchange(Connection connection) throws InsightsCustomException {
		try (Channel channel = connection.createChannel()){
			channel.exchangeDeclare(MQMessageConstants.RECOVER_EXCHANGE_NAME, MQMessageConstants.RECOVER_EXCHANGE_TYPE, true);
			channel.queueDeclare(MQMessageConstants.RECOVER_QUEUE, true, false, false, null);
			channel.queueBind(MQMessageConstants.RECOVER_QUEUE, MQMessageConstants.RECOVER_EXCHANGE_NAME, MQMessageConstants.RECOVER_ROUNTINGKEY_QUEUE);
		} catch (Exception e) {
			log.error(CommonsAndDALConstants.RABBIT_MQ_EXCEPTION, e);
			throw new InsightsCustomException(CommonsAndDALConstants.RABBIT_MQ_EXCEPTION + e);
		}
	}
	
	public static Map<String, Object> getQueueArguments() {
		Map<String, Object> args = new HashMap<>();
		if(ApplicationConfigProvider.getInstance().getMessageQueue().isEnableDeadLetterExchange()) {
			args.put(MQMessageConstants.RECOVER_EXCHANGE_PROPERTY, MQMessageConstants.RECOVER_EXCHANGE_NAME);
		}
		return args;
	}

	public static Channel initilizeChannel(Channel channel, String routingKey, String queueName, String exchangeName, String exchangeType) throws IOException, InsightsCustomException {
		channel.exchangeDeclare(exchangeName,exchangeType , true);
		channel.queueDeclare(queueName, true, false, false, RabbitMQConnectionProvider.getQueueArguments());
		channel.queueBind(queueName, exchangeName, routingKey);
		channel.basicQos(ApplicationConfigProvider.getInstance().getMessageQueue().getPrefetchCount());
		return channel;
	}
}