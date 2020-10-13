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
package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

public class WorkflowDetails implements Serializable {

	/**
	 * This class is use for workflow configuration setup
	 */
	private static final long serialVersionUID = 1L;
	
	private int corePoolSize =8;
	private int maximumPoolSize=20;
	private long keepAliveTime=5;	
	private int waitingQueueSize=60;
	private String workflowExecutorCron = "1 0 0 * * ?";
	private String workflowRetryExecutorCron = "0 0 */4 ? * *";
	private String workflowAutoCorrectionSchedular="0 0 */4 ? * *";
	
	
	public int getCorePoolSize() {
		return corePoolSize;
	}
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}
	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}
	public long getKeepAliveTime() {
		return keepAliveTime;
	}
	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}
	public int getWaitingQueueSize() {
		return waitingQueueSize;
	}
	public void setWaitingQueueSize(int waitingQueueSize) {
		this.waitingQueueSize = waitingQueueSize;
	}

	public String getWorkflowExecutorCron() {
		return workflowExecutorCron;
	}

	public void setWorkflowExecutorCron(String workflowExecutorCron) {
		this.workflowExecutorCron = workflowExecutorCron;
	}

	public String getWorkflowRetryExecutorCron() {
		return workflowRetryExecutorCron;
	}

	public void setWorkflowRetryExecutorCron(String workflowRetryExecutorCron) {
		this.workflowRetryExecutorCron = workflowRetryExecutorCron;
	}
	public String getWorkflowAutoCorrectionSchedular() {
		return workflowAutoCorrectionSchedular;
	}
	public void setWorkflowAutoCorrectionSchedular(String workflowAutoCorrectionSchedular) {
		this.workflowAutoCorrectionSchedular = workflowAutoCorrectionSchedular;
	}
	
}
