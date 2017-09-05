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
package com.cognizant.devops.platformcommons.dal.postgres;

public class AgentConfigData {
	
	private int agentId;
	private String toolName;
	private String toolCategory;
	private String agentJson;
	private String [] agentFields;
	private String updatedDate;
	
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
	public String[] getAgentFields() {
		return agentFields;
	}
	public void setAgentFields(String[] agentFields) {
		this.agentFields = agentFields;
	}
	public String getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}
	
}
