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

import com.cognizant.devops.insightsemail.core.util.AlertEmailJobExecutor;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;


public class InsightsEmailTriggerApplication {
	private static Logger log = Logger.getLogger(InsightsEmailTriggerApplication.class);
	private static int defaultInterval = 600;

	public static void main(String[] args) {
		
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
		
		
	}



}
