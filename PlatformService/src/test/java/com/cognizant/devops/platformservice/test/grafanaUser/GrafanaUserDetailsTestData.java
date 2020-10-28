/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.grafanaUser;

import javax.servlet.http.Cookie;

public class GrafanaUserDetailsTestData {

	Cookie[] cookies = { new Cookie("JSESSIONID", ""), new Cookie("grafanaOrg", "1"),
			new Cookie("grafanaRole", "Admin"), new Cookie("XSRF-TOKEN", "") };

	String accept = "application/json, text/plain, */*";
	String authorization = "";
	String origin = "http://localhost:8181";
	String referer = "http://localhost:8181/app";
	String contentType = "application/json";
	String XSRFTOKEN = "";

	String authorizationException = "";
	String host = "insights.cogdevops.com";
	String contentTypeException = "application/json";
}
