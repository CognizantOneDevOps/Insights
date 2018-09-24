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
package com.cognizant.devops.platformengine.modules.correlation;

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

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.CorrelationConfig;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jFieldIndexRegistry;
import com.cognizant.devops.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformengine.modules.correlation.model.Correlation;
import com.cognizant.devops.platformengine.modules.correlation.model.CorrelationNode;
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
		if(correlationConfig != null) {
			new DataExtractor().execute();
			loadCorrelationConfiguration(correlationConfig);
			List<Correlation> correlations = loadCorrelations();
			if(correlations == null) {
				log.error("Unable to load correlations");
				return;
			}
			for(Correlation correlation: correlations) {
				applyFieldIndices(correlation);
				updateNodesMissingCorrelationFields(correlation.getDestination());
				int availableRecords = 1;
				while(availableRecords > 0) {
					List<JsonObject> sourceDataList = loadDestinationData(correlation.getDestination(), correlation.getSource(), correlation.getRelationName());
					availableRecords = sourceDataList.size();
					if(sourceDataList.size() > 0) {
						executeCorrelations(correlation, sourceDataList, correlation.getRelationName());
					}
				}
				removeRawLabel(correlation.getDestination());
			}
		}else {
			log.error("Correlation configuration is not provided.");
		}
	}
	
	/**
	 * Identify the correlation fields and add index for these fields
	 * @param correlation
	 */
	private void applyFieldIndices(Correlation correlation) {
		CorrelationNode source = correlation.getSource();
		String sourceToolName = source.getToolName();
		List<String> sourceFields = source.getFields();
		for(String sourceField : sourceFields) {
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex(sourceToolName, sourceField);
		}
		CorrelationNode destination = correlation.getDestination();
		String destinationToolName = destination.getToolName();
		List<String> destinationFields = destination.getFields();
		for(String destinationField : destinationFields) {
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex(destinationToolName, destinationField);
		}
	}
	
	/**
	 * Update the destination node max time where the correlation fields are missing.
	 * @param destination
	 */
	private void updateNodesMissingCorrelationFields(CorrelationNode destination) {
		String destinationToolName = destination.getToolName();
		List<String> fields = destination.getFields();
		StringBuffer cypher = new StringBuffer();
		cypher.append("MATCH (destination:RAW:DATA:").append(destinationToolName).append(") ");
		cypher.append("where not exists(destination.maxCorrelationTime) AND ");
		cypher.append(" ");
		for(String field : fields) {
			cypher.append("coalesce(size(destination.").append(field).append("), 0) = 0 AND ");
		}
		cypher.delete(cypher.length()-4, cypher.length());
		cypher.append(" WITH distinct destination limit ").append(dataBatchSize).append(" ");
		cypher.append("set destination.maxCorrelationTime=").append(maxCorrelationTime).append(" , destination.correlationTime=").append(maxCorrelationTime).append(" ");
		cypher.append("return count(distinct destination) as count");
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			int processedRecords = 1;
			while(processedRecords > 0) {
				long st = System.currentTimeMillis();
				GraphResponse response = dbHandler.executeCypherQuery(cypher.toString());
				processedRecords = response.getJson()
						.get("results").getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(0).getAsJsonObject()
						.get("row").getAsInt();
				log.debug("Pre Processed "+destinationToolName+" records: "+processedRecords+" in: "+(System.currentTimeMillis() - st) + " ms");
			}
		} catch (GraphDBException e) {
			log.error("Error occured while loading the destination data for correlations.", e);
		}
	}
	
	/**
	 * Identify the destination nodes which are available for building correlations and load the uuid for source nodes.
	 * @param destination
	 * @param relName
	 * @return
	 */
	private List<JsonObject> loadDestinationData(CorrelationNode destination, CorrelationNode source, String relName) {
		List<JsonObject> destinationDataList = null;
		String destinationToolName = destination.getToolName();
		List<String> fields = destination.getFields();
		StringBuffer cypher = new StringBuffer();
		cypher.append("MATCH (destination:RAW:DATA:").append(destinationToolName).append(") ");
		cypher.append("where not ((destination) <-[:").append(relName).append("]- (:DATA:").append(source.getToolName()).append(")) ");
		cypher.append("AND (not exists(destination.correlationTime) OR ");
		cypher.append("destination.correlationTime < ").append(lastCorrelationTime).append(" ) ");
		cypher.append("AND (");
		for(String field : fields) {
			cypher.append("exists(destination.").append(field).append(") OR ");
		}
		cypher.delete(cypher.length()-3, cypher.length());
		cypher.append(") ");
		cypher.append("WITH distinct destination limit ").append(dataBatchSize).append(" ");
		cypher.append("WITH destination, []  ");
		for(String field : fields) {
			cypher.append(" + CASE ");
			cypher.append(" 	WHEN exists(destination.").append(field).append(") THEN destination.").append(field).append(" ");
			cypher.append(" 	ELSE [] ");
			cypher.append(" END ");
		}
		//cypher.append("as values WITH destination.uuid as uuid, values UNWIND values as value WITH uuid, value where value <> \"\" ");
		//cypher.append("coalesce(size(destination.").append(field).append("), 0) = 0 AND ");
		cypher.append("as values WITH destination.uuid as uuid, values UNWIND values as value WITH uuid, ");
		cypher.append(" CASE ");
		cypher.append(" 	WHEN (toString(value) contains \",\") THEN split(value, \",\") "); //If the value contains comma, the split the value
		cypher.append(" 	ELSE value "); 
		cypher.append(" END as values ");
		cypher.append("UNWIND values as value WITH uuid, value where value <> \"\" ");
		cypher.append("WITH distinct uuid, collect(distinct value) as values WITH { uuid : uuid, values : values} as data ");
		cypher.append("RETURN collect(data) as data");
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			GraphResponse response = dbHandler.executeCypherQuery(cypher.toString());
			JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
			destinationDataList = new ArrayList<JsonObject>();
			if(rows.isJsonNull() || rows.size() == 0 || rows.get(0).getAsJsonArray().size() == 0) {
				return destinationDataList;
			}
			JsonArray dataArray = rows.get(0).getAsJsonArray();
			for(JsonElement data : dataArray) {
				destinationDataList.add(data.getAsJsonObject());
			}
		} catch (GraphDBException e) {
			log.error("Error occured while loading the destination data for correlations.", e);
		}
		return destinationDataList;
	}
	
	/**
	 * Execute the correlations for the given set of source and destination nodes.
	 * @param correlation
	 * @param dataList
	 * @param allowedSourceRelationCount
	 */
	private int executeCorrelations(Correlation correlation, List<JsonObject> dataList, String relationName) {
		CorrelationNode source = correlation.getSource(); 
		CorrelationNode destination = correlation.getDestination();
		StringBuffer correlationCypher = new StringBuffer();
		String sourceField = source.getFields().get(0); //currently, we will support only one source field.
		correlationCypher.append("UNWIND {props} as properties ");
		correlationCypher.append("MATCH (destination:DATA:RAW:").append(destination.getToolName()).append(" {uuid: properties.uuid}) ");
		correlationCypher.append("set destination.correlationTime=").append(currentCorrelationTime).append(", ");
		correlationCypher.append("destination.maxCorrelationTime=coalesce(destination.maxCorrelationTime, ").append(maxCorrelationTime).append(") WITH destination, properties ");
		correlationCypher.append("MATCH (source:DATA:").append(source.getToolName()).append(") ");
		correlationCypher.append("WHERE source.").append(sourceField).append(" IN properties.values ");
		correlationCypher.append("CREATE UNIQUE (source) -[r:").append(relationName).append("]-> (destination) ");
		correlationCypher.append("RETURN count(distinct destination) as count");
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		JsonObject correlationExecutionResponse;
		int processedRecords = 0;
		try {
			long st = System.currentTimeMillis();
			correlationExecutionResponse = dbHandler.bulkCreateNodes(dataList, null, correlationCypher.toString());
			log.debug(correlationExecutionResponse);
			processedRecords = correlationExecutionResponse.get("response").getAsJsonObject()
					.get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject()
					.get("row").getAsInt();
			log.debug("Correlated "+destination.getToolName()+" records: "+processedRecords+" in: "+(System.currentTimeMillis() - st) + " ms");
		} catch (GraphDBException e) {
			log.error("Error occured while executing correlations for relation "+relationName+".", e);
			EngineStatusLogger.getInstance().createEngineStatusNode(" Error occured while executing correlations for relation "+e.getMessage(),PlatformServiceConstants.FAILURE);
		}
		return processedRecords;
	}
	
	private int removeRawLabel(CorrelationNode destination) {
		StringBuffer correlationCypher = new StringBuffer();
		correlationCypher.append("MATCH (destination:DATA:RAW:").append(destination.getToolName()).append(") ");
		correlationCypher.append("where destination.maxCorrelationTime < ").append(currentCorrelationTime).append(" ");
		correlationCypher.append("WITH destination limit ").append(dataBatchSize).append(" ");
		correlationCypher.append("remove destination.maxCorrelationTime, destination.correlationTime, destination:RAW ");
		correlationCypher.append("return count(distinct destination) ");
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		JsonObject correlationExecutionResponse;
		int processedRecords = 1;
		try {
			while(processedRecords > 0) {
				long st = System.currentTimeMillis();
				correlationExecutionResponse = dbHandler.executeCypherQuery(correlationCypher.toString()).getJson();
				log.debug(correlationExecutionResponse);
				processedRecords = correlationExecutionResponse
						.get("results").getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(0).getAsJsonObject()
						.get("row").getAsInt();
				log.debug("Processed "+processedRecords+" records in "+(System.currentTimeMillis() - st) + " ms");
			}
		} catch (GraphDBException e) {
			log.error("Error occured while removing RAW label from tool: "+destination.getToolName()+".", e);
		}
		return processedRecords;
	}
	
	/**
	 * Load the correlation.json and population Correlations object.
	 * @return
	 */
	private List<Correlation> loadCorrelations() {
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
	
	/**
	 * Update the correlation time variables.
	 */
	private void loadCorrelationConfiguration(CorrelationConfig correlations) {
		dataBatchSize = correlations.getBatchSize();
		currentCorrelationTime = System.currentTimeMillis()/1000;
		maxCorrelationTime = currentCorrelationTime + correlations.getCorrelationWindow() * 60 * 60;
		lastCorrelationTime = currentCorrelationTime - correlations.getCorrelationFrequency() * 60 * 60;
	}
}