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
package com.cognizant.devops.platformreports.assessment.datamodel;

public class GrafanaReportConfigurationDTO {

	private String title;
	private String pdfType;
	private String scheduleType;
	private String source;
	private String organisation;
	private String dashboardJson;
	private String workflowType;
	private String variables;
	private Boolean isAssessmentReport = Boolean.FALSE;
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
	public String getScheduleType() {
		return scheduleType;
	}
	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getOrganisation() {
		return organisation;
	}
	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}
	public String getDashboardJson() {
		return dashboardJson;
	}
	public void setDashboardJson(String dashboardJson) {
		this.dashboardJson = dashboardJson;
	}
	public String getWorkflowType() {
		return workflowType;
	}
	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}
	public String getVariables() {
		return variables;
	}
	public void setVariables(String variables) {
		this.variables = variables;
	}
	
	public Boolean getIsAssessmentReport() {
		return isAssessmentReport;
	}
	public void setIsAssessmentReport(Boolean isAssessmentReport) {
		this.isAssessmentReport = isAssessmentReport;
	}
	@Override
	public String toString() {
		return "GrafanaReportConfigurationDTO [title=" + title + ", pdfType=" + pdfType + ", scheduleType="
				+ scheduleType + ", source=" + source + ", organisation=" + organisation + ", dashboardJson="
				+ dashboardJson + ", workflowType=" + workflowType + ", variables=" + variables + "]";
	}
	
}
