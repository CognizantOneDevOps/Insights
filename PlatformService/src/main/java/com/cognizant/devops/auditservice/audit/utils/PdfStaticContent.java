package com.cognizant.devops.auditservice.audit.utils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="PdfStaticContent")
public class PdfStaticContent {
	String title;
	String version;
	String body;
	
	public String getTitle() {
		return title;
	}
	@XmlElement
	public void setTitle(String title) {
		this.title = title;
	}
	public String getVersion() {
		return version;
	}
	@XmlElement
	public void setVersion(String version) {
		this.version = version;
	}
	public String getBody() {
		return body;
	}
	@XmlElement
	public void setBody(String body) {
		this.body = body;
	}
	
	
}
