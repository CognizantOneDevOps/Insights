package com.cognizant.devops.platformdal.webhookConfig;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "WEBHOOK_CONFIGURATION")
public class WebHookConfig {
	
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "SUBSCRIBE_STATUS")
	private Boolean subscribeStatus;
	
	@Column(name = "TOOL_NAME")
	private String toolName;
	
	@Column(name = "EVENT_TO_SUBSCRIBE")
	private String eventName;
	
	@Column(name = "DATA_FORMAT")
	private String dataFormat;

	@Column(name = "MQ_CHANNEL")
	private String mqChannel;
			
	@Column(name = "WEBHOOK_NAME", unique = true, nullable = false)
	private String webhookName;
	
	
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
