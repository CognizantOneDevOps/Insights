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
import java.util.Date;
import java.util.List;

import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platformcommons.core.enums.KPISentiment;

public class InsightsInferenceDetail implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4922026359338156088L;
	
	private String kpi;
	private KPISentiment sentiment;
	private String trendline;
	private ExecutionActions action;	
	private Integer count;
	private String inference;
	private Integer ranking;
	private Integer kpiId;
	private String schedule;
	private Date lastRun;	
	private Long currentResult;
	private Long lastResult;
	private List<ResultSetModel> resultSet;	
	
	
	
	public List<ResultSetModel> getResultSet() {
		return resultSet;
	}
	public void setResultSet(List<ResultSetModel> resultSet) {
		this.resultSet = resultSet;
	}
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
		
	public String getSchedule() {
		return schedule;
	}
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}
	public Date getLastRun() {
		return lastRun;
	}
	public void setLastRun(Date lastRun) {
		this.lastRun = lastRun;
	}
	
	public Integer getKpiId() {
		return kpiId;
	}
	public void setKpiId(Integer kpiId) {
		this.kpiId = kpiId;
	}
	public String getKpi() {
		return kpi;
	}
	public void setKpi(String kpi) {
		this.kpi = kpi;
	}
	public KPISentiment getSentiment() {
		return sentiment;
	}
	public void setSentiment(KPISentiment sentiment) {
		this.sentiment = sentiment;
	}
	public String getTrendline() {
		return trendline;
	}
	public void setTrendline(String trendline) {
		this.trendline = trendline;
	}
	public ExecutionActions getAction() {
		return action;
	}
	public void setAction(ExecutionActions action) {
		this.action = action;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
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

}
