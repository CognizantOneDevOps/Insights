package com.cognizant.devops.platformcommons.config;

public class SchedulerConfigData {

	private int auditEngineInterval = 60;
	private int webhookEngineInterval = 10;
	private int engineAggregatorModuleInterval = 10;
	private int engineCorrelatorModuleInterval = 60;
	private int projectMapperModuleInterval = 10;
	private int dataPurgingExecutorInterval = 300;
	private int offlineDataProcessingExecutorInterval = 10;
	
	/*
	 * private boolean enableAuditEngine ; private boolean enableWebHookEngine ;
	 */

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

	public int getProjectMapperModuleInterval() {
		return projectMapperModuleInterval;
	}

	public void setProjectMapperModuleInterval(int projectMapperModuleInterval) {
		this.projectMapperModuleInterval = projectMapperModuleInterval;
	}

	public int getDataPurgingExecutorInterval() {
		return dataPurgingExecutorInterval;
	}

	public void setDataPurgingExecutorInterval(int dataPurgingExecutorInterval) {
		this.dataPurgingExecutorInterval = dataPurgingExecutorInterval;
	}

	public int getOfflineDataProcessingExecutorInterval() {
		return offlineDataProcessingExecutorInterval;
	}

	public void setOfflineDataProcessingExecutorInterval(int offlineDataProcessingExecutorInterval) {
		this.offlineDataProcessingExecutorInterval = offlineDataProcessingExecutorInterval;
	}

}
