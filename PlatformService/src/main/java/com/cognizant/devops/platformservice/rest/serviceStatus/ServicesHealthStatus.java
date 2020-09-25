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
 package com.cognizant.devops.platformservice.rest.serviceStatus;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.util.SystemStatus;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformdal.dal.PostgresMetadataHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/ServicesHealthStatus")
public class ServicesHealthStatus {
	
	private static final String VERSION = "version";
	private static final String HOST_ENDPOINT = "endPoint";
	private static final String PLATFORM_SERVICE_VERSION_FILE = "version.properties";

	static Logger log = LogManager.getLogger(ServicesHealthStatus.class.getName());
	@GetMapping(value = "/getStatus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
		
		log.debug(" servicesHealthStatus "+servicesHealthStatus.toString());
		return servicesHealthStatus;		
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
			version=ServicesHealthStatus.class.getPackage().getSpecificationVersion();
			log.info("message version =================== "+version);
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
		String serviceResponse=null;
		ElasticSearchDBHandler apiCallElasticsearch =new ElasticSearchDBHandler();
		try {
			if(serviceType.equalsIgnoreCase("PlatformEngine")) {
				graphResponse = loadHealthData("HEALTH:ENGINE");
				log.debug(" graphResponse message arg 0  "+graphResponse);
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
				serviceResponse=apiCallElasticsearch.search(apiUrl);
				log.info(" PlatformInsightSpark service response "+serviceResponse);
				graphResponse = loadHealthData("HEALTH:INSIGHTS");
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
				
			}
		} catch (Exception e) {
			log.error("Error while getting component status "+e.getMessage());
		}
		return returnObject;
	}
	
	private GraphResponse loadHealthData(String label) {
		String query = "MATCH (n:"+label+") where n.inSightsTime IS NOT NULL RETURN n order by n.inSightsTime DESC LIMIT 1";
		GraphResponse response =null;
		try { 
			GraphDBHandler dbHandler = new GraphDBHandler();
			response= dbHandler.executeCypherQuery(query);
		} catch (Exception e) {
			log.error(e);
			response=new GraphResponse();
		}
		return response;
	}
	
}