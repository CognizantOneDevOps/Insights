/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.assessmentreport;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "\"INSIGHTS_REPORT_VISUALIZATION_CONTAINER\"")
public class InsightsReportVisualizationContainer implements Serializable {
	private static final long serialVersionUID = 1597920089L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true)
	int id;

	@Column(name = "mailId")
	private Integer mailId = 0;

	@Column(name = "executiontime")
	private Long executionTime = 0L;

	@Column(name = "executionid", unique = true)
	private Long executionId = 0L;

	@Column(name = "subject")
	private String subject;

	@Column(name = "mailto", length = 2000)
	private String mailTo;

	@Column(name = "mailfrom")
	private String mailFrom;

	@Column(name = "mailbody", length = 10000)
	private String mailBody;

	@Column(name = "attachmentpath")
	private String attachmentPath;

	@Column(name = "status")
	private String status;

	@Column(name = "mailcc", length = 2000)
	private String mailCC;

	@Column(name = "mailbcc", length = 2000)
	private String mailBCC;

	@Column(name = "workflowId")
	private String workflowId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getMailId() {
		return mailId;
	}

	public void setMailId(Integer mailId) {
		this.mailId = mailId;
	}

	public Long getExecutionTime() {
		return this.executionTime == null ? 0L : this.executionTime;
		
	}

	public void setExecutionTime(Long executionTime) {
		this.executionTime = executionTime;
	}

	public Long getExecutionId() {
		return this.executionId == null ? 0L : this.executionId;

	}

	public void setExecutionId(Long executionId) {
		this.executionId = executionId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public String getMailBody() {
		return mailBody;
	}

	public void setMailBody(String mailBody) {
		this.mailBody = mailBody;
	}

	public String getAttachmentPath() {
		return attachmentPath;
	}

	public void setAttachmentPath(String attachmentPath) {
		this.attachmentPath = attachmentPath;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getWorkflowConfig() {
		return workflowId;
	}

	public void setWorkflowConfig(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getMailCC() {
		return mailCC;
	}

	public void setMailCC(String mailCC) {
		this.mailCC = mailCC;
	}

	public String getMailBCC() {
		return mailBCC;
	}

	public void setMailBCC(String mailBCC) {
		this.mailBCC = mailBCC;
	}

}
