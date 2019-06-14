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
package com.cognizant.devops.platforminsights.datamodel;

import java.io.Serializable;

import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;

public class Neo4jKPIDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7825944051560318074L;

	private Integer kpiID;
	private String name;
	private String expectedTrend;
	private ExecutionActions action;
	private JobSchedule schedule;
	private String startTimeField;
	private String endTimeField;
	private boolean aggregatedResult;
	private String timeFormat;
	private String durationField;
	private String resultOutPutType;
	private boolean isComparisionKpi;

	private boolean isGroupBy;
	private String vector;
	private String toolName;
	private String groupByFieldName;
	private String groupByField;
	private String averageField;
	private String sumCalculationField;

	private String dbType;
	private String dataQuery;
	private String neo4jQuery;

	private String neo4jLabel;
	private String nextRun;
	private Long lastRunTime;
	private boolean isActive;

	public Integer getKpiID() {
		return kpiID;
	}

	public void setKpiID(Integer kpiID) {
		this.kpiID = kpiID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpectedTrend() {
		return expectedTrend;
	}

	public void setExpectedTrend(String expectedTrend) {
		this.expectedTrend = expectedTrend;
	}

	public ExecutionActions getAction() {
		return action;
	}

	public void setAction(ExecutionActions action) {
		this.action = action;
	}

	public JobSchedule getSchedule() {
		return schedule;
	}

	public void setSchedule(JobSchedule schedule) {
		this.schedule = schedule;
	}

	public String getStartTimeField() {
		return startTimeField;
	}

	public void setStartTimeField(String startTimeField) {
		this.startTimeField = startTimeField;
	}

	public String getEndTimeField() {
		return endTimeField;
	}

	public void setEndTimeField(String endTimeField) {
		this.endTimeField = endTimeField;
	}

	public boolean isAggregatedResult() {
		return aggregatedResult;
	}

	public void setAggregatedResult(boolean aggregatedResult) {
		this.aggregatedResult = aggregatedResult;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public String getDurationField() {
		return durationField;
	}

	public void setDurationField(String durationField) {
		this.durationField = durationField;
	}

	public String getResultOutPutType() {
		return resultOutPutType;
	}

	public void setResultOutPutType(String resultOutPutType) {
		this.resultOutPutType = resultOutPutType;
	}

	public boolean isComparisionKpi() {
		return isComparisionKpi;
	}

	public void setComparisionKpi(boolean isComparisionKpi) {
		this.isComparisionKpi = isComparisionKpi;
	}

	public boolean isGroupBy() {
		return isGroupBy;
	}

	public void setGroupBy(boolean isGroupBy) {
		this.isGroupBy = isGroupBy;
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

	public String getGroupByFieldName() {
		return groupByFieldName;
	}

	public void setGroupByFieldName(String groupByFieldName) {
		this.groupByFieldName = groupByFieldName;
	}

	public String getGroupByField() {
		return groupByField;
	}

	public void setGroupByField(String groupByField) {
		this.groupByField = groupByField;
	}

	public String getAverageField() {
		return averageField;
	}

	public void setAverageField(String averageField) {
		this.averageField = averageField;
	}

	public String getSumCalculationField() {
		return sumCalculationField;
	}

	public void setSumCalculationField(String sumCalculationField) {
		this.sumCalculationField = sumCalculationField;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getDataQuery() {
		return dataQuery;
	}

	public void setDataQuery(String dataQuery) {
		this.dataQuery = dataQuery;
	}

	public String getNeo4jQuery() {
		return neo4jQuery;
	}

	public void setNeo4jQuery(String neo4jquery) {
		this.neo4jQuery = neo4jquery;
	}

	public String getNeo4jLabel() {
		return neo4jLabel;
	}

	public void setNeo4jLabel(String neo4jLabel) {
		this.neo4jLabel = neo4jLabel;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getNextRun() {
		return nextRun;
	}

	public void setNextRun(String nextRun) {
		this.nextRun = nextRun;
	}

	public Long getLastRunTime() {
		return lastRunTime;
	}

	public void setLastRunTime(Long lastRunTime) {
		this.lastRunTime = lastRunTime;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public String toString() {
		return "Neo4jKPIDefinition [kpiID=" + kpiID + ", name=" + name + ", expectedTrend=" + expectedTrend
				+ ", action=" + action + ", schedule=" + schedule + ", startTimeField=" + startTimeField
				+ ", endTimeField=" + endTimeField + ", aggregatedResult=" + aggregatedResult + ", timeFormat="
				+ timeFormat + ", durationField=" + durationField + ", resultOutPutType=" + resultOutPutType
				+ ", isComparisionKpi=" + isComparisionKpi + ", isGroupBy=" + isGroupBy + ", vector=" + vector
				+ ", toolName=" + toolName + ", groupByFieldName=" + groupByFieldName + ", groupByField=" + groupByField
				+ ", averageField=" + averageField + ", sumCalculationField=" + sumCalculationField + ", dbType="
				+ dbType + ", dataQuery=" + dataQuery + ", neo4jQuery=" + neo4jQuery + ", neo4jLabel=" + neo4jLabel
				+ ", nextRun=" + nextRun + ", lastRunTime=" + lastRunTime + ", isActive=" + isActive + "]";
	}

}
