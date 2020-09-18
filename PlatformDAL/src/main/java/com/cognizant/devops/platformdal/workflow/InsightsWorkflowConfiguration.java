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
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;

@Entity
@Table(name = "\"INSIGHTS_WORKFLOW_CONFIG\"")
public class InsightsWorkflowConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1249381939971490215L;

	@Id
	@Column(name = "workflowId")
	private String workflowId;

	@Column(name = "workflowType")
	private String workflowType;

	@Column(name = "scheduleType")
	private String scheduleType;

	@Column(name = "reoccurence")
	private Boolean reoccurence = false;

	@Column(name = "lastRun")
	private Long lastRun = 0L;

	@Column(name = "nextRun")
	private Long nextRun = 0L;

	@Column(name = "runImmediate")
	private Boolean runImmediate = false;

	@Column(name = "status")
	private String status;

	@Column(name = "isActive")
	private Boolean isActive = false;

	@OneToOne(mappedBy = "workflowConfig", fetch = FetchType.EAGER)
	private InsightsAssessmentConfiguration assessmentConfig;

	@OneToOne(mappedBy = "workflowConfig", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private InsightsEmailTemplates emailConfig;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "workflowConfig")
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

	public Boolean isReoccurence() {
		return this.reoccurence == null ? Boolean.FALSE  : this.reoccurence;			
	}

	public void setReoccurence(Boolean reoccurence) {
		this.reoccurence = reoccurence;
	}

	public Long getLastRun() {
		return this.lastRun == null ? 0L : this.lastRun;		
	}

	public void setLastRun(Long lastRun) {
		this.lastRun = lastRun;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean isActive() {
		return this.isActive == null ? Boolean.FALSE  : this.isActive;		
	}

	public void setActive(Boolean isActive) {
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

	public Long getNextRun() {
		return this.nextRun == null ? 0L : this.nextRun;		
	}

	public void setNextRun(Long nextRun) {
		this.nextRun = nextRun;
	}

	public Boolean isRunImmediate() {
		return this.runImmediate == null ? Boolean.FALSE : this.runImmediate;			
	}

	public void setRunImmediate(Boolean runImmediate) {
		this.runImmediate = runImmediate;
	}

	public InsightsEmailTemplates getEmailConfig() {
		return emailConfig;
	}

	public void setEmailConfig(InsightsEmailTemplates emailConfig) {
		this.emailConfig = emailConfig;
	}

}
