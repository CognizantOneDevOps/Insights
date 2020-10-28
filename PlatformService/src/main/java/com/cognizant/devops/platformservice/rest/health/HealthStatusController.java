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
package com.cognizant.devops.platformservice.rest.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformservice.rest.health.service.HealthStatusService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/health")
public class HealthStatusController {

	@Autowired
	HealthStatusService healthStatusService;

	static Logger log = LogManager.getLogger(HealthStatusController.class);
	private static final String PLATFORM_SERVICE_VERSION_FILE = "version.properties";

	@RequestMapping(value = "/globalHealth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getHealthStatus() {
		JsonObject servicesHealthStatus = new JsonObject();
		String username = null;
		String password = null;
		String authToken = null;
		try {
			String hostEndPoint = "";
			String apiUrl = "";
			hostEndPoint = ServiceStatusConstants.POSTGRESQL_HOST;
			apiUrl = hostEndPoint;
			JsonObject postgreStatus = healthStatusService.getClientResponse(hostEndPoint, apiUrl,
					ServiceStatusConstants.DB, ServiceStatusConstants.PgSQL, Boolean.FALSE, username, password,
					authToken);
			servicesHealthStatus.add(ServiceStatusConstants.PgSQL, postgreStatus);
			log.debug("After Postgres================");
			/* PlatformService health check */
			hostEndPoint = ServiceStatusConstants.PLATFORM_SERVICE_HOST;
			JsonObject platformServStatus = healthStatusService.getVersionDetails(PLATFORM_SERVICE_VERSION_FILE,
					hostEndPoint, ServiceStatusConstants.Service);
			servicesHealthStatus.add(ServiceStatusConstants.PlatformService, platformServStatus);
			log.debug("After Platform Service================");
			/* Neo4j health check */
			hostEndPoint = ServiceStatusConstants.NEO4J_HOST;
			apiUrl = hostEndPoint + "/db/data/";
			authToken = ApplicationConfigProvider.getInstance().getGraph().getAuthToken();
			JsonObject neo4jStatus = healthStatusService.getClientResponse(hostEndPoint, apiUrl,
					ServiceStatusConstants.DB, ServiceStatusConstants.Neo4j, Boolean.TRUE, username, password,
					authToken);
			servicesHealthStatus.add(ServiceStatusConstants.Neo4j, neo4jStatus);
			log.debug("After Neo4j================");
			/* Elastic Search health check */
			hostEndPoint = ServiceStatusConstants.ES_HOST;
			apiUrl = hostEndPoint;
			JsonObject EsStatus = healthStatusService.getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
					ServiceStatusConstants.ES, Boolean.FALSE, username, password, authToken);
			servicesHealthStatus.add(ServiceStatusConstants.ES, EsStatus);
			log.debug("After ElasticSearch================");
			/* Rabbit Mq health check */
			hostEndPoint = ServiceStatusConstants.RABBIT_MQ;
			apiUrl = hostEndPoint + "/api/overview";
			authToken = null;
			username = ApplicationConfigProvider.getInstance().getMessageQueue().getUser();
			password = ApplicationConfigProvider.getInstance().getMessageQueue().getPassword();
			JsonObject rabbitMq = healthStatusService.getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
					ServiceStatusConstants.RabbitMq, Boolean.TRUE, username, password, authToken);
			servicesHealthStatus.add(ServiceStatusConstants.RabbitMq, rabbitMq);
			log.debug("After Rabbit Mq================");
			/* Platform Engine Health Check */
			hostEndPoint = ServiceStatusConstants.PlatformEngine;
			apiUrl = hostEndPoint;
			JsonObject jsonPlatformEngineStatus = healthStatusService.getComponentStatus("PlatformEngine", "");
			servicesHealthStatus.add(ServiceStatusConstants.PlatformEngine, jsonPlatformEngineStatus);
			log.debug("After Platform Engine================");

