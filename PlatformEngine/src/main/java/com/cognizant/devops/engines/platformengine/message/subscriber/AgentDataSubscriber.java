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
package com.cognizant.devops.engines.platformengine.message.subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.AgentDataConstants;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.platformengine.modules.aggregator.BusinessMappingData;
import com.cognizant.devops.engines.util.DataEnrichUtils;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.EngineConstants;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.ws.rs.ProcessingException;

public class AgentDataSubscriber extends EngineSubscriberResponseHandler {

	private static Logger log = LogManager.getLogger(AgentDataSubscriber.class.getName());
	GraphDBHandler dbHandler = new GraphDBHandler();

	public AgentDataSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	private String category="";
	private String toolName="";
	private String labelName=null;
	private Boolean isEnrichmentRequired= false;
	private String targetProperty="";
	private String keyPattern="";
	private String sourceProperty ="";
	private String agentId="";
	List<BusinessMappingData> businessMappingList = new ArrayList<>(0);
	private Map<String,String> loggingInfo = new ConcurrentHashMap<>();

	public AgentDataSubscriber(String routingKey, String category, String labelName, String toolName,
			List<BusinessMappingData> businessMappingList, String agentId,JsonObject enrichTool) throws Exception {
		super(routingKey);
		this.category = category;
		this.toolName = toolName;
		this.labelName = labelName;
		this.businessMappingList = businessMappingList;
		if(enrichTool != null) {
			isEnrichmentRequired = enrichTool.get("isEnrichmentRequired").getAsBoolean();
			targetProperty = enrichTool.get("targetProperty").getAsString();
			keyPattern = enrichTool.get("keyPattern").getAsString();
			sourceProperty = enrichTool.get("sourceProperty").getAsString();
		}
		this.agentId = agentId;
	}
	
	

	public void setMappingData(List<BusinessMappingData> businessMappingList) {
		this.businessMappingList = businessMappingList;
	}

	@Override
	public void handleDelivery(String routingKey, String message ) throws Exception {
		try {
			ApplicationConfigProvider.performSystemCheck();
			long startTime = System.nanoTime();
			boolean enableOnlineDatatagging = ApplicationConfigProvider.getInstance().isEnableOnlineDatatagging();
			log.debug(
					" Type=AgentEngine toolName={} category={} agentId={} routingKey={} dataSize={} execId={} ProcessingTime={} Data ==== Routing key in data {} received data size {} ",
					toolName, category, agentId, routingKey, message.length(), "-", 0, routingKey, message.length());
			List<String> labels = new ArrayList<>();
			labels.add("RAW");
			
			prepareDataLabels(routingKey, labels);

			JsonElement json = JsonUtils.parseString(message);
			boolean dataUpdateSupported = false;
			String uniqueKey = "";
			JsonObject relationMetadata = null;
		
			if (json.isJsonObject()) {
				JsonObject messageObject = json.getAsJsonObject();
				json = messageObject.get("data");
				if (messageObject.has("metadata")) {			
					JsonObject metadata = messageObject.get("metadata").getAsJsonObject();
					labels = extractMetadataLabel(metadata,labels);	
					
					dataUpdateSupported = extractDataSupportedFlag(dataUpdateSupported, metadata);
					
					uniqueKey = extractUniqueKeyFromMetadata(uniqueKey, metadata);
					
					relationMetadata = extractRelationMetadata(relationMetadata, metadata);
				}
			} 

			if (json.isJsonArray()) {
				List<JsonObject> dataList = prepareDatalist(json,enableOnlineDatatagging);
				String cypherQuery = prepareCypherQuery(labels,relationMetadata,dataUpdateSupported,uniqueKey);			
				insertNodes(dataList,cypherQuery,routingKey);
				long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
				log.debug(
						" Type=AgentEngine toolName={} category={} agentId={} routingKey={} dataSize={} execId={} ProcessingTime={} Data ==== Processingtime={} ms",
						toolName, category, agentId, "-", 0, loggingInfo.get(EngineConstants.EXECID), processingTime, processingTime);
			}
			
		} catch (ProcessingException e) {
			log.error(" toolName={} category={} agentId={} execId={} ProcessingException occured ", toolName, category,
					agentId, loggingInfo.get(EngineConstants.EXECID), e);
			throw new ProcessingException(e.getMessage());
		} catch (InsightsCustomException e) {
			log.error("Error in payload {} ",message);
			log.error(" toolName={} category={} agentId={} execId={} InsightsCustomException occured  ", toolName,
					category, agentId, loggingInfo.get(EngineConstants.EXECID), e);
			throw new InsightsCustomException(e.getMessage());
		} catch (Exception e) {
			log.error("Error in payload {} ",message);
			log.error(" toolName={} category={} agentId={} execId={} Exception occured  ", toolName, category, agentId,
					loggingInfo.get(EngineConstants.EXECID), e);
			throw new Exception(e.getMessage());
		}
	}



