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

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.util.SystemStatus;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformdal.dal.PostgresMetadataHandler;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@RestController
@RequestMapping("/admin/health")
public class HealthStatus {

	private static final String VERSION = "version";
	private static final String HOST_ENDPOINT = "endPoint";
	private static final String PLATFORM_SERVICE_VERSION_FILE = "version.properties";

	static Logger log = Logger.getLogger(HealthStatus.class.getName());
	@RequestMapping(value = "/globalHealth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public JsonObject getHealthStatus() throws IOException{
		JsonObject servicesHealthStatus = new JsonObject();
		String username=null;
		String password=null;
		String authToken=null;

		//ApplicationConfigCache.loadConfigCache();
		/*PostgreSQL health check*/
		String hostEndPoint = ServiceStatusConstants.POSTGRESQL_HOST;
		String apiUrl = hostEndPoint;		
		JsonObject postgreStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,ServiceStatusConstants.PgSQL,Boolean.FALSE,username,password,authToken);
		servicesHealthStatus.add(ServiceStatusConstants.PgSQL, postgreStatus);

		/*PlatformService health check*/
		hostEndPoint = ServiceStatusConstants.PLATFORM_SERVICE_HOST;
		JsonObject platformServStatus = getVersionDetails(PLATFORM_SERVICE_VERSION_FILE, hostEndPoint, ServiceStatusConstants.Service);
		servicesHealthStatus.add(ServiceStatusConstants.PlatformService, platformServStatus);

		/*Insights Inference health check*/	
		hostEndPoint = ServiceStatusConstants.INSIGHTS_INFERENCE_MASTER_HOST;
		apiUrl = hostEndPoint+"/jobs";
		JsonObject inferenceServStatus = getComponentStatus("PlatformInsightSpark",apiUrl);//getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.Service,"");
		servicesHealthStatus.add(ServiceStatusConstants.InsightsInference, inferenceServStatus);

		/*Neo4j health check*/
		hostEndPoint = ServiceStatusConstants.NEO4J_HOST;
		apiUrl = hostEndPoint+"/db/data/";
		authToken=ApplicationConfigProvider.getInstance().getGraph().getAuthToken();
		JsonObject neo4jStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,ServiceStatusConstants.Neo4j,Boolean.TRUE,username,password,authToken);
		servicesHealthStatus.add(ServiceStatusConstants.Neo4j, neo4jStatus);

		/*Elastic Search health check*/
		hostEndPoint = ServiceStatusConstants.ES_HOST;
		apiUrl = hostEndPoint;
		JsonObject EsStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,ServiceStatusConstants.ES,Boolean.FALSE,username,password,authToken);
		servicesHealthStatus.add(ServiceStatusConstants.ES, EsStatus);

		/*Rabbit Mq health check */
		hostEndPoint = ServiceStatusConstants.RABBIT_MQ;
		apiUrl = hostEndPoint+"/api/overview";
		authToken=null;
		username=ApplicationConfigProvider.getInstance().getMessageQueue().getUser();
		password=ApplicationConfigProvider.getInstance().getMessageQueue().getPassword();
		JsonObject rabbitMq = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,ServiceStatusConstants.RabbitMq,Boolean.TRUE,username,password,authToken);
		servicesHealthStatus.add(ServiceStatusConstants.RabbitMq, rabbitMq);

		/*Patform Engine Health Check*/
		hostEndPoint = ServiceStatusConstants.PlatformEngine;
		apiUrl = hostEndPoint;
		JsonObject jsonPlatformEngineStatus = getComponentStatus("PlatformEngine","");
		servicesHealthStatus.add(ServiceStatusConstants.PlatformEngine, jsonPlatformEngineStatus);

