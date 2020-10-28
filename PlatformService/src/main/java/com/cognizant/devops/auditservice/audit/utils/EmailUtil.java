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
package com.cognizant.devops.auditservice.audit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;

/**
 * 
 * Email utility .
 *
 */
public class EmailUtil {

	private static final Logger log = LoggerFactory.getLogger(EmailUtil.class.getName());

	//Load Mail Server Details
	
	EmailConfiguration emailConfiguration = ApplicationConfigProvider.getInstance().getEmailConfiguration();

	
	static final String BODY = String.join(
			System.getProperty("line.separator"),
			"<h1>Insights Report</h1>",
			"<p>This email was sent by Insights scheduler .Please do not reply. </p>"
			);

	/**
	 * Send Mail with MIME attachment.
	 * This method is specific to report generation based on subscribers.
	 * 
	 * @param attachment
	 * @param attachmentName
	 * @param subscribers 
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void sendEmailWithAttachment(InputStream attachment, String attachmentName, 
			String subscribers) throws AddressException, MessagingException, IOException{
		Properties props = configInfo();
		Session session = Session.getDefaultInstance(props);
		MimeMessage msg = messageInfo(session, subscribers, attachmentName);
		mimeBodyAttachment(attachment, attachmentName, msg);
		sendMail(session, msg);
	}

	/**
	 * Send Mail based on Mime msg.
	 * @param session
	 * @param msg
	 * @throws NoSuchProviderException
	 * @throws MessagingException
	 */
	private void sendMail(Session session, MimeMessage msg) throws NoSuchProviderException, MessagingException {
		// Create a transport.
		Transport transport = session.getTransport();
		// Send the message.
		try
		{
			log.info("Sending Mail...");
			// Connect to Mail server using the SMTP username and password specified .
			transport.connect(emailConfiguration.getSmtpHostServer(), emailConfiguration.getSmtpUserName(), emailConfiguration.getSmtpPassword());
			//transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			log.info("Email sent!");
		}
		catch (Exception ex) {
			ex.printStackTrace();
			log.error("Error message: " + ex.getMessage());
		}
		finally
		{
			// Close and terminate the connection.
			transport.close();
		}
	}

	/**
	 * Attach file to Mime body based on response and name.
	 * @param attachment
	 * @param attachmentName
	 * @param msg
	 * @throws MessagingException
	 * @throws IOException 
	 */
	private void mimeBodyAttachment(InputStream attachment, String attachmentName, MimeMessage msg) throws MessagingException, IOException {
		MimeBodyPart att = new MimeBodyPart(); 
		ByteArrayDataSource bds = new ByteArrayDataSource(attachment,"application/pdf"); 
		//DataSource bds = new FileDataSource(new File("test.pdf").getAbsolutePath()); Attachment for physical file.
		att.setDataHandler(new DataHandler(bds)); 
		att.setFileName(attachmentName); 

		//Add attachment to mimebody
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(att);
		msg.setContent(mp);
		
		MimeBodyPart bodyMessagePart = new MimeBodyPart();
		bodyMessagePart.setContent(BODY, "text/html");
		mp.addBodyPart(bodyMessagePart);
		//msg.setContent(mp);
	}

	/**
	 * Create a message with the specified information
	 * @param session
	 * @param subscribers 
	 * @param subject 
	 * @return MimeMessage
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 * @throws AddressException
	 */
	private MimeMessage messageInfo(Session session, String subscribers, String subject)
			throws MessagingException, UnsupportedEncodingException, AddressException {
		log.info("List of subscribers are = "+subscribers);
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(emailConfiguration.getMailFrom()));
		//msg.setRecipient(Message.RecipientType.TO, new InternetAddress(subscribers));
		msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(subscribers));
		msg.setSubject(subject.substring(0,subject.indexOf(".")));
		//msg.setContent(BODY,"text/html");
		return msg;
	}

	/**
	 * Create a Properties object to contain connection configuration information.
	 * @return Properties
	 */
	private Properties configInfo() {
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", emailConfiguration.getSmtpPort()); 
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		return props;
	}

}
