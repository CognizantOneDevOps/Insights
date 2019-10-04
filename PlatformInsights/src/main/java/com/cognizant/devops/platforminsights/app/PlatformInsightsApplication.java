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
package com.cognizant.devops.platforminsights.app;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platforminsights.core.InferenceJobExecutor;
import com.cognizant.devops.platforminsights.core.InsightsStatusProvider;

/**
 * Engine execution will start from Application.
 * 1. Load the iSight config
 * 2. Initialize Inference Module.
 * 3. Log Platform Insight Health data in DB
 */
public class PlatformInsightsApplication {
	private static Logger log = LogManager.getLogger(PlatformInsightsApplication.class);

	private static int defaultIntervalInSec = 600;

	private PlatformInsightsApplication() {

	}

	public static void main(String[] args) {
		if (args.length > 0) {
			defaultIntervalInSec = Integer.valueOf(args[0]);
		}
		// Load isight config
		ApplicationConfigCache.loadConfigCache();
		// Create Default users
		ApplicationConfigProvider.performSystemCheck();

		try {
			Timer timerInferenceEngineJobExecutor = new Timer("InferenceEngineJobExecutorModule");
			TimerTask inferenceAggregatorTrigger = new InferenceJobExecutor();
			timerInferenceEngineJobExecutor.schedule(inferenceAggregatorTrigger, 0, defaultIntervalInSec * 1000);

		} catch (Exception e) {
			log.error("Exception in Sparkjob ", e);
			InsightsStatusProvider.getInstance().createInsightStatusNode(
					"Platform Insights Spark Application not started ", PlatformServiceConstants.FAILURE);
		}
	}
}
