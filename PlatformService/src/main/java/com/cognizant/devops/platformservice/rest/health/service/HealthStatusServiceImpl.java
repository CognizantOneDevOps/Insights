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
package com.cognizant.devops.platformservice.rest.health.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.healthutil.HealthUtil;
import com.cognizant.devops.platformservice.rest.health.HealthStatusController;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@Service("healthStatusService")
public class HealthStatusServiceImpl  {
	static Logger log = LogManager.getLogger(HealthStatusServiceImpl.class);
	HealthUtil healthUtil = new HealthUtil();

	/**
	 * Method to get Health status
	 * 
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject getHealthStatus() throws InsightsCustomException {
		JsonObject servicesHealthStatus = new JsonObject();
		try {
			JsonObject dataComponentJson = healthUtil.getDataComponentStatus();
			JsonObject serviceJson = healthUtil.getServiceStatus();
			servicesHealthStatus = mergeJson(dataComponentJson, serviceJson);
		} catch (Exception e) {
			log.error("Error occured while fetching health status {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}

		log.debug(" servicesHealthStatus {}", servicesHealthStatus.toString());
		return servicesHealthStatus;
	}

	/**
	 * Method to fetch Agent Health status
	 * 
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject getAgentsHealthStatus() throws InsightsCustomException {
		JsonObject servicesAgentsHealthStatus = new JsonObject();
		try {
			JsonObject jsonAgentStatus = healthUtil.getAgentsStatus();
			servicesAgentsHealthStatus.add(ServiceStatusConstants.Agents, jsonAgentStatus);
		} catch (Exception e) {
			log.error("Error occured while fetching agent health status {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return servicesAgentsHealthStatus;
	}

	/**
	 * Method to fetch Detailed Health records
	 * 
	 * @param category
	 * @param tool
	 * @param agentId
	 * @return GraphResponse
	 * @throws InsightsCustomException
	 */
	public GraphResponse getDetailHealth(String category, String tool, String agentId) throws InsightsCustomException {
		try {
			final int MAX_RECORD = 10;
			log.debug(" message tool name {}  {}  {} ", category, tool, agentId);
			StringBuilder label = new StringBuilder("HEALTH");
			if (category.equalsIgnoreCase(ServiceStatusConstants.PlatformService)) {
				label.append(":").append("INSIGHTS_PLATFORMSERVICE");
			} else if (category.equalsIgnoreCase(ServiceStatusConstants.PlatformEngine)) {
				label.append(":").append("ENGINE");
			} 
			else if (category.equalsIgnoreCase(ServiceStatusConstants.PlatformWorkflow)) {
					label.append(":").append("INSIGHTS_WORKFLOW");
			} 
			else {
				label.append(":").append(category);
				label.append(":").append(tool);
			}
			return healthUtil.loadHealthData(label.toString(), agentId, MAX_RECORD);
		} catch (Exception e) {
			log.error("Error occured while fetching detail health status {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Method to create Agent Health Failure Label
	 * 
	 * @param category
	 * @param tool
	 * @param agentId
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject createAgentFailureHealthLabel(String category, String tool, String agentId)
			throws InsightsCustomException {
		try {
			log.debug(" message tool name {}  {} ", category, tool);
			StringBuilder label = new StringBuilder("HEALTH_FAILURE");
			if (StringUtils.isEmpty(category) || StringUtils.isEmpty(tool)) {
				return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
			} else {
				label.append(":").append(category);
				label.append(":").append(tool);
			}
			return loadAgentsFailureHealthData(label.toString(), agentId, 10);
		} catch (Exception e) {
			log.error("Error occured while creating agent failure health label {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Method to fetch Agent Failure Health data
	 * 
	 * @param nodeLabel
	 * @param agentId
	 * @param limitOfRow
	 * @return JsonObject
	 */
	private JsonObject loadAgentsFailureHealthData(String nodeLabel, String agentId, int limitOfRow) {
		String query = "";

		if (agentId.equalsIgnoreCase("")) {
			query = "MATCH (n:" + nodeLabel
					+ ") where n.inSightsTime IS NOT NULL RETURN n order by n.inSightsTime DESC LIMIT " + limitOfRow;
		} else if (!agentId.equalsIgnoreCase("")) {
			String queueName = healthUtil.getAgentHealthQueueName(agentId);
			// To handle case where Agent delete from Postgres but data present in Neo4j
			if (queueName == null) {
				queueName = nodeLabel;
			}
			queueName = queueName.replaceFirst("HEALTH", "HEALTH_FAILURE");
			query = "MATCH (n:" + queueName + ") where n.inSightsTime IS NOT NULL and n.agentId ='" + agentId
					+ "' RETURN n order by n.inSightsTime DESC LIMIT " + limitOfRow;
		}
		try {
			GraphDBHandler dbHandler = new GraphDBHandler();
			GraphResponse response = dbHandler.executeCypherQuery(query);
			return PlatformServiceUtil.buildSuccessResponseWithData(response);
		} catch (Exception e) {
			log.error("Error occured while loading agent failure health data {}", e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
	}

	/**
	 * Method to get Version Details
	 * 
	 * @param hostEndPoint
	 * @param type
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject getVersionDetails(String hostEndPoint, String type) throws InsightsCustomException {
		try {
			String version = "";
			String strResponse = "";
			if (version.equalsIgnoreCase("")) {
				version = HealthStatusController.class.getPackage().getSpecificationVersion();
				strResponse = "Version captured as " + version;
				return healthUtil.buildSuccessResponse(strResponse, hostEndPoint, type, version);
			} else {
				strResponse = "Error while capturing PlatformService (version " + version + ") health check at "
						+ hostEndPoint;
				return healthUtil.buildFailureResponse(strResponse, hostEndPoint, type, version);
			}
		} catch (Exception e) {
			log.error("Error occured while fetching version details {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}
	
	/**
	 * Method to merge JsonObjects
	 * 
	 * @param dataComponentJson
	 * @param serviceJson
	 * @return JsonObject
	 */
	private JsonObject mergeJson(JsonObject dataComponentJson,JsonObject serviceJson) {
		JsonObject mergedJson = new JsonObject();
		for(String component:dataComponentJson.keySet()) {
			mergedJson.add(component, dataComponentJson.get(component).getAsJsonObject());
		}
		for(String service:serviceJson.keySet()) {
			mergedJson.add(service, serviceJson.get(service).getAsJsonObject());
		}
		return mergedJson;
	}

}
