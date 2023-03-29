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
package com.cognizant.devops.platformdal.milestone;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;

@Entity
@Table(name = "\"INSIGHTS_MILESTONE_CONFIG\"")
public class MileStoneConfig {

	@Id
	@Column(name = "mileStoneId", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "MILESTONE_NAME")
	private String mileStoneName;
	
	@Column(name = "START_DATE")
	private Long startDate;
	
	@Column(name = "END_DATE")
	private Long endDate;
	
	@Column(name = "STATUS")
	private String status;
	
	@OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER, mappedBy = "mileStoneConfig")
	private Set<InsightsMileStoneOutcomeConfig> listOfOutcomes = new HashSet<>();
	
	@Column(name = "createdDate")
	private Long createdDate;

	@Column(name = "updatedDate")
	private Long updatedDate = 0L;
	
	@Column(name = "MILESTONE_RELEASE_ID")
	private String milestoneReleaseID;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMileStoneName() {
		return mileStoneName;
	}

	public void setMileStoneName(String mileStoneName) {
		this.mileStoneName = mileStoneName;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Set<InsightsMileStoneOutcomeConfig> getListOfOutcomes() {
		return listOfOutcomes;
	}

	public void setListOfOutcomes(Set<InsightsMileStoneOutcomeConfig> listOfOutcomes) {
		this.listOfOutcomes = listOfOutcomes;
	}

	public Long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Long createdDate) {
		this.createdDate = createdDate;
	}

	public Long getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Long updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getMilestoneReleaseID() {
		return milestoneReleaseID;
	}

	public void setMilestoneReleaseID(String milestoneReleaseID) {
		this.milestoneReleaseID = milestoneReleaseID;
	}


}
