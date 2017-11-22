package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

public class EmailConfiguration implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1942949927527968050L;
	private String smtpHostServer;
	private String restUserName;
	private String restPassword;
	private String restUrl;
	private String mailFrom;
	private String mailTo;
	
	public String getSmtpHostServer() {
		return smtpHostServer;
	}
	public void setSmtpHostServer(String smtpHostServer) {
		this.smtpHostServer = smtpHostServer;
	}
	public String getRestUserName() {
		return restUserName;
	}
	public void setRestUserName(String restUserName) {
		this.restUserName = restUserName;
	}
	public String getRestPassword() {
		return restPassword;
	}
	public void setRestPassword(String restPassword) {
		this.restPassword = restPassword;
	}
	public String getRestUrl() {
		return restUrl;
	}
	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
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

}
