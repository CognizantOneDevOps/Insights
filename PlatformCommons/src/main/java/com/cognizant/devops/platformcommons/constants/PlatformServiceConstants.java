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

public interface PlatformServiceConstants {
	String STATUS = "status";
	String SUCCESS = "success";
	String FAILURE = "failure";
	String MESSAGE = "message";
	String DATA = "data";
	String INVALID_REQUEST = "Invalid request.";
	String INVALID_REQUEST_BODY = "Invalid request,Please check Request Body Or Request Payload of API.";
	String INVALID_RESPONSE_DATA_HTML = "Invalid response data,Response might be contain some Html tag.";
	String INVALID_RESPONSE_DATA = "Invalid response data while parsing response data.";
	String INVALID_REQUEST_ORIGIN = " UnknownHostException :Please make sure that your host is in trusted host list.";
	String HOST_NOT_FOUND = "UnknownHostException : Unable to find valid host information. ";
	String INVALID_FILE = "Invalid file";
	String INVALID_TOKEN = "Invalid Autharization Token";
	String TRANSFORMATION_DECODED = new String(
			Base64.getDecoder().decode(ConfigOptions.TRANSFORMATION_ENCODED.getBytes()));
	String SP_DECODED = new String(Base64.getDecoder().decode(ConfigOptions.SP_ENCODED.getBytes()));
	String RSA_DECODED = new String(Base64.getDecoder().decode(ConfigOptions.RSA_ENCODED.getBytes()));
	String GRAFANA_LOGIN_ISSUE = "Unable to connect to Grafana";
	String INCORRECT_RESPONSE_TEMPLATE = "Incorrect Response Template";
	String WEBHOOK_NAME = "Webhook name already exists";
	String INSIGHTSTIME = "inSightsTime";
	String INSIGHTSTIMEX = "inSightsTimeX";
}
