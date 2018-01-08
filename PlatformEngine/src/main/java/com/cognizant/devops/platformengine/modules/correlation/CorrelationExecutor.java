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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
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
	private static final Logger log = Logger.getLogger(CorrelationExecutor.class);
	private long maxPreviousCorrelationTime;
	private long minPreviousCorrelationTime;
	private long currentCorrelationTime;
	
	private int dataBatchSize = 2000;
	
	/**
	 * Correlation execution starting point.
	 */
	public void execute() {
		updateCorrelationTimeVars();
		List<Correlation> correlations = loadCorrelations();
		if(correlations == null) {
			log.error("Unable to load correlations");
			return;
		}
		Map<String, List<String>> sourceCorrelationMapping = buildSourceCorrelationMapping(correlations);
		for(Correlation correlation: correlations) {
			String relationName = buildRelationName(correlation);
			int processedRecords = 1;
			while(processedRecords > 0) {
				List<JsonObject> sourceDataList = loadSourceData(correlation.getSource(), relationName);
				processedRecords = executeCorrelations(correlation, sourceDataList, sourceCorrelationMapping.get(correlation.getSource().getToolName()).size());
				//remove RAW labels
			}
		}
	}
	
	/**
	 * Identify the source nodes which are available for building correlations and load the uuid for source nodes.
	 * @param source
	 * @param relName
	 * @return
	 */
	private List<JsonObject> loadSourceData(CorrelationNode source, String relName) {
		List<JsonObject> sourceDataList = null;
		String sourceToolName = source.getToolName();
		List<String> fields = source.getFields();
		StringBuffer cypher = new StringBuffer();
		cypher.append("MATCH (source:DATA:RAW:").append(sourceToolName).append(") ");
		cypher.append("where (not exists(source.rels) OR not \"").append(relName).append("\" in source.rels) ");
		cypher.append("OR (not exists(source.correlationTime) OR ");
		cypher.append("( source.correlationTime <= ").append(maxPreviousCorrelationTime).append(" ");
		cypher.append("AND source.correlationTime > ").append(minPreviousCorrelationTime).append(")) ");
		cypher.append("WITH source limit ").append(dataBatchSize).append(" ");
		cypher.append("WITH source, [] ");
		for(String field : fields) {
			cypher.append("+ split(coalesce(source.").append(field).append(", \"\"),\",\") ");
		}
		cypher.append("as values WITH source.uuid as uuid, values UNWIND values as value WITH uuid, value where value <> \"\" ");
		cypher.append("WITH distinct uuid, collect(distinct value) as values WITH { uuid : uuid, values : values} as data ");
		cypher.append("RETURN collect(data) as data");
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			GraphResponse response = dbHandler.executeCypherQuery(cypher.toString());
			JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
			if(rows.isJsonNull() || rows.size() == 0 || rows.get(0).getAsJsonArray().size() == 0) {
				return null;
			}
			JsonArray dataArray = rows.get(0).getAsJsonArray();
			sourceDataList = new ArrayList<JsonObject>();
			for(JsonElement data : dataArray) {
				sourceDataList.add(data.getAsJsonObject());
			}
		} catch (GraphDBException e) {
			log.error("Error occured while loading the source data for correlations.", e);
		}
		return sourceDataList;
	}
	
	/**
	 * Execute the correlations for the given set of source and destination nodes.
	 * @param correlation
	 * @param dataList
	 * @param allowedSourceRelationCount
	 */
	private int executeCorrelations(Correlation correlation, List<JsonObject> dataList, int allowedSourceRelationCount) {
		CorrelationNode source = correlation.getSource(); 
		CorrelationNode destination = correlation.getDestination();
		StringBuffer correlationCypher = new StringBuffer();
		String destinationField = destination.getFields().get(0); //currently, we will support only one destination field.
		String relName = buildRelationName(correlation);
		correlationCypher.append("UNWIND {props} as properties ");
		correlationCypher.append("MATCH (source:DATA:RAW:").append(source.getToolName()).append(" {uuid: properties.uuid}) ");
		correlationCypher.append("set source.correlationTime=").append(currentCorrelationTime).append(" WITH source, properties ");
		correlationCypher.append("MATCH (destination:DATA:").append(destination.getToolName()).append(") ");
		correlationCypher.append("WHERE destination.").append(destinationField).append(" IN properties.values ");
		correlationCypher.append("CREATE (source) -[r:").append(relName).append("]-> (destination) ");
		correlationCypher.append("set source.rels = coalesce(source.rels, []) + \"").append(relName).append("\" ");
		correlationCypher.append("WITH source where size(source.rels) >= ").append(allowedSourceRelationCount).append(" ");
		correlationCypher.append("REMOVE source:RAW RETURN count(distinct source) as count");
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
			log.debug("Processed "+processedRecords+" records in "+(System.currentTimeMillis() - st) + " ms");
			System.out.println("Processed "+processedRecords+" records in "+(System.currentTimeMillis() - st) + " ms");
		} catch (GraphDBException e) {
			log.error("Error occured while executing correlations for relation "+relName+".", e);
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
	 * Build the source correlation mapping i.e. all the possible outgoing relations from the source.
	 * @param correlations
	 * @return
	 */
	private Map<String, List<String>> buildSourceCorrelationMapping(List<Correlation> correlations){
		Map<String, List<String>> sourceCorrelationMap = new HashMap<String, List<String>>();
		for(Correlation correlation : correlations) {
			String sourceToolName = correlation.getSource().getToolName();
			List<String> relations = sourceCorrelationMap.get(sourceToolName);
			if(relations == null) {
				relations = new ArrayList<String>();
				sourceCorrelationMap.put(sourceToolName, relations);
			}
			String relationName = buildRelationName(correlation);
			if(!relations.contains(relationName)) {
				relations.add(relationName);
			}
		}
		return sourceCorrelationMap;
	}
	
	/**
	 * Build the name for the relation between source and destination.
	 * @param correlation
	 * @return
	 */
	private String buildRelationName(Correlation correlation) {
		return "FROM_"+correlation.getSource().getToolName()+"_TO_"+correlation.getDestination().getToolName();
	}
	
	/**
	 * Update the correlation time variables.
	 */
	private void updateCorrelationTimeVars() {
		currentCorrelationTime = System.currentTimeMillis()/1000;
		maxPreviousCorrelationTime = currentCorrelationTime - 1 * 60 * 60;
		minPreviousCorrelationTime = currentCorrelationTime - 1 * 24 * 60 * 60;
	}
}