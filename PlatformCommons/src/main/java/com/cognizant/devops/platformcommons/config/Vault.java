/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
