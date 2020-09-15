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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;
import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service("webhookConfigurationService")
public class WebHookServiceImpl implements IWebHook {
	private static final Logger log = LogManager.getLogger(WebHookServiceImpl.class);
	WebHookConfigDAL webhookConfigurationDAL = new WebHookConfigDAL();

	@Override
	public Boolean saveWebHookConfiguration(JsonObject registerWebhookjson) throws InsightsCustomException {
		try {
			WebHookConfig webHookConfig = populateWebHookConfiguration(registerWebhookjson);
			return webhookConfigurationDAL.saveWebHookConfiguration(webHookConfig);
		} catch (InsightsCustomException e) {
			log.error("Error while saving the webhook .. {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	@Override
	public List<WebHookConfig> getRegisteredWebHooks() throws InsightsCustomException {

		try {
			List<WebHookConfig> webhookConfigList = webhookConfigurationDAL.getAllWebHookConfigurations();
			return webhookConfigList;
		} catch (Exception e) {
			log.error("Error getting all webhook config.. {}", e);
			throw new InsightsCustomException(e.toString());
		}

	}

	/**
	 * Validation of the Response Template which has been entered by the user
	 * 
	 * @param responseTemplate
	 * @return
	 * @throws InsightsCustomException
	 */
	private Boolean checkResponseTemplate(String responseTemplate) throws InsightsCustomException {
		try {
			StringTokenizer st = new StringTokenizer(responseTemplate, ",");
			while (st.hasMoreTokens()) {
				String keyValuePairs = st.nextToken();
				int count = StringUtils.countOccurrencesOf(keyValuePairs, "=");
				if (count != 1) {
					throw new InsightsCustomException(PlatformServiceConstants.INCORRECT_RESPONSE_TEMPLATE);
				} else {
					String[] dataKeyMapper = keyValuePairs.split("=");
					log.debug(" {}  , {} ", dataKeyMapper[0].trim(), dataKeyMapper[1].trim());
				}
			}
			return true;
		} catch (InsightsCustomException e) {
			log.error("Error in Response Template.. {}", e.getMessage());
			throw new InsightsCustomException(PlatformServiceConstants.INCORRECT_RESPONSE_TEMPLATE);
		}
	}

	/**
	 * Populatating the data received from the user into the Object of the entity class.
	 * 
	 * @param registerWebhookjson
	 * @return
	 * @throws InsightsCustomException
	 */
	private WebHookConfig populateWebHookConfiguration(JsonObject registerWebhookjson) throws InsightsCustomException {
		try {
			WebHookConfig webhookConfiguration = new WebHookConfig();
			String responseTemplate = registerWebhookjson.get("responseTemplate").getAsString();
			if (responseTemplate != "") {
				checkResponseTemplate(responseTemplate);
			}
			String dynamicTemplate = registerWebhookjson.get("dynamicTemplate").getAsString();
			Boolean isUpdateRequired = registerWebhookjson.get("isUpdateRequired").getAsBoolean();
			String eventConfig=registerWebhookjson.get("eventConfig").getAsString();
			Boolean isEventProcessing = registerWebhookjson.get("isEventProcessing").getAsBoolean();
			String webhookName = registerWebhookjson.get("webhookName").getAsString();
			JsonArray derivedOperationsArray = registerWebhookjson.get("derivedOperations").getAsJsonArray();
			Set<WebhookDerivedConfig> setWebhookDerivedConfigs = new HashSet<WebhookDerivedConfig>();
			webhookConfiguration.setDataFormat(registerWebhookjson.get("dataformat").getAsString());
			webhookConfiguration.setLabelName(registerWebhookjson.get("labelDisplay").getAsString().toUpperCase());
			webhookConfiguration.setToolName(registerWebhookjson.get("toolName").getAsString());
			webhookConfiguration.setMQChannel(registerWebhookjson.get("mqchannel").getAsString());
			webhookConfiguration.setWebHookName(webhookName);
			webhookConfiguration.setSubscribeStatus(registerWebhookjson.get("statussubscribe").getAsBoolean());
			webhookConfiguration.setResponseTemplate(responseTemplate);
			webhookConfiguration.setDynamicTemplate(dynamicTemplate);
			webhookConfiguration.setFieldUsedForUpdate(registerWebhookjson.get("fieldUsedForUpdate").getAsString());
			webhookConfiguration.setEventConfigJson(eventConfig);
			webhookConfiguration.setEventProcessing(isEventProcessing);
			if (responseTemplate.isEmpty()) {
				webhookConfiguration.setResponseTemplate(null);
			}
			if (dynamicTemplate.isEmpty()) {
				webhookConfiguration.setDynamicTemplate(null);
			}
			if(eventConfig.isEmpty())
			{
				webhookConfiguration.setEventConfigJson(null);
			}
			webhookConfiguration.setIsUpdateRequired(isUpdateRequired);
			if (!isUpdateRequired.booleanValue()) {
				webhookConfiguration.setFieldUsedForUpdate(null);
			}
			for (JsonElement webhookDerivedConfigJson : derivedOperationsArray) {
				WebhookDerivedConfig webhookDerivedConfig = new WebhookDerivedConfig();
				JsonObject receivedObject = webhookDerivedConfigJson.getAsJsonObject();
				int wid = receivedObject.get("wid").getAsInt();
				webhookDerivedConfig.setOperationName(receivedObject.get("operationName").getAsString());
				webhookDerivedConfig.setOperationFields(receivedObject.get("operationFields").toString());
				webhookDerivedConfig.setWebhookName(webhookName);
				if (wid != -1) {
					webhookDerivedConfig.setWid(wid);
				}
				setWebhookDerivedConfigs.add(webhookDerivedConfig);
			}
			webhookConfiguration.setWebhookDerivedConfig(setWebhookDerivedConfigs);
			return webhookConfiguration;
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	@Override
	public String uninstallWebhook(String webhookname) throws InsightsCustomException {
		try {

			webhookConfigurationDAL.deleteWebhookConfigurations(webhookname);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while un-installing webhook..{}", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	@Override
	public Boolean updateWebHook(JsonObject registerWebhookjson) throws InsightsCustomException {
		Boolean status = false;
		try {
			WebHookConfig webHookConfig = populateWebHookConfiguration(registerWebhookjson);
			status = webhookConfigurationDAL.updateWebHookConfiguration(webHookConfig);
		} catch (Exception e) {
			log.error("Error in updating the webhook.. {}", e);
			throw new InsightsCustomException(e.toString());
		}
		return status;
	}

	public String updateWebhookStatus(JsonObject updateWebhookJsonValidated) throws InsightsCustomException {
		try {
			String webhookName = updateWebhookJsonValidated.get("webhookName").getAsString();
			Boolean statussubscribe = updateWebhookJsonValidated.get("statussubscribe").getAsBoolean();
			webhookConfigurationDAL.updateWebhookStatus(webhookName, statussubscribe);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while updating webhook status..{}", e);
			throw new InsightsCustomException(e.toString());
		}
	}

}
