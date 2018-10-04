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
package com.cognizant.devops.platformengine.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

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
 * Engine execution will start from Application. 1. Load the iSight config 2.
 * Initialize Publisher and subscriber modules 3. Initialize Correlation Module.
 */
public class Application {
	private static Logger log = LogManager.getLogger(Application.class.getName());
	
	private static int defaultInterval = 600;
	private Application(){
		
	}
	
	public static void main(String[] args) {
		if(args.length > 0){
			defaultInterval = Integer.valueOf(args[0]);
		}
		try {
			// Load insight configuration
			ApplicationConfigCache.loadConfigCache();
			
			ApplicationConfigProvider.performSystemCheck();
			
			// Subscribe for desired events.
			JobDetail aggrgatorJob = JobBuilder.newJob(EngineAggregatorModule.class)
					.withIdentity("EngineAggregatorModule", "iSight")
					.build();
	
			Trigger aggregatorTrigger = TriggerBuilder.newTrigger()
					.withIdentity("EngineAggregatorModuleTrigger", "iSight")
					.startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInSeconds(defaultInterval)
							.repeatForever())
					.build();
	
			// Schedule the Correlation Module.
			JobDetail correlationJob = JobBuilder.newJob(EngineCorrelatorModule.class)
					.withIdentity("EngineCorrelatorModule", "iSight")
					.build();
	
			Trigger correlationTrigger = TriggerBuilder.newTrigger()
					.withIdentity("EngineCorrelatorModuleTrigger", "iSight")
					.startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInSeconds(defaultInterval)
							.repeatForever())
					.build();
			
			// Schedule the Project Mapping Module.
			JobDetail projectMappingJob = JobBuilder.newJob(ProjectMapperModule.class)
					.withIdentity("ProjectMapperModule", "iSight")
					.build();
	
			Trigger projectMappingTrigger = TriggerBuilder.newTrigger()
					.withIdentity("ProjectMapperModuleTrigger", "iSight")
					.startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInSeconds(defaultInterval)
							.repeatForever())
					.build();
			// Schedule the DataPurging Executor Job
			JobDetail dataPurgingJob = JobBuilder.newJob(DataPurgingExecutor.class)
					.withIdentity("DataPurgingExecutor", "iSight")
					.build();
	
			Trigger dataPurgingTrigger = TriggerBuilder.newTrigger()
					.withIdentity("DataPurgingExecutorTrigger", "iSight")
					.startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInSeconds(defaultInterval)
							.repeatForever())
					.build();
			
			// Schedule the OfflineDataProcessingExecutor job
			JobDetail offlineDataProcessingJob = JobBuilder.newJob(OfflineDataProcessingExecutor.class)
					.withIdentity("OfflineDataProcessingExecutor", "iSight")
					.build();
	
			Trigger offlineDataProcessingTrigger = TriggerBuilder.newTrigger()
					.withIdentity("OfflineDataProcessingExecutorTrigger", "iSight")
					.startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInSeconds(defaultInterval)
							.repeatForever())
					.build();
	
			// Tell quartz to schedule the job using our trigger
			Scheduler scheduler;
		
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			scheduler.scheduleJob(aggrgatorJob, aggregatorTrigger);
			scheduler.scheduleJob(correlationJob, correlationTrigger);
			scheduler.scheduleJob(projectMappingJob, projectMappingTrigger);
			scheduler.scheduleJob(dataPurgingJob, dataPurgingTrigger);
			scheduler.scheduleJob(offlineDataProcessingJob, offlineDataProcessingTrigger);
			EngineStatusLogger.getInstance().createEngineStatusNode("Platform Engine Service Started ",PlatformServiceConstants.SUCCESS);
		} catch (SchedulerException e) {
			EngineStatusLogger.getInstance().createEngineStatusNode("Platform Engine Service not running due to Scheduler Exception "+e.getMessage(),PlatformServiceConstants.FAILURE);
			log.error(e);
		}catch (Exception e) {
			EngineStatusLogger.getInstance().createEngineStatusNode("Platform Engine Service not running "+e.getMessage(),PlatformServiceConstants.FAILURE);
			log.error(e);
		}
	}
	
}
