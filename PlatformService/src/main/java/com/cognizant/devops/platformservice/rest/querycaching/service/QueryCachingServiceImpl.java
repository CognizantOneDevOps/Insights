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
package com.cognizant.devops.platformservice.rest.querycaching.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("queryCachingService")
public class QueryCachingServiceImpl implements QueryCachingService {

	private final String LOAD_CACHETIME_QUERY_FROM_RESOURCES = loadEsQueryFromJsonFile(
			QueryCachingConstants.LOAD_CACHETIME_QUERY_FROM_RESOURCES);
	private final String LOAD_CACHEVARIANCE_QUERY_FROM_RESOURCES = loadEsQueryFromJsonFile(
			QueryCachingConstants.LOAD_CACHEVARIANCE_QUERY_FROM_RESOURCES);
	private final ElasticSearchDBHandler esDbHandler = new ElasticSearchDBHandler();
	private static Logger log = LogManager.getLogger(QueryCachingServiceImpl.class);
	JsonParser parser = new JsonParser();

	@Override
	public JsonObject getCacheResults(String requestPayload) {

		JsonObject resultJson = null;
		JsonObject requestJson = parser.parse(requestPayload).getAsJsonObject();
		String isTestDBConnectivity = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
				.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.TEST_DATABASE)
				.toString();
		try {
			if (isTestDBConnectivity.equals("true")) {
				log.debug("Query Caching Test Request For Data Source Connectivity Found. ");
				resultJson = getNeo4jDatasourceResults(requestPayload);
			} else {
				log.debug("Fetching Query Cache Results.");
				resultJson = getEsCachedResults(requestPayload);
				log.debug("Returned Query Cache record =====");
			}
		} catch (InsightsCustomException e) {
			log.error("Error caught - ", e);
		}
		return resultJson;

	}

	private JsonObject getNeo4jDatasourceResults(String queryjson) throws InsightsCustomException {

		GraphDBHandler GraphDBHandler = new GraphDBHandler();
		GraphResponse response = null;
		JsonObject json = parser.parse(queryjson).getAsJsonObject();

		try {
			StringBuilder stringBuilder = new StringBuilder();
			Iterator<JsonElement> iterator = json.get(QueryCachingConstants.STATEMENTS).getAsJsonArray().iterator();
			while (iterator.hasNext()) {
				stringBuilder = stringBuilder
						.append(iterator.next().getAsJsonObject().get(QueryCachingConstants.STATEMENT).getAsString())
						.append(QueryCachingConstants.NEW_STATEMENT);
			}
			String[] queriesArray = stringBuilder.toString().split(QueryCachingConstants.NEW_STATEMENT);
			response = GraphDBHandler.executeCypherQueryMultiple(queriesArray);
		} catch (InsightsCustomException e) {
			log.error("Exception in neo4j query execution", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return response.getJson();
	}

	private JsonObject getEsCachedResults(String requestPayload) {

		try {
			JsonObject requestJson = parser.parse(requestPayload).getAsJsonObject();
			boolean isCacheResult = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
					.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.RESULT_CACHE)
					.getAsBoolean();

			if (isCacheResult) {

				log.debug("Inside Get Query isCacheResult Caching Results Method Call.");
				String esCacheIndex = QueryCachingConstants.ES_CACHE_INDEX;

				esCacheIndex = esCacheIndex == null ? QueryCachingConstants.DEFAULT_ES_CACHE_INDEX
						: esCacheIndex + "/querycacheresults";

				String sourceESCacheUrl = QueryCachingConstants.ES_HOST + "/" + esCacheIndex;
				String cachingType = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject()
						.get(QueryCachingConstants.CACHING_TYPE).getAsString();
				int cachingValue = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject()
						.get(QueryCachingConstants.CACHING_VALUE).getAsInt();
				Long startTime = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.START_TIME)
						.getAsLong();
				Long endTime = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.END_TIME)
						.getAsLong();
				Long currentTime = InsightsUtils.getCurrentTimeInSeconds();

				log.debug(
						"Query Caching Index {}  Caching Type {} Caching Value {}  startTimeStr {} endTimeStr {} currentTime {}",
						sourceESCacheUrl, cachingType, cachingValue, String.valueOf(startTime),
						String.valueOf(endTime), currentTime);
				String queryHash = getQueryHash(requestJson, cachingType, cachingValue, startTime, endTime);

				String esQuery = getESQueryBasedonCacheType(cachingType, cachingValue, startTime, endTime, currentTime,
						queryHash);

				JsonObject esResponse = esDbHandler.queryES(sourceESCacheUrl + "/_search", esQuery);


				if (esResponse.has(QueryCachingConstants.ES_HITS)) {
					JsonArray esResponseArray = esResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
					if (esResponseArray.size() > 0) {
						log.debug("Query Caching Record found in ES, returning record =====");
						JsonObject esSource = esResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray()
								.get(0)
							.getAsJsonObject().get("_source").getAsJsonObject();
						return parser.parse(esSource.get(QueryCachingConstants.CACHE_RESULT).getAsString())
								.getAsJsonObject();
					} else {
						log.debug("Index present in ES and record not found, adding new record for queryHash {} ",
								queryHash);
						return fetchRecordFromGraphDBAndsaveInES(requestPayload, requestJson, sourceESCacheUrl,
								cachingType, cachingValue, startTime, endTime, queryHash);
					}
				} else if (esResponse.has(QueryCachingConstants.ES_STATUS)
						&& (esResponse.get(QueryCachingConstants.ES_STATUS).getAsInt() == 404
								|| esResponse.get(QueryCachingConstants.ES_STATUS).getAsInt() == 400)) {
					log.debug("Index not found adding new index and record in ES, queryHash {} ", queryHash);
					return fetchRecordFromGraphDBAndsaveInES(requestPayload, requestJson, sourceESCacheUrl, cachingType,
							cachingValue, startTime, endTime, queryHash);
				}
			} else {
				log.debug("Query Caching Option Is Not Selected. Fetching Results From Neo4j.");
				return getNeo4jDatasourceResults(requestPayload);
			}

		} catch (Exception e) {
			log.error("Query Caching - Error in capturing Elasticsearch response", e);
			try {
				return getNeo4jDatasourceResults(requestPayload);
			} catch (InsightsCustomException graphDBEx) {
				log.error("Query Caching - Exception in neo4j query execution", graphDBEx);
			}
		}

		return null;
	}

	private String getESQueryBasedonCacheType(String cachingType, int cachingValue, Long startTime, Long endTime,
			Long currentTime, String queryHash) {
		String esQuery;
		if (cachingType.equalsIgnoreCase(QueryCachingConstants.FIXED_TIME)) {
			esQuery = esQueryWithFixedTime(LOAD_CACHETIME_QUERY_FROM_RESOURCES, currentTime, cachingValue,
					queryHash);
		} else {
			esQuery = esQueryTemplateWithVariance(LOAD_CACHEVARIANCE_QUERY_FROM_RESOURCES, cachingValue,
					startTime, endTime, queryHash);
		}
		return esQuery;
	}

	private String getQueryHash(JsonObject requestJson, String cachingType, int cachingValue, Long startTime,
			Long endTime) {
		String statement = "";
		StringBuilder tempStatementsCombination = new StringBuilder();

		Iterator<JsonElement> iterator = requestJson.get(QueryCachingConstants.STATEMENTS).getAsJsonArray().iterator();
		//boolean checkModifier = false;
		while (iterator.hasNext()) {
			statement = iterator.next().getAsJsonObject().get(QueryCachingConstants.STATEMENT).getAsString();

			String statementWithoutTime = getStatementWithoutTime(statement, String.valueOf(startTime),
					String.valueOf(endTime));
			tempStatementsCombination.append(statementWithoutTime);
		}

		statement = tempStatementsCombination.toString();
		String cacheDetailsHash = getCacheDetailsHash(cachingType, cachingValue);
		String queryHash = DigestUtils.md5Hex(statement + cacheDetailsHash).toUpperCase();
		return queryHash;
	}

	private JsonObject fetchRecordFromGraphDBAndsaveInES(String requestPayload,
			JsonObject requestJson, String sourceESCacheUrl, String cachingType, int cachingValue, Long startTime,
			Long endTime, String queryHash) throws InsightsCustomException {
		JsonObject saveCache = new JsonObject();

		JsonObject graphResponse = null;
		Long beforeQueryExecutionTime = InsightsUtils.getSystemTimeInNanoSeconds();
		graphResponse = getNeo4jDatasourceResults(requestPayload);
		String statements = requestJson.get(QueryCachingConstants.STATEMENTS).getAsJsonArray().toString();
		Long afterQueryExecutionTime = InsightsUtils.getSystemTimeInNanoSeconds();
		Long queryExecutionTimeInNanoSec = (afterQueryExecutionTime - beforeQueryExecutionTime);
		saveCache.addProperty(QueryCachingConstants.QUERY_HASH, queryHash);
		saveCache.addProperty(QueryCachingConstants.CACHING_TYPE, cachingType);
		saveCache.addProperty(QueryCachingConstants.CACHING_VALUE, cachingValue);
		saveCache.addProperty(QueryCachingConstants.START_TIME_RANGE, startTime);
		saveCache.addProperty(QueryCachingConstants.END_TIME_RANGE, endTime);
		saveCache.addProperty(QueryCachingConstants.START_TIME_IN_MS, startTime * 1000);
		saveCache.addProperty(QueryCachingConstants.END_TIME_IN_MS, endTime * 1000);
		saveCache.addProperty(QueryCachingConstants.CYPHER_QUERY, statements);
		saveCache.addProperty(QueryCachingConstants.CACHE_RESULT, graphResponse.toString());
		saveCache.addProperty(QueryCachingConstants.CREATION_TIME, InsightsUtils.getCurrentTimeInSeconds());
		saveCache.addProperty(QueryCachingConstants.QUERY_EXECUTION_TIME, queryExecutionTimeInNanoSec);
		saveCache.addProperty(QueryCachingConstants.NEO4J_RESULT_CREATION_TIME,
				InsightsUtils.getCurrentTimeInEpochMilliSeconds());

		esDbHandler.queryES(sourceESCacheUrl, saveCache.toString());
		log.debug("Saving Fetched Neo4j Results Into Elasticsearch!");
		return parser.parse(saveCache.get(QueryCachingConstants.CACHE_RESULT).getAsString())
				.getAsJsonObject();
	}

	private static String getCacheDetailsHash(String cacheType, int cachingValue) {
		String cacheDetails = "Cache Type: " + cacheType + " and Cache Value: " + cachingValue;
		return DigestUtils.md5Hex(cacheDetails).toUpperCase();
	}

	private static String getStatementWithoutTime(String statement, String startTime, String endTime) {
		return statement.replace(String.valueOf(startTime), QueryCachingConstants.START_TIME_IN_STATEMENT_REPLACE)
				.replace(String.valueOf(endTime), QueryCachingConstants.END_TIME_IN_STATEMENT_REPLACE);
	}

	private static String esQueryTemplateWithVariance(String esQuery, int cacheVariance, Long startTime, Long endTime,
			String queryHash) {

		long varianceSeconds = getVarianceEpochSeconds(cacheVariance, endTime - startTime);

		esQuery = esQuery
				.replace(String.valueOf(QueryCachingConstants.CACHED_STARTTIME),
						InsightsUtils.subtractVarianceTime(startTime, varianceSeconds).toString())
				.replace(String.valueOf(QueryCachingConstants.START_TIME_REPLACE),
						InsightsUtils.addVarianceTime(startTime, varianceSeconds).toString())
				.replace(String.valueOf(QueryCachingConstants.END_TIME_REPLACE),
						InsightsUtils.addVarianceTime(endTime, varianceSeconds).toString())
				.replace(String.valueOf(QueryCachingConstants.CACHED_ENDTIME),
						InsightsUtils.subtractVarianceTime(endTime, varianceSeconds).toString())
				.replace(String.valueOf(QueryCachingConstants.QUERY_CACHE), queryHash);
		return esQuery;
	}

	private static String esQueryWithFixedTime(String esQuery, Long currentTime, int cacheDuration, String queryHash) {

		esQuery = esQuery.replace(String.valueOf(QueryCachingConstants.QUERY_CACHE), queryHash)
				.replace(String.valueOf(QueryCachingConstants.CACHED_PREVIOUS_TIME),
						InsightsUtils.subtractTimeInHours(currentTime, cacheDuration).toString())
				.replace(String.valueOf(QueryCachingConstants.CURRENT_TIME), currentTime.toString());
		return esQuery;
	}

	private static long getVarianceEpochSeconds(int cacheVariance, Long durationSeconds) {
		return (durationSeconds * cacheVariance) / 100;
	}

	private String loadEsQueryFromJsonFile(String fileName) {
		/*
		 * BufferedReader reader = null; InputStream in = null;
		 */
		log.debug(
				"Inside loadEsQueryFromJsonFile method. Loading Elasticsearch - Query Caching Query From Resources!");
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
			return org.apache.commons.io.IOUtils.toString(reader);
		} catch (Exception e) {
			log.error("Error in reading file!", e);
		}
		return null;
	}

	/*	private boolean validateModifierKeywords(String query) {
			boolean isModifier = false;
			try {
				String queryToLowerChars = query.toLowerCase();
				String[] modifierKeywords = { " update ", " update(", ")update ", " delete ", " delete(", ")delete ",
						" detach ", " detach(", ")detach ", " set ", " set(", ")set ", " create ", " create(", ")create " };
				for (int i = 0; i < modifierKeywords.length; i++) {
					if (queryToLowerChars.contains(modifierKeywords[i])) {
						log.debug("Datasource modifier keyword found!");
						isModifier = true;
						break;
					}
				}
			} catch (Exception e) {
				log.error("Exception caught in validateModifierKeywords method. ", e);
			}
			return isModifier;
		}*/

}
