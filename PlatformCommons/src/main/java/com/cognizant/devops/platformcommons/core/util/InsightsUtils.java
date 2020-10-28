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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;

public class InsightsUtils {

	static Logger log = LogManager.getLogger(InsightsUtils.class.getName());

	private InsightsUtils() {
	}

	private static String sparkTimezone = ApplicationConfigProvider.getInstance().getInsightsTimeZone();
	public static ZoneId zoneId = ZoneId.of(sparkTimezone);
	public static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	/**
	 * this function work on start time and it will give sprint end time as per
	 * sprint time line *
	 * 
	 * @param schedule
	 * @param startTime
	 * @return time in milliseconds
	 */

	public static long getWeekendDateFromStartTime(long startTime) {
		int i = LocalDate.now().get(IsoFields.QUARTER_OF_YEAR);
		ZonedDateTime startDateZone = ZonedDateTime.ofInstant(Instant.ofEpochSecond(startTime), InsightsUtils.zoneId);
		ZonedDateTime startDateOfTheWeek = startDateZone.toLocalDate().atStartOfDay(zoneId);
		ZonedDateTime endDateTimeOfWeek = startDateOfTheWeek.plusDays(7).minusSeconds(1);
		long time = endDateTimeOfWeek.toInstant().toEpochMilli();
		return time;
	}

	// replace from with end
	public static long getSprintDataEndTime(String schedule, long startTime) {
		Long sprintEndTime = null;
		ZonedDateTime startDateZone = ZonedDateTime.ofInstant(Instant.ofEpochSecond(startTime), InsightsUtils.zoneId);
		ZonedDateTime startOfTheDay = startDateZone.toLocalDate().atStartOfDay(zoneId);
		if (WorkflowTaskEnum.WorkflowSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			sprintEndTime = startOfTheDay.plusDays(13).toInstant().getEpochSecond();
		} else if (WorkflowTaskEnum.WorkflowSchedule.TRI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			sprintEndTime = startOfTheDay.plusDays(20).toInstant().getEpochSecond();
		}
		return sprintEndTime;
	}

	public static long getDurationBetweenTime(long time)
	{
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return now.toInstant().getEpochSecond()-time;
	}

	public static String specficTimeFormat(long inputTime, String timeformat) {
		SimpleDateFormat format = new SimpleDateFormat(timeformat);
		int length = String.valueOf(inputTime).length();
		if (length == 10) {
			inputTime = TimeUnit.SECONDS.toMillis(inputTime);
		}
		Date date = new Date(inputTime);
		String formatted = format.format(date);
		return formatted;
	}

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
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			ZonedDateTime lastDayofLastMonth = firstDayofMonth.minusMinutes(1);
			ZonedDateTime firstDayOfTheLastQuarter = lastDayofLastMonth.minusMonths(3);
			firstDayOfTheLastQuarter = firstDayOfTheLastQuarter.plusSeconds(1);
			time = firstDayOfTheLastQuarter.toInstant().toEpochMilli();
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

	public static long getStartOfTheDay(long startTime) {
		ZonedDateTime startDateZone = ZonedDateTime.ofInstant(Instant.ofEpochSecond(startTime), InsightsUtils.zoneId);
		ZonedDateTime startDateOfTheWeek = startDateZone.toLocalDate().atStartOfDay(zoneId);
		return startDateOfTheWeek.toInstant().getEpochSecond();
	}

	public static long getInitialLastRunTime(String schedule) {

		Long time = null;
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.toLocalDate().atStartOfDay(zoneId);
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
			firstDayofLastMonth = firstDayofLastMonth.minusSeconds(1);
			time = firstDayofLastMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {

			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			ZonedDateTime lastDayofLastMonth = firstDayofMonth.minusMinutes(1);
			ZonedDateTime firstDayOfTheLastQuarter = lastDayofLastMonth.minusMonths(3);
			firstDayOfTheLastQuarter = firstDayOfTheLastQuarter.plusSeconds(1);
			time = firstDayOfTheLastQuarter.toInstant().toEpochMilli();

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
		} else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);

