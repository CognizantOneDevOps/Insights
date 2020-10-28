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
package com.cognizant.devops.platformworkflow.workflowtask.core;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;

public class WorkflowAutoCorrectionExecutor implements Job {

	private static final Logger log = LogManager.getLogger(WorkflowAutoCorrectionExecutor.class);
	WorkflowDAL workflowDAL = new WorkflowDAL();
	long adjustedNextRunTime = 0l;
	private static final String LOGMESSAGE = "WorkflowAutoCorrection executor === correction needed on workflowId : {} | frequency {} | "
			+ "old nextruntime: {} | correction nextruntime : {} ";
	private static final String WEEKS = "WEEKS";
	private static final String MONTHS = "MONTHS";
	private static final String YEARS = "YEARS";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		List<InsightsWorkflowConfiguration> activeWorkflowList = workflowDAL.getAllActiveWorkflowConfiguration();

		for (InsightsWorkflowConfiguration eachWorkflow : activeWorkflowList) {

			long currentTime = InsightsUtils.getCurrentTimeInSeconds();
			long nextRunTime = eachWorkflow.getNextRun();

			/*
			 * First check if current time is greater than nextruntime if yes then calculate
			 * correction based on diff between current date and nextruntime based on diff
			 * add specific days based on schedule and update nextruntime
			 */
			if (eachWorkflow.getScheduleType().equalsIgnoreCase(WorkflowTaskEnum.WorkflowSchedule.DAILY.name())) {
				if (currentTime > nextRunTime) {
					long diff = InsightsUtils.getDurationInDays(nextRunTime);
					if (diff >= 1) {
						adjustedNextRunTime = InsightsUtils.addDaysInGivenTime(nextRunTime, diff + 1);
					}

				}
			} else if (eachWorkflow.getScheduleType()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowSchedule.WEEKLY.name())) {

				long diff = InsightsUtils.getDurationInDays(nextRunTime);
				if (diff >= 7) {
					long noOfWeeks = InsightsUtils.getScheduleWiseDuration(nextRunTime, currentTime,
							WorkflowTaskEnum.WorkflowSchedule.WEEKLY.name());
					adjustedNextRunTime = InsightsUtils.addTimeInCurrentTime(nextRunTime, WEEKS, noOfWeeks);

				}
			}

			else if (eachWorkflow.getScheduleType()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowSchedule.BI_WEEKLY_SPRINT.name())) {

				long diff = InsightsUtils.getDurationInDays(nextRunTime);
				if (diff >= 14) {
					long noOfSprints = InsightsUtils.getScheduleWiseDuration(nextRunTime, currentTime,
							WorkflowTaskEnum.WorkflowSchedule.BI_WEEKLY_SPRINT.name()) / 2;
					adjustedNextRunTime = InsightsUtils.addTimeInCurrentTime(nextRunTime, WEEKS, noOfSprints * 2);

				}
			} else if (eachWorkflow.getScheduleType()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowSchedule.TRI_WEEKLY_SPRINT.name())) {

				long diff = InsightsUtils.getDurationInDays(nextRunTime);
				if (diff >= 21) {
					long noOfSprints = InsightsUtils.getScheduleWiseDuration(nextRunTime, currentTime,
							"TRI_WEEKLY_SPRINT") / 3;
					adjustedNextRunTime = InsightsUtils.addTimeInCurrentTime(nextRunTime, WEEKS, noOfSprints * 3);

				}
			} else if (eachWorkflow.getScheduleType()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowSchedule.MONTHLY.name())) {

				long lenghtOfMonth = InsightsUtils.getMonthDays(nextRunTime);
				long diff = InsightsUtils.getDurationInDays(nextRunTime);
				if (diff >= lenghtOfMonth) {
					long noOfMonths = InsightsUtils.getScheduleWiseDuration(nextRunTime, currentTime,
							WorkflowTaskEnum.WorkflowSchedule.MONTHLY.name());
					adjustedNextRunTime = InsightsUtils.addTimeInCurrentTime(nextRunTime, MONTHS, noOfMonths);

				}
			} else if (eachWorkflow.getScheduleType()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowSchedule.QUARTERLY.name())) {

				long lenghtOfQuarter = InsightsUtils.getDaysInQuarter(nextRunTime);
				long diff = InsightsUtils.getDurationInDays(nextRunTime);
				if (diff >= lenghtOfQuarter) {
					long noOfQuarters = InsightsUtils.getScheduleWiseDuration(nextRunTime, currentTime,
							WorkflowTaskEnum.WorkflowSchedule.QUARTERLY.name()) / 3;
					adjustedNextRunTime = InsightsUtils.addTimeInCurrentTime(nextRunTime, MONTHS, noOfQuarters * 3);
				}
			} else if (eachWorkflow.getScheduleType()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowSchedule.YEARLY.name())) {

				long lenghtOfYear = InsightsUtils.getLengthOfYear(nextRunTime);
				long diff = InsightsUtils.getDurationInDays(nextRunTime);
				if (diff >= lenghtOfYear) {
					long noOfYears = InsightsUtils.getScheduleWiseDuration(nextRunTime, currentTime,
							WorkflowTaskEnum.WorkflowSchedule.YEARLY.name());
					adjustedNextRunTime = InsightsUtils.addTimeInCurrentTime(nextRunTime, YEARS, noOfYears);
				}
			}

			if (adjustedNextRunTime != 0) {
				eachWorkflow.setNextRun(adjustedNextRunTime);
				try {
					log.debug(LOGMESSAGE, eachWorkflow.getWorkflowId(), eachWorkflow.getScheduleType(), nextRunTime,
							adjustedNextRunTime);
					adjustedNextRunTime=0;
					workflowDAL.updateWorkflowConfig(eachWorkflow);
					log.debug("WorkflowAutoCorrection executor === correction performed on workflow : {} ",
							eachWorkflow.getWorkflowId());
				} catch (Exception e) {
					log.error(
							"WorkflowAutoCorrection executor === correction failed to update on workflow : {} due to {}",
							eachWorkflow.getWorkflowId(), e.getMessage());
				}
			} else {
				log.debug("WorkflowAutoCorrection executor === no workflows found for correction");
			}

		}

	}

}
