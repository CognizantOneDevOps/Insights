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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

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

	public static void publishMessageInMQ(String routingKey, String publishDataJson) throws InsightsCustomException, IOException, TimeoutException {
		String queueName = routingKey.replace(".", "_");
		Channel channel = null;
		try {
			
			channel = RabbitMQConnectionProvider.getConnection().createChannel();
			channel = RabbitMQConnectionProvider.initilizeChannel(channel, routingKey, queueName, MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE);
			channel.basicPublish(MQMessageConstants.EXCHANGE_NAME, routingKey, null, publishDataJson.getBytes());
		
		} catch (IOException e) {
			log.debug("Message not published in queue", e);
		} finally {
			if(channel != null) {
				channel.close();
			}
		}
	}

}