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

import java.util.Map;

public class InsightsKPIResultDetails {

	private Long kpiId;
	private String kpiName;
	private String schedule;
	private String groupName;
	private String toolname;
	private String resultField;	
	private Map<String, Object> results;
	private Long resultTime;
	private String resultTimeX;
	private long executionId;
	private long recordDate;
	public Long getKpiId() {
		return kpiId;
	}
	public void setKpiId(Long kpiId) {
		this.kpiId = kpiId;
	}

	public String getKpiName() {
		return kpiName;
	}

	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}
	public String getSchedule() {
		return schedule;
	}
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getToolname() {
		return toolname;
	}

	public void setToolname(String toolname) {
		this.toolname = toolname;
	}
	public String getResultField() {
		return resultField;
	}
	public void setResultField(String resultField) {
		this.resultField = resultField;
	}
	public Map<String, Object> getResults() {
		return results;
	}
	public void setResults(Map<String, Object> results) {
		this.results = results;
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
	public long getRecordDate() {
		return recordDate;
	}
	public void setRecordDate(long recordDate) {
		this.recordDate = recordDate;
	}

	@Override
	public String toString() {
		return "InsightsKPIResultDetails [kpiId=" + kpiId + ", kpiName=" + kpiName + ", schedule=" + schedule
				+ ", groupName=" + groupName + ", toolname=" + toolname + ", resultField=" + resultField + ", results="
				+ results + ", resultTime=" + resultTime + ", resultTimeX=" + resultTimeX + ", executionId="
				+ executionId + ", recordDate=" + recordDate + "]";
	}

}
