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
package com.cognizant.devops.engines.platformengine.message.core;


import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformdal.healthutil.InsightsAgentHealthDetailDAL;
import com.cognizant.devops.platformdal.healthutil.InsightsAgentHealthDetails;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDAL;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskStatus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformdal.healthutil.HealthUtil;




public class EngineStatusLogger extends ComponentHealthLogger {
	private static Logger log = LogManager.getLogger(EngineStatusLogger.class);
	static EngineStatusLogger instance = null;
	InsightsSchedulerTaskDAL schedularTaskDAL = new InsightsSchedulerTaskDAL();
	InsightsAgentHealthDetailDAL agentHealthDetailDAL= new InsightsAgentHealthDetailDAL();
	HealthUtil healthUtil = new HealthUtil();
	
	private EngineStatusLogger() {

	}

	public static EngineStatusLogger getInstance() {
		if (instance == null) {
			instance = new EngineStatusLogger();
		}
		return instance;
	}

	public boolean createEngineStatusNode(String message, String status) {
		try {
			String version = "";
			version = EngineStatusLogger.class.getPackage().getImplementationVersion();
			log.debug(" Engine version for createComponentHealthDetails{} ", version);
			//Saves Engine Health details into the Component Health Details table
			healthUtil.createComponentHealthDetails(ServiceStatusConstants.PLATFORM_ENGINE, version, message, status);			
		} catch (Exception e) {
			log.error("Unable to insert Engine Component Health details {} ", e.getMessage());
		}
		return Boolean.TRUE;
	}
	
		
	public void createSchedularTaskStatusNode(String message, String status, String timerTaskMapping) {
		createSchedularTaskStatusNode(message,status,timerTaskMapping,0);
	}
	
	public void createSchedularTaskStatusNode(String message, String status, String timerTaskMapping, long processingTime) {
		try {
			String version = EngineStatusLogger.class.getPackage().getImplementationVersion();
			
			InsightsSchedulerTaskStatus schedularTaskSatus = new InsightsSchedulerTaskStatus();
			schedularTaskSatus.setMessage(message);
			schedularTaskSatus.setStatus(status);
			schedularTaskSatus.setVersion(version == null ? "NA" : version);
			schedularTaskSatus.setTimerTaskMapping(timerTaskMapping);
			schedularTaskSatus.setRecordtimestamp(System.currentTimeMillis());
			schedularTaskSatus.setRecordtimestampX(InsightsUtils.insightsTimeXFormat(System.currentTimeMillis()));
			schedularTaskSatus.setProcessingTime(processingTime);
			
			schedularTaskDAL.saveOrUpdateSchedulerTaskStatus(schedularTaskSatus);
			
		} catch (Exception e) {
			log.error(" Unable to create InsightsSchedulerTaskStatus node {} ", e.getMessage());
			log.error(e);
		}
	}
	
	
	/**
	 * 
	 * Gets agent health details properties from JsonObject received from Rabbit MQ health queue
	 * @param jsonElement
	 * @param labels
	 */
	public void extractAndStoreHealthRecord(JsonElement jsonElement,List<String> labels) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String agentId =  JsonUtils.getValueFromJson(jsonElement, AgentCommonConstant.AGENTID);   
        String categoryName =  "";
        String toolName =  "";
        String status =  JsonUtils.getValueFromJson(jsonElement, "status");
        String execId =  JsonUtils.getValueFromJson(jsonElement, "execId");
        String message =  JsonUtils.getValueFromJson(jsonElement, "message");
        Long inSightsTime = Long.valueOf(String.valueOf(jsonObject.get("inSightsTime")));			
        if (jsonObject.has(AgentCommonConstant.TOOLNAME)) {    
            toolName = jsonObject.get(AgentCommonConstant.TOOLNAME).getAsString();
            categoryName = JsonUtils.getValueFromJson(jsonElement, "categoryName");
        }    
        else {
            if (labels.size() > 1) {
                categoryName = labels.get(0);
                toolName = labels.get(1);
            }
        }                        
        populateAgentHealthDetail(agentId,categoryName,toolName,status,
                execId,message,inSightsTime);
    }
	
	
	/**
	 * 
	 * Populates Agent Health Detail object and save them into the database
	 * 
	 * @param agentId
	 * @param categoryName
	 * @param toolName
	 * @param status
	 * @param execId
	 * @param message
	 * @param inSightsTime
	 */
	public void populateAgentHealthDetail(String agentId,String categoryName,String toolName,
			String status, String execId,String message,Long inSightsTime) {		
		InsightsAgentHealthDetails agentHealthDetail = new InsightsAgentHealthDetails();		
		agentHealthDetail.setAgentId(agentId);
		agentHealthDetail.setToolName(toolName);
		agentHealthDetail.setCategoryName(categoryName);
		agentHealthDetail.setStatus(status);
		agentHealthDetail.setExecId(execId);
		agentHealthDetail.setMessage(message);
		agentHealthDetail.setInSightsTime(inSightsTime);
		
		agentHealthDetailDAL.saveAgentHealthDetail(agentHealthDetail);
	}
	
	
	
}
