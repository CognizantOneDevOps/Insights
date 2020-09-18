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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="\"INSIGHTS_WORKFLOW_TASK_SEQUENCE\"")
public class InsightsWorkflowTaskSequence implements Serializable {

	private static final long serialVersionUID = -5968663754633303132L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name = "sequence")
	private Integer sequence=1;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "workflowId", referencedColumnName = "workflowId")
	private InsightsWorkflowConfiguration workflowConfig;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="taskId" , referencedColumnName="taskId")
	private InsightsWorkflowTask workflowTaskEntity ;
	
	@Column(name="nextTask")
	private Integer nextTask=-1;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public InsightsWorkflowTask getWorkflowTaskEntity() {
		return workflowTaskEntity;
	}

	public void setWorkflowTaskEntity(InsightsWorkflowTask workflowTaskEntity) {
		this.workflowTaskEntity = workflowTaskEntity;
	}

	public Integer getNextTask() {
		return nextTask;
	}

	public void setNextTask(Integer nextTask) {
		this.nextTask = nextTask;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public InsightsWorkflowConfiguration getWorkflowConfig() {
		return workflowConfig;
	}

	public void setWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		this.workflowConfig = workflowConfig;
	}

	
						
}
