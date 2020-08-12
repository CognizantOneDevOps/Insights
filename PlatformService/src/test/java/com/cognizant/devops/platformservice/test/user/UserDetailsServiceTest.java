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
package com.cognizant.devops.platformservice.test.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.test.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.test.utility.ServiceTestUtilities;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UserDetailsServiceTest {
	
     
    @Test //To Test whether the user exists or not
    public void testSearchUserResponseStatus() throws InsightsCustomException {
    	String requestPath = "/user/search?query=205562";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	assertEquals("Should return SUCCESS",ServiceTestConstants.SUCCESS_RESPONSE,jsonObj.get("status").getAsString());
    }
    
    @Test// To test whether it returns correct employee ID or not
    public void testSearchUserResponseData() throws InsightsCustomException {
    	String requestPath = "/user/search?query=205562";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	JsonObject jsonDataObj = (JsonObject) jsonObj.get("data");
    	assertEquals("Should return correct employee id","205562",jsonDataObj.get("employeeId").getAsString());
    }
    
    @Test // Test when ID is incorrect
    public void testSearchUserResponseNoLDAPUser() throws InsightsCustomException {
    	String requestPath = "/user/search?query=205562123";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	assertEquals("Should return User not found in LDAP","User not found in LDAP",jsonObj.get("error").getAsString());
    }
    
    @Test // To check Invalid Authorization
    public void testInvalidAuthorization() throws InsightsCustomException {
    	GrafanaHandler grafanaHandler = new GrafanaHandler();
    	String requestPath = "/user/search?query=205562";
    	String requestUrl = ServiceTestConstants.BASE_URI + requestPath;
    	JsonObject jsonObj = null;
    	
    	Map<String, String> requestHeaders = new HashMap<String,String>();
    	requestHeaders.put("Authorization", "test");
    	requestHeaders.put("Accept", MediaType.APPLICATION_JSON);
    	String response =  grafanaHandler.grafanaGet(requestUrl, requestHeaders);
    	
    	if( null != response ){
    		String jsonResp = response;
			//System.out.println(jsonResp);
    		JsonParser jsonParser = new JsonParser();
    		jsonObj = (JsonObject)jsonParser.parse(jsonResp);
    	}

    	assertNotNull("Should return error",jsonObj.get("error"));
    }
    
//Accessing GET through POST call
    
    @Test //To Test whether the user exists or not using POST call
    public void testSearchUserResponseStatusPost() throws InsightsCustomException {
    	String requestPath = "/user/search?query=205562";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test// To test whether it returns correct employee ID or not using POST call
    public void testSearchUserResponseDataPost() throws InsightsCustomException {
    	String requestPath = "/user/search?query=205562";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test // Test when ID is incorrect using POST call
    public void testSearchUserResponseNoLDAPUserPost() throws InsightsCustomException {
    	String requestPath = "/user/search?query=205562123";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
	
}
