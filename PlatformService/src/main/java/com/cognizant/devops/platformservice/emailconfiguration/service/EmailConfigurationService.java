/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.emailconfiguration.service;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface EmailConfigurationService {
	
	public JsonArray getAllReportTitles(String source,String userDetails) throws InsightsCustomException;

	public int saveEmailConfig(JsonObject emailConfigJson) throws InsightsCustomException;
	
	public JsonArray getAllGroupEmailConfigurations(String source) throws InsightsCustomException;
	
	public String deleteGroupEmailConfiguration(int configId) throws InsightsCustomException;

	public String updateGroupEmailConfig(JsonObject emailConfigJson) throws InsightsCustomException;
	
	public String updateGroupEmailConfigState(JsonObject updateEmailConfigJson) throws InsightsCustomException;

}
