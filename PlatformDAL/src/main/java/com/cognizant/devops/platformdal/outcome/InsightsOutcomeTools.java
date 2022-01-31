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
package com.cognizant.devops.platformdal.outcome;

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
@Table(name = "\"INSIGHTS_OUTCOME_TOOLS\"")
public class InsightsOutcomeTools implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "OUTCOME_NAME")
	private String outcomeName;

	@Column(name = "OUTCOME_TYPE")
	private String outcomeType;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "TOOL_NAME", referencedColumnName = "TOOL_NAME")
	private InsightsTools insightsTools = new InsightsTools();

	@Column(name = "TOOL_CONFIG_JSON", length = 10000)
	private String configJson;

	@Column(name = "ISACTIVE")
	private Boolean isActive = Boolean.FALSE;

	@Column(name = "CREATED_DATE")
	private Long createdDate;

	@Column(name = "UPDATED_DATE")
	private Long updatedDate = 0L;

	@Column(name = "METRIC_URL", length = 1000)
	private String metricUrl;

	@Column(name = "REQUEST_PARAMETERS", length = 10000)
	private String requestParameters;

	public String getMetricUrl() {
		return metricUrl;
	}

	public void setMetricUrl(String metricUrl) {
		this.metricUrl = metricUrl;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOutcomeName() {
		return outcomeName;
	}

	public void setOutcomeName(String outcomeName) {
		this.outcomeName = outcomeName;
	}

	public String getOutcomeType() {
		return outcomeType;
	}

	public void setOutcomeType(String outcomeType) {
		this.outcomeType = outcomeType;
	}

	public InsightsTools getInsightsTools() {
		return insightsTools;
	}

	public void setInsightsTools(InsightsTools insightsTools) {
		this.insightsTools = insightsTools;
	}

	public String getConfigJson() {
		return configJson;
	}

	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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
	

	public String getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(String requestParameters) {
		this.requestParameters = requestParameters;
	}

}