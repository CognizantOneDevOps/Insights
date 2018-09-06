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
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@RestController
@RequestMapping("/ServicesHealthStatus")
public class ServicesHealthStatus {
	
	private static final String VERSION = "version";
	private static final String HOST_ENDPOINT = "endPoint";
	private static final String PLATFORM_SERVICE_VERSION_FILE = "version.properties";

	static Logger log = Logger.getLogger(ServicesHealthStatus.class.getName());
	@RequestMapping(value = "/getStatus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public JsonObject getHealthStatus() throws IOException{
		JsonObject servicesHealthStatus = new JsonObject();
		
		//ApplicationConfigCache.loadConfigCache();
		/*PostgreSQL health check*/
		String hostEndPoint = ServiceStatusConstants.POSTGRESQL_HOST;
		String apiUrl = hostEndPoint;		
		JsonObject postgreStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,"");
		servicesHealthStatus.add(ServiceStatusConstants.PgSQL, postgreStatus);
		
		/*PlatformService health check*/
		hostEndPoint = ServiceStatusConstants.PLATFORM_SERVICE_HOST;
		JsonObject platformServStatus = getVersionDetails(PLATFORM_SERVICE_VERSION_FILE, hostEndPoint, ServiceStatusConstants.Service);
		servicesHealthStatus.add(ServiceStatusConstants.PlatformService, platformServStatus);
		
		/*Insights Inference health check*/	
		hostEndPoint = ServiceStatusConstants.INSIGHTS_INFERENCE_MASTER_HOST;
		apiUrl = hostEndPoint+"/jobs";
		JsonObject inferenceServStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.Service,"");
		servicesHealthStatus.add(ServiceStatusConstants.InsightsInference, inferenceServStatus);
		
		/*Neo4j health check*/
		hostEndPoint = ServiceStatusConstants.NEO4J_HOST;
		apiUrl = hostEndPoint+"/browser";
		JsonObject neo4jStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,"");
		servicesHealthStatus.add(ServiceStatusConstants.Neo4j, neo4jStatus);
		
		/*Elastic Search health check*/
		hostEndPoint = ServiceStatusConstants.ES_HOST;
		apiUrl = hostEndPoint;
		JsonObject EsStatus = getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,"");
		servicesHealthStatus.add(ServiceStatusConstants.ES, EsStatus);
		
		/*Demon Agent Health Check */
		hostEndPoint = ServiceStatusConstants.DemonAgent;
		apiUrl = hostEndPoint;
		JsonParser parser = new JsonParser();
		String jsonString="{\"DemonAgent\":{\"status\":\"success\",\"message\":\"Response successfully recieved from Demon Agent \",\"endPoint\":\"\",\"type\":\"Database\"}}";
		JsonObject demonStatus = (JsonObject) parser.parse(jsonString);
		servicesHealthStatus.add(ServiceStatusConstants.DemonAgent, demonStatus);
		
		log.info(" servicesHealthStatus "+servicesHealthStatus.toString());
		return servicesHealthStatus;		
	}
	
	private JsonObject getClientResponse(String hostEndPoint, String apiUrl, String type,String version){
		try {
			Client client = Client.create();
			WebResource webResource = client
				   .resource(apiUrl);

				ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON.toString())
		                   .get(ClientResponse.class);

				if (response.getStatus() != 200) {
				   throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatus());
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
	
	private JsonObject getVersionDetails(String fileName, String hostEndPoint, String type) throws IOException {
		InputStream input = ServicesHealthStatus.class.getClassLoader().getResourceAsStream(fileName);
		String version=""; 
		try {
			Properties prop = new Properties();
			prop.load(input);
			String successResponse = "";
			version=prop.getProperty(VERSION);
			if(input != null){
				successResponse = "Version captured as "+version;
				return buildSuccessResponse(successResponse, hostEndPoint, type,version);
			}
		}finally {
			try {
				if( null != input ){
				   input.close();
				}
			} catch (IOException e) {
				log.error("Error while capturing PlatformService health check at "+hostEndPoint,e);
			}
		}
		String failureResponse = "Error while capturing PlatformService health check at "+hostEndPoint;
		return buildFailureResponse(failureResponse, hostEndPoint, type,version);
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
	
}
