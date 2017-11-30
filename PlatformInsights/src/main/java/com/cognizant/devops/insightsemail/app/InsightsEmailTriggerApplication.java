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

package com.cognizant.devops.insightsemail.app;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.cognizant.devops.insightsemail.job.AlertEmailJobExecutor;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;


public class InsightsEmailTriggerApplication {
	private static Logger log = Logger.getLogger(InsightsEmailTriggerApplication.class);
	private static int defaultInterval = 600;

	/*public static void main(String[] args) {
		
		Scheduler scheduler;
		ApplicationConfigCache.loadConfigCache();
		ApplicationConfigProvider.performSystemCheck();
		
		 JobDetail emailJob = JobBuilder.newJob(AlertEmailJobExecutor.class)
				                 .withIdentity("Emailexecutor", "insightsemail")
				                 .build();

		 Trigger emailTrigger = TriggerBuilder.newTrigger()
					.withIdentity("EmailexecutorTrigger", "insightsemail")
					.startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
								.withIntervalInSeconds(defaultInterval)
								.repeatForever())
								.build();
		
			try {
				scheduler = new StdSchedulerFactory().getScheduler();
				scheduler.start();
				scheduler.scheduleJob(emailJob, emailTrigger);
			} catch (SchedulerException e) {
				log.error("Exception in email scheduler:",e);
			}
		
		
	}*/



}
