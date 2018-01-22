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
package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

public class SparkConfigurations implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4137658777393986499L;
	private String appName, master, sparkExecutorMemory, sparkElasticSearchHost,
	sparkElasticSearchPort, sparkElasticSearchConfigIndex, sparkElasticSearchResultIndex, kpiSize, 
	sparkMasterExecutionEndPoint;
	
	private Long sparkResultSince;
	
	public SparkConfigurations()
	{
		
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getSparkExecutorMemory() {
		return sparkExecutorMemory;
	}

	public void setSparkExecutorMemory(String sparkExecutorMemory) {
		this.sparkExecutorMemory = sparkExecutorMemory;
	}
	
	public String getSparkElasticSearchHost() {
		return sparkElasticSearchHost;
	}

	public void setSparkElasticSearchHost(String sparkElasticSearchHost) {
		this.sparkElasticSearchHost = sparkElasticSearchHost;
	}

	public String getSparkElasticSearchPort() {
		return sparkElasticSearchPort;
	}

	public void setSparkElasticSearchPort(String sparkElasticSearchPort) {
		this.sparkElasticSearchPort = sparkElasticSearchPort;
	}

	public String getSparkElasticSearchConfigIndex() {
		return sparkElasticSearchConfigIndex;
	}

	public void setSparkElasticSearchConfigIndex(String sparkElasticSearchConfigIndex) {
		this.sparkElasticSearchConfigIndex = sparkElasticSearchConfigIndex;
	}

	public String getSparkElasticSearchResultIndex() {
		return sparkElasticSearchResultIndex;
	}

	public void setSparkElasticSearchResultIndex(String sparkElasticSearchResultIndex) {
		this.sparkElasticSearchResultIndex = sparkElasticSearchResultIndex;
	}

	public String getKpiSize() {
		return kpiSize;
	}

	public void setKpiSize(String kpiSize) {
		this.kpiSize = kpiSize;
	}

	public Long getSparkResultSince() {
		return sparkResultSince;
	}

	public void setSparkResultSince(Long sparkResultSince) {
		this.sparkResultSince = sparkResultSince;
	}
	
	public String getSparkMasterExecutionEndPoint() {
		return sparkMasterExecutionEndPoint;
	}

	public void setSparkEnvironmentEndPoint(String sparkMasterExecutionEndPoint) {
		this.sparkMasterExecutionEndPoint = sparkMasterExecutionEndPoint;
	}
	
}
