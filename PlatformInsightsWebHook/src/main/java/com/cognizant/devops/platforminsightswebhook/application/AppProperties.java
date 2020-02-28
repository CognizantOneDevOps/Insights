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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.cognizant.devops.platforminsightswebhook.config.WebHookConstants;

@Configuration
@ConfigurationProperties(prefix = "app")
@PropertySource("file:${properties.basedir}/" + WebHookConstants.WEBHOOK_PROPERTY_FILE_NAME)
public class AppProperties {
	private static Logger LOG = LogManager.getLogger(AppProperties.class);
	
	@Value("${app.mqHost}")
	public static String mqHost;
	
	@Value("${app.mqUser}")
	public static String mqUser;
	
	@Value("${app.mqPassword}")
	public static String mqPassword;
	
	@Value("${app.mqExchangeName}")
	public static String mqExchangeName;

	@Value("${app.instance_name}")
	public static String instanceName;

	public static String getMqHost() {
		return mqHost;
	}

	public  void setMqHost(String mqHost) {
		this.mqHost = mqHost;
	}

	public  String getMqUser() {
		return mqUser;
	}

	public  void setMqUser(String mqUser) {
		this.mqUser = mqUser;
	}

	public  String getMqPassword() {
		return mqPassword;
	}

	public  void setMqPassword(String mqPassword) {
		this.mqPassword = mqPassword;
	}

	public String getMqExchangeName() {
		return mqExchangeName;
	}

	public void setMqExchangeName(String mqExchangeName) {
		this.mqExchangeName = mqExchangeName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

}
