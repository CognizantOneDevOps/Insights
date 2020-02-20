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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;


public class InsightsUtils {

	static Logger log = LogManager.getLogger(InsightsUtils.class.getName());
	private InsightsUtils() {
	}

	private static String sparkTimezone = ApplicationConfigProvider.getInstance().getInsightsTimeZone();
	public static ZoneId zoneId = ZoneId.of(sparkTimezone);
	public static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static long getDataFromTime(String schedule) {
		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneId);

		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusDays(1);
			start = start.toLocalDate().atStartOfDay(zoneId);
			time = start.toInstant().toEpochMilli();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusDays(7).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			start = start.toLocalDate().atStartOfDay(zoneId);
			start = start.plusSeconds(1);
			time = start.toInstant().toEpochMilli();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			ZonedDateTime firstDayofLastMonth = firstDayofMonth.minusMinutes(1)
					.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneId);
			firstDayofLastMonth = firstDayofLastMonth.plusSeconds(1);
			time = firstDayofLastMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			// Will add code later for this usecase
			// zdt = zdt.plusMonths(3);
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate()
					.atStartOfDay(zoneId);
			ZonedDateTime firstDayofLastYear = firstDayofYear.minusMinutes(1).with(TemporalAdjusters.firstDayOfYear())
					.toLocalDate().atStartOfDay(zoneId);
			firstDayofYear = firstDayofLastYear.plusSeconds(1);
			time = firstDayofLastYear.toInstant().toEpochMilli();
		}

		return time;
	}

	public static long getDataToTime(String schedule) {

		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneId);

		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime today = now.toLocalDate().atStartOfDay(zoneId);
			ZonedDateTime yesterdayEnd = today.minusSeconds(1);
			time = yesterdayEnd.toInstant().toEpochMilli();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			ZonedDateTime lastWeek = todayStart.minusSeconds(1);
			time = lastWeek.toInstant().toEpochMilli();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			ZonedDateTime firstDayofLastMonth = firstDayofMonth.minusMinutes(1)
					.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneId);
			ZonedDateTime lastDayOftheMonth = firstDayofLastMonth.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			lastDayOftheMonth = lastDayOftheMonth.minusSeconds(1);
			time = lastDayOftheMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			// Will add code later for this usecase
			// zdt = zdt.plusMonths(3);
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate()
					.atStartOfDay(zoneId);
			ZonedDateTime firstDayofLastYear = firstDayofYear.minusMinutes(1).with(TemporalAdjusters.firstDayOfYear())
					.toLocalDate().atStartOfDay(zoneId);
			ZonedDateTime lastDayofYear = firstDayofLastYear.with(TemporalAdjusters.lastDayOfYear()).toLocalDate()
					.atStartOfDay(zoneId);
			lastDayofYear = lastDayofYear.minusSeconds(1);
			time = lastDayofYear.toInstant().toEpochMilli();
		}
		return time;

	}

	public static long getDataFromTime(String schedule, Integer since) {
		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		since = since - 1; // Till time will give result of last week. So if it is last 5 weeks or years,
							// it will be always since - 1
		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusDays(since);
			start = start.toLocalDate().atStartOfDay(zoneId);
			time = start.toInstant().toEpochMilli();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusWeeks(since);
			start = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			start = start.toLocalDate().atStartOfDay(zoneId);
			start = start.plusSeconds(1);
			time = start.toInstant().toEpochMilli();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.minusMonths(since);
			ZonedDateTime firstDayofLastMonth = firstDayofMonth.minusMinutes(1)
					.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneId);
			firstDayofLastMonth = firstDayofLastMonth.plusSeconds(1);
			time = firstDayofLastMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			// Will add code later for this usecase
			// zdt = zdt.plusMonths(3);
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.minusYears(since);
			ZonedDateTime firstDayofLastYear = firstDayofYear.minusMinutes(1).with(TemporalAdjusters.firstDayOfYear())
					.toLocalDate().atStartOfDay(zoneId);
			firstDayofYear = firstDayofLastYear.plusSeconds(1);
			time = firstDayofLastYear.toInstant().toEpochMilli();
		}

		return time;
	}

	/**
	 * Whatever first run date is set, next calculation of daily or weekly will be
	 * based on that. Also need to pass Zone details to this method. As of now
	 * hardcoding it.
	 * 
	 * @param schedule
	 * @return
	 */
	public static Long getNextRun(String schedule) {
		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneId);

		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
			todayStart = todayStart.plusMinutes(1);
			ZonedDateTime tomorrowStart = todayStart.plusDays(1);
			time = tomorrowStart.toInstant().toEpochMilli();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			todayStart = todayStart.plusMinutes(1);
			ZonedDateTime nextweek = todayStart.plusWeeks(1);
			time = nextweek.toInstant().toEpochMilli();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofNextMonth = now.with(TemporalAdjusters.firstDayOfNextMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			firstDayofNextMonth = firstDayofNextMonth.plusMinutes(1);
			time = firstDayofNextMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			// Will add code later for this usecase
			// zdt = zdt.plusMonths(3);
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofNextYear = now.with(TemporalAdjusters.firstDayOfNextYear()).toLocalDate()
					.atStartOfDay(zoneId);
			firstDayofNextYear = firstDayofNextYear.plusMinutes(1);
			time = firstDayofNextYear.toInstant().toEpochMilli();
		}
		return time;
	}

	/**
	 * Keeping the date as start of the day or week or month or year, as it will
	 * facilitate to run next scheduled
	 * 
	 * @param schedule
	 * @return
	 */
	public static Long getLastRunTime(String schedule) {
		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneId);

		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
			todayStart = todayStart.plusMinutes(1);
			time = todayStart.toInstant().toEpochMilli();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime sundayDate = now.toLocalDate().atStartOfDay(zoneId)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			sundayDate = sundayDate.plusMinutes(1);
			time = sundayDate.toInstant().toEpochMilli();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			firstDayofMonth = firstDayofMonth.plusMinutes(1);
			time = firstDayofMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			// Will add code later for this usecase
			// zdt = zdt.plusMonths(3);
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate()
					.atStartOfDay(zoneId);
			firstDayofYear = firstDayofYear.plusMinutes(1);
			time = firstDayofYear.toInstant().toEpochMilli();
		}
		return time;
	}

	public static Long getStartEpochTime() {
		ZonedDateTime now = ZonedDateTime.now(zoneId);

		ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
		todayStart = todayStart.plusSeconds(1);
		return todayStart.toInstant().toEpochMilli();
	}

	public static Long getEndEpochTime() {
		ZonedDateTime now = ZonedDateTime.now(zoneId);

		ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
		todayStart = todayStart.plusHours(24).minusSeconds(1);
		return todayStart.toInstant().toEpochMilli();
	}

	public static Long getTodayTime() {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return now.toInstant().toEpochMilli();
	}

	public static ZonedDateTime getTodayTimeX() {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return now;
	}

	public static Long getTimeAfterDays(Long days) {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime after = now.plusDays(days);
		return after.toInstant().toEpochMilli();
	}

	public static Long getTimeBeforeDays(Long days) {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime after = now.minusDays(days);
		return after.toInstant().toEpochMilli();
	}

	public static Long getTimeBeforeDaysInSeconds(Long days) {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime after = now.minusDays(days);
		return after.toInstant().getEpochSecond();
	}

	public static Long getDurationBetweenDatesInDays(Long inputEpochMillis) {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochMilli(inputEpochMillis), zoneId);
		Duration d = Duration.between(fromInput, now);
		return d.toDays();
	}

	public static int getCurrentMonthDays() {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return now.toLocalDate().lengthOfMonth();
	}

	public static int getCurrentYearDays() {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return now.toLocalDate().lengthOfYear();
	}

	public static Long addTimeInHours(long inputTime, long hours) {
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(inputTime), zoneId);
		fromInput = fromInput.plusHours(hours);
		return fromInput.toEpochSecond();
	}

	public static Long subtractTimeInHours(long inputTime, long hours) {
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(inputTime), zoneId);
		fromInput = fromInput.minusHours(hours);
		return fromInput.toEpochSecond();
	}

	public static Long getCurrentTimeInSeconds() {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return now.toEpochSecond();
	}

	public static Long getSystemTimeInNanoSeconds() {
		return System.nanoTime();
	}

	public static Long getCurrentTimeInEpochMilliSeconds() {
		return System.currentTimeMillis();
	}

	public static Long addVarianceTime(long inputTime, long varianceSeconds) {
		Long varianceTime = inputTime + varianceSeconds;
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(varianceTime), zoneId);
		return fromInput.toEpochSecond();
	}

	public static Long subtractVarianceTime(long inputTime, long varianceSeconds) {
		Long varianceTime = inputTime - varianceSeconds;
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(varianceTime), zoneId);
		return fromInput.toEpochSecond();
	}

	public static Boolean isAfterRange(String schedule, Long days) {
		Boolean result = Boolean.FALSE;
		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			if (days >= 1) {
				result = Boolean.TRUE;
			}
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			if (days >= 7) {
				result = Boolean.TRUE;
			}
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			if (days >= getCurrentMonthDays()) {
				result = Boolean.TRUE;
			}
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			if (days >= getCurrentYearDays()) {
				result = Boolean.TRUE;
			}
		}

		return result;
	}

	public static String getLocalDateTime(String formatPattern) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatPattern);
		return dtf.format(now);
	}
	
	public static String getUtcTime(String timezone) {
		SimpleDateFormat  dtf = new SimpleDateFormat(DATE_TIME_FORMAT);
		dtf.setTimeZone(TimeZone.getTimeZone(timezone));
		return  dtf.format(new Date());
	}

	/**
	 * 
	 * Calculates difference between currentTime and lastRunTime (now - lastRunTime)
	 * 
	 * @param lastRunTime
	 * @return
	 */
	public static long getDifferenceFromLastRunTime(long lastRunTime) {
		ZonedDateTime now = ZonedDateTime.now(InsightsUtils.zoneId);
		ZonedDateTime lastRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastRunTime),
				InsightsUtils.zoneId);
		Duration d = Duration.between(lastRunTimeInput, now);
		return d.abs().toMillis();
	}

	/**
	 * Calculates difference between nextRunTime and lastRunTime (nextRunTime -
	 * lastRunTime )
	 * 
	 * @param lastRunTime
	 * @param nextRunTime
	 * @return
	 */
	public static long getDifferenceFromNextRunTime(Long lastRunTime, Long nextRunTime) {
		ZonedDateTime lastRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastRunTime),
				InsightsUtils.zoneId);
		ZonedDateTime nextRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime),
				InsightsUtils.zoneId);
		Duration d = Duration.between(lastRunTimeInput, nextRunTimeInput);
		return d.abs().toMillis();
	}
	
	public static long getEpochTime(String datetime) {

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
		Date dt;
		try {
			dt = sdf.parse(datetime);			
			return dt.getTime();

		} catch (ParseException e) {
			log.error(e.getMessage());
			return 0;
		}
	}
	
	public static String getDateTimeFromEpoch(long milliseconds) {

		String duration;
		long days = TimeUnit.DAYS.convert(milliseconds, TimeUnit.MILLISECONDS);
		long yrs = 0;
		long month = 0;
		if (days > 365) {
			yrs = days / 365;
			days = days - (365 * yrs);
			if (days >= 30) // Calculate month
			{
				month = days / 30;
				days = days - (month * 30);
			}
			duration = yrs + " Yrs " + month + "Month(s) " + days + " Days ";
		} else if (days >= 30 && days < 365) // Calculate month
		{
			month = days / 30;
			days = days - (month * 30);
			duration = month + "Month(s) " + days + " Days ";
		} else {
			duration = days + " Days ";
		} 
		return duration;
	}
}
