
package com.cognizant.devops.insightsemail.core;

import java.io.Serializable;

public class Mail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6584480569886905118L;

	private String mailFrom;
	
	private String mailTo;
	
	private String contentType;
	
	private String templateName;
	
	private String subject;

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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	

}
