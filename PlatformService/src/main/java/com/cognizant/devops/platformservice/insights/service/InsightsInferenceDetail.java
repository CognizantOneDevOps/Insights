/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy
*  of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.insights.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InsightsInferenceDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4922026359338156088L;

	private Long kpiId;
	private Long contentId;
	private String kpiName;
	private Double threshold;
	private String schedule;
	private String group;
	private String toolName;
	private String sentiment;
	private String trendline;
	private String action;
	private Integer noOfResult;
	private String inference;
	private Integer ranking;
	private String expectedTrend;
	private String actualTrend;
	private Long resultTime;
	private String resultTimeX;
	private long executionId;
	private String inferenceText;
	private String resultField;
	private List<ResultSetModel> resultSet = new ArrayList<>(0);

	public void setKpiId(Long kpiId) {
		this.kpiId = kpiId;
	}
	public Long getKpiId() {
		return kpiId;
	}

	public Long getContentId() {
		return contentId;
	}

	public void setContentId(Long contentId) {
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

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setVector(String vector) {
		this.group = vector;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public String getSentiment() {
		return sentiment;
	}

	public void setSentiment(String sentiment) {
		this.sentiment = sentiment;
	}

	public String getTrendline() {
		return trendline;
	}

	public void setTrendline(String trendline) {
		this.trendline = trendline;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Integer getNoOfResult() {
		return noOfResult;
	}

	public void setNoOfResult(Integer noOfResult) {
		this.noOfResult = noOfResult;
	}

	public String getInference() {
		return inference;
	}

	public void setInference(String inference) {
		this.inference = inference;
	}

	public Integer getRanking() {
		return ranking;
	}

	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	public String getExpectedTrend() {
		return expectedTrend;
	}

	public void setExpectedTrend(String expectedTrend) {
		this.expectedTrend = expectedTrend;
	}

	public String getActualTrend() {
		return actualTrend;
	}

	public void setActualTrend(String actualTrend) {
		this.actualTrend = actualTrend;
	}

	public Long getResultTime() {
		return resultTime;
	}

	public void setResultTime(Long resultTime) {
		this.resultTime = resultTime;
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

	public List<ResultSetModel> getResultSet() {
		return resultSet;
	}

	public void setResultSet(List<ResultSetModel> resultSet) {
		this.resultSet = resultSet;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getInferenceText() {
		return inferenceText;
	}

	public void setInferenceText(String inferenceText) {
		this.inferenceText = inferenceText;
	}

	public String getResultField() {
		return resultField;
	}

	public void setResultField(String resultField) {
		this.resultField = resultField;
	}

	@Override
	public String toString() {
		return "InsightsInferenceDetail [kpiId=" + kpiId + ", contentId=" + contentId + ", kpiName=" + kpiName
				+ ", threshold=" + threshold + ", schedule=" + schedule + ", group=" + group + ", toolName=" + toolName
				+ ", sentiment=" + sentiment + ", trendline=" + trendline + ", action=" + action + ", noOfResult="
				+ noOfResult + ", inference=" + inference + ", ranking=" + ranking + ", expectedTrend=" + expectedTrend
				+ ", actualTrend=" + actualTrend + ", resultTime=" + resultTime + ", resultTimeX=" + resultTimeX
				+ ", executionId=" + executionId + ", inferenceText=" + inferenceText + ", resultField=" + resultField
				+ ", resultSet=" + resultSet + "]";
	}

}