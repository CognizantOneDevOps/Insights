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

import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class MessagePublisherFactory {
	private MessagePublisherFactory() {

	}

	public static void publish(String routingKey, String data) throws IOException, TimeoutException, InsightsCustomException {
		String queueName = routingKey.replace(".", "_");
		Channel channel = null;
		try (Connection connection = RabbitMQConnectionProvider.getConnection();) {
			channel = RabbitMQConnectionProvider.getConnection().createChannel();
			channel = RabbitMQConnectionProvider.initilizeChannel(channel, routingKey, queueName, MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE);
   		    String message = new GsonBuilder().disableHtmlEscaping().create().toJson(data);
			channel.basicPublish(MQMessageConstants.EXCHANGE_NAME, routingKey, null, message.getBytes());
		
		} finally {
			if(channel != null) {
				channel.close();
			}
		}
	}
}
