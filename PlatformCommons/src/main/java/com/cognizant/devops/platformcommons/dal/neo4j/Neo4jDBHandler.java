/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


/**
 * 
 * @author 146414
 *
 * This class will handle all the interactions with graph database.
 */

public class Neo4jDBHandler {
	DocumentParser parser = new DocumentParser();
	
	
	/*
	 * Create Nodes using neo4j rest api with transaction support. Following are request details:
	 * 		POST http://localhost:7474/db/data/transaction/commit
	 * 		Accept: application/json; charset=UTF-8
	 * 		Content-Type: application/json
	 * Here, we are committing the entire transaction at once.
	 */
	 
	public JsonObject createNodesWithLabel(List<JsonObject> dataList, List<String> labels, String uniqueAttribute) throws GraphDBException{
		if(dataList == null || dataList.size() == 0){
			return new JsonObject();
		}
		String cypherQuery = null;
		String queryLabel = "";
		for(String label : labels){
			queryLabel += ":"+label;
		}
		if(uniqueAttribute.trim().length() > 0){
			cypherQuery = "MERGE (n"+queryLabel+" {"+uniqueAttribute+":{props}."+uniqueAttribute+"}) ON CREATE SET n={props} RETURN n";
			//MERGE (n:SCM:GIT {ScmRevisionNumber:{props}.ScmRevisionNumber}  ) ON CREATE SET n={props} RETURN n
		}else{
			cypherQuery = "CREATE (n"+queryLabel+" {props}) return n";
		}
		JsonObject requestJson = buildRequestJson(dataList, cypherQuery);
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		return buildResponseJson(response);
	}
	
	/**
	 * @param dataList
	 * @param labels
	 * @param cypherQuery
	 * @return JsonObject
	 * @throws GraphDBException
	 */
	public JsonObject bulkCreateNodes(List<JsonObject> dataList, List<String> labels, String cypherQuery) throws GraphDBException{
		if(dataList == null || dataList.size() == 0 || cypherQuery == null || cypherQuery.trim().length() == 0){
			return new JsonObject();
		}
		JsonArray props = new JsonArray();
		for(JsonObject data : dataList){
			props.add(data);
		}
		JsonObject statement = getCreateCypherQueryStatement(props, cypherQuery);
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		statementArray.add(statement);
		requestJson.add("statements", statementArray);
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		return buildResponseJson(response);
	}
	
	public JsonObject bulkCreateCorrelations(List<String> correlationCyphers) throws GraphDBException{
		JsonArray statementArray = new JsonArray();
		for(String cypherQuery : correlationCyphers) {
			JsonObject statement = new JsonObject();
			statement.addProperty("statement", cypherQuery);
			statementArray.add(statement);
		}
		JsonObject requestJson = new JsonObject();
		requestJson.add("statements", statementArray);
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		return buildResponseJson(response);
	}
	
	/**
	 * @param cypherQuery
	 * @param dataList
	 * @return JsonObject
	 * @throws GraphDBException
	 */
	public JsonObject executeQueryWithData(String cypherQuery, List<JsonObject> dataList) throws GraphDBException{
		if(dataList == null || dataList.size() == 0){
			return new JsonObject();
		}		
		JsonObject requestJson = buildRequestJson(dataList, cypherQuery);
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		return buildResponseJson(response);
	}
	


	/**
	 * @param label
	 * @return NodeData
	 * 
	 * @throws GraphDBException
	 */
	public NodeData fetchTrackingNode(String label) throws GraphDBException{
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		JsonObject statement = new JsonObject();
		statement.addProperty("statement", "MATCH (n"+label+") return n");
		statementArray.add(statement);
		requestJson.add("statements", statementArray);
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		List<NodeData> processGraphDBNode = parser.processGraphDBNode(response.getEntity(String.class)).getNodes();
		if(processGraphDBNode.size() == 0){
			processGraphDBNode = createTrackingNode(label);
		}
		return processGraphDBNode.get(0);
	}
	
	/**
	 * @param query
	 * @return GraphResponse
	 * @throws GraphDBException
	 */
	public GraphResponse executeCypherQuery(String query) throws GraphDBException{
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
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		return parser.processGraphDBNode(response.getEntity(String.class));
	}
	
