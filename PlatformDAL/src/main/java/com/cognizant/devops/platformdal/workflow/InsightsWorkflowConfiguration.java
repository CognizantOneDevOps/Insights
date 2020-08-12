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
package com.cognizant.devops.platformdal.workflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;

@Entity
@Table(name="\"INSIGHTS_WORKFLOW_CONFIG\"")
public class InsightsWorkflowConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1249381939971490215L; 

	@Id
	@Column(name="workflowId")
	private String workflowId;
	
	@Column(name="workflowType")
	private String workflowType;
	
	@Column(name="scheduleType")
	private String scheduleType;
	
	@Column(name="reoccurence")
	private boolean reoccurence;
	
	@Column(name="lastRun")
	private long lastRun;
	
	@Column(name="nextRun")
	private long nextRun;
	
	@Column(name="status")
	private String status;
	
	@Column(name="isActive")
	private boolean isActive;		
	
	@OneToOne(mappedBy="workflowConfig" , fetch=FetchType.EAGER)
	private InsightsAssessmentConfiguration assessmentConfig;
	
	
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL , mappedBy="workflowConfig")
	private Set<InsightsWorkflowTaskSequence> taskSequenceEntity = new HashSet<>();

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public boolean isReoccurence() {
		return reoccurence;
	}

	public void setReoccurence(boolean reoccurence) {
		this.reoccurence = reoccurence;
	}

	public long getLastRun() {
		return lastRun;
	}

	public void setLastRun(long lastRun) {
		this.lastRun = lastRun;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public InsightsAssessmentConfiguration getAssessmentConfig() {
		return assessmentConfig;
	}

	public void setAssessmentConfig(InsightsAssessmentConfiguration assessmentConfig) {
		this.assessmentConfig = assessmentConfig;
	}

	public Set<InsightsWorkflowTaskSequence> getTaskSequenceEntity() {
		return taskSequenceEntity;
	}

	public void setTaskSequenceEntity(Set<InsightsWorkflowTaskSequence> taskSequenceEntity) {
		this.taskSequenceEntity = taskSequenceEntity;
	}

	public long getNextRun() {
		return nextRun;
	}

	public void setNextRun(long nextRun) {
		this.nextRun = nextRun;
	}
}
