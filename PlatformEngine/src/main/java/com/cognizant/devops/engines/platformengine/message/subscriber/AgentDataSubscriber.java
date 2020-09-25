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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.ws.rs.ProcessingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.AgentDataConstants;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.platformengine.modules.aggregator.BusinessMappingData;
import com.cognizant.devops.engines.util.DataEnrichUtils;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class AgentDataSubscriber extends EngineSubscriberResponseHandler {
	private static Logger log = LogManager.getLogger(AgentDataSubscriber.class.getName());
	GraphDBHandler dbHandler = new GraphDBHandler();

	public AgentDataSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	private boolean dataUpdateSupported;
	private String uniqueKey;
	private String category;
	private String toolName;
	private String labelName;
	private String targetProperty;
	private String keyPattern;
	private String sourceProperty;
	private Boolean isEnrichmentRequired;
	List<BusinessMappingData> businessMappingList = new ArrayList<BusinessMappingData>(0);

	public AgentDataSubscriber(String routingKey, boolean dataUpdateSupported, String uniqueKey, String category,
			String labelName, String toolName, List<BusinessMappingData> businessMappingList,
			boolean isEnrichmentRequired, String targetProperty, String keyPattern, String sourceProperty)
			throws Exception {
		super(routingKey);
		this.dataUpdateSupported = dataUpdateSupported;
		this.uniqueKey = uniqueKey;
		this.category = category;
		this.toolName = toolName;
		this.labelName = labelName;
		this.businessMappingList = businessMappingList;
		this.isEnrichmentRequired = isEnrichmentRequired;
		this.targetProperty = targetProperty;
		this.keyPattern = keyPattern;
		this.sourceProperty = sourceProperty;
	}

	public void setMappingData(List<BusinessMappingData> businessMappingList) {
		this.businessMappingList = businessMappingList;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		ApplicationConfigProvider.performSystemCheck();
		boolean enableOnlineDatatagging = ApplicationConfigProvider.getInstance().isEnableOnlineDatatagging();
		String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
		String routingKey = envelope.getRoutingKey();
		log.debug("Routing key in data " + routingKey);
		List<String> labels = new ArrayList<String>();
		labels.add("RAW");
		if (this.labelName == null) {
			labels.addAll(Arrays.asList(routingKey.split(MQMessageConstants.ROUTING_KEY_SEPERATOR)));
		} else {
			labels.add(this.category.toUpperCase());
			labels.add(this.toolName.toUpperCase());
			labels.add(this.labelName);
			labels.add("DATA");
		}

		List<JsonObject> dataList = new ArrayList<JsonObject>();
		JsonElement json = new JsonParser().parse(message);
		boolean dataUpdateSupported = this.dataUpdateSupported;
		String uniqueKey = this.uniqueKey;
		JsonObject relationMetadata = null;
		if (json.isJsonObject()) {
			JsonObject messageObject = json.getAsJsonObject();
			json = messageObject.get("data");
			if (messageObject.has("metadata")) {
				JsonObject metadata = messageObject.get("metadata").getAsJsonObject();
				if (metadata.has("labels")) {
					JsonArray additionalLabels = metadata.get("labels").getAsJsonArray();
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
					// Below Code has the ability to add derived properties as part of Nodes
					if (this.isEnrichmentRequired && e.getAsJsonObject().has(sourceProperty)) {
						JsonElement sourceElem = e.getAsJsonObject().get(sourceProperty);
						if (sourceElem.isJsonPrimitive()) {
							String enrichedData = DataEnrichUtils.dataExtractor(sourceElem.getAsString(), keyPattern);
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
			try {
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
					cypherQuery = "UNWIND {props} AS properties CREATE (n" + queryLabel
							+ ") set n=properties return count(n)";
				}

				List<List<JsonObject>> partitionList = partitionList(dataList, 1000);
				for (List<JsonObject> chunk : partitionList) {
					JsonObject graphResponse = dbHandler.bulkCreateNodes(chunk, labels, cypherQuery);
					if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
						log.error("Unable to insert nodes for routing key: " + routingKey + ", error occured: "
								+ graphResponse);
					}
				}
				getChannel().basicAck(envelope.getDeliveryTag(), false);
			} catch (ProcessingException e) {
				log.error("ProcessingException occured {} ", e);
				getChannel().basicNack(envelope.getDeliveryTag(), false, true);
			} catch (InsightsCustomException e) {
				log.error("InsightsCustomException occured {} ", e);
			} catch (Exception e) {
				log.error("Exception occured {} ", e);
			}
		}
	}

	private JsonObject mergeProperty(JsonElement e, String jsonStr) {
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObj = (JsonObject) jsonParser.parse(jsonStr);
		Map<String, String> metaDataJson = new Gson().fromJson(jsonObj, HashMap.class);
		Map<String, String> agentJson = new Gson().fromJson(e.getAsJsonObject(), HashMap.class);
		Map<String, String> finalobj = new HashMap<String, String>();
		finalobj.putAll(metaDataJson);
		finalobj.putAll(agentJson);
		String resultJson = new Gson().toJson(finalobj);
		JsonObject finalJson = (JsonObject) jsonParser.parse(resultJson);
		return finalJson;
	}

	private JsonObject applyDataTagging(JsonObject asJsonObject) {
		List<String> selectedBusinessMappingArray = new ArrayList<String>(0);
		Map<String, String> labelMappingMap = new TreeMap<String, String>();
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
			JsonParser jsonParser = new JsonParser();
			Gson gson = new Gson();
			for (Entry<String, String> entry : labelMappingMap.entrySet()) {
				List<String> items = Arrays.asList(entry.getValue().split("\\s*,\\s*"));
				JsonArray jsonArray = (JsonArray) jsonParser.parse(items.toString());
				asJsonObject.add(entry.getKey(), jsonArray);
			}

		}
		return asJsonObject;
	}

	private Map<String, Map<String, NodeData>> getMetaData(GraphDBHandler dbHandler) {
		List<NodeData> nodes = null;
		Map<String, NodeData> nodepropertyMap = null;
		Map<String, Map<String, NodeData>> metaDataMap = new HashMap<String, Map<String, NodeData>>();
		try {
			GraphResponse response = dbHandler.executeCypherQuery("MATCH (n:METADATA:DATATAGGING) return n");
			nodes = response.getNodes();
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		if (nodes.size() > 0) {
			for (NodeData node : nodes) {
				StringBuilder labelVal = new StringBuilder();
				StringBuilder key = new StringBuilder();
				nodepropertyMap = new HashMap<String, NodeData>();

				if (null != node.getProperty(AgentDataConstants.PROPERTY_1)
						&& !node.getProperty(AgentDataConstants.PROPERTY_1).isEmpty()) {
					key.append(node.getProperty(AgentDataConstants.PROPERTY_1));
					key.append(AgentDataConstants.COLON);
				}
				if (null != node.getProperty(AgentDataConstants.PROPERTY_2)
						&& !node.getProperty(AgentDataConstants.PROPERTY_2).isEmpty()) {
					key.append(node.getProperty(AgentDataConstants.PROPERTY_2));
					key.append(AgentDataConstants.COLON);
				}
				if (null != node.getProperty(AgentDataConstants.PROPERTY_3)
						&& !node.getProperty(AgentDataConstants.PROPERTY_3).isEmpty()) {
					key.append(node.getProperty(AgentDataConstants.PROPERTY_3));
					key.append(AgentDataConstants.COLON);
				}
				if (null != node.getProperty(AgentDataConstants.PROPERTY_4)
						&& !node.getProperty(AgentDataConstants.PROPERTY_4).isEmpty()) {
					key.append(node.getProperty(AgentDataConstants.PROPERTY_4));
					key.append(AgentDataConstants.COLON);
				}
				if (null != node.getProperty(AgentDataConstants.PROPERTYVALUE_1)
						&& !node.getProperty(AgentDataConstants.PROPERTYVALUE_1).isEmpty()) {
					labelVal.append(node.getProperty(AgentDataConstants.PROPERTYVALUE_1));
					labelVal.append(AgentDataConstants.COLON);
				}
				if (null != node.getProperty(AgentDataConstants.PROPERTYVALUE_2)
						&& !node.getProperty(AgentDataConstants.PROPERTYVALUE_2).isEmpty()) {
					labelVal.append(node.getProperty(AgentDataConstants.PROPERTYVALUE_2));
					labelVal.append(AgentDataConstants.COLON);
				}
				if (null != node.getProperty(AgentDataConstants.PROPERTYVALUE_3)
						&& !node.getProperty(AgentDataConstants.PROPERTYVALUE_3).isEmpty()) {
					labelVal.append(node.getProperty(AgentDataConstants.PROPERTYVALUE_3));
					labelVal.append(AgentDataConstants.COLON);
				}
				if (null != node.getProperty(AgentDataConstants.PROPERTYVALUE_4)
						&& !node.getProperty(AgentDataConstants.PROPERTYVALUE_4).isEmpty()) {
					labelVal.append(node.getProperty(AgentDataConstants.PROPERTYVALUE_4));
				}

				nodepropertyMap.put(StringUtils.stripEnd(labelVal.toString(), AgentDataConstants.COLON), node);
				metaDataMap.put(StringUtils.stripEnd(key.toString(), AgentDataConstants.COLON), nodepropertyMap);
			}
		}
		return metaDataMap;
	}

	private <T> List<List<T>> partitionList(List<T> list, final int size) {
		List<List<T>> parts = new ArrayList<List<T>>();
		final int N = list.size();
		for (int i = 0; i < N; i += size) {
			parts.add(getPartitionSubList(list, i, size, N));
		}
		return parts;
	}

	private <T> ArrayList<T> getPartitionSubList(List<T> list, int index, int size, final int N) {
		return new ArrayList<T>(list.subList(index, Math.min(N, index + size)));
	}

	private String buildCypherQuery(String labels, String fieldName) {
		StringBuffer query = new StringBuffer();
		query.append("UNWIND {props} AS properties MERGE (node:LATEST").append(labels).append(" { ");
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
		cypherQuery.append("UNWIND {props} AS properties MERGE (source").append(labels);
		if (source.has("labels")) {
			JsonArray sourceLabels = source.getAsJsonArray("labels");
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
		if (destination.has("labels")) {
			JsonArray destinationLabels = destination.getAsJsonArray("labels");
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
		if (relationMetadata.has("properties")) {
			cypherQuery.append(" set ");
			JsonArray properties = relationMetadata.getAsJsonArray("properties");
			for (JsonElement property : properties) {
				cypherQuery.append("r.").append(property.getAsString()).append(" = properties.")
						.append(property.getAsString()).append(",");
			}
			cypherQuery.delete(cypherQuery.length() - 1, cypherQuery.length());
		}

		return cypherQuery.toString();
	}

	private void buildNodePropertiesQueryPart(JsonObject node, String nodeName, StringBuffer cypherQuery) {
		if (node.has("properties")) {
			cypherQuery.append(" set ").append(nodeName).append("+=properties ");
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
}