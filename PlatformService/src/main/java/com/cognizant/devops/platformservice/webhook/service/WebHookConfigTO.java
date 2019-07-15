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
package com.cognizant.devops.platformservice.webhook.service;

import java.io.Serializable;

public class WebHookConfigTO implements Serializable  {
	


private static final long serialVersionUID = 7152728519255360286L;

private int id;


private String toolName;

private String webhookName;

private String mqChannel;

private Boolean subscribeStatus;

private String eventName;

private String dataFormat;

private String responseTemplate;

public int getId() {
	return id;
}

public void setId(int id) {
	this.id = id;
}

public Boolean getSubscribeStatus() {
	return subscribeStatus;
}

public void setSubscribeStatus(Boolean subscribestatus) {
	this.subscribeStatus = subscribestatus;
}


public String getToolName() {
	return toolName;
}

public void setToolName(String toolName) {
	this.toolName = toolName;
}


public String getEventName() {
	return eventName;
}

public void setEventName(String eventname) {
	this.eventName = eventname;
}



public String getMQChannel() {
	return mqChannel;
}

public void setMQChannel(String mqchannel) {
	this.mqChannel = mqchannel;
}

public String getDataFormat() {
	return dataFormat;
}

public void setDataFormat(String dataformat) {
	this.dataFormat = dataformat;
}

public String getWebHookName() {
	return webhookName;
}

public void setWebHookName(String webhookName) {
	this.webhookName = webhookName;
}
public String getResponseTemplate() {
	return responseTemplate;
}

public void setResponseTemplate(String responseTemplate) {
	this.responseTemplate = responseTemplate;
}



}