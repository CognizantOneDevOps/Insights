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
package com.cognizant.devops.platformreports.assessment.datamodel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(value = { "contentConfig" ,"dBQuery","datasource","inputDatasource"})
public class InsightsKPIConfigDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7825944051560318074L;

	private int kpiId;
	private String kpiName;
	private WorkflowTaskEnum.WorkflowSchedule schedule;
	private String toolname;
	private String groupName;
	private String dBQuery;	
	private String category;
	private String resultField;
	private long executionId;
	private int assessmentId;
	private int reportId;
	private String workflowId;
	private String datasource;
	private String inputDatasource;
	private String outputDatasource;
	private long lastRunTime;
	private long nextRunTime;
	private long oneTimeReportStartTime;
	private long oneTimeReportEndDate;

	private Set<InsightsContentConfig> contentConfig = new HashSet<>(0);
	
	

	public int getKpiId() {
		return kpiId;
	}

	public void setKpiId(int kpiId) {
		this.kpiId = kpiId;
	}

	public WorkflowTaskEnum.WorkflowSchedule getSchedule() {
		return schedule;
	}

	public void setSchedule(WorkflowTaskEnum.WorkflowSchedule schedule) {
		this.schedule = schedule;
	}

	public String getResultField() {
		return resultField;
	}

	public void setResultField(String resultField) {
		this.resultField = resultField;
	}

	public long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(long executionId) {
		this.executionId = executionId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public int getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(int assessmentId) {
		this.assessmentId = assessmentId;
	}


	public int getReportId() {
		return reportId;
	}

	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	
	public String getKpiName() {
		return kpiName;
	}

	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}

	public String getToolname() {
		return toolname;
	}

	public void setToolname(String toolname) {
		this.toolname = toolname;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getdBQuery() {
		return dBQuery;
	}

	public void setdBQuery(String dBQuery) {
		this.dBQuery = dBQuery;
	}

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	public Set<InsightsContentConfig> getContentConfig() {
		return contentConfig;
	}

	public void setContentConfig(Set<InsightsContentConfig> contentConfig) {
		this.contentConfig = contentConfig;
	}

	public String getInputDatasource() {
		return inputDatasource;
	}

	public void setInputDatasource(String inputDatasource) {
		this.inputDatasource = inputDatasource;
	}

	public String getOutputDatasource() {
		return outputDatasource;
	}

	public void setOutputDatasource(String outputDatasource) {
		this.outputDatasource = outputDatasource;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public long getLastRunTime() {
		return lastRunTime;
	}

	public void setLastRunTime(long lastRunTime) {
		this.lastRunTime = lastRunTime;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public long getNextRunTime() {
		return nextRunTime;
	}

	public void setNextRunTime(long nextRunTime) {
		this.nextRunTime = nextRunTime;
	}

	public long getOneTimeReportStartTime() {
		return oneTimeReportStartTime;
	}

	public void setOneTimeReportStartTime(long oneTimeReportStartTime) {
		this.oneTimeReportStartTime = oneTimeReportStartTime;
	}

	public long getOneTimeReportEndDate() {
		return oneTimeReportEndDate;
	}

	public void setOneTimeReportEndDate(long oneTimeReportEndDate) {
		this.oneTimeReportEndDate = oneTimeReportEndDate;
	}
	
}
