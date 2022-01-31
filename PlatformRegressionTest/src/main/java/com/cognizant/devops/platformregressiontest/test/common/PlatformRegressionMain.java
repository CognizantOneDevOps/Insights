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
package com.cognizant.devops.platformregressiontest.test.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.TestNG;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.constants.LogLevelConstants;

public class PlatformRegressionMain {
	static Logger log = LogManager.getLogger(PlatformRegressionMain.class.getName());

	public static void main(String[] args) {

		ApplicationConfigCache.updateLogLevel(LogLevelConstants.PLATFORMREGRESSIONTEST);

		List<String> file = new ArrayList<>();

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator + ConfigOptionsTest.AUTO_DIR
				+ File.separator + ConfigOptionsTest.TESTNG_FILE;

		file.add(path);
		File testngFile = new File(path);
		try {
			log.debug(" Testng file path for PlatformRegressionTest {} ", testngFile.getCanonicalPath());
		} catch (IOException e) {
			log.error(e);
		}
		TestNG testNG = new TestNG();
		testNG.setTestSuites(file);
		testNG.run();
	}

}
