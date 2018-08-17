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

public class GrafanaData implements Serializable {
	private String grafanaEndpoint;
	private String grafanaExternalEndPoint;
	private String grafanaDBEndpoint;
	private String adminUserName;
	private String adminUserPassword;
	private String dbUserName;
	private String dbPassword;
	private String grafanaVersion;

	public String getGrafanaEndpoint() {
		return grafanaEndpoint;
	}

	public void setGrafanaEndpoint(String grafanaEndpoint) {
		this.grafanaEndpoint = grafanaEndpoint;
	}

	public String getGrafanaDBEndpoint() {
		return grafanaDBEndpoint;
	}

	public void setGrafanaDBEndpoint(String grafanaDBEndpoint) {
		this.grafanaDBEndpoint = grafanaDBEndpoint;
	}

	public String getAdminUserName() {
		return adminUserName;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public String getAdminUserPassword() {
		return adminUserPassword;
	}

	public void setAdminUserPassword(String adminUserPassword) {
		this.adminUserPassword = adminUserPassword;
	}

	public String getDbUserName() {
		return dbUserName;
	}

	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getGrafanaExternalEndPoint() {
		return grafanaExternalEndPoint;
	}

	public void setGrafanaExternalEndPoint(String grafanaExternalEndPoint) {
		this.grafanaExternalEndPoint = grafanaExternalEndPoint;
	}

	public String getGrafanaVersion() {
		return grafanaVersion;
	}

	public void setGrafanaVersion(String grafanaVersion) {
		this.grafanaVersion = grafanaVersion;
	}
}
