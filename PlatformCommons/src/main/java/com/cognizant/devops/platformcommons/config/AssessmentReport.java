/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

public class AssessmentReport implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1592475870L;
	
	
	
	private String outputDatasource = "NEO4J";
	private int maxWorkflowRetries = 5;
	private String fusionExportAPIUrl = "http://localhost:1337/api/v2.0/export";

	public String getOutputDatasource() {
		return outputDatasource;
	}

	public void setOutputDatasource(String outputDatasource) {
		this.outputDatasource = outputDatasource;
	}

	public int getMaxWorkflowRetries() {
		return maxWorkflowRetries;
	}

	public void setMaxWorkflowRetries(int maxWorkflowRetries) {
		this.maxWorkflowRetries = maxWorkflowRetries;
	}

	public String getFusionExportAPIUrl() {
		return fusionExportAPIUrl;
	}

	public void setFusionExportAPIUrl(String fusionExportAPIUrl) {
		this.fusionExportAPIUrl = fusionExportAPIUrl;
	}
	
	
}

