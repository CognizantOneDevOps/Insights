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

package com.cognizant.devops.insightsemail.job;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.insightsemail.core.InsightsEmailService;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.core.email.Mail;


public class AlertEmailJobExecutor implements Job,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7027158833277846218L;
	private static final Logger log = LogManager.getLogger(AlertEmailJobExecutor.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		sendMail();
	}

	public void sendMail() {
		
		//ApplicationConfigCache.loadConfigCache();
		EmailConfiguration emailConfiguration=ApplicationConfigProvider.getInstance().getEmailConfiguration();
		InsightsEmailService service = new InsightsEmailService();
		Mail mail=new Mail();
		mail.setMailTo(emailConfiguration.getMailTo());
		mail.setMailFrom(emailConfiguration.getMailFrom());
		mail.setSubject(emailConfiguration.getSubject());
		try {
			service.sendEmail(mail);
			log.debug("Email has been sent successfully..");
		} catch (UnsupportedEncodingException e) {
			log.error("Encoding exception :"+e);
		} catch (MessagingException e) {
			log.error("Mesaaging exception"+e);
		}
	}
	
	/*public static void main(String[] a){
		
		AlertEmailJobExecutor exe=new AlertEmailJobExecutor();
		exe.sendMail();
		
	}*/

}
