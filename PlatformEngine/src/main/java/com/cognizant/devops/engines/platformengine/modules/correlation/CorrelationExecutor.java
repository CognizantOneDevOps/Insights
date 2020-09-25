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
package com.cognizant.devops.engines.platformengine.modules.correlation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.modules.correlation.model.Correlation;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.CorrelationConfig;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.relationshipconfig.RelationshipConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Vishal Ganjare (vganjare)
 */
public class CorrelationExecutor {
	private static final Logger log = LogManager.getLogger(CorrelationExecutor.class);
	private long maxCorrelationTime;
	private long lastCorrelationTime;
	private long currentCorrelationTime;
	private int dataBatchSize;
	/**
	 * Correlation execution starting point.
	 */
	public void execute() {
		CorrelationConfig correlationConfig = ApplicationConfigProvider.getInstance().getCorrelations();
		if (correlationConfig != null) {
			loadCorrelationConfiguration(correlationConfig);
			List<CorrelationConfiguration> correlations = getRelation();
			if (correlations.isEmpty()) { // No information in DB (CORRELATION_CONFIGURATION table)
				new DataExtractor().execute();
				List<Correlation> correlationsJson = loadCorrelationsFromFile();
				getRelationFromFile(correlationsJson, correlations);
			}
			if (correlations.isEmpty()) {
				log.error("No Correlation Configuration found in DB as well as in correlation.json");
				return;
			}
			for (CorrelationConfiguration correlation : correlations) {
				log.debug(" Correlation started for " + correlation.getRelationName());
				if (correlation.isSelfRelation()) {
					continue;
				}
				updateNodesMissingCorrelationFields(correlation);
				int availableRecords = 1;
				while (availableRecords > 0) {
					List<JsonObject> sourceDataList = loadDestinationData(correlation);
					availableRecords = sourceDataList.size();
					if (!sourceDataList.isEmpty()) {
						executeCorrelations(correlation, sourceDataList);
					} else {
						log.debug("No record found for Correlation of " + correlation.getRelationName());
					}
				}
				removeRawLabel(correlation);
				log.debug(" Correlation end for " + correlation.getRelationName());
			}
		} else {
			log.error("Correlation configuration is not provided in server-config.json.");
		}
	}

	

