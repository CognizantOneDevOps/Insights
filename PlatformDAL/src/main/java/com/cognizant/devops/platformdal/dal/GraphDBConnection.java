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
