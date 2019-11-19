/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.rest.health.service;

import java.io.IOException;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.google.gson.JsonObject;

public interface HealthStatusService {

	GraphResponse loadHealthData(String label, String type, String agentId, int limitOfRow);

	JsonObject getComponentStatus(String serviceType, String apiUrl);

	JsonObject getVersionDetails(String fileName, String hostEndPoint, String type) throws IOException;

	JsonObject getClientResponse(String hostEndPoint, String apiUrl, String displayType, String serviceType,
			boolean isRequiredAuthentication, String username, String password, String authToken);

	JsonObject createAgentFailureHealthLabel(String category, String tool, String agentId);

}
