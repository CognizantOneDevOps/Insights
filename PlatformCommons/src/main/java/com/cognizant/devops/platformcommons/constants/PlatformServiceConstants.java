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
package com.cognizant.devops.platformcommons.constants;

import java.util.Base64;

public final class PlatformServiceConstants {
	public static final String STATUS = "status";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	public static final String ERROR = "ERROR";
	public static final String MESSAGE = "message";
	public static final String DATA = "data";
	public static final String INVALID_REQUEST = "Invalid request.";
	public static final String INVALID_REQUEST_BODY = "Invalid request,Please check Request Body Or Request Payload of API.";
	public static final String INVALID_RESPONSE_DATA_HTML = "Invalid response data,Response might be contain some Html tag.";
	public static final String INVALID_RESPONSE_DATA = "Invalid response data while parsing response data.";
	public static final String INVALID_REQUEST_ORIGIN = " UnknownHostException :Please make sure that your host is in trusted host list.";
	public static final String HOST_NOT_FOUND = "UnknownHostException : Unable to find valid host information. ";
	public static final String INVALID_FILE = "Invalid file";
	public static final String INVALID_TOKEN = "Invalid Autharization Token";
	public static final String TRANSFORMATION_DECODED = new String(
			Base64.getDecoder().decode(ConfigOptions.TRANSFORMATION_ENCODED.getBytes()));
	public static final String SP_DECODED = new String(Base64.getDecoder().decode(ConfigOptions.SP_ENCODED.getBytes()));
	public static final String RSA_DECODED = new String(Base64.getDecoder().decode(ConfigOptions.RSA_ENCODED.getBytes()));
	public static final String GRAFANA_LOGIN_ISSUE = "Unable to connect to Grafana";
	public static final String INCORRECT_RESPONSE_TEMPLATE = "Incorrect Response Template";
	public static final String WEBHOOK_NAME = "Webhook name already exists";
	public static final String INSIGHTSTIME = "inSightsTime";
	public static final String INSIGHTSTIMEX = "inSightsTimeX";
	public static final String API_ADMIN_USERS = "/api/admin/users/";
	public static final String JTOKEN = "jtoken";
	public static final String VAULT_TOKEN= "X-Vault-Token";
	public static final String VAULT_DATA_VALUE="value";
	// AccessGroupManagement
	public static final String ORGID = "orgId";
	public static final String USERS = "/users";
	public static final String LOGIN_OR_EMAIL = "loginOrEmail";
	public static final String EMAIL = "email";
	public static final String TITLE = "title";
	public static final String API_ORGS = "/api/orgs/";
	public static final String API_USER = "/api/user";
	public static final String API_USER_ORGS = "/api/user/orgs";
	public static final String GET_DATASOURCE_PATH = "/api/datasources/name/";
	public static final String ADD_DATASOURCE_PATH = "/api/datasources";
	public static final String API_DASHBOARD_PATH = "/api/dashboards/db";
	public static final String SEARCH_DASHBOARD_PATH = "/api/search?query=";
	public static final String API_DASHBOARDS_UID = "/api/dashboards/uid/";
	public static final String API_AUTH_KEYS = "/api/auth/keys";
	public static final String API_MIGRATE = "/api/serviceaccounts/migrate";
	public static final String API_SERVICE_ACCOUNT = "/api/serviceaccounts/search";
	public static final String API_CREATE_SERVICE_ACCOUNT = "/api/serviceaccounts";
	public static final String API_TOKEN = "/api/serviceaccounts/:id/tokens";
	public static final String API_TOKEN_DELETE = "/api/serviceaccounts/:id/tokens/:tokenId";
	public static final String LF = "\n";
	public static final String CR = "\r";
	public static final String TAB = "\t";
	public static final String EMPTY = "";
	public static final String BUILD="build";
	public static final String TRUSTED_HOSTS="trustedHosts";
	public static final String MATCH_N_STRING="match(n:";
	public static final String DATA_PATTERN_STR=":DATA{";
	public static final String ISSUE_TYPE ="issueType";
	public static final String COMPONENT_NAME="componentName";
	public static final String DEPENDENCY="dependency";
	public static final String SUBSCRIBE = "subscribe";
	public static final String VAULT = "vault";
	public static final String RESULT = "result";

}

