package com.cognizant.devops.platformdal.dal;

import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;

public interface InsightsDaoFactoryInterface {
	
	public Neo4jDBHandler getNeo4jRESTDBHandler();
	public BaseGraphDBHandler getNeo4jBoltDBHandler();
}
