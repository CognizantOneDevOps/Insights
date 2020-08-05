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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class CommonUtils {

	private static final Logger log = LogManager.getLogger(CommonUtils.class);

	public static Properties props = null;

	static String jSessionID = null;
	static String xsrfToken;

	static {
		loadProperties();

	}

	public static void loadProperties() {

		try {
			FileReader reader = null;

			String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
					+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.PROP_FILE;

			reader = new FileReader(path);
			props = new Properties();

			props.load(reader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	public static String getJsessionId() throws InterruptedException, IOException {
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
		log.debug("SessionID----------------------------> {}", jSessionId);
		getXSRFToken(jSessionId);
		jSessionID = jSessionId;

		return jSessionID;
	}

	public static String getXSRFToken(String jSessionId) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI")
				+ "/PlatformService/admin/agentConfiguration/getRegisteredAgents";
		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.cookie("JSESSIONID", jSessionId);
		httpRequest.header("Authorization", CommonUtils.getProperty("authorization"));

		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.request(Method.GET, "/");
		String cookies = response.getCookies().toString();
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(cookies.trim(), JsonElement.class);
		JsonObject json = jelement.getAsJsonObject();
		String Xsrf = json.get("XSRF-TOKEN").getAsString();
		log.debug("XSRF----------------------------> {}", Xsrf);
		xsrfToken = Xsrf;
		return Xsrf;
	}

}
