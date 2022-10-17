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
import com.cognizant.devops.platformregressiontest.test.ui.utility.ReadJsonData;

public class OutcomeConfigDataProvider {

	ReadJsonData readJsonData = ReadJsonData.getInstance();
	
	public static final String OUTCOME_WITHOUT_PARAM = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.ROI_DIR + File.separator
			+ ConfigOptionsTest.OUTCOME_TEST_DATA;
	
	public static final String OUTCOME_WITH_PARAM = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.ROI_DIR + File.separator
			+ ConfigOptionsTest.OUTCOME_WITH_PARAM_TEST;
	
	public static final String EDIT_OUTCOME= System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
			+ ConfigOptionsTest.ROI_DIR + File.separator
			+ ConfigOptionsTest.EDIT_OUTCOME_DETAILS;
	
	@DataProvider(name = "configureOutcomeWithoutReqParams")
	Object[][] configureOutcomeWithoutReqParams() throws IOException {
		return (ReadJsonData.readOutcomeConfigData(OUTCOME_WITHOUT_PARAM));
	}
	
	@DataProvider(name = "configureOutcomeWithReqParams")
	Object[][] configureOutcomeWithReqParams() throws IOException {
		return (ReadJsonData.readOutcomeConfigData(OUTCOME_WITH_PARAM));
	}
	
	@DataProvider(name = "editOutcome")
	Object[][] editOutcome() throws IOException {
		return (ReadJsonData.readOutcomeConfigData(EDIT_OUTCOME));
	}
	
}
