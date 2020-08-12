/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.workflow;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name="\"INSIGHTS_WORKFLOW_TASK\"")
public class InsightsWorkflowTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3311266957490868799L;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)     
    @Column(name="taskId")
    private int taskId;
    
    @Column(name="description")
    private String description;
    
    @Column(name="MQChannel")
    private String mqChannel;
    
    @Column(name="componentName")
    private String compnentName;
    
    @Column (name="dependency")
    private int dependency;   
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="workflowType", referencedColumnName="workflowType")
    private InsightsWorkflowType workflowType ;
    
  	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMqChannel() {
		return mqChannel;
	}

	public void setMqChannel(String mqChannel) {
		this.mqChannel = mqChannel;
	}

	public String getCompnentName() {
		return compnentName;
	}

	public void setCompnentName(String compnentName) {
		this.compnentName = compnentName;
	}

	public int getDependency() {
		return dependency;
	}

	public void setDependency(int dependency) {
		this.dependency = dependency;
	}

	public InsightsWorkflowType getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(InsightsWorkflowType workflowType) {
		this.workflowType = workflowType;
	}

	@Override
	public String toString() {
		return "InsightsWorkflowTask [taskId=" + taskId + ", description=" + description + ", mqChannel=" + mqChannel
				+ ", compnentName=" + compnentName + ", dependency=" + dependency + ", workflowType=" + workflowType
				+ "]";
	}
}
