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
package com.cognizant.devops.engines;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformdataarchivalengine.modules.aggregator.DataArchivalAggregatorModule;
import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.platformengine.modules.correlation.EngineCorrelatorModule;
import com.cognizant.devops.engines.platformengine.modules.mapper.ProjectMapperModule;
import com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.OfflineDataProcessingExecutor;
import com.cognizant.devops.engines.platformwebhookengine.offlineprocessing.WebhookOfflineEventProcessing;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;

/**
 * Engine execution will start from Application. 1. Load the iSight config 2.
 * Initialize Publisher and subscriber modules which receive data from tool 3.
 * Initialize Correlation Module. 4. Initialize ProjectMapperModule Module 5.
 * Initialize DataPurgingExecutor Module 6. Initialize
 * OfflineDataProcessingExecutor Module 7. Log Engine Health data in DB
 */
public class Application {

	private static Logger log = LogManager.getLogger(Application.class.getName());

	private Application() {
	}

	public static void main(String[] args) {

		try {
			// Load insight configuration
			ApplicationConfigCache.loadConfigCache();

			ApplicationConfigProvider.performSystemCheck();

			// Subscribe for desired events. This used to consume data from tool queue.
			Timer timerEngineAggregator = new Timer("EngineAggregatorModule");
			TimerTask engineAggregatorModuleTrigger = new EngineAggregatorModule();
			timerEngineAggregator.schedule(engineAggregatorModuleTrigger, 0,
					ApplicationConfigProvider.getInstance().getSchedulerConfigData().getEngineAggregatorModuleInterval()
							* 60 * 1000);

			// Schedule the Correlation Module.
			Timer timerEngineCorrelatorModule = new Timer("EngineCorrelatorModule");
			TimerTask engineCorrelatorModuleTrigger = new EngineCorrelatorModule();
			timerEngineCorrelatorModule.schedule(engineCorrelatorModuleTrigger, 0,
					ApplicationConfigProvider.getInstance().getSchedulerConfigData().getEngineCorrelatorModuleInterval()
							* 60 * 1000);

			// Schedule the Project Mapping Module.
			Timer timerProjectMapperModule = new Timer("ProjectMapperModule");
			TimerTask projectMapperModuleTrigger = new ProjectMapperModule();
			timerProjectMapperModule.schedule(projectMapperModuleTrigger, 0,
					ApplicationConfigProvider.getInstance().getSchedulerConfigData().getProjectMapperModuleInterval()
							* 60 * 1000);

			// Schedule the OfflineDataProcessingExecutor job
			Timer timerOfflineDataProcessingExecutor = new Timer("OfflineDataProcessingExecutor");
			TimerTask offlineDataProcessingExecutorTrigger = new OfflineDataProcessingExecutor();
			timerOfflineDataProcessingExecutor.schedule(offlineDataProcessingExecutorTrigger, 0,
					ApplicationConfigProvider.getInstance().getSchedulerConfigData()
							.getOfflineDataProcessingExecutorInterval() * 60 * 1000);

			EngineStatusLogger.getInstance().createEngineStatusNode("Platform Engine Service Started ",
					PlatformServiceConstants.SUCCESS);

		} catch (Exception e) {
			EngineStatusLogger.getInstance().createEngineStatusNode(
					"Platform Engine Service not running " + e.getMessage(), PlatformServiceConstants.FAILURE);
			log.error(e);
		}

		if (ApplicationConfigProvider.getInstance().isEnableAuditEngine()) {

			try {

				// Schedule the BlockChainExecuter job
				Timer timerBlockChainProcessing = new Timer("BlockChainProcessingExecutor");
				Class blockChainProcessingTrigger = Class.forName(
						"com.cognizant.devops.engines.platformauditing.blockchaindatacollection.modules.blockchainprocessing.PlatformAuditProcessingExecutor");
				TimerTask blockChainTimer = (TimerTask) blockChainProcessingTrigger.newInstance();
				timerBlockChainProcessing.schedule(blockChainTimer, 0,
						ApplicationConfigProvider.getInstance().getSchedulerConfigData().getAuditEngineInterval() * 60
								* 1000);
				// Schedule the jira executor job
				Timer timerJiraProcessing = new Timer("JiraProcessingExecutor");
				Class jiraProcessingTrigger = Class.forName(
						"com.cognizant.devops.engines.platformauditing.blockchaindatacollection.modules.blockchainprocessing.JiraProcessingExecutor");
				TimerTask jiraProcessingTimer = (TimerTask) jiraProcessingTrigger.newInstance();
				timerJiraProcessing.schedule(jiraProcessingTimer, 0,
						ApplicationConfigProvider.getInstance().getSchedulerConfigData().getAuditEngineInterval()
								* 1000);
				EngineStatusLogger.getInstance().createAuditStatusNode("Platform AuditEngine Service started ",
						PlatformServiceConstants.SUCCESS);
			} catch (ClassNotFoundException e) {
				EngineStatusLogger.getInstance().createAuditStatusNode(
						"Platform AuditEngine Service not running as it is not subscribed " + e.getMessage(),
						PlatformServiceConstants.FAILURE);
				log.error(e);
			} catch (Exception e) {
				EngineStatusLogger.getInstance().createAuditStatusNode(
						"Platform AuditEngine Service not running " + e.getMessage(), PlatformServiceConstants.FAILURE);
				log.error(e);
			}
		}

		if (ApplicationConfigProvider.getInstance().getWebhookEngine().isEnableWebHookEngine()) {

			try {
				// Scheduling Webhook Engine job.
				Timer timerWebhookEngineJobExecutorModule = new Timer("WebhookEngineJobExecutorModule");
				Class webhookAggregatorTrigger = Class.forName(
						"com.cognizant.devops.engines.platformwebhookengine.modules.aggregator.WebHookEngineAggregatorModule");
				TimerTask webHookTimer = (TimerTask) webhookAggregatorTrigger.newInstance();
				timerWebhookEngineJobExecutorModule.schedule(webHookTimer, 0,
						ApplicationConfigProvider.getInstance().getSchedulerConfigData().getWebhookEngineInterval() * 60
								* 1000);
				EngineStatusLogger.getInstance().createWebhookEngineStatusNode(
						"Platform WebhookEngine Service started. ", PlatformServiceConstants.SUCCESS);

				// Scheduling webhook offline processing job

				Timer timerOfflineEventProcessingExecutor = new Timer("WebhookOfflineEventProcessing");
				TimerTask offlineEventProcessingExecutorTrigger = new WebhookOfflineEventProcessing();
				timerOfflineEventProcessingExecutor.schedule(offlineEventProcessingExecutorTrigger, 0,
						ApplicationConfigProvider.getInstance().getSchedulerConfigData()
								.getOfflineWebhookEventProcessingInterval() * 60 * 1000);

				EngineStatusLogger.getInstance().createEngineStatusNode(
						"Platform WebhookOfflineEventProcessing Started ", PlatformServiceConstants.SUCCESS);

			} catch (ClassNotFoundException e) {
				EngineStatusLogger.getInstance().createWebhookEngineStatusNode(
						"Platform WebhookEngine Service not running as it is not subscribed " + e.getMessage(),
						PlatformServiceConstants.FAILURE);
				log.error(e);
			} catch (Exception e) {
				EngineStatusLogger.getInstance().createWebhookEngineStatusNode(
						"Platform WebhookEngine Service not running " + e.getMessage(),
						PlatformServiceConstants.FAILURE);
				log.error(e);
			}
		}

		if (ApplicationConfigProvider.getInstance().isEnableDataArchivalEngine()) {
			try {
				Timer timerEngineDataArchivalAggregator = new Timer("DataArchivalAggregatorModule");
				TimerTask dataArchivalAggregatorModuleTrigger = new DataArchivalAggregatorModule();
				timerEngineDataArchivalAggregator.schedule(dataArchivalAggregatorModuleTrigger, 0,
						ApplicationConfigProvider.getInstance().getSchedulerConfigData().getDataArchivalEngineInterval()
								* 60 * 1000);
				EngineStatusLogger.getInstance().createDataArchivalStatusNode(
						"Platform Data Archival Engine Service started. ", PlatformServiceConstants.SUCCESS);
			} catch (Exception e) {
				EngineStatusLogger.getInstance().createDataArchivalStatusNode(
						"Data Archival Engine Service not running " + e.getMessage(), PlatformServiceConstants.FAILURE);
				log.error(e);
			}
		}
	}
}
