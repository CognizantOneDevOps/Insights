package com.cognizant.devops.platformwebhookengine.parser;

public class InsightsWebhookParserFactory {
	
		public static InsightsWebhookParserInterface getParserInstance(String toolName)
		{
			if(toolName.equals("SONAR"))
			{
				return new InsightsGeneralParser();
			}
			else {
			return new InsightsGeneralParser();
			}
		}
	
}
