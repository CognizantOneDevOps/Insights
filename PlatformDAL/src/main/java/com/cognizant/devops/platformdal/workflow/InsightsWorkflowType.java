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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="\"INSIGHTS_WORKFLOW_TYPE\"")
public class InsightsWorkflowType implements Serializable {

	private static final long serialVersionUID = 7491825362189572015L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="workflowType", unique=true, nullable=false)
	private String workflowType;
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true , mappedBy="workflowType")
	private Set<InsightsWorkflowTask> workflowTaskEntity;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}

	public Set<InsightsWorkflowTask> getWorkflowTaskEntity() {
		return workflowTaskEntity;
	}

	public void setWorkflowTaskEntity(Set<InsightsWorkflowTask> workflowTaskEntity) {
		this.workflowTaskEntity = workflowTaskEntity;
	}
}
