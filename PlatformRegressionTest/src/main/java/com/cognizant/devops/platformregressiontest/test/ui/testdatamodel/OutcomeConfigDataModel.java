/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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

public class OutcomeConfigDataModel {
	
	private String outcomeName;
	private String outcomeType;
	private String toolName;
	private String metricUrl;
	private String reqParamName;
	private String reqParamValue;
	private String indexName;
	
	
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public String getOutcomeName() {
		return outcomeName;
	}
	public void setOutcomeName(String outcomeName) {
		this.outcomeName = outcomeName;
	}
	public String getOutcomeType() {
		return outcomeType;
	}
	public void setOutcomeType(String outcomeType) {
		this.outcomeType = outcomeType;
	}
	public String getToolName() {
		return toolName;
	}
	public void setToolName(String toolName) {
		this.toolName = toolName;
	}
	public String getMetricUrl() {
		return metricUrl;
	}
	public void setMetricUrl(String metricUrl) {
		this.metricUrl = metricUrl;
	}
	public String getReqParamName() {
		return reqParamName;
	}
	public void setReqParamName(String reqParamName) {
		this.reqParamName = reqParamName;
	}
	public String getReqParamValue() {
		return reqParamValue;
	}
	public void setReqParamValue(String reqParamValue) {
		this.reqParamValue = reqParamValue;
	}
	
}
