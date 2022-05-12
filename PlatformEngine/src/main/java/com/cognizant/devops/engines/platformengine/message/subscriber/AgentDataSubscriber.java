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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

import jakarta.ws.rs.ProcessingException;

public class AgentDataSubscriber extends EngineSubscriberResponseHandler {

	private static Logger log = LogManager.getLogger(AgentDataSubscriber.class.getName());
	GraphDBHandler dbHandler = new GraphDBHandler();

	public AgentDataSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	private String category;
	private String toolName;
	private String labelName;
	private String targetProperty;
	private String keyPattern;
	private String sourceProperty;
	private Boolean isEnrichmentRequired;
	private String agentId;
	List<BusinessMappingData> businessMappingList = new ArrayList<>(0);
	private Map<String,String> loggingInfo = new ConcurrentHashMap<>();

	public AgentDataSubscriber(String routingKey, String category, String labelName, String toolName,
			List<BusinessMappingData> businessMappingList, boolean isEnrichmentRequired, String targetProperty,
			String keyPattern, String sourceProperty, String agentId) throws Exception {
		super(routingKey);
		this.category = category;
		this.toolName = toolName;
		this.labelName = labelName;
		this.businessMappingList = businessMappingList;
		this.isEnrichmentRequired = isEnrichmentRequired;
		this.targetProperty = targetProperty;
		this.keyPattern = keyPattern;
		this.sourceProperty = sourceProperty;
		this.agentId = agentId;
	}

