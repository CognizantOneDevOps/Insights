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
package com.cognizant.devops.platformregressiontest.test.roi;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.DataProvider;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.XLUtils;

public class OutcomeTest {
	public static final String OUTCOME = "Outcome";
	public static final String MILESTONE = "Milestone";


	@DataProvider(name ="outcomedataprovider")
	String[][] getOutComeData() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path,  OUTCOME);
		int colNum = XLUtils.getCellCount(path, OUTCOME, 1);

		String outcomeData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				outcomeData[i - 1][j] = XLUtils.getCellData(path, OUTCOME, i, j);

			}
		}

		return (outcomeData);

	}
	@DataProvider(name ="milestonedataprovider")
	String[][] getMileStoneData() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path,  MILESTONE);
		int colNum = XLUtils.getCellCount(path, MILESTONE, 1);

		String milestoneData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				milestoneData[i - 1][j] = XLUtils.getCellData(path, MILESTONE, i, j);

			}
		}

		return (milestoneData);
	}
}
