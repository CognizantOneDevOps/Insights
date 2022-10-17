/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
import com.cognizant.devops.platformregressiontest.test.ui.utility.ReadExcelData;
import com.cognizant.devops.platformregressiontest.test.ui.utility.ReadJsonData;

public class WorkflowDataProvider {

	ReadExcelData readExceldata = ReadExcelData.getInstance();

	public static final String WORKFLOW_REPORT_JSON = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.WORKFLOW_TASK + File.separator
			+ ConfigOptionsTest.WORKFLOW_TASK_JSON_FILE;
	
	public static final String UPDATE_WORKFLOW_REPORT_JSON = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.WORKFLOW_TASK + File.separator
			+ ConfigOptionsTest.UPDATE_WORKFLOW_TASK_JSON_FILE;
	
	@DataProvider(name = "workFlowDataProvider")
	Object[][] workFlowDetails() throws IOException {
		return (ReadJsonData.readWorkflowData(WORKFLOW_REPORT_JSON));
	}
	@DataProvider(name = "updateWorkFlowDataProvider")
	Object[][] updateWorkFlowDetails() throws IOException {
		return (ReadJsonData.readWorkflowData(UPDATE_WORKFLOW_REPORT_JSON));
	}

}
