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
package com.cognizant.devops.platformdal.offlineDataProcessing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This Entity is used to store Offline Data Configuration
 */
@Entity
@Table(name = "\"INSIGHTS_OFFLINE_DATA_CONFIGURATION\"")
public class InsightsOfflineConfig {

	 private static final long serialVersionUID = 1410588331275L;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "queryName", unique = true, nullable = false)
	private String queryName;

	@Column(name = "toolName")
	private String toolName;

	@Column(name = "lastRunTime")
	private Long lastRunTime;
	
	@Column(name = "queryProcessingTime")
	private Long queryProcessingTime;

	@Column(name = "recordsProcessed")
	private int recordsProcessed;
	
	@Column(name = "retryCount")
	private int retryCount;

	@Column(name = "isActive")
	private Boolean isActive = false;

	@Column(name = "cypherQuery", length = 8000)
	private String cypherQuery;
	
	@Column(name = "cronSchedule")
	private String cronSchedule;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "message", length = 3000)
	private String message;
	
	@Column(name = "queryGroup")
	private String queryGroup;

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

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public Long getLastRunTime() {
		return lastRunTime;
	}

	public void setLastRunTime(Long lastRunTime) {
		this.lastRunTime = lastRunTime;
	}

	public Long getQueryProcessingTime() {
		return queryProcessingTime;
	}

	public void setQueryProcessingTime(Long queryProcessingTime) {
		this.queryProcessingTime = queryProcessingTime;
	}

	public int getRecordsProcessed() {
		return recordsProcessed;
	}

	public void setRecordsProcessed(int recordsProcessed) {
		this.recordsProcessed = recordsProcessed;
	}

	
	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getCypherQuery() {
		return cypherQuery;
	}

	public void setCypherQuery(String cypherQuery) {
		this.cypherQuery = cypherQuery;
	}

	public String getCronSchedule() {
		return cronSchedule;
	}

	public void setCronSchedule(String cronSchedule) {
		this.cronSchedule = cronSchedule;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getqueryGroup() {
		return queryGroup;
	}

	public void setqueryGroup(String queryGroup) {
		this.queryGroup = queryGroup;
	}
}
