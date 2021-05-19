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
package com.cognizant.devops.platformdal.grafana.pdf;

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
@Table(name = "\"INSIGHTS_GRAFANA_DASHBOARD_PDF_CONFIG\"")
public class GrafanaDashboardPdfConfig {
	
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "TITLE")
	private String title;

	@Column(name = "PDFTYPE")
	private String pdfType;
	
	@Column(name = "EMAIL")
	private String email;

	@Column(name = "VARIABLES", length = 10000)
	private String variables;
	
	@Column(name = "DASHBOARD_JSON", length = 50000)
	private String dashboardJson;
	
	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "SCHEDULE_TYPE")
	private String scheduleType;
	
	@Column(name = "EMAIL_BODY", length = 1000)
	private String emailbody;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "workflowId", referencedColumnName = "workflowId")
	private InsightsWorkflowConfiguration workflowConfig;
	
	@Column(name = "CREATED_DATE")
	private Long createdDate;

	@Column(name = "UPDATED_DATE")
	private Long updatedDate = 0L;
	
	@Column(name = "SOURCE")
	private String source;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPdfType() {
		return pdfType;
	}

	public void setPdfType(String pdfType) {
		this.pdfType = pdfType;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getVariables() {
		return variables;
	}

	public void setVariables(String variables) {
		this.variables = variables;
	}

	public String getDashboardJson() {
		return dashboardJson;
	}

	public void setDashboardJson(String dashboardJson) {
		this.dashboardJson = dashboardJson;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getEmailbody() {
		return emailbody;
	}

	public void setEmailbody(String emailbody) {
		this.emailbody = emailbody;
	}

	public InsightsWorkflowConfiguration getWorkflowConfig() {
		return workflowConfig;
	}

	public void setWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		this.workflowConfig = workflowConfig;
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
	

}
