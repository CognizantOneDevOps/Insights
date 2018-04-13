package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

public class QueryCache implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -11994006968623059L;
	
	private String esCacheIndex;

	public String getEsCacheIndex() {
		return esCacheIndex;
	}

	public void setEsCacheIndex(String esCacheIndex) {
		this.esCacheIndex = esCacheIndex;
	}
}
