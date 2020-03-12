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
package com.cognizant.devops.platformservice.traceabilitydashboard.util;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TraceabilitySummaryUtil {

	static Logger log = LogManager.getLogger(TraceabilitySummaryUtil.class.getName());
	static final String PATTERN ="[\\[\\](){}\"\\\"\"]";

	public static String calTimeDiffrence(String operandName, List<JsonObject> toolRespPayload, String message)
			throws ParseException {
		int totalCount = toolRespPayload.size();
		if (totalCount >= 0 && toolRespPayload.get(0).has(operandName)) {
			int numOfUniqueAuthers = (int) toolRespPayload.stream()
					.filter(distinctByKey(payload -> payload.get("author").getAsString())).count();
			String firstCommitTime = toolRespPayload.get(0).get(operandName).getAsString();
			String lastCommitTime = toolRespPayload.get(totalCount - 1).get(operandName).getAsString();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
			Date firstDate = sdf.parse(epochToHumanDate(firstCommitTime));
			Date secondDate = sdf.parse(epochToHumanDate(lastCommitTime));
			long diffInMillies = Math.abs(firstDate.getTime() - secondDate.getTime());
			String duration = InsightsUtils.getDateTimeFromEpoch(diffInMillies);
			MessageFormat mf = new MessageFormat(message);
			return mf.format(new Object[] { duration, numOfUniqueAuthers });
		}

		return "";

	}

	public static String calSUM(String operandName, String operandValue, List<JsonObject> toolRespPayload,
			String message) {
		int totalCount = toolRespPayload.size();
		if (totalCount >= 0 && toolRespPayload.get(0).has(operandName)) {			
				int count = (int) toolRespPayload.stream()
						.filter(payload -> payload.get(operandName).getAsString().equals(operandValue)).count();
				int rem = totalCount - count;
				MessageFormat mf = new MessageFormat(message);
				return mf.format(new Object[] { count, rem });
			
		}
		return "";

	}

	public static String calPercentage(String operandName, String operandValue, List<JsonObject> toolRespPayload,
			String message) {
		int totalCount = toolRespPayload.size();
		if (totalCount >= 0 && toolRespPayload.get(0).has(operandName)) {
			int count = (int) toolRespPayload.stream()
					.filter(payload -> payload.get(operandName).getAsString().equals(operandValue)).count();
			int perc = (count * 100) / totalCount;
			MessageFormat mf = new MessageFormat(message);
			return mf.format(new Object[] { perc, totalCount });
		}

		return "";

	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	private static String epochToHumanDate(String epochtime) {
		Long epoch = Long.valueOf(epochtime.split("\\.", 2)[0]);
		Date date = new Date(epoch * 1000L);
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		return format.format(date);
	}

	public static JsonObject timelag(HashMap<String, List<JsonObject>> map, JsonObject dataModel) {

		HashMap<String, Long> timelagMap = new HashMap<>();
		JsonObject timelagObject = new JsonObject();
		map.forEach(new BiConsumer<String, List<JsonObject>>() {
			public void accept(String toolName, List<JsonObject> payload) {
				JsonObject toolJson = dataModel.get(toolName).getAsJsonObject();				
				if (toolJson.has("timelagParam")) {
					JsonArray timelagArray = toolJson.get("timelagParam").getAsJsonArray();
					int timelagArraySize = timelagArray.size();
					if (timelagArraySize == 1) {
						String timelagParam = timelagArray.get(0).toString().replaceAll(PATTERN, "")
								.split(",")[0];
						if (calTimelag(timelagParam, payload) != -1)
							timelagMap.put(toolName, calTimelag(timelagParam, payload));
					} else if (timelagArraySize == 2) {
						String timelagParam1 = timelagArray.get(0).toString().replaceAll(PATTERN, "")
								.split(":")[0];
						String timelagParam2 = timelagArray.get(1).toString().replaceAll(PATTERN, "")
								.split(":")[0];
						if (calTimelag(timelagParam1, timelagParam2, payload) != -1)
							timelagMap.put(toolName, calTimelag(timelagParam1, timelagParam2, payload));

					}
				}
			}

		});

		timelagMap.forEach(
				(toolName, time) -> timelagObject.addProperty(toolName, InsightsUtils.getDateTimeFromEpoch(time)));

		return timelagObject;
	}

	public static long calTimelag(String timelagParam1, String timelagParam2, List<JsonObject> payload) {

		List<Long> epoch = new ArrayList<>();
		String startTime = "";
		String endTime = "";
		for (JsonObject eachObject : payload) {
			if (eachObject.has(timelagParam1)) {
				startTime = eachObject.get(timelagParam1).getAsString();
			}
			if (eachObject.has(timelagParam2)) {
				endTime = eachObject.get(timelagParam2).getAsString();
			}
			if (!startTime.equals("") && !endTime.equals("")) {
				Long timeDiff = InsightsUtils.getEpochTime(endTime) - InsightsUtils.getEpochTime(startTime);
				epoch.add(timeDiff);
			}
		}
		int size = epoch.size();
		List<Long> epochTimeList = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			if (i != size - 1) {
				long firstElement = epoch.get(i);
				long secondElement = epoch.get(i + 1);
				epochTimeList.add(Math.abs(firstElement - secondElement));
			}
		}
		if (!epochTimeList.isEmpty()) {
			return (long) epochTimeList.stream().mapToLong(val -> val).average().getAsDouble();
		}
		return -1;

	}

	public static long calTimelag(String timelagParam, List<JsonObject> payload) {
		List<Long> epoch = new ArrayList<>();
		for (JsonObject eachObject : payload) {
			if (eachObject.has(timelagParam)) {
				String commitTime = eachObject.get(timelagParam).getAsString();
				epoch.add(InsightsUtils.getEpochTime(commitTime));
			}
		}
		int size = epoch.size();
		List<Long> epochTimeList = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			if (i != size - 1) {
				long firstElement = epoch.get(i);
				long secondElement = epoch.get(i + 1);
				epochTimeList.add(firstElement - secondElement);
			}
		}
		if (!epochTimeList.isEmpty()) {
			return (long) epochTimeList.stream().mapToLong(val -> val).average().getAsDouble();
		}
		return -1;
	}

}
