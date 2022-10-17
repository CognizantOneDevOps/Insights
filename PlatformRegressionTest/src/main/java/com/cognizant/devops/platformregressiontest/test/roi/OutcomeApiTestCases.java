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
package com.cognizant.devops.platformregressiontest.test.roi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.CommonUtils;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class OutcomeApiTestCases extends OutcomeTest {
	private static final Logger log = LogManager.getLogger(OutcomeApiTestCases.class);

	@Test(priority = 1)
	public void getAllActiveOutcome() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getAllActiveOutcome");
		// Response Object
        Response getResponse =setHeaders(null,"GET");

		String responseOutcome = getResponse.getBody().asString();
		log.debug("SuccessResponse {}", responseOutcome);

		int statusCode = getResponse.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseOutcome.contains("success"), true);
	}
	@Test(priority = 2)
	public void fetchMileStoneTools() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("fetchMileStoneTools");
		// Response Object
        Response getResponse =setHeaders(null,"GET");

		String responseOutcome = getResponse.getBody().asString();
		log.debug("SuccessResponse {}", responseOutcome);

		int statusCode = getResponse.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseOutcome.contains("success"), true);
	}
	@Test(priority = 3,dataProvider = "outcomedataprovider")
	public void saveOutcomeConfig(String config,String updateConfig,String updateConfigStatus,String id) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("saveOutcomeConfig");
		// Response Object
        Response getResponse =setHeaders(config,"POST");

		String responseOutcome = getResponse.getBody().asString();
		log.debug("SuccessResponse {}", responseOutcome);

		int statusCode = getResponse.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseOutcome.contains("success"), true);
	}
	@Test(priority = 4,dataProvider = "outcomedataprovider")
	public void updateOutcomeConfig(String config,String updateConfig,String updateConfigStatus,String id) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("updateOutcomeConfig");        
		// Response Object
        Response getResponse =setHeaders(updateConfig,"POST");

		String responseOutcome = getResponse.getBody().asString();
		log.debug("SuccessResponse {}", responseOutcome);

		int statusCode = getResponse.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseOutcome.contains("success"), true);
	}
	@Test(priority = 5,dataProvider = "outcomedataprovider")
	public void updateOutcomeConfigStatus(String config,String updateConfig,String updateConfigStatus,String id) {
		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("updateOutcomeConfigStatus");
        Response getResponse =setHeaders(updateConfigStatus,"POST");
		String responseOutcome = getResponse.getBody().asString();
		log.debug("SuccessResponse {}", responseOutcome);
		int statusCode = getResponse.getStatusCode();
		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseOutcome.contains("success"), true);
	}
	@Test(priority = 6,dataProvider = "outcomedataprovider")
	public void deleteOutcomeConfig(String config,String updateConfig,String updateConfigStatus,String id) {
		String idString="?id="+id;
		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteOutcomeConfig")+idString;
        Response getResponse =setHeaders(id,"POST");
		String responseOutcome = getResponse.getBody().asString();
		log.debug("SuccessResponse {}", responseOutcome);
		int statusCode = getResponse.getStatusCode();
		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseOutcome.contains("success"), true);
	}
  public Response setHeaders(String parameters,String type) {
	    Response responseAll=null;
		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, CommonUtils.jSessionID,
				ConfigOptionsTest.GRAFANA_COOKIES_ORG, CommonUtils.getProperty("grafanaOrg"),
				ConfigOptionsTest.GRAFANA_COOKIES_ROLE, CommonUtils.getProperty("grafanaRole"),
				ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);
		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		if(type.equalsIgnoreCase("POST")) {
			httpRequest.body(parameters);
			 responseAll= httpRequest.request(Method.POST, "/");
		}
		if(type.equalsIgnoreCase("GET")) {
			 responseAll= httpRequest.request(Method.GET, "/");
		}
  return responseAll;		
  }
}
