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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.AgentManagementDataModel;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.ConfigurationFileManagementDataModel;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.DashboardReportDataModel;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.OutcomeConfigDataModel;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.ReportConfigurationDataModel;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.TraceabilityDataModel;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.WorkflowTakDataModel;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ReadJsonData {
	private static final Logger log = LogManager.getLogger(ReadJsonData.class);

	private ReadJsonData() {
	}

	public static ReadJsonData getInstance() {
		return new ReadJsonData();
	}

	public static Object[][] readAgentJsonData(String jsonFile) throws IOException {
		JsonElement jsonData = JsonUtils.parseReader(new FileReader(new File(jsonFile).getCanonicalPath()));
		JsonElement dataSet = jsonData.getAsJsonObject().get("dataset");
		List<AgentManagementDataModel> testData = new Gson().fromJson(dataSet,
				new TypeToken<List<AgentManagementDataModel>>() {
				}.getType());
		Object[][] returnValue = new Object[testData.size()][1];
		int index = 0;
		for (Object[] each : returnValue) {
			each[0] = testData.get(index++);
		}
		return returnValue;
	}

	public static Object[][] readConfigurationFileJsonData(String jsonFile) throws IOException {
		JsonElement jsonData = JsonUtils.parseReader(new FileReader(new File(jsonFile).getCanonicalPath()));
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

	public static Object[][] readReportData(String jsonFile)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonElement jsonData = null;
		try {
			jsonData = new JsonParser().parse(new FileReader(new File(jsonFile).getCanonicalPath()));
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			log.error(e);
		}
		JsonElement dataSet = jsonData.getAsJsonObject().get("dataset");
		List<DashboardReportDataModel> testData = new Gson().fromJson(dataSet,
				new TypeToken<List<DashboardReportDataModel>>() {
				}.getType());
		Object[][] returnValue = new Object[testData.size()][1];
		int index = 0;
		for (Object[] each : returnValue) {
			each[0] = testData.get(index++);
		}
		return returnValue;
	}
	
	public static Object[][] readTraceabilityData(String jsonFile)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonElement jsonData = null;
		try {
			jsonData = new JsonParser().parse(new FileReader(new File(jsonFile).getCanonicalPath()));
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			log.error(e);
		}
		JsonElement dataSet = jsonData.getAsJsonObject().get("dataset");
		List<TraceabilityDataModel> testData = new Gson().fromJson(dataSet,
				new TypeToken<List<TraceabilityDataModel>>() {
				}.getType());
		Object[][] returnValue = new Object[testData.size()][1];
		int index = 0;
		for (Object[] each : returnValue) {
			each[0] = testData.get(index++);
		}
		return returnValue;
	}
	
	@SuppressWarnings("deprecation")
	public static Object[][] readWorkflowData(String jsonFile)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonElement jsonData = null;
		try {
			jsonData = new JsonParser().parse(new FileReader(new File(jsonFile).getCanonicalPath()));
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			log.error(e);
		}
		JsonElement dataSet = jsonData.getAsJsonObject().get("data");
		List<WorkflowTakDataModel> testData = new Gson().fromJson(dataSet,
				new TypeToken<List<WorkflowTakDataModel>>() {
				}.getType());
		Object[][] returnValue = new Object[testData.size()][1];
		int index = 0;
		for (Object[] each : returnValue) {
			each[0] = testData.get(index++);
		}
		return returnValue;
	}

	
	public static Object[][] readOutcomeConfigData(String jsonFile)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonElement jsonData = null;
		try {
			jsonData = new JsonParser().parse(new FileReader(new File(jsonFile).getCanonicalPath()));
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			log.error(e);
		}
		JsonElement dataSet = jsonData.getAsJsonObject().get("dataset");
		List<OutcomeConfigDataModel> testData = new Gson().fromJson(dataSet,
				new TypeToken<List<OutcomeConfigDataModel>>() {
				}.getType());
		Object[][] returnValue = new Object[testData.size()][1];
		int index = 0;
		for (Object[] each : returnValue) {
			each[0] = testData.get(index++);
		}
		return returnValue;
	}
	
	public static Object[][] readReportConfigData(String jsonFile)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonElement jsonData = null;
		try {
			jsonData = new JsonParser().parse(new FileReader(new File(jsonFile).getCanonicalPath()));
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			log.error(e);
		}
		JsonElement dataSet = jsonData.getAsJsonObject().get("dataset");
		List<ReportConfigurationDataModel> testData = new Gson().fromJson(dataSet,
				new TypeToken<List<ReportConfigurationDataModel>>() {
				}.getType());
		Object[][] returnValue = new Object[testData.size()][1];
		int index = 0;
		for (Object[] each : returnValue) {
			each[0] = testData.get(index++);
		}
		return returnValue;
	}
}