			todayStart = todayStart.minusWeeks(2);
		}

		else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
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
	public static long addTimeInCurrentTime(long currentTimeInSeconds, String frequency, long value)
	{
		long time = 0l;
		ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentTimeInSeconds), zoneId);
		if (frequency.equalsIgnoreCase("DAYS")) {

			time = currentTime.plusDays(value).toEpochSecond();
		}
		else if (frequency.equalsIgnoreCase("WEEKS")) {

			time = currentTime.plusWeeks(value).toEpochSecond();
		}
		else if (frequency.equalsIgnoreCase("MONTHS")) {

			time = currentTime.plusMonths(value).toEpochSecond();
		}
		else if (frequency.equalsIgnoreCase("YEARS")) {

			time = currentTime.plusYears(value).toEpochSecond();
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
	public static long addDaysInGivenTime(long time, long days) {
		ZonedDateTime givenTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneId);
		ZonedDateTime calculatedTime = givenTime.plusDays(days);
		return calculatedTime.toInstant().getEpochSecond();
	}

	public static long getStartFromTime(long nextRunTime, String schedule) {
		long time = 0;
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneId);
			ZonedDateTime startTime = nextTime.minusDays(1);
			time = startTime.toInstant().getEpochSecond();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneId);
			ZonedDateTime startTime = nextTime.minusDays(7);
			time = startTime.toInstant().getEpochSecond();

		} else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {

			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneId);
			ZonedDateTime startTime = nextTime.minusDays(14);
			time = startTime.toInstant().getEpochSecond();
		} else if (JobSchedule.TRI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {

			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneId);
			ZonedDateTime startTime = nextTime.minusDays(21);
			time = startTime.toInstant().getEpochSecond();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneId);
			ZonedDateTime startTime = nextTime.minusMonths(1);
			time = startTime.toInstant().getEpochSecond();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneId);
			ZonedDateTime startTime = nextTime.minusMonths(3);
			time = startTime.toInstant().getEpochSecond();

		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime nextTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneId);
			ZonedDateTime startTime = nextTime.minusYears(1);
			time = startTime.toInstant().getEpochSecond();
		}
		else if(JobSchedule.ONETIME.name().equalsIgnoreCase(schedule))
		{
			ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), zoneId);
		//	startTime=startTime.toLocalDate().atStartOfDay().atZone(zoneId);
			time=startTime.toInstant().getEpochSecond();
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
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneId);
			ZonedDateTime nextRunTime = currentTime.plusDays(1);
			time = nextRunTime.toInstant().getEpochSecond();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneId);
			ZonedDateTime nextRunTime = currentTime.plusDays(7);
			time = nextRunTime.toInstant().getEpochSecond();

		} else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {

			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneId);
			ZonedDateTime nextRunTime = currentTime.plusDays(14);
			time = nextRunTime.toInstant().getEpochSecond();
		} else if (JobSchedule.TRI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {

			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneId);
			ZonedDateTime nextRunTime = currentTime.plusDays(21);
			time = nextRunTime.toInstant().getEpochSecond();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneId);
			ZonedDateTime nextRunTime = currentTime.plusMonths(1);
			time = nextRunTime.toInstant().getEpochSecond();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {

			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneId);
			ZonedDateTime nextRunTime = currentTime.plusMonths(3);
			time = nextRunTime.toInstant().getEpochSecond();

		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime currentTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(currentNextTime), zoneId);
			ZonedDateTime nextRunTime = currentTime.plusYears(1);
			time = nextRunTime.toInstant().getEpochSecond();

		}
		return time;
	}

	public static long getStartDay(long startDate, String schedule) {

		Long time = null;

		ZonedDateTime now = ZonedDateTime.now(zoneId);

		if (JobSchedule.DAILY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime start = now.toLocalDate().atStartOfDay(zoneId).plusSeconds(1);
			time = start.toInstant().getEpochSecond();
		} else if (JobSchedule.WEEKLY.name().equalsIgnoreCase(schedule)) {

			ZonedDateTime start = now.minusDays(7).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			start = start.toLocalDate().atStartOfDay(zoneId);
			start = start.plusSeconds(1);
			time = start.toInstant().getEpochSecond();
		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneId);			
			ZonedDateTime startOfFirstDayOfLastMonth = firstDayofMonth.minusMinutes(1).with(TemporalAdjusters.firstDayOfMonth())
					.toLocalDate().atStartOfDay(zoneId);
			startOfFirstDayOfLastMonth = startOfFirstDayOfLastMonth.plusSeconds(1);
			time = startOfFirstDayOfLastMonth.toInstant().getEpochSecond();
		} else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)
				|| JobSchedule.TRI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {

			ZonedDateTime givenStartTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(startDate), zoneId);
			ZonedDateTime startOfTheGivenDate = givenStartTime.toLocalDate().atStartOfDay(zoneId).plusSeconds(1);
			time = startOfTheGivenDate.toInstant().getEpochSecond();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			int crrentQuarterMonth = firstDayofMonth.getMonth().firstMonthOfQuarter().getValue();
			ZonedDateTime firstDayOfTheCurrentQuarter = Year.now().atMonth(crrentQuarterMonth).atDay(1)
					.atStartOfDay().atZone(zoneId);
			ZonedDateTime firstDayOfTheLastQuarter = firstDayOfTheCurrentQuarter.minusMonths(3).plusSeconds(1);
			time = firstDayOfTheLastQuarter.toInstant().getEpochSecond();

		} else if (JobSchedule.YEARLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate()
					.atStartOfDay(zoneId);
			firstDayofYear = firstDayofYear.minusYears(1).plusSeconds(1);
			time = firstDayofYear.toInstant().getEpochSecond();
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
		} else if (JobSchedule.BI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			todayStart = todayStart.plusMinutes(1);
			ZonedDateTime nextweek = todayStart.plusWeeks(2);
			time = nextweek.toInstant().toEpochMilli();
		} else if (JobSchedule.TRI_WEEKLY_SPRINT.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId)
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
			ZonedDateTime lastWeek = todayStart.minusSeconds(3);
			time = lastWeek.toInstant().toEpochMilli();

		} else if (JobSchedule.MONTHLY.name().equalsIgnoreCase(schedule)) {
			ZonedDateTime firstDayofNextMonth = now.with(TemporalAdjusters.firstDayOfNextMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			firstDayofNextMonth = firstDayofNextMonth.plusMinutes(1);
			time = firstDayofNextMonth.toInstant().toEpochMilli();
		} else if (JobSchedule.QUARTERLY.name().equalsIgnoreCase(schedule)) {
			// Will add code later for this usecase
			// zdt = zdt.plusMonths(3);
			ZonedDateTime firstDayofNextMonth = now.with(TemporalAdjusters.firstDayOfNextMonth()).toLocalDate()
					.atStartOfDay(zoneId);
			ZonedDateTime lastDayofLastMonth = firstDayofNextMonth.minusMinutes(1);
			ZonedDateTime lastDayOfNextQuarter = lastDayofLastMonth.plusMonths(3);
			time = lastDayOfNextQuarter.toInstant().toEpochMilli();
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
	
	public static Long getDurationInDays(Long inputEpocSeconds) {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(inputEpocSeconds), zoneId);
		Duration d = Duration.between(fromInput, now);
		return d.toDays();
	}
		
	public static Long getDurationBetweenDates(long startTime, long endTime) {		
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(startTime), zoneId);
		ZonedDateTime toInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(endTime), zoneId);
		Duration d = Duration.between(fromInput, toInput);
		return d.toDays();
	}
	public static long getScheduleWiseDuration(long startEpochInSeconds,long endEpochInSeconds,String schedule) {
		LocalDateTime startDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(getStartOfTheDay(startEpochInSeconds)),
				zoneId);
		LocalDateTime endDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(getStartOfTheDay(endEpochInSeconds)),
				zoneId);
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
	public static Long getDurationBetweenDatesInDaysByRunTimeInSec(Long inputEpochMillis) {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(inputEpochMillis), zoneId);
		Duration d = Duration.between(fromInput, now);
		return d.toDays();
	}

	public static int getMonthDays(long time) {
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneId);
		return fromInput.toLocalDate().lengthOfMonth();
	}
	public static long getTimeInCurrentTimezone(long time)
	{
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneId);		
		return fromInput.toEpochSecond();
	}
	
	public static long getDaysInQuarter(long time)
	{
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneId).with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
				.atStartOfDay(zoneId);
		int currentQuarterMonth = fromInput.getMonth().firstMonthOfQuarter().getValue();
		ZonedDateTime firstDayOfTheCurrentQuarter = Year.now().atMonth(currentQuarterMonth).atDay(1)
						.atStartOfDay().atZone(zoneId);
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
		ZonedDateTime fromInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneId);
		return fromInput.toLocalDate().lengthOfYear();
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
		SimpleDateFormat dtf = new SimpleDateFormat(DATE_TIME_FORMAT);
		dtf.setTimeZone(TimeZone.getTimeZone(timezone));
		return dtf.format(new Date());
	}

	public static String insightsTimeXFormat(long inputTime) {
		SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
		int length = String.valueOf(inputTime).length();
		if (length == 10) {
			inputTime = TimeUnit.SECONDS.toMillis(inputTime);
		}
		Date date = new Date(inputTime);
		String formatted = format.format(date);
		return formatted;
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

		return calculateEpochTime(datetime);
	}

	public static long getEpochTime(String datetime, String dateFormat) throws InsightsCustomException {

		return convertToEpoch(datetime, dateFormat);
	}

	private static long calculateEpochTime(String datetime) {
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

	private static long convertToEpoch(String datetime, String dateFormat) throws InsightsCustomException {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Date dt = sdf.parse(datetime);
			return TimeUnit.MILLISECONDS.toSeconds(dt.getTime());
		} catch (ParseException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
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

		} else if (days <= 1) {
			duration = "1 Day ";
		} else {
			duration = days + " Days ";
		}
		return duration;
	}
}
