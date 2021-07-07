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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("businessMappingService")
public class BusinessMappingServiceImpl implements BusinessMappingService {

	static Logger log = LogManager.getLogger(BusinessMappingServiceImpl.class.getName());




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
			log.debug("arg0 {} ", json);
			nodeProperties.add(json);
			JsonObject graphResponse = dbHandler.bulkCreateNodes(nodeProperties, null, query);
			if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
					.getAsJsonArray().size() > 0) {
				log.error(graphResponse);
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
		List<Map<String, String>> propertyList = new ArrayList<>();
		try {
			response = dbHandler.executeCypherQuery(query);
			int size = response.getNodes().size();
			log.debug("arg0 size {}", size);
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
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(agentMappingJson);
			GraphDBHandler dbHandler = new GraphDBHandler();
			JsonParser parser = new JsonParser();
			JsonObject json = (JsonObject) parser.parse(validatedResponse);
			String uuid = json.get("uuid").getAsString();
			log.debug("arg0 {} uuid  ", uuid);
			JsonArray asJsonArray = getCurrentRecords(uuid, dbHandler);
			log.debug("arg0 {} ", asJsonArray);
			String replaceString = agentMappingJson.replaceAll("\\\"(\\w+)\\\"\\:", "$1:");
			String updateCypherQuery = " MATCH (n :METADATA:BUSINESSMAPPING {uuid:'" + uuid + "'})SET n ="
					+ replaceString + " RETURN n";
			log.debug("to {} replace", updateCypherQuery);
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
		return rows.getAsJsonArray();
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
