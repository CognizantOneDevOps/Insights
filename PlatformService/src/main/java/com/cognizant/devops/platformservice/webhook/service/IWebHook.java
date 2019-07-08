package com.cognizant.devops.platformservice.webhook.service;

import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;

public interface IWebHook {

	public Boolean saveWebHookConfiguration(String webhookname,String toolName,String eventname,String dataformat,String mqchannel,Boolean subscribestatus);
	//public WebHookConfig loadWebHookConfiguration(String settingsType);
}
