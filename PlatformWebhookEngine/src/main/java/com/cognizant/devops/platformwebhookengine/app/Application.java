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

package com.cognizant.devops.platformwebhookengine.app;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformwebhookengine.message.core.WebhookEngineStatusLogger;
import com.cognizant.devops.platformwebhookengine.modules.aggregator.EngineAggregatorModule;

public class Application {
    private static Logger log = LogManager.getLogger(Application.class.getName());

	private static int defaultIntervalInSec = 600;
    private Application() {

    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
			defaultIntervalInSec = Integer.valueOf(args[0]);
        }
        try {
      
            ApplicationConfigCache.loadConfigCache();

            ApplicationConfigProvider.performSystemCheck();

			Timer timerWebhookEngineJobExecutorModule = new Timer("WebhookEngineJobExecutorModule");
			TimerTask webhookAggregatorTrigger = new EngineAggregatorModule();
			timerWebhookEngineJobExecutorModule.schedule(webhookAggregatorTrigger, 0, defaultIntervalInSec * 1000);

            WebhookEngineStatusLogger.getInstance().createEngineStatusNode("Platform Webhook Engine Service Started ",PlatformServiceConstants.SUCCESS);

		} catch (Exception e) {
            WebhookEngineStatusLogger.getInstance().createEngineStatusNode("Platform Webhook Engine Service not running "+e.getMessage(),PlatformServiceConstants.FAILURE);
            log.error(e);
        }
    }

}