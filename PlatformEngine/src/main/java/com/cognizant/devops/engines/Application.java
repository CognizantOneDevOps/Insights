/*******************************************************************************
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
 ******************************************************************************/
package com.cognizant.devops.engines;

import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.constants.LogLevelConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;

/**
 * Engine execution will start from Application. 1. Load the iSight config 2.
 * Initialize Publisher and subscriber modules which receive data from tool 3.
 * Initialize Correlation Module. 4. Initialize ProjectMapperModule Module  6. Initialize
 * OfflineDataProcessingExecutor Module 7. Log Engine Health data in DB
 */
public class Application implements ApplicationConfigInterface {

	private static Logger log = LogManager.getLogger(Application.class.getName());
	
	public static final String JOBCORN ="0 */20 * ? * *";
	public static final String CLEANUP_JOBCORN ="0 0,45 23 ? * * *";
	
	public static void main(String[] args) {

		try {

			ApplicationConfigInterface.loadConfiguration();

			ApplicationConfigCache.updateLogLevel(LogLevelConstants.PLATFORMENGINE);
			
			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			
			JobDetail jobInitializer = JobBuilder.newJob(ScheduledJobExecutor.class)
			.withIdentity("ScheduledJobExecutor","EngineScheduledJobExecutorGroup").build();

			CronTrigger jobInitializerTrigger = TriggerBuilder.newTrigger()
					.withIdentity("ScheduledJobExecutorTrigger", "EngineScheduledJobExecutorGroup")
					.startNow().withSchedule(CronScheduleBuilder.cronSchedule(JOBCORN).inTimeZone(TimeZone.getTimeZone("UTC")))
					.build();
			
			log.debug("Engine Job Detail ==== jobInitializer {} ==== {} ",jobInitializer.getKey(),JOBCORN);
			
			JobDetail recordCleanUpJob = JobBuilder.newJob(CleanUpJobExecutor.class)
			.withIdentity("HistoryRecordCleanUpJob","HistoryRecordCleanUpJobGroup").build();

			CronTrigger recordCleanUpJobTrigger = TriggerBuilder.newTrigger()
					.withIdentity("HistoryRecordCleanUpJobTrigger", "HistoryRecordCleanUpJobGroup")
					.startNow().withSchedule(CronScheduleBuilder.cronSchedule(CLEANUP_JOBCORN).inTimeZone(TimeZone.getTimeZone("UTC")))
					.build();
			
			log.debug("Engine Job Detail ==== jobInitializer {} ==== {} ",recordCleanUpJob.getKey(),CLEANUP_JOBCORN);
			
			scheduler.start();
			scheduler.scheduleJob(jobInitializer, jobInitializerTrigger);
			scheduler.scheduleJob(recordCleanUpJob, recordCleanUpJobTrigger);
			
			scheduler.triggerJob(jobInitializer.getKey());
			
			EngineStatusLogger.getInstance().createEngineStatusNode(
					"Platform Engine Service started successfully", PlatformServiceConstants.SUCCESS);
			
		} catch (Exception e) {
			EngineStatusLogger.getInstance().createEngineStatusNode(
					"Platform Engine Service not running " + e.getMessage(), PlatformServiceConstants.FAILURE);
			log.error(e);
		}
	}
}
