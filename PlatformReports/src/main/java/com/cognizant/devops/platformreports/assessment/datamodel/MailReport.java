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
package com.cognizant.devops.platformreports.assessment.datamodel;

import java.io.Serializable;
import java.util.List;

import javax.mail.internet.InternetAddress;

public class MailReport implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6584480569886905118L;
	private List<InternetAddress> mailTo;	
	private String asseementreportname;
	private String reportFilePath;	
	private String timeOfReportGeneration;


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

}