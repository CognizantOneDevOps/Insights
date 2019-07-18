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
package com.cognizant.devops.platformservice.rest.util;

import org.junit.Assert;
import org.junit.Test;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;

public class PlatformServiceUtilTest {
	
	
	@Test
	public void testBuildFailureResponseNotNull(){
		String message = "Failed Message";
		JsonObject response = PlatformServiceUtil.buildFailureResponse(message);
		Assert.assertNotNull(response);
	}
	
	@Test
	public void testBuildFailureResponse(){
		String message = "Failed Message";
		JsonObject response = PlatformServiceUtil.buildFailureResponse(message);
		String responseMessage = response.get(PlatformServiceConstants.MESSAGE).getAsString();
		String responseStatus = response.get(PlatformServiceConstants.STATUS).getAsString();
		Assert.assertEquals(message, responseMessage);
		Assert.assertEquals(PlatformServiceConstants.FAILURE, responseStatus);
	}
	
	@Test
	public void testBuildSuccessResponseNotNull(){
		JsonObject response = PlatformServiceUtil.buildSuccessResponse();
		Assert.assertNotNull(response);
	}
	
	@Test
	public void testBuildSuccessResponse(){
		JsonObject response = PlatformServiceUtil.buildSuccessResponse();
		String responseStatus = response.get(PlatformServiceConstants.STATUS).getAsString();
		Assert.assertEquals(PlatformServiceConstants.SUCCESS, responseStatus);
	}
	
	@Test
	public void testBuildSuccessResponseWithDataNotNull(){
		Object data = new Object();
		JsonObject response = PlatformServiceUtil.buildSuccessResponseWithData(data);
		Assert.assertNotNull(response);
	}
	
	@Test
	public void testBuildSuccessResponseWithData(){
		Object data = new Object();
		JsonObject response = PlatformServiceUtil.buildSuccessResponseWithData(data);
		String responseStatus = response.get(PlatformServiceConstants.STATUS).getAsString();
		Assert.assertEquals(PlatformServiceConstants.SUCCESS, responseStatus);
	}
	
	@Test
	public void testPublishConfigChangesNotNull(){
		JsonObject requestJson = new JsonObject();
		ClientResponse response = PlatformServiceUtil.publishConfigChanges("localhost",7474,requestJson);
		Assert.assertNotNull(response);
	}

}
*/