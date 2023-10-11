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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * @author 146414
 *         This class will handle all the interactions with graph database.
 */
public class GraphDBHandler implements AutoCloseable{
	private static Logger log = LogManager.getLogger(GraphDBHandler.class);
	String COMMIT_URL = "/db/data/transaction/commit";
	String SCHEMAURL = "/db/data/schema/index/";
	String SCHEMA_INDEX_URL = ApplicationConfigProvider.getInstance().getGraph().getEndpoint()
			+ SCHEMAURL;
	String TRANSACTION_COMMIT_URL = ApplicationConfigProvider.getInstance().getGraph().getEndpoint()
			+ COMMIT_URL;
	DocumentParser parser;

	public GraphDBHandler() {
		if(ApplicationConfigProvider.getInstance().getGraph().getVersion() != null && ApplicationConfigProvider.getInstance().getGraph().getVersion().contains("4.")) {
			String databaseName = ApplicationConfigProvider.getInstance().getGraph().getDatabaseName();
			COMMIT_URL = "/db/"+databaseName+"/tx/commit";
		}
		parser = new DocumentParser();
		SCHEMA_INDEX_URL = ApplicationConfigProvider.getInstance().getGraph().getEndpoint() + SCHEMAURL;
		TRANSACTION_COMMIT_URL = ApplicationConfigProvider.getInstance().getGraph().getEndpoint()
				+ COMMIT_URL;
	}

	public GraphDBHandler(String inputDataSource) {
		parser = new DocumentParser();
		SCHEMA_INDEX_URL = inputDataSource + SCHEMAURL;
		TRANSACTION_COMMIT_URL = inputDataSource + COMMIT_URL;
	}

	/**
	 * @param dataList
	 * @param cypherQuery
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject bulkCreateNodes(List<JsonObject> dataList, String cypherQuery)
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
		requestJson.add(ConfigOptions.STATEMENTS , statementArray);
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
		requestJson.add(ConfigOptions.STATEMENTS , statementArray);
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
		statement.addProperty(ConfigOptions.STATEMENT, query);
		statementArray.add(statement);
		JsonArray resultDataContents = new JsonArray();
		resultDataContents.add("row");
		resultDataContents.add(ConfigOptions.GRAPH);
		statement.add(ConfigOptions.RESULTDATACONTENTS, resultDataContents);

		requestJson.add(ConfigOptions.STATEMENTS , statementArray);
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
		statement.addProperty(ConfigOptions.STATEMENT, query);
		statementArray.add(statement);
		JsonArray resultDataContents = new JsonArray();
		resultDataContents.add("row");
		resultDataContents.add(ConfigOptions.GRAPH);
		statement.add(ConfigOptions.RESULTDATACONTENTS, resultDataContents);

		requestJson.add(ConfigOptions.STATEMENTS , statementArray);
		String response = neo4jCommunication(requestJson);
		return JsonUtils.parseStringAsJsonObject(response);
	}

	/**
	 * @param queryJson
	 * @return String
	 * @throws InsightsCustomException
	 */
	public String executeCypherQueryRaw(String queryJson) throws InsightsCustomException {
		JsonObject requestJson = JsonUtils.parseStringAsJsonObject(queryJson);
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
			statement.addProperty(ConfigOptions.STATEMENT, query);
			resultDataContents = new JsonArray();
			resultDataContents.add("row");
			resultDataContents.add(ConfigOptions.GRAPH);
			statement.add(ConfigOptions.RESULTDATACONTENTS, resultDataContents);
			statementArray.add(statement);
		}
		requestJson.add(ConfigOptions.STATEMENTS , statementArray);
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
			responseJson.add("response", JsonUtils.parseString(response));
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
			requestJson.add(ConfigOptions.STATEMENTS , statementArray);
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
		long time = System.currentTimeMillis();
		int rowCount = 0;
		String returnResponse = null;
		Map<String, String> headers = new HashMap<>();
		headers.put(ConfigOptions.AUTHORIZATION, ApplicationConfigProvider.getInstance().getGraph().getAuthToken());
		headers.put("X-Stream", "true");
		returnResponse = RestApiHandler.doPost(TRANSACTION_COMMIT_URL, requestJson, headers);
		
