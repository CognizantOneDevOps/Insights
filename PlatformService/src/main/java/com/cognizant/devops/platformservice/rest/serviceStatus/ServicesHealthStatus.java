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

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.google.gson.JsonObject;
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
		String endPoint = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
		String apiUrl = endPoint+"/api/health";		
		JsonObject postgreStatus = getClientResponse(endPoint, apiUrl);
		servicesHealthStatus.add("PostgreSQL", postgreStatus);
		
		/*PlatformService health check*/
		endPoint = ApplicationConfigProvider.getInstance().getInsightsServiceURL();
		JsonObject platformServStatus = getVersionDetails(PLATFORM_SERVICE_VERSION_FILE, endPoint);
		servicesHealthStatus.add("Platform Service", platformServStatus);
		
		/*Insights Inference health check*/	
		endPoint = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkMasterExecutionEndPoint();
		apiUrl = endPoint+"/jobs";
		JsonObject inferenceServStatus = getClientResponse(endPoint, apiUrl);
		servicesHealthStatus.add("Insights Inference", inferenceServStatus);
		
		/*Neo4j health check*/
		endPoint = ApplicationConfigProvider.getInstance().getGraph().getEndpoint();
		apiUrl = endPoint+"/browser";
		JsonObject neo4jStatus = getClientResponse(endPoint, apiUrl);
		servicesHealthStatus.add("Neo4j", neo4jStatus);
		
		/*Elastic Search health check*/
		endPoint = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint();
		apiUrl = endPoint;
		JsonObject EsStatus = getClientResponse(endPoint, apiUrl);
		servicesHealthStatus.add("Elastic Search", EsStatus);
		
		return servicesHealthStatus;		
	}
	
	private JsonObject getClientResponse(String endPoint, String apiUrl){
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

				return buildSuccessResponse(successResponse, endPoint);

			  } catch (Exception e) {

				  log.error("Error while capturing health check at "+apiUrl,e);


			  }
		String failureResponse = "Error while capturing health check at "+apiUrl;
		return buildFailureResponse(failureResponse, endPoint);
	}
	
	private JsonObject getVersionDetails(String fileName, String endPoint) throws IOException {
		InputStream input = ServicesHealthStatus.class.getClassLoader().getResourceAsStream(fileName);
		try {
			Properties prop = new Properties();
			prop.load(input);
			String successResponse = "";
			if(input != null){
				successResponse = "Version captured as "+prop.getProperty(VERSION);
				return buildSuccessResponse(successResponse, endPoint);
			}
		}finally {
			try {
				if( null != input ){
				   input.close();
				}
			} catch (IOException e) {
				log.error("Error while capturing PlatformService health check at "+endPoint,e);
			}
		}
		String failureResponse = "Error while capturing PlatformService health check at "+endPoint;
		return buildFailureResponse(failureResponse, endPoint);
	}
	
	private JsonObject buildSuccessResponse(String message, String apiUrl) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty(HOST_ENDPOINT, apiUrl);
		return jsonResponse;
	}
	
	private JsonObject buildFailureResponse(String message, String apiUrl) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty(HOST_ENDPOINT, apiUrl);
		return jsonResponse;
	}
}
