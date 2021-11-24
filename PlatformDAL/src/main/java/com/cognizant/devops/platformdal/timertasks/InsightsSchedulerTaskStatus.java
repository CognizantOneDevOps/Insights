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
package com.cognizant.devops.platformdal.timertasks;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Index;


@Entity
@Table(name="\"INSIGHTS_SCHEDULER_TASK_STATUS\"",indexes = { 
		@Index(name = "timertaskmapping_def", columnList = "timerTaskMapping"),
		@Index(name = "recordtimestamp_sort", columnList = "recordtimestamp" )
		})
public class InsightsSchedulerTaskStatus implements Serializable {

	private static final long serialVersionUID = 1628503440L;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)     
    @Column(name="taskStatusId")
    private int taskStatusId;
    
    @Column(name="recordtimestamp")
    private Long recordtimestamp;
    
    @Column(name="message")
    private String message;
    
    @Column(name="status")
    private String status;
    
    @Column(name="version")
    private String version;
    
    @Column (name="timerTaskMapping", nullable = false)
    private String timerTaskMapping ;
    
    @Column(name="processingTime")
    private Long processingTime;
    
    @Column(name="recordtimestampX")
    private String recordtimestampX;
    
    public int getTaskStatusId() {
		return taskStatusId;
	}

	public void setTaskStatusId(int taskStatusId) {
		this.taskStatusId = taskStatusId;
	}

	public Long getRecordtimestamp() {
		return recordtimestamp;
	}

	public void setRecordtimestamp(Long recordtimestamp) {
		this.recordtimestamp = recordtimestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTimerTaskMapping() {
		return timerTaskMapping;
	}

	public void setTimerTaskMapping(String timerTaskMapping) {
		this.timerTaskMapping = timerTaskMapping;
	}

	public Long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(Long processingTime) {
		this.processingTime = processingTime;
	}

	public String getRecordtimestampX() {
		return recordtimestampX;
	}

	public void setRecordtimestampX(String recordtimestampX) {
		this.recordtimestampX = recordtimestampX;
	}
}
