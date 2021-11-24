/*********************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
 *******************************************************************************/
package com.cognizant.devops.platformservice.scheduletaskmanagement;

import java.util.List;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskStatus;
import com.google.gson.JsonObject;

public interface TaskManagementService {

	List<JsonObject> getAllScheduleTaskDetail();

	List<InsightsSchedulerTaskStatus> getScheduleTaskHistoryDetail(JsonObject validatedTaskJson);

	void saveOrEditTaskDefinition(JsonObject validatedTaskJson) throws Exception;

	void statusUpdateTaskDefinition(JsonObject validatedTaskJson) throws InsightsCustomException;

	void deleteTaskDefinition(JsonObject validatedTaskJson) throws InsightsCustomException;

}
