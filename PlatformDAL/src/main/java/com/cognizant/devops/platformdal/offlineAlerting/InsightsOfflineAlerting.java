/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.offlineAlerting;

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
@Table(name = "\"INSIGHTS_OFFLINE_ALERTING_CONFIG\"")
public class InsightsOfflineAlerting {

	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "alertName", unique = true, nullable = false)
	private String alertName;

	@Column(name = "frequency")
	private int frequency;

	@Column(name = "threshold")
	private double threshold;

	@Column(name = "scheduleType")
	private String scheduleType;

	@Column(name = "trend")
	private String trend;

	@Column(name = "breachedCount")
	private int breachedCount;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "retryCount")
	private int retryCount;

	@Column(name = "ALERT_JSON", length = 50000)
	private String alertJson;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "workflowId", referencedColumnName = "workflowId")
	private InsightsWorkflowConfiguration workflowConfig;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAlertName() {
		return alertName;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getTrend() {
		return trend;
	}

	public void setTrend(String trend) {
		this.trend = trend;
	}

	public String getAlertJson() {
		return alertJson;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public void setAlertJson(String alertJson) {
		this.alertJson = alertJson;
	}

	public InsightsWorkflowConfiguration getWorkflowConfig() {
		return workflowConfig;
	}

	public void setWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		this.workflowConfig = workflowConfig;
	}

	public int getBreachedCount() {
		return breachedCount;
	}

	public void setBreachedCount(int breachedCount) {
		this.breachedCount = breachedCount;
	}

}
