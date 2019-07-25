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
package com.cognizant.devops.platformservice.webhook.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;

@Service("webhookConfigurationService")
public class WebHookService implements IWebHook {
	private static final Logger log = LogManager.getLogger(WebHookService.class);
	private static final String SUCCESS = "SUCCESS";

	@Override
	public Boolean saveWebHookConfiguration(String webhookname, String toolName, String eventname, String dataformat,
			String mqchannel, Boolean subscribestatus, String responseTemplate) throws InsightsCustomException {
		try {
			WebHookConfig webHookConfig = populateWebHookConfiguration(webhookname, toolName, eventname, dataformat,
					mqchannel, subscribestatus, responseTemplate);
			WebHookConfigDAL webhookConfigurationDAL = new WebHookConfigDAL();
			return webhookConfigurationDAL.saveWebHookConfiguration(webHookConfig);
		} catch (InsightsCustomException e) {

			throw new InsightsCustomException(e.getMessage());
		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}
	}

	public List<WebHookConfigTO> getRegisteredWebHooks() throws InsightsCustomException {
		WebHookConfigDAL webhookConfigDAL = new WebHookConfigDAL();
		List<WebHookConfigTO> webhookList = null;
		try {
			List<WebHookConfig> webhookConfigList = webhookConfigDAL.getAllWebHookConfigurations();
			webhookList = new ArrayList<>(webhookConfigList.size());
			for (WebHookConfig webhookConfig : webhookConfigList) {
				WebHookConfigTO to = new WebHookConfigTO();
				BeanUtils.copyProperties(webhookConfig, to, new String[] { "agentJson", "updatedDate" });
				webhookList.add(to);
			}
		} catch (Exception e) {
			log.error("Error getting all webhook config", e);
			throw new InsightsCustomException(e.toString());
		}

		return webhookList;
	}

	public WebHookConfig loadWebHookConfiguration(String webhookName) {
		WebHookConfigDAL webhookConfigurationDAL = new WebHookConfigDAL();
		return webhookConfigurationDAL.loadWebHookConfiguration(webhookName);
	}

	private WebHookConfig populateWebHookConfiguration(String webhookname, String toolName, String eventname,
			String dataformat, String mqchannel, Boolean subscribestatus, String responseTemplate) {
		WebHookConfig webhookConfiguration = new WebHookConfig();
		webhookConfiguration.setDataFormat(dataformat);
		webhookConfiguration.setEventName(eventname);
		webhookConfiguration.setToolName(toolName);
		webhookConfiguration.setMQChannel(mqchannel);
		webhookConfiguration.setWebHookName(webhookname);
		webhookConfiguration.setSubscribeStatus(subscribestatus);
		webhookConfiguration.setResponseTemplate(responseTemplate);
		return webhookConfiguration;
	}

	public String uninstallWebhook(String webhookname) throws InsightsCustomException {
		try {
			WebHookConfigDAL webhookConfigDAL = new WebHookConfigDAL();
			webhookConfigDAL.deleteWebhookConfigurations(webhookname);
		} catch (Exception e) {
			log.error("Error while un-installing webhook..", e);
			throw new InsightsCustomException(e.toString());
		}

		return SUCCESS;
	}

	public Boolean updateWebHook(String webhookname, String toolName, String eventname, String dataformat,
			String mqchannel, Boolean subscribestatus, String responseTemplate) throws InsightsCustomException {
		// Boolean status;
		try {
			WebHookConfig webHookConfig = populateWebHookConfiguration(webhookname, toolName, eventname, dataformat,
					mqchannel, subscribestatus, responseTemplate);
			WebHookConfigDAL webhookConfigurationDAL = new WebHookConfigDAL();
			// log.error(status);
			return webhookConfigurationDAL.updateWebHookConfiguration(webHookConfig);
		} catch (Exception e) {
			log.error("Error updating the webhook", e);
			throw new InsightsCustomException(e.toString());
		}
	}

}
