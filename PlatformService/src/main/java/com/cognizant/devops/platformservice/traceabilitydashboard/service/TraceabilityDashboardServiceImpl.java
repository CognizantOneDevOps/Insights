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
package com.cognizant.devops.platformservice.traceabilitydashboard.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.stereotype.Service;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.traceabilitydashboard.constants.TraceabilityConstants;
import com.cognizant.devops.platformservice.traceabilitydashboard.util.TraceabilitySummaryUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.client.ClientHandlerException;

@Service("TreceabilityDashboardService")
public class TraceabilityDashboardServiceImpl implements TraceabilityDashboardService {

	Cache<String, String> pipelineCache;
	Cache<String, String> masterdataCache;

	String toolName, fieldName, fieldValue;
	String cacheKey;

	private static final Logger LOG = LogManager.getLogger(TraceabilityDashboardServiceImpl.class.getName());

	{
		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("traceability",
						CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
								ResourcePoolsBuilder.newResourcePoolsBuilder().heap(30, EntryUnit.ENTRIES).offheap(10,
										MemoryUnit.MB)))
				.build();

		cacheManager.init();

		pipelineCache = cacheManager.createCache("pipeline",
				CacheConfigurationBuilder
						.newCacheConfigurationBuilder(String.class, String.class,
								ResourcePoolsBuilder.heap(TraceabilityConstants.PIPELINE_CACHE_HEAP_SIZE_BYTES))
						.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(
								Duration.ofSeconds(TraceabilityConstants.PIPELINE_CACHE_EXPIRY_IN_SEC))));

		masterdataCache = cacheManager.createCache("masterdata",
				CacheConfigurationBuilder
						.newCacheConfigurationBuilder(String.class, String.class,
								ResourcePoolsBuilder.heap(TraceabilityConstants.MASTER_CACHE_HEAP_SIZE_BYTES))
						.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(
								Duration.ofSeconds(TraceabilityConstants.MASTER_CACHE_EXPIRY_IN_SEC))));

	}

	private JsonObject getPipeLineResponse(HashMap<String, List<JsonObject>> map, JsonObject dataModel) {
		JsonArray pipeLineArray = new JsonArray();
		JsonObject pipeLineObject = new JsonObject();
		Set<Entry<String, List<JsonObject>>> keyset = map.entrySet();
		for (Map.Entry<String, List<JsonObject>> keyvaluePair : keyset) {
			List<JsonObject> limitedList = keyvaluePair.getValue().stream().limit(4).collect(Collectors.toList());
			limitedList.forEach(obj -> pipeLineArray.add(obj));
		}
		/* Prepare Summary */
		JsonObject summaryObj = prepareSummary(map, dataModel);
		JsonArray summaryArray = new JsonArray();
		summaryArray.add(summaryObj);

		/* Pipeline Response */

		pipeLineObject.add("pipeline", pipeLineArray);
		pipeLineObject.add("summary", summaryArray);

		return pipeLineObject;

	}

	public static JsonArray stringify(List<String> val) {
		JsonArray jsArray = new JsonArray();
		for (String value : val) {
			jsArray.add(value);
		}
		return jsArray;
	}

	public String buildCypherQuery(String toolname, String toolField, List<String> toolVal, List<String> excludeLabels,
			int hopCount) {

		if (hopCount == 1) {
			String query = "match(n:" + toolname + ":DATA{" + toolField + ":" + "'" + toolVal.get(0).toString() + "'"
					+ "}) return n.toolName as toolname ,n.uuid as uuid";
			return query;
		} else if (hopCount == 2) {

			String query = "MATCH (a:DATA:" + toolname + ") -[*0..1]- (b:DATA) WHERE ";
			if (toolField != "") {
				query += "a." + toolField + " IN " + stringify(toolVal) + " ";
			}
			if (excludeLabels.size() > 0) {
				query += "AND NOT (";
				for (String excludeLabel : excludeLabels) {
					query += "b:" + excludeLabel + " OR ";
				}
				query = query.substring(0, query.lastIndexOf("OR"));
				query += ")";
			}
			query += "with a, b, collect(distinct a.uuid) as uuids ";
			query += "WHERE NOT (b.uuid IN uuids) ";
			query += "return  b.toolName as toolName, collect(distinct b.uuid) as uuids";
			return query;
		} else if (hopCount == 3) {
			String query = "MATCH (a:DATA) WHERE a.uuid IN " + stringify(toolVal)
					+ " WITH distinct a.toolName as toolName, collect(distinct a) as nodes Return toolName, nodes";

			return query;
		} else {
			String query = "MATCH (a:" + toolname + ":DATA) WHERE a.uuid IN " + stringify(toolVal)
					+ " WITH distinct a.toolName as toolName, collect(distinct a) as nodes Return toolName, nodes";
			return query;

		}

	}

	public HashMap<String, String> formatNeo4jResponse(JsonObject resp, int hopCount)
			throws JsonIOException, JsonSyntaxException {

		HashMap<String, String> map = new HashMap<String, String>();
		JsonArray jsonDataRespArray = resp.getAsJsonArray("results").getAsJsonArray().get(0).getAsJsonObject()
				.get("data").getAsJsonArray();
		if (jsonDataRespArray.size() > 0) {
			if (hopCount == 1) {
				// To Extract the UUID of the selected tool in UI for the first time
				String jsonResp = jsonDataRespArray.getAsJsonArray().get(0).getAsJsonObject().get("row").toString();
				String extractedToolsWithUUID[] = jsonResp.replaceAll("[\\[\\](){}\"\\\"\"]", "").split(",");
				for (int j = 1; j < extractedToolsWithUUID.length; j++)
					map.put(extractedToolsWithUUID[j], extractedToolsWithUUID[0]);
			} else {
				for (int i = 0; i < jsonDataRespArray.size(); i++) {
					String rawOutput = jsonDataRespArray.get(i).getAsJsonObject().get("row").toString();
					String extractedToolsWithUUID[] = rawOutput.replaceAll("[\\[\\](){}\"\\\"\"]", "").split(",");
					for (int j = 1; j < extractedToolsWithUUID.length; j++)
						map.put(extractedToolsWithUUID[j], extractedToolsWithUUID[0]);
				}
			}
		}

		return map;
	}

	public JsonObject executeCypherQuery(String query) throws GraphDBException, ClientHandlerException {

		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		GraphResponse neo4jResponse = new GraphResponse();
		neo4jResponse = dbHandler.executeCypherQuery(query);
		LOG.debug("Response received from neo4j");
		return neo4jResponse.getJson();

	}

	private String[] getUpTool(String toolName, JsonObject dataModel)
			throws JsonIOException, JsonSyntaxException, InsightsCustomException {
		try {
			return dataModel.getAsJsonObject(toolName).get("uptool").toString().replaceAll("[\\[\\](){}\"\\\"\"]", "")
					.split(",");
		} catch (Exception e) {
			throw new InsightsCustomException("toolname not found in datamodel");
		}
	}

	private List<JsonObject> sortToolsPayload(List<JsonObject> payload) {
		payload.sort(new Comparator<JsonObject>() {
			@Override
			public int compare(JsonObject o1, JsonObject o2) {
				final String KEY_NAME = "timestamp";
				SimpleDateFormat sdfo = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				String valA = new String();
				String valB = new String();
				Date d1 = new Date();
				Date d2 = new Date();
				try {
					valA = (String) o1.getAsJsonPrimitive(KEY_NAME).getAsString();
					valB = (String) o2.getAsJsonPrimitive(KEY_NAME).getAsString();
					d1 = sdfo.parse(epochToHumanDate(valA));
					d2 = sdfo.parse(epochToHumanDate(valB));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return (d1.getTime() > d2.getTime() ? -1 : 1);
			}
		});
		return payload;
	}

	private static String epochToHumanDate(String epochtime) {
		Long epoch = Long.valueOf(epochtime.split("\\.", 2)[0]);
		Date date = new Date(epoch * 1000L);
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		return format.format(date);
	}

	private HashMap<String, List<JsonObject>> getMasterResponse(JsonObject response, JsonObject dataModel)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {

		JsonArray finaltoolsArray = new JsonArray();
		// Master Map contains toolname as string and list of toolpayload .
		HashMap<String, List<JsonObject>> masterMap = new HashMap<String, List<JsonObject>>();
		Set<Entry<String, JsonElement>> keyset = dataModel.entrySet();
		JsonArray responseArray = response.getAsJsonArray("results").getAsJsonArray().get(0).getAsJsonObject()
				.get("data").getAsJsonArray();
		int count = responseArray.size();
		for (int j = 0; j < count; j++) {
			List<JsonObject> toolsPayload = new ArrayList<JsonObject>();
			// find the toolname in neo4j response
			LOG.debug("Data From Neo4J has been loaded properly");
			JsonArray toolsArray = responseArray.get(j).getAsJsonObject().get("row").getAsJsonArray();
			String toolNameFromNeo4j = toolsArray.get(0).toString().replaceAll("\"", "");
			// Get the response template of the specific tool from data model
			JsonObject toolPayloadFromDatamodel = dataModel.get(toolNameFromNeo4j).getAsJsonObject();
			if (toolPayloadFromDatamodel != null) {
				keyset = toolPayloadFromDatamodel.entrySet();
				int numOfObjectsPerTool = toolsArray.get(1).getAsJsonArray().size();
				for (int i = 0; i < numOfObjectsPerTool; i++) {
					JsonObject formattedJsonObject = new JsonObject();
					for (Map.Entry<String, JsonElement> toolKeyValueSetFromDataModel : keyset) {
						// Every tool is JsonArray so loop it if it has more than one element get
						// extract the value of the each key
						//
						if (toolKeyValueSetFromDataModel.getKey().equals("order"))
							formattedJsonObject.addProperty(toolKeyValueSetFromDataModel.getKey(),
									toolKeyValueSetFromDataModel.getValue().toString());
						JsonElement propertyValFromNeo4j = toolsArray.get(1).getAsJsonArray().get(i).getAsJsonObject()
								.get(toolKeyValueSetFromDataModel.getValue().toString().replaceAll("\"", ""));
						if (propertyValFromNeo4j != null) {
							String neo4jValue = propertyValFromNeo4j.toString().replaceAll("\"", "");
							if (toolKeyValueSetFromDataModel.getKey().equals("timestamp")) {
								int iEnd = propertyValFromNeo4j.toString().replaceAll("\"", "").indexOf(".");
								if (iEnd != -1)
									neo4jValue = propertyValFromNeo4j.toString().replaceAll("\"", "").substring(0,
											iEnd);
								else
									neo4jValue = propertyValFromNeo4j.toString().replaceAll("\"", "");
							}
							formattedJsonObject.addProperty(toolKeyValueSetFromDataModel.getKey(), neo4jValue);
						}
					}
					/* Add toolStatus property explicitly if the object does not have it already */
					if (formattedJsonObject.get("toolstatus") == null)
						formattedJsonObject.addProperty("toolstatus", "Success");
					formattedJsonObject.addProperty("count", Integer.toString(numOfObjectsPerTool));
					toolsPayload.add(formattedJsonObject);
				}
				/* sort the tools payload with timestamp before adding into the mastermap */
				List<JsonObject> sortedPayload = sortToolsPayload(toolsPayload);
				/* prepare the summary */

				/* Add each tool list payload to mastermap with toolname as key */
				masterMap.put(toolNameFromNeo4j, sortedPayload);
			}
		}
		/* prepare the summary */

		JsonObject finalObj = new JsonObject();
		finalObj.add("data", finaltoolsArray);
		return masterMap;

	}

	private JsonObject prepareSummary(HashMap<String, List<JsonObject>> masterMap, JsonObject dataModel) {

		HashMap<String, List<JsonObject>> map = masterMap;
		JsonObject summaryObject = new JsonObject();
		map.forEach(new BiConsumer<String, List<JsonObject>>() {
			public void accept(String toolName, List<JsonObject> payload) {
				JsonObject summary = new JsonObject();
				JsonObject toolObjectFromDataModel = dataModel.get(toolName).getAsJsonObject();
				if (toolObjectFromDataModel.has("messages")) // check the toolname has message
				{
					int messageSize = toolObjectFromDataModel.get("messages").getAsJsonArray().size();
					for (int i = 0; i < messageSize; i++) {
						JsonObject messageClause = toolObjectFromDataModel.get("messages").getAsJsonArray().get(i)
								.getAsJsonObject();
						String operationName = messageClause.get("Operation").toString().replace("\"", "");
						if (operationName.equals("SUM")) {
							String operandName = messageClause.get("OperandName").toString().replace("\"", "");
							String operandValue = messageClause.get("OperandValue").toString().replace("\"", "");
							String message = messageClause.get("Message").toString();
							String resp = TraceabilitySummaryUtil.SUM(operandName, operandValue, payload, message);
							if (resp != "") {
								summary.addProperty(String.valueOf(i), resp);
								summaryObject.add(toolName, summary);
							}
						}
						if (operationName.equals("PERCENTAGE")) {
							String operandName = messageClause.get("OperandName").toString().replace("\"", "");
							String operandValue = messageClause.get("OperandValue").toString().replace("\"", "");
							String message = messageClause.get("Message").toString();
							String resp = TraceabilitySummaryUtil.Percentage(operandName, operandValue, payload,
									message);
							if (resp != "") {
								summary.addProperty(String.valueOf(i), resp);
								summaryObject.add(toolName, summary);
							}
						}
						if (operationName.equals("TIMEDIFF")) {
							String operandName = messageClause.get("OperandName").toString().replace("\"", "");
							String operandValue = messageClause.get("OperandValue").toString().replace("\"", "");
							String message = messageClause.get("Message").toString();
							String resp;
							try {
								resp = TraceabilitySummaryUtil.TimeDiffrence(operandName, operandValue, payload,
										message);
								if (resp != "") {
									summary.addProperty(String.valueOf(i), resp);
									summaryObject.add(toolName, summary);
								}
							} catch (ParseException e) {

								e.printStackTrace();
							}

						}

					}
				}

			}

		});

		return summaryObject;

	}

	/*
	 * public long humanToepochTime(String timestamp) throws ParseException {
	 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	 * Date dt = sdf.parse(timestamp); long epoch = dt.getTime(); return
	 * (int)(epoch/1000); }
	 */

	private String[] getDownTool(String toolName, JsonObject dataModel)
			throws JsonIOException, JsonSyntaxException, InsightsCustomException {
		try {
			return dataModel.getAsJsonObject(toolName).get("downtool").toString().replaceAll("[\\[\\](){}\"\\\"\"]", "")
					.split(",");
		} catch (Exception e) {
			throw new InsightsCustomException("toolname not found in datamodel");
		}

	}

	private HashMap<String, String> resolveUpAndDownLinks(HashMap<String, String> drilldownListMap,
			List<String> baseTool) throws GraphDBException, ClientHandlerException {
		HashMap<String, String> mainToolList = new HashMap<String, String>();
		List<String> excludeLabels = new ArrayList<String>();
		excludeLabels.addAll(baseTool);
		// Distinct List
		while (drilldownListMap.size() > 0) {
			List<String> labelsTemp = new ArrayList<String>();
			HashMap<String, String> tempMap = new HashMap<String, String>();
			List<String> distictToolList = drilldownListMap.values().stream().distinct().collect(Collectors.toList());
			for (String tool : distictToolList) {
				drilldownListMap.entrySet().stream().filter((v) -> v.getValue().equals(tool)).map(Map.Entry::getKey)
						.collect(Collectors.toList()).forEach(uuid -> mainToolList.put(uuid, tool));
				/* Query Output */
				String cypher = buildCypherQuery(tool, "uuid", drilldownListMap.entrySet().stream()
						.filter((v) -> v.getValue().equals(tool)).map(Map.Entry::getKey).collect(Collectors.toList()),
						excludeLabels, 2);
				/* collect all parent or child uuids for current basenode. */
				tempMap.putAll(formatNeo4jResponse(executeCypherQuery(cypher), 2));
				/* collect the tools as basetool for the next hop in drilldown */
				labelsTemp.addAll(Arrays.asList(tool));
			}
			excludeLabels = (labelsTemp.stream().distinct().collect(Collectors.toList()));
			drilldownListMap.clear();
			drilldownListMap.putAll(tempMap);
		}
		return mainToolList;
	}

	@Override
	public JsonObject getPipeline(String toolName, String fieldName, String fieldValue) throws InsightsCustomException {

		this.toolName = toolName;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		cacheKey = toolName + "." + fieldName + "." + fieldValue;
		if (pipelineCache.get(cacheKey) != null) {
			LOG.debug("Pipeline response loaded from cache");
			return new JsonParser().parse(pipelineCache.get(cacheKey)).getAsJsonObject();
		} else {
			/* Load JsonObject from DataModel.json for processing */
			String DATA_MODEL_FILE_RESOLVED_PATH = System.getenv().get(TraceabilityConstants.ENV_VAR_NAME)
					+ File.separator + TraceabilityConstants.DATAMODEL_FOLDER_NAME + File.separator
					+ TraceabilityConstants.DATAMODEL_FILE_NAME;
			try {
				JsonObject dataModel = (JsonObject) new JsonParser()
						.parse(new FileReader(DATA_MODEL_FILE_RESOLVED_PATH));
				HashMap<String, String> uplinkMap = new HashMap<String, String>();
				HashMap<String, String> downlinkMap = new HashMap<String, String>();
				HashMap<String, String> toolsListMap = new HashMap<String, String>();
				/* Get the uuid of the tool selected in UI and store it in Main Map */
				String cypher = buildCypherQuery(toolName, fieldName, new ArrayList<String>(Arrays.asList(fieldValue)),
						Collections.emptyList(), 1);
				toolsListMap.putAll(formatNeo4jResponse(executeCypherQuery(cypher), 1));
				/* Get the upTool and DownTool of the selected tool in UI */
				String upTools[] = getUpTool(toolName, dataModel);
				String downTool[] = getDownTool(toolName, dataModel);
				/*
				 * Execute the first query to get the linked tools for the tool selected in UI
				 */
				HashMap<String, String> temp = formatNeo4jResponse(executeCypherQuery(buildCypherQuery(toolName,
						fieldName, new ArrayList<String>(Arrays.asList(fieldValue)), Collections.emptyList(), 2)), 2);
				if (temp.size() > 0) {
					uplinkMap = (HashMap<String, String>) temp.entrySet().stream()
							.filter(e -> Arrays.stream(upTools).anyMatch(e.getValue()::equals))
							.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
					downlinkMap = (HashMap<String, String>) temp.entrySet().stream()
							.filter(e -> Arrays.stream(downTool).anyMatch(e.getValue()::equals))
							.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				}
				if (uplinkMap.size() > 0) {
					toolsListMap.putAll(resolveUpAndDownLinks(uplinkMap, Arrays.asList(toolName)));
					LOG.debug("drilldown completed for uplink");
				}
				if (downlinkMap.size() > 0) {
					toolsListMap.putAll(resolveUpAndDownLinks(downlinkMap, Arrays.asList(toolName)));
					LOG.debug("drilldown completed for downlink");
				}
				List<String> uuid = new ArrayList<String>();
				toolsListMap.keySet().forEach(k -> uuid.add(k));

				String pipelineCypher = buildCypherQuery(null, null, uuid, null, 3);
				JsonObject neo4jResponse = executeCypherQuery(pipelineCypher);
				LOG.debug("pipeline response received from neo4j");
				/* Get the MasterResponse Map which will used for caching */
				HashMap<String, List<JsonObject>> masterMap = getMasterResponse(neo4jResponse, dataModel);

				masterdataCache.put(cacheKey, masterMap.toString());
				LOG.debug("Traceability data has been loaded successfully");
				/* Filter MaterMap and send only first 4 JsonObjects for the tool */
				JsonObject response = getPipeLineResponse(masterMap, dataModel);
				LOG.debug("Pipeline response prepared successfully");
				/* Get the MasterResponse Map which will used for caching */
				pipelineCache.put(cacheKey, response.toString());
				return response;

			} catch (ClientHandlerException | FileNotFoundException | JsonSyntaxException | JsonIOException
					| GraphDBException | InsightsCustomException ex1) {
				LOG.debug(ex1.getMessage());
				throw new InsightsCustomException(ex1.getMessage());
			}
		}

	}

	@Override
	public List<JsonObject> getToolSummary(String toolName, String cacheKey) throws InsightsCustomException {
		String toolSummary = masterdataCache.get(cacheKey);
		LOG.error(cacheKey);
		if (toolSummary != null) {
			Gson gson = new Gson();
			Map<String, List<JsonObject>> attributes = gson.fromJson(toolSummary, Map.class);
			List<JsonObject> toolPayload = attributes.get(toolName);
			LOG.debug("Summary data loaded from cache");
			return toolPayload;
		} else
			return Collections.emptyList();

	}

	@Override
	public List<String> getAvailableTools() throws InsightsCustomException {
		GraphResponse neo4jResponse = new GraphResponse();
		try {
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			neo4jResponse = dbHandler.executeCypherQuery("match(n:DATA) return collect(distinct n.toolName)");
		} catch (GraphDBException e) {

			LOG.debug("Exception in neo4j");
			throw new InsightsCustomException(e.getMessage());
		}
		return Arrays.asList(neo4jResponse.getJson().getAsJsonArray("results").getAsJsonArray().get(0).getAsJsonObject()
				.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString()
				.replaceAll("[\\[\\](){}\"\\\"\"]", "").split(","));
	}

	@Override
	public List<String> getToolKeyset(String toolName) throws InsightsCustomException {

		GraphResponse neo4jResponse = new GraphResponse();
		try {
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			neo4jResponse = dbHandler.executeCypherQuery("MATCH(n:DATA:" + toolName
					+ ") with KEYS(n) AS keys UNWIND keys AS key RETURN COLLECT(DISTINCT key) AS props");
		} catch (GraphDBException e) {

			LOG.debug("Exception in neo4j");
			throw new InsightsCustomException(e.getMessage());
		}
		return Arrays.asList(neo4jResponse.getJson().getAsJsonArray("results").getAsJsonArray().get(0).getAsJsonObject()
				.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString()
				.replaceAll("[\\[\\](){}\"\\\"\"]", "").split(","));

	}

}
