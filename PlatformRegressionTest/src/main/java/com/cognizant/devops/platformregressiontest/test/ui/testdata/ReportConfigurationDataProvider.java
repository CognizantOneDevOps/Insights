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
package com.cognizant.devops.platformregressiontest.test.ui.testdata;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.DataProvider;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.ui.utility.ReadJsonData;

public class ReportConfigurationDataProvider {
	
	ReadJsonData readJsonData = ReadJsonData.getInstance();
	
	public static final String KPI_TEST_DATA = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.REPORT_MANAGEMENT_DIR + File.separator
			+ ConfigOptionsTest.KPI_TEST_DATA;
	
	public static final String VALIDATE_KPI_TEST_DATA = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.REPORT_MANAGEMENT_DIR + File.separator
			+ ConfigOptionsTest.VALIDATE_KPI_TEST_DATA;
	
	public static final String CONTENT_TEST_DATA = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.REPORT_MANAGEMENT_DIR + File.separator
			+ ConfigOptionsTest.CONTENT_TEST_DATA;
	
	@DataProvider(name = "saveKPI")
	Object[][] saveKPI() throws IOException {
		return (ReadJsonData.readReportConfigData(KPI_TEST_DATA));
	}
	
	@DataProvider(name = "validateKPI")
	Object[][] validateKPI() throws IOException {
		return (ReadJsonData.readReportConfigData(VALIDATE_KPI_TEST_DATA));
	}
	
	@DataProvider(name = "saveContent")
	Object[][] saveContent() throws IOException {
		return (ReadJsonData.readReportConfigData(CONTENT_TEST_DATA));
	}

}
