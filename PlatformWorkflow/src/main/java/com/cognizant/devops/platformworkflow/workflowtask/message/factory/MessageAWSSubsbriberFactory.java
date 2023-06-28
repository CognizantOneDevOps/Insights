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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.AWSSQSProvider;
import com.cognizant.devops.platformcommons.mq.core.AwsListener;

public class MessageAWSSubsbriberFactory extends MessageFactory {

	private static final Logger log = LogManager.getLogger(MessageAWSSubsbriberFactory.class);
	SQSConnection localconnection;
	String routingKey;

	public void registerSubscriber(String routingKey, final WorkflowTaskSubscriberHandler responseHandler)
			throws InterruptedException, InsightsCustomException, JMSException {
		this.routingKey = routingKey;


		MessageListener listner = new AwsListener() {
			@Override
			public void onMessage(Message message) {
				try {
					responseHandler.onMessage(routingKey, message);
				} catch (Exception e) {
					log.error("Error : ", e);
				}
			}
		};

		localconnection = AWSSQSProvider.registerListner(routingKey, listner);
		localconnection.start();
	}

}
