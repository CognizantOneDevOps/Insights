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

package com.cognizant.devops.platformservice.test.grafanaAuthentication;

import java.nio.charset.Charset;
import org.springframework.http.MediaType;

public class GrafanaAuthenticationTestData {

	public static final String accept = "application/json, text/plain, */*";
	public static final String authorization = "token";

	public static final String invalid_autharization = "token";

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	public static final String CORRECT_PASSWORD = "admin";
	public static final String CORRECT_USERNAME = "admin";

	public static final String INCORRECT_PASSWORD = "password1";
	public static final String INCORRECT_USERNAME = "user1";

	public static final String REQUEST_PARAMETER_PASSWORD = "admin";
	public static final String REQUEST_PARAMETER_USERNAME = "admin";

}