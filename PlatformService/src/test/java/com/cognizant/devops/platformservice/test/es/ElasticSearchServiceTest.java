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
package com.cognizant.devops.platformservice.test.es;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.test.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.test.utility.ServiceTestUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ElasticSearchServiceTest {


    
    @Test //When all the users are fetched successfully
    public void testLoadDashboardData() throws InsightsCustomException {//succesful API call
    	String requestPath = "/search/dashboards";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonArray jsonDataArry = (JsonArray) jsonObj.get("dashboards");//Dashboards Array is fetched
        assertNotNull(jsonObj.get("dashboards"));//Returns list of Dashboards(NOT NULL)
        assertNotNull(jsonDataArry);// Dashboards Array is not NULL
    }
    
    
    @Test  //No Data Available of parameters
    public void testCopyKibanaIndex() throws InsightsCustomException {//succesful API call
    	String requestPath = "/search/copyKibana?from=0&to=10";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
    	assertEquals("Should return FAILURE with Internal Server Error","failure",jsonObj.get("status").getAsString());
    }
    
    //Accessing GET through POST call
    
    @Test //When all the users are fetched successfully using POST call
    public void testLoadDashboardDataPost() throws InsightsCustomException {//succesful API call
    	String requestPath = "/search/dashboards";
    	JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
    	assertEquals("Should return FAILURE","failure",jsonObj.get("status").getAsString());

    }
    
    
    @Test  //No Data Available of parameters using POST call
    public void testCopyKibanaIndexPost() throws InsightsCustomException {//succesful API call
    	String requestPath = "/search/copyKibana?from=0&to=100";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
    	assertEquals("Should return FAILURE","failure",jsonObj.get("status").getAsString());
    }

    
}
