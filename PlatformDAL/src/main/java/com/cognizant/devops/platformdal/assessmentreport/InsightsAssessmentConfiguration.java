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
package com.cognizant.devops.platformdal.assessmentreport;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;

@Entity
@Table(name = "\"INSIGHTS_ASSESSMENT_CONFIGURATION\"")
public class InsightsAssessmentConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2113999776672790672L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "configId", unique = true, nullable = false)
	private int id;

	@Column(name = "asseementreportname", unique = true, nullable = false)
	private String asseementreportname;

	@Column(name = "emailList", nullable = false, length=2000)
	private String emails;

	@Column(name = "startDate")
	private long startDate;
	
	@Column(name="endDate")
	private long endDate;
	
	@Column(name="inputDatasource")
	private String inputDatasource;	

	@Column(name = "isActive")
	private boolean isActive;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "reportId", referencedColumnName = "reportId")
	private InsightsAssessmentReportTemplate reportTemplateEntity ;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "workflowId", referencedColumnName = "workflowId")
	private InsightsWorkflowConfiguration workflowConfig ;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAsseementreportname() {
		return asseementreportname;
	}

	public void setAsseementreportname(String asseementreportname) {
		this.asseementreportname = asseementreportname;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public String getInputDatasource() {
		return inputDatasource;
	}

	public void setInputDatasource(String inputDatasource) {
		this.inputDatasource = inputDatasource;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public InsightsAssessmentReportTemplate getReportTemplateEntity() {
		return reportTemplateEntity;
	}

	public void setReportTemplateEntity(InsightsAssessmentReportTemplate reportTemplateEntity) {
		this.reportTemplateEntity = reportTemplateEntity;
	}

	public InsightsWorkflowConfiguration getWorkflowConfig() {
		return workflowConfig;
	}

	public void setWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		this.workflowConfig = workflowConfig;
	}

	
	

}
