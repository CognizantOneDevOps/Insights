package com.cognizant.devops.platformservice.rest.querycaching.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("insightDatasourceService")
public class InsightsDatasourceServiceImpl implements InsightsDatasourceService {

	private static Logger log = Logger.getLogger(InsightsDatasourceServiceImpl.class);

	@Override
	public JsonObject getNeo4jDatasource(String queryjson) throws GraphDBException {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = null;
		GraphResponse response = null ;
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(queryjson).getAsJsonObject();
		if(json.get("statements").getAsJsonArray().get(0).getAsJsonObject().has("statement")){
			query = json.get("statements").getAsJsonArray().get(0).getAsJsonObject().get("statement").getAsString();
		}
		try {
			response = dbHandler.executeCypherQuery(query);
		} catch (GraphDBException e) {
			log.error("Exception in neo4j query execution",e);
			throw e;
		}
		return response.getJson();
	}

}
