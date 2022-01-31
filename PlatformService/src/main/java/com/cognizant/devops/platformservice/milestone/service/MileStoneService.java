/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.milestone.service;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface MileStoneService {
	public int saveMileStoneConfig(JsonObject config) throws InsightsCustomException;

	public JsonArray getMileStoneConfigs() throws InsightsCustomException;

	public void updateMileStoneConfig(JsonObject configJson) throws InsightsCustomException;

	public String deleteMileStoneDetails(int id) throws InsightsCustomException;

	public JsonArray fetchOutcomeConfig() throws InsightsCustomException;

	public Boolean restartMileStoneConfig(JsonObject configJson) throws InsightsCustomException;
}
