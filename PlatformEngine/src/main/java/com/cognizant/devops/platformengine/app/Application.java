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
package com.cognizant.devops.platformengine.app;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.platformengine.modules.correlation.EngineCorrelatorModule;
import com.cognizant.devops.platformengine.modules.datapurging.DataPurgingExecutor;
import com.cognizant.devops.platformengine.modules.mapper.ProjectMapperModule;
import com.cognizant.devops.platformengine.modules.offlinedataprocessing.OfflineDataProcessingExecutor;

/**
 * Engine execution will start from Application.
 * 1. Load the iSight config
 * 2. Initialize Publisher and subscriber modules which receive data from tool
 * 3. Initialize Correlation Module.
 * 4. Initialize ProjectMapperModule Module
 * 5. Initialize DataPurgingExecutor Module
 * 6. Initialize OfflineDataProcessingExecutor Module
 * 7. Log Engine Health data in DB
 */
public class Application {
	private static Logger log = LogManager.getLogger(Application.class.getName());

	private static int defaultIntervalInSec = 600;

	private Application() {

	}

	public static void main(String[] args) {
		if (args.length > 0) {
			defaultIntervalInSec = Integer.valueOf(args[0]);
		}
		try {
			// Load insight configuration
			ApplicationConfigCache.loadConfigCache();

			ApplicationConfigProvider.performSystemCheck();

			// Subscribe for desired events. This used to consume data from tool queue
			Timer timerEngineAggregator = new Timer("EngineAggregatorModule");
			TimerTask engineAggregatorModuleTrigger = new EngineAggregatorModule();
			timerEngineAggregator.schedule(engineAggregatorModuleTrigger, 0, defaultIntervalInSec * 1000);

			// Schedule the Correlation Module.
			Timer timerEngineCorrelatorModule = new Timer("EngineCorrelatorModule");
			TimerTask engineCorrelatorModuleTrigger = new EngineCorrelatorModule();
			timerEngineCorrelatorModule.schedule(engineCorrelatorModuleTrigger, 0, defaultIntervalInSec * 1000);

			// Schedule the Project Mapping Module.
			Timer timerProjectMapperModule = new Timer("ProjectMapperModule");
			TimerTask projectMapperModuleTrigger = new ProjectMapperModule();
			timerProjectMapperModule.schedule(projectMapperModuleTrigger, 0, defaultIntervalInSec * 1000);

			// Schedule the DataPurging Executor Job
			Timer timerDataPurgingExecutor = new Timer("DataPurgingExecutor");
			TimerTask dataPurgingExecutorTrigger = new DataPurgingExecutor();
			timerDataPurgingExecutor.schedule(dataPurgingExecutorTrigger, 0, defaultIntervalInSec * 1000);

			// Schedule the OfflineDataProcessingExecutor job
			Timer timerOfflineDataProcessingExecutor = new Timer("OfflineDataProcessingExecutor");
			TimerTask offlineDataProcessingExecutorTrigger = new OfflineDataProcessingExecutor();
			timerOfflineDataProcessingExecutor.schedule(offlineDataProcessingExecutorTrigger, 0,
					defaultIntervalInSec * 1000);

			EngineStatusLogger.getInstance().createEngineStatusNode("Platform Engine Service Started ",
					PlatformServiceConstants.SUCCESS);
		} catch (Exception e) {
			EngineStatusLogger.getInstance().createEngineStatusNode(
					"Platform Engine Service not running " + e.getMessage(), PlatformServiceConstants.FAILURE);
			log.error(e);
		}
	}

}
