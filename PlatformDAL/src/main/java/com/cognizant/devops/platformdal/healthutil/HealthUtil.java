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
package com.cognizant.devops.platformdal.healthutil;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.SystemStatus;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.dal.PostgresMetadataHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HealthUtil {
	static Logger log = LogManager.getLogger(HealthUtil.class);
	private static final String VERSION = "version";
	private static final String HOST_ENDPOINT = "endPoint";
	private static final String AGENT_NODES = "agentNodes";
	private static final String HEALTH_STATUS = "healthStatus";
	private static final String LAST_RUN_TIME = "lastRunTime";

	/**
	 * Method to fetch Client Response for Services and components
	 * 
	 * @param hostEndPoint
	 * @param apiUrl
	 * @param displayType
	 * @param serviceType
	 * @param isRequiredAuthentication
	 * @param username
	 * @param password
	 * @param authToken
	 * @return JsonObject
	 */
	public JsonObject getClientResponse(String hostEndPoint, String apiUrl, String displayType, String serviceType,
			boolean isRequiredAuthentication, String username, String password, String authToken) {
		JsonObject returnResponse = null;
		String strResponse = "";
		JsonObject json = null;
		String version = "";
		String serviceResponse;
		ElasticSearchDBHandler apiCallElasticsearch = new ElasticSearchDBHandler();
		try {
			if (isRequiredAuthentication) {
				serviceResponse = SystemStatus.jerseyGetClientWithAuthentication(apiUrl, username, password, authToken);
			} else {
				serviceResponse = apiCallElasticsearch.search(apiUrl);
			}

			if (serviceResponse != null && !("").equalsIgnoreCase(serviceResponse)) {
				strResponse = "Response successfully recieved from " + apiUrl;
				log.info("response: {} ",serviceResponse);
				if (serviceType.equalsIgnoreCase(ServiceStatusConstants.Neo4j)) {
					json = JsonUtils.parseStringAsJsonObject(serviceResponse);
					version = json.get("neo4j_version").getAsString();
					String totalDBSize = getNeo4jDBSize(hostEndPoint, username, password, authToken);
					returnResponse = buildSuccessResponse(strResponse, hostEndPoint, displayType, version);
					returnResponse.addProperty("totalDBSize", totalDBSize);
				} else if (serviceType.equalsIgnoreCase(ServiceStatusConstants.RabbitMq)) {
					json = JsonUtils.parseStringAsJsonObject(serviceResponse);
					version = "RabbitMq version " + json.get("rabbitmq_version").getAsString() + "\n Erlang version "
							+ json.get("erlang_version").getAsString();
					returnResponse = buildSuccessResponse(strResponse, hostEndPoint, displayType, version);
				} else if (serviceType.equalsIgnoreCase(ServiceStatusConstants.ES)) {
					json = JsonUtils.parseStringAsJsonObject(serviceResponse);
					JsonObject versionElasticsearch = (JsonObject) json.get(VERSION);
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
			log.error("Error while capturing health check at {} ",apiUrl, e);
			log.error(e.getMessage());
			strResponse = "Error while capturing health check at " + apiUrl;
			returnResponse = buildFailureResponse(strResponse, hostEndPoint, displayType, version);
		}
		return returnResponse;
	}


	/**
	 * Method to build Success Response
	 * 
	 * @param message
	 * @param apiUrl
	 * @param type
	 * @param version
	 * @return JsonObject
	 */
	public JsonObject buildSuccessResponse(String message, String apiUrl, String type, String version) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty(HOST_ENDPOINT, apiUrl);
		jsonResponse.addProperty(ServiceStatusConstants.type, type);
		jsonResponse.addProperty(VERSION, version);
		return jsonResponse;
	}

	/**
	 * Method to build Failure Response
	 * 
	 * @param message
	 * @param apiUrl
	 * @param type
	 * @param version
	 * @return JsonObject
	 */
	public JsonObject buildFailureResponse(String message, String apiUrl, String type, String version) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty(HOST_ENDPOINT, apiUrl);
		jsonResponse.addProperty(ServiceStatusConstants.type, type);
		jsonResponse.addProperty(VERSION, version);
		return jsonResponse;
	}

	
	/**
	 * Method to fetch status of Components
	 * 
	 * @param serviceType
	 * @return JsonObject
	 */
	public JsonObject getComponentStatus(String serviceType) {
		JsonObject returnObject = null;
		try {
			if (serviceType.equalsIgnoreCase("PlatformEngine")) {
				returnObject = getServiceResponse("HEALTH:ENGINE", 1);
			} else if (serviceType.equalsIgnoreCase("PlatformWebhookSubscriber")) {
				returnObject = getServiceResponse("HEALTH:WEBHOOKSUBSCRIBER", 1);
			} else if (serviceType.equalsIgnoreCase("PlatformWebhookEngine")) {
				returnObject = getServiceResponse("HEALTH:WEBHOOKENGINE", 1);
			} else if (serviceType.equalsIgnoreCase("PlatformAuditEngine")) {
				returnObject = getServiceResponse("HEALTH:AUDITENGINE", 1);
			} else if (serviceType.equalsIgnoreCase("PlatformDataArchivalEngine")) {
				returnObject = getServiceResponse("HEALTH:DATAARCHIVALENGINE", 1);
			} else if (serviceType.equalsIgnoreCase("PlatformWorkflow")) {
				returnObject = getServiceResponse("HEALTH:INSIGHTS_WORKFLOW", 1);
			} else if (serviceType.equalsIgnoreCase("PlatformService")) {
				returnObject = getServiceResponse("HEALTH:INSIGHTS_PLATFORMSERVICE", 1);
			}else if (serviceType.equalsIgnoreCase("Agents")) {
				returnObject = getAgentResponse("HEALTH:LATEST",100);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return returnObject;
	}

	
	/**
	 * Method to load Health Data
	 * 
	 * @param label
	 * @param agentId
	 * @param limitOfRow
	 * @return GraphResponse
	 */
	public GraphResponse loadHealthData(String label, String agentId, int limitOfRow) {

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

	/**
	 * Method to build Agent Response
	 * 
	 * @param status
	 * @param message
	 * @param graphResponse
	 * @return JsonObject
	 */
	private JsonObject buildAgentResponse(String status, String message, GraphResponse graphResponse) {
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
				toolcategory = node.getPropertyMap().get(AgentCommonConstant.CATEGORY);
				toolName = node.getPropertyMap().get(AgentCommonConstant.TOOLNAME);
				if (node.getPropertyMap().containsKey(AgentCommonConstant.AGENTID)) {
					agentId = node.getPropertyMap().get(AgentCommonConstant.AGENTID);
				} else {
					agentId = "";
				}
				agentstatus = node.getPropertyMap().get(PlatformServiceConstants.STATUS);
				insightTimeX = node.getPropertyMap().get(PlatformServiceConstants.INSIGHTSTIMEX);
				JsonObject jsonResponse2 = new JsonObject();
				jsonResponse2.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, insightTimeX);
				jsonResponse2.addProperty(AgentCommonConstant.TOOLNAME, toolName);
				jsonResponse2.addProperty(AgentCommonConstant.AGENTID, agentId);
				jsonResponse2.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, insightTimeX);
				jsonResponse2.addProperty(PlatformServiceConstants.STATUS, agentstatus);
				jsonResponse2.addProperty(AgentCommonConstant.CATEGORY, toolcategory);
				agentNode.add(jsonResponse2);
			}
			jsonResponse.add(AGENT_NODES, agentNode);
		} else {
			jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
			jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
			jsonResponse.addProperty(AgentCommonConstant.CATEGORY, toolcategory);
			jsonResponse.addProperty(ServiceStatusConstants.type, ServiceStatusConstants.Agents);
			jsonResponse.addProperty(AgentCommonConstant.TOOLNAME, toolName);
			jsonResponse.addProperty(PlatformServiceConstants.INSIGHTSTIMEX, insightTimeX);
			jsonResponse.addProperty(VERSION, "");
			jsonResponse.add(AGENT_NODES, agentNode);
		}

		return jsonResponse;
	}

	/**
	 * Method to get Neo4h DB size
	 * 
	 * @param hostEndPoint
	 * @param username
	 * @param password
	 * @param authToken
	 * @return String
	 */
	private String getNeo4jDBSize(String hostEndPoint, String username, String password, String authToken) {
		long totalStoreSize = 0L;
		String returnSize = "";
		try {
			String apiUrlForSize = hostEndPoint
					+ "/db/manage/server/jmx/domain/org.neo4j/instance%3Dkernel%230%2Cname%3DStore+sizes";
			String serviceNeo4jResponse = SystemStatus.jerseyGetClientWithAuthentication(apiUrlForSize, username,
					password, authToken);
			log.debug("serviceNeo4jResponse ====== {} ",serviceNeo4jResponse);

			JsonElement object = JsonUtils.parseString(serviceNeo4jResponse);
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
			log.debug(" info totalStoreSize  ==== {}",totalStoreSize);
			if (totalStoreSize > 0) {
				returnSize = humanReadableByteCount(totalStoreSize, Boolean.FALSE);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			log.error(" Error while geeting neo4j Size");
		}
		return returnSize;
	}

	/**
	 * Method to generate Human Readable byte count
	 * 
	 * @param bytes
	 * @param si
	 * @return String
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Method to get Agent Health Queue name
	 * 
	 * @param agentId
	 * @return String
	 */
	public String getAgentHealthQueueName(String agentId) {
		String healthRoutingKey = null;
		try {
			AgentConfigDAL agentConfigDal = new AgentConfigDAL();
			AgentConfig agentConfig = agentConfigDal.getAgentConfigurations(agentId);
			JsonObject config = JsonUtils.parseStringAsJsonObject(agentConfig.getAgentJson());
			JsonObject json = config.get("publish").getAsJsonObject();
			healthRoutingKey = json.get("health").getAsString().replace(".", ":");
		} catch (Exception e) {
			log.error(" No DB record found for agentId {} ", agentId);
		}
		return healthRoutingKey;
	}
	
	/**
	 * Method to get Service Response
	 * 
	 * @param labels
	 * @param noOfRows
	 * @return JsonObject
	 */
	private JsonObject getServiceResponse(String labels,int noOfRows) {
		String successResponse = "";
		String version = "";
		String status = "";
		JsonObject returnObject = null;
		GraphResponse graphResponse = loadHealthData(labels,"", noOfRows);
		if (graphResponse != null) {
			if (!graphResponse.getNodes().isEmpty()) {
				successResponse = graphResponse.getNodes().get(0).getPropertyMap().get("message");
				version = graphResponse.getNodes().get(0).getPropertyMap().get(VERSION);
				status = graphResponse.getNodes().get(0).getPropertyMap().get("status");
				if (status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
					returnObject = buildSuccessResponse(successResponse, "-",
							ServiceStatusConstants.Service, version);
				} else {
					returnObject = buildFailureResponse(successResponse, "-",
							ServiceStatusConstants.Service, version);
				}
			} else {
				successResponse = "Node list is empty in response not received from Neo4j";
				returnObject = buildFailureResponse(successResponse, "-",
						ServiceStatusConstants.Service, version);
			}
		} else {
			successResponse = "Response not received from Neo4j";
			returnObject = buildFailureResponse(successResponse, "-", ServiceStatusConstants.Service,
					version);
		}
		return returnObject;
	}
	
	/**
	 * Method to fetch response of Agents
	 * 
	 * @param labels
	 * @param noOfRows
	 * @return JsonObject
	 */
	private JsonObject getAgentResponse(String labels, int noOfRows) {
		String successResponse = "";
		String status = "";
		GraphResponse graphResponse = loadHealthData(labels, "", noOfRows);
		if (graphResponse != null) {
			if (!graphResponse.getNodes().isEmpty()) {
				status = PlatformServiceConstants.SUCCESS;
			} else {
				successResponse = "Node list is empty in response not received from Neo4j";
				status = PlatformServiceConstants.FAILURE;
			}
		} else {
			successResponse = "Response not received from Neo4j";
			status = PlatformServiceConstants.FAILURE;
		}
		log.debug("message {} ", successResponse);
		return buildAgentResponse(status, successResponse, graphResponse);
	}
	
	/**
	 * Method to fetch data component HTML
	 * 
	 * @return String
	 */
	public JsonObject getDataComponentStatus() {
		JsonObject dataComponentStatus = new JsonObject();
		try {
			String username = null;
			String password = null;
			String authToken = null;
			String hostEndPoint = "";
			String apiUrl = "";
			hostEndPoint = ServiceStatusConstants.POSTGRESQL_HOST;
			apiUrl = hostEndPoint;
			JsonObject postgreStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
					ServiceStatusConstants.PgSQL, Boolean.FALSE, username, password, authToken);
			dataComponentStatus.add(ServiceStatusConstants.PgSQL, postgreStatus);
			hostEndPoint = ServiceStatusConstants.NEO4J_HOST;
			apiUrl = hostEndPoint + "/db/data/";
			authToken = ApplicationConfigProvider.getInstance().getGraph().getAuthToken();
			JsonObject neo4jStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
					ServiceStatusConstants.Neo4j, Boolean.TRUE, username, password, authToken);
			dataComponentStatus.add(ServiceStatusConstants.Neo4j, neo4jStatus);
			hostEndPoint = ServiceStatusConstants.ES_HOST;
			apiUrl = hostEndPoint;
			JsonObject esStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
					ServiceStatusConstants.ES, Boolean.FALSE, username, password, authToken);
			dataComponentStatus.add(ServiceStatusConstants.ES, esStatus);
			hostEndPoint = ServiceStatusConstants.RABBIT_MQ;
			apiUrl = hostEndPoint + "/api/overview";
			authToken = null;
			username = ApplicationConfigProvider.getInstance().getMessageQueue().getUser();
			password = ApplicationConfigProvider.getInstance().getMessageQueue().getPassword();
			JsonObject rabbitMq = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
					ServiceStatusConstants.RabbitMq, Boolean.TRUE, username, password, authToken);
			dataComponentStatus.add(ServiceStatusConstants.RabbitMq, rabbitMq);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error creating HTML body for data components");
		}
		log.debug(" dataComponentStatus {} ", dataComponentStatus);
		return dataComponentStatus;

	}

	/**
	 * Method to fetch Service HTML
	 * 
	 * @return String
	 */
	public JsonObject getServiceStatus() {
		JsonObject serviceStatus = new JsonObject();
		try {

			JsonObject jsonPlatformServiceStatus = getComponentStatus("PlatformService");
			serviceStatus.add(ServiceStatusConstants.PlatformService, jsonPlatformServiceStatus);
			JsonObject jsonPlatformEngineStatus = getComponentStatus("PlatformEngine");
			serviceStatus.add(ServiceStatusConstants.PlatformEngine, jsonPlatformEngineStatus);
			JsonObject jsonPlatformWorkflowStatus = getComponentStatus("PlatformWorkflow");
			serviceStatus.add(ServiceStatusConstants.PlatformWorkflow, jsonPlatformWorkflowStatus);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error creating HTML body for services");
		}
		log.debug(" serviceStatus {} ", serviceStatus);
		return serviceStatus;

	}
	
	/**
	 * Method to get health status of all agents
	 * 
	 * @return JsonObject
	 */
	public JsonObject getAgentsStatus() {
		return getComponentStatus("Agents");
	}
	
	public JsonObject getRegisteredAgentsAndHealth() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		JsonObject agentDetails = new  JsonObject();
		JsonArray agentNodes = new JsonArray();
		try {
			List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
			for (AgentConfig agentConfig : agentConfigList) {
				JsonObject node = new JsonObject();
				node.addProperty("toolName", agentConfig.getToolName());
				node.addProperty("agentId", agentConfig.getAgentKey());
				JsonObject agentHealthNode = getAgentHealth(node, agentConfig.getAgentKey());
				if(agentHealthNode.has(LAST_RUN_TIME)) {
					node.addProperty(LAST_RUN_TIME,agentHealthNode.get(LAST_RUN_TIME).getAsString());
				} else {
					node.addProperty(LAST_RUN_TIME, "");
				}
				if(agentHealthNode.has(HEALTH_STATUS)) {
					node.addProperty(HEALTH_STATUS,agentHealthNode.get(HEALTH_STATUS).getAsString());
				} else {
					node.addProperty(HEALTH_STATUS,"");
				}
				agentNodes.add(node);
			}
			agentDetails.add(AGENT_NODES, agentNodes);
		} catch (Exception e) {
			log.error("Error getting all agent config ", e);
			throw new InsightsCustomException(e.toString());
		}
		return agentDetails;
	}
	
	public JsonObject getAgentHealth(JsonObject agentJson, String agentId) {
		GraphDBHandler graphDBHandler = new GraphDBHandler();
		JsonObject response;
		try {
			response = graphDBHandler.executeCypherQueryForJsonResponse("MATCH (n:HEALTH:LATEST) where n.agentId='"+agentId+"' return n order by n.inSightsTime desc limit 1");		
			JsonArray responseArray = response.get("results").getAsJsonArray();
			JsonArray responseData = responseArray.get(0).getAsJsonObject().get("data").getAsJsonArray();
			if(responseData.size() > 0) {
				agentJson.addProperty(LAST_RUN_TIME, responseData.get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().get(PlatformServiceConstants.INSIGHTSTIMEX).getAsString());
				agentJson.addProperty(HEALTH_STATUS, responseData.get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().get(PlatformServiceConstants.STATUS).getAsString());
			}
		} catch (Exception e) {
			log.error("Error getting agent health ", e);
		}
		
		return agentJson;
	}

}
