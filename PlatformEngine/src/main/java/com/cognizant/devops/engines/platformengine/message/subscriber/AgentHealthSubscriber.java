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
package com.cognizant.devops.engines.platformengine.message.subscriber;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class AgentHealthSubscriber extends EngineSubscriberResponseHandler {

	private static Logger log = LogManager.getLogger(AgentHealthSubscriber.class.getName());
	
	public AgentHealthSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	public void handleDelivery(String routingKey, String message) throws Exception
			 {
		try {
			log.debug( " [] Received '{} ':' {} '",routingKey,message);
			List<String> labels = Arrays.asList(routingKey.split(MQMessageConstants.ROUTING_KEY_SEPERATOR));
			JsonElement jsonElement = JsonUtils.parseString(message);			
			
			if (jsonElement.isJsonArray()) {
				JsonArray asJsonArray = jsonElement.getAsJsonArray();
				for (JsonElement e : asJsonArray) {
					if (e.isJsonObject()) {
						EngineStatusLogger.getInstance().extractAndStoreHealthRecord(e,labels);
					}
				}				
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}
}