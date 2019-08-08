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
package com.cognizant.devops.platformmockserver;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Parameter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * @author 668284
 *
 */
public class MockServerImpl {
	
	private static final String FILE_SEPERATOR 	= File.separator;
	static Logger log = LogManager.getLogger(MockServerImpl.class.getName());
	
	public static void main(String[] args) {
		MockServerImpl mock= new MockServerImpl();
		mock.startMockServerWithExpectations();
	}
	
	/**
	 * This function will read all json files from getAllMockRequestsFromJson method
	 * and create expectations from the given Paths and Response in the individual
	 * tool's json.
	 */
	public void startMockServerWithExpectations() {
		// Starting mock server on port 1080.
		MockServerClient mockServer = startClientAndServer(1080);
		// Reading mock requests from multiple JSON files.
		List<MockRequest> mockRequests = getAllMockRequestsFromJson();
		String response = null;
		
		Iterator<MockRequest> mockIterator = mockRequests.iterator();
		while (mockIterator.hasNext()) {
			
			MockRequest request = mockIterator.next();
			List<Parameter> parameterList = new ArrayList<>();
			
			if(request.isResponseJson()) {
				response = request.getResponse().get(0).toString();
			} else {
				response = request.getResponse().toString();	
			}
			
			// checking if the request has any parameters.
			if(request.getParameters() != null) {
				for (Map.Entry<String,String> paramEntry : request.getParameters().entrySet()) {
					
					Parameter param = new Parameter(paramEntry.getKey(), paramEntry.getValue());
					parameterList.add(param);
				}
			}
			
			// Creating mock Expectation 
			mockServer
			.when(
					request()
					.withPath(request.getPath())
					.withQueryStringParameters(parameterList)
				)
			.respond(
					response()
					.withStatusCode(200)
					.withBody(response)
					);
			
			log.debug("Expectation created for the path {} with query string parmeters {}",request.getPath(),parameterList);
		}
		log.info("Mock Server Started");
	 }
	
	/**
	 * This function will read all {@link MockRequest} from json file and appends it
	 * to the mockRequests list.
	 * 
	 * @return mockRequests List
	 */
	private List<MockRequest> getAllMockRequestsFromJson() {
		List<MockRequest> mockRequests = new ArrayList<>();
		String folderPath = System.getenv("INSIGHTS_HOME") + FILE_SEPERATOR + "mock_json";   
		final File requestsFolder = new File(folderPath);
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		Type type = new TypeToken<List<MockRequest>>() {}.getType();
		
		for (final File currFile : requestsFolder.listFiles()) {
			
			String mockFileName = currFile.getName();
			String fileExtension = mockFileName.substring(mockFileName.lastIndexOf(".")); 
			if(!fileExtension.equals(".json")) {
				log.error(mockFileName + " is not a json file");
				continue;
			}
			
			JsonElement objObject = null;
			List<MockRequest> requests = new ArrayList<MockRequest>();
			
			try {
				log.info("Reading {} file for mock requests",mockFileName);
				File mockFile = new File(currFile.getPath());
				Reader jsonFileReader = new FileReader(mockFile);
				objObject = parser.parse(jsonFileReader);
				requests = gson.fromJson(objObject, type);
			} catch (IOException e) {
				log.error("Error while reading mock requests from file " + mockFileName);
			}
			if(requests != null)
				mockRequests.addAll(requests);
		};
		
        return mockRequests;
	}

}
