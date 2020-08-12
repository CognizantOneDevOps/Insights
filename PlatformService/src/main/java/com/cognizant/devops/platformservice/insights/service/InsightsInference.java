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

import java.io.Serializable;
import java.util.List;

public class InsightsInference implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -8891545017966408640L;
	
	
	private String heading;
	private List<InsightsInferenceDetail> inferenceDetails;
	private Integer ranking;
	private Long currentResult;
	private Long lastResult;
	private String executionDate;
	
	public Long getCurrentResult() {
		return currentResult;
	}
	public void setCurrentResult(Long currentResult) {
		this.currentResult = currentResult;
	}
	public Long getLastResult() {
		return lastResult;
	}
	public void setLastResult(Long lastResult) {
		this.lastResult = lastResult;
	}
		
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	public List<InsightsInferenceDetail> getInferenceDetails() {
		return inferenceDetails;
	}
	public void setInferenceDetails(List<InsightsInferenceDetail> inferenceDetails) {
		this.inferenceDetails = inferenceDetails;
	}
	public Integer getRanking() {
		return ranking;
	}
	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}
	public String getExecutionDate() {
		return executionDate;
	}
	public void setExecutionDate(String executionDate) {
		this.executionDate = executionDate;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
