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
package com.cognizant.devops.platforminsights.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.insightsemail.job.AlertEmailJobExecutor;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platforminsights.core.avg.AverageActionImpl;
import com.cognizant.devops.platforminsights.core.count.CountActionImpl;
import com.cognizant.devops.platforminsights.core.job.config.SparkJobConfigHandler;
import com.cognizant.devops.platforminsights.core.job.config.SparkJobConfiguration;
import com.cognizant.devops.platforminsights.core.minmax.MinMaxActionImpl;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsSparkJobFailedException;

/**
 * 
 * @author 146414
 * Framework entry point for the InSights KPI's
 * 
 */
public class SparkJobExecutor implements Job,Serializable{
	/**
	 * 
	 */
	private static final Logger log = Logger.getLogger(SparkJobExecutor.class);
	private static final long serialVersionUID = -4343203105485076004L;
	public void execute(JobExecutionContext context) {
		try {
			startExecution();
			InsightsStatusProvider.getInstance().createInsightStatusNode("Platform Insights Spark Application started Successfully", PlatformServiceConstants.SUCCESS);
		} catch (JobExecutionException e) {
			InsightsStatusProvider.getInstance().createInsightStatusNode("message Exception occur while Job Execution "+e.getMessage(), PlatformServiceConstants.FAILURE);
		}
	}
	
	private void startExecution() throws JobExecutionException {
		log.debug("Starting Spark Jobs Execution");
		//ApplicationConfigCache.loadConfigCache();
		SparkJobConfigHandler configHandler = new SparkJobConfigHandler();
		try {
			List<SparkJobConfiguration> jobs = configHandler.loadJobsFromES();
			
			log.debug("Counting the number of Spark jobs. Count:  "+jobs.size());
			List<SparkJobConfiguration> updatedJobs = new ArrayList<>();
			
				for(SparkJobConfiguration job : jobs){
					try {
						if(!(job.isActive() && isJobScheduledToRun(job.getLastRunTime(),job.getSchedule()))) {
							continue;
						}
						executeJob(job);
						configHandler.updateNextRun(job);
						job.setLastRunTime(InsightsUtils.getLastRunTime(job.getSchedule()));
						updatedJobs.add(job);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
				if(updatedJobs.size() > 0) {
					if(ApplicationConfigProvider.getInstance().getEmailConfiguration().getSendEmailEnabled()) {
						sendEmail();
					}
					configHandler.updateJobsInES(jobs);
				}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobExecutionException("Platform Insights Spark Application not started "+e.getMessage());
			//InsightsStatusProvider.createInsightStatusNode("Platform Insights Spark Application not started "+e.getMessage(), PlatformServiceConstants.FAILURE);
		}
	}
	
	private void sendEmail() {
		log.debug("Sending email from SparkJobExecutor...");
		AlertEmailJobExecutor exe=new AlertEmailJobExecutor();
		exe.sendMail();
	}

	private void executeJob(SparkJobConfiguration job)  throws InsightsSparkJobFailedException{
		
		KPIDefinition kpiDefinition = job.getKpiDefinition(job.getKpiDefinition());
		
		if(ExecutionActions.AVERAGE == kpiDefinition.getAction()){
			log.debug("KPI action found as AVERAGE");
			BaseActionImpl impl = new AverageActionImpl(kpiDefinition);
			impl.execute();
		} else if(ExecutionActions.COUNT == kpiDefinition.getAction()){
			log.debug("KPI action found as COUNT");
			BaseActionImpl impl = new CountActionImpl(kpiDefinition);
			impl.execute();
		}
		else if(ExecutionActions.MINMAX == kpiDefinition.getAction()){
			log.debug("KPI action found as MINMAX");
			BaseActionImpl impl = new MinMaxActionImpl(kpiDefinition);
			impl.execute();
		}
	}
	
	private boolean isJobScheduledToRun(Long lastRun, String jobSchedule) {
		
		Long lastRunSinceDays = InsightsUtils.getDurationBetweenDatesInDays(lastRun);
		return InsightsUtils.isAfterRange(jobSchedule, lastRunSinceDays);
	}

}
