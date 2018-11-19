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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
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

import com.cognizant.devops.platformcommons.config.EmailConfiguration;

public class EmailUtil {
	private static final Logger LOG = LogManager.getLogger(EmailUtil.class);

	private static final EmailUtil emailUtil = new EmailUtil();

	private EmailUtil() {

	}

	public static EmailUtil getInstance() {
		return emailUtil;
	}

	public void sendEmail(Mail mail,EmailConfiguration emailConf, String emailBody) throws MessagingException,
																					IOException,UnsupportedEncodingException {

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
		msg.setSentDate(new Date());
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getMailTo(), false));
		MimeBodyPart contentPart = new MimeBodyPart();
		contentPart.setContent(emailBody, EmailConstants.HTML);

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(contentPart);

		String imageId="logoImage";
		String imagePath="/img/Insights.jpg";
		MimeBodyPart imagePart = generateContentId(imageId, imagePath);
		multipart.addBodyPart(imagePart);

		imageId="footerImage";
		imagePath="/img/FooterImg.jpg";
		MimeBodyPart imagePart_1 = generateContentId(imageId, imagePath);
		multipart.addBodyPart(imagePart_1);

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

	private MimeBodyPart generateContentId(String imageId, String path) throws IOException, MessagingException
	{
		MimeBodyPart imagePart = new MimeBodyPart();
		InputStream stream = getClass().getResourceAsStream(path);
		DataSource fds = null;
		try {
			fds = new ByteArrayDataSource(stream,"image/jpg");
			imagePart.setDataHandler(new DataHandler(fds));
			imagePart.setContentID("<"+ imageId +">");
			imagePart.setHeader("Content-Type", "image/jpg");
		} catch (IOException ex) {
			LOG.debug(ex);
			throw ex;
		} catch (MessagingException ex) {
			LOG.debug(ex);
			throw ex;
		}

		return imagePart;
	}

}
