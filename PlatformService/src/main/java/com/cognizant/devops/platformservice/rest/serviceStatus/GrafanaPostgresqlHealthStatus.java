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

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@RestController
@RequestMapping("/admin/grafanaPostgreHealth")
public class GrafanaPostgresqlHealthStatus {
	static Logger log = Logger.getLogger(GrafanaPostgresqlHealthStatus.class.getName());

	@RequestMapping(value = "/getStatus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getHealthStatus(){
		String apiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint()+"/api/health";
				
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
				
				String output = response.getEntity(String.class);

				return PlatformServiceUtil.buildSuccessResponseWithData(new JsonParser().parse(output));

			  } catch (Exception e) {

				  log.error("Error while capturing Grafana-PostgreSQL health check",e);


			  }
		String failureResponse = "Error while capturing Grafana-PostgreSQL health check";
		return PlatformServiceUtil.buildFailureResponse(failureResponse);		
	}
	
}
