/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformwebhookengine.message.subscriber;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.healthutil.HealthUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class WebhookHealthSubscriber extends EngineSubscriberResponseHandler {
	private static Logger log = LogManager.getLogger(WebhookHealthSubscriber.class);
	private String jobName = ""; 
	HealthUtil healthUtil = new HealthUtil();
	
	public WebhookHealthSubscriber(String routingKey, String jobName) throws Exception {
		super(routingKey);
		this.jobName=jobName;
	}

	@Override
	public void handleDelivery(String routingKey, String message) throws InsightsCustomException {
		log.debug("  Received  {} : {}", routingKey, message);
		List<JsonObject> dataList = new ArrayList<>();
		JsonElement json = JsonUtils.parseString(message);
		if (json.isJsonObject()) {
			log.debug("This is normal json object for webhook health ");
			dataList.add(json.getAsJsonObject());
		}				
		JsonObject messageObj = json.getAsJsonObject();
		String componentName = ServiceStatusConstants.PLATFORM_WEBHOOK_SUBSCRIBER;
		String status = JsonUtils.getValueFromJson(messageObj, "status");
		String healthMessage = JsonUtils.getValueFromJson(messageObj , "message");
		String version = JsonUtils.getValueFromJson(messageObj , "version");
		
		try {		
			log.debug("Type=WebhookHealth {} routingKey={} status={} serverPort={}",message,routingKey,status,messageObj.get("serverPort").getAsString());
			if (!dataList.isEmpty()) {				
				healthUtil.createComponentHealthDetails(componentName,version,healthMessage,status);				
				log.debug("Insights WebhookHealthSubscriber Health Record updated");
			} else {
				log.error(" Data List is empty for webhook health record ");
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						"Data List is empty for webhook health record: " + routingKey,
						PlatformServiceConstants.FAILURE,jobName);
			}
		} catch (InsightsCustomException e) {
			log.error("Type=WebhookHealth routingKey={} message={}  status={} serverPort={} error={}"
					,routingKey,message,status,messageObj.get("serverPort").getAsString(),e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	
}