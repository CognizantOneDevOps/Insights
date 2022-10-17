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

public class WorkflowTakDataModel {
	
	private String workflowtype;
	private String mqchannel;
	private String description;
	private String dependency;
	private String componentname;

	public String getWorkflowtype() {
		return workflowtype;
	}
	public void setWorkflowtype(String workflowtype) {
		this.workflowtype = workflowtype;
	}
	public String getMqchannel() {
		return mqchannel;
	}
	public void setMqchannel(String mqchannel) {
		this.mqchannel = mqchannel;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDependency() {
		return dependency;
	}
	public void setDependency(String dependency) {
		this.dependency = dependency;
	}
	public String getComponentname() {
		return componentname;
	}
	public void setComponentname(String componentname) {
		this.componentname = componentname;
	}
}
