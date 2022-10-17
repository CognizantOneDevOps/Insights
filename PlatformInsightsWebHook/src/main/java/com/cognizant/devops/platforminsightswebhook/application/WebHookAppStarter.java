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


import java.io.File;

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
	private static Logger log = LogManager.getLogger(WebHookAppStarter.class);
	public static final String PROPERTIES_BASEDIR="properties.basedir";
    public static void main(String[] args) {
		log.debug(" Inside Webhook Message Publisher ... ");
		String configFile = System.getenv().get("INSIGHTS_HOME") + File.separator + ".InSights" + File.separator ;
		System.setProperty(PROPERTIES_BASEDIR, configFile);
		log.debug(" Spring properties location  {} " , System.getProperty(PROPERTIES_BASEDIR));
		ApplicationContext applicationContext = SpringApplication.run(WebHookAppStarter.class, args);
		log.debug(" message Application Name {} instance name {} " , applicationContext.getApplicationName() ,
				 AppProperties.instanceName);
		SubscriberStatusLogger.getInstance()
				.createSubsriberStatusNode(
						" Service Started with instance name " + AppProperties.instanceName
								+ " and context path as " + applicationContext.getApplicationName() + ".",
				WebHookConstants.SUCCESS);
	}
}