		JsonObject graphJsonObj= JsonUtils.parseStringAsJsonObject(returnResponse);

		parseGraphResponseForError(graphJsonObj, requestJson.toString());
		rowCount = getRecordCount(graphJsonObj); 
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTrace = stacktrace[3];

		long processingTime = (System.currentTimeMillis() - time);

		if (processingTime > ApplicationConfigProvider.getInstance().getGraph()
				.getLogQueryIfProcessingTimeGreaterThanInMS()) {
			Set<String> queryList = getQueryListFromRequestJson(requestJson);
			log.debug(
					"Type=GraphDB className={} methodName={} lineNo={} "
							+ " Datasource={} processingTime={} rowCount={} queryList={}",
					stackTrace.getFileName(), stackTrace.getMethodName(), stackTrace.getLineNumber(),
					ApplicationConfigProvider.getInstance().getGraph().getEndpoint(), processingTime, rowCount,
					queryList);
		} else {
			log.debug(
					"Type=GraphDB className={} methodName={} lineNo={} "
							+ "Datasource={} processingTime={} rowCount={} ",
					stackTrace.getFileName(), stackTrace.getMethodName(), stackTrace.getLineNumber(),
					ApplicationConfigProvider.getInstance().getGraph().getEndpoint(), processingTime, rowCount);
		}
		return returnResponse;
	}
	


	void parseGraphResponseForError(JsonObject graphResponse,String requestJson) throws InsightsCustomException {
		JsonArray errorMessage = graphResponse.getAsJsonArray("errors");
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTrace = stacktrace[4];
		if (errorMessage != null && errorMessage.size() >= 1) {
			for (JsonElement jsonElement : errorMessage) {
				String errorMessageText = jsonElement.getAsJsonObject().get("message").getAsString();
				log.error("Type=GraphDB className {} methodName {} lineNo {} "
						+ "requestJson {} message {} ", 
						stackTrace.getClassName(),stackTrace.getMethodName(),stackTrace.getLineNumber(),
						requestJson,errorMessageText);
			}
			throw new InsightsCustomException("Error while running Neo4j Query ");
		}
	}
	
	private Set<String> getQueryListFromRequestJson(JsonObject requestJson) {
		Set<String> queryList = new HashSet<>();
		try {
			if(requestJson.has("statements")){
				JsonArray data = requestJson.getAsJsonArray("statements");
				for (JsonElement jsonElement : data) {
					queryList.add(jsonElement.getAsJsonObject().get("statement").toString());
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return queryList;
	}

	private int getRecordCount(JsonObject graphResp) {
		try {
			JsonObject graphResultJsonResult = graphResp.getAsJsonArray(ConfigOptions.RESULTS).get(0).getAsJsonObject();
			if (graphResultJsonResult.has("data")) {
				JsonArray data = graphResultJsonResult.getAsJsonArray("data");
				return data.size();
			}
		} catch (Exception e) {
			log.error(e);
		}
		return 0;
	}
	


	/**
	 * Load the available field indices in Neo4J
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonArray loadFieldIndices() throws InsightsCustomException {
		Map<String, String> headers = new HashMap<>();
		headers.put(ConfigOptions.AUTHORIZATION, ApplicationConfigProvider.getInstance().getGraph().getAuthToken());
		String response = RestApiHandler.doGet(SCHEMA_INDEX_URL, headers);
		return JsonUtils.parseStringAsJsonArray(response);
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
		headers.put(ConfigOptions.AUTHORIZATION, ApplicationConfigProvider.getInstance().getGraph().getAuthToken());
		String url = SCHEMA_INDEX_URL + label;
		String response = RestApiHandler.doPost(url, requestJson, headers);
		return JsonUtils.parseStringAsJsonObject(response);
	}

	/**
	 * @param data
	 * @param cypherQuery
	 * @return JsonObject
	 */
	private JsonObject getCreateCypherQueryStatement(JsonElement data, String cypherQuery) {
		JsonObject statement = new JsonObject();
		statement.addProperty(ConfigOptions.STATEMENT, cypherQuery);
		JsonObject props = new JsonObject();
		props.add("props", data);
		statement.add("parameters", props);
		return statement;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
}