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

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class CommonUtils {

	private static final Logger log = LogManager.getLogger(CommonUtils.class);

	private CommonUtils() {
		super();
	}

	public static Properties props = null;

	public static String jSessionID = null;
	public static String xsrfToken;
	public static String jtoken;
	public static Map<String, String> cookiesMap =new HashMap<>();

	static {
		loadProperties();
	}

	public static void loadProperties() {
		try {
			String path = new File(System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator + ConfigOptionsTest.AUTO_DIR
					+ File.separator + ConfigOptionsTest.PROP_FILE).getCanonicalPath();
	
			FileReader reader = new FileReader(path);
	
			props = new Properties();
	
			props.load(reader);

		} catch (FileNotFoundException e) {
			log.error("Property File is not found {}", e);
		} catch (IOException e) {
			log.error(e);
		}
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	public static String getJsessionId() {
		RestAssured.baseURI = CommonUtils.getProperty("baseURI");
		Response response = given().header("Authorization", CommonUtils.getProperty("authorization")).and()
				.header("Content-Type", "application/json").when().post("PlatformService/user/authenticate").then()
				.extract().response();
		String cookies = response.getCookies().toString();
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(cookies.trim(), JsonElement.class);
		JsonObject json = jelement.getAsJsonObject();
		String jSessionId = json.get("JSESSIONID").getAsString();
		log.debug("SessionID----------------------------> {}", jSessionId);
		jSessionID = jSessionId;
		JsonPath responseJson = new JsonPath(response.getBody().asString());
		log.info("responseJson--------------------------------> {}", responseJson.prettyPrint());
		CommonUtils.jtoken = responseJson.getString("data.jtoken");
		log.info("jtoken--------------------------------> {}", CommonUtils.jtoken);
		getXSRFToken(jSessionId);
		CommonUtils.cookiesMap.put("JSESSIONID",jSessionID);
		String data = responseJson.get("data").toString();
		log.info("data--------------------------------> {}", data);
		parseAndgetMap(data);
		String cookiesString = CommonUtils.cookiesMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining(";"));
		log.info("cookiesString--------------------------------> {}", cookiesString);
		
		return jSessionID;
	}

	public static String getXSRFToken(String jSessionId) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI")
				+ "/PlatformService/admin/agentConfiguration/getRegisteredAgents";
		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.cookie("JSESSIONID", jSessionId);
		httpRequest.header("Authorization",CommonUtils.jtoken);

		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.request(Method.GET, "/");
		String cookies = response.getCookies().toString();
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(cookies.trim(), JsonElement.class);
		JsonObject json = jelement.getAsJsonObject();
		String Xsrf = json.get("XSRF-TOKEN").getAsString();
		log.debug("XSRF----------------------------> {}", Xsrf);
		CommonUtils.xsrfToken = Xsrf;
		return Xsrf;
	}
	
	public static void parseAndgetMap(String json) {
	    JsonObject object = JsonUtils.parseStringAsJsonObject(json);
	    for (Map.Entry<String,JsonElement> entry : object.entrySet()) {
	    	CommonUtils.cookiesMap.put(entry.getKey(), entry.getValue().toString());
	    }
	}
}
