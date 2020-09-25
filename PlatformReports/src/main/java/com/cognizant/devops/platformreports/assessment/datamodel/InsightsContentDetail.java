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
import java.util.Map;

import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InsightsContentDetail implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4922026359338156088L;
	
	private Long kpiId;
	private int contentId;
	private String kpiName;
	private Double threshold;
	private String schedule;
	private String group;
	private String toolName;
	private ReportEngineEnum.KPISentiment sentiment;
	private String trendline;
	private ReportEngineEnum.ContentCategory category;
	private Integer noOfResult;
	private String inferenceText;
	private Integer ranking;
	private String expectedTrend;
	private String actualTrend;
	private String resultField;
	private Long resultTime;
	private String resultTimeX;
	private long executionId;
	private int reportId;
	private int assessmentId;

	@JsonIgnore
	private transient Map<String, Object> resultValuesMap;
	
	public Long getKpiId() {
		return kpiId;
	}

	public void setKpiId(Long kpiId) {
		this.kpiId = kpiId;
	}

	public int getContentId() {
		return contentId;
	}

	public void setContentId(int contentId) {
		this.contentId = contentId;
	}

	public String getKpiName() {
		return kpiName;
	}

	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public ReportEngineEnum.KPISentiment getSentiment() {
		return sentiment;
	}

	public void setSentiment(ReportEngineEnum.KPISentiment sentiment) {
		this.sentiment = sentiment;
	}

	public String getExpectedTrend() {
		return expectedTrend;
	}

	public void setExpectedTrend(String expectedTrend) {
		this.expectedTrend = expectedTrend;
	}

	public String getTrendline() {
		return trendline;
	}

	public void setTrendline(String trendline) {
		this.trendline = trendline;
	}

	

	public Integer getNoOfResult() {
		return noOfResult;
	}

	public void setNoOfResult(Integer noOfResult) {
		this.noOfResult = noOfResult;
	}

	public String getInferenceText() {
		return inferenceText;
	}

	public void setInferenceText(String inferenceText) {
		this.inferenceText = inferenceText;
	}

	public Integer getRanking() {
		return ranking;
	}

	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	

	public String getActualTrend() {
		return actualTrend;
	}

	public void setActualTrend(String actualTrend) {
		this.actualTrend = actualTrend;
	}

	public String getResultField() {
		return resultField;
	}

	public void setResultField(String resultField) {
		this.resultField = resultField;
	}

	public Long getResultTime() {
		return resultTime;
	}

	public void setResultTime(Long resultTime) {
		this.resultTime = resultTime;
	}

	public ReportEngineEnum.ContentCategory getCategory() {
		return category;
	}

	public void setCategory(ReportEngineEnum.ContentCategory category) {
		this.category = category;
	}

	public String getResultTimeX() {
		return resultTimeX;
	}

	public void setResultTimeX(String resultTimeX) {
		this.resultTimeX = resultTimeX;
	}

	public long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(long executionId) {
		this.executionId = executionId;
	}

	public int getReportId() {
		return reportId;
	}

	public void setReportId(int reportId) {
		this.reportId = reportId;
	}

	public int getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(int assessmentId) {
		this.assessmentId = assessmentId;
	}

	public Map<String, Object> getResultValuesMap() {
		return resultValuesMap;
	}

	public void setResultValuesMap(Map<String, Object> resultValuesMap) {
		this.resultValuesMap = resultValuesMap;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "InsightsContentDetail [kpiId=" + kpiId + ", contentId=" + contentId + ", kpiName=" + kpiName
				+ ", threshold=" + threshold + ", schedule=" + schedule + ", group=" + group + ", toolName=" + toolName
				+ ", sentiment=" + sentiment + ", trendline=" + trendline + ", category=" + category + ", noOfResult="
				+ noOfResult + ", inferenceText=" + inferenceText + ", ranking=" + ranking + ", expectedTrend="
				+ expectedTrend + ", actualTrend=" + actualTrend + ", resultField=" + resultField + ", resultTime="
				+ resultTime + ", resultTimeX=" + resultTimeX + ", executionId=" + executionId + ", reportId="
				+ reportId + ", assessmentId=" + assessmentId + ", resultValuesMap=" + resultValuesMap + "]";
	}

}
