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
package com.cognizant.devops.platformcommons.core.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataPurgingUtils {

	private static final String DATE_TIME_FORMAT = "yyyy/MM/dd hh:mm a";
	
	private DataPurgingUtils() {		
	}
	
	/**
	 * Calculates nextRunTime as per dataArchivalFrequency and job schedule
	 * @param dataArchivalFrequency
	 * @return
	 */
	public static String calculateNextRunTime(String dataArchivalFrequency) {
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
		String nextRunTime = null;
		if(JobSchedule.DAILY.name().equalsIgnoreCase(dataArchivalFrequency)){
			//Schedule daily at 01:00 am
			LocalDateTime nextDaySchedule = currentDateTime.plusDays(1);
			nextDaySchedule = nextDaySchedule.withHour(01).withMinute(00);
			nextRunTime = dtf.format(nextDaySchedule);
		} else if(JobSchedule.WEEKLY.name().equalsIgnoreCase(dataArchivalFrequency)){
			//Schedule weekly every Monday at 01:00 am
			LocalDateTime nextOrSameMonday = currentDateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY ) ) ;
			nextOrSameMonday = nextOrSameMonday.withHour(01).withMinute(00);
			nextRunTime = dtf.format(nextOrSameMonday);
		} else if(JobSchedule.MONTHLY.name().equalsIgnoreCase(dataArchivalFrequency)){
			//Schedule monthly 01st day of every month at 01:00 AM
			LocalDateTime firstDayOfNextMonth = currentDateTime.with(TemporalAdjusters.firstDayOfNextMonth()) ;
			firstDayOfNextMonth = firstDayOfNextMonth.withHour(01).withMinute(00);
			nextRunTime = dtf.format(firstDayOfNextMonth);
		}
		return nextRunTime;
	}
	
	/**
	 * Returns value of lastRunTime in String from JsonObject
	 * @param settingsJsonObject
	 * @return
	 */
	public static String getLastRunTime(JsonObject settingsJsonObject) {
		String lastRunTime = null;
		if (settingsJsonObject != null && settingsJsonObject.get(ConfigOptions.LAST_RUN_TIME)!= null) {
			lastRunTime = settingsJsonObject.get(ConfigOptions.LAST_RUN_TIME).getAsString();
		}
		return lastRunTime;
	}
	
	/**
	 * Returns value of nextRunTime in String from JsonObject
	 * @param settingsJsonObject
	 * @return
	 */
	public static String getNextRunTime(JsonObject settingsJsonObject) {
		String nextRunTime = null;
		if (settingsJsonObject != null && settingsJsonObject.get(ConfigOptions.NEXT_RUN_TIME)!= null) {
			nextRunTime = settingsJsonObject.get(ConfigOptions.NEXT_RUN_TIME).getAsString();
		}
		return nextRunTime;
	}
	
	/**
	 * Returns value of dataArchivalFrequency in String from JsonObject
	 * @param settingsJsonObject
	 * @return
	 */
	public static String getDataArchivalFrequency(JsonObject settingsJsonObject) {
		String dataArchivalFrequency = null;
		if (settingsJsonObject != null && settingsJsonObject.get(ConfigOptions.DATA_ARCHIVAL_FREQUENCY)!= null) {
			dataArchivalFrequency = settingsJsonObject.get(ConfigOptions.DATA_ARCHIVAL_FREQUENCY).getAsString();
		}
		return dataArchivalFrequency;
	}
	
	/**
	 * Converts settingJson String into a JsonObject
	 * @param settingsJson
	 * @return
	 */
	public static JsonObject convertSettingsJsonObject(String settingsJson) {
		if (settingsJson != null && !settingsJson.isEmpty()) {
			Gson gson = new Gson();
			JsonElement jsonElement = gson.fromJson(settingsJson.trim(),JsonElement.class);
			return jsonElement.getAsJsonObject();
		}
		return null;
	}
	
	/**
	 * Updates calculated next run time inside JsonObject
	 * @param settingsJsonObject
	 * @param nextRunTime
	 * @return
	 */
	public static JsonObject updateNextRunTime(JsonObject settingsJsonObject, String nextRunTime) {
		if (settingsJsonObject != null) {
			settingsJsonObject.remove(ConfigOptions.NEXT_RUN_TIME);
			settingsJsonObject.addProperty(ConfigOptions.NEXT_RUN_TIME, nextRunTime);
		}
		return settingsJsonObject;
	}
	
	/**
	 * Updates last run time inside JsonObject
	 * @param settingsJsonObject
	 * @param lastRunTime
	 * @return
	 */
	public static JsonObject updateLastRunTime(JsonObject settingsJsonObject, String lastRunTime) {
		if (settingsJsonObject != null) {
			settingsJsonObject.remove(ConfigOptions.LAST_RUN_TIME);
			settingsJsonObject.addProperty(ConfigOptions.LAST_RUN_TIME, lastRunTime);
		}
		return settingsJsonObject;
	}
	

}
