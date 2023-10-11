/* Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.platformreports.assessment.core;


import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsJobFailedException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.offlineAlerting.InsightsOfflineAlerting;
import com.cognizant.devops.platformdal.offlineAlerting.InsightsOfflineAlertingDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonObject;

public class OfflineAlertSubscriber extends WorkflowTaskSubscriberHandler {
	private static Logger log = LogManager.getLogger(OfflineAlertSubscriber.class.getName());
	InsightsOfflineAlertingDAL offlineAlertingDAL = new InsightsOfflineAlertingDAL();
	InsightsOfflineAlerting alertConfig = new InsightsOfflineAlerting();
	WorkflowDAL workflowDAL = new WorkflowDAL();
	AlertEmailSubscriber alertEmailSubscriber = new AlertEmailSubscriber();
	InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	public static final int MAX_RETRY_COUNT = 2;

	private int executionId;
	private String workflowId;

	public OfflineAlertSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(String incomingTaskMessage) throws IOException {
		try {
			log.debug("Workflow Alert Detail ==== OfflineAlertSubscriber started ... "
					+ "routing key  message handleDelivery ===== {} ", incomingTaskMessage);

			JsonObject incomingTaskMessageJson = JsonUtils.parseStringAsJsonObject(incomingTaskMessage);
			executionId = incomingTaskMessageJson.get("executionId").getAsInt();
			workflowId = incomingTaskMessageJson.get("workflowId").getAsString();
			alertConfig = offlineAlertingDAL.getAlertConfigByWorkflowId(workflowId);
			JsonObject alertJson = JsonUtils.parseStringAsJsonObject(alertConfig.getAlertJson());
			int breachedCount = executeQueryAndGetBreachedCount(alertJson, alertConfig);
			int frequency = alertConfig.getFrequency();

			if (breachedCount >= frequency) {
				// create record in INSIGHTS_REPORT_VISUALIZATION_CONTAINER
				setDetailsInEmailHistory(incomingTaskMessageJson, alertJson);
				if (alertEmailSubscriber.alertEmailing(incomingTaskMessageJson)) {
					alertConfig.setBreachedCount(0);
					alertConfig.setStatus("Success");
					setStatusLog("Alert triggered.");
					log.debug(StringExpressionConstants.STR_EXP_ALERT_WORKFLOW, executionId, workflowId,
							alertConfig.getAlertName(), "-", "-", "-", "Email send successfully.");
				}
			} else {
				alertConfig.setStatus("Completed");
				setStatusLog("Task completed successfully and "+alertConfig.getBreachedCount() +"(breach count) < "+alertConfig.getFrequency()+"(frequency)");
			}
			offlineAlertingDAL.updateOfflineAlertingConfig(alertConfig);
		} catch (Exception e) {
			setStatusLog(e.getMessage());
			updateRetryCount(alertConfig);
			log.error(StringExpressionConstants.STR_EXP_ALERT_WORKFLOW, executionId, workflowId,
					alertConfig.getAlertName(), "-", "-", "-", e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());
		}
	}

	private int  executeQueryAndGetBreachedCount(JsonObject alertJson, InsightsOfflineAlerting alertConfig) {
		int result = 0;
		try {
			double queryResult = executeCypherQuery(alertJson.get("cypherQuery").getAsString(), alertJson.get("from").getAsString());
			String trend = alertJson.get("trend").getAsString();
			double threshold = alertJson.get("threshold").getAsDouble();
			int breachedCount = alertConfig.getBreachedCount();
			int count = 0;
			if (trend.equalsIgnoreCase("ABOVE") && (queryResult > threshold)) {
				++count;
			} else if (trend.equalsIgnoreCase("BELOW") && (queryResult < threshold)) {
				++count;
			} 
			alertConfig.setBreachedCount(breachedCount + count);
			result = alertConfig.getBreachedCount();
			log.debug(StringExpressionConstants.STR_EXP_ALERT_WORKFLOW, executionId, workflowId,
					alertConfig.getAlertName(), trend, threshold, alertConfig.getBreachedCount(),
					"Update the alert breached count.");
		} catch (Exception e) {
			log.error(StringExpressionConstants.STR_EXP_ALERT_WORKFLOW, executionId, workflowId,
					alertConfig.getAlertName(), "-", "-", "-", "Error while Updating the alert breached count.");
			throw new InsightsJobFailedException(e.getMessage());
		}
		return result;
	}

	private void setDetailsInEmailHistory(JsonObject incomingTaskMessageJson, JsonObject alertJson) {
		try {
			InsightsReportVisualizationContainer emailHistoryConfig = new InsightsReportVisualizationContainer();
			emailHistoryConfig.setExecutionId(
					incomingTaskMessageJson.get(AssessmentReportAndWorkflowConstants.EXECUTIONID).getAsLong());
			emailHistoryConfig.setStatus(WorkflowTaskEnum.EmailStatus.NOT_STARTED.name());
			emailHistoryConfig.setWorkflowConfig(incomingTaskMessageJson.get("workflowId").getAsString());
			emailHistoryConfig
					.setSubject(alertJson.get("emailDetails").getAsJsonObject().get("mailSubject").getAsString());
			emailHistoryConfig
					.setMailBody(alertJson.get("emailDetails").getAsJsonObject().get("mailBodyTemplate").getAsString());
			emailHistoryConfig.setMailAttachmentName("");
			workflowDAL.saveEmailExecutionHistory(emailHistoryConfig);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error setting Email details in Email History table");
			throw new InsightsJobFailedException(
					"Workflow Detail ==== Error setting Email details in Email History table");
		}
	}

	private double executeCypherQuery(String query, String timeRange) {
		double queryResult = 1;
		String startTime = getStartTime(timeRange).toString();
		String endTime = InsightsUtils.getCurrentTimeInSeconds().toString();
		try (GraphDBHandler dbHandler = new GraphDBHandler()) {
			if(query.toUpperCase().contains("?START_TIME?")) {
				query = query.replace("?START_TIME?", startTime);
			} 
			if(query.toUpperCase().contains("?END_TIME?")) {
				query = query.replace("?END_TIME?", endTime);
			}
			GraphResponse response = dbHandler.executeCypherQuery(query);
			queryResult = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsDouble();
			log.debug(StringExpressionConstants.STR_EXP_ALERT_WORKFLOW, executionId, workflowId,
					alertConfig.getAlertName(), "-", "-", "-", "Executed Alert Query, record count= " + queryResult);

		} catch (Exception e) {
			log.error(StringExpressionConstants.STR_EXP_ALERT_WORKFLOW, executionId, workflowId,
					alertConfig.getAlertName(), "-", "-", "-", e.getMessage());
			throw new InsightsJobFailedException("Error while executing query: "+ e.getMessage());
		}
		return queryResult;
	}

	private Long getStartTime(String timeRange) {
		Long time=0l;
		if(!timeRange.isBlank()) {
		String schedule = timeRange.replaceAll("\\d", "");
		String timeValue = timeRange.replaceAll("\\D", "");
		if(schedule.equalsIgnoreCase("hours")) {
			time = InsightsUtils.getEpochTimeInSecBasedOnSchedule("HOURLY", Long.parseLong(timeValue));
		} else if(schedule.equalsIgnoreCase("days")) {
			time = InsightsUtils.getEpochTimeInSecBasedOnSchedule("DAILY", Long.parseLong(timeValue));
		} else if(schedule.equalsIgnoreCase("months")) {
			time = InsightsUtils.getEpochTimeInSecBasedOnSchedule("MONTHLY", Long.parseLong(timeValue));
		} else if(schedule.equalsIgnoreCase("years")) {
			time = InsightsUtils.getEpochTimeInSecBasedOnSchedule("YEARLY", Long.parseLong(timeValue));
		}
		}
		return time;
	}
	
    private void updateRetryCount( InsightsOfflineAlerting alertConfig) {
    	int retryCount = alertConfig.getRetryCount();
    	alertConfig.setRetryCount(++retryCount);
    	alertConfig.setStatus("Failure");
    	offlineAlertingDAL.updateOfflineAlertingConfig(alertConfig);
		if (retryCount > MAX_RETRY_COUNT) {
			workflowConfig = workflowDAL.getWorkflowByWorkflowId(alertConfig.getWorkflowConfig().getWorkflowId());
			workflowConfig.setActive(false);
			workflowDAL.updateWorkflowConfig(workflowConfig);
		}
    }

}
