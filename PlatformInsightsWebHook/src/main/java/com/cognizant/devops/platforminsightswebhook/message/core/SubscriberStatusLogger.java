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
package com.cognizant.devops.platforminsightswebhook.message.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platforminsightswebhook.application.AppProperties;
import com.cognizant.devops.platforminsightswebhook.application.ServerProperties;
import com.cognizant.devops.platforminsightswebhook.config.WebHookConstants;
import com.cognizant.devops.platforminsightswebhook.config.WebHookMessagePublisher;
import com.google.gson.JsonObject;

public class SubscriberStatusLogger {

	private static Logger log = LogManager.getLogger(SubscriberStatusLogger.class);
	static SubscriberStatusLogger instance = null;

	private SubscriberStatusLogger() {

	}

	public static SubscriberStatusLogger getInstance() {
		if (instance == null) {
			instance = new SubscriberStatusLogger();
		}
		return instance;
	}

	public boolean createSubsriberStatusNode(String message, String status) {
	try {
			String version = "";
			version = SubscriberStatusLogger.class.getPackage().getImplementationVersion();
			log.debug(" Subscriber version = {} port = {} contextPath ={} " + version + "  port  "
					+ ServerProperties.port + "   " + ServerProperties.contextPath);
			Map<String, String> extraParameter = new HashMap<String, String>(0);
			createComponentStatusNode(version, message, status, extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node " + e.getMessage());
		}
		return Boolean.TRUE;
	}
	
	public boolean createComponentStatusNode(String version, String message, String status,
			Map<String, String> parameter) {
		boolean response=Boolean.FALSE;
		try {
			String utcdate = getUtcTime(WebHookConstants.TIMEZONE);
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("version", version==null?"-":version);
			jsonObj.addProperty("message", message);
			jsonObj.addProperty("inSightsTime",System.currentTimeMillis());
			jsonObj.addProperty("inSightsTimeX", utcdate);
			jsonObj.addProperty("instanceName", AppProperties.instanceName);
			jsonObj.addProperty("serverPort", ServerProperties.port);
			if (ServerProperties.contextPath != null) {
				jsonObj.addProperty("contextPath", ServerProperties.contextPath);
			}
			jsonObj.addProperty(WebHookConstants.STATUS, status);
			for (Map.Entry<String,String> entry: parameter.entrySet()){
				jsonObj.addProperty(entry.getKey(), entry.getValue());
			}
			log.debug("  message " + jsonObj.toString());
			WebHookMessagePublisher.getInstance().publishHealthData(jsonObj.toString().getBytes(),
					WebHookConstants.WEBHOOK_SUBSCRIBER_HEALTH_QUEUE,
					WebHookConstants.WEBHOOK_SUBSCRIBER_HEALTH_ROUTING_KEY);
			response =Boolean.TRUE;
		} catch (Exception e) {
			log.error("Unable to create Node  createComponentStatusNode " + e);
		}
		log.debug("  response for publishHealthData " + response);
		return response;
	}

	public String getUtcTime(String timezone) {
		SimpleDateFormat dtf = new SimpleDateFormat(WebHookConstants.DATE_TIME_FORMAT);
		dtf.setTimeZone(TimeZone.getTimeZone(timezone));
		return dtf.format(new Date());
	}
}
