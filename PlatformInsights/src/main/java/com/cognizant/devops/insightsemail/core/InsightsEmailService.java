package com.cognizant.devops.insightsemail.core;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


import com.cognizant.devops.insightsemail.configs.EmailConstants;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsEmailService {
	EmailConfiguration emailConfiguration=ApplicationConfigProvider.getInstance().getEmailConfiguration();

	public void sendEmail(Mail mail) throws MessagingException, UnsupportedEncodingException {
		String smtpHostServer=emailConfiguration.getSmtpHostServer();
		JsonObject json=getInferenceDetails();
		StringWriter writer = populateTemplate(json); 
		Properties props = System.getProperties();
		props.put(EmailConstants.SMTPHOST, smtpHostServer);
		Session session = Session.getInstance(props, null);
			MimeMessage msg = new MimeMessage(session);
			msg.addHeader(EmailConstants.CONTENTTYPE, EmailConstants.CHARSET);
			msg.addHeader(EmailConstants.FORMAT, EmailConstants.FLOWED);
			msg.addHeader(EmailConstants.ENCODING, EmailConstants.BIT);
			msg.setFrom(new InternetAddress(mail.getMailFrom(),EmailConstants.NOREPLY));
			msg.setReplyTo(InternetAddress.parse(mail.getMailTo(), false));
			msg.setSubject(mail.getSubject(), EmailConstants.UTF);
			msg.setContent(writer.toString(), EmailConstants.HTML);
			msg.setSentDate(new Date());
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getMailTo(), false));
			//	Transport.send(msg);
	}

	private JsonObject getInferenceDetails() {
		RestTemplate restTemplate = new RestTemplate();
		String credential = emailConfiguration.getRestUserName()+":"+
							emailConfiguration.getRestPassword();
		byte[] credsBytes = credential.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(credsBytes);
		String base64Creds = new String(base64CredsBytes);
		HttpHeaders headers = new HttpHeaders();
		headers.set(EmailConstants.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(EmailConstants.AUTHORIZATION, "Basic "+base64Creds);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String restUrl=emailConfiguration.getRestUrl()+"/PlatformService/insights/inferences";
		HttpEntity<String> response = restTemplate.exchange(restUrl,HttpMethod.GET,entity,String.class);
		JsonParser parser = new JsonParser(); 
		JsonObject resultJson=new JsonObject();
		resultJson= (JsonObject) parser.parse(response.getBody()).getAsJsonObject();

		return resultJson;
	}
	
	private StringWriter populateTemplate(JsonObject json) {
		StringWriter stringWriter = new StringWriter();
		JsonArray array = json.get(EmailConstants.DATA).getAsJsonArray();
		for(JsonElement element : array){
			
			int noOfPositives = 0;
			int noOfNegatives =0;
			int noOfNeutral= 0;
			JsonObject output = element.getAsJsonObject();
			JsonArray arrayinfer = output.get(EmailConstants.INFERENCEDETAILS).getAsJsonArray();
			for(JsonElement elementinfer : arrayinfer){
				
				JsonObject outputinfer = elementinfer.getAsJsonObject();
				String sentiment=outputinfer.get(EmailConstants.SENTIMENT).getAsString() ;
				 if(EmailConstants.NEUTRAL.equals(sentiment)){
	            	 noOfNeutral=noOfNeutral+1;
	             }else if(EmailConstants.POSITIVE.equals(sentiment)){
	            	 noOfPositives=noOfPositives+1;
	            	 
	             }else if(EmailConstants.NEGATIVE.equals(sentiment)){
	            	 noOfNegatives=noOfNegatives+1;
	             }
				
			}
			 element.getAsJsonObject().addProperty(EmailConstants.NOOFNEUTRAL,noOfNeutral);
			 element.getAsJsonObject().addProperty(EmailConstants.NOOFPOSITIVE,noOfPositives);
			 element.getAsJsonObject().addProperty(EmailConstants.NOOFNEGATIVE,noOfNegatives);
		}
		Template template = initializeTemplate();
		VelocityContext context = new VelocityContext(); 
		context.put(EmailConstants.ACCORDIANDATA,array);
		template.merge(context,stringWriter);
		System.out.println(stringWriter.toString());
		return stringWriter;
	}

	private Template initializeTemplate() {
		VelocityEngine velocityEngine=new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,EmailConstants.CLASSPATH);
		velocityEngine.setProperty(EmailConstants.LOADER,ClasspathResourceLoader.class.getName());
		velocityEngine.init();      
		Template template = velocityEngine.getTemplate("templates/inference.vm");
		return template;
	}
	
	public static void main(String[] args)
	{	
		/**ApplicationConfigCache.loadConfigCache();
		InsightsEmailService services=new InsightsEmailService();
		Mail mail=new Mail();
		mail.setMailTo("");
		mail.setMailFrom("");
		mail.setSubject("insights");
		try {
			services.sendEmail(mail);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}**/

	}


}
