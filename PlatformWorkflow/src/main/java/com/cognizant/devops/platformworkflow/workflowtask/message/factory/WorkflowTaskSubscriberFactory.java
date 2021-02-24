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

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.MessageQueueDataModel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class WorkflowTaskSubscriberFactory {
	private static final Logger log = LogManager.getLogger(WorkflowTaskSubscriberFactory.class);
	private Connection connection = null;
	private static WorkflowTaskSubscriberFactory instance = new WorkflowTaskSubscriberFactory();

	/**
	 * Initilize Rabbitmq subscriber connetion factory
	 */
	private void initConnectionFactory() {
		MessageQueueDataModel messageQueueConfig = ApplicationConfigProvider.getInstance().getMessageQueue();
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(messageQueueConfig.getHost());
			factory.setUsername(messageQueueConfig.getUser());
			factory.setPassword(messageQueueConfig.getPassword());
			connection = factory.newConnection();
		} catch (IOException e) {
			log.error("Unable to create MQ connection", e);

		} catch (TimeoutException e) {
			log.error("Unable to create MQ connection within specified time.", e);
		}
	}

	private WorkflowTaskSubscriberFactory() {

	}

	public static WorkflowTaskSubscriberFactory getInstance() {
		if (instance.connection == null) {
			instance.initConnectionFactory();
		}
		return instance;
	}

	public Connection getConnection() {
		return instance.connection;
	}
}
