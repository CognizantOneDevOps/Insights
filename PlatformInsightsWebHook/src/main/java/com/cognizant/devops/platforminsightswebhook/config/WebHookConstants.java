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

public final class WebHookConstants {

	public static final String WEBHOOK_PROPERTY_FILE_NAME = "webhook_subscriber.properties";
	public static final String REQUEST_PARAM_KEY_WEBHOOKNAME = "webHookName";
	public static final String EXCHANGE_TYPE = "topic";
	public static final String MQ_CHANNEL_PREFIX = "IPW_";
	public static final String WEBHOOK_EVENTDATA = "WEBHOOK_EVENTDATA";
	public static final String TIMEZONE = "GMT";
	public static final String WEBHOOK_SUBSCRIBER_HEALTH_QUEUE = "WEBHOOKSUBSCRIBER_HEALTH";
	public static final String WEBHOOK_SUBSCRIBER_HEALTH_ROUTING_KEY = WebHookConstants.WEBHOOK_SUBSCRIBER_HEALTH_QUEUE.replace("_", ".");
	public static final String STATUS = "status";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	public static final String MESSAGE = "message";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String RECOVER_EXCHANGE_NAME ="iRecover";
	public static final String RECOVER_EXCHANGE_TYPE ="fanout";
	public static final String RECOVER_QUEUE="INSIGHTS_RECOVER_QUEUE";
	public static final String RECOVER_ROUNTINGKEY_QUEUE="INSIGHTS.RECOVER.QUEUE";
	public static final String RECOVER_EXCHANGE_PROPERTY= "x-dead-letter-exchange";
	public static final String FIFO_EXTENSION = ".fifo";
}
