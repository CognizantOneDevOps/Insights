package com.cognizant.devops.platformservice.webhook.service;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;

public interface IWebHook {

	public Boolean saveWebHookConfiguration(String webhookname,String toolName,String eventname,String dataformat,String mqchannel,Boolean subscribestatus, String responseTemplate) throws InsightsCustomException;
	//public WebHookConfig loadWebHookConfiguration(String settingsType);
}
