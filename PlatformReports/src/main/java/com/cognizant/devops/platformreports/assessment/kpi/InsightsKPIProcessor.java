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
package com.cognizant.devops.platformreports.assessment.kpi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformreports.assessment.dal.ReportDataHandler;
import com.cognizant.devops.platformreports.assessment.dal.ReportDataHandlerFactory;
import com.cognizant.devops.platformreports.assessment.dal.ReportGraphDataHandler;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.cognizant.devops.platformreports.assessment.datamodel.QueryModel;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum.ContentCategory;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsKPIProcessor {

	private static Logger log = LogManager.getLogger(InsightsKPIProcessor.class);

	protected InsightsKPIConfigDTO kpiConfigDTO;

	protected ReportGraphDataHandler reportGraphDBHandler = new ReportGraphDataHandler();

	Gson gson = new Gson();
	JsonParser jsonParser = new JsonParser();

	public InsightsKPIProcessor(InsightsKPIConfigDTO kpiConfigDTO) {
		this.kpiConfigDTO = kpiConfigDTO;
	}

	protected int processKPI(InsightsKPIConfigDTO kpiDefinition) {

		try {

			ReportDataHandler kPIQueryDataHandler = ReportDataHandlerFactory
					.getDataSource(kpiDefinition.getDatasource());

			String graphQuery = kpiDefinition.getdBQuery();
			List<JsonObject> listOfResultJson = new ArrayList<>(0);

			List<QueryModel> queryModelList = new ArrayList<>();

			long startDate;
			long endDate;
			int noOfDays;

			if (kpiDefinition.getSchedule().equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME)) {
				startDate = kpiDefinition.getOneTimeReportStartTime();
				endDate = kpiDefinition.getOneTimeReportEndDate();
				noOfDays = InsightsUtils.getDurationBetweenDates(startDate, endDate).intValue();
			} else {
				startDate = kpiDefinition.getNextRunTime();
				endDate = startDate;// kpiDefinition.getOneTimeReportEndDate(); /// end date to nextRunTime
				noOfDays = kpiDefinition.getSchedule().getValue();
			}

			if (kpiDefinition.getCategory().equals(ContentCategory.THRESHOLD.name())
					|| kpiDefinition.getCategory().equals(ContentCategory.THRESHOLD_RANGE.name())
					|| kpiDefinition.getCategory().equals(ContentCategory.MINMAX.name())
					|| kpiDefinition.getCategory().equals(ContentCategory.TREND.name())) {
				getQueryBySchedule(startDate, kpiDefinition.getSchedule(), noOfDays, graphQuery, endDate,
						queryModelList);
			} else {
				getQueryWithScheduleDates(startDate, kpiDefinition.getSchedule(), graphQuery, endDate,
						queryModelList);
			}

			log.debug("Worlflow Detail ====  In processKPI for kpiId {} category {} queryModelList {}",
					kpiDefinition.getKpiId(), kpiDefinition.getCategory(), queryModelList.size());

			for (QueryModel model : queryModelList) {
				listOfResultJson.addAll(kPIQueryDataHandler.fetchKPIData(model.getQuery(), kpiDefinition, model));
			}

			ReportDataHandler kPIResultDataHandler = ReportDataHandlerFactory
					.getDataSource(ApplicationConfigProvider.getInstance().getAssessmentReport().getOutputDatasource());

			log.debug("Worlflow Detail ====  Number of record fetch against kpi Id {} is {}", kpiDefinition.getKpiId(),
					listOfResultJson.size());
			if (!listOfResultJson.isEmpty()) {
				kPIResultDataHandler.saveData(listOfResultJson);
			} else {
				log.error("Worlflow Detail ====  No result to store in database(neo4j or ES) for job : {} ",
						kpiConfigDTO.getKpiId());
				return ReportEngineEnum.StatusCode.NO_DATA.getValue();
			}
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Some calculation job failed for kpiID - " + kpiDefinition.getKpiId(), e);
			throw new InsightsJobFailedException("Something went wrong with KPI query execution " + e.getMessage());
		}
		return ReportEngineEnum.StatusCode.SUCCESS.getValue();
	}

	protected void getQueryWithScheduleDates(long nextRuntime, WorkflowTaskEnum.WorkflowSchedule schedule,
			String neo4jQuery, long oneTimeEndDate, List<QueryModel> queryModelList) {
		QueryModel queryModel = new QueryModel();
		Map<String, Long> dateReplaceMap = new HashMap<>();
		long fromDate = InsightsUtils.getStartFromTime(nextRuntime, schedule.name()) - 1;
		long toDate;
		if (schedule.name().equalsIgnoreCase(WorkflowTaskEnum.WorkflowSchedule.ONETIME.name())) {
			toDate = oneTimeEndDate;
		} else {
			toDate = nextRuntime - 2; // minus 2 sec to get time 23:59:59
		}
		dateReplaceMap.put(ReportEngineUtils.START_TIME_FIELD, fromDate);
		dateReplaceMap.put(ReportEngineUtils.END_TIME_FIELD, toDate);
		StringSubstitutor sub = new StringSubstitutor(dateReplaceMap, "{", "}");
		queryModel.setRecordDate(fromDate);
		queryModel.setQuery(sub.replace(neo4jQuery));
		queryModelList.add(queryModel);

	}

	private void getQueryBySchedule(long nextRunTime, WorkflowTaskEnum.WorkflowSchedule schedule, int days,
			String query, long endTime, List<QueryModel> queryModelList) {

		Map<String, Long> dateReplaceMap = new HashMap<>();
		List<String> neo4jQueries = new ArrayList<>();
		if (days <= 31) {
			int noOfQueries = days;
			long startDate = InsightsUtils.getStartFromTime(nextRunTime, schedule.name()) - 1;
			long endDate = InsightsUtils.addDaysInGivenTime(startDate, 1) - 1;
			if (schedule.name().equalsIgnoreCase("Monthly")) {
				noOfQueries = InsightsUtils.getMonthDays(startDate);
			}
			for (int i = 0; i < noOfQueries; i++) {

				QueryModel qmodel = new QueryModel();
				dateReplaceMap.put(ReportEngineUtils.START_TIME_FIELD, startDate);
				dateReplaceMap.put(ReportEngineUtils.END_TIME_FIELD, endDate);
				StringSubstitutor sub = new StringSubstitutor(dateReplaceMap, "{", "}");
				qmodel.setRecordDate(startDate);
				qmodel.setQuery(sub.replace(query));
				queryModelList.add(qmodel);
				startDate = InsightsUtils.addDaysInGivenTime(startDate, 1);
				endDate = InsightsUtils.addDaysInGivenTime(endDate, 1);

			}
		} else {

			int weeks = days / 7;
			long startOfTheDayInWeek = InsightsUtils.getStartFromTime(nextRunTime, schedule.name()) - 1;
			long endDayOfTheWeek = InsightsUtils.addDaysInGivenTime(startOfTheDayInWeek, 7) - 1;
			for (int i = 0; i < weeks; i++) {
				QueryModel qmodel = new QueryModel();
				dateReplaceMap.put(ReportEngineUtils.START_TIME_FIELD, startOfTheDayInWeek);
				dateReplaceMap.put(ReportEngineUtils.END_TIME_FIELD, endDayOfTheWeek);
				StringSubstitutor sub = new StringSubstitutor(dateReplaceMap, "{", "}");
				neo4jQueries.add(sub.replace(query));
				qmodel.setRecordDate(startOfTheDayInWeek);
				qmodel.setQuery(sub.replace(query));
				queryModelList.add(qmodel);
				startOfTheDayInWeek = InsightsUtils.addDaysInGivenTime(startOfTheDayInWeek, 7);
				endDayOfTheWeek = InsightsUtils.addDaysInGivenTime(endDayOfTheWeek, 7);
				if (endDayOfTheWeek > endTime) {
					endDayOfTheWeek = endTime;
				}

			}
		}
	}
}
