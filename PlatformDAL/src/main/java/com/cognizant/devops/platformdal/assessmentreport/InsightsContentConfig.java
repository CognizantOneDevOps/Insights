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
package com.cognizant.devops.platformdal.assessmentreport;

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
@Table(name = "\"INSIGHTS_CONTENT_CONFIGURATION\"")
public class InsightsContentConfig implements Serializable {

	private static final long serialVersionUID = 1588331301L;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	
	@Column(name = "contentId", unique = true, nullable = false)
	private Integer contentId;

	@Column(name = "contentName")
	private String contentName;
	
	@Column(name = "category")
	private String category;

	@Column(name = "contentJson", length = 10000)
	private String contentJson;

	@Column(name = "isActive")
	private Boolean isActive=false;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "kpiId", referencedColumnName = "kpiId")
	InsightsKPIConfig kpiConfig = new InsightsKPIConfig();

	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public Integer getContentId() {		
		return contentId;
	}


	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}


	public String getContentName() {
		return contentName;
	}


	public void setContentName(String contentName) {
		this.contentName = contentName;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public String getContentJson() {
		return contentJson;
	}


	public void setContentJson(String contentJson) {
		this.contentJson = contentJson;
	}


	public Boolean isActive() {
		return isActive;
	}


	public void setActive(Boolean isActive) {
		this.isActive = isActive;
	}


	public InsightsKPIConfig getKpiConfig() {
		return kpiConfig;
	}


	public void setKpiConfig(InsightsKPIConfig kpiConfig) {
		this.kpiConfig = kpiConfig;
	}


	@Override
	public String toString() {
		return "InferenceContentConfig [id=" + id + ", contentId=" + contentId
				+ ", contentName=" + contentName + ", contentJson=" + contentJson + ", category =" + category
				+ ", isActive=" + isActive + ", kpiConfig=" + kpiConfig + "]";
	}

}
