/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

public class AgentDetails implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2037234494858868857L;
	
	private String docrootUrl;
	private String unzipPath;
	private String agentExchange;
	private String agentPkgQueue;
	private boolean isOnlineRegistration =  true;
	private String onlineRegistrationMode;
	private String downloadRepoUrl;
	private String browseRepoUrl;
	
	public String getOnlineRegistrationMode() {
		return onlineRegistrationMode;
	}
	public void setOnlineRegistrationMode(String onlineRegistrationMode) {
		this.onlineRegistrationMode = onlineRegistrationMode;
	}
	public String getDownloadRepoUrl() {
		return downloadRepoUrl;
	}
	public void setDownloadRepoUrl(String downloadRepoUrl) {
		this.downloadRepoUrl = downloadRepoUrl;
	}
	public String getBrowseRepoUrl() {
		return browseRepoUrl;
	}
	public void setBrowseRepoUrl(String browseRepoUrl) {
		this.browseRepoUrl = browseRepoUrl;
	}
	private String offlineAgentPath;
	
	public String getDocrootUrl() {
		return docrootUrl;
	}
	public void setDocrootUrl(String docrootUrl) {
		this.docrootUrl = docrootUrl;
	}
	public String getUnzipPath() {
		return unzipPath;
	}
	public void setUnzipPath(String unzipPath) {
		this.unzipPath = unzipPath;
	}
	public String getAgentExchange() {
		return agentExchange;
	}
	public void setAgentExchange(String agentExchange) {
		this.agentExchange = agentExchange;
	}
	public String getAgentPkgQueue() {
		return agentPkgQueue;
	}
	public void setAgentPkgQueue(String agentPkgQueue) {
		this.agentPkgQueue = agentPkgQueue;
	}
	public boolean isOnlineRegistration() {
		return isOnlineRegistration;
	}
	public void setOnlineRegistration(boolean isOnlineRegistration) {
		this.isOnlineRegistration = isOnlineRegistration;
	}
	public String getOfflineAgentPath() {
		return offlineAgentPath;
	}
	public void setOfflineAgentPath(String offlineAgentPath) {
		this.offlineAgentPath = offlineAgentPath;
	}

}
