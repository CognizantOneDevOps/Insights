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
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
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

	@Override
	public JsonObject getCacheResults(String requestPayload) {

		JsonObject resultJson = null;
		JsonParser parser = new JsonParser();
		JsonObject requestJson = parser.parse(requestPayload).getAsJsonObject();
		String isTestDBConnectivity = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
				.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.TEST_DATABASE)
				.toString();
		try {
			if (isTestDBConnectivity.equals("true")) {
				log.debug("\n\nQuery Caching Test Request For Data Source Connectivity Found.");
				resultJson = getNeo4jDatasourceResults(requestPayload);
			} else {
				log.debug("\n\nFetching Query Cache Results.");
				resultJson = getEsCachedResults(requestPayload);
			}
		} catch (GraphDBException e) {
			log.error("Error caught - ", e);
		}
		return resultJson;

	}

	private JsonObject getNeo4jDatasourceResults(String queryjson) throws GraphDBException {

		Neo4jDBHandler Neo4jDbHandler = new Neo4jDBHandler();
		GraphResponse response = null;
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(queryjson).getAsJsonObject();

		try {
			StringBuilder stringBuilder = new StringBuilder();
			Iterator<JsonElement> iterator = json.get(QueryCachingConstants.STATEMENTS).getAsJsonArray().iterator();
			boolean checkModifier = false;
			while (iterator.hasNext()) {
				stringBuilder = stringBuilder
						.append(iterator.next().getAsJsonObject().get(QueryCachingConstants.STATEMENT).getAsString())
						.append(QueryCachingConstants.NEW_STATEMENT);
				checkModifier = validateModifierKeywords(stringBuilder.toString());
				if (checkModifier) {
					return null;
				}
			}
			String[] queriesArray = stringBuilder.toString().split(QueryCachingConstants.NEW_STATEMENT);
			response = Neo4jDbHandler.executeCypherQueryMultiple(queriesArray);
		} catch (GraphDBException e) {
			log.error("\n\nException in neo4j query execution", e);
			throw e;
		}
		return response.getJson();
	}

	private JsonObject getEsCachedResults(String requestPayload) {

		try {
			log.debug("\n\nInside Get Query Caching Results Method Call.");
			JsonParser parser = new JsonParser();
			JsonObject requestJson = parser.parse(requestPayload).getAsJsonObject();
			boolean isCacheResult = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
					.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.RESULT_CACHE)
					.getAsBoolean();

			if (isCacheResult) {
				String esCacheIndex = QueryCachingConstants.ES_CACHE_INDEX;
				if (esCacheIndex == null)
					esCacheIndex = QueryCachingConstants.DEFAULT_ES_CACHE_INDEX;
				else
					esCacheIndex = esCacheIndex + "/querycacheresults";
				String sourceESCacheUrl = QueryCachingConstants.ES_HOST + "/" + esCacheIndex;
				log.debug("\n\nQuery Caching Index Found As: " + sourceESCacheUrl);
				String cachingType = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject()
						.get(QueryCachingConstants.CACHING_TYPE).getAsString();
				log.debug("Selected Caching Type Found As: " + cachingType);
				int cachingValue = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject()
						.get(QueryCachingConstants.CACHING_VALUE).getAsInt();
				log.debug("Selected Caching Value Found As: " + cachingValue);
				String startTimeStr = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.START_TIME)
						.getAsString();
				String endTimeStr = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.END_TIME)
						.getAsString();
				String statement = "";
				StringBuilder tempStatementsCombination = new StringBuilder();

				Iterator<JsonElement> iterator = requestJson.get(QueryCachingConstants.STATEMENTS).getAsJsonArray()
						.iterator();
				boolean checkModifier = false;
				while (iterator.hasNext()) {
					statement = iterator.next().getAsJsonObject().get(QueryCachingConstants.STATEMENT).getAsString();
					checkModifier = validateModifierKeywords(statement);
					if (checkModifier) {
						return null;
					}
					String statementWithoutTime = getStatementWithoutTime(statement, startTimeStr, endTimeStr);
					tempStatementsCombination.append(statementWithoutTime);
				}

				statement = tempStatementsCombination.toString();
				Long startTime = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.START_TIME)
						.getAsLong();
				Long endTime = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.END_TIME)
						.getAsLong();
				Long currentTime = InsightsUtils.getCurrentTimeInSeconds();

				String cacheDetailsHash = getCacheDetailsHash(cachingType, cachingValue);
				String queryHash = DigestUtils.md5Hex(statement + cacheDetailsHash).toUpperCase();
				String esQuery = "";

				if (cachingType.equalsIgnoreCase(QueryCachingConstants.FIXED_TIME)) {
					esQuery = esQueryWithFixedTime(LOAD_CACHETIME_QUERY_FROM_RESOURCES, currentTime, cachingValue,
							queryHash);
				} else {
					esQuery = esQueryTemplateWithVariance(LOAD_CACHEVARIANCE_QUERY_FROM_RESOURCES, cachingValue,
							startTime, endTime, queryHash);
				}

				JsonObject esResponse = esDbHandler.queryES(sourceESCacheUrl + "/_search", esQuery);
				JsonArray esResponseArray = new JsonArray();

				if (esResponse.has("status") && esResponse.get("status").getAsInt() == 404)
					log.debug("\n\nNo such elasticsearch index is found. Creating a new index - " + sourceESCacheUrl);
				else
					esResponseArray = esResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();

				if (esResponseArray.size() != 0) {
					log.debug("\n\nQuery Caching Response Found At Index: " + sourceESCacheUrl);
					esResponse = esResponseArray.get(0).getAsJsonObject().get("_source").getAsJsonObject();
				} else {
					log.debug(
							"\n\nNo Query Cached Results Found In Elasticsearch. Redirecting & Fetching Results From Neo4j.");
					JsonObject saveCache = new JsonObject();

					JsonObject graphResponse = null;
					graphResponse = getNeo4jDatasourceResults(requestPayload);
					saveCache.addProperty(QueryCachingConstants.QUERY_HASH, queryHash);
					saveCache.addProperty(QueryCachingConstants.CACHING_TYPE, cachingType);
					saveCache.addProperty(QueryCachingConstants.CACHING_VALUE, cachingValue);
					saveCache.addProperty(QueryCachingConstants.START_TIME_RANGE, startTime);
					saveCache.addProperty(QueryCachingConstants.END_TIME_RANGE, endTime);
					saveCache.addProperty(QueryCachingConstants.CACHE_RESULT, graphResponse.toString());
					saveCache.addProperty(QueryCachingConstants.CREATION_TIME, currentTime);

					esDbHandler.queryES(sourceESCacheUrl, saveCache.toString());
					log.debug("\n\nSaving Fetched Neo4j Results Into Elasticsearch!");
					return parser.parse(saveCache.get(QueryCachingConstants.CACHE_RESULT).getAsString())
							.getAsJsonObject();
				}
				return parser.parse(esResponse.get(QueryCachingConstants.CACHE_RESULT).getAsString()).getAsJsonObject();
			} else {
				log.debug("\n\nQuery Caching Option Is Not Selected. Fetching Results From Neo4j.");
				return getNeo4jDatasourceResults(requestPayload);
			}

		} catch (Exception e) {
			log.error("\n\nQuery Caching - Error in capturing Elasticsearch response", e);
			try {
				return getNeo4jDatasourceResults(requestPayload);
			} catch (GraphDBException graphDBEx) {
				log.error("\n\nQuery Caching - Exception in neo4j query execution", graphDBEx);
			}
		}

		return null;
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
				"\n\nInside loadEsQueryFromJsonFile method. Loading Elasticsearch - Query Caching Query From Resources!");
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
			return org.apache.commons.io.IOUtils.toString(reader);
		} catch (Exception e) {
			log.error("\n\nError in reading file!" + e);
		}
		return null;
	}

	private boolean validateModifierKeywords(String query) {
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
			log.error("Exception caught in validateModifierKeywords method. " + e);
		}
		return isModifier;
	}

}