	public void setMappingData(List<BusinessMappingData> businessMappingList) {
		this.businessMappingList = businessMappingList;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		String message = new String(body, StandardCharsets.UTF_8);
		try {
			ApplicationConfigProvider.performSystemCheck();
			long startTime = System.nanoTime();
			boolean enableOnlineDatatagging = ApplicationConfigProvider.getInstance().isEnableOnlineDatatagging();
			String routingKey = envelope.getRoutingKey();
			log.debug(
					" Type=AgentEngine toolName={} category={} agentId={} routingKey={} dataSize={} execId={} ProcessingTime={} Data ==== Routing key in data {} received data size {} ",
					toolName, category, agentId, routingKey, message.length(), "-", 0, routingKey, message.length());
			List<String> labels = new ArrayList<>();
			labels.add("RAW");
			if (this.labelName == null) {
				labels.addAll(Arrays.asList(routingKey.split(MQMessageConstants.ROUTING_KEY_SEPERATOR)));
			} else {
				labels.add(this.category.toUpperCase());
				labels.add(this.toolName.toUpperCase());
				labels.add(this.labelName);
				labels.add("DATA");
			}

			List<JsonObject> dataList = new ArrayList<>();
			JsonElement json = JsonUtils.parseString(message);
			boolean dataUpdateSupported = false;
			String uniqueKey = "";
			JsonObject relationMetadata = null;
			if (json.isJsonObject()) {
				JsonObject messageObject = json.getAsJsonObject();
				json = messageObject.get("data");
				if (messageObject.has("metadata")) {
					JsonObject metadata = messageObject.get("metadata").getAsJsonObject();
					if (metadata.has(AgentDataConstants.LABELS)) {
						JsonArray additionalLabels = metadata.get(AgentDataConstants.LABELS).getAsJsonArray();
						for (JsonElement additionalLabel : additionalLabels) {
							String label = additionalLabel.getAsString();
							if (!labels.contains(label)) {
								labels.add(label);
							}
						}
					}
					if (metadata.has("dataUpdateSupported")) {
						dataUpdateSupported = metadata.get("dataUpdateSupported").getAsBoolean();
					}
					if (metadata.has("uniqueKey")) {
						JsonArray uniqueKeyArray = metadata.getAsJsonArray("uniqueKey");
						StringBuffer keys = new StringBuffer();
						for (JsonElement key : uniqueKeyArray) {
							keys.append(key.getAsString()).append(",");
						}
						keys.delete(keys.length() - 1, keys.length());
						uniqueKey = keys.toString();
					}
					if (metadata.has("relation")) {
						relationMetadata = metadata.get("relation").getAsJsonObject();
					}
				}
			}

			if (json.isJsonArray()) {
				JsonArray asJsonArray = json.getAsJsonArray();
				JsonObject dataWithproperty;
				for (JsonElement e : asJsonArray) {
					if (e.isJsonObject()) {
						dataWithproperty = e.getAsJsonObject();						
						loggingInfo.put(EngineConstants.EXECID,String.valueOf(dataWithproperty.get(EngineConstants.EXECID)));
						// Below Code has the ability to add derived properties as part of Nodes
						if (Boolean.TRUE.equals(this.isEnrichmentRequired) && e.getAsJsonObject().has(sourceProperty)) {
							JsonElement sourceElem = e.getAsJsonObject().get(sourceProperty);
							if (sourceElem.isJsonPrimitive()) {
								String enrichedData = DataEnrichUtils.dataExtractor(sourceElem.getAsString(),
										keyPattern);
								if (enrichedData != null) {
									dataWithproperty.addProperty(targetProperty, enrichedData);
								}
							}
						}
						if (enableOnlineDatatagging) {
							JsonObject jsonWithLabel = applyDataTagging(dataWithproperty);
							if (jsonWithLabel != null) {
								dataList.add(jsonWithLabel);// finalJson
							} else {
								dataList.add(dataWithproperty);
							}
						} else {
							dataList.add(dataWithproperty);
						}
					}
				}

				String cypherQuery = "";
				String queryLabel = "";
				for (String label : labels) {
					if (label != null && label.trim().length() > 0) {
						queryLabel += ":" + label;
					}
				}
				if (relationMetadata != null) {
					cypherQuery = buildRelationCypherQuery(relationMetadata, queryLabel);
				} else if (dataUpdateSupported) {
					cypherQuery = buildCypherQuery(queryLabel, uniqueKey);
				} else {
					cypherQuery = "UNWIND $props AS properties CREATE (n" + queryLabel
							+ ") set n=properties return count(n)";
				}

				List<List<JsonObject>> partitionList = partitionList(dataList, 1000);
				for (List<JsonObject> chunk : partitionList) {
					JsonObject graphResponse = dbHandler.bulkCreateNodes(chunk, labels, cypherQuery);
					if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
						log.error("Unable to insert nodes for routing key: {}  error occured: {} ", routingKey,
								graphResponse);
					}
				}
				long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
				log.debug(
						" Type=AgentEngine toolName={} category={} agentId={} routingKey={} dataSize={} execId={} ProcessingTime={} Data ==== Processingtime={} ms",
						toolName, category, agentId, "-", 0, loggingInfo.get(EngineConstants.EXECID), processingTime, processingTime);
				getChannel().basicAck(envelope.getDeliveryTag(), false);
			}
		} catch (ProcessingException e) {
			log.error(" toolName={} category={} agentId={} execId={} ProcessingException occured ", toolName, category,
					agentId, loggingInfo.get(EngineConstants.EXECID), e);
			getChannel().basicNack(envelope.getDeliveryTag(), false, true);
		} catch (InsightsCustomException e) {
			log.error("Error in payload {} ",message);
			log.error(" toolName={} category={} agentId={} execId={} InsightsCustomException occured  ", toolName,
					category, agentId, loggingInfo.get(EngineConstants.EXECID), e);
			getChannel().basicReject(envelope.getDeliveryTag(), false);
		} catch (Exception e) {
			log.error("Error in payload {} ",message);
			log.error(" toolName={} category={} agentId={} execId={} Exception occured  ", toolName, category, agentId,
					loggingInfo.get(EngineConstants.EXECID), e);
			getChannel().basicReject(envelope.getDeliveryTag(), false);
		}
	}

	private JsonObject applyDataTagging(JsonObject asJsonObject) {
		List<String> selectedBusinessMappingArray = new ArrayList<>(0);
		Map<String, String> labelMappingMap = new TreeMap<>();
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
				selectedBusinessMappingArray.add(businessMappingData.getBusinessMappingLabel());
			}
		}

		if (!selectedBusinessMappingArray.isEmpty()) {
			for (int i = 0; i < selectedBusinessMappingArray.size(); i++) {
				StringTokenizer sk = new StringTokenizer(selectedBusinessMappingArray.get(i), ":");
				int level = 0;
				while (sk.hasMoreTokens()) {
					String token = sk.nextToken();
					level++;
					String key = "orgLevel_" + level;
					if (!labelMappingMap.containsKey(key)) {
						labelMappingMap.put(key, token);
					} else {
						if (!labelMappingMap.get(key).contains(token)) {
							labelMappingMap.put(key, labelMappingMap.get(key).concat("," + token));
						}
					}
				}
			}
			Gson gson = new Gson();
			for (Entry<String, String> entry : labelMappingMap.entrySet()) {
				List<String> items = Arrays.asList(entry.getValue().split("\\s*,\\s*"));
				JsonArray jsonArray = JsonUtils.parseStringAsJsonArray(items.toString());
				asJsonObject.add(entry.getKey(), jsonArray);
			}

		}
		return asJsonObject;
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
		StringBuffer query = new StringBuffer();
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
		StringBuffer cypherQuery = new StringBuffer();
		cypherQuery.append("UNWIND $props AS properties MERGE (source").append(labels);
		if (source.has(AgentDataConstants.LABELS)) {
			JsonArray sourceLabels = source.getAsJsonArray(AgentDataConstants.LABELS);
			for (JsonElement sourceLabel : sourceLabels) {
				String label = sourceLabel.getAsString();
				if (label != null && !labels.contains(label)) {
					cypherQuery.append(":").append(label);
				}
			}
		}
		cypherQuery.append(buildPropertyConstraintQueryPart(source, "constraints"));
		cypherQuery.append(") ");
		buildNodePropertiesQueryPart(source, "source", cypherQuery);
		cypherQuery.append(" WITH source, properties ");
		cypherQuery.append("MERGE (destination").append(labels);
		if (destination.has(AgentDataConstants.LABELS)) {
			JsonArray destinationLabels = destination.getAsJsonArray(AgentDataConstants.LABELS);
			for (JsonElement destinationLabel : destinationLabels) {
				String label = destinationLabel.getAsString();
				if (label != null && !labels.contains(label)) {
					cypherQuery.append(":").append(label);
				}
			}
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

	private void buildNodePropertiesQueryPart(JsonObject metaDataNode, String nodeName, StringBuffer cypherQuery) {
        if (metaDataNode.has(AgentDataConstants.PROPERTIES)) {
            cypherQuery.append(EngineConstants.SET).append(nodeName).append("+=properties ");
        } else if (metaDataNode.has(AgentDataConstants.SELECTED_PROPERTIES)) {
            cypherQuery.append(appendSelectedAdditionalPropertyQueryPart(metaDataNode, nodeName,
                    AgentDataConstants.SELECTED_PROPERTIES));
        }
    }

	private String buildPropertyConstraintQueryPart(JsonObject json, String memberName) {
		StringBuffer cypherQuery = new StringBuffer();
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