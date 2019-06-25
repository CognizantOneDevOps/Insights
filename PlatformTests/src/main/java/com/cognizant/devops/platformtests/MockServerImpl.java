/**
 * 
 */
package com.cognizant.devops.platformtests;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockserver.client.MockServerClient;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * @author 668284
 *
 */
public class MockServerImpl {
	
	private static Logger log = LogManager.getLogger(MockServerImpl.class);
	
	public static void main(String[] args) {
		
		MockServerImpl mock= new MockServerImpl();
		mock.startMockServerWithExpectations();
	}
	
	/**
	 * This function will read all json files from Mock_Json folder and create
	 * expectations from the given Paths and Response in the individual tool's json.
	 */
	public void startMockServerWithExpectations() {
		List<MockRequest> mockRequests 	= new ArrayList<>();
		MockServerClient mockServer 	= startClientAndServer(1080);
		final File requestsFolder 		= new File("D:\\InSights_Windows\\Mock_Json");
		
		for (final File currFile : requestsFolder.listFiles()) {
			mockRequests = readMockRequestsfromJson(mockRequests, currFile);
		};
		
		for (int requestIndex = 0; requestIndex < mockRequests.size(); requestIndex++) {
			mockServer
				.when(
						request()
						.withPath(mockRequests.get(requestIndex).getPath())
						)
				.respond(
						response()
						.withStatusCode(200)
						.withBody(mockRequests.get(requestIndex).getResponse())
						);
		}
		System.out.println("Mock Server Started");
		log.debug("Mock");
	 }
	
	/**
	 * This function will read all {@link MockRequest} from json file and appends it
	 * to the mockRequests list.
	 * 
	 * @param mockRequests
	 *            List of all mock requests gathered from individual json files.
	 * @param fileToRead
	 *            File from which mock requests to be read.
	 * @return mockRequests List
	 */
	private List<MockRequest> readMockRequestsfromJson(List<MockRequest> mockRequests, File fileToRead) {
        JsonElement objObject = null;
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        Type type = new TypeToken<List<MockRequest>>() {}.getType();
        List<MockRequest> requests = new ArrayList<MockRequest>();
        try {
	        File fileName = new File(fileToRead.getPath());
	        Reader jsonFileReader = new FileReader(fileName);
	        objObject = parser.parse(jsonFileReader);
	        requests = gson.fromJson(objObject, type);
        } catch (IOException e) {
        	System.out.println("Error while reading mock requests from file " + fileToRead.getName());
        }
        mockRequests.addAll(requests);
        return mockRequests;
	}

}
