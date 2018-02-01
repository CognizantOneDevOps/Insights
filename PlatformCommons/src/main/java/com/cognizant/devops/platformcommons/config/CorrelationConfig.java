package com.cognizant.devops.platformcommons.config;

public class CorrelationConfig {
	private int correlationWindow = 48;
	private int correlationFrequency = 4;
	private int batchSize = 2000;
	
	public int getCorrelationWindow() {
		return correlationWindow;
	}
	public void setCorrelationWindow(int correlationWindow) {
		this.correlationWindow = correlationWindow;
	}
	public int getCorrelationFrequency() {
		return correlationFrequency;
	}
	public void setCorrelationFrequency(int correlationFrequency) {
		this.correlationFrequency = correlationFrequency;
	}
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
