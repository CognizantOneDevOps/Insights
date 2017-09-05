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
package com.cognizant.devops.platformservice.rest.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.cognizant.devops.platformservice.rest.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.rest.utility.ServiceTestUtilities;
import com.google.gson.JsonObject;


public class SetupApplicationTest  {
	
  
    
    @Test //Test to check the read the configuration file and its details
    public void testLoadConfig() {//successful API call
    	String requestPath = "/configure/read";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        JsonObject obj= (JsonObject) jsonObj.get("endpointData");  
        JsonObject obj1= (JsonObject) jsonObj.get("ldapConfiguration");  
        assertNotNull(obj);//Checks that End point data is not NULL
        assertNotNull(obj1);//Checks that LDAP configuration data is not NULL
    }
    
    
    @Test //Test to check the read the configuration file
    public void testReInitialize() {//successful API call
    	String requestPath = "/configure/loadConfigFromResources";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
        assertEquals("Should return failure with Internal Server Error","failure",jsonObj.get("status").getAsString());//Returns Internal Server Error
    }

    //Accessing GET through POST call
    
    
    @Test //Test to check the read the configuration file and its details using POST
    public void testLoadConfigPost() {//successful API call
    	String requestPath = "/configure/read";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
    }
    
    
    @Test //Test to check the read the configuration file using POST
    public void testReInitializePost() {//successful API call
    	String requestPath = "/configure/loadConfigFromResources";
        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());
        }

}
