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

import javax.validation.constraints.Pattern;

public class MessageQueueDataModel implements Serializable {

	@Pattern(regexp = "^(?!\\s*$).+", message = "MessageQueueDataModel host must not be blank")
	private String host;

	@Pattern(regexp = "^(?!\\s*$).+", message = "MessageQueueDataModel user authToken must not be blank")
	private String user;

	@Pattern(regexp = "^(?!\\s*$).+", message = "MessageQueueDataModel password authToken must not be blank")
	private String password;

	private int port = 5672;

	private int prefetchCount = 10;
	
	private boolean enableDeadLetterExchange = false; 
	
	private String providerName = "RabbitMQ";
	private String awsAccessKey = "";
	private String awsSecretKey="";
	private String awsRegion = "";

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getAwsRegion() {
		return awsRegion;
	}

	public void setAwsRegion(String awsRegion) {
		this.awsRegion = awsRegion;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPrefetchCount() {
		return prefetchCount;
	}

	public void setPrefetchCount(int prefetchCount) {
		this.prefetchCount = prefetchCount;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isEnableDeadLetterExchange() {
		return enableDeadLetterExchange;
	}

	public void setEnableDeadLetterExchange(boolean enableDeadLetterExchange) {
		this.enableDeadLetterExchange = enableDeadLetterExchange;
	}
}
