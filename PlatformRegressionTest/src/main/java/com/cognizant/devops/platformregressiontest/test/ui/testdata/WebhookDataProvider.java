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
package com.cognizant.devops.platformregressiontest.test.ui.testdata;

import java.io.File;
import org.testng.annotations.DataProvider;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
/**
 * @author NivethethaS
 * 
 *         Class contains the data used for webhook Configuration module test
 *         cases
 *
 */
public class WebhookDataProvider {

	public static final String PATH = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) 
			+ File.separator + ConfigOptionsTest.AUTO_DIR
			+ File.separator + ConfigOptionsTest.WEBHOOK_CONFIG_DIR + File.separator + ConfigOptionsTest.DYNAMICRESPONSE_JSON_FILE;
	
	public static final String EVENT_PATH = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) 
			+ File.separator + ConfigOptionsTest.AUTO_DIR
			+ File.separator + ConfigOptionsTest.WEBHOOK_CONFIG_DIR + File.separator + ConfigOptionsTest.EVENTCONFIG_JSON_FILE;
	
	/**
	 * @return content of the JSON file in string format
	 * @throws Exception
	 */
	@DataProvider(name = "dynamicMessagedataprovider")
	public String getDynamicTemplate()  throws Exception {
		return readFileAsString(PATH);
	}
	
	/**
	 * @return content of the JSON file in string format
	 * @throws Exception
	 */
	@DataProvider(name = "eventConfigdataprovider")
	public String getEventConfig()  throws Exception {
		return readFileAsString(EVENT_PATH);
	}
	
	/**
	 * Converts JSON file's content into a string
	 * @param file
	 * @return content of the JSON file in string format
	 * @throws Exception
	 */
	public static String readFileAsString(String file)throws Exception{
        return new String(Files.readAllBytes(Paths.get(file)));
    }
}
