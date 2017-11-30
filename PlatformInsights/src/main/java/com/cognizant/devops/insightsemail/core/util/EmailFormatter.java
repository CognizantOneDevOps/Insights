package com.cognizant.devops.insightsemail.core.util;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.cognizant.devops.platformcommons.core.email.EmailConstants;
import com.google.gson.JsonArray;

public class EmailFormatter {
	
	private final static EmailFormatter emailFormatter = new EmailFormatter();
	
	private EmailFormatter() {
	}
	
	public static EmailFormatter getInstance() {
		return emailFormatter;
	}

	public StringWriter populateTemplate(JsonArray array,String templateName) {
		StringWriter stringWriter = new StringWriter();
		
		Template template = initializeTemplate(templateName);
		
		VelocityContext context = new VelocityContext(); 
		context.put(EmailConstants.ACCORDIANDATA,array);
		template.merge(context,stringWriter);
		System.out.println(stringWriter.toString());
		return stringWriter;
	}

	private Template initializeTemplate(String templateName) {
		VelocityEngine velocityEngine=new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,EmailConstants.CLASSPATH);
		velocityEngine.setProperty(EmailConstants.LOADER,ClasspathResourceLoader.class.getName());
		velocityEngine.init();      
		Template template = velocityEngine.getTemplate(templateName);
		return template;
	}
}
