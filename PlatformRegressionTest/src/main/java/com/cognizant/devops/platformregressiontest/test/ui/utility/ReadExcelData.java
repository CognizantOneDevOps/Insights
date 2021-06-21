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
package com.cognizant.devops.platformregressiontest.test.ui.utility;

import java.io.File;
import java.io.IOException;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.XLUtils;

public class ReadExcelData {

	private ReadExcelData() {
	}

	public static ReadExcelData getInstance() {
		return new ReadExcelData();
	}

	public static final String PATH = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
			+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.REPORT_MANAGEMENT_DIR + File.separator + ConfigOptionsTest.REPORT_CONFIGURATION_FILE;

	public static String[][] readExelData(String workBookName) throws IOException {
		int rowNum = XLUtils.getRowCount(PATH, workBookName);
		int colNum = XLUtils.getCellCount(PATH, workBookName, 1);

		String data[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				data[i - 1][j] = XLUtils.getCellData(PATH, workBookName, i, j);

			}
		}

		return (data);
	}
}
