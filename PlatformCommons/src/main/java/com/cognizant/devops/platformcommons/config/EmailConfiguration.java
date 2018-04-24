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

package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

public class EmailConfiguration implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1942949927527968050L;
	private Boolean sendEmailEnabled = false;
	private String smtpHostServer;
	private String smtpPort;
	private String smtpUserName;
	private String smtpPassword;
	private Boolean isAuthRequired;
	private Boolean smtpStarttlsEnable;
	private String emailVelocityTemplate;
	private String mailFrom;
	private String mailTo;
	private String subject;
	private String logo;
	private String line;
	private String footerLogo;
	
	
	public Boolean getSendEmailEnabled() {
		return sendEmailEnabled;
	}
	public void setSendEmailEnabled(Boolean sendEmailEnabled) {
		this.sendEmailEnabled = sendEmailEnabled;
	}
	public String getSmtpHostServer() {
		return smtpHostServer;
	}
	public void setSmtpHostServer(String smtpHostServer) {
		this.smtpHostServer = smtpHostServer;
	}
	public String getSmtpPort() {
		return smtpPort;
	}
	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}
	public String getSmtpUserName() {
		return smtpUserName;
	}
	public void setSmtpUserName(String smtpUserName) {
		this.smtpUserName = smtpUserName;
	}
	public String getSmtpPassword() {
		return smtpPassword;
	}
	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}
	public Boolean getIsAuthRequired() {
		return isAuthRequired;
	}
	public void setIsAuthRequired(Boolean isAuthRequired) {
		this.isAuthRequired = isAuthRequired;
	}
	public Boolean getSmtpStarttlsEnable() {
		return smtpStarttlsEnable;
	}
	public void setSmtpStarttlsEnable(Boolean smtpStarttlsEnable) {
		this.smtpStarttlsEnable = smtpStarttlsEnable;
	}
	public String getEmailVelocityTemplate() {
		return emailVelocityTemplate;
	}
	public void setEmailVelocityTemplate(String emailVelocityTemplate) {
		this.emailVelocityTemplate = emailVelocityTemplate;
	}
	public String getMailFrom() {
		return mailFrom;
	}
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}
	public String getMailTo() {
		return mailTo;
	}
	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public String getFooterLogo() {
		return footerLogo;
	}
	public void setFooterLogo(String footerLogo) {
		this.footerLogo = footerLogo;
	}

}
