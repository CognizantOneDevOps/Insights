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

public class GroupEmailRepoMgmDataProvider {
	
	ReadJsonData readJsonData = ReadJsonData.getInstance();

	public static final String CREATE_GROUP_EMAIL_FOR_REPORT = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.GROUP_EMAIL_CONFIG_REPO + File.separator
			+ ConfigOptionsTest.GROUP_EMAIL_REPO_MGM;

	public static final String EDIT_GROUP_EMAIL_REPORT = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.GROUP_EMAIL_CONFIG_REPO + File.separator
			+ ConfigOptionsTest.GROUP_EMAIL_REPORT_EDIT;
	
	
	@DataProvider(name = "createGroupEmailReportMgm")
	Object[][] createGroupEmail() throws IOException {
		return (ReadJsonData.readGroupEmailData(CREATE_GROUP_EMAIL_FOR_REPORT));
	}
	
	@DataProvider(name = "editGroupEmailReportMgm")
	Object[][] editDashboardReport() throws IOException {
		return (ReadJsonData.readGroupEmailData(EDIT_GROUP_EMAIL_REPORT));
	}

}
