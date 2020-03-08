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

import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

import com.cognizant.devops.platforminsightswebhook.config.WebHookMessagePublisher;
import com.cognizant.devops.platforminsightswebhook.events.WebHookHandlerServlet;

@Configuration
@ComponentScan(basePackages = { "com.cognizant.devops.platforminsightswebhook.*" })
public class WebConfig {
	
	private static Logger LOG = LogManager.getLogger(WebConfig.class);

	@Bean(name = "appPropertiesLoad")
	@Order(1)
	public AppProperties appPropertiesLoad() {
		LOG.debug(" AppProperties  Bean .....");
		return new AppProperties();
	}

	/**
	 * Used to initilize Rabbitq Connection
	 * Webhook Servlet initilizition
	 * 
	 * @throws Exception
	 * 
	 */
	@Bean
	@DependsOn("appPropertiesLoad")
	public ServletRegistrationBean<HttpServlet> webhookServlet() {
		LOG.debug(
				" In webhookServlet  ======== instanceName ={} host = {} user = {} passcode = {} exchangeName= {} serverPort = {} context = {}",
				AppProperties.instanceName, AppProperties.mqHost, AppProperties.mqUser, AppProperties.mqPassword,
				AppProperties.mqExchangeName,
				ServerProperties.port, ServerProperties.contextPath);
		try {
			WebHookMessagePublisher.getInstance().initilizeMq();
		} catch (Exception e) {
			LOG.error("Unable to connect to RabbitMQ, Please check RabbitMQ service ");
			throw new RuntimeException("Unable to connect to RabbitMQ, Please check RabbitMQ service ");
		}
		LOG.debug(" start servelet registration in webhookServlet ");
		ServletRegistrationBean<HttpServlet> servRegBean = new ServletRegistrationBean<>();
		WebHookHandlerServlet webhookEvent = new WebHookHandlerServlet();
		servRegBean.setServlet(webhookEvent);
		servRegBean.addUrlMappings("/insightsDevOpsWebHook/*");
		servRegBean.setLoadOnStartup(1);
		return servRegBean;
	}



}