	/**
	 * @param queryJson
	 * @return String
	 */
	public String executeCypherQueryRaw(String queryJson){
		JsonObject requestJson = new JsonParser().parse(queryJson).getAsJsonObject();
		ClientResponse response = doCommitCall(requestJson);
		return response.getEntity(String.class);
	}
	
	
	/**
	 * @param query
	 * @param count
	 * @return GraphResponse
	 * @throws GraphDBException
	 */
	public GraphResponse executeCypherQueryMultiple(String query, int count) throws GraphDBException{
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		JsonObject statement = null;
		JsonArray resultDataContents = null;
		for(int i=0; i<count; i++){
			statement = new JsonObject();
			statement.addProperty("statement", query);
			resultDataContents = new JsonArray();
			resultDataContents.add("row");
			resultDataContents.add("graph");
			statement.add("resultDataContents", resultDataContents);
			statementArray.add(statement);
		}
		requestJson.add("statements", statementArray);
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		return parser.processGraphDBNode(response.getEntity(String.class));
	}


	/**
	 * @param label
	 * @return List<NodeData>
	 * @throws GraphDBException
	 */
	private List<NodeData> createTrackingNode(String label) throws GraphDBException {
		JsonObject jsonObj = new JsonObject();
		jsonObj.addProperty("TRACKING_ID", "");
		ArrayList<JsonObject> dataList = new ArrayList<JsonObject>();
		dataList.add(jsonObj);
		String cypherQuery = "CREATE (n"+label+" {props}) return n";
		JsonObject requestJson = buildRequestJson(dataList, cypherQuery);
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		List<NodeData> processGraphDBNode = parser.processGraphDBNode(response.getEntity(String.class)).getNodes();
		return processGraphDBNode;
	}
	
	
	/**
	 * @param label
	 * @param updatedTrackingId
	 * @return List<NodeData>
	 * @throws GraphDBException
	 */
	public List<NodeData> updateTrackingNode(String label, String updatedTrackingId) throws GraphDBException {
		JsonObject jsonObj = new JsonObject();
		ArrayList<JsonObject> dataList = new ArrayList<JsonObject>();
		dataList.add(jsonObj);
		String cypherQuery = "match(n"+label+") SET n.TRACKING_ID = '"+updatedTrackingId+"'  return n";
		JsonObject requestJson = buildRequestJson(dataList, cypherQuery);
		ClientResponse response = doCommitCall(requestJson);
		if(response.getStatus() != 200){
			throw new GraphDBException(response);
		}
		List<NodeData> processGraphDBNode = parser.processGraphDBNode(response.getEntity(String.class)).getNodes();
		return processGraphDBNode;
	}
	
	
	/**
	 * @param response
	 * @return JsonObject
	 */
	private JsonObject buildResponseJson(ClientResponse response) {
		JsonObject responseJson = new JsonObject();
		responseJson.add("response", new JsonParser().parse(response.getEntity(String.class)));
		return responseJson;
	}


	private JsonObject buildRequestJson(List<JsonObject> dataList, String cypherQuery) {
		JsonObject requestJson = new JsonObject();
		JsonArray statementArray = new JsonArray();
		for(JsonObject data : dataList){
			JsonObject statement = getCreateCypherQueryStatement(data, cypherQuery);
			statementArray.add(statement);
		}
		requestJson.add("statements", statementArray);
		return requestJson;
	}


	/**
	 * @param requestJson
	 * @return ClientResponse
	 */
	private ClientResponse doCommitCall(JsonObject requestJson) {
		WebResource resource = Client.create()
				//.resource("http://localhost:7474/db/data/transaction/commit");
				.resource(ApplicationConfigProvider.getInstance().getGraph().getEndpoint()+"/db/data/transaction/commit");
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON ).header("Authorization", ApplicationConfigProvider.getInstance().getGraph().getAuthToken())
		//ClientResponse response = resource.accept( MediaType.APPLICATION_JSON ).header("Authorization", "Basic bmVvNGo6YWRtaW4=")
				.type(MediaType.APPLICATION_JSON)
				.entity(requestJson.toString())
				.post(ClientResponse.class);
		return response;
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
