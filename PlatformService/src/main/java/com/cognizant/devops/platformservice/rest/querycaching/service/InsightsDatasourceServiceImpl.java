package com.cognizant.devops.platformservice.rest.querycaching.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("insightDatasourceService")
public class InsightsDatasourceServiceImpl implements InsightsDatasourceService {

	private static Logger log = Logger.getLogger(InsightsDatasourceServiceImpl.class);

	@Override
	public JsonArray getNeo4jDatasource(String queryjson) throws GraphDBException {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = null;
		GraphResponse response = null ;
		JsonArray rows = null; 
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
		if(null != query){
			rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray();
			if(rows.size() >0){
				return rows.get(0).getAsJsonObject().get("row").getAsJsonArray();
			}
		}
		return rows;
	}

}
