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
package com.cognizant.devops.platforminsightswebhook.application;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import com.cognizant.devops.platforminsightswebhook.config.WebHookConstants;
import com.cognizant.devops.platforminsightswebhook.message.core.SubscriberStatusLogger;


@SpringBootApplication
@ServletComponentScan
@EnableConfigurationProperties(AppProperties.class)
public class WebHookAppStarter {
	private static Logger LOG = LogManager.getLogger(WebHookAppStarter.class);
    public static void main(String[] args) {
		LOG.debug(" Inside Webhook Message Publisher ... ");
		System.setProperty("properties.basedir", ".");
		for (String argCommandLine : args) {
			LOG.debug(argCommandLine);
			if (argCommandLine.contains("config.file.location")) {
				String[] argArray = argCommandLine.split("=");
				System.setProperty("properties.basedir", argArray[1]);
			}
		}
		LOG.debug(" Spring properties location  " + System.getProperty("properties.basedir"));
		ApplicationContext applicationContext = SpringApplication.run(WebHookAppStarter.class, args);
		LOG.debug(" message Application Name " + applicationContext.getApplicationName() + " instance name "
				+ AppProperties.instanceName);
		SubscriberStatusLogger.getInstance()
				.createSubsriberStatusNode(
						" Service Started with instance name " + AppProperties.instanceName
								+ " and context path as " + applicationContext.getApplicationName() + ".",
				WebHookConstants.SUCCESS);
	}
}