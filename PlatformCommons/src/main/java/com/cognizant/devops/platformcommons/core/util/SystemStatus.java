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
package com.cognizant.devops.platformcommons.core.util;

import java.util.List;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import sun.misc.BASE64Encoder;

public  class SystemStatus {
	private static Logger log = Logger.getLogger(SystemStatus.class.getName());
	
	public static JsonObject addSystemInformationInNeo4j(String version,List<JsonObject> dataList,List<String> labels) {
		Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
		JsonObject response = null;
		try {
			String queryLabel = "";
			for (String label : labels) {
				if (label != null && label.trim().length() > 0) {
					queryLabel += ":" + label;
				}
			}
	
			String cypherQuery = "CREATE (n" + queryLabel + " {props} ) return count(n)";
	
			response = graphDBHandler.executeQueryWithData(cypherQuery,dataList);
			//log.info("  GraphDB response created " + response);
		
		} catch (Exception e) {
			log.error(" Neo4j Node not created "+e.getMessage());
		}
		
		return response;
	}
	
	public static String jerseyGetClientWithAuthentication(String url, String name, String password,String authtoken) {
		String output;
		String authStringEnc;
		ClientResponse response = null;
        try {
        	if(authtoken==null) {
        		String authString = name + ":" + password;
    			authStringEnc= new BASE64Encoder().encode(authString.getBytes());
    		}else {
    			authStringEnc=authtoken;
    		}
			Client restClient = Client.create();
			WebResource webResource = restClient.resource(url);
			response= webResource.accept("application/json")
			                                 .header("Authorization", "Basic " + authStringEnc)
			                                 .get(ClientResponse.class);
			if(response.getStatus() != 200){
				throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());
			}else {
				output= response.getEntity(String.class);
			}
		} catch (Exception e) {
			log.error(" error while getting jerseyGetClientWithAuthentication "+e.getMessage());
			throw new RuntimeException("Failed : error while getting jerseyGetClientWithAuthentication : "+ e.getMessage());
		}finally {
			if(response!=null) {
				response.close();
			}
		}
        return output;
	}
}
