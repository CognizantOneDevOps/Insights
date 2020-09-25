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

import java.io.IOException;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.util.SystemStatus;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.dal.PostgresMetadataHandler;
import com.cognizant.devops.platformservice.rest.health.HealthStatusController;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("healthStatusService")
public class HealthStatusServiceImpl implements HealthStatusService {
	static Logger log = LogManager.getLogger(HealthStatusServiceImpl.class);
	private static final String VERSION = "version";
	private static final String HOST_ENDPOINT = "endPoint";

	@Override
	public JsonObject getClientResponse(String hostEndPoint, String apiUrl, String displayType, String serviceType,
			boolean isRequiredAuthentication, String username, String password, String authToken) {
		JsonObject returnResponse = null;
		String strResponse = "";
		JsonObject json = null;
		String version = "";
		String serviceResponse;
		JsonParser jsonParser = new JsonParser();
		ElasticSearchDBHandler apiCallElasticsearch = new ElasticSearchDBHandler();
		try {
			if (isRequiredAuthentication) {
				serviceResponse = SystemStatus.jerseyGetClientWithAuthentication(apiUrl, username, password, authToken);
			} else {
				serviceResponse = apiCallElasticsearch.search(apiUrl);
			}

			if (serviceResponse != null && !("").equalsIgnoreCase(serviceResponse)) {
				strResponse = "Response successfully recieved from " + apiUrl;
				log.info("response: " + serviceResponse);
				if (serviceType.equalsIgnoreCase(ServiceStatusConstants.Neo4j)) {
					json = (JsonObject) jsonParser.parse(serviceResponse);
					version = json.get("neo4j_version").getAsString();
					String totalDBSize = getNeo4jDBSize(hostEndPoint, username, password, authToken);
					returnResponse = buildSuccessResponse(strResponse, hostEndPoint, displayType, version);
					returnResponse.addProperty("totalDBSize", totalDBSize);
				} else if (serviceType.equalsIgnoreCase(ServiceStatusConstants.RabbitMq)) {
					json = (JsonObject) jsonParser.parse(serviceResponse);
					version = "RabbitMq version " + json.get("rabbitmq_version").getAsString() + "\n Erlang version "
							+ json.get("erlang_version").getAsString();
					returnResponse = buildSuccessResponse(strResponse, hostEndPoint, displayType, version);
				} else if (serviceType.equalsIgnoreCase(ServiceStatusConstants.ES)) {
					json = (JsonObject) jsonParser.parse(serviceResponse);
					JsonObject versionElasticsearch = (JsonObject) json.get("version");
					if (versionElasticsearch != null) {
						version = versionElasticsearch.get("number").getAsString();
					}
					returnResponse = buildSuccessResponse(strResponse, hostEndPoint, displayType, version);
				} else if (serviceType.equalsIgnoreCase(ServiceStatusConstants.PgSQL)) {
					PostgresMetadataHandler pgdbHandler = new PostgresMetadataHandler();
					version = pgdbHandler.getPostgresDBVersion();
					returnResponse = buildSuccessResponse(strResponse, hostEndPoint, displayType, version);
				}
			} else {
				strResponse = "Response not received from service " + apiUrl;
				returnResponse = buildFailureResponse(strResponse, hostEndPoint, displayType, version);
			}
		} catch (Exception e) {
			log.error("Error while capturing health check at " + apiUrl, e);
			log.error(e.getMessage());
			strResponse = "Error while capturing health check at " + apiUrl;
			returnResponse = buildFailureResponse(strResponse, hostEndPoint, displayType, version);
		}
		return returnResponse;
	}

	@Override
	public JsonObject getVersionDetails(String fileName, String hostEndPoint, String type) throws IOException {

		String version = "";
		String strResponse = "";
		if (version.equalsIgnoreCase("")) {
			version = HealthStatusController.class.getPackage().getSpecificationVersion();
			strResponse = "Version captured as " + version;
			return buildSuccessResponse(strResponse, hostEndPoint, type, version);
		} else {
			strResponse = "Error while capturing PlatformService (version " + version + ") health check at "
					+ hostEndPoint;
			return buildFailureResponse(strResponse, hostEndPoint, type, version);
		}
	}

