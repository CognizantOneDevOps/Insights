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

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.cognizant.devops.platforminsightswebhook.config.WebHookMessagePublisher;
import com.cognizant.devops.platforminsightswebhook.events.WebHookHandlerServlet;

@Configuration
@ComponentScan(basePackages = { "com.cognizant.devops.platforminsightswebhook.*" })
public class WebConfig {

	@Bean
	public ServletRegistrationBean<HttpServlet> webhookServlet() {
			ServletRegistrationBean<HttpServlet> servRegBean = new ServletRegistrationBean<>();
			WebHookHandlerServlet webhookEvent = new WebHookHandlerServlet();
			servRegBean.setServlet(webhookEvent);
			servRegBean.addUrlMappings("/webhookEvent/*");
			servRegBean.setLoadOnStartup(1);
			return servRegBean;
	}
}