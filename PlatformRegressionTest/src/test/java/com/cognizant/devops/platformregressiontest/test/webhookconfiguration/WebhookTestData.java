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
package com.cognizant.devops.platformregressiontest.test.webhookconfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import com.cognizant.devops.platformregressiontest.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.common.XLUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class WebhookTestData {

	String jSessionID;
	String xsrfToken;
	private FileReader reader = null;
	Properties p = null;

	@BeforeMethod
	public Properties onInit() throws InterruptedException, IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.PROP_FILE;

		reader = new FileReader(path);

		p = new Properties();

		p.load(reader);
		return p;

	}

	String authorization = "U2FsdGVkX19WFjYlorGzpolzbX1Ro+f5XcvD3Lt5lzaEo3JvlyHNfIeRMwiApFgR99b74c48-f15e-4";

	public String getJsessionId() throws InterruptedException, IOException {

		// private FileReader reader;
		Properties p = null;

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.PROP_FILE;

		reader = new FileReader(path);

		p = new Properties();

		p.load(reader);

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/user/authenticate";
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
		System.out.println("SessionID---------------------------->" + jSessionId);
		getXSRFToken(jSessionId);
		jSessionID = jSessionId;
		return jSessionId;

	}

	public String getXSRFToken(String jSessionId) throws InterruptedException, IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.PROP_FILE;

		reader = new FileReader(path);

		p = new Properties();

		p.load(reader);
		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/webhook/loadwebhookConfiguration";
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
		System.out.println("XSRF---------------------------->" + Xsrf);
		xsrfToken = Xsrf;
		return Xsrf;
	}

	@DataProvider(name = "webhookdataprovider")
	String[][] getAgentData() throws IOException {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.TESTDATA_FILE;

		int rowNum = XLUtils.getRowCount(path, "SaveWebhook");
		int colNum = XLUtils.getCellCount(path, "SaveWebhook", 1);

		String webhookData[][] = new String[rowNum][colNum];

		for (int i = 1; i <= rowNum; i++) {

			for (int j = 0; j < colNum; j++) {

				webhookData[i - 1][j] = XLUtils.getCellData(path, "SaveWebhook", i, j);

			}
		}

		return (webhookData);

	}

}
