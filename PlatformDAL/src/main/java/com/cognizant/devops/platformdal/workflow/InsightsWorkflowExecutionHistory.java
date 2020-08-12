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
@Table(name="\"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\"")
public class InsightsWorkflowExecutionHistory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9069315770588008786L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;		
	
	@Column(name="executionId", nullable=false)
    private long executionId;
	
	@Column(name="currentTask")
	private int currenttask;
	
	@Column(name="startTime")
	private long startTime;
	
	@Column(name="endTime")
	private long endTime;
	
	@Column(name="statusLog" , length=5000)
	private String statusLog;	
	
	@Column(name="taskStatus")
	private String taskStatus;
	
	@Column(name="requestMessage")
	private String requestMessage;
	
	@Column(name="retryCount")
	private int retryCount;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="workflowId" ,referencedColumnName="workflowId")
	private InsightsWorkflowConfiguration workflowConfig ;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(long executionId) {
		this.executionId = executionId;
	}

	public int getCurrenttask() {
		return currenttask;
	}

	public void setCurrenttask(int currenttask) {
		this.currenttask = currenttask;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getStatusLog() {
		return statusLog;
	}

	public void setStatusLog(String statusLog) {
		this.statusLog = statusLog;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getRequestMessage() {
		return requestMessage;
	}

	public void setRequestMessage(String requestMessage) {
		this.requestMessage = requestMessage;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public InsightsWorkflowConfiguration getWorkflowConfig() {
		return workflowConfig;
	}

	public void setWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		this.workflowConfig = workflowConfig;
	}

		

}
