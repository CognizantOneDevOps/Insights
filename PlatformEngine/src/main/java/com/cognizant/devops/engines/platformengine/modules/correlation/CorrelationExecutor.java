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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.modules.correlation.model.Correlation;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.CorrelationConfig;
import com.cognizant.devops.platformcommons.constants.EngineConstants;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformdal.relationshipconfig.RelationshipConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * @author Vishal Ganjare (vganjare)
 */
public class CorrelationExecutor {
	
	public static final String RESULT = "results";
	private static final Logger log = LogManager.getLogger(CorrelationExecutor.class);
	private long maxCorrelationTime;
	private long lastCorrelationTime;
	private long currentCorrelationTime;
	private int dataBatchSize;
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	private Map<String,String> loggingInfo = new ConcurrentHashMap<>();
	private String jobName = ""; 
	
	/**
	 * Correlation execution starting point.
	 */
	public void execute(String jobName) {
		loggingInfo.put(EngineConstants.EXECID, String.valueOf(System.currentTimeMillis()));
		this.jobName = jobName;
		CorrelationConfig correlationConfig = ApplicationConfigProvider.getInstance().getCorrelations();
		if (correlationConfig != null) {
			loadCorrelationConfiguration(correlationConfig);
			List<CorrelationConfiguration> correlations = getRelation();
			if (correlations.isEmpty()) {
				// No information in DB fetch correlation detail from file
				loadCorrelationConfigfromJson(correlations);		
			}
			if (correlations.isEmpty()) {
				log.error(" execId={} No Correlation Configuration found in DB as well as in correlation.json",loggingInfo.get(EngineConstants.EXECID));
				return;
			}
			for (CorrelationConfiguration correlation : correlations) {
				loggingInfo.put(EngineConstants.SOURCE_TOOL, String.valueOf(correlation.getSourceToolName()));
				loggingInfo.put(EngineConstants.DESTINATION_TOOL, String.valueOf(correlation.getDestinationToolName()));
				loggingInfo.put(EngineConstants.CORRELATION_NAME, String.valueOf(correlation.getRelationName()));
				log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} Correlation started for {}",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),0,0, correlation.getRelationName());
				
				updateNodesMissingCorrelationFields(correlation);
				
				loadDestinationDataAndExecuteCorrelation(correlation);
				
				if (!correlation.isSelfRelation()) {
					removeRawLabel(correlation);
				}
				log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} Correlation end for{}",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),0,0, correlation.getRelationName());
			}
		} else {
			log.error(" execId={} correlationName={} sourceTool={} destinationTool={} Correlation configuration is not provided in server-config.json.",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL));
		}
	}

	private void loadDestinationDataAndExecuteCorrelation(CorrelationConfiguration correlation) {
		int availableRecords = 1;
		while (availableRecords > 0) {
			List<JsonObject> sourceDataList = loadDestinationData(correlation);
			availableRecords = sourceDataList.size();
			log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} fetch Destination tool record {}" ,loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),availableRecords);
			if (!sourceDataList.isEmpty()) {
				executeCorrelations(correlation, sourceDataList);
			} else {
				log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} No record found for Correlation of {}" ,loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),0,0, correlation.getRelationName());
			}
		}
	}
   
	public void loadCorrelationConfigfromJson(List<CorrelationConfiguration> correlations) { 
		List<Correlation> correlationsList = loadCorrelationsFromFile();
		if(correlationsList != null && !correlationsList.isEmpty()) {
			convertCorrelationDTO(correlationsList, correlations);
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
		StringBuilder cypher = new StringBuilder();
		cypher.append("MATCH (destination:RAW:DATA:").append(destinationLabelName).append(") ");
		cypher.append("where destination.maxCorrelationTime IS NULL AND ");
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
				processedRecords = response.getJson().get(RESULT).getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsInt();
				log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} Pre Processed {} {}  records: {} in: {} ms",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),(System.currentTimeMillis() - st),processedRecords,correlation.getDestinationToolName(),destinationLabelName,processedRecords,(System.currentTimeMillis() - st));
			}
		} catch (InsightsCustomException e) {
			log.error(" execId={} correlationName={} sourceTool={} destinationTool={} Error occured while loading the destination data for correlations.",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL), e);
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
		log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} In loadDestinationData, lastCorrelationTime {} maxCorrelationTime  {} currentCorrelationTime {}",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),0,0,lastCorrelationTime,maxCorrelationTime,currentCorrelationTime);
		List<JsonObject> destinationDataList = new ArrayList<>();
		
		String destinationLabelName = correlation.getDestinationLabelName();
		List<String> destinationFields = Arrays.asList(correlation.getDestinationFields().split("\\s*,\\s*"));
		
		StringBuilder cypher = new StringBuilder();
		cypher.append("MATCH (destination:RAW:DATA:").append(destinationLabelName).append(") ");
		cypher.append("where not ((destination) <-[:").append(correlation.getRelationName()).append("]- (:DATA:")
				.append(correlation.getSourceLabelName()).append(")) ");
		cypher.append("AND (destination.correlationTime IS NULL OR ");
		cypher.append("destination.correlationTime < ").append(lastCorrelationTime).append(" ) ");
		cypher.append("AND (");
		
		for (String field : destinationFields) {
			cypher.append("destination.").append(field).append(" IS NOT NULL OR ");
		}
		
		cypher.delete(cypher.length() - 3, cypher.length());
		cypher.append(") ");
		cypher.append("WITH distinct destination limit ").append(dataBatchSize).append(" ");
		cypher.append("WITH destination, []  ");
		for (String field : destinationFields) {
			cypher.append(" + CASE ");
			cypher.append(" WHEN destination.").append(field).append(" IS NOT NULL THEN destination.").append(field)
					.append(" ");
			cypher.append(" ELSE [] ");
			cypher.append(" END ");
		}
		cypher.append("as values WITH destination.uuid as uuid, values UNWIND values as value WITH uuid, ");
		cypher.append(" CASE ");
		// If the value contains comma, the split the value
		cypher.append(" WHEN (toString(value) contains \",\") THEN split(value, \",\") ");
		cypher.append(" ELSE value ");
		cypher.append(" END as values ");
		cypher.append("UNWIND values as value WITH uuid, value where value <> \"\" ");
		cypher.append(
				"WITH distinct uuid, collect(distinct value) as values WITH { uuid : uuid, values : values} as data ");
		cypher.append("RETURN collect(data) as data");
		log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} DestinationData {} ", loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),0,0,cypher);
		
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			
			GraphResponse response = dbHandler.executeCypherQuery(cypher.toString());
			
			JsonArray rows = response.getJson().get(RESULT).getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
			
			if (rows.isJsonNull() || rows.size() == 0 || rows.get(0).getAsJsonArray().size() == 0) {
				return destinationDataList;
			}
			JsonArray dataArray = rows.get(0).getAsJsonArray();
			
			for (JsonElement data : dataArray) {
				destinationDataList.add(data.getAsJsonObject());
			}
			
		} catch (InsightsCustomException e) {
			log.error(" execId={} correlationName={} sourceTool={} destinationTool={} Error occured while loading the destination data for correlations.",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL), e);
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
		
		StringBuilder correlationCypher = new StringBuilder();
		String sourceField = correlation.getSourceFields(); // currently, we will support only one source field.
		
		correlationCypher.append("UNWIND $props as properties ");
		correlationCypher.append("MATCH (destination:DATA:RAW:").append(correlation.getDestinationLabelName())
				.append(" {uuid: properties.uuid}) ");
		correlationCypher.append("set destination.correlationTime=").append(currentCorrelationTime).append(", ");
		correlationCypher.append("destination.maxCorrelationTime=coalesce(destination.maxCorrelationTime, ")
				.append(maxCorrelationTime).append(") WITH destination, properties ");
		correlationCypher.append("MATCH (source:DATA:").append(correlation.getSourceLabelName()).append(") ");
		correlationCypher.append("WHERE source.").append(sourceField).append(" IN properties.values ")
				.append("with source , destination ");
		 correlationCypher.append("MERGE (source) -[r:").append(correlation.getRelationName())
		 .append(" {uuid:(source.uuid+'.'+destination.uuid)} ]-> (destination) ");
	
		String propertyVal = appendRelationshipProperties(correlation);
		
		if (!propertyVal.isEmpty()) {
			correlationCypher.append("set ").append(propertyVal).append(" ");
		}
		
		correlationCypher.append("RETURN count(distinct destination) as count");
		log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} CorrelationExecution Started executeCorrelations {} ",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),0,0, correlationCypher);
		
		GraphDBHandler dbHandler = new GraphDBHandler();
		JsonObject correlationExecutionResponse;
		int processedRecords = 0;
		try {
			
			long st = System.currentTimeMillis();
			
			correlationExecutionResponse = dbHandler.bulkCreateNodes(dataList,correlationCypher.toString());

			log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} CorrelationExecution Response = {} ",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),0,0,correlationExecutionResponse);
			
			processedRecords = correlationExecutionResponse.get("response").getAsJsonObject().get(RESULT)
					.getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject()
					.get("row").getAsInt();
			log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} Processed Records for correlation= {}  ",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),(System.currentTimeMillis() - st),processedRecords,processedRecords);
		
		} catch (InsightsCustomException e) {
			log.error(" execId={} correlationName={} sourceTool={} destinationTool={} Error occured while executing correlations",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Error occured while executing correlations for relation " + e.getMessage(),
					PlatformServiceConstants.FAILURE, jobName);
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
		
		StringBuilder correlationCypher = new StringBuilder();
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
				log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} CorrelationExecution Response={}",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),0,0,correlationExecutionResponse);
				processedRecords = correlationExecutionResponse.get(RESULT).getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsInt();
				log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} Processed records for label removal: {}  ms",loggingInfo.get(EngineConstants.EXECID),loggingInfo.get(EngineConstants.CORRELATION_NAME),loggingInfo.get(EngineConstants.SOURCE_TOOL),loggingInfo.get(EngineConstants.DESTINATION_TOOL),(System.currentTimeMillis() - st),processedRecords,processedRecords);
			}
		} catch (InsightsCustomException e) {
			log.error(" Error occured while removing RAW label from tool:{}",destination.getDestinationLabelName(),
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
		
		List<Correlation> correlations = null;
		
		try {
			
			List<InsightsConfigFiles> configFile = configFilesDAL
					.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.CORRELATION.name());
			if (configFile != null && !configFile.isEmpty()) {
				
				String configFileData = new String(configFile.get(0).getFileData(), MQMessageConstants.MESSAGE_ENCODING);
				Correlation[] correlationArray = new Gson().fromJson(configFileData, Correlation[].class);
				correlations = Arrays.asList(correlationArray);
				
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Correlation.json is successfully loaded.",
						PlatformServiceConstants.SUCCESS, jobName);
				
				log.debug(" Type=Correlator execId={} correlationName={} sourceTool={} destinationTool={} ProcessingTime={} processedRecords={} Correlation.json is successfully loaded.",loggingInfo.get(EngineConstants.EXECID),"-","-","-",0,0);
			
			} else {
				log.error(" execId={} Correlation.json not found in DB.",loggingInfo.get(EngineConstants.EXECID));
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Correlation.json not found in DB.",
						PlatformServiceConstants.FAILURE, jobName);
			}
			
		} catch (JsonSyntaxException e) {
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Correlation.json is not formatted.",
					PlatformServiceConstants.FAILURE, jobName);
			log.error(" execId={} Correlation.json is not formatted",loggingInfo.get(EngineConstants.EXECID));
		} catch (Exception e) {
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Error while loading Correlation.json.",
					PlatformServiceConstants.FAILURE, jobName);
			log.error(" execId={} Exception while loading Correlation.json",loggingInfo.get(EngineConstants.EXECID));
		}
		
		return correlations;
	}

	private void convertCorrelationDTO(List<Correlation> correlationsJson, List<CorrelationConfiguration> correlations) {

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
			log.error(" execId={} unable to get correlation from database",loggingInfo.get(EngineConstants.EXECID));
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("unable to get correlation from database",
					PlatformServiceConstants.FAILURE, jobName);
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