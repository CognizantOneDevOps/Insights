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
import com.cognizant.devops.platformdal.healthutil.InsightsComponentHealthDetailDAL;
import com.cognizant.devops.platformdal.healthutil.InsightsAgentHealthDetailDAL;


public class CleanUpJobExecutor implements Job , ApplicationConfigInterface{
	/**
	 * 
	 */
	private static final Logger log = LogManager.getLogger(CleanUpJobExecutor.class);
	
	private static final long serialVersionUID = -282836461086826715L;
	InsightsSchedulerTaskDAL schedularTaskDAL = new InsightsSchedulerTaskDAL();
	InsightsAgentHealthDetailDAL agentHealthDetailDAL = new InsightsAgentHealthDetailDAL();
	InsightsComponentHealthDetailDAL componentHealthDetailDAL = new InsightsComponentHealthDetailDAL();

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.debug(" CleanUpJobExecutor ==== Inside Cleanup Job Schedular Executor ");
		try {
			ApplicationConfigInterface.loadConfiguration();
			cleanUpTimerTaskStatusRecord();
			cleanUpAgentHealthDetailsRecords();
			cleanUpComponentHealthDetailsRecords();
		} catch (InsightsCustomException e) {
			log.error(" CleanUpJobExecutor Error ====  {}",e.getMessage());			
		}
	}

	/***
	 * 
	 */
	private void cleanUpTimerTaskStatusRecord() {
		try {
			String query= " WITH limitStatusRecord AS " + 
					"  (SELECT STSDEL.taskstatusid " + 
					"   FROM public.\"INSIGHTS_SCHEDULER_TASK_STATUS\" STSDEL, " + 
					" (SELECT min(STS.recordtimestamp) AS minRecordTimeSatus,  " + 
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
	
	/***
	 * Clean Up job to delete older agent health detail records 
	 * 
	 */	
	private void cleanUpAgentHealthDetailsRecords() {
		try {
			String deleteAgentHealthQuery= "WITH oldAgentHealthRecords AS" +
					" (SELECT agentHealthRecords.id as agentHealthId " +  
				    " FROM public.\"INSIGHTS_AGENT_HEALTH_DETAILS\" agentHealthRecords," +   
			        " (SELECT min(agentHealthRecords1.inSightsTime) AS minExecutionTime,agentHealthRecords1.agent_id " + 
                    " FROM public.\"INSIGHTS_AGENT_HEALTH_DETAILS\" agentHealthRecords1 " +
			        " WHERE agentHealthRecords1.id IN " + 
			        " (SELECT agentHealthRecords2.id " +  
			        " FROM public.\"INSIGHTS_AGENT_HEALTH_DETAILS\" agentHealthRecords2 " +
			        " WHERE agentHealthRecords2.agent_id = agentHealthRecords1.agent_id  " +
			        " ORDER BY agentHealthRecords2.inSightsTime DESC " + 
			        " LIMIT 50 )  " +	   
			        " GROUP BY agentHealthRecords1.agent_id " +
				    " ORDER BY agentHealthRecords1.agent_id " +
				    " ) AS minIdRecord " +  
				    " WHERE " + 
				    " agentHealthRecords.agent_id = minIdRecord.agent_id  " +
				    " AND agentHealthRecords.inSightsTime < minIdRecord.minExecutionTime " + 
				    " ORDER BY agentHealthRecords.agent_id,agentHealthRecords.inSightsTime DESC )  " +			  
				    " DELETE " +  
				    " FROM public.\"INSIGHTS_AGENT_HEALTH_DETAILS\" " +   
				    " WHERE id IN (SELECT agentHealthId FROM oldAgentHealthRecords); ";				
			
			log.debug("deleteAgentHealthQuery : {}", deleteAgentHealthQuery); 
			int executedRecord = agentHealthDetailDAL.executeUpdateWithSQLQuery(deleteAgentHealthQuery);
			log.debug("cleanUpAgentHealthDetailsRecords--- Deleted Records Count: {} ",executedRecord);
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		
	}
	
	
	/**
	 * Clean Up job to delete older component health detail records 
	 * 
	 */
	private void cleanUpComponentHealthDetailsRecords() {
		try {
			String deleteComponentHealthQuery= " WITH oldComponentHealthRecords AS "
					+ " (SELECT componentHealthRecords.id as componentHealthId "
					+ " FROM public.\"INSIGHTS_COMPONENTS_HEALTH_DETAILS\" componentHealthRecords,"
					+ " (SELECT min(componentHealthRecords1.inSightsTime) AS minExecutionTime,componentHealthRecords1.component_name "
					+ "  FROM public.\"INSIGHTS_COMPONENTS_HEALTH_DETAILS\" componentHealthRecords1 "
					+ "  WHERE componentHealthRecords1.id IN "
					+ "  (SELECT componentHealthRecords2.id"
					+ "  FROM public.\"INSIGHTS_COMPONENTS_HEALTH_DETAILS\" componentHealthRecords2 "
					+ "  WHERE componentHealthRecords2.component_name = componentHealthRecords1.component_name"
					+ "  ORDER BY componentHealthRecords2.inSightsTime DESC "
					+ "  LIMIT 50 ) "
					+ "	 GROUP BY componentHealthRecords1.component_name"
					+ "  ORDER BY componentHealthRecords1.component_name"
					+ "  ) AS minIdRecord "
					+ "  WHERE componentHealthRecords.component_name = minIdRecord.component_name"
					+ "  AND componentHealthRecords.inSightsTime < minIdRecord.minExecutionTime"
					+ "  ORDER BY componentHealthRecords.component_name,componentHealthRecords.inSightsTime DESC )"
					+ "  DELETE"
					+ "  FROM public.\"INSIGHTS_COMPONENTS_HEALTH_DETAILS\""
					+ "  WHERE id IN "
					+ "  (SELECT componentHealthId FROM oldComponentHealthRecords);" ;
					
			log.debug("deleteComponentHealthQuery : {}", deleteComponentHealthQuery);
			int executedRecord = componentHealthDetailDAL.executeUpdateWithSQLQuery(deleteComponentHealthQuery);
			log.debug("cleanUpComponentHealthDetailsRecords----Deleted Records Count: {} ",executedRecord);
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}		
	}
}
