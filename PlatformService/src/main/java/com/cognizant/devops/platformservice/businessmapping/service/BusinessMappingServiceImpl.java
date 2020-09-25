/*********************************************************************************
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
 *******************************************************************************/
package com.cognizant.devops.platformservice.businessmapping.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.businessmapping.constants.BusinessMappingConstants;
import com.cognizant.devops.platformservice.businessmapping.model.Node;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("businessMappingService")
public class BusinessMappingServiceImpl implements BusinessMappingService {

	static Logger log = LogManager.getLogger(BusinessMappingServiceImpl.class.getName());

	@Override
	public JsonObject getAllHierarchyDetails() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:METADATA:DATATAGGING) return n";
		GraphResponse response;
		JsonArray parentArray = new JsonArray();
		try {
			response = dbHandler.executeCypherQuery(query);
			JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray();
			JsonArray asJsonArray = rows.getAsJsonArray();
			JsonObject jsonObject = populateHierarchyDetails(asJsonArray);
			parentArray.add(jsonObject);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(parentArray);
	}

	/**
	 * @param array
	 * @return
	 */
	private JsonObject populateHierarchyDetails(JsonArray array) {
		int rowCount = 0;
		List<List<String>> valueStore = new ArrayList<>();
		for (JsonElement element : array) {
			JsonElement jsonElement = element.getAsJsonObject().get("row").getAsJsonArray().get(0);
			List<String> valueList = new ArrayList<>();
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			if (jsonObject != null && jsonObject.get(BusinessMappingConstants.LEVEL1) != null) {
				String level1Value = jsonObject.get(BusinessMappingConstants.LEVEL1).getAsString();
				if (null != level1Value && !level1Value.isEmpty()) {
					valueList.add(level1Value);
				}
			}
			if (jsonObject != null && jsonObject.get(BusinessMappingConstants.LEVEL2) != null) {
				String level2Value = jsonObject.get(BusinessMappingConstants.LEVEL2).getAsString();
				if (null != level2Value && !level2Value.isEmpty()) {
					valueList.add(level2Value);
				}
			}
			if (jsonObject != null && jsonObject.get(BusinessMappingConstants.LEVEL3) != null) {
				String level3Value = jsonObject.get(BusinessMappingConstants.LEVEL3).getAsString();
				if (null != level3Value && !level3Value.isEmpty()) {
					valueList.add(level3Value);
				}
			}
			if (jsonObject != null && jsonObject.get(BusinessMappingConstants.LEVEL4) != null) {
				String level4Value = jsonObject.get(BusinessMappingConstants.LEVEL4).getAsString();
				if (null != level4Value && !level4Value.isEmpty()) {
					valueList.add(level4Value);
				}
			}
			valueStore.add(rowCount, valueList);
			rowCount++;
		}
		// Logic of converting data into tree structure
		// create special 'root' Node with id=0
		Node root = new Node(null, 0, "root");
		for (List<String> values : valueStore) {
			Node parent = root;
			for (int i = 0; i < values.size(); i++) {
				Node node = new Node(parent, i + 1, values.get(i));
				if (parent.getChild(node) == null) {
					parent.addChild(node);
					parent = node;
				} else {
					parent = parent.getChild(node);
				}
			}
		}
		return populateJsonTree(root);
	}

	/**
	 * Populates a json object with tree structure from Node object which is a tree
	 * representation
	 * 
	 * @param root
	 * @return
	 */
	private JsonObject populateJsonTree(Node root) {
		JsonObject jsonTree = new JsonObject();
		jsonTree.addProperty(BusinessMappingConstants.NAME, root.getName());
		createJsonObject(root, jsonTree);
		return jsonTree;
	}

	/**
	 * @param node
	 * @param parentJson
	 */
	private void createJsonObject(Node node, JsonObject parentJson) {
		JsonArray childArray = new JsonArray();
		// recurse
		for (Node childNode : node.getChildren()) {
			JsonObject childJson = new JsonObject();
			childJson.addProperty(BusinessMappingConstants.NAME, childNode.getName());
			childArray.add(childJson);
			createJsonObject(childNode, childJson);
		}
		if (childArray.size() != 0) {
			parentJson.add(BusinessMappingConstants.CHILDREN, childArray);
		}
	}

	/*
	 * Fetches all tools and property details configured for a particular Business
	 * Hierarchy Provides all four levels as an input to that method (non-Javadoc)
	 * 
	 * @see com.cognizant.devops.platformservice.businessmapping.service.
	 * BusinessMappingService#getHierarchyProperties(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JsonObject getHierarchyProperties(String level1, String level2, String level3, String level4)
			throws InsightsCustomException {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String queryLabels = ":METADATA:DATATAGGING";
		StringBuilder sb = new StringBuilder();
		if (null != level1 && !level1.isEmpty()) {
			sb.append("level_1:'");
			sb.append(level1.trim());
			sb.append("'");
			sb.append(",");
		}
		if (null != level2 && !level2.isEmpty()) {
			sb.append("level_2:'");
			sb.append(level2.trim());
			sb.append("'");
			sb.append(",");
		}
		if (null != level3 && !level3.isEmpty()) {
			sb.append("level_3:'");
			sb.append(level3.trim());
			sb.append("'");
			sb.append(",");
		}
		if (null != level4 && !level4.isEmpty()) {

			sb.append("level_4:'");
			sb.append(level4.trim());
			sb.append("'");

		}
		String props = StringUtils.stripEnd(sb.toString(), ",");
		String query = "MATCH (n " + queryLabels + "{" + props + "}" + ") return n";
		GraphResponse response = dbHandler.executeCypherQuery(query);
		return PlatformServiceUtil.buildSuccessResponseWithData(response.getNodes());
	}

	@Override
	public JsonObject saveToolsMappingLabel(String agentMappingJson) {
		List<JsonObject> nodeProperties = new ArrayList<>();
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(agentMappingJson);
			GraphDBHandler dbHandler = new GraphDBHandler();
			dbHandler.executeCypherQuery("CREATE CONSTRAINT ON (n:METADATA) ASSERT n.metadata_id  IS UNIQUE");
			String query = "UNWIND {props} AS properties " + "CREATE (n:METADATA:BUSINESSMAPPING) "
					+ "SET n = properties"; // DATATAGGING
			JsonParser parser = new JsonParser();
			JsonObject json = (JsonObject) parser.parse(validatedResponse);
			log.debug("arg0  " + json);
			nodeProperties.add(json);
			JsonObject graphResponse = dbHandler.bulkCreateNodes(nodeProperties, null, query);
			if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
					.getAsJsonArray().size() > 0) {
				log.error(graphResponse);
				// return "success";
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}

	@Override
	public JsonObject getToolsMappingLabel(String agentName) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:METADATA:BUSINESSMAPPING) where n.toolName ='" + agentName
				+ "' return n order by n.inSightsTime desc"; // 'GIT'
		GraphResponse response;
		List<Map<String, String>> propertyList = new ArrayList<Map<String, String>>();
		try {
			response = dbHandler.executeCypherQuery(query);
			int size = response.getNodes().size();
			log.debug("arg0  size " + size);
			for (int i = 0; i < size; i++) {
				propertyList.add(response.getNodes().get(i).getPropertyMap());
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(propertyList); // response.getNodes()
	}

	@Override
	public JsonObject editToolsMappingLabel(String agentMappingJson) {
		List<JsonObject> nodeProperties = new ArrayList<>();
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(agentMappingJson);
			GraphDBHandler dbHandler = new GraphDBHandler();
			JsonParser parser = new JsonParser();
			JsonObject json = (JsonObject) parser.parse(validatedResponse);
			String uuid = json.get("uuid").getAsString();
			log.debug("arg0 uuid  " + uuid);
			JsonArray asJsonArray = getCurrentRecords(uuid, dbHandler);
			log.debug("arg0  " + asJsonArray);
			String replaceString = agentMappingJson.replaceAll("\\\"(\\w+)\\\"\\:", "$1:");
			String updateCypherQuery = " MATCH (n :METADATA:BUSINESSMAPPING {uuid:'" + uuid + "'})SET n ="
					+ replaceString + " RETURN n";
			log.debug("to replace" + updateCypherQuery);
			GraphResponse updateGraphResponse = dbHandler.executeCypherQuery(updateCypherQuery);
			log.debug(updateGraphResponse);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}

	private JsonArray getCurrentRecords(String uuid, GraphDBHandler dbHandler) throws InsightsCustomException {
		String cypherQuery = " MATCH (n :METADATA:BUSINESSMAPPING) WHERE n.uuid='" + uuid + "'  RETURN n";
		GraphResponse graphResponse = dbHandler.executeCypherQuery(cypherQuery);
		JsonArray rows = graphResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
				.getAsJsonArray();
		JsonArray asJsonArray = rows.getAsJsonArray();
		return asJsonArray;
	}

	@Override
	public JsonObject deleteToolsMappingLabel(String uuid) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		GraphResponse graphresponce = new GraphResponse();
		try {

			String cypherQuery = "MATCH (n:METADATA:BUSINESSMAPPING) where n.uuid= '" + uuid + "'  detach delete n";
			graphresponce = dbHandler.executeCypherQuery(cypherQuery);

		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(graphresponce);
	}

}
