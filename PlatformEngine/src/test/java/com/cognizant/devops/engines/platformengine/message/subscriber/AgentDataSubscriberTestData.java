/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformengine.message.subscriber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformdataarchivalengine.test.engine.DataArchivalEngineData;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;

public class AgentDataSubscriberTestData {
	private static Logger log = LogManager.getLogger(DataArchivalEngineData.class.getName());

	public static String gitLabel = "GIT";
	
	public int readNeo4JData(String nodeName, String value) {
		int countOfRecords = 0;
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:" + nodeName + ") where n.authorName='" + value + "' return n";
		log.debug(" query  {} ", query);
		GraphResponse neo4jResponse;
		try {
			neo4jResponse = dbHandler.executeCypherQuery(query);
			JsonArray tooldataObject = neo4jResponse.getJson().get("results").getAsJsonArray();
			countOfRecords = tooldataObject.size();
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return countOfRecords;

	}
}
