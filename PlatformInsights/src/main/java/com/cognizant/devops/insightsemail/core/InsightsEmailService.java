/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.cognizant.devops.insightsemail.core;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.cognizant.devops.insightsemail.core.util.EmailFormatter;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.core.email.EmailConstants;
import com.cognizant.devops.platformcommons.core.email.EmailUtil;
import com.cognizant.devops.platformcommons.core.email.Mail;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsEmailService {
	
	private static final Logger LOG = LogManager.getLogger(InsightsEmailService.class);
	EmailConfiguration emailConfiguration = ApplicationConfigProvider.getInstance().getEmailConfiguration();

	public void sendEmail(Mail mail) throws MessagingException, UnsupportedEncodingException {
		
		//LOG.debug("Inside sendEmail method");
		try {
			
			JsonObject json = getInferenceDetails();
			String emailTemplate = emailConfiguration.getEmailVelocityTemplate();
			String emailBody = getFormattedEmailContent(json,emailTemplate); 
			EmailUtil.getInstance().sendEmail(mail, emailConfiguration,emailBody);
			LOG.debug("Email has been sent successfully..");
		} catch (Exception e) {
			LOG.error("Error sending email", e);
		}
	}

	private JsonObject getInferenceDetails() throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		//RestTemplate restTemplate = new RestTemplate();
		RestTemplate restTemplate = restTemplate();
		String credential = ApplicationConfigProvider.getInstance().getUserId()+":"+
				ApplicationConfigProvider.getInstance().getPassword();
		byte[] credsBytes = credential.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(credsBytes);
		String base64Creds = new String(base64CredsBytes);
		HttpHeaders headers = new HttpHeaders();
		headers.set(EmailConstants.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(EmailConstants.AUTHORIZATION, "Basic "+base64Creds);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String restUrl = ApplicationConfigProvider.getInstance().getInsightsServiceURL()+"/PlatformService/insights/inferences";
		//String restUrl = "http://localhost:7080"+"/PlatformService/insights/inferences";
		HttpEntity<String> response = restTemplate.exchange(restUrl,HttpMethod.GET,entity,String.class);
		JsonParser parser = new JsonParser(); 
		JsonObject resultJson=new JsonObject();
		resultJson= (JsonObject) parser.parse(response.getBody()).getAsJsonObject();
		LOG.debug("Insights inference details received from service");
		return resultJson;
	}
	
	private String getFormattedEmailContent(JsonObject json, String emailTemplate) {
	
		JsonArray array = json.get(EmailConstants.DATA).getAsJsonArray();
		for(JsonElement element : array){
			
			int noOfPositives = 0;
			int noOfNegatives =0;
			int noOfNeutral= 0;
			ArrayList<String> positive=new ArrayList<>();
			ArrayList<String> negative=new ArrayList<>();
			ArrayList<String> neutral=new ArrayList<>();
			JsonObject output = element.getAsJsonObject();
			JsonArray arrayinfer = output.get(EmailConstants.INFERENCEDETAILS).getAsJsonArray();
			for(JsonElement elementinfer : arrayinfer){
				JsonObject outputinfer = elementinfer.getAsJsonObject();
				String sentiment=outputinfer.get(EmailConstants.SENTIMENT).getAsString() ;
				String inference=outputinfer.get("inference").getAsString() ;
				 if(EmailConstants.NEUTRAL.equals(sentiment)){
	            	 noOfNeutral=noOfNeutral+1;
	            	 neutral.add(inference);
	            	
	             }else if(EmailConstants.POSITIVE.equals(sentiment)){
	            	 noOfPositives=noOfPositives+1;
	            	 positive.add(inference);
	            	
	            	 
	             }else if(EmailConstants.NEGATIVE.equals(sentiment)){
	            	 noOfNegatives=noOfNegatives+1;
	            	 negative.add(inference);
	            	
	             }
			}

			 element.getAsJsonObject().addProperty(EmailConstants.NOOFNEUTRAL,noOfNeutral);
			 element.getAsJsonObject().addProperty(EmailConstants.NOOFPOSITIVE,noOfPositives);
			 element.getAsJsonObject().addProperty(EmailConstants.NOOFNEGATIVE,noOfNegatives);
		}
		

		StringWriter stringWriter = EmailFormatter.getInstance().populateTemplate(array,emailTemplate);
		return stringWriter.toString();
	}

	public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
	    TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

	    SSLContext sslContext = SSLContexts.custom()
	                    .loadTrustMaterial(null, acceptingTrustStrategy)
	                    .build();

	    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	    CloseableHttpClient httpClient = HttpClients.custom()
	                    .setSSLSocketFactory(csf)
	                    .build();

	    HttpComponentsClientHttpRequestFactory requestFactory =
	                    new HttpComponentsClientHttpRequestFactory();

	    requestFactory.setHttpClient(httpClient);
	    RestTemplate restTemplate = new RestTemplate(requestFactory);
	    return restTemplate;
	 }
	
	public static void main(String[] args)
	{	
		/*ApplicationConfigCache.loadConfigCache();
		InsightsEmailService services=new InsightsEmailService();
		Mail mail=new Mail();
		mail.setMailTo("");
		mail.setMailFrom("");
		mail.setSubject("insights");
		try {
			services.sendEmail(mail);
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.toString());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   */
	}


}


