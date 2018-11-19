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
package com.cognizant.devops.platforminsights.app;

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
import com.cognizant.devops.platforminsights.core.InsightsStatusProvider;
import com.cognizant.devops.platforminsights.core.SparkJobExecutor;

/**
 * Engine execution will start from Application. 1. Load the iSight config 2.
 * Initialize Publisher and subscriber modules 3. Initialize Correlation Module.
 */
public class PlatformInsightsSparkApplication {
	private static Logger log = LogManager.getLogger(PlatformInsightsSparkApplication.class);
	
	private static int defaultInterval = 600;
	private PlatformInsightsSparkApplication(){
		
	}
	
	public static void main(String[] args) {
		if(args.length > 0){
			defaultInterval = Integer.valueOf(args[0]);
		}

		// Load isight config
		ApplicationConfigCache.loadConfigCache();
		// Create Default users
		ApplicationConfigProvider.performSystemCheck();
				
		// Subscribe for desired events.
		JobDetail sparkAggrgatorJob = JobBuilder.newJob(SparkJobExecutor.class)
				.withIdentity("SparkJobExecutorModule", "iSightSpark")
				.build();

		Trigger sparkAggregatorTrigger = TriggerBuilder.newTrigger()
				.withIdentity("SparkJobExecutorModuleTrigger", "iSightSpark")
				.startNow()
				.withSchedule(SimpleScheduleBuilder.simpleSchedule()
						.withIntervalInSeconds(defaultInterval)
						.repeatForever())
				.build();

		// Tell quartz to schedule the job using our trigger
		Scheduler scheduler;
		try {
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			scheduler.scheduleJob(sparkAggrgatorJob, sparkAggregatorTrigger);
			log.debug("Job has been scheduled with interval of - "+defaultInterval);
			//Insight status to DB
		} catch (SchedulerException e) {
			log.error("Exception in Sparkjob schedular",e);
			InsightsStatusProvider.getInstance().createInsightStatusNode("Platform Insights Spark Application not started ", PlatformServiceConstants.FAILURE);
		}catch (Exception e) {
			log.error("Exception in Sparkjob ",e);
			InsightsStatusProvider.getInstance().createInsightStatusNode("Platform Insights Spark Application not started ", PlatformServiceConstants.FAILURE);
		}
	}
}
