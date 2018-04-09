package com.cognizant.devops.platformservice.rest.querycaching.service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.google.gson.JsonObject;

public interface InsightsDatasourceService {

	public JsonObject getNeo4jDatasource(String json) throws GraphDBException;
	

}
