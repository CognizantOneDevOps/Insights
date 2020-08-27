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
package com.cognizant.devops.platformreports.assessment.datamodel;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentConfigDefinition {
	private static final long serialVersionUID = 1587999803000L;

	private int contentId;
	private Integer kpiId;
	private ReportEngineEnum.ExecutionActions action;
	private ReportEngineEnum.DirectionOfThreshold directionOfThreshold;
	private ReportEngineEnum.ContentCategory category;
	private WorkflowTaskEnum.WorkflowSchedule schedule;
	private String contentName;
	private String message;
	private String expectedTrend; 
	private Integer noOfResult;
	private Double threshold;
	private String thresholds;
	private long executionId;
	private int assessmentId;
	private int reportId;
	private String workflowId;
	private String resultField;
	

	public String getThresholds() {
		return thresholds;
	}

	public void setThresholds(String thresholds) {
		this.thresholds = thresholds;
	}

	public int getContentId() {
		return contentId;
	}

	public void setContentId(int contentId) {
		this.contentId = contentId;
	}

	public Integer getKpiId() {
		return kpiId;
	}

	public void setKpiId(Integer kpiId) {
		this.kpiId = kpiId;
	}
	
	public ReportEngineEnum.ExecutionActions getAction() {
		return action;
	}

	public void setAction(ReportEngineEnum.ExecutionActions action) {
		this.action = action;
	}

	public ReportEngineEnum.DirectionOfThreshold getDirectionOfThreshold() {
		return directionOfThreshold;
	}

	public void setDirectionOfThreshold(ReportEngineEnum.DirectionOfThreshold directionOfThreshold) {
		this.directionOfThreshold = directionOfThreshold;
	}

	public String getContentName() {
		return contentName;
	}

	public void setContentName(String contentName) {
		this.contentName = contentName;
	}

	
	public String getExpectedTrend() {
		return expectedTrend;
	}

	public void setExpectedTrend(String expectedTrend) {
		this.expectedTrend = expectedTrend;
	}

	public ReportEngineEnum.ContentCategory getCategory() {
		return category;
	}

	public void setCategory(ReportEngineEnum.ContentCategory category) {
		this.category = category;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getNoOfResult() {
		return noOfResult;
	}

	public void setNoOfResult(Integer noOfResult) {
		this.noOfResult = noOfResult;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	public long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(long executionId) {
		this.executionId = executionId;
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

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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

	@Override
	public String toString() {
		return "ContentConfigDefinition [contentId=" + contentId + ", kpiId=" + kpiId + ", action=" + action
				+ ", directionOfThreshold=" + directionOfThreshold + ", category=" + category + ", schedule=" + schedule
				+ ", contentName=" + contentName + ", message=" + message + ", expectedTrend=" + expectedTrend
				+ ", noOfResult=" + noOfResult + ", threshold=" + threshold + ", thresholds=" + thresholds
				+ ", executionId=" + executionId + ", assessmentId=" + assessmentId + ", reportId=" + reportId
				+ ", workflowId=" + workflowId + ", resultField=" + resultField + "]";
	}
}
