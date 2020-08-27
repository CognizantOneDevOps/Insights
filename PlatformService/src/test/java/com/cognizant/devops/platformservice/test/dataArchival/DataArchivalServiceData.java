/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.dataArchival;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataArchivalServiceData {
	JsonParser parser = new JsonParser();
	
	String configDetails = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SYSTEM.ELASTICTRANSFER.CONFIG\",\"agentCtrlQueue\":\"Archival_agent_test\",\"dataArchivalQueue\":\"SYSTEM.ELASTICTRANSFER.CONFIG\"},\"publish\":{\"data\":\"SYSTEM.ELASTICTRANSFER.DATA\",\"health\":\"SYSTEM.ELASTICTRANSFER.HEALTH\"},\"agentId\":\"Archival_agent_test\",\"toolCategory\":\"SYSTEM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"startFrom\":\"2017-10-01 00:00:01\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v6.8\",\"toolName\":\"ELASTICTRANSFER\",\"labelName\":\"ELASTICTRANSFER\"}";
	JsonObject agentJson = convertStringIntoJson(configDetails);
	String trackingDetails = "";
	Date updateDate = Timestamp.valueOf(LocalDateTime.now());
	Boolean vault = false;
	
	String saveArchivalRecords = "{\"archivalName\":\"Dib_testng_10\",\"startDate\":\"2020-07-08T00:00:00Z\",\"endDate\":\"2020-07-10T00:00:00Z\",\"daysToRetain\":3,\"author\":\"\"}";
	Long expectedStartDate = InsightsUtils.getEpochTime("2020-07-08T00:00:00Z") / 1000;
	Long expectedEndDate = InsightsUtils.getEpochTime("2020-07-10T00:00:00Z") / 1000;
	int expectedDaysToRetain = 3;
	String saveArchivalRecordsForDeleteCase = "{\"archivalName\":\"Dib_testng_del_10\",\"startDate\":\"2020-07-05T00:00:00Z\",\"endDate\":\"2020-07-10T00:00:00Z\",\"daysToRetain\":3,\"author\":\"admin\"}";
	Long expectedStartDateForDeleteCase = InsightsUtils.getEpochTime("2020-07-05T00:00:00Z") / 1000;
	Long expectedEndDateForDeleteCase = InsightsUtils.getEpochTime("2020-07-10T00:00:00Z") / 1000;
	int expectedDaysToRetainForDeleteCase = 3;
	String saveArchivalRecordsWithoutName = "{\"archivalName\":\"\",\"startDate\":\"2020-07-08T00:00:00Z\",\"endDate\":\"2020-07-10T00:00:00Z\",\"daysToRetain\":3,\"author\":\"\"}";
	String saveArchivalRecordsIncompleteData = "{\"archivalName\":Test_incomplete\"\",\"startDate\":\"2020-07-08T00:00:00Z\",\"endDate\":\"2020-07-10T00:00:00Z\",\"author\":\"\"}";
	String saveArchivalRecordsIncorrectNameData = "{\"archivalName\":Test-incomplete\"\",\"startDate\":\"2020-07-08T00:00:00Z\",\"endDate\":\"2020-07-10T00:00:00Z\",\"daysToRetain\":3,\"author\":\"\"}";
	String saveArchivalRecordsIncorrectDateData = "{\"archivalName\":Test_rec\"\",\"startDate\":\"abc\",\"endDate\":\"2020-07-10T00:00:00Z\",\"daysToRetain\":3,\"author\":\"\"}";
	String saveArchivalRecordsIncorrectDaysToRetainData = "{\"archivalName\":Test_rec\"\",\"startDate\":\"abc\",\"endDate\":\"2020-07-10T00:00:00Z\",\"daysToRetain\":3,\"author\":\"\"}";
	String saveArchivalRecordsLargeDaysToRetainData = "{\"archivalName\":Test_rec\"\",\"startDate\":\"abc\",\"endDate\":\"2020-07-10T00:00:00Z\",\"daysToRetain\":100000000000,\"author\":\"\"}";
	String saveArchivalRecordsWithStartDateGreaterThanEndDateData = "{\"archivalName\":\"Dib_testng_10\",\"startDate\":\"2020-07-10T00:00:00Z\",\"endDate\":\"2020-07-08T00:00:00Z\",\"daysToRetain\":3,\"author\":\"\"}";
	String updateSourceURL = "{\"archivalName\":\"Dib_testng_10\",\"sourceUrl\":\"http://localhost:7575\"}";
	String updateSourceURLWithEmptyName = "{\"archivalName\":\"\",\"sourceUrl\":\"http://localhost:7575\"}";
	String wrongRecordName = "abc";
	
	public JsonObject updateSourceURLJson = convertStringIntoJson(updateSourceURL);
	public JsonObject updateSourceURLJsonWithEmptyName = convertStringIntoJson(updateSourceURLWithEmptyName);
	public JsonObject saveArchivalRecordsJson = convertStringIntoJson(saveArchivalRecords);
	public JsonObject saveArchivalRecordsWithoutNameJson = convertStringIntoJson(saveArchivalRecordsWithoutName);
	public JsonObject saveArchivalRecordsIncompleteDataJson = convertStringIntoJson(saveArchivalRecordsIncompleteData);
	public JsonObject saveArchivalRecordsIncorrectNameDataJson = convertStringIntoJson(saveArchivalRecordsIncorrectNameData);
	public JsonObject saveArchivalRecordsIncorrectDateDataJson = convertStringIntoJson(saveArchivalRecordsIncorrectDateData);
	public JsonObject saveArchivalRecordsIncorrectDaysToRetainDataJson = convertStringIntoJson(saveArchivalRecordsIncorrectDaysToRetainData);
	public JsonObject saveArchivalRecordsLargeDaysToRetainDataJson = convertStringIntoJson(saveArchivalRecordsLargeDaysToRetainData);
	public JsonObject saveArchivalRecordsForDeleteCaseJson = convertStringIntoJson(saveArchivalRecordsForDeleteCase);
	public JsonObject saveArchivalRecordsWithStartDateGreaterThanEndDateDataJson = convertStringIntoJson(saveArchivalRecordsWithStartDateGreaterThanEndDateData);
	
	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = parser.parse(convertregisterkpi).getAsJsonObject();
		return objectJson;
	}
	
	public Long getExpiryDate(Long createdOn, int daysToRetain) {
		Long days = (long)(daysToRetain *24*60*60);
		return (createdOn + days);
		
	}
}
