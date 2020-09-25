/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.dal.neo4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author 146414
 *         This class will handle all the interactions with graph database.
 */
public class GraphDBHandler {
	String SCHEMA_INDEX_URL = ApplicationConfigProvider.getInstance().getGraph().getEndpoint()
			+ "/db/data/schema/index/";
	String TRANSACTION_COMMIT_URL = ApplicationConfigProvider.getInstance().getGraph().getEndpoint()
			+ "/db/data/transaction/commit";
	DocumentParser parser;

	public GraphDBHandler() {
		parser = new DocumentParser();
	}

	public GraphDBHandler(String inputDataSource) {
		SCHEMA_INDEX_URL = inputDataSource + "/db/data/schema/index/";
		TRANSACTION_COMMIT_URL = inputDataSource + "/db/data/transaction/commit";
		parser = new DocumentParser();
	}

	/**
	 * @param dataList
	 * @param labels
	 * @param cypherQuery
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject bulkCreateNodes(List<JsonObject> dataList, List<String> labels, String cypherQuery)
			throws InsightsCustomException {
		if (dataList == null || dataList.isEmpty() || cypherQuery == null || cypherQuery.trim().length() == 0) {
			return new JsonObject();
		}
		JsonArray props = new JsonArray();
		for (JsonObject data : dataList) {
			props.add(data);
		}
		JsonObject statement = getCreateCypherQueryStatement(props, cypherQuery);
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		statementArray.add(statement);
		requestJson.add("statements", statementArray);
		String response = neo4jCommunication(requestJson);
		return buildResponseJsonFromString(response);
	}

	/**
	 * @param data
	 * @param cypherQuery
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonObject createNodesWithSingleData(JsonObject data, String cypherQuery) throws InsightsCustomException {
		if (data == null || cypherQuery == null || cypherQuery.trim().length() == 0) {
			return new JsonObject();
		}

		JsonObject statement = getCreateCypherQueryStatement(data, cypherQuery);
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		statementArray.add(statement);
		requestJson.add("statements", statementArray);
		String response = neo4jCommunication(requestJson);
		return buildResponseJsonFromString(response);
	}

	/**
	 * @param cypherQuery
	 * @param dataList
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject executeQueryWithData(String cypherQuery, List<JsonObject> dataList)
			throws InsightsCustomException {
		if (dataList == null || dataList.isEmpty()) {
			return new JsonObject();
		}
		JsonObject requestJson = buildRequestJson(dataList, cypherQuery);
		String response = neo4jCommunication(requestJson);
		return buildResponseJsonFromString(response);
	}

	/**
	 * @param query
	 * @return GraphResponse
	 * @throws InsightsCustomException
	 */
	public GraphResponse executeCypherQuery(String query) throws InsightsCustomException {
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		JsonObject statement = new JsonObject();
		statement.addProperty("statement", query);
		statementArray.add(statement);
		JsonArray resultDataContents = new JsonArray();
		resultDataContents.add("row");
		resultDataContents.add("graph");
		statement.add("resultDataContents", resultDataContents);

		requestJson.add("statements", statementArray);
		String response = neo4jCommunication(requestJson);
		return parser.processGraphDBNode(response);
	}

	/**
	 * execute Cypher Query to get Json Response
	 * 
	 * @param query
	 * @return GraphResponse
	 * @throws InsightsCustomException
	 */
	public JsonObject executeCypherQueryForJsonResponse(String query) throws InsightsCustomException {
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		JsonObject statement = new JsonObject();
		statement.addProperty("statement", query);
		statementArray.add(statement);
		JsonArray resultDataContents = new JsonArray();
		resultDataContents.add("row");
		resultDataContents.add("graph");
		statement.add("resultDataContents", resultDataContents);

		requestJson.add("statements", statementArray);
		String response = neo4jCommunication(requestJson);
		return new JsonParser().parse(response).getAsJsonObject();
	}

	/**
	 * @param queryJson
	 * @return String
	 * @throws InsightsCustomException
	 */
	public String executeCypherQueryRaw(String queryJson) throws InsightsCustomException {
		JsonObject requestJson = new JsonParser().parse(queryJson).getAsJsonObject();
		return neo4jCommunication(requestJson);
	}

	/**
	 * @param query
	 * @param count
	 * @return GraphResponse
	 * @throws InsightsCustomException
	 */
	public GraphResponse executeCypherQueryMultiple(String[] queriesArray) throws InsightsCustomException {
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		JsonObject statement = null;
		JsonArray resultDataContents = null;
		int count = queriesArray.length;
		for (int i = 0; i < count; i++) {
			String query = queriesArray[i];
			statement = new JsonObject();
			statement.addProperty("statement", query);
			resultDataContents = new JsonArray();
			resultDataContents.add("row");
			resultDataContents.add("graph");
			statement.add("resultDataContents", resultDataContents);
			statementArray.add(statement);
		}
		requestJson.add("statements", statementArray);
		String response = neo4jCommunication(requestJson);
		return parser.processGraphDBNode(response);
	}

	/**
	 * @param response
	 * @return
	 * @throws InsightsCustomException
	 */
	private JsonObject buildResponseJsonFromString(String response) throws InsightsCustomException {
		try {
			JsonObject responseJson = new JsonObject();
			responseJson.add("response", new JsonParser().parse(response));
			return responseJson;
		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * @param dataList
	 * @param cypherQuery
	 * @return
	 * @throws InsightsCustomException
	 */
	private JsonObject buildRequestJson(List<JsonObject> dataList, String cypherQuery) throws InsightsCustomException {
		try {
			JsonObject requestJson = new JsonObject();
			JsonArray statementArray = new JsonArray();
			for (JsonObject data : dataList) {
				JsonObject statement = getCreateCypherQueryStatement(data, cypherQuery);
				statementArray.add(statement);
			}
			requestJson.add("statements", statementArray);
			return requestJson;
		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}

	}

	/**
	 * @param requestJson
	 * @return String
	 * @throws InsightsCustomException
	 */
	private String neo4jCommunication(JsonObject requestJson) throws InsightsCustomException {
		String returnResponse = null;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", ApplicationConfigProvider.getInstance().getGraph().getAuthToken());
		returnResponse = RestApiHandler.doPost(TRANSACTION_COMMIT_URL, requestJson, headers);
		return returnResponse;
	}

	/**
	 * Load the available field indices in Neo4J
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonArray loadFieldIndices() throws InsightsCustomException {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", ApplicationConfigProvider.getInstance().getGraph().getAuthToken());
		String response = RestApiHandler.doGet(SCHEMA_INDEX_URL, headers);
		return new JsonParser().parse(response).getAsJsonArray();
	}

	/**
	 * Add the field index for give label and field
	 * 
	 * @param label
	 * @param field
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonObject addFieldIndex(String label, String field) throws InsightsCustomException {
		JsonObject requestJson = new JsonObject();
		JsonArray properties = new JsonArray();
		properties.add(field);
		requestJson.add("property_keys", properties);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", ApplicationConfigProvider.getInstance().getGraph().getAuthToken());
		String url = SCHEMA_INDEX_URL + label;
		String response = RestApiHandler.doPost(url, requestJson, headers);
		return new JsonParser().parse(response).getAsJsonObject();
	}

	/**
	 * @param data
	 * @param cypherQuery
	 * @return JsonObject
	 */
	private JsonObject getCreateCypherQueryStatement(JsonElement data, String cypherQuery) {
		JsonObject statement = new JsonObject();
		statement.addProperty("statement", cypherQuery);
		JsonObject props = new JsonObject();
		props.add("props", data);
		statement.add("parameters", props);
		return statement;
	}
}