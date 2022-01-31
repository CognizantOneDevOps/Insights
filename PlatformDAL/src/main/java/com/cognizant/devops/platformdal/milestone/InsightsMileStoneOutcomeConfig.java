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
package com.cognizant.devops.platformdal.milestone;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.cognizant.devops.platformdal.outcome.InsightsOutcomeTools;

@Entity
@Table(name="\"INSIGHTS_MILESTONE_OUTCOME_CONFIGURATION\"")
public class InsightsMileStoneOutcomeConfig {

	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;	
	
	@ManyToOne(fetch=FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinColumn(name="mileStoneId")
	MileStoneConfig mileStoneConfig = new MileStoneConfig();
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
	@JoinColumn(name = "outcomeName")
	InsightsOutcomeTools insightsOutcomeTools = new InsightsOutcomeTools();
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "statusMessage", length = 2000)
	private String statusMessage;
	
	@Column(name = "lastUpdatedDate")
	private Long lastUpdatedDate;

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public MileStoneConfig getMileStoneConfig() {
		return mileStoneConfig;
	}

	public void setMileStoneConfig(MileStoneConfig mileStoneConfig) {
		this.mileStoneConfig = mileStoneConfig;
	}

	public InsightsOutcomeTools getInsightsOutcomeTools() {
		return insightsOutcomeTools;
	}

	public void setInsightsOutcomeTools(InsightsOutcomeTools insightsOutcomeTools) {
		this.insightsOutcomeTools = insightsOutcomeTools;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Long lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}
	
	
	

	
}
