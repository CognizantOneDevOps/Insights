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
@Table(name="\"INSIGHTS_REPORTS_KPI_CONFIGURATION\"")

public class InsightsReportsKPIConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9165076963622782743L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")	
	int id;	
	
	@Column(name = "vConfig", length = 10000)
	String vConfig;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="reportId", referencedColumnName="reportId")
	InsightsAssessmentReportTemplate reportTemplateEntity = new InsightsAssessmentReportTemplate();
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="kpiId" , referencedColumnName="kpiId")
	InsightsKPIConfig kpiConfig = new InsightsKPIConfig();
		
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public InsightsKPIConfig getKpiConfig() {
		return kpiConfig;
	}

	public void setKpiConfig(InsightsKPIConfig kpiConfig) {
		this.kpiConfig = kpiConfig;
	}

	
	public InsightsAssessmentReportTemplate getReportTemplateEntity() {
		return reportTemplateEntity;
	}

	public void setReportTemplateEntity(InsightsAssessmentReportTemplate reportTemplateEntity) {
		this.reportTemplateEntity = reportTemplateEntity;
	}

	public String getvConfig() {
		return vConfig;
	}

	public void setvConfig(String vConfig) {
		this.vConfig = vConfig;
	}

}