	private boolean extractDataSupportedFlag(boolean dataUpdateSupported, JsonObject metadata) {
		if (metadata.has("dataUpdateSupported")) {
			dataUpdateSupported = metadata.get("dataUpdateSupported").getAsBoolean();
		}
		return dataUpdateSupported;
	}

	private String extractUniqueKeyFromMetadata(String uniqueKey, JsonObject metadata) {
		if (metadata.has("uniqueKey")) {					
			uniqueKey = getUniqueKey(metadata);
		}
		return uniqueKey;
	}

	private JsonObject extractRelationMetadata(JsonObject relationMetadata, JsonObject metadata) {
		if (metadata.has("relation")) {
			relationMetadata = metadata.get("relation").getAsJsonObject();
		}
		return relationMetadata;
	}

	private void prepareDataLabels(String routingKey, List<String> labels) {
		if (this.labelName == null) {
			labels.addAll(Arrays.asList(routingKey.split(MQMessageConstants.ROUTING_KEY_SEPERATOR)));
		} else {
			labels.add(this.category.toUpperCase());
			labels.add(this.toolName.toUpperCase());
			labels.add(this.labelName);
			labels.add("DATA");
		}
	}
	
	private String getUniqueKey(JsonObject metadata) {
    	String uniqueKey = "";
    	JsonArray uniqueKeyArray = metadata.getAsJsonArray("uniqueKey");
    	StringBuilder keys = new StringBuilder();
		for (JsonElement key : uniqueKeyArray) {
			keys.append(key.getAsString()).append(",");
		}
		keys.delete(keys.length() - 1, keys.length());
		uniqueKey = keys.toString();
		return uniqueKey;    	
    }
    
