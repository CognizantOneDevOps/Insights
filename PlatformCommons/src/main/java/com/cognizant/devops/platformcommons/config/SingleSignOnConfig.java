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

public class SingleSignOnConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2813046128665335728L;
	
	
	private String entityId;
	private String appId;
	private boolean metadataThroughFile;
	private String metadataUrl;
	private String metdataFilePath;
	private String keyStoreFilePath;
	private String keyAlias;
	private String keyPass;
	private String keyStorePass;
	private String appBaseUrl;
	private String relayStateUrl;
	private String defaultTargetUrl;
	private String postLogoutURL;
	private String tokenSigningKey;
	private String servicePrincipalKerberos;
	private String keyTabLocationKerberos;
	
	public String getEntityId() {
		return entityId;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public boolean isMetadataThroughFile() {
		return metadataThroughFile;
	}
	public void setMetadataThroughFile(boolean metadataThroughFile) {
		this.metadataThroughFile = metadataThroughFile;
	}
	public String getMetadataUrl() {
		return metadataUrl;
	}
	public void setMetadataUrl(String metadataUrl) {
		this.metadataUrl = metadataUrl;
	}
	public String getMetdataFilePath() {
		return metdataFilePath;
	}
	public void setMetdataFilePath(String metdataFilePath) {
		this.metdataFilePath = metdataFilePath;
	}
	public String getKeyStoreFilePath() {
		return keyStoreFilePath;
	}
	public void setKeyStoreFilePath(String keyStoreFilePath) {
		this.keyStoreFilePath = keyStoreFilePath;
	}
	public String getKeyAlias() {
		return keyAlias;
	}
	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}
	public String getKeyPass() {
		return keyPass;
	}
	public void setKeyPass(String keyPass) {
		this.keyPass = keyPass;
	}
	public String getKeyStorePass() {
		return keyStorePass;
	}
	public void setKeyStorePass(String keyStorePass) {
		this.keyStorePass = keyStorePass;
	}
	public String getAppBaseUrl() {
		return appBaseUrl;
	}
	public void setAppBaseUrl(String appBaseUrl) {
		this.appBaseUrl = appBaseUrl;
	}
	public String getRelayStateUrl() {
		return relayStateUrl;
	}
	public void setRelayStateUrl(String relayStateUrl) {
		this.relayStateUrl = relayStateUrl;
	}
	public String getDefaultTargetUrl() {
		return defaultTargetUrl;
	}
	public void setDefaultTargetUrl(String defaultTargetUrl) {
		this.defaultTargetUrl = defaultTargetUrl;
	}
	public String getPostLogoutURL() {
		return postLogoutURL;
	}
	public void setPostLogoutURL(String postLogoutURL) {
		this.postLogoutURL = postLogoutURL;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getTokenSigningKey() {
		return tokenSigningKey;
	}
	public void setTokenSigningKey(String tokenSigningKey) {
		this.tokenSigningKey = tokenSigningKey;
	}

	public String getServicePrincipalKerberos() {
		return servicePrincipalKerberos;
	}

	public void setServicePrincipalKerberos(String servicePrincipalKerberos) {
		this.servicePrincipalKerberos = servicePrincipalKerberos;
	}

	public String getKeyTabLocationKerberos() {
		return keyTabLocationKerberos;
	}

	public void setKeyTabLocationKerberos(String keyTabLocationKerberos) {
		this.keyTabLocationKerberos = keyTabLocationKerberos;
	}

}
