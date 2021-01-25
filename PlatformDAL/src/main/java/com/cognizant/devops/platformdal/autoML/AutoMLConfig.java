/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.autoML;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;

@Entity
@Table(name = "\"INSIGHTS_AUTOML_CONFIGURATION\"")
public class AutoMLConfig {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "USECASENAME", unique = true, nullable = false)
	private String useCaseName;
	
	@Column(name = "FILE")
	private byte[] file;	

	@Column(name = "CONFIG_JSON", length = 10000)
	private String configJson;

	@Column(name = "ISACTIVE")
	private Boolean isActive = true;

	@Column(name = "PREDICTION_COLUMN")
	private String predictionColumn;

	@Column(name = "TRAININGPERC")
	private Integer trainingPerc;

	@Column(name = "NUMOFMODELS")
	private String numOfModels;
	
	@Column(name="MODEL_ID")
	private String modelId;

	@Column(name = "USECASEFILE")
	private String useCaseFile;

	@Column(name = "MOJO_DEPLOYED")
	private String mojoDeployed="";
	
	@Column(name="MOJO_DEPLOYED_ZIP")
	private byte[] mojoDeployedZip;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "CREATED_DATE")
	private Long createdDate;

	@Column(name = "UPDATED_DATE")
	private Long updatedDate = 0L;
	
	@Column(name="PREDICTION_TYPE")
	private String predictionType;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "workflowId", referencedColumnName = "workflowId")
	private InsightsWorkflowConfiguration workflowConfig;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUseCaseName() {
		return useCaseName;
	}

	public void setUseCaseName(String useCaseName) {
		this.useCaseName = useCaseName;
	}

	public String getConfigJson() {
		return configJson;
	}

	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getPredictionColumn() {
		return predictionColumn;
	}

	public void setPredictionColumn(String predictionColumn) {
		this.predictionColumn = predictionColumn;
	}

	public Integer getTrainingPerc() {
		return trainingPerc;
	}

	public void setTrainingPerc(Integer trainingPerc) {
		this.trainingPerc = trainingPerc;
	}

	public String getNumOfModels() {
		return numOfModels;
	}

	public void setNumOfModels(String numOfModels) {
		this.numOfModels = numOfModels;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}	

	public String getMojoDeployed() {
		return mojoDeployed;
	}

	public void setMojoDeployed(String mojoDeployed) {
		this.mojoDeployed = mojoDeployed;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Long createdDate) {
		this.createdDate = createdDate;
	}

	public Long getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Long updatedDate) {
		this.updatedDate = updatedDate;
	}

	public InsightsWorkflowConfiguration getWorkflowConfig() {
		return workflowConfig;
	}

	public void setWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		this.workflowConfig = workflowConfig;
	}

	public byte[] getMojoDeployedZip() {
		return mojoDeployedZip;
	}

	public void setMojoDeployedZip(byte[] mojoDeployedZip) {
		this.mojoDeployedZip = mojoDeployedZip;
	}

	public String getPredictionType() {
		return predictionType;
	}

	public void setPredictionType(String predictionType) {
		this.predictionType = predictionType;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public String getUseCaseFile() {
		return useCaseFile;
	}

	public void setUseCaseFile(String useCaseFile) {
		this.useCaseFile = useCaseFile;
	}
	

}
