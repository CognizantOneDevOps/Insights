/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.healthutil.HealthUtil;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.core.InsightsStatusProvider;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SystemNotificationDetailSubscriber extends WorkflowTaskSubscriberHandler {
	private static Logger log = LogManager.getLogger(SystemNotificationDetailSubscriber.class.getName());
	HealthUtil healthUtil = new HealthUtil();
	private WorkflowDAL workflowDAL = new WorkflowDAL();

	public SystemNotificationDetailSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		try {
			String incomingTaskMessage = new String(body, StandardCharsets.UTF_8);
			JsonObject incomingTaskMessageJson = new JsonParser().parse(incomingTaskMessage).getAsJsonObject();
			String htmlDataComponentResponseBuffer = componentHTML(healthUtil.getDataComponentStatus(), "Data Component");
			String htmlServiceResponseBuffer = componentHTML(healthUtil.getServiceStatus(), "Services");
			String htmlAgentResponseBuffer = getAgentHTML();
			Map<String,String> idDataMap = new LinkedHashMap<>();
			idDataMap.put("table_agent", htmlAgentResponseBuffer);
			idDataMap.put("table_services", htmlServiceResponseBuffer);
			idDataMap.put("table_data_components", htmlDataComponentResponseBuffer);
			String mailHTML = createEmailHTML(idDataMap);
			Map<String, String> valuesMap = new HashMap<>();
			valuesMap.put("date", InsightsUtils.specficTimeFormat(incomingTaskMessageJson.get("executionId").getAsLong(), "yyyy-MM-dd"));
			StringSubstitutor sub = new StringSubstitutor(valuesMap, "{", "}");
			mailHTML=sub.replace(mailHTML);
			setDetailsInEmailHistory(incomingTaskMessageJson, mailHTML);
			InsightsStatusProvider.getInstance().createInsightStatusNode("SystemNotificationDetailSubscriber completed",
					PlatformServiceConstants.SUCCESS);
		} catch (InsightsJobFailedException ijfe) {
			log.error("Worlflow Detail ==== SystemNotificationDetail Subscriber Completed with error ", ijfe);
			InsightsStatusProvider.getInstance().createInsightStatusNode(
					"SystemNotificationDetail Completed with error " + ijfe.getMessage(),
					PlatformServiceConstants.FAILURE);
			statusLog = ijfe.getMessage();
			throw ijfe;
		} catch (Exception e) {
			log.error("Worlflow Detail ==== SystemNotificationDetail Subscriber Completed with error ", e);
			InsightsStatusProvider.getInstance().createInsightStatusNode(
					"SystemNotificationDetail Completed with error " + e.getMessage(),
					PlatformServiceConstants.FAILURE);
			throw new InsightsJobFailedException(e.getMessage());
		}

	}
	
	/**
	 * Method to create Email HTML
	 * 
	 * @param idDataMap
	 * @return String
	 */
	private String createEmailHTML(Map<String,String> idDataMap) {
		try {
			
			InputStream templateStream = getClass().getClassLoader().getResourceAsStream("mailTemplate.html");
			BufferedReader r = new BufferedReader(new InputStreamReader(templateStream));
			String line;
			StringBuffer render =new StringBuffer();
			while ((line = r.readLine()) != null) {
				render.append(line);
			}
			r.close();
			Document document = Jsoup.parse(render.toString(), StandardCharsets.UTF_8.name());
			return createComponentTable(idDataMap,document);
			
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error creating HTML body for email");
			throw new InsightsJobFailedException("Worlflow Detail ==== Error creating HTML body for email");
		}

	}
	
	/**
	 * Method to append HTML data in Document
	 * 
	 * @param idDataMap
	 * @param document
	 * @return String
	 */
	private String createComponentTable(Map<String,String> idDataMap,Document document) {
		for(Map.Entry<String, String> entry : idDataMap.entrySet()) {
		Elements allTableDiv = document.getElementsByAttributeValueMatching("id", entry.getKey());
		if (!allTableDiv.isEmpty()) {
			allTableDiv.append(entry.getValue());
		}
		}
		return document.toString();
		
	}




	/**
	 * Method to fetch Agent HTML
	 * 
	 * @return String
	 */
	private String getAgentHTML() {
		try {
			JsonObject jsonAgentResponse = healthUtil.getAgentsStatus();
			return agentHTML(jsonAgentResponse);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error creating HTML body for agents");
			throw new InsightsJobFailedException("Worlflow Detail ==== Error creating HTML body for agents");
		}
	}

	/**
	 * Method to create HTML table for a component
	 * 
	 * @param componentResponseJson
	 * @param caption
	 * @return String
	 */
	private String componentHTML(JsonObject componentResponseJson, String caption) {
		String[] columns = { "Name","Version","Status" };
		StringBuffer tableList = new StringBuffer();
		tableList.append("<h2>").append(caption).append("</h2>");
		tableList.append("<table>");
		tableList.append("<thead>");
		tableList.append("<tr>");
		for (String column : columns) {
			tableList.append("<th>" + column + "</th>");
		}
		tableList.append("</tr>");
		tableList.append("</thead>");
		tableList.append("<tbody>");
		for (String component : componentResponseJson.keySet()) {
			tableList.append("<tr>");
			tableList.append("<td>").append(component).append("</td>");
			tableList.append("<td>");
			if(!componentResponseJson.get(component).getAsJsonObject().get("version").isJsonNull()) {
				tableList.append(componentResponseJson.get(component).getAsJsonObject().get("version").getAsString());
			}else {
				tableList.append("-");
			}
			tableList.append("</td>");
			tableList.append("<td>")
					.append(formatStatus(componentResponseJson.get(component).getAsJsonObject().get("status").getAsString()))
					.append("</td>");
			tableList.append("</tr>");
		}
		tableList.append("</tbody>");
		tableList.append("</table>");
		return tableList.toString();
	}

	/**
	 * Method to create HTML table for Agent
	 * 
	 * @param agentResponseJson
	 * @return String
	 */
	private String agentHTML(JsonObject agentResponseJson) {
		String[] columns = { "Name", "Agent Id","Last Reporting Time", "Status" };
		StringBuffer tableList = new StringBuffer();
		tableList.append("<h2> Agents </h2>");
		tableList.append("<table>");
		tableList.append("<thead>");
		tableList.append("<tr>");
		for (String column : columns) {
			tableList.append("<th>" + column + "</th>");
		}
		tableList.append("</tr>");
		tableList.append("</thead>");
		tableList.append("<tbody>");
		JsonArray agentDetails = agentResponseJson.get("agentNodes").getAsJsonArray();
		for (JsonElement agentJson : agentDetails) {
			tableList.append("<tr>");
			tableList.append("<td>").append(agentJson.getAsJsonObject().get("toolName").getAsString()).append("</td>");
			tableList.append("<td>").append(agentJson.getAsJsonObject().get("agentId").getAsString()).append("</td>");
			long time = InsightsUtils.getEpochTime(agentJson.getAsJsonObject().get("inSightsTimeX").getAsString());
			tableList.append("<td>").append(InsightsUtils.specficTimeFormat(time, "yyyy-MM-dd HH:mm:ss")).append("</td>");
			tableList.append("<td>").append(formatStatus(agentJson.getAsJsonObject().get("status").getAsString())).append("</td>");
			tableList.append("</tr>");
		}
		tableList.append("</tbody>");
		tableList.append("</table>");
		return tableList.toString();
	}

	/**
	 * Method to save Email details in Email History table
	 * 
	 * @param incomingTaskMessageJson
	 * @param mailBody
	 */
	private void setDetailsInEmailHistory(JsonObject incomingTaskMessageJson, String mailBody) {
		try {
			InsightsReportVisualizationContainer emailHistoryConfig = new InsightsReportVisualizationContainer();
			emailHistoryConfig.setExecutionId(incomingTaskMessageJson.get("executionId").getAsLong());
			emailHistoryConfig.setStatus(WorkflowTaskEnum.EmailStatus.NOT_STARTED.name());
			emailHistoryConfig.setWorkflowConfig(incomingTaskMessageJson.get("workflowId").getAsString());
			emailHistoryConfig.setMailBody(mailBody);
			emailHistoryConfig.setMailAttachmentName("");
			workflowDAL.saveEmailExecutionHistory(emailHistoryConfig);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error setting Email details in Email History table");
			throw new InsightsJobFailedException(
					"Worlflow Detail ==== Error setting Email details in Email History table");
		}
	}
	
	/**
	 * Method to format status
	 * 
	 * @param status
	 * @return String
	 */
	private String formatStatus(String status) {
		if(status.toLowerCase().equalsIgnoreCase(PlatformServiceConstants.SUCCESS))
			return "Success";
		else
			return "Failure";
	}

}
