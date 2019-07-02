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
package com.cognizant.devops.platformservice.insights.service;

public class InferenceResultDetails {

	private Long kpiID;
	private String name;
	private String action;
	private String schedule;
	private String vector;
	private String toolName;
	private Boolean isComparisionKpi;
	private String resultOutPutType;
	private Boolean isGroupBy;
	private String groupByName;
	private String groupByField;
	private String groupByFieldVal;
	private String expectedTrend;
	private Long result;
	private Long resultTime;
	private String resultTimeX;
	

	public Long getKpiID() {
		return kpiID;
	}
	public void setKpiID(Long kpiID) {
		this.kpiID = kpiID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getSchedule() {
		return schedule;
	}
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}
	public String getVector() {
		return vector;
	}
	public void setVector(String vector) {
		this.vector = vector;
	}
	public String getToolName() {
		return toolName;
	}
	public void setToolName(String toolName) {
		this.toolName = toolName;
	}
	public Boolean getIsComparisionKpi() {
		return isComparisionKpi;
	}
	public void setIsComparisionKpi(Boolean isComparisionKpi) {
		this.isComparisionKpi = isComparisionKpi;
	}
	public String getResultOutPutType() {
		return resultOutPutType;
	}
	public void setResultOutPutType(String resultOutPutType) {
		this.resultOutPutType = resultOutPutType;
	}
	public Boolean getIsGroupBy() {
		return isGroupBy;
	}
	public void setIsGroupBy(Boolean isGroupBy) {
		this.isGroupBy = isGroupBy;
	}
	public String getGroupByName() {
		return groupByName;
	}
	public void setGroupByName(String groupByName) {
		this.groupByName = groupByName;
	}
	public String getGroupByField() {
		return groupByField;
	}
	public void setGroupByField(String groupByField) {
		this.groupByField = groupByField;
	}
	public String getGroupByFieldVal() {
		return groupByFieldVal;
	}
	public void setGroupByFieldVal(String groupByFieldVal) {
		this.groupByFieldVal = groupByFieldVal;
	}
	public String getExpectedTrend() {
		return expectedTrend;
	}
	public void setExpectedTrend(String expectedTrend) {
		this.expectedTrend = expectedTrend;
	}
	public Long getResult() {
		return result;
	}
	public void setResult(Long result) {
		this.result = result;
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
	@Override
	public String toString() {
		return "InferenceResultDetails [kpiID=" + kpiID + ", name=" + name + ", action=" + action + ", schedule="
				+ schedule + ", vector=" + vector + ", toolName=" + toolName + ", isComparisionKpi=" + isComparisionKpi
				+ ", resultOutPutType=" + resultOutPutType + ", isGroupBy=" + isGroupBy + ", groupByName=" + groupByName
				+ ", groupByField=" + groupByField + ", groupByFieldVal=" + groupByFieldVal + ", expectedTrend="
				+ expectedTrend + ", result=" + result + ", resultTime=" + resultTime + ", resultTimeX=" + resultTimeX
				+ "]";
	}
}
