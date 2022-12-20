/*******************************************************************************
O * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformworkflow.workflowtask.email;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.core.email.EmailConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.mail.util.MailConnectException;

public class EmailProcesser implements Callable<JsonObject> {
	private static final Logger log = LogManager.getLogger(EmailProcesser.class);

	private static final long serialVersionUID = -4343203101561316774L;
	EmailConfiguration emailConf = ApplicationConfigProvider.getInstance().getEmailConfiguration();
	MailReport mailReportDTO;

	public EmailProcesser(MailReport mailReportDTO) {
		super();
		this.mailReportDTO = mailReportDTO;
	}

	@Override
	public JsonObject call() throws Exception {
		JsonObject returnMessage = new JsonObject();
		try {
			setEmailInfoAndSendEmail(mailReportDTO);
			returnMessage.addProperty("status", "success");
		} catch (Exception e) {
			log.error(e);
			returnMessage.addProperty("status", "error");
			Set<String> keySet = mailReportDTO.getMailAttachments().keySet();
			returnMessage.addProperty("reportsName", keySet.toString());
			returnMessage.addProperty("message", e.getMessage());
		}
		return returnMessage;
	}

	public boolean setEmailInfoAndSendEmail(MailReport mail) throws InsightsCustomException {
		Properties props = System.getProperties();
		boolean isMailSent = false;
		props.put(EmailConstants.SMTPHOST, emailConf.getSmtpHostServer());
		props.put(EmailConstants.SMTPPORT, emailConf.getSmtpPort());
		props.put("mail.smtp.auth", emailConf.getIsAuthRequired());
		props.put("mail.smtp.starttls.enable", emailConf.getSmtpStarttlsEnable());
		Session session = Session.getDefaultInstance(props);
		MimeMessage msg = new MimeMessage(session);
		try {
			long startTime = System.nanoTime();
			msg.setSentDate(new Date());
			msg.setSubject(mail.getSubject());
			msg.setFrom(new InternetAddress(mail.getMailFrom(), EmailConstants.NOREPLY));
			if (mail.getMailTo() != null) {
				msg.setRecipients(Message.RecipientType.TO, mail.getMailTo().stream().toArray(InternetAddress[]::new));
			}
			if (mail.getMailCC() != null) {
				msg.setRecipients(Message.RecipientType.CC, mail.getMailCC().stream().toArray(InternetAddress[]::new));
			}
			if (mail.getMailBCC() != null) {
				msg.setRecipients(Message.RecipientType.BCC,
						mail.getMailBCC().stream().toArray(InternetAddress[]::new));
			}
			msg.addHeader(EmailConstants.CONTENTTYPE, EmailConstants.CHARSET);
			msg.addHeader(EmailConstants.FORMAT, EmailConstants.FLOWED);
			msg.addHeader(EmailConstants.ENCODING, EmailConstants.BIT);
			msg = attachMsgBodyAndAttachFile(msg, mail);
			sendFinalEmail(session, msg);
			isMailSent = true;
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			logDebugStatement(mail,processingTime,"-");			
		} catch (MessagingException e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Unable to create Mime Message ===== ", e);
			logErrorStatement(mail,e.getMessage());		
			throw new InsightsCustomException("Unable to create Mime Message");
		} catch (UnsupportedEncodingException e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Message Encoding not supported ===== ", e);
			logErrorStatement(mail,e.getMessage());			
			throw new InsightsCustomException("Message Encoding not supported!");
		} catch (Exception e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Message ", e);
			logErrorStatement(mail,e.getMessage());				
			throw new InsightsCustomException(e.getMessage());
		}
		return isMailSent;
	}

	public MimeMessage attachMsgBodyAndAttachFile(MimeMessage msg, MailReport mail) throws InsightsCustomException {
		try {
			long startTime = System.nanoTime();
			String htmlText = mail.getMailBody();
			Multipart multipart = new MimeMultipart();
			// Create the attachment part
			if (mail.getMailAttachments()!= null && mail.getMailAttachments().size() > 0) {
				for (Entry<String, byte[]> entry : mail.getMailAttachments().entrySet()) {
					BodyPart messageBodyPart = new MimeBodyPart();
					InputStream inputStream = new ByteArrayInputStream(entry.getValue());
					ByteArrayDataSource bds = new ByteArrayDataSource(inputStream, "application/pdf");
					log.debug("Workflow Detail ==== Attaching file from {} ", mail.getReportFilePath());
					messageBodyPart.setDataHandler(new DataHandler(bds));
					messageBodyPart.setFileName(entry.getKey() + ".pdf");
					multipart.addBodyPart(messageBodyPart);
				}
			}	
		
			// Create the HTML Part
			BodyPart htmlBodyPart = new MimeBodyPart();
			htmlBodyPart.setContent(htmlText, "text/html");
			multipart.addBodyPart(htmlBodyPart);
			// Set the Multipart's to be the email's content
			msg.setContent(multipart);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Workflow Detail ==== ReportEmailSubscriber  Mime Message created =====  ");
			logDebugStatement(mail,processingTime,"-");			
		} catch (MessagingException | IOException e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Unable to create Mime Message =====  ", e);
			logErrorStatement(mail,e.getMessage());			
			throw new InsightsCustomException("Unable to create Mime Message");
		}
		return msg;
	}

	public void sendFinalEmail(Session session, MimeMessage msg) throws InsightsCustomException {
		Transport transport = null;
		try {
			Object content = msg.getContent();
			Address[] allRecipients = msg.getAllRecipients();
			transport = session.getTransport();
			log.debug("Workflow Detail ==== ReportEmailSubscriber Sending Email ===== ");
			transport.connect(emailConf.getSmtpHostServer(), emailConf.getSmtpUserName(), emailConf.getSmtpPassword());
			transport.sendMessage(msg, msg.getAllRecipients());
			log.debug("Workflow Detail ==== ReportEmailSubscriber Email Sent ===== ");
		} catch (MailConnectException ex) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Error while sending email =====  ", ex);
			throw new InsightsCustomException("Unable to sent Message, Connetion Refused ");
		} catch (Exception ex) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Error while sending email =====  ", ex);
			throw new InsightsCustomException("Error while sending an email");
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					log.error("Workflow Detail ==== ReportEmailSubscriber Unable to close SMTP connection ===== ", e);
				}
			}
		}
	}
	
	private void logDebugStatement(MailReport mail,long processingTime,String message)
	{
		mail.getMailAttachments().forEach((attachmentName, fileData) -> {
			log.debug("Type = EmailProcesser mailTo={}  mailCC={} mailBCC={} emailAttachmentName={} "
					+ "reportFilePath={} processingTime={} message={}" ,mail.getMailTo()
					,mail.getMailCC(),mail.getMailBCC(), attachmentName
					,mail.getReportFilePath(),processingTime,message);
		});
		
	}
	
	private void logErrorStatement(MailReport mail,String message)
	{
		mail.getMailAttachments().forEach((attachmentName, fileData) -> {
		log.error("Type = EmailProcesser mailTo={}  mailCC={} mailBCC={} emailAttachmentName={} "
				+ "reportFilePath={}  processingTime={} message={}" ,mail.getMailTo()
				,mail.getMailCC(),mail.getMailBCC(), attachmentName
				,mail.getReportFilePath(),0,message);
		});
	}

}
