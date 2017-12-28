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

package com.cognizant.devops.platformcommons.core.email;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.config.EmailConfiguration;

public class EmailUtil {
	private static final Logger LOG = Logger.getLogger(EmailUtil.class);
	
	private static final EmailUtil emailUtil = new EmailUtil();
	
	private EmailUtil() {
		
	}
	
	public static EmailUtil getInstance() {
		return emailUtil;
	}
	
	public void sendEmail(Mail mail,EmailConfiguration emailConf, String emailBody) throws MessagingException, UnsupportedEncodingException {

		
		Properties props = System.getProperties();
		props.put(EmailConstants.SMTPHOST, emailConf.getSmtpHostServer());
		props.put(EmailConstants.SMTPPORT, emailConf.getSmtpPort());
		props.put("mail.smtp.auth", emailConf.getIsAuthRequired());
		props.put("mail.smtp.starttls.enable", emailConf.getSmtpStarttlsEnable());

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);

		MimeMessage msg = new MimeMessage(session);
		msg.addHeader(EmailConstants.CONTENTTYPE, EmailConstants.CHARSET);
		msg.addHeader(EmailConstants.FORMAT, EmailConstants.FLOWED);
		msg.addHeader(EmailConstants.ENCODING, EmailConstants.BIT);
		msg.setFrom(new InternetAddress(mail.getMailFrom(), EmailConstants.NOREPLY));
		msg.setReplyTo(InternetAddress.parse(mail.getMailTo(), false));
		msg.setSubject(mail.getSubject(), EmailConstants.UTF);
		msg.setContent(emailBody, EmailConstants.HTML);
		msg.setSentDate(new Date());
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getMailTo(), false));
		
/*		URL url =getClass().getClassLoader().getResource("img/Insight.svg");
 		Image imageLine  = null;
		Image imagefooterLogo  = null;
		new ImageIcon(getClass().getClassLoader().getResource(logo));
		try {
			imageLine = ImageIO.read(ClassLoader.getSystemResource(line));
			imagefooterLogo = ImageIO.read(ClassLoader.getSystemResource("img/FooterLogo.svg"));
		} catch (IOException e) {
			LOG.debug(e);
		}*/
		Multipart multipart = new MimeMultipart();
		MimeBodyPart imagePart = new MimeBodyPart();
		try {
			
			imagePart.setHeader("Content-ID", "<cid>");
			imagePart.attachFile("img/Insight.svg");
		} catch (IOException e) {
			LOG.debug(e);
		}
		multipart.addBodyPart(imagePart);
		msg.setContent(multipart);
		
		
		try (Transport transport = session.getTransport();) {
			LOG.debug("Sending email...");

			transport.connect(emailConf.getSmtpHostServer(), emailConf.getSmtpUserName(),emailConf.getSmtpPassword());

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			LOG.debug("Email sent!");
		} catch (Exception ex) {
			LOG.error("Error sending email",ex);
			throw ex;
		} 

	}

}