	/**
	 * Update the destination node max time where the correlation fields are
	 * missing.
	 * 
	 * @param correlation
	 */
	private void updateNodesMissingCorrelationFields(CorrelationConfiguration correlation) {
		String destinationLabelName = correlation.getDestinationLabelName();
		List<String> destinationFields = Arrays.asList(correlation.getDestinationFields().split("\\s*,\\s*"));
		StringBuffer cypher = new StringBuffer();
		cypher.append("MATCH (destination:RAW:DATA:").append(destinationLabelName).append(") ");
		cypher.append("where not exists(destination.maxCorrelationTime) AND ");
		cypher.append(" ");
		for (String field : destinationFields) {
			cypher.append("coalesce(size(destination.").append(field).append("), 0) = 0 AND ");
		}
		cypher.delete(cypher.length() - 4, cypher.length());
		cypher.append(" WITH distinct destination limit ").append(dataBatchSize).append(" ");
		cypher.append("set destination.maxCorrelationTime=").append(maxCorrelationTime)
				.append(" , destination.correlationTime=").append(maxCorrelationTime).append(" ");
		cypher.append("return count(distinct destination) as count");
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			int processedRecords = 1;
			while (processedRecords > 0) {
				long st = System.currentTimeMillis();
				GraphResponse response = dbHandler.executeCypherQuery(cypher.toString());
				processedRecords = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsInt();
				log.debug("Pre Processed " + correlation.getDestinationToolName() + " " + destinationLabelName
						+ " records: " + processedRecords + " in: " + (System.currentTimeMillis() - st) + " ms");
			}
		} catch (InsightsCustomException e) {
			log.error("Error occured while loading the destination data for correlations.", e);
		}
	}

	/**
	 * Identify the destination nodes which are available for building correlations
	 * and load the uuid for source nodes.
	 * 
	 * @param destination
	 * @param relName
	 * @return
	 */
	private List<JsonObject> loadDestinationData(CorrelationConfiguration correlation) {
		log.debug(" In loadDestinationData, lastCorrelationTime " + lastCorrelationTime + " maxCorrelationTime  "
				+ maxCorrelationTime + " currentCorrelationTime " + currentCorrelationTime);
		List<JsonObject> destinationDataList = new ArrayList<>();
		String destinationLabelName = correlation.getDestinationLabelName();
		List<String> destinationFields = Arrays.asList(correlation.getDestinationFields().split("\\s*,\\s*"));
		StringBuffer cypher = new StringBuffer();
		cypher.append("MATCH (destination:RAW:DATA:").append(destinationLabelName).append(") ");
		cypher.append("where not ((destination) <-[:").append(correlation.getRelationName()).append("]- (:DATA:")
				.append(correlation.getSourceLabelName()).append(")) ");
		cypher.append("AND (not exists(destination.correlationTime) OR ");
		cypher.append("destination.correlationTime < ").append(lastCorrelationTime).append(" ) ");
		cypher.append("AND (");
		for (String field : destinationFields) {
			cypher.append("exists(destination.").append(field).append(") OR ");
		}
		cypher.delete(cypher.length() - 3, cypher.length());
		cypher.append(") ");
		cypher.append("WITH distinct destination limit ").append(dataBatchSize).append(" ");
		cypher.append("WITH destination, []  ");
		for (String field : destinationFields) {
			cypher.append(" + CASE ");
			cypher.append(" 	WHEN exists(destination.").append(field).append(") THEN destination.").append(field)
					.append(" ");
			cypher.append(" 	ELSE [] ");
			cypher.append(" END ");
		}
		cypher.append("as values WITH destination.uuid as uuid, values UNWIND values as value WITH uuid, ");
		cypher.append(" CASE ");
		cypher.append(" 	WHEN (toString(value) contains \",\") THEN split(value, \",\") ");// If the value contains
		// comma, the split the
		// value
		cypher.append(" 	ELSE value ");
		cypher.append(" END as values ");
		cypher.append("UNWIND values as value WITH uuid, value where value <> \"\" ");
		cypher.append(
				"WITH distinct uuid, collect(distinct value) as values WITH { uuid : uuid, values : values} as data ");
		cypher.append("RETURN collect(data) as data");
		log.debug("DestinationData    " + cypher);
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			GraphResponse response = dbHandler.executeCypherQuery(cypher.toString());
			JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
			destinationDataList = new ArrayList<>();
			if (rows.isJsonNull() || rows.size() == 0 || rows.get(0).getAsJsonArray().size() == 0) {
				return destinationDataList;
			}
			JsonArray dataArray = rows.get(0).getAsJsonArray();
			for (JsonElement data : dataArray) {
				destinationDataList.add(data.getAsJsonObject());
			}
		} catch (InsightsCustomException e) {
			log.error("Error occured while loading the destination data for correlations.", e);
		}
		return destinationDataList;
	}

	/**
	 * Execute the correlations for the given set of source and destination nodes.
	 * 
	 * @param correlation
	 * @param dataList
	 * @param allowedSourceRelationCount
	 */
	private int executeCorrelations(CorrelationConfiguration correlation, List<JsonObject> dataList) {
		StringBuffer correlationCypher = new StringBuffer();
		String sourceField = correlation.getSourceFields(); // currently, we will support only one source field.
		correlationCypher.append("UNWIND {props} as properties ");
		correlationCypher.append("MATCH (destination:DATA:RAW:").append(correlation.getDestinationLabelName())
				.append(" {uuid: properties.uuid}) ");
		correlationCypher.append("set destination.correlationTime=").append(currentCorrelationTime).append(", ");
		correlationCypher.append("destination.maxCorrelationTime=coalesce(destination.maxCorrelationTime, ")
				.append(maxCorrelationTime).append(") WITH destination, properties ");
		correlationCypher.append("MATCH (source:DATA:").append(correlation.getSourceLabelName()).append(") ");
		correlationCypher.append("WHERE source.").append(sourceField).append(" IN properties.values ")
				.append("with source , destination ");
		correlationCypher.append("CREATE UNIQUE (source) -[r:").append(correlation.getRelationName())
				.append("]-> (destination) ");

		String propertyVal = appendRelationshipProperties(correlation);
		if (!propertyVal.isEmpty()) {
			correlationCypher.append("set ").append(propertyVal).append(" ");
		}
		correlationCypher.append("RETURN count(distinct destination) as count");
		log.debug("CorrelationExecution Started   " + correlationCypher);
		GraphDBHandler dbHandler = new GraphDBHandler();
		JsonObject correlationExecutionResponse;
		int processedRecords = 0;
		try {
			long st = System.currentTimeMillis();
			correlationExecutionResponse = dbHandler.bulkCreateNodes(dataList, null, correlationCypher.toString());
			log.debug(correlationExecutionResponse);
			processedRecords = correlationExecutionResponse.get("response").getAsJsonObject().get("results")
					.getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject()
					.get("row").getAsInt();
			log.debug("Correlated " + correlation.getDestinationToolName() + " " + correlation.getDestinationLabelName()
					+ " records: " + processedRecords + " in: " + (System.currentTimeMillis() - st) + " ms");
		} catch (InsightsCustomException e) {
			log.error("Error occured while executing correlations for relation " + correlation.getRelationName() + ".",
					e);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					" Error occured while executing correlations for relation " + e.getMessage(),
					PlatformServiceConstants.FAILURE);
		}
		return processedRecords;
	}
	private String appendRelationshipProperties(CorrelationConfiguration configuration) {

		StringBuilder propertyValueBuilder = new StringBuilder();
		List<RelationshipConfiguration> allRelationshipConfigList = new ArrayList<>(
				configuration.getRelationshipConfig());
		for (RelationshipConfiguration rc : allRelationshipConfigList) {
			String operationName = rc.getOperation();
			if (operationName.equals("DIFF")) {
				String propValue = RelationshipPropertiesUtil.calDiff(rc.getOperationjson());
				propValue = "r." + rc.getFieldValue() + "=" + propValue;
				if (!propertyValueBuilder.toString().isEmpty()) {
					propertyValueBuilder.append(",").append(propValue);
				} else {
					propertyValueBuilder.append(propValue);
				}
			}
			if (operationName.equals("SUM")) {
				String propValue = RelationshipPropertiesUtil.calSUM(rc.getOperationjson());
				propValue = "r." + rc.getFieldValue() + "=" + propValue;
				if (!propertyValueBuilder.toString().isEmpty()) {
					propertyValueBuilder.append(",").append(propValue);
				} else {
					propertyValueBuilder.append(propValue);
				}
			}
		}
		return propertyValueBuilder.toString();
	}
	
	private int removeRawLabel(CorrelationConfiguration destination) {
		StringBuffer correlationCypher = new StringBuffer();
		correlationCypher.append("MATCH (destination:DATA:RAW:").append(destination.getDestinationLabelName())
				.append(") ");
		correlationCypher.append("where destination.maxCorrelationTime < ").append(currentCorrelationTime).append(" ");
		correlationCypher.append("WITH destination limit ").append(dataBatchSize).append(" ");
		correlationCypher
				.append("remove destination.maxCorrelationTime, destination.correlationTime, destination:RAW ");
		correlationCypher.append("return count(distinct destination) ");
		GraphDBHandler dbHandler = new GraphDBHandler();
		JsonObject correlationExecutionResponse;
		int processedRecords = 1;
		try {
			while (processedRecords > 0) {
				long st = System.currentTimeMillis();
				correlationExecutionResponse = dbHandler.executeCypherQuery(correlationCypher.toString()).getJson();
				log.debug(correlationExecutionResponse);
				processedRecords = correlationExecutionResponse.get("results").getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsInt();
				log.debug("Processed " + processedRecords + " records in " + (System.currentTimeMillis() - st) + " ms");
			}
		} catch (InsightsCustomException e) {
			log.error(
					"Error occured while removing RAW label from tool: " + destination.getDestinationLabelName() + ".",
					e);
		}
		return processedRecords;
	}

	/**
	 * Load the correlation.json and population Correlations object.
	 * 
	 * @return
	 */
	private List<Correlation> loadCorrelationsFromFile() {
		BufferedReader reader = null;
		InputStream in = null;
		List<Correlation> correlations = null;
		File correlationTemplate = new File(ConfigOptions.CORRELATION_FILE_RESOLVED_PATH);
		try {
			if (correlationTemplate.exists()) {
				reader = new BufferedReader(new FileReader(correlationTemplate));
			} else {
				in = getClass().getResourceAsStream("/" + ConfigOptions.CORRELATION_TEMPLATE);
				reader = new BufferedReader(new InputStreamReader(in));
			}
			Correlation[] correlationArray = new Gson().fromJson(reader, Correlation[].class);
			correlations = Arrays.asList(correlationArray);
		} catch (FileNotFoundException e) {
			log.error("Correlations.json file not found.", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				log.error("Unable to read the correlation.json file.", e);
			}
		}
		return correlations;
	}

	private void getRelationFromFile(List<Correlation> correlationsJson, List<CorrelationConfiguration> correlations) {

		for (Correlation correlation : correlationsJson) {
			CorrelationConfiguration correlationConfiguration = new CorrelationConfiguration();
			correlationConfiguration.setSourceToolName(correlation.getSource().getToolName());
			correlationConfiguration.setSourceToolCategory(correlation.getSource().getToolCategory());
			if (correlation.getSource().getLabelName().isEmpty()) {
				correlationConfiguration.setSourceLabelName(correlation.getSource().getToolName());
			} else {
				correlationConfiguration.setSourceLabelName(correlation.getSource().getLabelName());
			}
			correlationConfiguration.setSourceFields(String.join(",", correlation.getSource().getFields()));

			correlationConfiguration.setDestinationToolName(correlation.getDestination().getToolName());
			correlationConfiguration.setDestinationToolCategory(correlation.getDestination().getToolCategory());
			if (correlation.getDestination().getLabelName().isEmpty()) {
				correlationConfiguration.setDestinationLabelName(correlation.getDestination().getToolName());
			} else {
				correlationConfiguration.setDestinationLabelName(correlation.getDestination().getLabelName());
			}
			correlationConfiguration.setDestinationFields(String.join(",", correlation.getDestination().getFields()));
			correlationConfiguration.setRelationName(correlation.getRelationName());
			correlations.add(correlationConfiguration);
		}

	}

	private List<CorrelationConfiguration> getRelation() {
		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		List<CorrelationConfiguration> correlationList = new ArrayList<>();
		try {
			correlationList = correlationConfigDAL.getActiveCorrelations();
		} catch (Exception e) {
			EngineStatusLogger.getInstance().createEngineStatusNode("unable to get correlation from database",
					PlatformServiceConstants.FAILURE);
		}
		return correlationList;
	}

	/**
	 * Update the correlation time variables.
	 */
	private void loadCorrelationConfiguration(CorrelationConfig correlations) {
		dataBatchSize = correlations.getBatchSize();
		currentCorrelationTime = System.currentTimeMillis() / 1000;
		maxCorrelationTime = currentCorrelationTime + correlations.getCorrelationWindow() * 60 * 60;
		lastCorrelationTime = currentCorrelationTime - correlations.getCorrelationFrequency() * 60 * 60;
	}

}