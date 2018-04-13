package com.cognizant.devops.platformservice.rest.querycaching.service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.google.gson.JsonObject;

public interface QueryCachingService {

	public JsonObject getCacheResults(String requestPayload) throws GraphDBException;
	

}
