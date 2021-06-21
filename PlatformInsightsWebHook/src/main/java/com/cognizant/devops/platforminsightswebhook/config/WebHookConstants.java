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

public interface WebHookConstants {

	String WEBHOOK_PROPERTY_FILE_NAME = "webhook_subscriber.properties";
	String REQUEST_PARAM_KEY_WEBHOOKNAME = "webHookName";
	String EXCHANGE_TYPE = "topic";
	String MQ_CHANNEL_PREFIX = "IPW_";
	String WEBHOOK_EVENTDATA = "WEBHOOK_EVENTDATA";
	String TIMEZONE = "GMT";
	String WEBHOOK_SUBSCRIBER_HEALTH_QUEUE = "WEBHOOKSUBSCRIBER_HEALTH";
	String WEBHOOK_SUBSCRIBER_HEALTH_ROUTING_KEY = WebHookConstants.WEBHOOK_SUBSCRIBER_HEALTH_QUEUE.replace("_", ".");
	String STATUS = "status";
	String SUCCESS = "success";
	String FAILURE = "failure";
	String MESSAGE = "message";
	String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	String RECOVER_EXCHANGE_NAME ="iRecover";
	String RECOVER_EXCHANGE_TYPE ="fanout";
	String RECOVER_QUEUE="INSIGHTS_RECOVER_QUEUE";
	String RECOVER_ROUNTINGKEY_QUEUE="INSIGHTS.RECOVER.QUEUE";
	String RECOVER_EXCHANGE_PROPERTY= "x-dead-letter-exchange";
}
