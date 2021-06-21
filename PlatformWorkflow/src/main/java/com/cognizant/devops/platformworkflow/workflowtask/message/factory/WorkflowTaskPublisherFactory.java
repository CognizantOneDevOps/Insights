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
package com.cognizant.devops.platformworkflow.workflowtask.message.factory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.cognizant.devops.platformworkflow.workflowtask.utils.MQMessageConstants;
import com.rabbitmq.client.Channel;

public class WorkflowTaskPublisherFactory {
	private static Logger LOG = LogManager.getLogger(WorkflowTaskPublisherFactory.class);

	private WorkflowTaskPublisherFactory() {
		super();
	}

	/**
	 * used to publish message based on request json and routing key
	 * 
	 * @param routingKey
	 * @param data
	 * @throws InsightsCustomException 
	 * @throws Exception
	 */
	public static void publish(String routingKey, String data) throws InsightsCustomException {
		String queueName = routingKey.replace(".", "_");
		try (Channel channel = RabbitMQConnectionProvider.getChannel(routingKey, queueName, MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE)) {
			channel.basicPublish(MQMessageConstants.EXCHANGE_NAME, routingKey, null, data.getBytes());
			LOG.debug("Worlflow Detail ==== data published time in queue {}", routingKey);
		} catch (IOException | TimeoutException e) {
			LOG.debug("data not publish in queue");
		}
	}
}
