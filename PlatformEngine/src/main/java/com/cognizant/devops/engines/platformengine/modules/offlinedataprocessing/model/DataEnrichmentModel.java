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
package com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.model;

/**
 * Datamodel of configuration json
 * used for Offline Data Processing
 * 
 * @author 368419
 *
 */
public class DataEnrichmentModel  {

	private String queryName;
	private String cypherQuery;
	private Long runSchedule;
	private String lastExecutionTime;
	private int recordsProcessed;
	private long queryProcessingTime;
	private String cronSchedule;
	
	
	public String getQueryName() {
		return queryName;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	public String getCypherQuery() {
		return cypherQuery;
	}
	public void setCypherQuery(String cypherQuery) {
		this.cypherQuery = cypherQuery;
	}
	public Long getRunSchedule() {
		return runSchedule;
	}
	public String getLastExecutionTime() {
		return lastExecutionTime;
	}
	public void setLastExecutionTime(String lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}
	public void setRunSchedule(Long runSchedule) {
		this.runSchedule = runSchedule;
	}
	public int getRecordsProcessed() {
		return recordsProcessed;
	}
	public void setRecordsProcessed(int recordsProcessed) {
		this.recordsProcessed = recordsProcessed;
	}
	public long getQueryProcessingTime() {
		return queryProcessingTime;
	}
	public void setQueryProcessingTime(long queryProcessingTime) {
		this.queryProcessingTime = queryProcessingTime;
	}
	public String getCronSchedule() {
		return cronSchedule;
	}
	public void setCronSchedule(String cronSchedule) {
		this.cronSchedule = cronSchedule;
	}
	
	

}
