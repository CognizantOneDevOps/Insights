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
package com.cognizant.devops.platformdal.webhookConfig;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "WEBHOOK_CONFIGURATION")
public class WebHookConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6788438109848406935L;

	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "SUBSCRIBE_STATUS")
	private Boolean subscribeStatus;

	@Column(name = "RESPONSE_TEMPLATE", length = 5000)
	private String responseTemplate;

	@Column(name = "DYNAMIC_TEMPLATE", length = 5000)
	private String dynamicTemplate;

	@Column(name = "TOOL_NAME")
	private String toolName;

	@Column(name = "LABEL_NAME")
	private String labelDisplay;

	@Column(name = "DATA_FORMAT")
	private String dataFormat;

	@Column(name = "IS_UPDATE_REQUIRED")
	private Boolean isUpdateRequired = false;

	@Column(name = "FIELD_USED_FOR_UPDATE")
	private String fieldUsedForUpdate;

	@Column(name = "MQ_CHANNEL", nullable = false)
	private String mqChannel;

	@Column(name = "WEBHOOK_NAME", unique = true, nullable = false)
	private String webhookName;

	@Column(name = "IS_EVENT_PROCESSING")
	private Boolean isEventProcessing = false;

	@Column(name = "EVENT_CONFIG_JSON", length = 10000)
	private String eventConfigJson;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "WEBHOOK_CONFIGID")
	private Set<WebhookDerivedConfig> derivedOperations = new HashSet<>(0);

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

	public String getLabelName() {
		return labelDisplay;
	}

	public void setLabelName(String labelDisplay) {
		this.labelDisplay = labelDisplay;
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

	public String getDynamicTemplate() {
		return dynamicTemplate;
	}

	public void setDynamicTemplate(String dynamicTemplate) {
		this.dynamicTemplate = dynamicTemplate;
	}

	public Boolean getIsUpdateRequired() {
		return isUpdateRequired;
	}

	public void setIsUpdateRequired(Boolean isUpdateRequired) {
		this.isUpdateRequired = isUpdateRequired;
	}

	public String getFieldUsedForUpdate() {
		return fieldUsedForUpdate;
	}

	public void setFieldUsedForUpdate(String fieldUsedForUpdate) {
		this.fieldUsedForUpdate = fieldUsedForUpdate;
	}

	public Set<WebhookDerivedConfig> getWebhookDerivedConfig() {
		return derivedOperations;
	}

	public void setWebhookDerivedConfig(Set<WebhookDerivedConfig> derivedOperations) {
		this.derivedOperations = derivedOperations;
	}

	public Boolean isEventProcessing() {
		return this.isEventProcessing == null ? false : this.isEventProcessing;
	}

	public void setEventProcessing(Boolean isEventProcessing) {
		this.isEventProcessing = isEventProcessing;
	}

	public String getEventConfigJson() {
		return eventConfigJson;
	}

	public void setEventConfigJson(String eventConfigJson) {
		this.eventConfigJson = eventConfigJson;
	}

	@Override
	public String toString() {
		return "WebHookConfig [id=" + id + ", subscribeStatus=" + subscribeStatus + ", responseTemplate="
				+ responseTemplate + ",isUpdateRequired=" + isUpdateRequired + ",fieldUsedForUpdate="
				+ fieldUsedForUpdate + ",dynamicTemplate=" + dynamicTemplate + " toolName=" + toolName
				+ ", labelDisplay=" + labelDisplay + ", dataFormat=" + dataFormat + ", mqChannel=" + mqChannel
				+ ", webhookName=" + webhookName + "]";
	}
}