    private void insertNodes(List<JsonObject> dataList, String cypherQuery, String routingKey) throws InsightsCustomException {
		List<List<JsonObject>> partitionList = partitionList(dataList, 1000);
		for (List<JsonObject> chunk : partitionList) {
			JsonObject graphResponse = dbHandler.bulkCreateNodes(chunk,cypherQuery);
			if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
				log.error("Unable to insert nodes for routing key: {}  error occured: {} ", routingKey,
						graphResponse);
			}
		}
	}
	
	
	private String prepareCypherQuery(List<String> labels, JsonObject relationMetadata, boolean dataUpdateSupported, String uniqueKey) {
		
		String preparedCypherQuery = "";
		StringBuilder queryLabel = new StringBuilder();
		for (String label : labels) {
			if (label != null && label.trim().length() > 0) {
				queryLabel.append(":").append(label);
			}
		}
		if (relationMetadata != null) {
			preparedCypherQuery = buildRelationCypherQuery(relationMetadata, queryLabel.toString());
		} else if (dataUpdateSupported) {
			preparedCypherQuery = buildCypherQuery(queryLabel.toString(), uniqueKey);
		} else {
			preparedCypherQuery = "UNWIND $props AS properties CREATE (n" + queryLabel
					+ ") set n=properties return count(n)";
		}
		
		return preparedCypherQuery;
	}
		
	private List<String> extractMetadataLabel(JsonObject metadata,List<String> labels){
		
		if (metadata.has(AgentDataConstants.LABELS)) {
			JsonArray additionalLabels = metadata.get(AgentDataConstants.LABELS).getAsJsonArray();
			for (JsonElement additionalLabel : additionalLabels) {
				String label = additionalLabel.getAsString();
				if (!labels.contains(label)) {
					labels.add(label);
				}
			}
		}
		return labels;
	}
	
	private List<JsonObject> prepareDatalist(JsonElement json, boolean enableOnlineDatatagging) {
		
		JsonArray asJsonArray = json.getAsJsonArray();
		JsonObject dataWithproperty;
		List<JsonObject> prepareDatalist = new ArrayList<>();
		
		for (JsonElement e : asJsonArray) {
			if (e.isJsonObject()) {
				dataWithproperty = e.getAsJsonObject();						
				loggingInfo.put(EngineConstants.EXECID,String.valueOf(dataWithproperty.get(EngineConstants.EXECID)));
				
				// Below Code has the ability to add derived properties as part of Nodes
				if (Boolean.TRUE.equals(this.isEnrichmentRequired) && e.getAsJsonObject().has(sourceProperty)) {
					addEnrichmentProperties(e,dataWithproperty);			
				}
				if (enableOnlineDatatagging) {
					applyDataTagging(dataWithproperty);
				}
				prepareDatalist.add(dataWithproperty);
			}
		}
		return prepareDatalist;
	}


	private void addEnrichmentProperties(JsonElement e, JsonObject dataWithproperty) {

		JsonElement sourceElem = e.getAsJsonObject().get(sourceProperty);
		if (sourceElem.isJsonPrimitive()) {
			String enrichedData = DataEnrichUtils.dataExtractor(sourceElem.getAsString(), keyPattern);
			if (enrichedData != null) {
				dataWithproperty.addProperty(targetProperty, enrichedData);
			}
		}
	}

	
	private void applyDataTagging(JsonObject asJsonObject) {
		
		List<String> selectedBusinessMappingArray = prepareBusinessMappingArray(asJsonObject);
		if (!selectedBusinessMappingArray.isEmpty()) {			
			Map<String, String>  labelMappingMap = prepareLabelMapping(selectedBusinessMappingArray);
			for (Entry<String, String> entry : labelMappingMap.entrySet()) {
				List<String> items = Arrays.asList(entry.getValue().split("\\s*,\\s*"));
				JsonArray jsonArray = JsonUtils.parseStringAsJsonArray(items.toString());
				asJsonObject.add(entry.getKey(), jsonArray);
			}
		}
	}
	
	private List<String> prepareBusinessMappingArray(JsonObject asJsonObject){
    	List<String> businessMappingArray = new ArrayList<>(0);
    	
    	for (BusinessMappingData businessMappingData : businessMappingList) {
			Map<String, String> map = businessMappingData.getPropertyMap();
			int totalCount = businessMappingData.getPropertyMap().size();
			int matchLabelcount = 0;
			for (Entry<String, String> mapValue : map.entrySet()) {
				if (asJsonObject.has(mapValue.getKey())) {
					String jsonValue = asJsonObject.get(mapValue.getKey()).getAsString();
					if (jsonValue.equalsIgnoreCase(mapValue.getValue())) {
						matchLabelcount++;
					}
				}
			}
			if (totalCount == matchLabelcount) {
				businessMappingArray.add(businessMappingData.getBusinessMappingLabel());
			}
		}
    	return businessMappingArray;
    }
    
    private Map<String, String> prepareLabelMapping(List<String> selectedBusinessMappingArray) {
    	
    	Map<String, String> labelMap = new TreeMap<>();
    	
    	for (int i = 0; i < selectedBusinessMappingArray.size(); i++) {
			StringTokenizer sk = new StringTokenizer(selectedBusinessMappingArray.get(i), ":");
			int level = 0;
			while (sk.hasMoreTokens()) {
				String token = sk.nextToken();
				level++;
				String key = "orgLevel_" + level;
				if (!labelMap.containsKey(key)) {
					labelMap.put(key, token);
				} else {
					if (!labelMap.get(key).contains(token)) {
						labelMap.put(key, labelMap.get(key).concat("," + token));
					}
				}
			}
		}
    	return labelMap;
    }

    private <T> List<List<T>> partitionList(List<T> list, final int size) {
		List<List<T>> parts = new ArrayList<>();
		final int N = list.size();
		for (int i = 0; i < N; i += size) {
			parts.add(getPartitionSubList(list, i, size, N));
		}
		return parts;
	}

	private <T> ArrayList<T> getPartitionSubList(List<T> list, int index, int size, final int N) {
		return new ArrayList<>(list.subList(index, Math.min(N, index + size)));
	}

	private String buildCypherQuery(String labels, String fieldName) {
		StringBuilder query = new StringBuilder();
		query.append("UNWIND $props AS properties MERGE (node:LATEST").append(labels).append(" { ");
		if (fieldName.contains(",")) {
			String[] fields = fieldName.split(",");
			JsonObject searchCriteria = new JsonObject();
			for (String field : fields) {
				searchCriteria.addProperty(field, field);
				query.append(field).append(" : properties.").append(field).append(",");
			}
			query.delete(query.length() - 1, query.length());
			query.append(" ");
		} else {
			query.append(fieldName).append(" : ").append("properties.").append(fieldName);
		}
		query.append(" }) set node+=properties ").append(" ");
		query.append("return count(node)").append(" ");
		return query.toString();
	}

	private String buildRelationCypherQuery(JsonObject relationMetadata, String labels) {
		JsonObject source = relationMetadata.getAsJsonObject("source");
		JsonObject destination = relationMetadata.getAsJsonObject("destination");
		String relationName = relationMetadata.get("name").getAsString();
		StringBuilder cypherQuery = new StringBuilder();
		cypherQuery.append("UNWIND $props AS properties MERGE (source").append(labels);
		if (source.has(AgentDataConstants.LABELS)) {
			setLabelToCypherQuery(labels, source, cypherQuery);
		}
		cypherQuery.append(buildPropertyConstraintQueryPart(source, "constraints"));
		cypherQuery.append(") ");
		buildNodePropertiesQueryPart(source, "source", cypherQuery);
		cypherQuery.append(" WITH source, properties ");
		cypherQuery.append("MERGE (destination").append(labels);
		if (destination.has(AgentDataConstants.LABELS)) {
			setLabelToCypherQuery(labels, destination, cypherQuery);
		}
		cypherQuery.append(buildPropertyConstraintQueryPart(destination, "constraints"));
		cypherQuery.append(") ");
		buildNodePropertiesQueryPart(destination, "destination", cypherQuery);
		cypherQuery.append(" MERGE (source)-[r:").append(relationName).append("]->(destination) ");
		if (relationMetadata.has(AgentDataConstants.PROPERTIES)) {
			cypherQuery.append(EngineConstants.SET);
			JsonArray properties = relationMetadata.getAsJsonArray(AgentDataConstants.PROPERTIES);
			for (JsonElement property : properties) {
				cypherQuery.append("r.").append(property.getAsString()).append(" = properties.")
						.append(property.getAsString()).append(",");
			}
			cypherQuery.delete(cypherQuery.length() - 1, cypherQuery.length());
		}

		return cypherQuery.toString();
	}
	//to reduce the cognitive complexity
	private void setLabelToCypherQuery(String labels, JsonObject relationMetadata, StringBuilder cypherQuery) {
		JsonArray relationMetadataLabels = relationMetadata.getAsJsonArray(AgentDataConstants.LABELS);
		for (JsonElement relationMetadataLabelsLabel : relationMetadataLabels) {
			String label = relationMetadataLabelsLabel.getAsString();
			if (label != null && !labels.contains(label)) {
				cypherQuery.append(":").append(label);
			}
		}
	}

	private void buildNodePropertiesQueryPart(JsonObject metaDataNode, String nodeName, StringBuilder cypherQuery) {
        if (metaDataNode.has(AgentDataConstants.PROPERTIES)) {
            cypherQuery.append(EngineConstants.SET).append(nodeName).append("+=properties ");
        } else if (metaDataNode.has(AgentDataConstants.SELECTED_PROPERTIES)) {
            cypherQuery.append(appendSelectedAdditionalPropertyQueryPart(metaDataNode, nodeName,
                    AgentDataConstants.SELECTED_PROPERTIES));
        }
    }

	private String buildPropertyConstraintQueryPart(JsonObject json, String memberName) {
		StringBuilder cypherQuery = new StringBuilder();
		if (json.has(memberName)) {
			JsonArray properties = json.getAsJsonArray(memberName);
			cypherQuery.append("{");
			for (JsonElement constraint : properties) {
				String fieldName = constraint.getAsString();
				cypherQuery.append(fieldName).append(" : properties.").append(fieldName).append(",");
			}
			cypherQuery.delete(cypherQuery.length() - 1, cypherQuery.length());
			cypherQuery.append(" }");
		}
		return cypherQuery.toString();
	}
	
	private String appendSelectedAdditionalPropertyQueryPart(JsonObject metaDataNodejson, String nodeName, String memberName) {
        StringBuilder cypherQuery = new StringBuilder();
        if (metaDataNodejson.has(memberName)) {
            JsonArray properties = metaDataNodejson.getAsJsonArray(memberName);
            if (properties.size() > 0) {
                cypherQuery.append(EngineConstants.SET);
                for (JsonElement additionalProperty : properties) {
                    String fieldName = additionalProperty.getAsString();
                    cypherQuery.append(nodeName).append(".").append(fieldName).append(" = properties.")
                            .append(fieldName).append(",");
                }
                cypherQuery.delete(cypherQuery.length() - 1, cypherQuery.length());
            }
        }
        return cypherQuery.toString();
    }

}