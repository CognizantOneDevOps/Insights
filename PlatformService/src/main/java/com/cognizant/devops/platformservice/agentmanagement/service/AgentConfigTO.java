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
package com.cognizant.devops.platformservice.agentmanagement.service;

import java.io.Serializable;
import java.util.Date;

public class AgentConfigTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7152728519255360286L;

	private int id;

	private int agentId;

	private String toolName;

	private String toolCategory;

	private String agentJson;

	private Date updatedDate;
	
	private Boolean dataUpdateSupported;
	
	private String uniqueKey;
	
	private String osVersion;
	
	private String agentVersion;
	
	private String agentStatus;
	
	private String agentKey;
	
	private boolean vault;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAgentId() {
		return agentId;
	}

	public void setAgentId(int agentId) {
		this.agentId = agentId;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public String getToolCategory() {
		return toolCategory;
	}

	public void setToolCategory(String toolCategory) {
		this.toolCategory = toolCategory;
	}

	public String getAgentJson() {
		return agentJson;
	}

	public void setAgentJson(String agentJson) {
		this.agentJson = agentJson;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Boolean isDataUpdateSupported() {
		return dataUpdateSupported;
	}

	public void setDataUpdateSupported(Boolean dataUpdateSupported) {
		this.dataUpdateSupported = dataUpdateSupported;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getAgentVersion() {
		return agentVersion;
	}

	public void setAgentVersion(String agentVersion) {
		this.agentVersion = agentVersion;
	}

	public String getAgentStatus() {
		return agentStatus;
	}

	public void setAgentStatus(String agentStatus) {
		this.agentStatus = agentStatus;
	}

	public String getAgentKey() {
		return agentKey;
	}

	public void setAgentKey(String agentKey) {
		this.agentKey = agentKey;
	}

	public boolean isVault() {
		return vault;
	}

	public void setVault(boolean vault) {
		this.vault = vault;
	}
	
	
}
