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
package com.cognizant.devops.platformservice.scheduletaskmanagement;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskStatus;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/scheduletaskmanagement")
public class TaskManagementController {
	private static Logger log = LogManager.getLogger(TaskManagementController.class);
	
	@Autowired
	TaskManagementService taskManagementServiceI;

	/** API will return all Schedule Task
	 * @return List of Task Definition 
	 */
	@GetMapping(value = "/getAllTaskDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getAllTaskDetail() {
		try {
			List<JsonObject> taskList = taskManagementServiceI.getAllScheduleTaskDetail();
			return PlatformServiceUtil.buildSuccessResponseWithData(taskList);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
	/** Get all Task Status History Detail
	 * @param taskName use to filter task status History  
	 * @return list of Task Status history
	 */
	@PostMapping(value = "/getTaskHistoryDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getScheduleTaskHistoryDetail(@RequestBody String taskrequestJson) {
		List<InsightsSchedulerTaskStatus> records = null;
		try {
			JsonObject validatedConfigIdJson = validateRequestString(taskrequestJson);
			records = taskManagementServiceI.getScheduleTaskHistoryDetail(validatedConfigIdJson);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(records);
	}

	/** This API use to save Task Definition
	 * @param mandatory API field  for TaskDefinition
	 * @return success and failure message once task registered 
	 */
	@PostMapping(value = "/saveOrEditTaskDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveTaskDefinition(@RequestBody String taskrequestJson) {
		try {
			JsonObject validatedConfigIdJson = validateRequestString(taskrequestJson);
			taskManagementServiceI.saveOrEditTaskDefinition(validatedConfigIdJson);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}
	
	/** API to use update status Task Definition
	 * @param json string contain component name and status to update
	 * @return success and failure message once task updated
	 */
	@PostMapping(value = "/statusUpdateForTaskDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject statusUpdateTaskDefinition(@RequestBody String taskrequestJson) {
		try {
			JsonObject validatedConfigIdJson = validateRequestString(taskrequestJson);
			taskManagementServiceI.statusUpdateTaskDefinition(validatedConfigIdJson);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}
	
	/** API to use delete Task Definition
	 * @param json string contain component name for delete 
	 * @return success and failure message once task updated
	 */
	@PostMapping(value = "/deleteTaskDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteTaskDefinition(@RequestBody String taskrequestJson) {
		try {
			JsonObject validatedConfigIdJson = validateRequestString(taskrequestJson);
			taskManagementServiceI.deleteTaskDefinition(validatedConfigIdJson);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}
	
	/**Use to validate Request Json String
	 * @param Request Json String 
	 * @return JsonObject representation request string
	 */
	private JsonObject validateRequestString(String taskrequestJson) {
		taskrequestJson = taskrequestJson.replace("\n", "").replace("\r", "");
		String validatedRequestJson = ValidationUtils.validateRequestBody(taskrequestJson);
		return JsonUtils.parseStringAsJsonObject(validatedRequestJson);
	}
}
