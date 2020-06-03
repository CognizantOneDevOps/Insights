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
package com.cognizant.devops.platformservice.test.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertNotNull;


import org.junit.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.test.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.test.utility.ServiceTestUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ToolsConfigurationServiceTest  {
	
  
    
    @Test //When a READ call is implemented
    public void testLoadToolsConfig() throws InsightsCustomException {//succesful API call
    	String requestPath = "/tools/read?category=SCM&toolName=GIT";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj = (JsonObject) jsonObj.get("data"); 
        JsonArray jsonDataArry = (JsonArray) obj.get("data");  
    	assertEquals("Should return SUCCESS(API call)","success",jsonObj.get("status").getAsString());
    	assertEquals("Should return SUCCESS without data",0,jsonDataArry.size());
    }
    
    @Test //Successful test to download data of a ToolsConfig
    public void testDownloadToolsConfig() throws InsightsCustomException {//succesful API call
    	String requestPath = "/tools/download?category=SCM&tool=GIT";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null); 
    	assertTrue(jsonObj.entrySet().isEmpty());
    }
    
    @Test //Successful ToolsConfig details test
    public void testGetToolsConfig() throws InsightsCustomException {//succesful API call
    	String requestPath = "/tools/toolsConfig";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj = (JsonObject) jsonObj.get("data"); 
        JsonObject obj2 = (JsonObject) obj.get("SCM");  
    	assertEquals("Should return SUCCESS(API call) with data","success",jsonObj.get("status").getAsString());
     	assertNotNull(obj2);//Value of SCM is not NULL
    }
    
    //Accessing GET through POST call
    
    @Test //When a READ call is implemented
    public void testLoadToolsConfigPost() throws InsightsCustomException {//succesful API call
    	String requestPath = "/tools/read?category=SCM&toolName=GIT";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //Successful test to download data of a ToolsConfig
    public void testDownloadToolsConfigPost() throws InsightsCustomException {//succesful API call
    	String requestPath = "/tools/download?category=SCM&tool=GIT";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //Successful ToolsConfig details test
    public void testGetToolsConfigPost() throws InsightsCustomException {//succesful API call
    	String requestPath = "/tools/toolsConfig";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
}
