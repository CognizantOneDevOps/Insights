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

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.AgentManagementDataModel;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.ConfigurationFileManagementDataModel;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ReadJsonData {

	private ReadJsonData() {
	}

	public static ReadJsonData getInstance() {
		return new ReadJsonData();
	}

	public static Object[][] readAgentJsonData(String jsonFile) throws IOException {
		JsonElement jsonData = new JsonParser().parse(new FileReader(jsonFile));
		JsonElement dataSet = jsonData.getAsJsonObject().get("dataset");
		List<AgentManagementDataModel> testData = new Gson().fromJson(dataSet, new TypeToken<List<AgentManagementDataModel>>() {
		}.getType());
		Object[][] returnValue = new Object[testData.size()][1];
		int index = 0;
		for (Object[] each : returnValue) {
			each[0] = testData.get(index++);
		}
		return returnValue;
	}

	public static Object[][] readConfigurationFileJsonData(String jsonFile) throws IOException {
		JsonElement jsonData = new JsonParser().parse(new FileReader(jsonFile));
		JsonElement dataSet = jsonData.getAsJsonObject().get("dataset");
		List<ConfigurationFileManagementDataModel> testData = new Gson().fromJson(dataSet,
				new TypeToken<List<ConfigurationFileManagementDataModel>>() {
				}.getType());
		Object[][] returnValue = new Object[testData.size()][1];
		int index = 0;
		for (Object[] each : returnValue) {
			each[0] = testData.get(index++);
		}
		return returnValue;
	}

}
