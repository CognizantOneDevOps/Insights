package com.cognizant.devops.platformservice.webhook.service;

import java.io.Serializable;
import java.util.Date;

public class WebHookConfigTO implements Serializable  {
	


private static final long serialVersionUID = 7152728519255360286L;

private int id;


private String toolName;

private String webhookName;

private String mqChannel;

private Boolean subscribeStatus;

private String eventName;

private String dataFormat;

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

}