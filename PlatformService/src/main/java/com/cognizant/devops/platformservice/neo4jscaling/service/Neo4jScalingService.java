/*******************************************************************************
 * Copyright 2024 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.neo4jscaling.service;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface Neo4jScalingService {

	public JsonObject getNeo4jScalingConfigs() throws InsightsCustomException;
	
	public JsonArray getAllReplicas() throws InsightsCustomException;
	
	public String saveNeo4jScalingConfigs(JsonObject sourceConfigJson, JsonObject replicaConfigJson) throws InsightsCustomException;
	
	public String deleteReplica(String replicaName) throws InsightsCustomException;

	public String resyncAll() throws InsightsCustomException;

	public JsonObject getNeo4jScalingLogDetails()throws InsightsCustomException;
}
