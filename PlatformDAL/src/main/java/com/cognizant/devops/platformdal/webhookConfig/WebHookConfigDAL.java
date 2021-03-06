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
package com.cognizant.devops.platformdal.webhookConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class WebHookConfigDAL extends BaseDAL {
	private static Logger log = LogManager.getLogger(WebHookConfigDAL.class);
	public static final String WEBHOOKCONFIG_QUERY = "FROM WebHookConfig WH WHERE WH.webhookName = :webhookName";
	public static final String WEBHOOKNAME = "webhookName";

	public Boolean updateWebHookConfiguration(WebHookConfig webhookConfiguration) {

		WebHookConfig parentConfigList = null;
		try {

			Map<String, Object> parameters = new HashMap<>();
			parameters.put(WEBHOOKNAME, webhookConfiguration.getWebHookName());
			parentConfigList = getSingleResult(WEBHOOKCONFIG_QUERY, WebHookConfig.class, parameters);

			if (parentConfigList != null) {
				Set<WebhookDerivedConfig> dataFromUI = webhookConfiguration.getWebhookDerivedConfig();
				parentConfigList.setDataFormat(webhookConfiguration.getDataFormat());
				parentConfigList.setLabelName(webhookConfiguration.getLabelName());
				parentConfigList.setWebHookName(webhookConfiguration.getWebHookName());
				parentConfigList.setToolName(webhookConfiguration.getToolName());
				parentConfigList.setMQChannel(webhookConfiguration.getMQChannel());
				parentConfigList.setSubscribeStatus(webhookConfiguration.getSubscribeStatus());
				parentConfigList.setResponseTemplate(webhookConfiguration.getResponseTemplate());
				parentConfigList.setDynamicTemplate(webhookConfiguration.getDynamicTemplate());
				parentConfigList.setIsUpdateRequired(webhookConfiguration.getIsUpdateRequired());
				parentConfigList.setFieldUsedForUpdate(webhookConfiguration.getFieldUsedForUpdate());
				parentConfigList.setEventConfigJson(webhookConfiguration.getEventConfigJson());
				parentConfigList.setEventProcessing(webhookConfiguration.isEventProcessing());
				Set<WebhookDerivedConfig> dataDriverFromTable = parentConfigList.getWebhookDerivedConfig();
				dataDriverFromTable.clear();
				dataDriverFromTable.addAll(dataFromUI);
				parentConfigList.setWebhookDerivedConfig(dataDriverFromTable);
			} else {
				return Boolean.FALSE;
			}
			saveOrUpdate(parentConfigList);
		} catch (Exception e) {
			log.error(e.getMessage());
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public Boolean saveWebHookConfiguration(WebHookConfig webhookConfiguration) throws InsightsCustomException {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(WEBHOOKNAME, webhookConfiguration.getWebHookName());
			List<WebHookConfig> resultList = getResultList(WEBHOOKCONFIG_QUERY, WebHookConfig.class, parameters);
			if (resultList != null && !resultList.isEmpty()) {
				throw new InsightsCustomException(PlatformServiceConstants.WEBHOOK_NAME);
			}
			save(webhookConfiguration);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}

		return Boolean.TRUE;
	}

	public List<WebHookConfig> getAllWebHookConfigurations() {

		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM WebHookConfig WH", WebHookConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<WebHookConfig> getAllActiveWebHookConfigurations() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM WebHookConfig WH WHERE WH.subscribeStatus = true", WebHookConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public String deleteWebhookConfigurations(String webhookName) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(WEBHOOKNAME, webhookName);
			WebHookConfig webhookConfig = getSingleResult("FROM WebHookConfig a WHERE a.webhookName = :webhookName",
					WebHookConfig.class, parameters);

			delete(webhookConfig);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public void updateWebhookStatus(String webhookName, boolean status) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(WEBHOOKNAME, webhookName);
			WebHookConfig updateWebhook = getSingleResult(WEBHOOKCONFIG_QUERY, WebHookConfig.class, parameters);

			updateWebhook.setSubscribeStatus(status);
			update(updateWebhook);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<WebHookConfig> getAllEventWebHookConfigurations() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM WebHookConfig WH WHERE WH.isEventProcessing = true", WebHookConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}

	}
}
