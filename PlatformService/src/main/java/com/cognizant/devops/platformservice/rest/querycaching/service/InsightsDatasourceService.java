package com.cognizant.devops.platformservice.rest.querycaching.service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.google.gson.JsonArray;

public interface InsightsDatasourceService {

	public JsonArray getNeo4jDatasource(String json) throws GraphDBException;
	

}
