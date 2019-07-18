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
 ******************************************************************************//*
package com.cognizant.devops.platformservice.rest.health;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


import org.junit.Test;

import com.cognizant.devops.platformservice.rest.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.rest.utility.ServiceTestUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class AgentHealthServiceTest  {
	
  
    
    @Test //Test to load all Agents Health
    public void testLoadAllAgentsHealth() {//succesful API call
    	String requestPath = "/agent/globalHealth";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj= (JsonObject) jsonObj.get("data");  
        JsonArray jsonDataArry = (JsonArray) obj.get("nodes");  
    	assertEquals("Should return SUCCESS(API call)","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry);//Returns list of nodes
    }
    

    @Test //Test to check Health details of Individual Agent
    public void testLoadAgentsHealth() {//succesful API call
    	String requestPath = "/agent/health?category=CI&tool=JENKINS";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj= (JsonObject) jsonObj.get("data");  
        JsonArray jsonDataArry = (JsonArray) obj.get("nodes");  
    	assertEquals("Should return SUCCESS(API call)","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry);//Returns list of nodes
    }
    
    @Test //Test to check Health details of Individual Agent when Category or Tool Name is entered wrong
    public void testLoadAgentsHealthWrong() {//succesful API call
    	String requestPath = "/agent/health?category=xyz&tool=JENKINS";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj= (JsonObject) jsonObj.get("data");  
        JsonArray jsonDataArry = (JsonArray) obj.get("nodes");  
    	assertEquals("Should return SUCCESS(API call)","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry);//Returns list of nodes
    }
    
    //Accessing GET through POST call
    

    @Test //Test to load all Agents Health using POST
    public void testLoadAllAgentsHealthPost() {//succesful API call
    	String requestPath = "/agent/globalHealth";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    

    @Test //Test to check Health details of Individual Agent using POST
    public void testLoadAgentsHealthPost() {//succesful API call
    	String requestPath = "/agent/health?category=CI&tool=JENKINS";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //Test to check Health details of Individual Agent when Category or Tool Name is entered wrong using POST
    public void testLoadAgentsHealthWrongPost() {//succesful API call
    	String requestPath = "/agent/health?category=xyz&tool=JENKINS";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }

}
*/