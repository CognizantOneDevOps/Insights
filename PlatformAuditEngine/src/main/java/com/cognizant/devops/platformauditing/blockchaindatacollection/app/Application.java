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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;


/**
 * Engine execution will start from Application. 1. Load the iSight config 2.
 * Initialize Publisher and subscriber modules 3. Initialize Correlation Module.
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
            JobDetail blockChainProcessingJob = JobBuilder.newJob(PlatformAuditProcessingExecutor.class)
                    .withIdentity("BlockChainProcessingExecutor", "iSight")
                    .build();

            Trigger blockChainProcessingTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("blockChainProcessingExecutorTrigger", "iSight")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(blockchainEngineInterval)
                            .repeatForever())
                    .build();

            //Schedule the jira executor job
            JobDetail jiraProcessingJob = JobBuilder.newJob(JiraProcessingExecutor.class)
                    .withIdentity("BlockChainChangelogProcessingExecutor", "iSight")
                    .build();

            Trigger jiraProcessingTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("blockChainChangelogProcessingExecutorTrigger", "iSight")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(jiraEngineInterval)
                            .repeatForever())
                    .build();
            // Tell quartz to schedule the job using our trigger
            Scheduler scheduler;

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(blockChainProcessingJob, blockChainProcessingTrigger);
            scheduler.scheduleJob(jiraProcessingJob, jiraProcessingTrigger);
            log.info("PlatformAudit Engine Service Started ", PlatformServiceConstants.SUCCESS);
        } catch (SchedulerException e) {
            log.info("PlatformAudit Engine Service not running due to Scheduler Exception " + e.getMessage(), PlatformServiceConstants.FAILURE);
            log.error(e);
        } catch (Exception e) {
            log.info("PlatformAudit Engine Service not running " + e.getMessage(), PlatformServiceConstants.FAILURE);
            log.error(e);
        }
    }

}
