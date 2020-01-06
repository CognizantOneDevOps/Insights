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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.gson.JsonObject;

public class TraceabilitySummaryUtil {

	public static String TimeDiffrence(String operandName, String operandValue, List<JsonObject> toolRespPayload,
			String message) throws ParseException {
		int totalCount = toolRespPayload.size();
		if (totalCount >= 0) {
			if (toolRespPayload.get(0).has(operandName)) {
				int numOfUniqueAuthers = (int) toolRespPayload.stream()
						.filter(distinctByKey(payload -> payload.get("author").toString().replaceAll("\"", "")))
						.count();
				String firstCommitTime = toolRespPayload.get(0).get(operandName).toString().replaceAll("\"", "");
				String lastCommitTime = toolRespPayload.get(totalCount - 1).get(operandName).toString().replaceAll("\"",
						"");
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
				Date firstDate = sdf.parse(epochToHumanDate(firstCommitTime));
				Date secondDate = sdf.parse(epochToHumanDate(lastCommitTime));
				long diffInMillies = Math.abs(firstDate.getTime() - secondDate.getTime());
				long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
				long hours = TimeUnit.MILLISECONDS.toHours(diffInMillies)
						- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diffInMillies));
				long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillies)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffInMillies));
				long yrs = 0;
				long month = 0;
				String duration;
				if (days > 365) {
					yrs = days / 365;
					days = days - (365 * yrs);
					if (days >= 30) // Calculate month
					{
						month = days / 30;
						days = days - (month * 30);
					}
					duration = yrs + " Yrs " + month + "Month(s) " + days + " Days " + hours + " Hrs " + minutes
							+ " Mins";
				} else if (days >= 30 && days < 365) // Calculate month
				{
					month = days / 30;
					days = days - (month * 30);
					duration = month + "Month(s) " + days + " Days " + hours + " Hrs " + minutes + " Mins";
				} else {
					duration = days + " Days " + hours + " Hrs " + minutes + " Mins";
				}
				MessageFormat mf = new MessageFormat(message);
				String msg = mf.format(new Object[] { duration, numOfUniqueAuthers }).replaceAll("\"", "");
				return msg;
			}
		}
		return "";

	}

	public static String SUM(String operandName, String operandValue, List<JsonObject> toolRespPayload,
			String message) {

		int totalCount = toolRespPayload.size();
		if (totalCount >= 0) {
			if (toolRespPayload.get(0).has(operandName)) {
				int count = (int) toolRespPayload.stream()
						.filter(payload -> payload.get(operandName).toString().replace("\"", "").equals(operandValue))
						.count();
				int rem = totalCount - count;
				MessageFormat mf = new MessageFormat(message);
				String msg = mf.format(new Object[] { count, rem }).replaceAll("\"", "");
				return msg;
			}
		}
		return "";

	}

	public static String Percentage(String operandName, String operandValue, List<JsonObject> toolRespPayload,
			String message) {
		int totalCount = toolRespPayload.size();
		if (totalCount >= 0) {
			if (toolRespPayload.get(0).has(operandName)) {
				int count = (int) toolRespPayload.stream()
						.filter(payload -> payload.get(operandName).toString().replace("\"", "").equals(operandValue))
						.count();
				int perc = (count * 100) / totalCount;
				MessageFormat mf = new MessageFormat(message);
				String msg = mf.format(new Object[] { perc, totalCount }).replaceAll("\"", "");
				return msg;
			}
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
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		return format.format(date);
	}

}
