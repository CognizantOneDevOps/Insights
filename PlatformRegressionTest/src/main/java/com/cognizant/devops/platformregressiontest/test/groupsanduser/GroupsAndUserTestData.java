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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;
import com.cognizant.devops.platformregressiontest.common.CommonUtils;
import com.cognizant.devops.platformregressiontest.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.common.XLUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GroupsAndUserTestData {
	
	private static final Logger log = LogManager.getLogger(GroupsAndUserTestData.class);

	String jSessionID;
	String xsrfToken;
	String authorization = "U2FsdGVkX19WFjYlorGzpolzbX1Ro+f5XcvD3Lt5lzaEo3JvlyHNfIeRMwiApFgR99b74c48-f15e-4";

	public String getJsessionId() throws IOException {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + "/PlatformService/user/authenticate";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header("Authorization",
				"U2FsdGVkX19WFjYlorGzpolzbX1Ro+f5XcvD3Lt5lzaEo3JvlyHNfIeRMwiApFgR99b74c48-f15e-4");
		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.request(Method.GET, "/");
		String cookies = response.getCookies().toString();
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(cookies.trim(), JsonElement.class);
		JsonObject json = jelement.getAsJsonObject();
		String jSessionId = json.get("JSESSIONID").getAsString();
		log.debug("SessionID---------------------------->" + jSessionId);
		getXSRFToken(jSessionId);
		jSessionID = jSessionId;
		return jSessionId;

	}

	public String getXSRFToken(String jSessionId) throws IOException {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI")
				+ "/PlatformService/admin/agentConfiguration/getRegisteredAgents";
		;
		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.cookie("JSESSIONID", jSessionId);
		httpRequest.header("Authorization", authorization);
		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.request(Method.GET, "/");
		String cookies = response.getCookies().toString();
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(cookies.trim(), JsonElement.class);
		JsonObject json = jelement.getAsJsonObject();
		String Xsrf = json.get("XSRF-TOKEN").getAsString();
		log.debug("XSRF---------------------------->" + Xsrf);
		xsrfToken = Xsrf;
		return Xsrf;
	}

	@DataProvider(name = "userdataprovider")
	String[][] getuserDetail() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

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
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

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
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

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
