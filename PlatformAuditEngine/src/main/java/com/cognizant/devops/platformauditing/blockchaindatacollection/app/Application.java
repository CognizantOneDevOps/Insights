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
package com.cognizant.devops.platformauditing.blockchaindatacollection.app;

import com.cognizant.devops.platformauditing.blockchaindatacollection.modules.blockchainprocessing.JiraProcessingExecutor;
import com.cognizant.devops.platformauditing.blockchaindatacollection.modules.blockchainprocessing.PlatformAuditProcessingExecutor;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Engine execution will start from Application.
 * 1. Load the iSight config
 * 2. Initialize BlockChain and JiraProcessing Module.
 */
public class Application {
    private static Logger log = LogManager.getLogger(Application.class.getName());

    private static int blockchainEngineInterval = 250;
    private static int jiraEngineInterval = 250;

    private Application() {

    }

    public static void main(String[] args) {
        if (args.length > 0) {
            blockchainEngineInterval = Integer.valueOf(args[0]);
        }
        if (args.length >= 2) {
            jiraEngineInterval = Integer.valueOf(args[1]);
        }
        try {
            // Load insight configuration
            ApplicationConfigCache.loadConfigCache();

            ApplicationConfigProvider.performSystemCheck();

            // Schedule the BlockChainExecuter job
			Timer timerBlockChainProcessing = new Timer("BlockChainProcessingExecutor");
			TimerTask blockChainProcessingTrigger = new PlatformAuditProcessingExecutor();
			timerBlockChainProcessing.schedule(blockChainProcessingTrigger, 0, blockchainEngineInterval * 1000);

			//Schedule the jira executor job
			Timer timerJiraProcessing = new Timer("JiraProcessingExecutor");
			TimerTask jiraProcessingTrigger = new JiraProcessingExecutor();
			timerJiraProcessing.schedule(jiraProcessingTrigger, 0, jiraEngineInterval * 1000);

            log.info("PlatformAudit Engine Service Started ", PlatformServiceConstants.SUCCESS);
		} catch (Exception e) {
            log.info("PlatformAudit Engine Service not running " + e.getMessage(), PlatformServiceConstants.FAILURE);
            log.error(e);
        }
    }

}
