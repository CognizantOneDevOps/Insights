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
package com.cognizant.devops.platformregressiontest.test.ui.reportmanagement;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.ReportManagementDataProvider;

public class AssessmentReportTest extends LoginAndSelectModule {

	AssessmentReportConfigPage assessmentReportPage;

	@BeforeTest
	public void setUp() {
		initialization();
		selectMenuOption("Report Management");
		assessmentReportPage = new AssessmentReportConfigPage();
	}

	@BeforeMethod
	public void beforeEachTestCase() {
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
	}

	@Test(priority = 1, dataProvider = "assessmentReportCreatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void addReportTest(String reportName, String title, String templateName, String frequency,
			String reoccurence, String day, String month, String year, String mailFrom, String mailTo, String ccMail,
			String bccMail, String subject, String bodyTemplate) {
		assessmentReportPage.addReport(reportName, title, templateName, frequency, reoccurence, day, month, year,
				mailFrom, mailTo, ccMail, bccMail, subject, bodyTemplate);

		// assessmentReportPage

	}

}