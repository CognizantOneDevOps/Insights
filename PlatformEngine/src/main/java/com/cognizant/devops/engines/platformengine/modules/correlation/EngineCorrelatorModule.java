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

import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;

/**
 * 
 * @author Vishal Ganjare (vganjare)
 * 
 *         Entry point for correlation executor.
 *
 */
public class EngineCorrelatorModule extends TimerTask {
	private static boolean isCorrelationExecutionInProgress = false;
	private static Logger log = LogManager.getLogger(EngineCorrelatorModule.class.getName());

	@Override
	public void run() {
		log.debug(" EngineCorrelatorModule start  ====");
		try {
			if (!isCorrelationExecutionInProgress) {
				isCorrelationExecutionInProgress = true;
				CorrelationExecutor correlationsExecutor = new CorrelationExecutor();
				correlationsExecutor.execute();
				isCorrelationExecutionInProgress = false;
			}
			EngineStatusLogger.getInstance().createEngineStatusNode("Correlation Execution Completed",
					PlatformServiceConstants.SUCCESS);
		} catch (Exception e) {
			log.error("Error in correlation module {} ", e);
			EngineStatusLogger.getInstance().createEngineStatusNode("Correlation Execution has some issue  ",
					PlatformServiceConstants.FAILURE);
		}

		log.debug(" EngineCorrelatorModule Completed ====");
	}
}
