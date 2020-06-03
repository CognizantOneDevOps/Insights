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
package com.cognizant.devops.platformservice.test.dashboard;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


import org.junit.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.test.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.test.utility.ServiceTestUtilities;
import com.google.gson.JsonObject;

public class CustomDashboardServiceTest {
	
     
    @Test //When portfolio=RELEASE_MANAGER is passed
    public void testGetCustomDashboard() throws InsightsCustomException {
    	String requestPath = "/dashboard/getCustomDashboard?portfolio=RELEASE_MANAGER";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	assertEquals("Should return SUCCESS","success",jsonObj.get("status").getAsString());
    }
    
    @Test //When incorrect portfolio=RELEASE_MANAGER1 is passed
    public void testGetCustomDashboardNull() throws InsightsCustomException {
    	String requestPath = "/dashboard/getCustomDashboard?portfolio=RELEASE_MANAGER1";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	assertNotEquals("Should be a FAILURE","success",jsonObj.get("status").getAsString());
    }
    
    @Test //When portfolio=DELIVERY_MANAGER is passed
    public void test1GetCustomDashboard() throws InsightsCustomException {
    	String requestPath = "/dashboard/getCustomDashboard?portfolio=DELIVERY_MANAGER";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	assertEquals("Should return SUCCESS","success",jsonObj.get("status").getAsString());
    }
    
    
    
    @Test //When incorrect portfolio=DELIVERY_MANAGER1 is passed
    public void test1GetCustomDashboardNull() throws InsightsCustomException {
    	String requestPath = "/dashboard/getCustomDashboard?portfolio=DELIVERY_MANAGER1";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	assertNotEquals("Should be a FAILURE","success",jsonObj.get("status").getAsString());
    }
    
   
    //Accessing GET through POST call
    
    
    @Test //When portfolio=RELEASE_MANAGER is passed in a POST call
    public void testGetCustomDashboardPost() throws InsightsCustomException {
    	String requestPath = "/dashboard/getCustomDashboard?portfolio=RELEASE_MANAGER";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
    	assertEquals("Should return FAILURE","failure",jsonObj.get("status").getAsString());
    }
    
    @Test //When incorrect portfolio=RELEASE_MANAGER1 is passed in a POST call
    public void testGetCustomDashboardNullPost() throws InsightsCustomException {
    	String requestPath = "/dashboard/getCustomDashboard?portfolio=RELEASE_MANAGER1";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return FAILURE","failure",jsonObj.get("status").getAsString());    }
    
    @Test //When portfolio=DELIVERY_MANAGER is passed in a POST call
    public void test1GetCustomDashboardPost() throws InsightsCustomException {
    	String requestPath = "/dashboard/getCustomDashboard?portfolio=DELIVERY_MANAGER";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return FAILURE","failure",jsonObj.get("status").getAsString());    }
    
    
    
    @Test //When incorrect portfolio=DELIVERY_MANAGER1 is passed in a POST call
    public void test1GetCustomDashboardNullPost() throws InsightsCustomException {
    	String requestPath = "/dashboard/getCustomDashboard?portfolio=DELIVERY_MANAGER1";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return FAILURE","failure",jsonObj.get("status").getAsString());    }
    
}
