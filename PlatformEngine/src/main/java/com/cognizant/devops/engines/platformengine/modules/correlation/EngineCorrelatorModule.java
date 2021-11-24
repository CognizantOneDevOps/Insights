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
package com.cognizant.devops.engines.platformengine.modules.correlation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;

/**
 * 
 * @author Vishal Ganjare (vganjare)
 * 
 *         Entry point for correlation executor.
 *
 */
public class EngineCorrelatorModule implements Job, ApplicationConfigInterface {
	private boolean isCorrelationExecutionInProgress = false;
	private static Logger log = LogManager.getLogger(EngineCorrelatorModule.class.getName());
	String jobName="";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug(" Engine Scheduled Job ====  EngineCorrelatorModule start ");
		long startTime =System.currentTimeMillis();
		jobName=context.getJobDetail().getKey().getName();
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Correlation execution Start ",
				PlatformServiceConstants.SUCCESS,jobName);
		try {
			ApplicationConfigInterface.loadConfiguration();
			if (!isCorrelationExecutionInProgress) {
				isCorrelationExecutionInProgress = true;
				executeCorrelation();
				isCorrelationExecutionInProgress = false;
			}
			log.debug("Engine Scheduled Job ==== Correlation Execution Completed");
			
		} catch (Exception e) {
			log.error("Error in correlation module ", e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Correlation execution has some issue  ",
					PlatformServiceConstants.FAILURE,jobName);	
		}
		
		long processingTime = System.currentTimeMillis() - startTime ;
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Correlation execution Completed",
				PlatformServiceConstants.SUCCESS,jobName,processingTime);
	}

	public void executeCorrelation() {
		CorrelationExecutor correlationsExecutor = new CorrelationExecutor();
		correlationsExecutor.execute(jobName);
	}
}
