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
package com.cognizant.devops.platforminsightswebhook.message.factory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSSession;
import com.cognizant.devops.platforminsightswebhook.application.AppProperties;
import com.cognizant.devops.platforminsightswebhook.config.WebHookConstants;
import com.cognizant.devops.platforminsightswebhook.message.core.SubscriberStatusLogger;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SqsException;
@Component("webhookmessagepublisherSQS")
public class WebhookMessagePublisherSQS extends WebhookMessageFactory{

	private static final Logger log = LogManager.getLogger(WebhookMessagePublisherSQS.class);
	private static SQSConnectionFactory connectionFactory = null;
	private static SQSConnection connection;
	Map<String, MessageProducer> producerMap = new HashMap<>(0);

	@Override
	public void initializeConnection() throws Exception {

		try {

			if (connectionFactory != null) {
				connection =  connectionFactory.createConnection();
			}

			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
					AppProperties.getAwsAccessKey(),
					AppProperties.getAwsSecretKey());

			SqsClientBuilder clientBuilder = SqsClient.builder()
					.region(Region.of(AppProperties.getAwsRegion()))
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds));

			connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(), clientBuilder.build());

			if(AppProperties.enableDeadLetterExchange) {
				declareDeadLetterExchange();
			}
			
			connection = connectionFactory.createConnection();
			
			SubscriberStatusLogger.getInstance().createSubsriberStatusNode(
					" Instance Name " + AppProperties.instanceName + " : Connection with SQS established successfully. ",
					WebHookConstants.SUCCESS);

		} catch (Exception ex) {
			log.error( "Exception while establishing SQS connection ", ex);
			SubscriberStatusLogger.getInstance().createSubsriberStatusNode(
					"Platform Webhook Subscriber : problem in establishing connection with SQS ",
					WebHookConstants.FAILURE);
			throw new Exception("Exception while establishing SQS connection " + ex);
		}

	}


	public static AmazonSQSMessagingClientWrapper getSQSClient(SQSConnection connection)
			throws  JMSException {

		return connection.getWrappedAmazonSQSClient();

	}

	public static void createSQSQueue(String queueName, AmazonSQSMessagingClientWrapper sqsClientWarrper)
			throws  JMSException {

		Map<QueueAttributeName, String> queueAttributes = new HashMap<>();
		queueAttributes.put(QueueAttributeName.FIFO_QUEUE, "true");
		queueAttributes.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true");
		try {
			CreateQueueRequest cqr = CreateQueueRequest.builder().queueName(queueName).attributes(queueAttributes)
					.build();
			sqsClientWarrper.createQueue(cqr);
		} catch (SqsException e) {
			log.error(e);
		}

	}

	public static void declareDeadLetterExchange() throws Exception {

		AmazonSQSMessagingClientWrapper client = WebhookMessagePublisherSQS.getSQSClient(connection);
		String queueName = WebHookConstants.RECOVER_QUEUE +  WebHookConstants.FIFO_EXTENSION;
		if (!client.queueExists(queueName))
			WebhookMessagePublisherSQS.createSQSQueue(queueName, client);
	}
	
	public static void publishInDLQ(String routingKey, String data) throws  Exception {

		Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
		String queueName =  WebHookConstants.RECOVER_QUEUE +  WebHookConstants.FIFO_EXTENSION;
		
		Queue queue = session.createQueue(queueName);
		MessageProducer producer = session.createProducer(queue);
		TextMessage dlqMessage = session.createTextMessage(data);
		dlqMessage.setStringProperty("sourceQueue", routingKey);
		dlqMessage.setStringProperty("JMSXGroupID", WebHookConstants.RECOVER_ROUNTINGKEY_QUEUE);
		producer.send(dlqMessage);
	}

	
	@Override
	public void publishEventAction(String data, String routingKey) throws Exception {

		AmazonSQSMessagingClientWrapper client = WebhookMessagePublisherSQS.getSQSClient(connection);
		Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
		String queueName = routingKey.replace(".", "_") +  WebHookConstants.FIFO_EXTENSION;
		if (!client.queueExists(queueName))
			WebhookMessagePublisherSQS.createSQSQueue(queueName, client);
		
		Queue queue = session.createQueue(queueName);
		MessageProducer producer = session.createProducer(queue);
		TextMessage message = session.createTextMessage(data);
		message.setStringProperty("JMSXGroupID", routingKey);
		producer.send(message);
		
	}

	@Override
	public void publishHealthData(String data, String healthQueueName)
			throws TimeoutException, IOException, JMSException {
		AmazonSQSMessagingClientWrapper client = WebhookMessagePublisherSQS.getSQSClient(connection);
		Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
		String queueName = healthQueueName +  WebHookConstants.FIFO_EXTENSION;
		if (!client.queueExists(queueName))
			WebhookMessagePublisherSQS.createSQSQueue(queueName, client);
		
		Queue queue = session.createQueue(queueName);
		MessageProducer producer = session.createProducer(queue);
		TextMessage message = session.createTextMessage(data);
		message.setStringProperty("JMSXGroupID", healthQueueName);
		producer.send(message);
		
	}
	


}
