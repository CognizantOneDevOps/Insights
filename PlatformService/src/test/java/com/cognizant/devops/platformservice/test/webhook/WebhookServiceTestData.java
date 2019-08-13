package com.cognizant.devops.platformservice.test.webhook;

public class WebhookServiceTestData {

	
	String webhookname = "git_webhook";
	String toolName = "GIT";
	String labelDisplay = "SCM:GIT:DATA";
	String dataformat = "json";
	String mqchannel = "IPW_git_webhook";
	Boolean subscribestatus = true;
	String responseTemplate = "head_commit.timestamp=commitTime,head_commit.author.name=authorName";
}
