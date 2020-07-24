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
package com.cognizant.devops.platformregressiontest.test.groupsanduser;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.DataProvider;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.XLUtils;

public class GroupsAndUserTestData {

	@DataProvider(name = "userdataprovider")
	String[][] getuserDetail() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path, "AddUser");
		int colNum = XLUtils.getCellCount(path, "AddUser", 1);

		String addUserData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				addUserData[i - 1][j] = XLUtils.getCellData(path, "AddUser", i, j);

			}
		}

		return (addUserData);

	}

	@DataProvider(name = "updateuserdataprovider")
	String[][] getUpdateUserData() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path, "UpdateUser");
		int colNum = XLUtils.getCellCount(path, "UpdateUser", 1);

		String updateUserData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				updateUserData[i - 1][j] = XLUtils.getCellData(path, "UpdateUser", i, j);

			}
		}

		return (updateUserData);

	}

	@DataProvider(name = "assignuserdataprovider")
	String[][] getCorrelationDeleteData() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path, "AssignUser");
		int colNum = XLUtils.getCellCount(path, "AssignUser", 1);

		String assignUserData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				assignUserData[i - 1][j] = XLUtils.getCellData(path, "AssignUser", i, j);

			}
		}

		return (assignUserData);

	}

}
