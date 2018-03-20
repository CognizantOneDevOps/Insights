/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.dal;

import java.util.Base64;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public final class GraphDBConnection implements AutoCloseable {

	private final Driver driver;
	private static GraphDBConnection graphDBConnection;
	private Integer defaultMaxIdleConnections = 25;
	
	private GraphDBConnection(String uri, String user, String password) {
		Integer maxIdleConnections = ApplicationConfigProvider.getInstance().getGraph().getMaxIdleConnections();
		if(maxIdleConnections == null) {
			maxIdleConnections = defaultMaxIdleConnections;
		}
		driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ),
										Config.build().withMaxIdleConnections(maxIdleConnections).toConfig() );
	}
	
	public static GraphDBConnection getInstance() {
		if(graphDBConnection == null) {
			String uri = ApplicationConfigProvider.getInstance().getGraph().getBoltEndPoint();
			
			String authToken = ApplicationConfigProvider.getInstance().getGraph().getAuthToken();
			String decodedAuthToken = new String(Base64.getDecoder().decode(authToken));
			String[] parts = decodedAuthToken.split(":");
			
			graphDBConnection = new GraphDBConnection(uri,parts[0],parts[1]);
		}
		
		return graphDBConnection;
	}
	
	@Override
	public void close() throws Exception {
		driver.close();

	}
	
	public Driver getDriver() {
		if(driver == null) {
			getInstance();
		}
		return driver;
	}

}
