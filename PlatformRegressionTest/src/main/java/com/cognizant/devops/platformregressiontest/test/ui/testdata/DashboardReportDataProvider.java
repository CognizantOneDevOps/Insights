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
package com.cognizant.devops.platformregressiontest.test.ui.testdata;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.DataProvider;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.ui.utility.ReadJsonData;

public class DashboardReportDataProvider {

	ReadJsonData readJsonData = ReadJsonData.getInstance();

	public static final String DASHBOARD_REPORT_JSON = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.DASHBOARD_REPORT_DOWNLOAD_DIR + File.separator
			+ ConfigOptionsTest.DASHBOARDREPORTDOWNLOAD_JSON_FILE;

	public static final String REPORT_WITHOUT_MAILING_DETAILS = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.DASHBOARD_REPORT_DOWNLOAD_DIR + File.separator
			+ ConfigOptionsTest.REPORT_WITHOUT_MAILING_DETAILS;
	
	public static final String EDIT_DASHBOARD_REPORT = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.DASHBOARD_REPORT_DOWNLOAD_DIR + File.separator
			+ ConfigOptionsTest.EDIT_DASHBOARD_REPORT;

	@DataProvider(name = "reportWithMailingDetails")
	Object[][] reportWithMailingDetails() throws IOException {
		return (ReadJsonData.readReportData(DASHBOARD_REPORT_JSON));
	}

	@DataProvider(name = "reportWithoutMailingDetails")
	Object[][] reportWithoutMailingDetails() throws IOException {
		return (ReadJsonData.readReportData(REPORT_WITHOUT_MAILING_DETAILS));
	}
	
	@DataProvider(name = "editDashboardReport")
	Object[][] editDashboardReport() throws IOException {
		return (ReadJsonData.readReportData(EDIT_DASHBOARD_REPORT));
	}
}
