/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platforminsightswebhook.config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Component;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component("webhookmessagepublisher")
public class WebHookMessagePublisher {

	private ConnectionFactory factory;
	private Channel channel;
	private Connection connection;
	String exchangeName;
	String routingKey;


	public WebHookMessagePublisher() {
		this.exchangeName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentExchange(); //"iSight";
		this.routingKey = "WEBHOOK_EVENTDATA";//ApplicationConfigProvider.getInstance().getMessageQueue().
	}

	public void publishEventAction(byte[] data) throws TimeoutException, IOException {
		if (channel != null) {
			channel.basicPublish(exchangeName, routingKey, null, data);
		}
	}

	public void initilizeMq() throws TimeoutException {

		try {
			//String hostPath = env.getProperty("mq.host");
			//System.out.println("  hostPath  " + hostPath);

			factory = new ConnectionFactory();
			factory.setHost(ApplicationConfigProvider.getInstance().getMessageQueue().getHost());//"localhost"
			factory.setUsername(ApplicationConfigProvider.getInstance().getMessageQueue().getUser());//"iSight"
			factory.setPassword(ApplicationConfigProvider.getInstance().getMessageQueue().getPassword());//"iSight"
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(exchangeName, "topic", true);
			channel.queueDeclare(routingKey, true, false, false, null);
			channel.queueBind(routingKey, exchangeName, routingKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void releaseMqConnetion() {
		try {
			if (channel != null && connection != null) {
				channel.close();
				connection.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
