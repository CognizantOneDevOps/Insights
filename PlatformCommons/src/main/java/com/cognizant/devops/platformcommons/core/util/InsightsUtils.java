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


import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;


public class InsightsUtils {

	static Logger log = LogManager.getLogger(InsightsUtils.class.getName());

	private InsightsUtils() {
	}
	private static final String DAYS= " Days ";
	private static String sparkTimezone = ApplicationConfigProvider.getInstance().getInsightsTimeZone();
	public static final ZoneId zoneId = ZoneId.of(sparkTimezone);
	public static final ZoneId zoneIdUTC = ZoneId.of("UTC");
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static long getDurationBetweenTime(long time) {
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);
		return now.toInstant().getEpochSecond() - time;
	}

	public static String specficTimeFormat(long inputTimeInMillis, String timeFormat) {
		LocalDateTime utcTime=ZonedDateTime.ofInstant(Instant.ofEpochMilli(inputTimeInMillis), zoneIdUTC).toLocalDateTime();
		return utcTime.format(DateTimeFormatter.ofPattern(timeFormat));	
	}

	public static long getDataFromTime(String schedule) {
		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);

		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusDays(1);
			start = start.toLocalDate().atStartOfDay(zoneIdUTC);
			time = start.toInstant().toEpochMilli();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusDays(7).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			start = start.toLocalDate().atStartOfDay(zoneIdUTC);
			start = start.plusSeconds(1);
			time = start.toInstant().toEpochMilli();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			ZonedDateTime firstDayofLastMonth = firstDayofMonth.minusMinutes(1)
					.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneIdUTC);
			firstDayofLastMonth = firstDayofLastMonth.plusSeconds(1);
			time = firstDayofLastMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			ZonedDateTime lastDayofLastMonth = firstDayofMonth.minusMinutes(1);
			ZonedDateTime firstDayOfTheLastQuarter = lastDayofLastMonth.minusMonths(3);
			firstDayOfTheLastQuarter = firstDayOfTheLastQuarter.plusSeconds(1);
			time = firstDayOfTheLastQuarter.toInstant().toEpochMilli();
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			ZonedDateTime firstDayofLastYear = firstDayofYear.minusMinutes(1).with(TemporalAdjusters.firstDayOfYear())
					.toLocalDate().atStartOfDay(zoneIdUTC);
			time = firstDayofLastYear.toInstant().toEpochMilli();
		}

		return time;
	}

	public static long getStartOfTheDay(long startTime) {
		ZonedDateTime startDateZone = ZonedDateTime.ofInstant(Instant.ofEpochSecond(startTime),
				InsightsUtils.zoneIdUTC);
		ZonedDateTime startDateOfTheWeek = startDateZone.toLocalDate().atStartOfDay(zoneIdUTC);
		return startDateOfTheWeek.toInstant().getEpochSecond();
	}
	
	public static long getDataToTime(String schedule) {
		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);

		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime today = now.toLocalDate().atStartOfDay(zoneIdUTC);
			ZonedDateTime yesterdayEnd = today.minusSeconds(1);
			time = yesterdayEnd.toInstant().toEpochMilli();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneIdUTC)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			ZonedDateTime lastWeek = todayStart.minusSeconds(1);
			time = lastWeek.toInstant().toEpochMilli();
		} else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneIdUTC);
		}
		else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			ZonedDateTime firstDayofLastMonth = firstDayofMonth.minusMinutes(1)
					.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneIdUTC);
			ZonedDateTime lastDayOftheMonth = firstDayofLastMonth.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			lastDayOftheMonth = lastDayOftheMonth.minusSeconds(1);
			time = lastDayOftheMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			ZonedDateTime firstDayofLastYear = firstDayofYear.minusMinutes(1).with(TemporalAdjusters.firstDayOfYear())
					.toLocalDate().atStartOfDay(zoneIdUTC);
			ZonedDateTime lastDayofYear = firstDayofLastYear.with(TemporalAdjusters.lastDayOfYear()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			lastDayofYear = lastDayofYear.minusSeconds(1);
			time = lastDayofYear.toInstant().toEpochMilli();
		}
		return time;
	}

	public static long addTimeInCurrentTime(long currentTimeInSeconds, String frequency, long value) {
		long time = 0l;
		ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentTimeInSeconds), zoneIdUTC);
		if (frequency.equalsIgnoreCase("DAYS")) {
			time = currentTime.plusDays(value).toEpochSecond();
		} else if (frequency.equalsIgnoreCase("WEEKS")) {
			time = currentTime.plusWeeks(value).toEpochSecond();
		} else if (frequency.equalsIgnoreCase("MONTHS")) {
			time = currentTime.plusMonths(value).toEpochSecond();
		} else if (frequency.equalsIgnoreCase("YEARS")) {
			time = currentTime.plusYears(value).toEpochSecond();
		}
		return time;
	}

	public static long getUTCTime(long dateTime) {
		ZonedDateTime utcTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(dateTime), zoneIdUTC);
		return utcTime.toInstant().getEpochSecond();
	}

	

	public static long getDataFromTime(String schedule, Integer since) {
		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);
		since = since - 1; // Till time will give result of last week. So if it is last 5 weeks or years,
							// it will be always since - 1
		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusDays(since);
			start = start.toLocalDate().atStartOfDay(zoneIdUTC);
			time = start.toInstant().toEpochMilli();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusWeeks(since);
			start = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			start = start.toLocalDate().atStartOfDay(zoneIdUTC);
			start = start.plusSeconds(1);
			time = start.toInstant().toEpochMilli();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.minusMonths(since);
			ZonedDateTime firstDayofLastMonth = firstDayofMonth.minusMinutes(1)
					.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneIdUTC);
			firstDayofLastMonth = firstDayofLastMonth.plusSeconds(1);
			time = firstDayofLastMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.minusYears(since);
			ZonedDateTime firstDayofLastYear = firstDayofYear.minusMinutes(1).with(TemporalAdjusters.firstDayOfYear())
					.toLocalDate().atStartOfDay(zoneIdUTC);
			time = firstDayofLastYear.toInstant().toEpochMilli();
		}
		return time;
	}

	public static long addDaysInGivenTime(long time, long days) {
		ZonedDateTime givenTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneIdUTC);
		ZonedDateTime calculatedTime = givenTime.plusDays(days);
		return calculatedTime.toInstant().getEpochSecond();
	}

	public static long getStartFromTime(long nextRunTime, String schedule) {
		long time = 0;	
		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneIdUTC);
			ZonedDateTime startTime = nextTime.minusDays(1);
			time = startTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneIdUTC);
			ZonedDateTime startTime = nextTime.minusDays(7);
			time = startTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneIdUTC);
			ZonedDateTime startTime = nextTime.minusDays(14);
			time = startTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.TRI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneIdUTC);
			ZonedDateTime startTime = nextTime.minusDays(21);
			time = startTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneIdUTC);
			ZonedDateTime startTime = nextTime.minusMonths(1);
			time = startTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneIdUTC);
			ZonedDateTime startTime = nextTime.minusMonths(3);
			time = startTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneIdUTC);
			ZonedDateTime startTime = nextTime.minusYears(1);
			time = startTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.ONETIME.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneIdUTC);
			time = startTime.toInstant().getEpochSecond();
		}
		return time;
	}

	public static Long getNextRunTime(long currentNextTime, String schedule, boolean isInitialNextRunTime) {
		long time = 0;

		if (isInitialNextRunTime) {
			long todayStartOfTheDay = getStartDay(currentNextTime, schedule);
			currentNextTime = todayStartOfTheDay;
		}
		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneIdUTC);
			ZonedDateTime nextRunTime = currentTime.plusDays(1);
			time = nextRunTime.toInstant().getEpochSecond();
		}
		else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneIdUTC);
			ZonedDateTime nextRunTime = currentTime.plusDays(7);
			time = nextRunTime.toInstant().getEpochSecond();
		}
		else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneIdUTC);
			ZonedDateTime nextRunTime = currentTime.plusDays(14);
			time = nextRunTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.TRI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneIdUTC);
			ZonedDateTime nextRunTime = currentTime.plusDays(21);
			time = nextRunTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneIdUTC);
			ZonedDateTime nextRunTime = currentTime.plusMonths(1);
			time = nextRunTime.toInstant().getEpochSecond();
		}
		else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneIdUTC);
			ZonedDateTime nextRunTime = currentTime.plusMonths(3);
			time = nextRunTime.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneIdUTC);
			ZonedDateTime nextRunTime = currentTime.plusYears(1);
			time = nextRunTime.toInstant().getEpochSecond();
		}
		return time;
	}

	public static long getStartDay(long startDate, String schedule) {

		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);
		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.toLocalDate().atStartOfDay(zoneIdUTC).plusSeconds(1);
			time = start.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.minusDays(7).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			start = start.toLocalDate().atStartOfDay(zoneIdUTC);
			start = start.plusSeconds(1);
			time = start.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			ZonedDateTime startOfFirstDayOfLastMonth = firstDayofMonth.minusMinutes(1)
					.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneIdUTC);
			startOfFirstDayOfLastMonth = startOfFirstDayOfLastMonth.plusSeconds(1);
			time = startOfFirstDayOfLastMonth.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)
				|| JobSchedule.TRI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			
			ZonedDateTime givenStartTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(startDate), zoneIdUTC);
			ZonedDateTime startOfTheGivenDate = givenStartTime.toLocalDate().atStartOfDay(zoneIdUTC).plusSeconds(1);
			time = startOfTheGivenDate.toInstant().getEpochSecond();
		} 
		else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			int crrentQuarterMonth = firstDayofMonth.getMonth().firstMonthOfQuarter().getValue();
			ZonedDateTime firstDayOfTheCurrentQuarter = Year.now().atMonth(crrentQuarterMonth).atDay(1).atStartOfDay()
					.atZone(zoneIdUTC);
			ZonedDateTime firstDayOfTheLastQuarter = firstDayOfTheCurrentQuarter.minusMonths(3).plusSeconds(1);
			time = firstDayOfTheLastQuarter.toInstant().getEpochSecond();

		} 
		else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate()
					.atStartOfDay(zoneIdUTC);
			firstDayofYear = firstDayofYear.minusYears(1).plusSeconds(1);
			time = firstDayofYear.toInstant().getEpochSecond();
		}
		return time;
	}

	
	

	public static Long getTodayTime() {
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);
		return now.toInstant().toEpochMilli();
	}

	public static ZonedDateTime getTodayTimeX() {
	return ZonedDateTime.now(zoneIdUTC);		
	}

	public static Long getTimeBeforeDaysInSeconds(Long days) {
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);
		ZonedDateTime after = now.minusDays(days);
		return after.toInstant().getEpochSecond();
	}

	public static Long getDurationInDays(Long inputEpocSeconds) {
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(inputEpocSeconds), zoneIdUTC);
		Duration d = Duration.between(fromInput, now);
		return d.toDays();
	}

	public static Long getDurationBetweenDates(long startTime, long endTime) {
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(startTime), zoneIdUTC);
		ZonedDateTime toInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(endTime), zoneIdUTC);
		Duration d = Duration.between(fromInput, toInput);
		return d.toDays();
	}

	public static long getScheduleWiseDuration(long startEpochInSeconds, long endEpochInSeconds, String schedule) {
		LocalDateTime startDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(getStartOfTheDay(startEpochInSeconds)),
				zoneIdUTC);
		LocalDateTime endDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(getStartOfTheDay(endEpochInSeconds)),
				zoneIdUTC);
		if (schedule.equalsIgnoreCase("WEEKLY") || schedule.equalsIgnoreCase("TRI_WEEKLY_SPRINT")
				|| schedule.equalsIgnoreCase("BI_WEEKLY_SPRINT")) {
			return ChronoUnit.WEEKS.between(startDate, endDate);
		} else if (schedule.equalsIgnoreCase("MONTHLY") || schedule.equalsIgnoreCase("QUARTERLY")) {
			return ChronoUnit.MONTHS.between(startDate, endDate);
		} else if (schedule.equalsIgnoreCase("YEARLY")) {
			return ChronoUnit.YEARS.between(startDate, endDate);
		}
		return 0l;
	}

	public static int getMonthDays(long time) {
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneIdUTC);
		return fromInput.toLocalDate().lengthOfMonth();
	}
	
	public static long getDaysInQuarter(long time) {
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneIdUTC)
				.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneIdUTC);
		int currentQuarterMonth = fromInput.getMonth().firstMonthOfQuarter().getValue();
		ZonedDateTime firstDayOfTheCurrentQuarter = Year.now().atMonth(currentQuarterMonth).atDay(1).atStartOfDay()
				.atZone(zoneIdUTC);
		ZonedDateTime firstDayOfTheNextQuarter = firstDayOfTheCurrentQuarter.plusMonths(3);
		Duration d = Duration.between(firstDayOfTheCurrentQuarter, firstDayOfTheNextQuarter);
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

	public static int getLengthOfYear(long time) {
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneIdUTC);
		return fromInput.toLocalDate().lengthOfYear();
	}

	public static Long subtractTimeInHours(long inputTime, long hours) {
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(inputTime), zoneIdUTC);
		fromInput = fromInput.minusHours(hours);
		return fromInput.toEpochSecond();
	}

	public static Long getCurrentTimeInSeconds() {
		ZonedDateTime now = ZonedDateTime.now(zoneIdUTC);
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
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(varianceTime), zoneIdUTC);
		return fromInput.toEpochSecond();
	}

	public static Long subtractVarianceTime(long inputTime, long varianceSeconds) {
		Long varianceTime = inputTime - varianceSeconds;
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(varianceTime), zoneIdUTC);
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
		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)&& days >= getCurrentYearDays()) {
			result = Boolean.TRUE;
		}
		return result;
	}

	public static String getLocalDateTime(String formatPattern) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatPattern);
		return dtf.format(now);
	}

	public static String getUtcTimeComponentStatus(String timezone) {
		SimpleDateFormat dtf = new SimpleDateFormat(DATE_TIME_FORMAT);
		dtf.setTimeZone(TimeZone.getTimeZone(timezone));
		return dtf.format(new Date());
	}

	public static String insightsTimeXFormat(long inputTime) {
		LocalDateTime utcTime=ZonedDateTime.ofInstant(Instant.ofEpochMilli(inputTime), zoneIdUTC).toLocalDateTime();
		return utcTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));			
	}
	
	public static String insightsTimeXFormatFromSeconds(long inputTime) {
		LocalDateTime utcTime=ZonedDateTime.ofInstant(Instant.ofEpochSecond(inputTime), zoneIdUTC).toLocalDateTime();
		return utcTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));			
	}

	/**
	 * 
	 * Calculates difference between currentTime and lastRunTime (now - lastRunTime)
	 * 
	 * @param lastRunTime
	 * @return
	 */
	public static long getDifferenceFromLastRunTime(long lastRunTime) {
		ZonedDateTime now = ZonedDateTime.now(InsightsUtils.zoneIdUTC);
		ZonedDateTime lastRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastRunTime),
				InsightsUtils.zoneIdUTC);
		Duration d = Duration.between(lastRunTimeInput, now);
		return d.abs().toDays();
	}
	
	/**
	 * 
	 * Calculates difference between currentTime and lastRunTime (now - lastRunTime)
	 * 
	 * @param lastRunTime
	 * @return
	 */
	public static long getDifferenceFromLastRunTimeInMinutes(long lastRunTimeInSeconds) {
		ZonedDateTime now = ZonedDateTime.now(InsightsUtils.zoneIdUTC);
		ZonedDateTime lastRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastRunTimeInSeconds),
				InsightsUtils.zoneIdUTC);
		Duration d = Duration.between(lastRunTimeInput, now);
		return d.abs().toMinutes();
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
				InsightsUtils.zoneIdUTC);
		ZonedDateTime nextRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime),
				InsightsUtils.zoneIdUTC);
		Duration d = Duration.between(lastRunTimeInput, nextRunTimeInput);
		return d.abs().toMillis();
	}

	public static long getEpochTime(String datetime) {

		return calculateEpochTime(datetime);
	}

	public static long getEpochTime(String datetime, String dateFormat) throws InsightsCustomException {

		return convertToEpochSeconds(datetime, dateFormat);
	}

	private static long calculateEpochTime(String datetime) {

		try {
			return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)).atZone(zoneIdUTC)
					.toInstant().toEpochMilli();
		} catch (Exception e) {
			log.error(e.getMessage());
			return 0;
		}
	}
	
	private static long convertToEpochSeconds(String datetime, String dateFormat) throws InsightsCustomException{
		try
		{
		return LocalDateTime.parse(datetime,DateTimeFormatter.ofPattern(dateFormat))
		        .atZone(zoneIdUTC).toInstant().getEpochSecond();
		}
		catch(Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());				
		}		
	}

	/** This method use in Traceability 
	 * @param milliseconds
	 * @return
	 */
	public static String getDateTimeFromEpoch(long milliseconds) {
		String duration;
		long days = TimeUnit.DAYS.convert(milliseconds, TimeUnit.MILLISECONDS);
		long minutes = TimeUnit.MINUTES.convert(milliseconds, TimeUnit.MILLISECONDS);
		long second = TimeUnit.SECONDS.convert(milliseconds, TimeUnit.MILLISECONDS);
		long yrs = 0;
		long month = 0;
		if(milliseconds ==0 ) {
			duration ="";
		}else if (days > 365) {
			yrs = days / 365;
			days = days - (365 * yrs);
			if (days >= 30) // Calculate month
			{
				month = days / 30;
				days = days - (month * 30);
			}
			duration = yrs + " Yrs " + month + "Month(s) " + days + DAYS;
		} else if (days >= 30 && days < 365) { // Calculate month
			month = days / 30;
			days = days - (month * 30);
			duration = month + "Month(s) " + days + DAYS;
		}else if (days <= 1  && minutes > 60) {
			long hour = TimeUnit.HOURS.convert(milliseconds, TimeUnit.MILLISECONDS);
			duration = hour +" minutes ";//less then 1 Day
		} else if(minutes < 60 && minutes > 1 ){
			duration = minutes +" minutes ";//less then 1 Day
		}else if (minutes < 1 && second > 0){
			duration = second +" second ";
		}else if(second <= 0) {
			duration =  " within a second ";
		}else{
			duration = days + DAYS;
		}
		return duration;
	}
	
	/**
	 * Converts Epoch date in seconds to required format.
	 * @param date
	 * @return 
	 */
	public static String epochToDateFormat(long date, String format) {
		LocalDateTime utcTime=ZonedDateTime.ofInstant(Instant.ofEpochSecond(date), zoneIdUTC).toLocalDateTime();
		return utcTime.format(DateTimeFormatter.ofPattern(format));
	}
}