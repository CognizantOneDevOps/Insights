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
package com.cognizant.devops.platformwebhookengine.modules.aggregator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;
import com.cognizant.devops.platformwebhookengine.message.core.WebhookEngineStatusLogger;
import com.cognizant.devops.platformwebhookengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformwebhookengine.message.subscriber.WebHookDataSubscriber;

public class EngineAggregatorModule implements Job {
	private static Logger log = LogManager.getLogger(EngineAggregatorModule.class.getName());
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<String, EngineSubscriberResponseHandler>();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		ApplicationConfigProvider.performSystemCheck();
		Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
		WebHookConfigDAL webhookConfigDal = new WebHookConfigDAL();
		List<WebHookConfig> allWebhookConfigurations = webhookConfigDal.getAllWebHookConfigurations();
		for (WebHookConfig webhookConfig : allWebhookConfigurations) {
			String webhookname = webhookConfig.getWebHookName().toUpperCase();
			String toolName = webhookConfig.getToolName().toUpperCase();
			Boolean subscribeStatus = webhookConfig.getSubscribeStatus();

			log.debug("Webhook Detail {}  subscribed Status {}", webhookname, subscribeStatus);

			if (subscribeStatus == true) {
				registerAggragators(webhookConfig, graphDBHandler, toolName);
			}
		}
	}

	private void registerAggragators(WebHookConfig webhookConfig, Neo4jDBHandler graphDBHandler, String toolName) {
		String dataRoutingKey = webhookConfig.getMQChannel();
		String labelName = webhookConfig.getLabelName();
		String webhookName = webhookConfig.getWebHookName();
		String responseTemplate = webhookConfig.getResponseTemplate();
		try {
			if (dataRoutingKey != null && !registry.containsKey(dataRoutingKey)) {
				try {
					registry.put(dataRoutingKey, new WebHookDataSubscriber(dataRoutingKey, responseTemplate, toolName,
							labelName, webhookName));
					log.debug("Webhook {} subscribed successfully ", webhookName);
					WebhookEngineStatusLogger.getInstance().createEngineStatusNode(
							"Webhook " + webhookName + " subscribed successfully ", PlatformServiceConstants.SUCCESS);
				} catch (Exception e) {
					WebhookEngineStatusLogger.getInstance().createEngineStatusNode(
							"Unable to subscribed Webhook " + webhookName + " Error Detail :" + e.getMessage(),
							PlatformServiceConstants.FAILURE);
					log.error("Unable to add subscriber for routing key: " + e);

				}
			}
		} catch (Exception e) {
			WebhookEngineStatusLogger.getInstance().createEngineStatusNode(
					"Unable to subscribed Webhook " + webhookName + " Error Detail :" + e.getMessage(),
					PlatformServiceConstants.FAILURE);
			log.error("Unable to add subscriber for routing key: " + e);

		}

	}
}