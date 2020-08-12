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
package com.cognizant.devops.platformreports.assessment.email;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.core.email.EmailConstants;
import com.cognizant.devops.platformreports.assessment.datamodel.MailReport;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
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
			returnMessage.addProperty("reportName", mailReportDTO.getAsseementreportname());
			returnMessage.addProperty("message", e.getMessage());
		}
		return returnMessage;
	}

	public boolean setEmailInfoAndSendEmail(MailReport mail) {
		Properties props = System.getProperties();
		boolean isMailSent = Boolean.FALSE;
		props.put(EmailConstants.SMTPHOST, emailConf.getSmtpHostServer());
		props.put(EmailConstants.SMTPPORT, emailConf.getSmtpPort());
		props.put("mail.smtp.auth", emailConf.getIsAuthRequired());
		props.put("mail.smtp.starttls.enable", emailConf.getSmtpStarttlsEnable());
		Session session = Session.getDefaultInstance(props);
		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setSentDate(new Date());
			msg.setSubject(mail.getAsseementreportname() + " " + mail.getTimeOfReportGeneration());
			msg.setFrom(new InternetAddress(emailConf.getMailFrom(), EmailConstants.NOREPLY));
			msg.setRecipients(Message.RecipientType.TO, mail.getMailTo().stream().toArray(InternetAddress[]::new));
			msg.addHeader(EmailConstants.CONTENTTYPE, EmailConstants.CHARSET);
			msg.addHeader(EmailConstants.FORMAT, EmailConstants.FLOWED);
			msg.addHeader(EmailConstants.ENCODING, EmailConstants.BIT);
			msg = attachMsgBodyAndAttachFile(msg, mail);
			sendFinalEmail(session, msg);
			isMailSent = Boolean.TRUE;
		} catch (MessagingException e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Unable to create Mime Message ===== {} ", e);
			throw new InsightsJobFailedException("Unable to create Mime Message");
		} catch (UnsupportedEncodingException e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Message Encoding not supported ===== {} ", e);
			throw new InsightsJobFailedException("Message Encoding not supported!");
		} catch (Exception e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Message  {} ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
		return isMailSent;
	}

	public MimeMessage attachMsgBodyAndAttachFile(MimeMessage msg, MailReport mail) {
		try {
			String htmlText = String.join(System.getProperty("line.separator"), "<label>Dear User,</label>",
					"<p>\n" + "\r\n" + "Please find attached Assessment Report <b>" + mail.getAsseementreportname()
							+ " </b>generated on " + mail.getTimeOfReportGeneration() + " </p><br>",
					"<p>Regards,<br> OneDevops Team</p>",
					"<p>\n" + "** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**</p>");
			Multipart multipart = new MimeMultipart();
			// Create the attachment part
			BodyPart messageBodyPart = new MimeBodyPart();
			((MimeBodyPart) messageBodyPart).attachFile(mail.getReportFilePath());
			multipart.addBodyPart(messageBodyPart);//
			// Create the HTML Part
			BodyPart htmlBodyPart = new MimeBodyPart();
			htmlBodyPart.setContent(htmlText, "text/html");
			multipart.addBodyPart(htmlBodyPart);
			// Set the Multipart's to be the email's content
			msg.setContent(multipart);
			log.debug("Workflow Detail ==== ReportEmailSubscriber  Mime Message created =====  ");
		} catch (MessagingException | IOException e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Unable to create Mime Message ===== {} ", e);
			throw new InsightsJobFailedException("Unable to create Mime Message");
		}
		return msg;
	}

	public void sendFinalEmail(Session session, MimeMessage msg) {
		Transport transport = null;
		try {
			transport = session.getTransport();
			log.debug("Workflow Detail ==== ReportEmailSubscriber Sending Email ===== ");
			transport.connect(emailConf.getSmtpHostServer(), emailConf.getSmtpUserName(), emailConf.getSmtpPassword());
			transport.sendMessage(msg, msg.getAllRecipients());
			log.debug("Workflow Detail ==== ReportEmailSubscriber Email Sent ===== ");
		} catch (MailConnectException ex) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Error while sending email ===== {} ", ex);
			throw new InsightsJobFailedException("Unable to sent Message, Connetion Refused ");
		} catch (Exception ex) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Error while sending email ===== {} ", ex);
			throw new InsightsJobFailedException("Error while sending an email");
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					log.error("Workflow Detail ==== ReportEmailSubscriber Unable to close SMTP connection ===== {} ",
							e);
				}
			}
		}
	}

}
