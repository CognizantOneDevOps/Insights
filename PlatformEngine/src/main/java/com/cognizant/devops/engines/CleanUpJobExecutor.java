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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDAL;

public class CleanUpJobExecutor implements Job , ApplicationConfigInterface{
	/**
	 * 
	 */
	private static final Logger log = LogManager.getLogger(CleanUpJobExecutor.class);
	
	private static final long serialVersionUID = -282836461086826715L;
	InsightsSchedulerTaskDAL schedularTaskDAL = new InsightsSchedulerTaskDAL();

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.debug(" CleanUpJobExecutor ==== Inside Cleanup Job Schedular Executor ");
		try {
			ApplicationConfigInterface.loadConfiguration();
			cleanUpTimerTaskStatusRecord();
		} catch (InsightsCustomException e) {
			log.error(" CleanUpJobExecutor Error ====  {}",e.getMessage());			
		}
	}


	private void cleanUpTimerTaskStatusRecord() {
		try {
			String query= "	WITH limitStatusRecord AS " + 
					"  (SELECT STSDEL.taskstatusid " + 
					"   FROM public.\"INSIGHTS_SCHEDULER_TASK_STATUS\" STSDEL, " + 
					"	(SELECT min(STS.recordtimestamp) AS minRecordTimeSatus, " + 
					"             STS.timertaskmapping " + 
					"      FROM public.\"INSIGHTS_SCHEDULER_TASK_STATUS\" STS " + 
					"      INNER JOIN public.\"INSIGHTS_SCHEDULER_TASK_DEFINITION\" STD ON STS.timertaskmapping = STD.componentname " + 
					"      WHERE STS.taskstatusid IN " + 
					"          (SELECT STSa2.taskstatusid " + 
					"           FROM public.\"INSIGHTS_SCHEDULER_TASK_STATUS\" STSa2 " + 
					"           WHERE STSa2.timertaskmapping = STD.componentname " + 
					"           ORDER BY STSa2.recordtimestamp DESC " + 
					"           LIMIT 50) " + 
					"      GROUP BY STS.timertaskmapping) AS minIdRecord " + 
					"   WHERE STSDEL.timertaskmapping = minIdRecord.timertaskmapping " + 
					"     AND STSDEL.recordtimestamp < minIdRecord.minRecordTimeSatus " + 
					"   ORDER BY STSDEL.timertaskmapping, " + 
					"            STSDEL.recordtimestamp DESC) " + 
					" DELETE " + 
					" FROM public.\"INSIGHTS_SCHEDULER_TASK_STATUS\" STSDEL " + 
					" WHERE STSDEL.taskstatusid IN " + 
					"    (SELECT taskstatusid " + 
					"     FROM limitStatusRecord);";
			int executedRecord = schedularTaskDAL.executeUpdateWithSQLQuery(query);
			log.debug(" cleanUpTimerTaskStatusRecord {} ",executedRecord);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		
	}

	
}
