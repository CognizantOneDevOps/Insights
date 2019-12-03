package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

public class Vault implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String vaultEndPoint;
	private String secretEngine;
	private String vaultToken;
	
	
	public String getVaultEndPoint() {
		return vaultEndPoint;
	}
	public void setVaultEndPoint(String vaultEndPoint) {
		this.vaultEndPoint = vaultEndPoint;
	}
	public String getSecretEngine() {
		return secretEngine;
	}
	public void setSecretEngine(String secretEngine) {
		this.secretEngine = secretEngine;
	}
	public String getVaultToken() {
		return vaultToken;
	}
	public void setVaultToken(String vaultToken) {
		this.vaultToken = vaultToken;
	}

}
