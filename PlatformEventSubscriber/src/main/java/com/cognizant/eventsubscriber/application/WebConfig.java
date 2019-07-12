package com.cognizant.eventsubscriber.application;

import javax.servlet.http.HttpServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.congnizant.eventsubscriber.events.Event;


@Configuration
public class WebConfig {
	@Bean
	public ServletRegistrationBean<HttpServlet> webhookServlet() {
		ServletRegistrationBean<HttpServlet> servRegBean = new ServletRegistrationBean<>();
		Event webhookEvent = new Event();
		servRegBean.setServlet(webhookEvent);
		servRegBean.addUrlMappings("/webhookEvent/*");
		servRegBean.setLoadOnStartup(1);
		return servRegBean;
	}

}