//		hostEndPoint = ServiceStatusConstants.Agents;
//		apiUrl = hostEndPoint;
//		JsonObject jsonAgentStatus = getComponentStatus("Agents","");
//		servicesHealthStatus.add(ServiceStatusConstants.Agents, jsonAgentStatus);

		log.debug(" servicesHealthStatus "+servicesHealthStatus.toString());
		return servicesHealthStatus;		
	}
	@RequestMapping(value = "/globalAgentsHealth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public JsonObject getAgentsHealthStatus() throws IOException{

		String hostEndPoint = ServiceStatusConstants.Agents;
		JsonObject servicesAgentsHealthStatus = new JsonObject();
		JsonObject jsonAgentStatus = getComponentStatus("Agents","");
		servicesAgentsHealthStatus.add(ServiceStatusConstants.Agents, jsonAgentStatus);
		return servicesAgentsHealthStatus;	
	}





	@RequestMapping(value = "/detailHealth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadAgentsHealth(@RequestParam String category, @RequestParam String tool,@RequestParam String agentId){
		if(StringUtils.isEmpty(category) || StringUtils.isEmpty(tool)){
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
		}
		log.debug(" message tool name "+category+"  "+tool+"  "+agentId);
		StringBuffer label = new StringBuffer("HEALTH");
		if(category.equalsIgnoreCase(ServiceStatusConstants.PlatformEngine)) {
			label.append(":").append("ENGINE");
		}else if(category.equalsIgnoreCase(ServiceStatusConstants.InsightsInference)) {
			label.append(":").append("INSIGHTS");
		}else {
			label.append(":").append(category);
			label.append(":").append(tool);	
		}
		GraphResponse response = loadHealthData(label.toString(),ServiceStatusConstants.Agents,agentId);
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}

	private JsonObject getClientResponse(String hostEndPoint, String apiUrl, String type,String version){
		try {
			Client client = Client.create();
			WebResource webResource = client
					.resource(apiUrl);

			ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON.toString())
					.get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());
			}

			String successResponse = "" ;
			if( response.getStatus() == 200){
				successResponse = "Response successfully recieved from "+apiUrl;
			}
			return buildSuccessResponse(successResponse, hostEndPoint, type,version);

		} catch (Exception e) {
			log.error("Error while capturing health check at "+apiUrl,e);
		}
		String failureResponse = "Error while capturing health check at "+apiUrl;
		return buildFailureResponse(failureResponse, hostEndPoint, type,version);
	}

	private JsonObject getClientResponse(String hostEndPoint, String apiUrl, String displayType,String serviceType, boolean isRequiredAuthentication,String username, String password,String authToken){
		JsonObject returnResponse=null;
		String strResponse = "" ;
		JsonObject json=null;
		String version="";
		String serviceResponse;
		JsonParser jsonParser = new JsonParser();
		ElasticSearchDBHandler apiCallElasticsearch =new ElasticSearchDBHandler();
		try {
			if(isRequiredAuthentication) {
				serviceResponse=SystemStatus.jerseyGetClientWithAuthentication(apiUrl, username, password,authToken);
			}else {
				serviceResponse=apiCallElasticsearch.search(apiUrl); 
			}

			if( serviceResponse !=null && !("").equalsIgnoreCase(serviceResponse)){
				strResponse = "Response successfully recieved from "+apiUrl;
				log.info("response: "+serviceResponse);

				if(serviceType.equalsIgnoreCase(ServiceStatusConstants.Neo4j)) {
					json=(JsonObject) jsonParser.parse(serviceResponse);
					version=json.get("neo4j_version").getAsString();
				}else if(serviceType.equalsIgnoreCase(ServiceStatusConstants.RabbitMq)) {
					json=(JsonObject) jsonParser.parse(serviceResponse);
					version="RabbitMq version "+json.get("rabbitmq_version").getAsString() + "\n Erlang version "+ json.get("erlang_version").getAsString();
				}else if(serviceType.equalsIgnoreCase(ServiceStatusConstants.ES)) { //0
					json=(JsonObject) jsonParser.parse(serviceResponse);
					JsonObject versionElasticsearch=(JsonObject) json.get("version");
					if(versionElasticsearch!=null) {
						version=versionElasticsearch.get("number").getAsString();
					}
				}else if(serviceType.equalsIgnoreCase(ServiceStatusConstants.PgSQL)) {
					PostgresMetadataHandler pgdbHandler =new PostgresMetadataHandler();
					version =pgdbHandler.getPostgresDBVersion();
				}
				returnResponse= buildSuccessResponse(strResponse, hostEndPoint, displayType,version);
			}else {
				strResponse="Response not received from service "+apiUrl;
				returnResponse= buildFailureResponse(strResponse, hostEndPoint, displayType,version);
			}
		} catch (Exception e) {
			log.error("Error while capturing health check at "+apiUrl,e);
			strResponse = "Error while capturing health check at "+apiUrl;
			returnResponse= buildFailureResponse(strResponse, hostEndPoint, displayType,version);
		}
		return returnResponse;
	}

	private JsonObject getVersionDetails(String fileName, String hostEndPoint, String type) throws IOException {

		String version ="";
		String strResponse  = "";
		if(version.equalsIgnoreCase("") ) {
			version=HealthStatus.class.getPackage().getSpecificationVersion();
			strResponse = "Version captured as "+version;
			return buildSuccessResponse(strResponse, hostEndPoint, type,version);	
		}else {
			strResponse = "Error while capturing PlatformService (version "+version+") health check at "+hostEndPoint;
			return buildFailureResponse(strResponse, hostEndPoint, type,version);
		}
	}

	private JsonObject buildSuccessResponse(String message, String apiUrl, String type,String version) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty(HOST_ENDPOINT, apiUrl);
		jsonResponse.addProperty(ServiceStatusConstants.type, type);
		jsonResponse.addProperty(VERSION,version);
		return jsonResponse;
	}

	private JsonObject buildFailureResponse(String message, String apiUrl, String type,String version) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty(HOST_ENDPOINT, apiUrl);
		jsonResponse.addProperty(ServiceStatusConstants.type, type);
		jsonResponse.addProperty(VERSION,version);
		return jsonResponse;
	}

	private JsonObject getComponentStatus(String serviceType, String apiUrl) {
		String successResponse="";
		String version="";
		String status="";
		JsonObject returnObject=null;
		GraphResponse graphResponse;
		String serviceResponse="";
		ElasticSearchDBHandler apiCallElasticsearch =new ElasticSearchDBHandler();
		try {
			if(serviceType.equalsIgnoreCase("PlatformEngine")) {
				graphResponse = loadHealthData("HEALTH:ENGINE",serviceType,"");
				if(graphResponse !=null ) {
					if(graphResponse.getNodes().size() > 0 ) {
						successResponse=graphResponse.getNodes().get(0).getPropertyMap().get("message");;
						version=graphResponse.getNodes().get(0).getPropertyMap().get("version");
						status=graphResponse.getNodes().get(0).getPropertyMap().get("status");
						if(status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
							returnObject=buildSuccessResponse(successResponse.toString(), "-", ServiceStatusConstants.Service,version);
						}else {
							returnObject=buildFailureResponse(successResponse.toString(), "-", ServiceStatusConstants.Service,version);
						}
					}else {
						successResponse="Node list is empty in response not received from Neo4j";
						returnObject=buildFailureResponse(successResponse.toString(), "-", ServiceStatusConstants.Service,version);
					}
				}else {
					successResponse="Response not received from Neo4j";
					returnObject=buildFailureResponse(successResponse.toString(), "-", ServiceStatusConstants.Service,version);
				}
			}else if(serviceType.equalsIgnoreCase("PlatformInsightSpark")) {
				try {
					serviceResponse=apiCallElasticsearch.search(apiUrl);
				} catch (Exception e) {
					successResponse="Unable to connect URL "+apiUrl;
					status=PlatformServiceConstants.FAILURE;
				}
				log.info(" PlatformInsightSpark service response "+serviceResponse);
				graphResponse = loadHealthData("HEALTH:INSIGHTS",serviceType,"");
				log.debug(" graphResponse message arg 0  "+graphResponse);
				if(graphResponse !=null ) {
					if(graphResponse.getNodes().size() > 0 ) {
						successResponse=graphResponse.getNodes().get(0).getPropertyMap().get("message");;
						version=graphResponse.getNodes().get(0).getPropertyMap().get("version");
						status=graphResponse.getNodes().get(0).getPropertyMap().get("status");
					}else {
						successResponse="Node list is empty in response not received from Neo4j";
						status=PlatformServiceConstants.FAILURE;
					}
				}else {
					successResponse="Response not received from Neo4j";
					status=PlatformServiceConstants.FAILURE;
				}

				if(status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS) && !("").equalsIgnoreCase(serviceResponse)) {
					returnObject=buildSuccessResponse(successResponse.toString(), apiUrl, ServiceStatusConstants.Service,version);
				}else {
					returnObject=buildFailureResponse(successResponse.toString(), apiUrl, ServiceStatusConstants.Service,version);
				}

			}else if(serviceType.equalsIgnoreCase("Agents")) {
				graphResponse = loadHealthData("HEALTH:LATEST",serviceType,"");
				if(graphResponse !=null ) {
					if(graphResponse.getNodes().size() > 0 ) {
						status=PlatformServiceConstants.SUCCESS;
					}else {
						successResponse="Node list is empty in response not received from Neo4j";
						status=PlatformServiceConstants.FAILURE;
					}
				}else {
					successResponse="Response not received from Neo4j";
					status=PlatformServiceConstants.FAILURE;
				}
				log.error("message "+successResponse);
				returnObject=buildAgentResponse(status,successResponse, graphResponse);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnObject;
	}

	private GraphResponse loadHealthData(String label, String type, String agentId) {

		int limitOfRow=1;
		String query ="";
		if(type.equalsIgnoreCase("Agents")) {
			limitOfRow=10;
		}
		if(agentId.equalsIgnoreCase("")) {
			query ="MATCH (n:"+label+") where n.inSightsTime IS NOT NULL RETURN n order by n.inSightsTime DESC LIMIT "+limitOfRow;
		}else if(!agentId.equalsIgnoreCase("")){
			query ="MATCH (n:"+label+") where n.inSightsTime IS NOT NULL and n.agentId ='"+agentId+"' RETURN n order by n.inSightsTime DESC LIMIT "+limitOfRow;
		}
		log.info("query  ====== "+query);
		GraphResponse graphResponse =null;
		try { 
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			graphResponse= dbHandler.executeCypherQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			graphResponse=new GraphResponse();
		}
		return graphResponse;
	}

	private JsonObject buildAgentResponse(String status ,String message, GraphResponse graphResponse) {
		log.error(" message in buildAgentResponse debug "+graphResponse.getNodes().size());
		String toolcategory=""; 
		String toolName="";
		String insightTimeX="";
		String agentstatus="";
		String agentId="";
		JsonObject jsonResponse = new JsonObject();
		JsonArray agentNode = new JsonArray();
		if(status.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {

			jsonResponse.addProperty(ServiceStatusConstants.type, ServiceStatusConstants.Agents);

			Iterator<NodeData> agentnodeIterator=graphResponse.getNodes().iterator();
			while(agentnodeIterator.hasNext()) {
				NodeData node =agentnodeIterator.next();
				insightTimeX=node.getPropertyMap().get("inSightsTimeX");;
				message=node.getPropertyMap().get("message");;
				toolcategory=node.getPropertyMap().get("category");;
				toolName=node.getPropertyMap().get("toolName");
				if(node.getPropertyMap().containsKey("agentId")) {
					agentId=node.getPropertyMap().get("agentId");	
				}else {
					agentId="";
				}
				agentstatus=node.getPropertyMap().get("status");
				insightTimeX=node.getPropertyMap().get("inSightsTimeX");;
				JsonObject jsonResponse2 = new JsonObject();
				jsonResponse2.addProperty("inSightsTimeX", insightTimeX);
				jsonResponse2.addProperty("toolName", toolName);
				jsonResponse2.addProperty("agentId", agentId);
				jsonResponse2.addProperty("inSightsTimeX", insightTimeX);
				jsonResponse2.addProperty(PlatformServiceConstants.STATUS, agentstatus);
				jsonResponse2.addProperty(PlatformServiceConstants.MESSAGE, message);
				jsonResponse2.addProperty("category", toolcategory);
				agentNode.add(jsonResponse2);
			}
			jsonResponse.add("agentNodes", agentNode);
		}else {
			jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
			jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
			jsonResponse.addProperty("category", toolcategory);
			jsonResponse.addProperty(ServiceStatusConstants.type, ServiceStatusConstants.Agents);
			jsonResponse.addProperty("toolName", toolName);
			jsonResponse.addProperty("inSightsTimeX", insightTimeX);
			jsonResponse.addProperty(VERSION,"");
			jsonResponse.add("agentNodes", agentNode);
		}

		return jsonResponse;
	}



}
