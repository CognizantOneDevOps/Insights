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
package com.cognizant.devops.platformservice.test.groupEmail;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

public class GroupEmailTestData extends AbstractTestNGSpringContextTests {

	private int reportEmailConfigId;
	private static int grafanaEmailConfigId;

	public static final String testSaveReportBatchString = "{\"batchName\":\"testReportBatch\",\"source\":\"Report\",\"schedule\":\"ONETIME\",\"reports\":[{\"id\":131,\"reportName\":\"Demo_Report\"},{\"id\":4970,\"reportName\":\"Demo_Report2\"}],\"emailDetails\":{\"receiverEmailAddress\":\"receiverTest@cognizant.com\",\"receiverCCEmailAddress\":\"receiverCCTest@cognizant.com\",\"receiverBCCEmailAddress\":\"receiverBCCTest@cognizant.com\",\"mailSubject\":\"Test_Subject\",\"mailBodyTemplate\":\"Test_Mail_Body\"}}";
	public static final String testSaveDashboardBatchString = "{\"batchName\":\"TestDashboardBatch\",\"source\":\"GRAFANADASHBOARDPDFREPORT\",\"schedule\":\"ONETIME\",\"reports\":[{\"id\":13727,\"reportName\":\"Demo_Dashboard_Report\"},{\"id\":104,\"reportName\":\"Demo_Dashboard_Report2\"}],\"emailDetails\":{\"receiverEmailAddress\":\"shreyash.jadhav@cognizant.com\",\"receiverCCEmailAddress\":\"shreyash.jadhav@cognizant.com\",\"receiverBCCEmailAddress\":\"shreyash.jadhav@cognizant.com\",\"mailSubject\":\"Test_Subject\",\"mailBodyTemplate\":\"Test_Mail_Body\"}}";

	public static String testEditReportBatchString = "";
	public static String testEditGrafanaBatchString = "";

	public static String testEditBatchState = "";
	public static final String testEditInvalidBatchState = "{\"id\":555,\"isActive\":false}";
	public static int deleteEmailConfigId;
	public static final int deleteEmailConfigInvalidId = 555;

	public static final String sourceReport = "Report";
	public static final String sourceGrafanaDashboard = "GRAFANADASHBOARDPDFREPORT";
	public static final String invalidSource = "REPORTGRAFANA";

	public static final String testSaveEmailWithDuplicateName = "{\"batchName\":\"testReportBatch\",\"source\":\"Report\",\"schedule\":\"ONETIME\",\"reports\":[{\"id\":131,\"reportName\":\"Demo_Report\"},{\"id\":4970,\"reportName\":\"Demo_Report2\"}],\"emailDetails\":{\"receiverEmailAddress\":\"receiverTest@cognizant.com\",\"receiverCCEmailAddress\":\"receiverCCTest@cognizant.com\",\"receiverBCCEmailAddress\":\"receiverBCCTest@cognizant.com\",\"mailSubject\":\"Test_Subject\",\"mailBodyTemplate\":\"Test_Mail_Body\"}}";
	public static final String inValidEmailConfigString = "{\"batchName\":\"\",\"source\":\"Report\",\"reports\":[{\"id\":131,\"reportName\":\"Demo_Report\"},{\"id\":4970,\"reportName\":\"Demo_Report2\"}],\"emailDetails\":{\"receiverEmailAddress\":\"receiverTest@cognizant.com\",\"receiverCCEmailAddress\":\"receiverCCTest@cognizant.com\",\"receiverBCCEmailAddress\":\"receiverBCCTest@cognizant.com\",\"mailSubject\":\"Test_Subject\",\"mailBodyTemplate\":\"Test_Mail_Body\"}}";
	public static final String emailConfigWithEmptyEmailDetails = "{\"batchName\":\"testReportBatch1\",\"source\":\"Report\",\"schedule\":\"ONETIME\",\"reports\":[{\"id\":131,\"reportName\":\"Demo_Report\"},{\"id\":4970,\"reportName\":\"Demo_Report2\"}],\"emailDetails\":{}}";

	public void setReportEmailConfigId(int reportEmailConfigId) {
		this.reportEmailConfigId = reportEmailConfigId;
		this.testEditReportBatchString = "{\"id\":" + this.reportEmailConfigId
				+ ",\"source\":\"Report\",\"reports\":[{\"id\":131,\"reportName\":\"Demo_Report\"},{\"id\":4970,\"reportName\":\"Demo_Report2\"}],\"emailDetails\":{\"receiverEmailAddress\":\"receiverTest@cognizant.com\",\"receiverCCEmailAddress\":\"receiverCCTest@cognizant.com\",\"receiverBCCEmailAddress\":\"receiverBCCTest@cognizant.com\",\"mailSubject\":\"Test_Subject\",\"mailBodyTemplate\":\"Test_Mail_Body\"}}";
		this.deleteEmailConfigId = reportEmailConfigId;
		this.testEditBatchState = "{\"id\":" + this.reportEmailConfigId + ",\"isActive\":false}";

	}

	public void setGrafanaEmailConfigId(int grafanaEmailConfigId) {
		this.grafanaEmailConfigId = grafanaEmailConfigId;
		this.testEditGrafanaBatchString = "{\"id\":" + this.grafanaEmailConfigId
				+ ",\"source\":\"GRAFANADASHBOARDPDFREPORT\",\"reports\":[{\"id\":131,\"reportName\":\"Demo_Report\"},{\"id\":4970,\"reportName\":\"Demo_Report2\"}],\"emailDetails\":{\"receiverEmailAddress\":\"receiverTest@cognizant.com\",\"receiverCCEmailAddress\":\"receiverCCTest@cognizant.com\",\"receiverBCCEmailAddress\":\"receiverBCCTest@cognizant.com\",\"mailSubject\":\"Test_Subject\",\"mailBodyTemplate\":\"Test_Mail_Body\"}}";
	}
}
