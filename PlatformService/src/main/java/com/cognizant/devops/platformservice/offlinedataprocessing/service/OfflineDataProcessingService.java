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
package com.cognizant.devops.platformservice.offlinedataprocessing.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.google.gson.JsonObject;

public interface OfflineDataProcessingService {

	public String saveOfflineDataInDatabase(MultipartFile file) throws InsightsCustomException;

	public String saveOfflineDefinition(JsonObject registerOfflineJson) throws InsightsCustomException;

	public String updateOfflineDefinition(JsonObject updateOfflineJson) throws InsightsCustomException;

	public void updateOfflineConfigStatus(JsonObject offlineConfigJson) throws InsightsCustomException;

	public boolean deleteOfflineDefinition(JsonObject deleteOfflineJson) throws InsightsCustomException;

	public List<InsightsOfflineConfig> getOfflineDataList() throws InsightsCustomException;
}