	private JsonObject buildSuccessResponse(String message, String apiUrl, String type, String version) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty(HOST_ENDPOINT, apiUrl);
		jsonResponse.addProperty(ServiceStatusConstants.type, type);
		jsonResponse.addProperty(VERSION, version);
		return jsonResponse;
	}

	private JsonObject buildFailureResponse(String message, String apiUrl, String type, String version) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty(HOST_ENDPOINT, apiUrl);
		jsonResponse.addProperty(ServiceStatusConstants.type, type);
		jsonResponse.addProperty(VERSION, version);
		return jsonResponse;
	}

	@Override
	public JsonObject getComponentStatus(String serviceType, String apiUrl) {
		String successResponse = "";
		String version = "";
		String status = "";
		JsonObject returnObject = null;
		GraphResponse graphResponse;
		String serviceResponse = "";
		ElasticSearchDBHandler apiCallElasticsearch = new ElasticSearchDBHandler();
		try {
			if (serviceType.equalsIgnoreCase("PlatformEngine")) {
				graphResponse = loadHealthData("HEALTH:ENGINE", serviceType, "", 1);
				if (graphResponse != null) {
					if (graphResponse.getNodes().size() > 0) {
						successResponse = graphResponse.getNodes().get(0).getPropertyMap().get("message");
						;
						version = graphResponse.getNodes().get(0).getPropertyMap().get("version");
						status = graphResponse.getNodes().get(0).getPropertyMap().get("status");
						if (status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
							returnObject = buildSuccessResponse(successResponse.toString(), "-",
									ServiceStatusConstants.Service, version);
						} else {
							returnObject = buildFailureResponse(successResponse.toString(), "-",
									ServiceStatusConstants.Service, version);
						}
					} else {
						successResponse = "Node list is empty in response not received from Neo4j";
						returnObject = buildFailureResponse(successResponse.toString(), "-",
								ServiceStatusConstants.Service, version);
					}
				} else {
					successResponse = "Response not received from Neo4j";
					returnObject = buildFailureResponse(successResponse.toString(), "-", ServiceStatusConstants.Service,
							version);
				}
			} else if (serviceType.equalsIgnoreCase("PlatformWebhookSubscriber")) {
				graphResponse = loadHealthData("HEALTH:WEBHOOKSUBSCRIBER", serviceType, "", 1);
				if (graphResponse != null) {
					if (graphResponse.getNodes().size() > 0) {
						successResponse = graphResponse.getNodes().get(0).getPropertyMap().get("message");
						;
						version = graphResponse.getNodes().get(0).getPropertyMap().get("version");
						status = graphResponse.getNodes().get(0).getPropertyMap().get("status");
						if (status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
							returnObject = buildSuccessResponse(successResponse.toString(), "-",
									ServiceStatusConstants.Service, version);
						} else {
							returnObject = buildFailureResponse(successResponse.toString(), "-",
									ServiceStatusConstants.Service, version);
						}
					} else {
						successResponse = "Node list is empty in response not received from Neo4j";
						returnObject = buildFailureResponse(successResponse.toString(), "-",
								ServiceStatusConstants.Service, version);
					}
				} else {
					successResponse = "Response not received from Neo4j";
					returnObject = buildFailureResponse(successResponse.toString(), "-", ServiceStatusConstants.Service,
							version);
				}
			}

			else if (serviceType.equalsIgnoreCase("PlatformWebhookEngine")) {
				graphResponse = loadHealthData("HEALTH:WEBHOOKENGINE", serviceType, "", 1);
				if (graphResponse != null) {
					if (graphResponse.getNodes().size() > 0) {
						successResponse = graphResponse.getNodes().get(0).getPropertyMap().get("message");
						;
						version = graphResponse.getNodes().get(0).getPropertyMap().get("version");
						status = graphResponse.getNodes().get(0).getPropertyMap().get("status");
						if (status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
							returnObject = buildSuccessResponse(successResponse.toString(), "-",
									ServiceStatusConstants.Service, version);
						} else {
							returnObject = buildFailureResponse(successResponse.toString(), "-",
									ServiceStatusConstants.Service, version);
						}
					} else {
						successResponse = "Node list is empty in response not received from Neo4j";
						returnObject = buildFailureResponse(successResponse.toString(), "-",
								ServiceStatusConstants.Service, version);
					}
				} else {
					successResponse = "Response not received from Neo4j";
					returnObject = buildFailureResponse(successResponse.toString(), "-", ServiceStatusConstants.Service,
							version);
				}
			} else if (serviceType.equalsIgnoreCase("PlatformAuditEngine")) {
				graphResponse = loadHealthData("HEALTH:AUDITENGINE", serviceType, "", 1);
				if (graphResponse != null) {
					if (graphResponse.getNodes().size() > 0) {
						successResponse = graphResponse.getNodes().get(0).getPropertyMap().get("message");
						;
						version = graphResponse.getNodes().get(0).getPropertyMap().get("version");
						status = graphResponse.getNodes().get(0).getPropertyMap().get("status");
						if (status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
							returnObject = buildSuccessResponse(successResponse.toString(), "-",
									ServiceStatusConstants.Service, version);
						} else {
							returnObject = buildFailureResponse(successResponse.toString(), "-",
									ServiceStatusConstants.Service, version);
						}
					} else {
						successResponse = "Node list is empty in response not received from Neo4j";
						returnObject = buildFailureResponse(successResponse.toString(), "-",
								ServiceStatusConstants.Service, version);
					}
				} else {
					successResponse = "Response not received from Neo4j";
					returnObject = buildFailureResponse(successResponse.toString(), "-", ServiceStatusConstants.Service,
							version);
				}
			} else if (serviceType.equalsIgnoreCase("PlatformInsight")) {
				graphResponse = loadHealthData("HEALTH:INSIGHTS", serviceType, "", 1);
				if (graphResponse != null) {
					if (graphResponse.getNodes().size() > 0) {
						successResponse = graphResponse.getNodes().get(0).getPropertyMap().get("message");
						;
						version = graphResponse.getNodes().get(0).getPropertyMap().get("version");
						status = graphResponse.getNodes().get(0).getPropertyMap().get("status");
					} else {
						successResponse = "Node list is empty in response not received from Neo4j";
						status = PlatformServiceConstants.FAILURE;
					}
				} else {
					successResponse = "Response not received from Neo4j";
					status = PlatformServiceConstants.FAILURE;
				}

				if (status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
					returnObject = buildSuccessResponse(successResponse.toString(), apiUrl,
							ServiceStatusConstants.Service, version);
				} else {
					returnObject = buildFailureResponse(successResponse.toString(), apiUrl,
							ServiceStatusConstants.Service, version);
				}

			} else if (serviceType.equalsIgnoreCase("PlatformDataArchivalEngine")) {
				graphResponse = loadHealthData("HEALTH:DATAARCHIVALENGINE", serviceType, "", 1);
				if (graphResponse != null) {
					if (graphResponse.getNodes().size() > 0) {
						successResponse = graphResponse.getNodes().get(0).getPropertyMap().get("message");
						;
						version = graphResponse.getNodes().get(0).getPropertyMap().get("version");
						status = graphResponse.getNodes().get(0).getPropertyMap().get("status");
					} else {
						successResponse = "Node list is empty in response not received from Neo4j";
						status = PlatformServiceConstants.FAILURE;
					}
				} else {
					successResponse = "Response not received from Neo4j";
					status = PlatformServiceConstants.FAILURE;
				}

				if (status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
					returnObject = buildSuccessResponse(successResponse.toString(), apiUrl,
							ServiceStatusConstants.Service, version);
				} else {
					returnObject = buildFailureResponse(successResponse.toString(), apiUrl,
							ServiceStatusConstants.Service, version);
				}

			} else if (serviceType.equalsIgnoreCase("Agents")) {
				graphResponse = loadHealthData("HEALTH:LATEST", serviceType, "", 100);
				if (graphResponse != null) {
					if (graphResponse.getNodes().size() > 0) {
						status = PlatformServiceConstants.SUCCESS;
					} else {
						successResponse = "Node list is empty in response not received from Neo4j";
						status = PlatformServiceConstants.FAILURE;
					}
				} else {
					successResponse = "Response not received from Neo4j";
					status = PlatformServiceConstants.FAILURE;
				}
				log.debug("message " + successResponse);
				returnObject = buildAgentResponse(status, successResponse, graphResponse);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return returnObject;
	}

	@Override
	public GraphResponse loadHealthData(String label, String type, String agentId, int limitOfRow) {

		String query = "";

		if (agentId.equalsIgnoreCase("")) {
			query = "MATCH (n:" + label
					+ ") where n.inSightsTime IS NOT NULL RETURN n order by n.inSightsTime DESC LIMIT " + limitOfRow;
		} else if (!agentId.equalsIgnoreCase("")) {
			String queueName = getAgentHealthQueueName(agentId);
			// To handle case where Agent delete from Postgres but data present in Neo4j
			if (queueName == null) {
				queueName = label;
			}
			query = "MATCH (n:" + queueName + ") where n.inSightsTime IS NOT NULL and n.agentId ='" + agentId
					+ "' RETURN n order by n.inSightsTime DESC LIMIT " + limitOfRow;
		}
		log.info("query  ====== {} ", query);
		GraphResponse graphResponse = null;
		try {
			GraphDBHandler dbHandler = new GraphDBHandler();
			graphResponse = dbHandler.executeCypherQuery(query);
		} catch (Exception e) {
			log.error(e.getMessage());
			graphResponse = new GraphResponse();
		}
		return graphResponse;
	}

	private JsonObject buildAgentResponse(String status, String message, GraphResponse graphResponse) {
		// log.debug(" message in buildAgentResponse debug
		// "+graphResponse.getNodes().size());
		String toolcategory = "";
		String toolName = "";
		String insightTimeX = "";
		String agentstatus = "";
		String agentId = "";
		JsonObject jsonResponse = new JsonObject();
		JsonArray agentNode = new JsonArray();
		if (status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {

			jsonResponse.addProperty(ServiceStatusConstants.type, ServiceStatusConstants.Agents);

			Iterator<NodeData> agentnodeIterator = graphResponse.getNodes().iterator();
			while (agentnodeIterator.hasNext()) {
				NodeData node = agentnodeIterator.next();
				insightTimeX = node.getPropertyMap().get("inSightsTimeX");
				;
				message = node.getPropertyMap().get("message");
				;
				toolcategory = node.getPropertyMap().get("category");
				;
				toolName = node.getPropertyMap().get("toolName");
				if (node.getPropertyMap().containsKey("agentId")) {
					agentId = node.getPropertyMap().get("agentId");
				} else {
					agentId = "";
				}
				agentstatus = node.getPropertyMap().get("status");
				insightTimeX = node.getPropertyMap().get("inSightsTimeX");
				;
				JsonObject jsonResponse2 = new JsonObject();
				jsonResponse2.addProperty("inSightsTimeX", insightTimeX);
				jsonResponse2.addProperty("toolName", toolName);
				jsonResponse2.addProperty("agentId", agentId);
				jsonResponse2.addProperty("inSightsTimeX", insightTimeX);
				jsonResponse2.addProperty(PlatformServiceConstants.STATUS, agentstatus);
				// jsonResponse2.addProperty(PlatformServiceConstants.MESSAGE, message);
				jsonResponse2.addProperty("category", toolcategory);
				agentNode.add(jsonResponse2);
			}
			jsonResponse.add("agentNodes", agentNode);
		} else {
			jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
			jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
			jsonResponse.addProperty("category", toolcategory);
			jsonResponse.addProperty(ServiceStatusConstants.type, ServiceStatusConstants.Agents);
			jsonResponse.addProperty("toolName", toolName);
			jsonResponse.addProperty("inSightsTimeX", insightTimeX);
			jsonResponse.addProperty(VERSION, "");
			jsonResponse.add("agentNodes", agentNode);
		}

		return jsonResponse;
	}

	private String getNeo4jDBSize(String hostEndPoint, String username, String password, String authToken) {
		long totalStoreSize = 0L;
		String returnSize = "";
		try {
			String apiUrlForSize = hostEndPoint
					+ "/db/manage/server/jmx/domain/org.neo4j/instance%3Dkernel%230%2Cname%3DStore+sizes";
			String serviceNeo4jResponse = SystemStatus.jerseyGetClientWithAuthentication(apiUrlForSize, username,
					password, authToken);
			log.debug("serviceNeo4jResponse ======  " + serviceNeo4jResponse);

			JsonElement object = new JsonParser().parse(serviceNeo4jResponse);
			if (object.isJsonArray()) {
				if (object.getAsJsonArray().get(0).getAsJsonObject().get("attributes").isJsonArray()) {
					JsonArray beans = object.getAsJsonArray().get(0).getAsJsonObject().get("attributes")
							.getAsJsonArray();
					for (JsonElement jsonElement : beans) {
						if (jsonElement.getAsJsonObject().get("name").getAsString()
								.equalsIgnoreCase("TotalStoreSize")) {
							totalStoreSize = jsonElement.getAsJsonObject().get("value").getAsLong();
						}
					}
				}
			}
			log.debug(" info totalStoreSize  ==== " + totalStoreSize);
			if (totalStoreSize > 0) {
				returnSize = humanReadableByteCount(totalStoreSize, Boolean.FALSE);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			log.error(" Error while geeting neo4j Size");
		}
		return returnSize;
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	@Override
	public JsonObject createAgentFailureHealthLabel(String category, String tool, String agentId) {
		log.debug(" message tool name {}  {} ", category, tool);
		StringBuilder label = new StringBuilder("HEALTH_FAILURE");
		if (StringUtils.isEmpty(category) || StringUtils.isEmpty(tool)) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
		} else {
			label.append(":").append(category);
			label.append(":").append(tool);
		}
		return loadAgentsFailureHealthData(label.toString(), agentId, 20);
	}

	private JsonObject loadAgentsFailureHealthData(String nodeLabel, String agentId, int limitOfRow) {
		String query = "";

		if (agentId.equalsIgnoreCase("")) {
			query = "MATCH (n:" + nodeLabel
					+ ") where n.inSightsTime IS NOT NULL RETURN n order by n.inSightsTime DESC LIMIT " + limitOfRow;
		} else if (!agentId.equalsIgnoreCase("")) {
			String queueName = getAgentHealthQueueName(agentId);
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
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
	}

	private String getAgentHealthQueueName(String agentId) {
		String healthRoutingKey = null;
		try {
			AgentConfigDAL agentConfigDal = new AgentConfigDAL();
			AgentConfig agentConfig = agentConfigDal.getAgentConfigurations(agentId);
			JsonObject config = (JsonObject) new JsonParser().parse(agentConfig.getAgentJson());
			JsonObject json = config.get("publish").getAsJsonObject();
			healthRoutingKey = json.get("health").getAsString().replace(".", ":");
		} catch (Exception e) {
			log.error(" No DB record found for agentId {}", agentId);
		}
		return healthRoutingKey;
	}

}
