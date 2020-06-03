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
package com.cognizant.devops.platformservice.test.toolslayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.test.utility.ServiceTestConstants;
import com.cognizant.devops.platformservice.test.utility.ServiceTestUtilities;
import com.google.gson.JsonObject;

public class ToolsLayoutServiceTest {
	
	 @Test //To read the details of the tool
	    public void testLoadToolsLayoutSuccess() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=SCM&toolName=GIT";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
	        JsonObject obj= (JsonObject) jsonObj.get("data");  
	        assertEquals("Should return SUCCESS with data","success",jsonObj.get("status").getAsString());
	        assertNotNull(obj);//Checks that Data is not NULL
	    }
	 
	 @Test //To read the details of the tool, when tool Category or tool name is incorrect
	    public void testLoadToolsLayoutSuccessNull() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=abc&toolName=def";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
	        JsonObject obj= (JsonObject) jsonObj.get("data");  
	        assertEquals("Should return SUCCESS without data","success",jsonObj.get("status").getAsString());
	        assertNull(obj);//Checks that Data is not NULL
	    }
	 
	 @Test //To test that the data is not NULL
	    public void testLoadToolsLayoutData() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=SCM&toolName=GIT";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
	        JsonObject jsonDataObj = (JsonObject) jsonObj.get("data");
	        JsonObject jsonLayoutObj = (JsonObject) jsonDataObj.get("layoutSettings");
	        assertNotNull("Should not be null",jsonLayoutObj);
	    }
	 
	 @Test // To check that tool layout doesnot exist
	    public void testLoadToolsLayoutNoLayout() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=SCM&toolName=Bitbucket";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
	        JsonObject jsonDataObj = (JsonObject) jsonObj.get("data");
	        assertEquals("Should return success","success",jsonObj.get("status").getAsString());
	        assertNull("Should return success if tool layout not exist",jsonDataObj);
	    }
	 
	 @Test //To read the details of the tool, when tool Category or tool name is incorrect
	    public void testLoadToolsLayoutNoData() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=xyz&toolName=test";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
	        assertEquals("Should return SUCCESS","success",jsonObj.get("status").getAsString());
	    }
	 
	 @Test //To check the details of all the tools present
	    public void testLoadAllToolsLayoutSuccess() throws InsightsCustomException {
	    	String requestPath = "/toollayout/readAll";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.GET_REQUEST,null);
	        assertEquals("Should return success","success",jsonObj.get("status").getAsString());
	    }
	 
	 //Accessing GET through POST
	 
	 @Test //To read the details of the tool
	    public void testLoadToolsLayoutSuccessPost() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=SCM&toolName=GIT";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
	        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	       
	 }
	 
	 @Test //To read the details of the tool, when tool Category or tool name is incorrect
	    public void testLoadToolsLayoutSuccessNullPost() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=abc&toolName=def";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
	        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	        
	    }
	 
	 @Test //To test that the data is not NULL
	    public void testLoadToolsLayoutDataPost() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=SCM&toolName=GIT";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
	        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	    }
	 
	 @Test // To check that tool layout doesnot exist
	    public void testLoadToolsLayoutNoLayoutPost() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=SCM&toolName=Bitbucket";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
	        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	        
	    }
	 
	 @Test //To read the details of the tool, when tool Category or tool name is incorrect
	    public void testLoadToolsLayoutNoDataPost() throws InsightsCustomException {
	    	String requestPath = "/toollayout/read?category=xyz&toolName=test";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
	        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	    }
	 
	 @Test //To check the details of all the tools present
	    public void testLoadAllToolsLayoutSuccessPost() throws InsightsCustomException {
	    	String requestPath = "/toollayout/readAll";
	        JsonObject jsonObj = ServiceTestUtilities.makeServiceRequest(requestPath,ServiceTestConstants.POST_REQUEST,null);
	        assertEquals("Should return failure","failure",jsonObj.get("status").getAsString());	   
	        }

}
