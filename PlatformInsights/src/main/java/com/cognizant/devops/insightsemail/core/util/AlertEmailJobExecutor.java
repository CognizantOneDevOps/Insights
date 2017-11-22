package com.cognizant.devops.insightsemail.core.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.insightsemail.configs.EmailConstants;
import com.cognizant.devops.insightsemail.core.InsightsEmailService;
import com.cognizant.devops.insightsemail.core.Mail;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;


public class AlertEmailJobExecutor implements Job,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7027158833277846218L;
	private static final Logger log = Logger.getLogger(AlertEmailJobExecutor.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		sendMail();
	}

	private void sendMail() {
		
		ApplicationConfigCache.loadConfigCache();
		EmailConfiguration emailConfiguration=ApplicationConfigProvider.getInstance().getEmailConfiguration();
		InsightsEmailService service =new InsightsEmailService();
		Mail mail=new Mail();
		mail.setMailTo(emailConfiguration.getMailTo());
		mail.setMailFrom(emailConfiguration.getMailFrom());
		mail.setSubject(EmailConstants.SUBJECT);
		try {
			service.sendEmail(mail);
		} catch (UnsupportedEncodingException e) {
			log.debug("Encoding exception :"+e);
		} catch (MessagingException e) {
			log.debug("Mesaaging exception"+e);
		}
	}
	
	public static void main(String[] a){
		
		/**AlertEmailJobExecutor exe=new AlertEmailJobExecutor();
		exe.sendMail();
		**/
		
	}

}
