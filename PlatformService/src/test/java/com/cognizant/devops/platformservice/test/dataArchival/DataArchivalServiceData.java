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

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.google.gson.JsonObject;


public class DataArchivalServiceData extends AbstractTestNGSpringContextTests {
	String trackingDetails = "";
	Date updateDate = Timestamp.valueOf(LocalDateTime.now());
	Boolean vault = false;
	Long expectedStartDate = InsightsUtils.getEpochTime("2020-07-08T00:00:00Z") / 1000;
	Long expectedEndDate = InsightsUtils.getEpochTime("2020-07-10T00:00:00Z") / 1000;
	int expectedDaysToRetain = 3;
	Long expectedStartDateForDeleteCase = InsightsUtils.getEpochTime("2020-07-05T00:00:00Z") / 1000;
	Long expectedEndDateForDeleteCase = InsightsUtils.getEpochTime("2020-07-10T00:00:00Z") / 1000;
	int expectedDaysToRetainForDeleteCase = 3;
	String wrongRecordName = "abc";
	
	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = JsonUtils.parseStringAsJsonObject(convertregisterkpi);
		return objectJson;
	}
	
	public Long getExpiryDate(Long createdOn, int daysToRetain) {
		Long days = (long)(daysToRetain *24*60*60);
		return (createdOn + days);
		
	}
}