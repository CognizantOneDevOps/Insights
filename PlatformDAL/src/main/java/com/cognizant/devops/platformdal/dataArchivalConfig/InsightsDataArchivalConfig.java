/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.dataArchivalConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "\"INSIGHTS_DATA_ARCHIVAL_CONFIGURATION\"")
public class InsightsDataArchivalConfig {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name = "ARCHIVAL_NAME", unique = true, nullable = false)
	private String archivalName;
	
	@Column(name = "SOURCE_URL", length = 500)
	private String sourceUrl;
	
	@Column(name = "START_DATE", nullable = false)
	private Long startDate;
	
	@Column(name = "END_DATE", nullable = false)
	private Long endDate;
	
	@Column(name = "AUTHOR", nullable = false)
	private String author;
	
	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "EXPIRY_DATE")
	private Long expiryDate;
	
	@Column(name = "DAYS_TO_RETAIN")
	private int daysToRetain;

	@Column(name = "CREATED_ON")
	private Long createdOn;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getArchivalName() {
		return archivalName;
	}

	public void setArchivalName(String archivalName) {
		this.archivalName = archivalName;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Long getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Long expiryDate) {
		this.expiryDate = expiryDate;
	}

	public int getDaysToRetain() {
		return daysToRetain;
	}

	public void setDaysToRetain(int daysToRetain) {
		this.daysToRetain = daysToRetain;
	}
	
	public Long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Long createdOn) {
		this.createdOn = createdOn;
	}

}
