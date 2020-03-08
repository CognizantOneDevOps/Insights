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
package com.cognizant.devops.platformdal.webhookConfig;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "\"INSIGHTS_WEBHOOK_DERIVED_CONFIG\"")
public class WebhookDerivedConfig implements Serializable {

	private static final long serialVersionUID = -9032532351820195903L;

	@Id
	@Column(name = "WBID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int wid;

	@Column(name = "WEBHOOK_NAME")
	String webhookName; 
	
	@Column(name = "OPERATION_NAME")
	String operationName;
	
	@Column(name = "OPERATION_FIELDS",length = 5000)
	String operationFields;
	
	@Column(name = "TARGET_FIELD_NAME")
	String targetName;

	
	public int getWid() {
		return wid;
	}
	
	public void setWid(int wid) {
		this.wid = wid;
	}

	
	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getOperationFields() {
		return operationFields;
	}

	public void setOperationFields(String operationFields) {
		this.operationFields = operationFields;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getWebhookName() {
		return webhookName;
	}

	public void setWebhookName(String webhookName) {
		this.webhookName = webhookName;
	}

	@Override
	public String toString() {
		return "WebhookDerivedConfig [wid=" + wid + ", webhookName=" + webhookName + ", operationName=" + operationName
				+ ", operationFields=" + operationFields + ", targetName=" + targetName + "]";
	}
}
