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
package com.cognizant.devops.platformservice.rest.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;


import com.cognizant.devops.platformservice.rest.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.rest.utility.ServiceTestUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PlatformMappingDataTest {
	
  
    
    @Test //Succesful API call for Tools
    public void testGetToolsList() {//succesful API call
    	String requestPath = "/mappingdata/tools";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");
        assertEquals("Should return SUCCESS(API call)","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry);  //Data array is not null
    }
    
    @Test //When tool name= JENKINS is correct for Tools Category
    public void testGetToolsCatList() {//Tool name is correct
    	String requestPath = "/mappingdata/toolsCategory?toolName=JENKINS";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");
        assertEquals("Should return SUCCESS with data","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry);  //Data array is not null
    }
    
    @Test //When tool name= JENKINS1 is incorrect for Tools Category
    public void testGetToolsCatListNullData() {//Tool name is wrong
    	String requestPath = "/mappingdata/toolsCategory?toolName=JENKINS1";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");  
        assertEquals("Should return SUCCESS(API call)","success",jsonObj.get("status").getAsString());
    	assertEquals("Should return SUCCESS without data",0,jsonDataArry.size()); //Data array is null
    	
    }
    
    @Test //When tool name= JENKINS is correct for Tools Field
    public void testLoadToolsField() {//Tool name is correct
    	String requestPath = "/mappingdata/toolsField?toolName=JENKINS";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj= (JsonObject) jsonObj.get("data"); //Data elements are fetched
        JsonArray jsonDataArry = (JsonArray) obj.get("nodes"); //Nodes Array is fetched
        assertEquals("Should return SUCCESS","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry); //Nodes array is not null
    }
    
    @Test //When tool name= JENKINS1 is incorrect for Tools Field
    public void testLoadToolsFieldNullData() {//for wrong tool name
    	String requestPath = "/mappingdata/toolsField?toolName=JENKINS1";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj= (JsonObject) jsonObj.get("data");        
        JsonArray jsonDataArry = (JsonArray) obj.get("nodes");
        assertEquals("Should return SUCCESS(API call)","success",jsonObj.get("status").getAsString());
        assertEquals("Should return SUCCESS without data",0,jsonDataArry.size()); //Nodes array is not null
        
        }
    
    @Test //When tool name= JENKINS is correct for Tools Field Values
    public void testLoadToolsFieldValue() {//Tool name and Field value is correct
    	String requestPath = "/mappingdata/toolsFieldValue?toolName=JENKINS&fieldName=jen_SCMRemoteUrl";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj= (JsonObject) jsonObj.get("data"); //Data elements are fetched
        JsonArray jsonDataArry = (JsonArray) obj.get("nodes"); //Nodes Array is fetched
        assertEquals("Should return SUCCESS","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry); //Nodes array is not null
    }
        
    @Test //When tool name= JENKINS1 is incorrect for Tools Field Values
    public void testLoadToolsFieldValueNullData() {//Tool name is incorrect and Field value is correct
    	String requestPath = "/mappingdata/toolsFieldValue?toolName=JENKINS1&fieldName=jen_SCMRemoteUrl";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj= (JsonObject) jsonObj.get("data");
        JsonArray jsonDataArry = (JsonArray) obj.get("nodes");
    	assertEquals("Should return SUCCESS with NULL data",0,jsonDataArry.size()); //Nodes array is null
    }
    
    //Accessing GET through POST call
    
    @Test //Succesful API call for Tools using POST
    public void testGetToolsListPost() {//succesful API call
    	String requestPath = "/mappingdata/tools";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //When tool name= JENKINS is correct for Tools Category using POST
    public void testGetToolsCatListPost() {//Tool name is correct
    	String requestPath = "/mappingdata/toolsCategory?toolName=JENKINS";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //When tool name= JENKINS is correct for Tools Field using POST
    public void testLoadToolsFieldPost() {//Tool name is correct
    	String requestPath = "/mappingdata/toolsField?toolName=JENKINS";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //When tool name= JENKINS is correct for Tools Field Values using POST
    public void testLoadToolsFieldValuePost() {//Tool name and Field value is correct
    	String requestPath = "/mappingdata/toolsFieldValue?toolName=JENKINS&fieldName=jen_SCMRemoteUrl";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
}
