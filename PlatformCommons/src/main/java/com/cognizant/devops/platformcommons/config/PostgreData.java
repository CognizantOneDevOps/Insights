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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class PostgreData implements Serializable{
	
	@NotEmpty 
	@Pattern(regexp = "^(?!\\s*$).+", message = "postgres username must not be blank")
	private String userName;
	
	@NotNull 
	@NotBlank
	@Pattern(regexp = "^(?!\\s*$).+", message = "postgres password must not be blank")
	private String password;
	
	private String insightsDBUrl;
	private String grafanaDBUrl;
	private String driver="org.postgresql.Driver";
	private String dialect="org.hibernate.dialect.PostgreSQLDialect";
	private String c3pMinSize="9";
	private String c3pMaxSize="25";
	private String c3pTimout="2000";
	private String c3pMaxStatements="300";
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getInsightsDBUrl() {
		return insightsDBUrl;
	}
	public void setInsightsDBUrl(String insightsDBUrl) {
		this.insightsDBUrl = insightsDBUrl;
	}
	public String getGrafanaDBUrl() {
		return grafanaDBUrl;
	}
	public void setGrafanaDBUrl(String grafanaDBUrl) {
		this.grafanaDBUrl = grafanaDBUrl;
	}
	public String getC3pMinSize() {
		return c3pMinSize;
	}
	public void setC3pMinSize(String c3pMinSize) {
		this.c3pMinSize = c3pMinSize;
	}
	public String getC3pMaxSize() {
		return c3pMaxSize;
	}
	public void setC3pMaxSize(String c3pMaxSize) {
		this.c3pMaxSize = c3pMaxSize;
	}
	public String getC3pTimout() {
		return c3pTimout;
	}
	public void setC3pTimout(String c3pTimout) {
		this.c3pTimout = c3pTimout;
	}
	public String getC3pMaxStatements() {
		return c3pMaxStatements;
	}
	public void setC3pMaxStatements(String c3pMaxStatements) {
		this.c3pMaxStatements = c3pMaxStatements;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getDialect() {
		return dialect;
	}
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}
	
	
}


