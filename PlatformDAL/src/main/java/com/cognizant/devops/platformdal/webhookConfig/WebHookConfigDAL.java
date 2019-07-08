package com.cognizant.devops.platformdal.webhookConfig;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;


public class WebHookConfigDAL  extends BaseDAL{
	private static final Logger log = LogManager.getLogger(	WebHookConfigDAL.class);
	public Boolean saveWebHookConfiguration(WebHookConfig webhookConfiguration) {
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
			getSession().update(webhookConfig);
		} else {
			//webhookConfiguration.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
			getSession().save(webhookConfiguration);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
	}
}
