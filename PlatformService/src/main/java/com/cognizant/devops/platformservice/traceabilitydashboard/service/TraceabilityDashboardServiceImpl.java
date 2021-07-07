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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformservice.config.PlatformServiceStatusProvider;
import com.cognizant.devops.platformservice.traceabilitydashboard.constants.TraceabilityConstants;
import com.cognizant.devops.platformservice.traceabilitydashboard.util.TraceabilitySummaryUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author 787498 (Sanket)
 *
 */
@Service("TreceabilityDashboardService")
public class TraceabilityDashboardServiceImpl implements TraceabilityDashboardService {

	Cache<String, String> pipelineCache;
	Cache<String, String> masterdataCache;
	String toolName;
	String fieldName;
	List<String> fieldValue;
	String cacheKey;
	Map<String,List<JsonObject>> mapOfPayload=new HashMap<>();

	static final String PATTERN = "[\\[\\](){}\"\\\"\"]";
	static final String DATE_PATTERN = "MM/dd/yyyy HH:mm:ss";
	JsonObject dataModel = null;
	static final String DATA_MODEL_FILE_RESOLVED_PATH = System.getenv().get(TraceabilityConstants.ENV_VAR_NAME)
			+ File.separator + TraceabilityConstants.DATAMODEL_FOLDER_NAME + File.separator
			+ TraceabilityConstants.DATAMODEL_FILE_NAME;
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();

	HashMap<String, String> handOverTimeMap = new HashMap<>();	
	HashMap<String, String> displayPropertyMap = new HashMap<>();
	HashMap<String, JsonArray> toolWiseSummaryMap = new HashMap<>();
	GsonBuilder gsonBuilder = new GsonBuilder();