			if (ApplicationConfigProvider.getInstance().isEnableWebHookEngine()) {
				hostEndPoint = ServiceStatusConstants.PlatformWebhookEngine;
				apiUrl = hostEndPoint;
				JsonObject jsonPlatformWebhookEngineStatus = healthStatusService
						.getComponentStatus("PlatformWebhookEngine", "");
				servicesHealthStatus.add(ServiceStatusConstants.PlatformWebhookEngine, jsonPlatformWebhookEngineStatus);

				hostEndPoint = ServiceStatusConstants.PlatformWebhookSubscriber;
				apiUrl = hostEndPoint;
				JsonObject jsonPlatformWebhookSubscriberStatus = healthStatusService
						.getComponentStatus("PlatformWebhookSubscriber", "");
				servicesHealthStatus.add(ServiceStatusConstants.PlatformWebhookSubscriber,
						jsonPlatformWebhookSubscriberStatus);
			}

			if (ApplicationConfigProvider.getInstance().isEnableAuditEngine()) {
				hostEndPoint = ServiceStatusConstants.PlatformAuditEngine;
				apiUrl = hostEndPoint;
				JsonObject jsonPlatformAuditEngineStatus = healthStatusService.getComponentStatus("PlatformAuditEngine",
						"");
				servicesHealthStatus.add(ServiceStatusConstants.PlatformAuditEngine, jsonPlatformAuditEngineStatus);
			}
			if (ApplicationConfigProvider.getInstance().isEnableDataArchivalEngine()) {
				hostEndPoint = ServiceStatusConstants.PlatformDataArchivalEngine;
				apiUrl = hostEndPoint;
				JsonObject jsonPlatformDataArchivalEngineStatus = healthStatusService
						.getComponentStatus("PlatformDataArchivalEngine", "");
				servicesHealthStatus.add(ServiceStatusConstants.PlatformDataArchivalEngine,
						jsonPlatformDataArchivalEngineStatus);
			}

			log.debug(" servicesHealthStatus {}",servicesHealthStatus.toString());
		} catch (Exception e) {
			PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(servicesHealthStatus);
	}

	@RequestMapping(value = "/globalAgentsHealth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getAgentsHealthStatus() {
		String hostEndPoint = ServiceStatusConstants.Agents;
		JsonObject servicesAgentsHealthStatus = new JsonObject();
		try {
			JsonObject jsonAgentStatus = healthStatusService.getComponentStatus("Agents", "");
			servicesAgentsHealthStatus.add(ServiceStatusConstants.Agents, jsonAgentStatus);
			log.debug(" servicesAgentsHealthStatus {}",servicesAgentsHealthStatus);
		} catch (Exception e) {
			log.error(e.getMessage());
			PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(servicesAgentsHealthStatus);
	}

	@RequestMapping(value = "/detailHealth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadAgentsHealth(@RequestParam String category, @RequestParam String tool,
			@RequestParam String agentId) {
		int MAX_RECORD = 10;
		if (StringUtils.isEmpty(category) || StringUtils.isEmpty(tool)) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
		}
		log.debug(" message tool name {}  {}  {} ", category, tool, agentId);
		StringBuilder label = new StringBuilder("HEALTH");
		if (category.equalsIgnoreCase(ServiceStatusConstants.PlatformEngine)) {
			label.append(":").append("ENGINE");
		} else if (category.equalsIgnoreCase(ServiceStatusConstants.PlatformWebhookEngine)) {
			label.append(":").append("WEBHOOKENGINE");
		} else if (category.equalsIgnoreCase(ServiceStatusConstants.PlatformAuditEngine)) {
			label.append(":").append("AUDITENGINE");
		} else if (category.equalsIgnoreCase("Platform WebhookSubscriber")) {
			label.append(":").append("WEBHOOKSUBSCRIBER");
		} else if (category.equalsIgnoreCase(ServiceStatusConstants.InsightsInference)) {
			label.append(":").append("INSIGHTS");
		} else if (category.equalsIgnoreCase(ServiceStatusConstants.PlatformDataArchivalEngine)) {
			label.append(":").append("DATAARCHIVALENGINE");
		} else {
			label.append(":").append(category);
			label.append(":").append(tool);
		}
		GraphResponse response = healthStatusService.loadHealthData(label.toString(), ServiceStatusConstants.Agents,
				agentId, MAX_RECORD);
		return PlatformServiceUtil.buildSuccessResponseWithHtmlData(response);
	}

	@RequestMapping(value = "/getAgentFailureDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getAgentFailureDetails(@RequestParam String category, @RequestParam String tool,
			@RequestParam String agentId) {
		log.debug("############ inside getAgentFailureDetails  -----");
		return healthStatusService.createAgentFailureHealthLabel(category, tool, agentId);
	}

}
