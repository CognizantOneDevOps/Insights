/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.timertasks;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="\"INSIGHTS_SCHEDULER_TASK_DEFINITION\"")
public class InsightsSchedulerTaskDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1628503444L;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)     
    @Column(name="timerTaskId")
    private int timerTaskId;
    
    @Column(name="componentName", unique = true, nullable = false)
    private String componentName;
    
    @Column(name="componentClassDetail", unique = true, nullable = false)
    private String componentClassDetail;
    
    @Column(name="schedule")
    private String schedule;
    
    @Column (name="action")
    private String action;
    

	public int getTimerTaskId() {
		return timerTaskId;
	}

	public void setTimerTaskId(int timerTaskId) {
		this.timerTaskId = timerTaskId;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentClassDetail() {
		return componentClassDetail;
	}

	public void setComponentClassDetail(String componentClassDetail) {
		this.componentClassDetail = componentClassDetail;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
