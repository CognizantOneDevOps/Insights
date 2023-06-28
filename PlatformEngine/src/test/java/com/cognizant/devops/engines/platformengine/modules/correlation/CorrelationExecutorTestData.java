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
package com.cognizant.devops.engines.platformengine.modules.correlation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.test.engine.CorrelationJson;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.google.gson.JsonObject;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

public class CorrelationExecutorTestData {
	private static Logger log = LogManager.getLogger(CorrelationExecutorTestData.class.getName());
	
	public CorrelationConfiguration saveConfig(String config) throws InsightsCustomException {
		
				CorrelationJson correlation = loadCorrelation(config);
				CorrelationConfiguration correlationConfig = new CorrelationConfiguration();
				correlationConfig.setSourceToolName(correlation.getSource().getToolName());
				correlationConfig.setSourceToolCategory(correlation.getSource().getToolCategory());
				if (null == correlation.getSource().getLabelName()) {
					correlationConfig.setSourceLabelName(correlation.getSource().getToolName());
				} else {
					correlationConfig.setSourceLabelName(correlation.getSource().getLabelName());
				}
				correlationConfig.setSourceFields(String.join(",", correlation.getSource().getFields()));
				correlationConfig.setDestinationToolName(correlation.getDestination().getToolName());
				correlationConfig.setDestinationToolCategory(correlation.getDestination().getToolCategory());
				correlationConfig.setDestinationLabelName(correlation.getDestination().getLabelName());
				if (null == correlation.getDestination().getLabelName()) {
					correlationConfig.setDestinationLabelName(correlation.getDestination().getToolName());
				} else {
					correlationConfig.setDestinationLabelName(correlation.getDestination().getLabelName());
				}
				correlationConfig.setDestinationFields(String.join(",", correlation.getDestination().getFields()));
				correlationConfig.setRelationName(correlation.getRelationName());
				if (correlation.getPropertyList().length > 0) {
					correlationConfig.setPropertyList(String.join(",", correlation.getPropertyList()));
				}
				correlationConfig.setEnableCorrelation(true);
				correlationConfig.setSelfRelation(false);
		
				CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
				correlationConfigDAL.saveCorrelationConfig(correlationConfig);
				return correlationConfig;
		
			}
			
			private CorrelationJson loadCorrelation(String config) {
				JsonObject json = JsonUtils.parseStringAsJsonObject(config);
				CorrelationJson correlation = new Gson().fromJson(json, CorrelationJson.class);
				return correlation;
			}
			
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
