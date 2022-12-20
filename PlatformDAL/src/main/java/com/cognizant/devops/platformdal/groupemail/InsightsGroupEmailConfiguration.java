/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.groupemail;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;

@Entity
@Table(name = "\"INSIGHTS_GROUP_EMAIL_CONFIGURATION\"")
public class InsightsGroupEmailConfiguration implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4328456860675036659L;

	@Id
	@Column(name = "GROUP_EMAIL_TEMPLATE_ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int groupEmailTemplateID;
	
	@Column(name = "BATCH_NAME", unique = true, nullable = false)
	private String batchName;
	
	@Column(name = "SCHEDULE_TYPE", nullable = false)
	private String schedule;
	
	@Column(name = "ISACTIVE", nullable = false)
	private boolean isActive;
	
	@Column(name = "SOURCE", nullable = false)
	private String source;
	
	@Column(name = "MAP_ID_LIST", nullable = false, length = 10000)
	private String mapIdList;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "WORKFLOWID", referencedColumnName = "workflowId")
	private InsightsWorkflowConfiguration workflowConfig;

	public int getGroupEmailTemplateID() {
		return groupEmailTemplateID;
	}

	public void setGroupEmailTemplateID(int groupEmailTemplateID) {
		this.groupEmailTemplateID = groupEmailTemplateID;
	}

	public String getBatchName() {
		return batchName;
	}

	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}


	public InsightsWorkflowConfiguration getWorkflowConfig() {
		return workflowConfig;
	}

	public void setWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		this.workflowConfig = workflowConfig;
	}

	public String getMapIdList() {
		return mapIdList;
	}

	public void setMapIdList(String mapIdList) {
		this.mapIdList = mapIdList;
	}
	
	
}
