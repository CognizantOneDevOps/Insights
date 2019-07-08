package com.cognizant.devops.platformservice.webhook.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;

import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;


@Service("webhookConfigurationService")
public class WebHookService implements IWebHook{
	private static final Logger log = LogManager.getLogger(	WebHookService.class);
	@Override
	 	
		public Boolean saveWebHookConfiguration(String webhookname,String toolName,String eventname,String dataformat,String mqchannel,Boolean subscribestatus) 
		{
		
		WebHookConfig webHookConfig = populateWebHookConfiguration(webhookname,toolName,eventname,dataformat,mqchannel,subscribestatus);
		
		log.error(webHookConfig.getDataFormat());
		log.error(webHookConfig.getEventName());
		log.error(webHookConfig.getWebHookName());
		log.error(webHookConfig.getMQChannel());
		log.error(webHookConfig.getSubscribeStatus());
		WebHookConfigDAL webhookConfigurationDAL = new WebHookConfigDAL();	
		
		
		return webhookConfigurationDAL.saveWebHookConfiguration(webHookConfig);		
	   
		}	
 
	 
	 private WebHookConfig populateWebHookConfiguration(String webhookname,String toolName,String eventname,String dataformat,String mqchannel,Boolean subscribestatus) {
			WebHookConfig webhookConfiguration = new WebHookConfig();
			
			//String updatedSettingsJson = updateNextRunTimeValue(settingsJson);
			webhookConfiguration.setDataFormat(dataformat);
			webhookConfiguration.setEventName(eventname);
			webhookConfiguration.setToolName(toolName);;
			webhookConfiguration.setMQChannel(mqchannel);
			webhookConfiguration.setWebHookName(webhookname);
			webhookConfiguration.setSubscribeStatus(subscribestatus);
			return webhookConfiguration;
		}
}
