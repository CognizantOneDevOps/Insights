/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformcommons.mq.core;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSSession;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.CommonsAndDALConstants;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class AWSSQSProvider {

	private static final Logger log = LogManager.getLogger(AWSSQSProvider.class);
	private static SQSConnectionFactory connectionFactory = null;


	public static SQSConnectionFactory getConnectionFactory() throws InsightsCustomException {

		try {

			if (connectionFactory != null) {
				return connectionFactory;
			}

			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
					ApplicationConfigProvider.getInstance().getMessageQueue().getAwsAccessKey(),
					ApplicationConfigProvider.getInstance().getMessageQueue().getAwsSecretKey());

			SqsClientBuilder clientBuilder = SqsClient.builder()
					.region(Region.of(ApplicationConfigProvider.getInstance().getMessageQueue().getAwsRegion()))
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds));

			connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(), clientBuilder.build());

			if(ApplicationConfigProvider.getInstance().getMessageQueue().isEnableDeadLetterExchange()) {
				declareDeadLetterExchange();
			}
			
			return connectionFactory;

		} catch (Exception ex) {
			log.error(CommonsAndDALConstants.AWS_SQS_EXCEPTION, ex);
			throw new InsightsCustomException(CommonsAndDALConstants.AWS_SQS_EXCEPTION + ex);
		}

	}

	public static SQSConnection getSQSConnectionFromFactory() throws InsightsCustomException, JMSException {

		return AWSSQSProvider.getConnectionFactory().createConnection();

	}

	public static AmazonSQSMessagingClientWrapper getSQSClient(SQSConnection connection)
			throws InsightsCustomException, JMSException {

		return connection.getWrappedAmazonSQSClient();

	}

	public static void createSQSQueue(String queueName, AmazonSQSMessagingClientWrapper sqsClientWarrper)
			throws InsightsCustomException, JMSException {

		Map<QueueAttributeName, String> queueAttributes = new HashMap<>();
		queueAttributes.put(QueueAttributeName.FIFO_QUEUE, "true");
		queueAttributes.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true");
		queueAttributes.put(QueueAttributeName.VISIBILITY_TIMEOUT, "120");
		try {
			CreateQueueRequest cqr = CreateQueueRequest.builder().queueName(queueName).attributes(queueAttributes)
					.build();
			sqsClientWarrper.createQueue(cqr);
		} catch (SqsException e) {
			log.error(e);
		}

	}

	public static void declareDeadLetterExchange() throws JMSException, InsightsCustomException {

		SQSConnection connection = AWSSQSProvider.getSQSConnectionFromFactory();
		AmazonSQSMessagingClientWrapper client = AWSSQSProvider.getSQSClient(connection);
		String queueName = MQMessageConstants.RECOVER_QUEUE +  MQMessageConstants.FIFO_EXTENSION;
		if (!client.queueExists(queueName))
			AWSSQSProvider.createSQSQueue(queueName, client);
	}

	public static void publish(String routingKey, String data) throws InsightsCustomException, JMSException {

		SQSConnection connection = AWSSQSProvider.getSQSConnectionFromFactory();
		AmazonSQSMessagingClientWrapper client = AWSSQSProvider.getSQSClient(connection);
		Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
		String queueName = routingKey.replace(".", "_") +  MQMessageConstants.FIFO_EXTENSION;
		if (!client.queueExists(queueName))
			AWSSQSProvider.createSQSQueue(queueName, client);
		
		Queue queue = session.createQueue(queueName);
		MessageProducer producer = session.createProducer(queue);
		TextMessage message = session.createTextMessage(data);
		message.setStringProperty("JMSXGroupID", routingKey);
		producer.send(message);
	}
	
	public static void publishInDLQ(String routingKey, String data) throws InsightsCustomException, JMSException {

		SQSConnection connection = AWSSQSProvider.getSQSConnectionFromFactory();
		Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
		String queueName =  MQMessageConstants.RECOVER_QUEUE +  MQMessageConstants.FIFO_EXTENSION;
		
		Queue queue = session.createQueue(queueName);
		MessageProducer producer = session.createProducer(queue);
		TextMessage dlqMessage = session.createTextMessage(data);
		dlqMessage.setStringProperty("sourceQueue", routingKey);
		dlqMessage.setStringProperty("JMSXGroupID", MQMessageConstants.RECOVER_ROUNTINGKEY_QUEUE);
		producer.send(dlqMessage);
	}

	public static SQSConnection registerListner(String routingKey, MessageListener listner)
			throws InsightsCustomException, JMSException {

		SQSConnection connectionSession = AWSSQSProvider.getSQSConnectionFromFactory();
		AmazonSQSMessagingClientWrapper sqsClientJMSWapper = AWSSQSProvider.getSQSClient(connectionSession);
		String queueName = routingKey.replace(".", "_") +  MQMessageConstants.FIFO_EXTENSION;

		try {
			GetQueueUrlResponse getQueueUrlResponse = sqsClientJMSWapper
					.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
			log.info(getQueueUrlResponse);
		} catch (Exception e) {
			AWSSQSProvider.createSQSQueue(queueName, sqsClientJMSWapper);
		}

		Session session = connectionSession.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);

		Queue queue = session.createQueue(queueName);
		MessageConsumer consumer = session.createConsumer(queue);
		consumer.setMessageListener(listner);

		return connectionSession;
	}

}
