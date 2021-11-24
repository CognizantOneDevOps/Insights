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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.SchedularTaskEnum.SchedularTaskAction;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDAL;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDefinition;

public class ScheduledJobExecutor implements Job, ApplicationConfigInterface {

	private static final Logger log = LogManager.getLogger(ScheduledJobExecutor.class);
	InsightsSchedulerTaskDAL schedularTaskDAL = new InsightsSchedulerTaskDAL();
	static Map<Integer, JobDetail> registry = new HashMap<>(0);
	Scheduler scheduler;

	/** <p>
     * Called by the  Scheduler when a  Trigger fires that is associated with the Job.
     * </p> This is Override method use for execution
	 * @param JobExecutionContext sent by Job Scheduler
	 * @throws JobExecutionException
     *           if there is an exception while executing the job.
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug(" Engine Scheduled Job ==== Inside Schedular Job Executor ");
		try {
			scheduler = context.getScheduler();
			ApplicationConfigInterface.loadConfiguration();
			initializeEngineScheduledTask();
		} catch (InsightsCustomException e) {
			log.error(" Engine Scheduled Job ==== Error ====  {}", e.getMessage());
			log.error(e);
		}
	}

	/** This method is responsible to create job definition, job definition fetch from database. 
	 * 
	 */
	public synchronized void initializeEngineScheduledTask() {
		long startTime = System.currentTimeMillis();

		try {
			List<InsightsSchedulerTaskDefinition> scheduledTaskDefinitionList = schedularTaskDAL
					.getAllSchedulerTaskConfigurations();

			for (InsightsSchedulerTaskDefinition schedulerTaskDef : scheduledTaskDefinitionList) {
				registerSchedularTask(schedulerTaskDef);
			}
		} catch (Exception e) {
			log.error(" Engine Scheduled Job ==== Error while registering Task ", e);
			log.error(e);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					" Error while registering Task " + e.getMessage(), PlatformServiceConstants.FAILURE);
		} finally {
			long processingTime = System.currentTimeMillis() - startTime;
			log.debug("Type=ServiceInitialization Engine Scheduled Job Initializer completed with Processing Time {} " , processingTime);
		}
	}

	/** This method is use to register task, It will create/rescheduled/delete task according to status
	 * @param schedulerTaskDef
	 */
	private void registerSchedularTask(InsightsSchedulerTaskDefinition schedulerTaskDef) {
		try {
			String jobkey = schedulerTaskDef.getComponentName();
			JobDetail currentRegistaryJobDetail = registry.get(schedulerTaskDef.getTimerTaskId());

			if (schedulerTaskDef.getAction().equalsIgnoreCase(SchedularTaskAction.NOT_STARTED.toString())
					|| (schedulerTaskDef.getAction().equalsIgnoreCase(SchedularTaskAction.START.toString())
							&& currentRegistaryJobDetail == null)) {

				log.debug(" Engine Scheduled Job ==== starting NOT_STARTED job key {} with corn ==== {} ", jobkey,
						schedulerTaskDef.getSchedule());
				createAndScheduledJob(schedulerTaskDef, jobkey);

			} else if (schedulerTaskDef.getAction().equalsIgnoreCase(SchedularTaskAction.STOP.toString())
					&& currentRegistaryJobDetail != null) {
				log.debug(" Engine Scheduled Job ==== stop job key {} with corn created ==== {} ", jobkey,
						schedulerTaskDef.getSchedule());
				deleteJobDetail(schedulerTaskDef, currentRegistaryJobDetail);

			} else if (schedulerTaskDef.getAction().equalsIgnoreCase(SchedularTaskAction.RESCHEDULE.toString())) {
				log.debug(" Engine Scheduled Job ==== rescheduled job key {} with corn created ==== {} ", jobkey,
						schedulerTaskDef.getSchedule());

				deleteJobDetail(schedulerTaskDef, currentRegistaryJobDetail);
				createAndScheduledJob(schedulerTaskDef, jobkey);
				EngineStatusLogger.getInstance()
						.createSchedularTaskStatusNode(
								"Engine Scheduled Job ==== RESCHEDULEING job key " + jobkey
										+ " with corn created ==== " + schedulerTaskDef.getSchedule(),
								PlatformServiceConstants.SUCCESS, jobkey);
			}
		} catch (Exception e) {
			log.error(" Engine Scheduled Job ==== Error while registering Task ", e);
			log.error(e);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					" Error while registering Task " + e.getMessage(), PlatformServiceConstants.FAILURE);
		}
	}

	/** This will handle delete job  functionality 
	 * @param schedulerTaskDefinition
	 * @param currentJobDetail
	 * @throws SchedulerException
	 */
	private void deleteJobDetail(InsightsSchedulerTaskDefinition schedulerTaskDefinition, JobDetail currentJobDetail)
			throws SchedulerException {
		try {
			if (currentJobDetail != null && scheduler.checkExists(currentJobDetail.getKey())) {
				scheduler.deleteJob(currentJobDetail.getKey());
				registry.remove(schedulerTaskDefinition.getTimerTaskId());
				log.info("Engine Scheduled Job ==== Job Name {} STOPPED during RESCHEDULE with corn  ====  {}", schedulerTaskDefinition.getComponentName()
						, schedulerTaskDefinition.getSchedule());
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						"Engine Scheduled Job ==== STOPPED  job key " + currentJobDetail.getKey()
								+ " with corn  ==== " + schedulerTaskDefinition.getSchedule(),
								PlatformServiceConstants.SUCCESS, schedulerTaskDefinition.getComponentName());
			}
		} catch (Exception e) {
			log.error(e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Exeception occur while task delelation , exception is " + e.getMessage(),
					PlatformServiceConstants.ERROR, schedulerTaskDefinition.getComponentName());
		}
	}

	/** This will use to create job from DB definition and scheduled it 
	 * @param schedulerTaskDefinition
	 * @param jobkey
	 * @throws ClassNotFoundException
	 * @throws SchedulerException
	 */
	@SuppressWarnings("unchecked")
	private void createAndScheduledJob(InsightsSchedulerTaskDefinition schedulerTaskDefinition, String jobkey)
			throws ClassNotFoundException, SchedulerException {

		try {

			Class<? extends Job> classDetail = (Class<? extends Job>) Class
					.forName(schedulerTaskDefinition.getComponentClassDetail());

			log.debug(" Class Name Context {} ", classDetail);

			JobDetail jobInitializer = JobBuilder.newJob(classDetail)
					.withIdentity(jobkey, schedulerTaskDefinition.getComponentName().concat("JobGroup")).build();

			CronTrigger jobInitializerTrigger = TriggerBuilder.newTrigger()
					.withIdentity(schedulerTaskDefinition.getComponentName(),
							schedulerTaskDefinition.getComponentName().concat("Group"))
					.withSchedule(CronScheduleBuilder.cronSchedule(schedulerTaskDefinition.getSchedule())
							.inTimeZone(TimeZone.getTimeZone("UTC")))
					.build();

			if (!scheduler.isStarted()) {
				scheduler.start();
			}
			if (!scheduler.checkExists(jobInitializer.getKey())) {
				scheduler.scheduleJob(jobInitializer, jobInitializerTrigger);
				scheduler.triggerJob(jobInitializer.getKey());
				registry.put(schedulerTaskDefinition.getTimerTaskId(), jobInitializer);

				updateSchedularTaskDefinition(schedulerTaskDefinition, SchedularTaskAction.START.toString());

				log.debug("Engine Scheduled Job ==== job key {} with corn Registered ==== {} ", jobInitializer.getKey(),
						schedulerTaskDefinition.getSchedule());
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						"Engine Scheduled Job ==== job key " + jobInitializer.getKey() + " with corn created ==== "
								+ schedulerTaskDefinition.getSchedule(),
						PlatformServiceConstants.SUCCESS, schedulerTaskDefinition.getComponentName());
			} else {
				log.debug(
						"Engine Scheduled Job ==== Job is alrady part of scheduler job key {} with corn created ==== {} ",
						jobInitializer.getKey(), schedulerTaskDefinition.getSchedule());
			}
		} catch (ClassNotFoundException e) {
			log.error(e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Exeception occur while registration task, Class Not found for job module class "
							+ schedulerTaskDefinition.getComponentClassDetail(),
					PlatformServiceConstants.ERROR, schedulerTaskDefinition.getComponentName());
		} catch (Exception e) {
			log.error(e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Exeception occur while registration task, exception is " + e.getMessage(),
					PlatformServiceConstants.ERROR, schedulerTaskDefinition.getComponentName());
		}

	}

	/**Use to update status of Scheduler Task Definition
	 * @param schedulerTaskDefinition
	 * @param action
	 * @return
	 */
	public boolean updateSchedularTaskDefinition(InsightsSchedulerTaskDefinition schedulerTaskDefinition,
			String action) {
		try {
			schedulerTaskDefinition.setAction(action);
			schedularTaskDAL.saveOrUpdateSchedulerTaskConfiguration(schedulerTaskDefinition);
		} catch (Exception e) {
			log.error(" Unable to create node {} ", e.getMessage());
		}
		return Boolean.TRUE;
	}
}
