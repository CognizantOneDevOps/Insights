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
package com.cognizant.devops.engines.util;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.MessageQueueDataModel;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EngineUtils {
	private static Logger log = LogManager.getLogger(EngineUtils.class);

	private EngineUtils() {
	}

	public static JsonObject mergeTwoJson(JsonObject json1Obj, JsonObject json2Obj) {

		Set<Entry<String, JsonElement>> entrySet1 = json1Obj.entrySet();
		for (Entry<String, JsonElement> entry : entrySet1) {
			String key1 = entry.getKey();
			if (json2Obj.get(key1) != null) {
				JsonElement tempEle2 = json2Obj.get(key1);
				JsonElement tempEle1 = entry.getValue();
				if (tempEle2.isJsonObject() && tempEle1.isJsonObject()) {
					JsonObject mergedObj = mergeTwoJson(tempEle1.getAsJsonObject(), tempEle2.getAsJsonObject());
					entry.setValue(mergedObj);
				}
			}
		}

		Set<Entry<String, JsonElement>> entrySet2 = json2Obj.entrySet();
		for (Entry<String, JsonElement> entry : entrySet2) {
			String key2 = entry.getKey();
			if (json1Obj.get(key2) == null) {
				json1Obj.add(key2, entry.getValue());
			}
		}
		return json1Obj;
	}
	
	public static void publishMessageInMQ(String routingKey, String publishDataJson) {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			MessageQueueDataModel messageQueueConfig = ApplicationConfigProvider.getInstance().getMessageQueue();
			factory.setHost(messageQueueConfig.getHost());
			factory.setUsername(messageQueueConfig.getUser());
			factory.setPassword(messageQueueConfig.getPassword());
			try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
				String queueName = routingKey.replace(".", "_");
				channel.exchangeDeclare(MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE, true);
				channel.queueDeclare(queueName, true, false, false, null);
				channel.queueBind(queueName, MQMessageConstants.EXCHANGE_NAME, routingKey);
				channel.basicPublish(MQMessageConstants.EXCHANGE_NAME, routingKey, null, publishDataJson.getBytes());
			}
		} catch (IOException |TimeoutException e) {
			log.debug("Message not published in queue");
		}
		
	}

}