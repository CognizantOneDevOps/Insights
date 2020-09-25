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
package com.cognizant.devops.platformservice.rest.datatagging;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.hierarchy.details.HierarchyDetailsDAL;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.cognizant.devops.platformservice.rest.datatagging.model.Node;
import com.cognizant.devops.platformservice.rest.datatagging.util.DataProcessorUtil;
import com.cognizant.devops.platformservice.rest.neo4j.GraphDBService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Deprecated
@RestController
@RequestMapping("/admin/hierarchyDetails")
public class HierarchyDetailsService {
	static Logger log = LogManager.getLogger(GraphDBService.class.getName());
	private JsonObject asJsonObject;


	@RequestMapping(value = "/fetchDistinctHierarchyName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchEntityHierarchyName() {
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		List<String> hierarchyList = hierarchyDetailsDAL.fetchDistinctHierarchyName();
		return PlatformServiceUtil.buildSuccessResponseWithData(hierarchyList);
	}

	@Deprecated
	@RequestMapping(value = "/uploadHierarchyDetails", headers = ("content-type=multipart/*"), method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject uploadHierarchyDetails(@RequestParam("file") MultipartFile file,
			@RequestParam String action) {
		boolean status = false;
		try {
			if (null != action && action.equals("upload")) {
				status = DataProcessorUtil.getInstance().createBusinessHierarchyMetaData(file);
			} else if (null != action && action.equals("update")) {
				status = DataProcessorUtil.getInstance().updateHiearchyProperty(file);
			}
			if (!status) {
				return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
			}
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

		return PlatformServiceUtil.buildSuccessResponse();

	}

	@RequestMapping(value = "/getAllHierarchyDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

	private JsonObject populateHierarchyDetails(JsonArray array) {
		int rowCount = 0;
		List<List<String>> valueStore = new ArrayList<>();
		for (JsonElement element : array) {
			JsonElement jsonElement = element.getAsJsonObject().get("row").getAsJsonArray().get(0);
			List<String> valueList = new ArrayList<>();
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			if (jsonObject != null && jsonObject.get(DatataggingConstants.LEVEL1) != null) {
				String level1Value = jsonObject.get(DatataggingConstants.LEVEL1).getAsString();
				if (null != level1Value && !level1Value.isEmpty()) {
					valueList.add(level1Value);
				}
			}
			if (jsonObject != null && jsonObject.get(DatataggingConstants.LEVEL2) != null) {
				String level2Value = jsonObject.get(DatataggingConstants.LEVEL2).getAsString();
				if (null != level2Value && !level2Value.isEmpty()) {
					valueList.add(level2Value);
				}
			}
			if (jsonObject != null && jsonObject.get(DatataggingConstants.LEVEL3) != null) {
				String level3Value = jsonObject.get(DatataggingConstants.LEVEL3).getAsString();
				if (null != level3Value && !level3Value.isEmpty()) {
					valueList.add(level3Value);
				}
			}
			if (jsonObject != null && jsonObject.get(DatataggingConstants.LEVEL4) != null) {
				String level4Value = jsonObject.get(DatataggingConstants.LEVEL4).getAsString();
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
		jsonTree.addProperty(DatataggingConstants.NAME, root.getName());
		createJsonObject(root, jsonTree);
		return jsonTree;
	}

	private void createJsonObject(Node node, JsonObject parentJson) {
		JsonArray childArray = new JsonArray();
		// recurse
		for (Node childNode : node.getChildren()) {
			JsonObject childJson = new JsonObject();
			childJson.addProperty(DatataggingConstants.NAME, childNode.getName());
			childArray.add(childJson);
			createJsonObject(childNode, childJson);
		}
		if (childArray.size() != 0) {
			parentJson.add(DatataggingConstants.CHILDREN, childArray);
		}
	}

	@RequestMapping(value = "/getHierarchyProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject getHierarchyProperties(@RequestParam String level1, @RequestParam String level2,
			@RequestParam String level3, @RequestParam String level4) throws InsightsCustomException {
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

	@RequestMapping(value = "/getMetaData", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getMetaData() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:METADATA:DATATAGGING) return n";
		GraphResponse response;
		try {
			response = dbHandler.executeCypherQuery(query);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response.getNodes());

	}

}
