/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformroi.aggregator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformroi.subscriber.MilestoneExecutor;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;

public class MileStoneExecutionAggregatorModule implements Job, ApplicationConfigInterface {
	
	private static Logger log = LogManager.getLogger(MileStoneExecutionAggregatorModule.class);
	MilestoneExecutor milestoneExecutor = new MilestoneExecutor();
	String jobName="";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug("MileStone Execution Aggregator Module start");
		long startTime = System.currentTimeMillis();
		jobName=context.getJobDetail().getKey().getName();
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("MileStone Execution Aggregator Module start ",
				PlatformServiceConstants.SUCCESS,jobName);
		
		try {
			milestoneExecutor.executeMilestone();
		} catch (Exception e) {
			log.error("Unable to add subscriber ", e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Error occured while executing Milestone Aggregator Module  " + e.getMessage(),
					PlatformServiceConstants.FAILURE,jobName);
		}
		
		log.debug("Milestone Aggregator Module completed");
		long processingTime = System.currentTimeMillis() - startTime  ;
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Milestone Aggregator Module completed",
				PlatformServiceConstants.SUCCESS,jobName,processingTime);
	}
}
