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

public class SchedulerConfigData implements Serializable {

	private static final long serialVersionUID = -3717741106149569981L;
	private int auditEngineInterval = 60;
	private int webhookEngineInterval = 10;
	private int engineAggregatorModuleInterval = 10;
	private int engineCorrelatorModuleInterval = 60;
	private int offlineDataProcessingExecutorInterval = 10;
	private int dataArchivalEngineInterval = 240;
	private int offlineWebhookEventProcessingInterval = 15;

	public int getAuditEngineInterval() {
		return auditEngineInterval;
	}

	public void setAuditEngineInterval(int auditEngineInterval) {
		this.auditEngineInterval = auditEngineInterval;
	}

	public int getWebhookEngineInterval() {
		return webhookEngineInterval;
	}

	public void setWebhookEngineInterval(int webhookEngineInterval) {
		this.webhookEngineInterval = webhookEngineInterval;
	}

	public int getEngineAggregatorModuleInterval() {
		return engineAggregatorModuleInterval;
	}

	public void setEngineAggregatorModuleInterval(int engineAggregatorModuleInterval) {
		this.engineAggregatorModuleInterval = engineAggregatorModuleInterval;
	}

	public int getEngineCorrelatorModuleInterval() {
		return engineCorrelatorModuleInterval;
	}

	public void setEngineCorrelatorModuleInterval(int engineCorrelatorModuleInterval) {
		this.engineCorrelatorModuleInterval = engineCorrelatorModuleInterval;
	}

	public int getOfflineDataProcessingExecutorInterval() {
		return offlineDataProcessingExecutorInterval;
	}

	public void setOfflineDataProcessingExecutorInterval(int offlineDataProcessingExecutorInterval) {
		this.offlineDataProcessingExecutorInterval = offlineDataProcessingExecutorInterval;
	}

	public int getDataArchivalEngineInterval() {
		return dataArchivalEngineInterval;
	}

	public void setDataArchivalEngineInterval(int dataArchivalEngineInterval) {
		this.dataArchivalEngineInterval = dataArchivalEngineInterval;
	}

	public int getOfflineWebhookEventProcessingInterval() {
		return offlineWebhookEventProcessingInterval;
	}

	public void setOfflineWebhookEventProcessingInterval(int offlineWebhookEventProcessingInterval) {
		this.offlineWebhookEventProcessingInterval = offlineWebhookEventProcessingInterval;
	}

}
