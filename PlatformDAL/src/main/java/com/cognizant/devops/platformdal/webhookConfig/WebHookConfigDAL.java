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

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class WebHookConfigDAL extends BaseDAL {
	private static Logger log = LogManager.getLogger(WebHookConfigDAL.class);

	public Boolean updateWebHookConfiguration(WebHookConfig webhookConfiguration) {
		Query<WebHookConfig> createQuery = getSession()
				.createQuery("FROM WebHookConfig WH WHERE WH.webhookName = :webhookName", WebHookConfig.class);
		createQuery.setParameter("webhookName", webhookConfiguration.getWebHookName());
		WebHookConfig parentConfigList = createQuery.getSingleResult();
		terminateSession();
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
			getSession().beginTransaction();
			getSession().saveOrUpdate(parentConfigList);
			getSession().getTransaction().commit();
			terminateSession();
			terminateSessionFactory();

			return Boolean.TRUE;

		} else {
			return Boolean.FALSE;
		}

	}

	public Boolean saveWebHookConfiguration(WebHookConfig webhookConfiguration) throws InsightsCustomException {
		Query<WebHookConfig> createQuery = getSession()
				.createQuery("FROM WebHookConfig WH WHERE WH.webhookName = :webhookName", WebHookConfig.class);
		createQuery.setParameter("webhookName", webhookConfiguration.getWebHookName());
		List<WebHookConfig> resultList = createQuery.getResultList();
		if (resultList != null && !resultList.isEmpty()) {
			throw new InsightsCustomException(PlatformServiceConstants.WEBHOOK_NAME);
		}
		getSession().beginTransaction();
		getSession().save(webhookConfiguration);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
	}

	public List<WebHookConfig> getAllWebHookConfigurations() {
		getSession().beginTransaction();
		Query<WebHookConfig> createQuery = getSession().createQuery("FROM WebHookConfig WH", WebHookConfig.class);
		List<WebHookConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	public List<WebHookConfig> getAllActiveWebHookConfigurations() {
		getSession().beginTransaction();
		Query<WebHookConfig> createQuery = getSession()
				.createQuery("FROM WebHookConfig WH WHERE WH.subscribeStatus = true", WebHookConfig.class);
		List<WebHookConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	public String deleteWebhookConfigurations(String webhookName) {
		Query<WebHookConfig> createQuery = getSession()
				.createQuery("FROM WebHookConfig a WHERE a.webhookName = :webhookName", WebHookConfig.class);
		createQuery.setParameter("webhookName", webhookName);
		WebHookConfig webhookConfig = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(webhookConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	public void updateWebhookStatus(String webhookName, boolean status) {
		Query<WebHookConfig> createQuery = getSession()
				.createQuery("FROM WebHookConfig WH WHERE WH.webhookName = :webhookName", WebHookConfig.class);
		createQuery.setParameter("webhookName", webhookName);
		WebHookConfig updateWebhook = createQuery.getSingleResult();
		updateWebhook.setSubscribeStatus(status);
		getSession().beginTransaction();
		getSession().update(updateWebhook);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
	}
	
	public List<WebHookConfig> getAllEventWebHookConfigurations()
	{
		getSession().beginTransaction();
		Query<WebHookConfig> createQuery = getSession()
				.createQuery("FROM WebHookConfig WH WHERE WH.isEventProcessing = true", WebHookConfig.class);
		List<WebHookConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}
}
