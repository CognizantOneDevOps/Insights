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
package com.cognizant.devops.platformregressiontest.test.correlationbuilder;

import java.io.File;
import java.io.IOException;
import org.testng.annotations.DataProvider;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.XLUtils;

public class CorrelationTestData {

	@DataProvider(name = "correlationdataprovider")
	String[][] getCorrelationData() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path, "SaveCorrelation");
		int colNum = XLUtils.getCellCount(path, "SaveCorrelation", 1);

		String correlationData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				correlationData[i - 1][j] = XLUtils.getCellData(path, "SaveCorrelation", i, j);

			}
		}

		return (correlationData);

	}

	@DataProvider(name = "correlationupdatedataprovider")
	String[][] getCorrelationUpdateData() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path, "UpdateCorrelation");
		int colNum = XLUtils.getCellCount(path, "UpdateCorrelation", 1);

		String correlationUpdateData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				correlationUpdateData[i - 1][j] = XLUtils.getCellData(path, "UpdateCorrelation", i, j);

			}
		}

		return (correlationUpdateData);

	}

	@DataProvider(name = "correlationdeletedataprovider")
	String[][] getCorrelationDeleteData() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path, "DeleteCorrelation");
		int colNum = XLUtils.getCellCount(path, "DeleteCorrelation", 1);

		String correlationUpdateData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				correlationUpdateData[i - 1][j] = XLUtils.getCellData(path, "DeleteCorrelation", i, j);

			}
		}

		return (correlationUpdateData);

	}

}
