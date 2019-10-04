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
package com.cognizant.devops.platformservice.test.grafana;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformservice.test.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.test.utility.ServiceTestUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class UserManagementServiceTest extends AbstractTestNGSpringContextTests {
	
	//@Autowired
	//ServiceTestUtilities serviceTestUtilities;
	
    @Test //When all the users are fetched successfully
    public void testGetAllUsers() {//succesful API call
    	String requestPath = "/userMgmt/getAllUsers";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");  
    	Assert.assertEquals("Should return SUCCESS(API call) with Users data","success",jsonObj.get("status").getAsString());
    	Assert.assertNotNull(jsonDataArry);//Returns list of Users(NOT NULL)
    }
    
    @Test //When all the Organization names are fetched successfully
    public void testGetOrgs() {//succesful API call
    	String requestPath = "/userMgmt/getOrgs";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");  
        Assert.assertEquals("Should return SUCCESS(API call) with Organizations","success",jsonObj.get("status").getAsString());
        Assert.assertNotNull(jsonDataArry);//Returns list of Organizations (NOT NULL)
    }
    
    /*
    @Test //When user details are fetched successfully(Returns User details)
    public void testGetUser() {//succesful API call
    	String requestPath = "/userMgmt/getUser";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        //JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");  
    	assertEquals("Should return SUCCESS(API call) with user details","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonObj.get("data"));//Returns tests of User (NOT NULL)
    }
    
    @Test //Returns the name of Organizations in which above user is added and its role 
    public void testGetCurrentUserOrgs() {//succesful API call
    	String requestPath = "/userMgmt/getCurrentUserOrgs";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");  
    	assertEquals("Should return SUCCESS(API call) with data","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry);//Returns list of Current User Organizations (NOT NULL)
    }
    
    //Accessing GET through POST call
    
    @Test //When all the users are fetched successfully
    public void testGetAllUsersPost() {//succesful API call
    	String requestPath = "/userMgmt/getAllUsers";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //When all the Organization names are fetched successfully
    public void testGetOrgsPost() {//succesful API call
    	String requestPath = "/userMgmt/getOrgs";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    
    @Test //When user details are fetched successfully(Returns User details)
    public void testGetUserPost() {//succesful API call
    	String requestPath = "/userMgmt/getUser";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //Returns the name of Organizations in which above user is added and its role 
    public void testGetCurrentUserOrgsPost() {//succesful API call
    	String requestPath = "/userMgmt/getCurrentUserOrgs";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }*/
}
