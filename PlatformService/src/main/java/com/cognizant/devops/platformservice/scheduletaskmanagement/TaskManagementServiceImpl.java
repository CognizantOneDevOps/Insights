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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.core.enums.SchedularTaskEnum.SchedularTaskAction;
import com.cognizant.devops.platformcommons.core.enums.SchedularTaskEnum.TaskDefinitionProperty;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDAL;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDefinition;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskStatus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Service("taskManagementServiceI")
public class TaskManagementServiceImpl implements TaskManagementService  {
	
	private static final Logger log = LogManager.getLogger(TaskManagementServiceImpl.class);
	InsightsSchedulerTaskDAL scheduleTaskDAL = new InsightsSchedulerTaskDAL();
	Gson gson = new Gson();
	JsonParser parser = new JsonParser();
	
	/** This method fetch all Schedule Task Definition
	 * @return
	 * @throws InsightsCustomException
	 */
	@Override
	public List<JsonObject> getAllScheduleTaskDetail() {
		List<JsonObject> taskList = new ArrayList<>();
		
		try {
			List<InsightsSchedulerTaskDefinition> taskListfromDB=scheduleTaskDAL.getAllSchedulerTaskConfigurations();
			List<Object[]> records = scheduleTaskDAL.getTaskLastRunTime();
			Map<String, Long> taskStatus = new HashMap<>(0);
			records.stream().forEach(record -> {
				taskStatus.put(String.valueOf(record[1]),(long) record[0]);
			});
			taskListfromDB.forEach(taskDefintion -> {
				JsonObject taskJsonObj = parser.parse(gson.toJson(taskDefintion)).getAsJsonObject();
				taskJsonObj.addProperty("lastrun", taskStatus.get(taskDefintion.getComponentName()));
				taskList.add(taskJsonObj);
			});
			
		} catch (Exception e) {
			log.error(" Error while loding loadServerConfigDetail ",e);
		}
		return taskList;
	}
	

	/** use to get all Task History Detail
	 * @param validatedTaskJson
	 * @return
	 */
	@Override
	public List<InsightsSchedulerTaskStatus> getScheduleTaskHistoryDetail(JsonObject validatedTaskJson) {
		List<InsightsSchedulerTaskStatus> taskList = new ArrayList<>();
		
		try {
			taskList=scheduleTaskDAL.getSchedulerTaskHistoryConfigurations(validatedTaskJson.get(TaskDefinitionProperty.COMPONENTNAME.getValue()).getAsString());
		} catch (Exception e) {
			log.error(" Error while loding loadServerConfigDetail ",e);
		}
		return taskList;
	}

	/** Method use to save Task Definition
	 * @param validatedTaskJson
	 * @throws Exception 
	 */
	@Override
	public void saveOrEditTaskDefinition(JsonObject validatedTaskJson) throws Exception {
		try {
			String componentName = validatedTaskJson.get(TaskDefinitionProperty.COMPONENTNAME.getValue()).getAsString();
			String componentClassPath = validatedTaskJson.get(TaskDefinitionProperty.COMPONENTCLASSDETAIL.getValue()).getAsString();
			String schedule = validatedTaskJson.get(TaskDefinitionProperty.SCHEDULE.getValue()).getAsString();
			if(!org.quartz.CronExpression.isValidExpression(schedule)) {
				throw new InsightsCustomException("Cron Expression is not valid, Plese check scheduled part");
			}
			InsightsSchedulerTaskDefinition saveSchedulerTaskDefinition = scheduleTaskDAL.getSchedulerTaskDefinitionBytaskId(componentName);
			if(saveSchedulerTaskDefinition==null) {
				saveSchedulerTaskDefinition= new InsightsSchedulerTaskDefinition();
				saveSchedulerTaskDefinition.setAction(SchedularTaskAction.NOT_STARTED.toString());
			}else {
				saveSchedulerTaskDefinition.setAction(SchedularTaskAction.RESCHEDULE.toString());
			}
			
			saveSchedulerTaskDefinition.setComponentClassDetail(componentClassPath);
			saveSchedulerTaskDefinition.setComponentName(componentName);
			saveSchedulerTaskDefinition.setSchedule(schedule);
			
			scheduleTaskDAL.saveOrUpdateSchedulerTaskConfiguration(saveSchedulerTaskDefinition);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	/** Method use to update Task Definition status 
	 * @param validatedTaskJson
	 * @throws InsightsCustomException
	 */
	@Override
	public void statusUpdateTaskDefinition(JsonObject validatedTaskJson) throws InsightsCustomException  {
		try {
			String componentName = validatedTaskJson.get(TaskDefinitionProperty.COMPONENTNAME.getValue()).getAsString();
			String action = validatedTaskJson.get(TaskDefinitionProperty.ACTION.getValue()).getAsString();
			InsightsSchedulerTaskDefinition saveSchedulerTaskDefinition = scheduleTaskDAL.getSchedulerTaskDefinitionBytaskId(componentName);
			if(saveSchedulerTaskDefinition==null) {
				throw new InsightsCustomException("Record not found");
			}
			saveSchedulerTaskDefinition.setAction(action);
			
			scheduleTaskDAL.saveOrUpdateSchedulerTaskConfiguration(saveSchedulerTaskDefinition);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	/** Method use to delete Task Definition
	 * @param validatedTaskJson
	 * @throws InsightsCustomException
	 */
	@Override
	public void deleteTaskDefinition(JsonObject validatedTaskJson) throws InsightsCustomException {
		try {
			String componentName = validatedTaskJson.get(TaskDefinitionProperty.COMPONENTNAME.getValue()).getAsString();
			InsightsSchedulerTaskDefinition saveSchedulerTaskDefinition = scheduleTaskDAL.getSchedulerTaskDefinitionBytaskId(componentName);
			if(saveSchedulerTaskDefinition==null) {
				throw new InsightsCustomException("Record not found");
			}
			int historydelete= scheduleTaskDAL.deleteSchedulerTaskHistoryConfigurations(saveSchedulerTaskDefinition.getComponentName());
			log.debug(" Deleted no of history record {} for componenct name {}  ",historydelete,saveSchedulerTaskDefinition.getComponentName());
			scheduleTaskDAL.delete(saveSchedulerTaskDefinition);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	
}
