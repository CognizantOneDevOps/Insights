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
package com.cognizant.devops.platformregressiontest.test.ui.testdatamodel;

public class ReportConfigurationDataModel {
	
	//KPI
	private String kpiId;
	private String kpiName;
	private String toolName;
	private String groupName;
	private String category;
	private String isActive;
	private String dbQuery;
	private String resultField;
	private String datasource;
	
	//Content Configuration
	private String contentId;
	private String contentName;
	private String expectedTrend;
	private String directionOfThreshold;
	private String noOfResult;
	private String threshold;
	private String action;
	private String message;
	
	public String getKpiId() {
		return kpiId;
	}
	public void setKpiId(String kpiId) {
		this.kpiId = kpiId;
	}
	public String getKpiName() {
		return kpiName;
	}
	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}
	public String getToolName() {
		return toolName;
	}
	public void setToolName(String toolName) {
		this.toolName = toolName;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	public String getDbQuery() {
		return dbQuery;
	}
	public void setDbQuery(String dbQuery) {
		this.dbQuery = dbQuery;
	}
	public String getResultField() {
		return resultField;
	}
	public void setResultField(String resultField) {
		this.resultField = resultField;
	}
	public String getDatasource() {
		return datasource;
	}
	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
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
	public String getDirectionOfThreshold() {
		return directionOfThreshold;
	}
	public void setDirectionOfThreshold(String directionOfThreshold) {
		this.directionOfThreshold = directionOfThreshold;
	}
	public String getNoOfResult() {
		return noOfResult;
	}
	public void setNoOfResult(String noOfResult) {
		this.noOfResult = noOfResult;
	}
	public String getThreshold() {
		return threshold;
	}
	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
