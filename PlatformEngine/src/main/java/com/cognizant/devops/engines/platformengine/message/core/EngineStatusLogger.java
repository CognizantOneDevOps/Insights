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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDAL;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskStatus;

public class EngineStatusLogger extends ComponentHealthLogger {
	private static Logger log = LogManager.getLogger(EngineStatusLogger.class);
	static EngineStatusLogger instance = null;
	InsightsSchedulerTaskDAL schedularTaskDAL = new InsightsSchedulerTaskDAL();

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
			log.debug(" Engine version for createEngineStatusNode {} ", version);
			Map<String, String> extraParameter = new HashMap<>(0);
			if (isDBUpdateSafe()) {
				createComponentStatusNode("HEALTH:ENGINE", version, message, status, extraParameter);
			}
		} catch (Exception e) {
			log.error(" Unable to create node {} ", e.getMessage());
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

	private boolean isDBUpdateSafe() {
		return ApplicationConfigProvider.getInstance().getGraph().getAuthToken() != null
				&& !ApplicationConfigProvider.getInstance().getGraph().getAuthToken().equals("");
	}
}
