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
package com.cognizant.devops.platformservice.traceabilitydashboard.service;

import java.util.List;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;

public interface TraceabilityDashboardService {
	 public JsonObject getPipeline(String toolName,String fieldName,String fieldValue) throws InsightsCustomException;
	 public List<JsonObject> getToolSummary(String toolName, String cacheKey) throws InsightsCustomException;
	 public List<String> getAvailableTools() throws InsightsCustomException;
	 public List<String> getToolKeyset(String toolName) throws InsightsCustomException;   
}