	private static final Logger LOG = LogManager.getLogger(TraceabilityDashboardServiceImpl.class.getName());
	{

		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("traceability",
						CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
								ResourcePoolsBuilder.newResourcePoolsBuilder().heap(30, EntryUnit.ENTRIES).offheap(10,
										MemoryUnit.MB)))
				.build();
		cacheManager.init();
		LOG.debug("Traceability===== Cache Manaher Initilized ");
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

	
	/** this method loads traceability json from database.
	 * @throws InsightsCustomException
	 */
	private void loadTraceabilityJson() throws InsightsCustomException {
		try {

			List<InsightsConfigFiles> configFile = configFilesDAL
					.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.TRACEABILITY.name());
			String configFileData = new String(configFile.get(0).getFileData(), StandardCharsets.UTF_8);
			dataModel = (JsonObject) new JsonParser().parse(configFileData);
			LOG.debug("Traceability ===== Traceability.json is present and loaded properly");
			PlatformServiceStatusProvider.getInstance().createPlatformServiceStatusNode(
					"Traceability.json is successfully loaded.", PlatformServiceConstants.SUCCESS);
			getToolDisplayProperties();
		} catch (JsonSyntaxException e) {
			LOG.error("Traceability =====Traceability.json is not formatted");
			PlatformServiceStatusProvider.getInstance().createPlatformServiceStatusNode(
					"Traceability.json is not formatted", PlatformServiceConstants.FAILURE);
			throw new InsightsCustomException("Traceability.json is not formatted");

		} catch (Exception e) {
			PlatformServiceStatusProvider.getInstance().createPlatformServiceStatusNode(
					"Error while loading Traceability.json", PlatformServiceConstants.FAILURE);
			LOG.error("Error while loading Traceability.json");
			throw new InsightsCustomException(
					"Could not load Traceability.json. Please check whether the file has been uploaded in Configuration File Management.");
		}
	}
    
	/**this methods takes master response and extract handover time sort it and prepare summary.
	 * @param map
	 * @param dataModel
	 * @return
	 * @throws InsightsCustomException
	 */
	private JsonObject getPipeLineResponse(LinkedHashMap<String, List<JsonObject>> map, JsonObject dataModel)
			throws InsightsCustomException {
		JsonArray pipeLineArray = new JsonArray();
		JsonObject pipeLineObject = new JsonObject();
		JsonArray  sortedCombinedSummary= new JsonArray();
		mapOfPayload= new HashMap<>();
		toolWiseSummaryMap.clear();
		Set<Entry<String, List<JsonObject>>> keyset = map.entrySet();
		
		/* Prepare Summary */
		JsonObject summaryObj = prepareSummary(map, dataModel);
		
		/* Pipeline Response prepare metadata */
		prepareMetaData();
		
		handOverTimeMap.forEach((k,v)->{                 
	        LOG.debug(" handOverTimeMap handOverTime  key {} value {} ",k,v);
	     });
		
		for (Map.Entry<String, List<JsonObject>> keyvaluePair : keyset) {
			JsonObject combinedSummary = new JsonObject();
			combinedSummary.addProperty(TraceabilityConstants.TOOL_NAME, keyvaluePair.getKey());
			List<JsonObject> toolPayload=map.get(keyvaluePair.getKey());
			combinedSummary.addProperty(TraceabilityConstants.RECORD_COUNT, Integer.toString(toolPayload.size()));
			combinedSummary.addProperty("displayProperty",displayPropertyMap.get(keyvaluePair.getKey()));
			combinedSummary.addProperty("handOverTime",handOverTimeMap.get(keyvaluePair.getKey()));
			List<JsonObject> limitedList = keyvaluePair.getValue().stream().collect(Collectors.toList());
			limitedList.forEach(pipeLineArray::add);
			//Handover time object extraction and sorting
			combinedSummary.add("summaryDetail",toolWiseSummaryMap.get(keyvaluePair.getKey()));
			sortedCombinedSummary.add(combinedSummary);
		}

		JsonArray summaryArray = new JsonArray();
		summaryArray.add(summaryObj);
		JsonObject displayPropertyObj= new JsonObject();
		JsonObject displayToolProperty= new JsonObject();
		displayPropertyMap.forEach(displayToolProperty::addProperty);
		displayPropertyObj.add(TraceabilityConstants.DISPLAY_PROPS_TEXT, displayToolProperty);
		
		/* Pipeline Response, Timelag Response */
		pipeLineObject.add(TraceabilityConstants.PIPELINE, pipeLineArray);
		pipeLineObject.add(TraceabilityConstants.SUMMARY, summaryArray);
		pipeLineObject.add(TraceabilityConstants.METADATA, displayPropertyObj);
		pipeLineObject.add(TraceabilityConstants.COMBINED_SUMMARY, sortedCombinedSummary);
		return pipeLineObject;
	}
     
	/**accept the list of uuids and convert them into comma separated array of uuids
	 * @param val
	 * @return
	 */
	public static JsonArray stringify(List<String> val) {
		JsonArray jsArray = new JsonArray();
		for (String value : val) {
			jsArray.add(value);
		}
		return jsArray;
	}

	/** builds the cypher query based on hops
	 * @param toolname
	 * @param toolField
	 * @param toolCategory
	 * @param toolVal
	 * @param excludeLabels
	 * @param hopCount
	 * @return
	 */
	public String buildCypherQuery(String toolname, String toolField, String toolCategory, List<String> toolVal,
			List<String> excludeLabels, int hopCount) {
		StringBuilder queryBuilder = new StringBuilder();
		if (hopCount == 1) {
			return queryBuilder.append("match(n:").append(toolCategory).append(":" + toolname).append(":DATA{")
					.append(toolField).append(":").append("'").append(toolVal.get(0)).append("'")
					.append("}) return n.toolName as toolname ,n.uuid as uuid").toString();
		} else if (hopCount == 2) {
			queryBuilder.append("MATCH (a:").append(toolname).append(":DATA:").append(toolCategory)
					.append(") -[r]- (b:DATA) WHERE ");
			if (!toolField.equals("")) {
				queryBuilder.append("a.").append(toolField).append(" IN ").append(stringify(toolVal)).append(" ")
						.append("and exists(b.toolName) ");
			}
			if (!excludeLabels.isEmpty()) {
				queryBuilder.append("AND NOT (");
				for (String excludeLabel : excludeLabels) {
					queryBuilder.append("b:").append(excludeLabel).append(" OR ");
				}
				queryBuilder.replace(0, queryBuilder.length(),
						queryBuilder.substring(0, queryBuilder.lastIndexOf("OR")));
				queryBuilder.append(")");
			}
			queryBuilder.append(
					"with case when exists(r.handovertime) then collect(r.handovertime) else [] end as val, a, b");
			queryBuilder.append(" unwind(case val when [] then [null] else val end) as list ");
			queryBuilder.append("return  b.toolName as toolName, collect(distinct b.uuid) as uuids ,b as nodes, abs(avg(list))");
			return queryBuilder.toString();
		} else if (hopCount == 3) {
			return "MATCH (a:DATA) WHERE a.uuid IN " + stringify(toolVal)
					+ " WITH distinct a.toolName as toolName, collect(distinct a) as nodes Return toolName, nodes";
		} else if (hopCount == 4) {
			return queryBuilder.append("match(n:").append(toolCategory).append(":" + toolname).append(":DATA{")
					.append(toolField).append(":").append("'").append(toolVal.get(0)).append("'").append("})")
					.append(" return n").toString();

		} else if (hopCount == 5) {
			return queryBuilder.append("match(n:").append(toolCategory).append(":" + toolname).append(":DATA{")
					.append("epicKey").append(":").append("'").append(toolVal.get(0)).append("'").append("})")
					.append(" return n").toString();
		}

		else if (hopCount == 6) {
			return queryBuilder.append("match(n:").append(toolCategory).append(":" + toolname).append(":DATA)")
					.append("where n.").append(toolField).append(" IN ").append(stringify(toolVal))
					.append(" return n.toolName as toolname ,n.uuid as uuid ,n as nodes").toString();
		}else {
			return "MATCH (a:" + toolname + ":DATA) WHERE a.uuid IN " + stringify(toolVal)
					+ " WITH distinct a.toolName as toolName, collect(distinct a) as nodes Return toolName, nodes";
		}

	}
    
	/**extracts the uuids , nodes after every cypher execution and add them into map for further processing.
	 * @param resp
	 * @param sourceTool
	 * @return
	 */
	public Map<String, List<String>> format(JsonObject resp) {
		Map<String, List<String>> mapOfToolAndUUIDS = new HashMap<>();
		JsonElement val = null;
		int dataArraySize = resp.getAsJsonArray(TraceabilityConstants.RESULTS).getAsJsonArray().get(0).getAsJsonObject()
				.get("data").getAsJsonArray().size();
		List<String> uuidList = new ArrayList<>();
		
		for (int i = 0; i < dataArraySize; i++) {
			List<JsonObject> payload=new ArrayList<>();
			JsonArray rowArray = resp.getAsJsonArray(TraceabilityConstants.RESULTS).getAsJsonArray().get(0)
					.getAsJsonObject().get("data").getAsJsonArray().get(i).getAsJsonObject().get("row")
					.getAsJsonArray();
			if (rowArray.get(1).isJsonArray()) {
				JsonArray arrOfuuid = rowArray.get(1).getAsJsonArray();
				val = rowArray.get(3);	
				for (int j = 0; j < arrOfuuid.size(); j++) {
					uuidList.add(arrOfuuid.get(j).getAsString());
					payload.add(rowArray.get(2).getAsJsonObject());
				}
			} else {
				uuidList.add(rowArray.get(1).getAsString());
				payload.add(rowArray.get(2).getAsJsonObject());
			}
			if (null != val && !val.isJsonNull()) {
				LOG.debug(" handOverTime actual values tool Name {} time {} ",rowArray.get(0).getAsString(),val.getAsLong());
				handOverTimeMap.put(rowArray.get(0).getAsString(),
						InsightsUtils.getDateTimeFromEpoch(val.getAsLong()));
			}
			mapOfToolAndUUIDS.put(rowArray.get(0).getAsString(), uuidList);
			if (mapOfPayload.containsKey(rowArray.get(0).getAsString())) {
				mapOfPayload.get(rowArray.get(0).getAsString()).addAll(payload);
			} else {
				mapOfPayload.put(rowArray.get(0).getAsString(), payload);
			}
		}

		return mapOfToolAndUUIDS;
	}

	/** Executes the given input cypher query
	 * @param query
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonObject executeCypherQuery(String query) throws InsightsCustomException {
		
		GraphDBHandler dbHandler = new GraphDBHandler();
		JsonObject neo4jResponse = dbHandler.executeCypherQueryForJsonResponse(query);			
		LOG.debug("Traceability ===== response received from neo4j");
		return neo4jResponse;

	}

	/** extracts the list of uptool for given base tool using datamodel.
	 * @param toolName
	 * @param dataModel
	 * @return
	 * @throws InsightsCustomException
	 */
	private List<String> getUpTool(String toolName, JsonObject dataModel) throws InsightsCustomException {
		final String UPTOOL = "uptool";
		List<String> tools = new ArrayList<>();
		JsonObject toolObject = dataModel.getAsJsonObject(toolName);		
		if (toolObject.has(UPTOOL)) {
			if (toolObject.get(UPTOOL).isJsonArray()) {
				JsonArray toolArray = toolObject.get(UPTOOL).getAsJsonArray();
				for (int i = 0; i < toolArray.size(); i++) {
					tools.add(toolArray.get(i).getAsString());
				}
				return tools;
			} else {
				throw new InsightsCustomException("Traceability not configured properly in datamodel.");
			}

		} else {
			throw new InsightsCustomException("Traceability not configured properly in datamodel.");
		}

	}

	/**sorts the input payload with time.
	 * @param payload
	 * @return
	 */
	private List<JsonObject> sortToolsPayload(List<JsonObject> payload) {
		payload.sort((JsonObject o1, JsonObject o2) -> {
			final String KEY_NAME = "inSightsTimeX";
			try {
				long d1 = InsightsUtils.getEpochTime(o1.get(KEY_NAME).getAsString());
				long d2 = InsightsUtils.getEpochTime(o2.get(KEY_NAME).getAsString());
				// use to sort descending order 
				if (d1 < d2) {
					return 1;
				} else if (d1 == d2) {
					return 0;
				} else {
					return -1;
				}
			} catch (Exception e) {
				LOG.error("Traceability==== No date availble for sorting");
				return 0;
			}

		});
		return payload;
	}
	
	/**takes the neo4j response and then format it according to traceability json structure
	 * @param response
	 * @param dataModel
	 * @return
	 */
	private Map<String, List<JsonObject>> getMasterResponse(Map<String,List<JsonObject>> response, JsonObject dataModel) {

		// Master Map contains toolname as string and list of toolpayload .
		HashMap<String, List<JsonObject>> masterMap = new HashMap<>();
		LinkedHashMap<String, List<JsonObject>> sortedmasterMap = new LinkedHashMap<>();		
		
		Set<String> toolPayloadFromNeo4j = response.keySet();
		for (String toolFromNeo4j : toolPayloadFromNeo4j) {
			List<JsonObject> toolsPayload = new ArrayList<>();

			if (dataModel.has(toolFromNeo4j)) {
				JsonObject toolPayloadFromDatamodel = dataModel.get(toolFromNeo4j).getAsJsonObject();
				if (toolPayloadFromDatamodel != null) {
					Set<Entry<String, JsonElement>> keyset = toolPayloadFromDatamodel.entrySet();
					List<JsonObject> toolPayload=response.get(toolFromNeo4j);
					for (JsonObject eachObjectFromNeo4j : toolPayload) {
						JsonObject formattedJsonObject = new JsonObject();
						for (Map.Entry<String, JsonElement> toolKeyValueSetFromDataModel : keyset) {
							// Every tool is JsonArray so loop it if it has more than one element get
							// extract the value of the each key
							if (toolKeyValueSetFromDataModel.getKey().equals(TraceabilityConstants.ORDER)) {
								formattedJsonObject.addProperty(toolKeyValueSetFromDataModel.getKey(),
										toolKeyValueSetFromDataModel.getValue().toString());
							}
							if (!toolKeyValueSetFromDataModel.getValue().isJsonArray()) {

								JsonElement propertyValFromNeo4j = eachObjectFromNeo4j
										.get(toolKeyValueSetFromDataModel.getValue().getAsString());

								if (propertyValFromNeo4j != null) {
									String neo4jValue = propertyValFromNeo4j.getAsString();
									formattedJsonObject.addProperty(toolKeyValueSetFromDataModel.getKey(), neo4jValue);
								}
							}
						}
						/* Add toolStatus property explicitly if the object does not have it already */
						if (formattedJsonObject.get("toolstatus") == null)
							formattedJsonObject.addProperty("toolstatus", "Success");
						formattedJsonObject.addProperty(TraceabilityConstants.COUNT,
								Integer.toString(toolPayload.size()));
						toolsPayload.add(formattedJsonObject);

					}

					/* sort the tools payload with timestamp before adding into the mastermap */
					List<JsonObject> sortedPayload = sortToolsPayload(toolsPayload);
					/* prepare the summary */
					/* Add each tool list payload to mastermap with toolname as key */
					masterMap.put(toolFromNeo4j, sortedPayload);

				}
			}
		}
		List<Map.Entry<String, List<JsonObject>>> list = new LinkedList<>(masterMap.entrySet());
		Collections.sort(list, (Map.Entry<String, List<JsonObject>> o1, Map.Entry<String, List<JsonObject>> o2) -> {
			if (o1.getValue().get(0).has(TraceabilityConstants.ORDER)) {
				return (Integer.valueOf(o1.getValue().get(0).get(TraceabilityConstants.ORDER).getAsInt()).compareTo(
						(Integer.valueOf(o2.getValue().get(0).get(TraceabilityConstants.ORDER).getAsInt()))));
			} else {
				return -1;
			}
		});
		for (Map.Entry<String, List<JsonObject>> entry : list) {
			sortedmasterMap.put(entry.getKey(), entry.getValue());
		}
		LOG.debug("Traceability ===== Master Response Received Successfully");
		return sortedmasterMap;
	}
	

	
	

	/**prepare the summary based on message clause provided in treceability json for given tools
	 * @param masterMap
	 * @param dataModel
	 * @return
	 * @throws InsightsCustomException
	 */
	private JsonObject prepareSummary(HashMap<String, List<JsonObject>> masterMap, JsonObject dataModel)
			throws InsightsCustomException {

		final String OPERAND_NAME = "OperandName";
		final String OPERAND_VALUE = "OperandValue";
		HashMap<String, List<JsonObject>> map = masterMap;
		JsonObject summaryObject = new JsonObject();
		StringBuilder summaryMessage = new StringBuilder();
		summaryMessage.append("This search has fetched ");
		for (Map.Entry<String, List<JsonObject>> entry : map.entrySet()) {
			try {
				String tool = entry.getKey();
				List<JsonObject> payload = entry.getValue();
				JsonArray messagesArray = new JsonArray();
				JsonObject summary = new JsonObject();
				JsonObject toolObjectFromDataModel = dataModel.get(tool).getAsJsonObject();
				String displayText = toolObjectFromDataModel.has("displayText")?toolObjectFromDataModel.get("displayText").getAsString():tool;
				summaryMessage.append(payload.size()+" "+displayText);
				summaryMessage.append(", ");
				if (toolObjectFromDataModel.has(TraceabilityConstants.MESSAGES)) // check the toolname has message
				{
					int messageSize = toolObjectFromDataModel.get(TraceabilityConstants.MESSAGES).getAsJsonArray()
							.size();
					for (int i = 0; i < messageSize; i++) {
						JsonObject messageClause = toolObjectFromDataModel.get(TraceabilityConstants.MESSAGES)
								.getAsJsonArray().get(i).getAsJsonObject();
						
						String operationName = messageClause.get("Operation").getAsString();
						String operandName = messageClause.get(OPERAND_NAME).getAsString();
						JsonArray operandValue = messageClause.get(OPERAND_VALUE).getAsJsonArray();
						String message = messageClause.get(TraceabilityConstants.MESSAGE).getAsString();
						String resp="";
						if (operationName.equals("SUM")) {
							resp = TraceabilitySummaryUtil.calSUM(operandName, operandValue, payload, message);
						}else if (operationName.equals("COUNT")) {
							resp = TraceabilitySummaryUtil.calCount(operandName, operandValue, payload, message);
						}else if (operationName.equals("PROPERTY_COUNT")) {
							resp = TraceabilitySummaryUtil.calPropertyCount(operandName, operandValue, payload, message);
						}else if (operationName.equals("PERCENTAGE")) {
							resp = TraceabilitySummaryUtil.calPercentage(operandName, operandValue, payload,
									message);
						}else if (operationName.equals("TIMEDIFF")) {
							resp = TraceabilitySummaryUtil.calTimeDiffrence(operandName, payload, message);
						}else if (operationName.equals("DISTINCT_COUNT")) {
							resp = TraceabilitySummaryUtil.calDistinctCount(operandName, operandValue, payload, message);
						}else {
							LOG.error("No claculation method found to calculate summary information ");
						}
						if (!resp.equals("")) {
							summary.addProperty(String.valueOf(i), resp);
							messagesArray.add(resp);
						}
					}
					LOG.debug("prepareSummary toolWiseSummaryMap tool {} messagesArray {} ",tool,messagesArray);
					toolWiseSummaryMap.put(tool, messagesArray);
				}
			} catch (Exception e) {
				LOG.error(e);
				throw new InsightsCustomException("Traceability dashboard not configured properly...");
			}
		}
		summaryMessage.setLength(summaryMessage.length() - 2);
		summaryObject.addProperty("Total", summaryMessage.toString());
		return summaryObject;

	}

	/** extracts the list of downtool for given provided base tool using traceability json
	 * @param toolName
	 * @param dataModel
	 * @return
	 * @throws InsightsCustomException
	 */
	private List<String> getDownTool(String toolName, JsonObject dataModel) throws InsightsCustomException {
		final String DOWNTOOL = "downtool";
		List<String> tools = new ArrayList<>();
		JsonObject toolObject = dataModel.getAsJsonObject(toolName);		
		if (toolObject.has(DOWNTOOL)) {
			if (toolObject.get(DOWNTOOL).isJsonArray()) {
				JsonArray toolArray = toolObject.get(DOWNTOOL).getAsJsonArray();
				for (int i = 0; i < toolArray.size(); i++) {
					tools.add(toolArray.get(i).getAsString());
				}
				return tools;
			} else {
				throw new InsightsCustomException("Traceability matrix not configured properly.");
			}
		} else {
			throw new InsightsCustomException("Traceability matrix not configured properly.");
		}

	}

	
	

	/** this function extracts all the tools in a neo4j relationship , drilldown till then end of neo4j relationship
	 * @param drilldownListMap
	 * @param baseTool
	 * @return
	 * @throws InsightsCustomException
	 */
	private HashMap<String, List<String>> resolveUpAndDownLinks(HashMap<String, List<String>> drilldownListMap,
			List<String> baseTool) throws InsightsCustomException {

		HashMap<String, List<String>> mainToolList = new HashMap<>();
		List<String> excludeLabels = new ArrayList<>();
		excludeLabels.addAll(baseTool);			
		while (drilldownListMap.size() > 0) {
		  
			List<String> labelsTemp = new ArrayList<>();
			HashMap<String, List<String>> tempMap = new HashMap<>();
			for (Map.Entry<String, List<String>> entry : drilldownListMap.entrySet()) {
				LOG.debug(" resolveUpAndDownLinks {}",entry.getKey());
				String tool = entry.getKey();
				List<String> uuids = entry.getValue();
				mainToolList.put(tool, uuids);
				if (!(dataModel.has(tool) && dataModel.get(tool).getAsJsonObject().has(TraceabilityConstants.CATEGORY))) {
					throw new InsightsCustomException("No category defined for tool :" + tool);
				}
				excludeLabels.addAll(getExcludeLabelList(tool));
				String category = dataModel.get(tool).getAsJsonObject().get(TraceabilityConstants.CATEGORY).getAsString();				
				String cypher = buildCypherQuery(tool, "uuid", category, uuids, excludeLabels, 2);
			
				/* collect all parent or child uuids for current basenode. */
				
				tempMap.putAll(format(executeCypherQuery(cypher)));
				/* collect the tools as basetool for the next hop in drilldown */
				labelsTemp.addAll(Arrays.asList(tool));

			}
			excludeLabels = (labelsTemp.stream().distinct().collect(Collectors.toList()));
			drilldownListMap.clear();
			drilldownListMap.putAll(tempMap);
		}
		
		return mainToolList;
	}

	/**
	 *  entry level function which accepts the selection from UI and distribute tasks to functions and returns the final response.
	 */
	@Override
	public JsonObject getPipeline(String toolName, String fieldName, List<String> fieldValue, String type)
			throws InsightsCustomException {
		loadTraceabilityJson();
		this.toolName = toolName;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		mapOfPayload= new HashMap<>();
		cacheKey = toolName + "." + fieldName + "." + type + "." + fieldValue;
		if (pipelineCache.get(cacheKey) != null) {
			LOG.debug("Pipeline response loaded from cache for key {} "
					,cacheKey);
			return new JsonParser().parse(pipelineCache.get(cacheKey)).getAsJsonObject();
		} else {
			try {
				/* Load JsonObject from DataModel.json for processing */
				/* check if it is already loaded */
				HashMap<String, List<String>> uplinkMap = new HashMap<>();
				HashMap<String, List<String>> downlinkMap = new HashMap<>();
				HashMap<String, List<String>> toolsListMap = new HashMap<>();
				/* Get the uuid of the tool selected in UI and store it in Main Map */
				if (!dataModel.get(toolName).getAsJsonObject().has(TraceabilityConstants.CATEGORY)) {
					throw new InsightsCustomException("No category defined for tool :" + this.toolName);
				}
				String toolCategory = dataModel.get(toolName).getAsJsonObject().get(TraceabilityConstants.CATEGORY).getAsString();
				if (type.equals("Epic")) {
					return processEpic(toolName, fieldName, toolCategory, new ArrayList<String>(fieldValue));

				} else {
					String cypher = buildCypherQuery(toolName, fieldName, toolCategory,
							new ArrayList<String>(fieldValue), Collections.emptyList(), 6);
					toolsListMap.putAll(format(executeCypherQuery(cypher)));

					/* Get the upTool and DownTool of the selected tool in UI */
					List<String> upTools = getUpTool(toolName, dataModel);
					List<String> downTools = getDownTool(toolName, dataModel);
					/*
					 * Execute the first query to get the linked tools for the tool selected in UI
					 */
					List<String> excludeLabels =getExcludeLabelList(toolName);
					String cypherQuery = buildCypherQuery(toolName, fieldName, toolCategory,
							new ArrayList<String>(fieldValue), excludeLabels, 2);
					JsonObject hopQueryOutput = executeCypherQuery(cypherQuery);
					HashMap<String, List<String>> temp = (HashMap<String, List<String>>) format(hopQueryOutput);

					if (temp.size() > 0) {
						uplinkMap = (HashMap<String, List<String>>) temp.entrySet().stream()
								.filter(e -> upTools.stream().anyMatch(e.getKey()::equals))
								.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

						downlinkMap = (HashMap<String, List<String>>) temp.entrySet().stream()
								.filter(e -> downTools.stream().anyMatch(e.getKey()::equals))
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
					List<String> uuids = new ArrayList<>();
					for (List<String> uuidList : toolsListMap.values()) {
						uuidList.forEach(uuids::add);
					}				
					/* Get the MasterResponse Map which will used for caching */				
					LinkedHashMap<String, List<JsonObject>> masterMap= (LinkedHashMap<String, List<JsonObject>>) getMasterResponse(mapOfPayload,dataModel);
					LOG.debug("Traceability data has been loaded successfully");
					/* Filter MaterMap and send only first 4 JsonObjects for the tool */
					JsonObject response = getPipeLineResponse(masterMap, dataModel);
					if (type.equals(TraceabilityConstants.OTHER_PIPELINE)) {
						response = formatOrder(response);
					}
					LOG.debug("Pipeline response prepared successfully");
					/* Get the MasterResponse Map which will used for caching */
					pipelineCache.put(cacheKey, response.toString());
					return response;
				}

			} catch (JsonSyntaxException | JsonIOException | InsightsCustomException ex1) {
				LOG.error(ex1.getMessage());
				throw new InsightsCustomException(ex1.getMessage());
			}
		}
	}

	/** accepts the response and format the payload as per order number.
	 * @param response
	 * @return
	 */
	private JsonObject formatOrder(JsonObject response) {		
		int epicCounter = 0;
		JsonArray pipelineArray = response.get("pipeline").getAsJsonArray();
		for (JsonElement element : pipelineArray) {
			JsonObject eachObj = element.getAsJsonObject();
			if(!eachObj.has(TraceabilityConstants.PROPERTY)) {
				LOG.error("category(property) field not available in response json {}", eachObj);	
			} 
			if (eachObj.get(TraceabilityConstants.PROPERTY).getAsString().equals("ALM") && eachObj.has(TraceabilityConstants.ISSUE_TYPE)
					&& eachObj.get(TraceabilityConstants.ISSUE_TYPE).getAsString().equals("Epic")) {
				eachObj.addProperty("order", 1);
				eachObj.addProperty(TraceabilityConstants.TOOL_NAME, "Epic");
				epicCounter++;
			} else if (eachObj.get(TraceabilityConstants.PROPERTY).getAsString().equals("ALM") && eachObj.has(TraceabilityConstants.ISSUE_TYPE)
					&& !(eachObj.get(TraceabilityConstants.ISSUE_TYPE).getAsString().equals("Epic"))) {
				eachObj.addProperty(TraceabilityConstants.COUNT, eachObj.get(TraceabilityConstants.COUNT).getAsInt() - epicCounter);
			}
		}
		// append epic counter
		for (JsonElement element : pipelineArray) {
			JsonObject eachObj = element.getAsJsonObject();
			if (eachObj.get("property").getAsString().equals("ALM") && eachObj.has("issueType")
					&& eachObj.get("issueType").getAsString().equals("Epic")) {
				eachObj.addProperty("count", epicCounter);
			}
		}		 

		return response;

	}

	/** ALM specific function to process Epic and in turn drill down neo4j relationships
	 * @param toolName
	 * @param fieldName
	 * @param toolCategory
	 * @param arrayList
	 * @return
	 * @throws InsightsCustomException
	 */
	private JsonObject processEpic(String toolName, String fieldName, String toolCategory, ArrayList<String> arrayList)
			throws InsightsCustomException {

		String epicfieldValue = arrayList.get(0);

		// To get epic node response
		String cypher = buildCypherQuery(toolName, fieldName, toolCategory,
				new ArrayList<String>(Arrays.asList(epicfieldValue)), Collections.emptyList(), 4);
		JsonObject response = executeCypherQuery(cypher);
		List<JsonObject> epic = formatResponse(response, toolName);
		epic.forEach((eachObject) -> {
			eachObject.addProperty("order", "0");
			eachObject.addProperty("toolName", "Epic");
		});

		cypher = buildCypherQuery(toolName, fieldName, toolCategory, new ArrayList<String>(Arrays.asList(epicfieldValue)),
				Collections.emptyList(), 5);
		response = executeCypherQuery(cypher);
		List<JsonObject> issues = formatResponse(response, toolName);

		List<String> uuids = new ArrayList<>();
		issues.forEach(obj -> uuids.add(obj.get("uuid").getAsString()));		

		JsonObject resp = getEpicPipeline(toolName, uuids);
		pipelineCache.put(cacheKey, resp.toString());	
		return resp;
	}

	
	/** accepts the selected node and fetches the pipeline.
	 * @param nodeObj
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonObject getPipelineForSelectedNode(JsonObject nodeObj) throws InsightsCustomException {
		JsonObject responsePipeline = new JsonObject();
		try {
			if (nodeObj.has("issueType") && nodeObj.get("issueType").getAsString().equals("Epic")) {
				String category = nodeObj.get("property").getAsString();
				toolName = dataModel.keySet().stream().filter(eachTool -> dataModel.get(eachTool).getAsJsonObject()
						.get("category").getAsString().equals(category)).findFirst().orElse("None");
				String field = nodeObj.get("searchKey").getAsString();
				String fieldVal = nodeObj.get("issueKey").getAsString();
				responsePipeline = getPipeline(toolName, field, Arrays.asList(fieldVal), "Epic");
			} else {
				toolName = nodeObj.get("toolName").getAsString();
				String fieldVal = nodeObj.get("uuid").getAsString();
				responsePipeline = getPipeline(toolName, "uuid", Arrays.asList(fieldVal), "Other");
			}
			return responsePipeline;
		} catch (Exception e) {
			LOG.error("Traceability========mandatory fields are missing in traceability json {}", e.getMessage());
			throw new InsightsCustomException("mandatory fields are missing in traceability json {}");
		}
	}

	/**ALM specific function to fetch epic pipeline
	 * @param toolName
	 * @param uuids
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonObject getEpicPipeline(String toolName, List<String> uuids) throws InsightsCustomException {
		JsonObject responsePipeline = new JsonObject();
		try {
			responsePipeline = getPipeline(toolName, "uuid", uuids, "Other");
			return responsePipeline;
		} catch (Exception e) {
			LOG.error("Traceability========Issue while fetching epic pipeline ", e);
			throw new InsightsCustomException("Traceability========Issue while fetching epic pipeline "+e.getMessage());
		}
	}

	/**format response for Epic case.
	 * @param response
	 * @param toolName
	 * @return
	 */
	private List<JsonObject> formatResponse(JsonObject response, String toolName) {

		// Master Map contains toolname as string and list of toolpayload .
		List<JsonObject> issues = new ArrayList<>();
		List<JsonObject> sortedList;
		JsonArray responseArray = response.getAsJsonArray(TraceabilityConstants.RESULTS).getAsJsonArray().get(0)
				.getAsJsonObject().get("data").getAsJsonArray();
		int count = responseArray.size();
		for (int j = 0; j < count; j++) {
			// find the toolname in neo4j response
			LOG.debug("Data From Neo4J has been loaded properly for tool {} ",toolName);
			JsonArray toolsArray = responseArray.get(j).getAsJsonObject().get("row").getAsJsonArray();
			JsonObject toolPayloadFromDatamodel = dataModel.get(toolName).getAsJsonObject();
			if (toolPayloadFromDatamodel != null) {
				Set<Entry<String, JsonElement>> keyset = toolPayloadFromDatamodel.entrySet();
				JsonObject neo4jEacRowResponse = toolsArray.get(0).getAsJsonObject();
				JsonObject formattedJsonObject = new JsonObject();
				for (Map.Entry<String, JsonElement> toolKeyValueSetFromDataModel : keyset) {
					// Every tool is JsonArray so loop it if it has more than one element get
					// extract the value of the each key
					if (toolKeyValueSetFromDataModel.getKey().equals(TraceabilityConstants.ORDER)) {
						formattedJsonObject.addProperty(toolKeyValueSetFromDataModel.getKey(),
								toolKeyValueSetFromDataModel.getValue().toString());
					}
					if (!toolKeyValueSetFromDataModel.getValue().isJsonArray()) {
						JsonElement propertyValFromNeo4j = neo4jEacRowResponse
								.get(toolKeyValueSetFromDataModel.getValue().getAsString());
						if (propertyValFromNeo4j != null) {
							String neo4jValue = propertyValFromNeo4j.getAsString();
							formattedJsonObject.addProperty(toolKeyValueSetFromDataModel.getKey(), neo4jValue);
						}
					}
				}
				formattedJsonObject.addProperty("count", Integer.toString(count));
				issues.add(formattedJsonObject);
			}
		}
		sortedList = sortToolsPayload(issues);
		return sortedList;
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
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 *fetch available tools from treceability json to show on UI
	 */
	@Override
	public List<String> getAvailableTools() throws InsightsCustomException {
		loadTraceabilityJson();
		List<String> availableTools = new ArrayList<>();
		try {
			availableTools = dataModel.keySet().stream().collect(Collectors.toList());
		} catch (Exception e) {
			LOG.error("Treceability ==== Unable to load  datamodel");
			throw new InsightsCustomException(e.getMessage());
		}
		return availableTools;
	}

	@Override
	public List<String> getToolKeyset(String toolName) throws InsightsCustomException {
		loadTraceabilityJson();
		final String FILTER = "uifilter";
		List<String> tools = new ArrayList<>();
		JsonObject toolObject = dataModel.getAsJsonObject(toolName);
		if (toolObject.has(FILTER) && toolObject.get(FILTER).isJsonArray()) {
			JsonArray toolArray = toolObject.get(FILTER).getAsJsonArray();
			for (int i = 0; i < toolArray.size(); i++) {
				tools.add(toolArray.get(i).getAsString());
			}
			return tools;
		}
		return Collections.emptyList();
	}

	/** fetch card display properties from traceability json 
	 * @return
	 */
	public JsonObject getToolDisplayProperties() {
		JsonObject responseObj = new JsonObject();
		try {
			final String DISPLAYPROPS = "cardDisplayProps";
			Set<Entry<String, JsonElement>> entrySet = dataModel.entrySet();
			for (Entry<String, JsonElement> eachElement : entrySet) {
				String tName = eachElement.getKey();
				JsonObject toolObject = dataModel.getAsJsonObject(tName);
				JsonArray toolArray = toolObject.get(DISPLAYPROPS).getAsJsonArray();
				responseObj.add(tName, toolArray);
			}
		} catch (Exception e) {
			LOG.error("Treceability ==== responseObj",e);
		}
		return responseObj;
	}
	
	/** Used to return Display properties
	 * @return
	 */
	private void prepareMetaData() {
		JsonObject displayToolProperty = new JsonObject();
		JsonObject displayProperty = new JsonObject();
		try {
			Set<Entry<String, JsonElement>> entrySet = dataModel.entrySet();
			for (Entry<String, JsonElement> eachElement : entrySet) {
				String tName = eachElement.getKey();
				JsonObject toolObject = dataModel.getAsJsonObject(tName);
				String toolDisplayText = toolObject.has(TraceabilityConstants.DISPLAYPROPSTEXT)?toolObject.get(TraceabilityConstants.DISPLAYPROPSTEXT).getAsString():tName;
				displayToolProperty.addProperty(tName, toolDisplayText);
				displayPropertyMap.put(tName, toolDisplayText);
			}
			displayProperty.add(TraceabilityConstants.DISPLAY_PROPS_TEXT, displayToolProperty);
		} catch (Exception e) {
			LOG.error("Treceability ==== responseObj");
		}
	}
	
	/** This will return exclude label list 
	 * @param toolName
	 * @return
	 */
	private List<String> getExcludeLabelList(String toolName) {
		List<String> excludeLabels = new ArrayList<>() ;
		if (dataModel.has(toolName) && dataModel.get(toolName).getAsJsonObject().has(TraceabilityConstants.EXCLUDE_LABEL_PROPERTY)) {
			LOG.debug("Tool {} has excludeLabels list {}",toolName,excludeLabels);
			JsonArray configuredExcludeLable = dataModel.get(toolName).getAsJsonObject().get(TraceabilityConstants.EXCLUDE_LABEL_PROPERTY).getAsJsonArray();
			for (JsonElement stringExcludeLabel : configuredExcludeLable) {
				excludeLabels.add(stringExcludeLabel.getAsString());
			}
		}
		return excludeLabels;
	}
	
	public boolean clearAllCacheValue() {
		try {
			pipelineCache.clear();
			masterdataCache.clear();
			return true;
		} catch (Exception e) {
			LOG.error(" Error while clearing cache value ",e);
			return false;
		}
	}

}