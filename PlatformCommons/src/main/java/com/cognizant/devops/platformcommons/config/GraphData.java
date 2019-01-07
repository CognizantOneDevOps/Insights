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

public class GraphData implements Serializable{
	private String endpoint;
	private String authToken;
	private String boltEndPoint;
	private Integer connectionExpiryTimeOut;
	private Integer maxIdleConnections;
	
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	public String getBoltEndPoint() {
		return boltEndPoint;
	}
	public void setBoltEndPoint(String boltEndPoint) {
		this.boltEndPoint = boltEndPoint;
	}
	public Integer getMaxIdleConnections() {
		return maxIdleConnections;
	}
	public void setMaxIdleConnections(Integer maxIdleConnections) {
		this.maxIdleConnections = maxIdleConnections;
	}
	public Integer getConnectionExpiryTimeOut() {
		return connectionExpiryTimeOut;
	}
	public void setConnectionExpiryTimeOut(Integer connectionExpiryTimeOut) {
		this.connectionExpiryTimeOut = connectionExpiryTimeOut;
	}
}
