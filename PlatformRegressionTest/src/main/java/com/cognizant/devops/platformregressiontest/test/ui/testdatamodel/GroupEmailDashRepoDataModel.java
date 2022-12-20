/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.testdatamodel;

public class GroupEmailDashRepoDataModel {
	
	private String batchName;
	private String schedule;
	private String reports;
	private String receiverEmailAddress;
	private String receiverCCEmailAddress;
	private String receiverBCCEmailAddress;
	private String mailSubject;
	private String mailBodyTemplate;
	
	public String getBatchName() {
		return batchName;
	}
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}
	public String getSchedule() {
		return schedule;
	}
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}
	public String getReports() {
		return reports;
	}
	public void setReports(String reports) {
		this.reports = reports;
	}
	public String getReceiverEmailAddress() {
		return receiverEmailAddress;
	}
	public void setReceiverEmailAddress(String receiverEmailAddress) {
		this.receiverEmailAddress = receiverEmailAddress;
	}
	public String getReceiverCCEmailAddress() {
		return receiverCCEmailAddress;
	}
	public void setReceiverCCEmailAddress(String receiverCCEmailAddress) {
		this.receiverCCEmailAddress = receiverCCEmailAddress;
	}
	public String getReceiverBCCEmailAddress() {
		return receiverBCCEmailAddress;
	}
	public void setReceiverBCCEmailAddress(String receiverBCCEmailAddress) {
		this.receiverBCCEmailAddress = receiverBCCEmailAddress;
	}
	public String getMailSubject() {
		return mailSubject;
	}
	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}
	public String getMailBodyTemplate() {
		return mailBodyTemplate;
	}
	public void setMailBodyTemplate(String mailBodyTemplate) {
		this.mailBodyTemplate = mailBodyTemplate;
	}
	
	
	
}
