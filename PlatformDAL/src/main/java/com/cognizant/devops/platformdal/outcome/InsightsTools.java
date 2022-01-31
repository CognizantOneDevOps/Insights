/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.outcome;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "\"INSIGHTS_ROI_TOOLS\"")
public class InsightsTools implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name = "TOOL_NAME", unique = true, nullable = false)
	private String toolName;
	
	@Column(name = "CATEGORY")
	private String category;
	
	@Column(name = "AGENT_COMMUNICATION_QUEUE")
	private String agentCommunicationQueue;
	
	@Column(name = "TOOL_CONFIG_JSON", length = 10000)
	private String toolConfigJson;
	
	@Column(name = "ISACTIVE")
	private Boolean isActive = Boolean.FALSE;
	
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAgentCommunicationQueue() {
		return agentCommunicationQueue;
	}

	public void setAgentCommunicationQueue(String agentCommunicationQueue) {
		this.agentCommunicationQueue = agentCommunicationQueue;
	}
	
	public String getToolConfigJson() {
		return toolConfigJson;
	}

	public void setToolConfigJson(String toolConfigJson) {
		this.toolConfigJson = toolConfigJson;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

}
