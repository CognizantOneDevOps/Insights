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
package com.cognizant.devops.platformworkflow.workflowtask.email;

import java.io.Serializable;
import java.util.List;

import javax.mail.internet.InternetAddress;

public class MailReport implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6584480569886905118L;
	private List<InternetAddress> mailTo;
	private List<InternetAddress> mailCC;
	private List<InternetAddress> mailBCC;
	private String asseementreportname;
	private String reportFilePath;
	private String timeOfReportGeneration;
	private String mailFrom;
	private String subject;
	private String mailBody;

	public String getAsseementreportname() {
		return asseementreportname;
	}

	public void setAsseementreportname(String asseementreportname) {
		this.asseementreportname = asseementreportname;
	}

	public String getReportFilePath() {
		return reportFilePath;
	}

	public void setReportFilePath(String reportFilePath) {
		this.reportFilePath = reportFilePath;
	}

	public List<InternetAddress> getMailTo() {
		return mailTo;
	}

	public void setMailTo(List<InternetAddress> mailTo) {
		this.mailTo = mailTo;
	}

	public String getTimeOfReportGeneration() {
		return timeOfReportGeneration;
	}

	public void setTimeOfReportGeneration(String timeOfReportGeneration) {
		this.timeOfReportGeneration = timeOfReportGeneration;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMailBody() {
		return mailBody;
	}

	public void setMailBody(String mailBody) {
		this.mailBody = mailBody;
	}

	public List<InternetAddress> getMailCC() {
		return mailCC;
	}

	public void setMailCC(List<InternetAddress> mailCC) {
		this.mailCC = mailCC;
	}

	public List<InternetAddress> getMailBCC() {
		return mailBCC;
	}

	public void setMailBCC(List<InternetAddress> mailBCC) {
		this.mailBCC = mailBCC;
	}

}