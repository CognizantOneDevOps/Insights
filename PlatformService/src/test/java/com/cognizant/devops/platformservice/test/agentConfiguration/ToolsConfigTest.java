package com.cognizant.devops.platformservice.test.agentConfiguration;
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
package com.cognizant.devops.platformservice.rest.agentConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;



import org.junit.Test;

import com.cognizant.devops.platformservice.rest.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.rest.utility.ServiceTestUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ToolsConfigTest  {
	
  
    
    @Test //Test to check the reading of Category and Tool name
    public void testLoadConfig() {//successful API call
    	String requestPath = "/toolsConfig/read";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");  
    	assertEquals("Should return SUCCESS(API call)","success",jsonObj.get("status").getAsString());
    	assertEquals("Array is empty. It should be inputed by user",0,jsonDataArry.size());//Checks that Array is empty
    }
    
    @Test //Test to check the reading of Category and Tool name and its data
    public void testLoadAllToolsConfig() {//successful API call
    	String requestPath = "/toolsConfig/readAll";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");  
    	assertEquals("Should return SUCCESS(API call) with data","success",jsonObj.get("status").getAsString());
    	assertNotNull(jsonDataArry);//Checks that "data" is not NULL  
    	}

    @Test //Test to check all the Tool Configurations
    public void testGetToolsConfig() {//successful API call
    	String requestPath = "/toolsConfig/toolsConfig";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	JsonObject obj = (JsonObject) jsonObj.get("data");  
    	assertEquals("Should return SUCCESS(API call) with data","success",jsonObj.get("status").getAsString());
    	assertNotNull(obj);//Checks that "data" is not NULL  
    	}
    

    @Test //Test to check the downloading, when Category, Tool name and AgentID is entered correct and configuration download data is present
    public void testDownloadToolsConfig() {//successful API call
    	String requestPath = "/toolsConfig/download?category=CI&toolName=JENKINS&agentId=1";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	assertEquals("Should return Access Token","Access Token",jsonObj.get("selectedAuthMtd").getAsString());
    	assertEquals("Should return Run Schedule ID","30",jsonObj.get("runSchedule").getAsString());
    	assertNotNull(jsonObj.get("runSchedule"));
    	}
    
    @Test //Test to check the downloading, when Category, Tool name and AgentID is entered correct but no download data is present
    public void testDownloadToolsConfigNullData() {//successful API call
    	String requestPath = "/toolsConfig/download?category=CI&toolName=Bamboo&agentId=1";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        assertEquals("Should return failure with Internal Error","failure",jsonObj.get("status").getAsString());	       
    	}
    
    

    @Test //Test to check the downloading, when Category or Tool name or AgentID is entered correct. It throws Server Error 
    public void testDownloadToolsConfigWrongData() {//successful API call
    	String requestPath = "/toolsConfig/download?category=xyz&toolName=JENKINS&agentId=1";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        assertEquals("Should return failure with Internal Error","failure",jsonObj.get("status").getAsString());	       
    	}
    
    //Accessing GET through POST
    
    @Test //Test to check the reading of Category and Tool name using POST call
    public void testLoadConfigPost() {//successful API call
    	String requestPath = "/toolsConfig/read";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	       
    }
    
    @Test //Test to check the reading of Category and Tool name and its data using POST call
    public void testLoadAllToolsConfigPost() {//successful API call
    	String requestPath = "/toolsConfig/readAll";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	       
    	}

    @Test //Test to check all the Tool Configurations using POST call
    public void testGetToolsConfigPost() {//successful API call
    	String requestPath = "/toolsConfig/toolsConfig";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	       
  
    	}
    

    @Test //Test to check the downloading, when Category, Tool name and AgentID is entered correct and configuration download data is present using POST call
    public void testDownloadToolsConfigPost() {//successful API call
    	String requestPath = "/toolsConfig/download?category=CI&toolName=JENKINS&agentId=1";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	       

    	}
    
    @Test //Test to check the downloading, when Category, Tool name and AgentID is entered correct but no download data is present using POST call
    public void testDownloadToolsConfigNullDataPost() {//successful API call
    	String requestPath = "/toolsConfig/download?category=CI&toolName=Bamboo&agentId=1";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	       
    	}
    
    

    @Test //Test to check the downloading, when Category or Tool name or AgentID is entered correct. It throws Server Error using POST call
    public void testDownloadToolsConfigWrongDataPost() {//successful API call
    	String requestPath = "/toolsConfig/download?category=xyz&toolName=JENKINS&agentId=1";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	       
    	}
}
*/