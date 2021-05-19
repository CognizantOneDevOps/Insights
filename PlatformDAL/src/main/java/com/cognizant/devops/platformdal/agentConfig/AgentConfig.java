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
package com.cognizant.devops.platformdal.agentConfig;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AGENT_CONFIGURATION")
public class AgentConfig {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "TOOL_NAME")
	private String toolName;

	@Column(name = "TOOL_CATEGORY")
	private String toolCategory;

	@Column(name = "LABEL_NAME")
	private String labelName;

	@Column(name = "AGENT_JSON", length = 10000)
	private String agentJson;

	@Column(name = "UPDATE_DATE")
	private Date updatedDate;

	@Column(name = "OS_VERSION")
	private String osVersion;

	@Column(name = "AGENT_VERSION")
	private String agentVersion;

	@Column(name = "AGENT_STATUS")
	private String agentStatus;

	@Column(name = "AGENT_KEY", unique = true, nullable = false)
	private String agentKey;

	@Column(name = "IS_VAULT_ENABLE")
	private Boolean vault;
	
	@Column(name = "iswebhook")
	private Boolean iswebhook = Boolean.FALSE;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
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

	public Boolean getVault() {
		if(this.vault == null) {
			return false;
		}
		return vault;
	}

	public void setVault(Boolean vault) {
		this.vault = vault;
	}

	public Boolean getIswebhook() {
		if(this.iswebhook == null) {
			return false;
		}
		return iswebhook;
	}

	public void setIswebhook(Boolean iswebhook) {
		if(iswebhook != null) {
			this.iswebhook = iswebhook;
		}else {
			this.iswebhook = Boolean.FALSE;
		}
	}
}
