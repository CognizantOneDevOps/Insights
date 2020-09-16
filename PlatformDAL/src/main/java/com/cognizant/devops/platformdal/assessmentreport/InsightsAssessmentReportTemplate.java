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
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;



@Entity
@Table(name = "\"INSIGHTS_ASSESSMENT_REPORT_TEMPLATE\"")
public class InsightsAssessmentReportTemplate implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;	

	@Column(name = "reportId", unique = true, nullable = false)
	private int reportId;
	
	@Column(name = "templateName", unique=true, nullable = false )
	private String templateName;

	@Column(name = "description")
	private String description;
	
	@Column(name = "isActive")
	private boolean isActive;	
	
	@Column(name = "file")
	private String file;	
	
	@Column(name = "visualizationutil")
	private String visualizationutil;
	
	@OneToMany(cascade=CascadeType.ALL ,mappedBy="reportTemplateEntity", fetch=FetchType.EAGER)
	private Set<InsightsReportsKPIConfig> reportsKPIConfig = new HashSet<>();
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getReportId() {
		return reportId;
	}

	public void setReportId(int reportId) {
		this.reportId = reportId;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getVisualizationutil() {
		return visualizationutil;
	}

	public void setVisualizationutil(String visualizationutil) {
		this.visualizationutil = visualizationutil;
	}

	public Set<InsightsReportsKPIConfig> getReportsKPIConfig() {
		return reportsKPIConfig;
	}

	public void setReportsKPIConfig(Set<InsightsReportsKPIConfig> reportsKPIConfig) {
		this.reportsKPIConfig = reportsKPIConfig;
	}


	


}
