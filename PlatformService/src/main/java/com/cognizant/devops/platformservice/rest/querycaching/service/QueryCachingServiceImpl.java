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
import java.io.IOException;
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
				resultJson = getNeo4jDatasource(requestPayload);
			} else {
				resultJson = getEsCachedResults(requestPayload);
			}
		} catch (GraphDBException e) {
			log.error("Error caught - ", e);
		}
		return resultJson;

	}

	private JsonObject getNeo4jDatasource(String queryjson) throws GraphDBException {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		GraphResponse response = null;
		JsonParser parser = new JsonParser();
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
			response = dbHandler.executeCypherQueryMultiple(queriesArray);
		} catch (GraphDBException e) {
			log.error("Exception in neo4j query execution", e);
			throw e;
		}
		return response.getJson();
	}

	private JsonObject getEsCachedResults(String requestPayload) {

		try {
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
				String cachingType = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject()
						.get(QueryCachingConstants.CACHING_TYPE).getAsString();
				int cachingValue = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
						.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject()
						.get(QueryCachingConstants.CACHING_VALUE).getAsInt();
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
				while (iterator.hasNext()) {
					statement = iterator.next().getAsJsonObject().get(QueryCachingConstants.STATEMENT).getAsString();
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
				String loadEsCacheQuery = "";

				if (cachingType.equalsIgnoreCase(QueryCachingConstants.FIXED_TIME)) {
					loadEsCacheQuery = loadEsQueryFromJsonFile(
							QueryCachingConstants.LOAD_CACHETIME_QUERY_FROM_RESOURCES);
					esQuery = esQueryWithFixedTime(loadEsCacheQuery, currentTime, cachingValue, queryHash);
				} else {
					loadEsCacheQuery = loadEsQueryFromJsonFile(
							QueryCachingConstants.LOAD_CACHEVARIANCE_QUERY_FROM_RESOURCES);
					esQuery = esQueryTemplateWithVariance(loadEsCacheQuery, cachingValue, startTime, endTime,
							queryHash);
				}

				ElasticSearchDBHandler esDbHandler = new ElasticSearchDBHandler();
				JsonObject esResponse = esDbHandler.queryES(sourceESCacheUrl + "/_search", esQuery);
				JsonArray esResponseArray = new JsonArray();

				if (esResponse.has("status") && esResponse.get("status").getAsInt() == 404)
					log.debug("No such elasticsearch index is found. Creating a new index - " + sourceESCacheUrl);
				else
					esResponseArray = esResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();

				if (esResponseArray.size() != 0) {
					esResponse = esResponseArray.get(0).getAsJsonObject().get("_source").getAsJsonObject();
				} else {
					JsonObject saveCache = new JsonObject();

					JsonObject graphResponse = null;
					graphResponse = getNeo4jDatasource(requestPayload);
					saveCache.addProperty(QueryCachingConstants.QUERY_HASH, queryHash);
					saveCache.addProperty(QueryCachingConstants.CACHING_TYPE, cachingType);
					saveCache.addProperty(QueryCachingConstants.CACHING_VALUE, cachingValue);
					saveCache.addProperty(QueryCachingConstants.START_TIME_RANGE, startTime);
					saveCache.addProperty(QueryCachingConstants.END_TIME_RANGE, endTime);
					saveCache.addProperty(QueryCachingConstants.CACHE_RESULT, graphResponse.toString());
					saveCache.addProperty(QueryCachingConstants.HAS_EXPIRED, false);
					saveCache.addProperty(QueryCachingConstants.CREATION_TIME, currentTime);

					esDbHandler.queryES(sourceESCacheUrl, saveCache.toString());
					return parser.parse(saveCache.get(QueryCachingConstants.CACHE_RESULT).getAsString())
							.getAsJsonObject();
				}
				return parser.parse(esResponse.get(QueryCachingConstants.CACHE_RESULT).getAsString()).getAsJsonObject();
			} else {
				return getNeo4jDatasource(requestPayload);
			}

		} catch (Exception e) {
			log.error("Error in capturing Elasticsearch response", e);
			try {
				return getNeo4jDatasource(requestPayload);
			} catch (GraphDBException graphDBEx) {
				log.error("Exception in neo4j query execution", graphDBEx);
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
		BufferedReader reader = null;
		InputStream in = null;
		try {
			in = getClass().getClassLoader().getResourceAsStream(fileName);
			reader = new BufferedReader(new InputStreamReader(in));
			return org.apache.commons.io.IOUtils.toString(reader);
		} catch (Exception e) {
			log.error("Error in reading file!" + e);
		} finally {
			try {
				in.close();
				reader.close();
			} catch (NullPointerException | IOException e) {
				log.error("Error closing IOStream" + e);
			}
		}
		return null;
	}
}
