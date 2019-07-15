package com.cognizant.devops.platformdal.webhookConfig;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;


public class WebHookConfigDAL  extends BaseDAL{
	private static final Logger log = LogManager.getLogger(	WebHookConfigDAL.class);
	public Boolean updateWebHookConfiguration(WebHookConfig webhookConfiguration) {
		log.error("webhookConfiguration");
		log.error(webhookConfiguration.getDataFormat());
		Query<WebHookConfig> createQuery = getSession().createQuery(
				"FROM WebHookConfig WH WHERE WH.webhookName = :webhookName",
				WebHookConfig.class);
		log.error("excuted");
		createQuery.setParameter("webhookName", webhookConfiguration.getWebHookName());
		List<WebHookConfig> resultList = createQuery.getResultList();
		log.error(resultList);
		WebHookConfig webhookConfig = null;
		if(resultList != null && !resultList.isEmpty()){
			webhookConfig = resultList.get(0);
		}
		
		getSession().beginTransaction();
		if (webhookConfig != null) {
			webhookConfig.setDataFormat(webhookConfiguration.getDataFormat());
			webhookConfig.setEventName(webhookConfiguration.getEventName());
			webhookConfig.setWebHookName(webhookConfiguration.getWebHookName());
			webhookConfig.setToolName(webhookConfiguration.getToolName());
			webhookConfig.setMQChannel(webhookConfiguration.getMQChannel());
			webhookConfig.setSubscribeStatus(webhookConfiguration.getSubscribeStatus());
			webhookConfig.setResponseTemplate(webhookConfiguration.getResponseTemplate());
			getSession().update(webhookConfig);
		} else {
			
			getSession().save(webhookConfiguration);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
	}
	
	
	
	
	public Boolean saveWebHookConfiguration(WebHookConfig webhookConfiguration) throws InsightsCustomException {
		log.error("webhookConfiguration");
		log.error(webhookConfiguration.getDataFormat());
		Query<WebHookConfig> createQuery = getSession().createQuery(
				"FROM WebHookConfig WH WHERE WH.webhookName = :webhookName",
				WebHookConfig.class);
		log.error("excuted");
		createQuery.setParameter("webhookName", webhookConfiguration.getWebHookName());
		List<WebHookConfig> resultList = createQuery.getResultList();
		log.error(resultList);
		WebHookConfig webhookConfig = null;
		if(resultList != null && !resultList.isEmpty()){
			webhookConfig = resultList.get(0);
		}
		
		getSession().beginTransaction();
		if (webhookConfig != null) {
			
			throw new InsightsCustomException("Webhook name already exists.");
		} else {
			
			getSession().save(webhookConfiguration);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
	}
	
	
	public List<WebHookConfig> getAllWebHookConfigurations() {
		Query<WebHookConfig> createQuery = getSession().createQuery("FROM WebHookConfig WH", WebHookConfig.class);
		List<WebHookConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}
	
	
	
	
	
	public WebHookConfig loadWebHookConfiguration(String webhookName) {
		Query<WebHookConfig> loadQuery = getSession().createQuery("FROM WebHookConfig SC WHERE SC.webhookName = :webhookName", WebHookConfig.class);
		loadQuery.setParameter("webhookName", webhookName);
		List<WebHookConfig> results = loadQuery.getResultList();
		WebHookConfig webhookConfiguration = null;
		if (results != null && !results.isEmpty()) {
			webhookConfiguration = results.get(0);
		}
		terminateSession();
		terminateSessionFactory();
		return webhookConfiguration;
	}
	
	
	public List<WebHookConfig> deleteWebhookConfigurations(String webhookName) {
		Query<WebHookConfig> createQuery = getSession().createQuery(
				"FROM WebHookConfig a WHERE a.webhookName = :webhookName",
				WebHookConfig.class);
		createQuery.setParameter("webhookName", webhookName);
		WebHookConfig webhookConfig = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(webhookConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return getAllWebHookConfigurations();
	}
}
