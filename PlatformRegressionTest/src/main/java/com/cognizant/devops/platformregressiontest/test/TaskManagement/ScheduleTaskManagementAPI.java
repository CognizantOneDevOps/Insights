/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.TaskManagement;

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


public class ScheduleTaskManagementAPI extends TaskManagementTest {
	private static final Logger log = LogManager.getLogger(ScheduleTaskManagementAPI.class);

	@Test(priority = 1)
	public void getAllTaskDetail() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getAllTaskDetail");

        Response getResponse =setHeaders(null,"GET");

		String responseApi = getResponse.getBody().asString();
		log.debug("SuccessResponse {}", responseApi);

		int statusCode = getResponse.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseApi.contains("success"), true);
	}
	@Test(priority = 2,dataProvider = "taskdataprovider")
	public void getTaskHistoryDetail(String config) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getTaskHistoryDetail");

        Response getResponse =setHeaders(config,"POST");

		String responseApi = getResponse.getBody().asString();
		log.debug("SuccessResponse {}", responseApi);

		int statusCode = getResponse.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseApi.contains("success"), true);
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
