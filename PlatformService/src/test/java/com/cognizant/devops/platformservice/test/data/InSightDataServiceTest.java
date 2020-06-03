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
package com.cognizant.devops.platformservice.test.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.test.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.test.utility.ServiceTestUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class InSightDataServiceTest {
	
     
    @Test //When parameter, orgId=1 is valid
    public void testFetchPrjtMappingByOrgId() throws InsightsCustomException {
    	String requestPath = "/data/fetchProjectMappingByOrgId?orgId=1";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");
        assertEquals("Should return SUCCESS","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry);//Data array is not null
    }
    @Test //When parameter, orgId=1000 is invalid
    public void testFetchPrjtMappingByOrgIdNullData() throws InsightsCustomException {
    	String requestPath = "/data/fetchProjectMappingByOrgId?orgId=1000";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");
        assertEquals("Should return SUCCESS with NULL data","success",jsonObj.get("status").getAsString());
        assertEquals("Should return data array as NULL",0,jsonDataArry.size());//Data array is null

    }
    @Test //When all the project mappings are fetched successfully
    public void testFetchAllPrjtMapping() throws InsightsCustomException {
    	String requestPath = "/data/fetchAllProjectMapping";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("data");
        assertEquals("Should return SUCCESS with data","success",jsonObj.get("status").getAsString());
        assertNotNull(jsonDataArry);//Data array is not null

    }
    
  //Accessing GET through POST call
    
    
    @Test //When parameter, orgId=1 is valid and called through POST method
    public void testFetchPrjtMappingByOrgIdPost() throws InsightsCustomException {
    	String requestPath = "/data/fetchProjectMappingByOrgId?orgId=1";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    @Test //When parameter, orgId=1000 is invalid and called through POST method
    public void testFetchPrjtMappingByOrgIdNullDataPost() throws InsightsCustomException {
    	String requestPath = "/data/fetchProjectMappingByOrgId?orgId=1000";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());

    }
    @Test //When all the project mappings are fetched successfully and called through POST method
    public void testFetchAllPrjtMappingPost() throws InsightsCustomException {
    	String requestPath = "/data/fetchAllProjectMapping";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
        
  }

}