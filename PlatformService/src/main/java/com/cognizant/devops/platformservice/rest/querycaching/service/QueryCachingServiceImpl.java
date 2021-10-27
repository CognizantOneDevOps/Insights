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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.querycaching.service.CustomExpiryPolicy;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.stream.Stream;

@Service("queryCachingService")
public class QueryCachingServiceImpl implements QueryCachingService {

	private static Logger log = LogManager.getLogger(QueryCachingServiceImpl.class);
	JsonParser parser = new JsonParser();
	public static final Long GRAFANA_DATASOURCE_CACHE_HEAP_SIZE_BYTES = 1000000l;
	@SuppressWarnings("rawtypes")
	Cache<String, EhcacheValue<JsonObject>> datasourceCache;
	
	{
		CustomExpiryPolicy<String, EhcacheValue<JsonObject>> expiryPolicy = new CustomExpiryPolicy<String, EhcacheValue<JsonObject>>();
		
		 Class<EhcacheValue<JsonObject>> myEhcacheValue = (Class<EhcacheValue<JsonObject>>)(Class<?>)EhcacheValue.class;

		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
		cacheManager.init();

		CacheConfiguration<String, EhcacheValue<JsonObject>> cacheConfiguration = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(String.class, myEhcacheValue,
						ResourcePoolsBuilder.heap(GRAFANA_DATASOURCE_CACHE_HEAP_SIZE_BYTES))
				.withExpiry(expiryPolicy)
				.build();

		datasourceCache = cacheManager.createCache("datasource", cacheConfiguration);

	}
	
	/**
     * Fetching Query Cache Results from Ehcache 
     *
     * If Save and Test option selected in Grafana it goes to neo4j DB
     * Else fetches data from Ehcache
     */

	@Override
	public JsonObject getCacheResults(String requestPayload) {

		JsonObject resultJson = null;
		JsonObject requestJson = parser.parse(requestPayload).getAsJsonObject();
		JsonObject dataJson = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
				.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject();

		String isTestDBConnectivity = dataJson.get(QueryCachingConstants.TEST_DATABASE).toString();

		try {
			if (isTestDBConnectivity.equals("true")) {
				log.debug("Query Caching Test Request For Data Source Connectivity Found. ");
				resultJson = getNeo4jDatasourceResults(requestPayload);
			} else {
				log.debug("Fetching Query Cache Results.");
				resultJson = getEhCachedResults(requestPayload);
			}
		} catch (InsightsCustomException e) {
			log.error("Error caught - ", e);
		}
		return resultJson;

	}
	
	/**
     * sending query as a request to DB and fetching result and return response
     *
     * return query result as a response
     * @throws InsightsCustomException
     */

	private JsonObject getNeo4jDatasourceResults(String queryjson) throws InsightsCustomException {

		GraphDBHandler graphDBHandler = new GraphDBHandler();
		GraphResponse response = null;
		JsonObject json = parser.parse(queryjson).getAsJsonObject();

		try {
			StringBuilder stringBuilder = new StringBuilder();
			Iterator<JsonElement> iterator = json.get(QueryCachingConstants.STATEMENTS).getAsJsonArray().iterator();
	
			JsonArray jsonArray=json.get(QueryCachingConstants.STATEMENTS).getAsJsonArray();
			
			Stream.of(jsonArray).forEach(obj-> stringBuilder
						.append(iterator.next().getAsJsonObject().get(QueryCachingConstants.STATEMENT).getAsString())
						.append(QueryCachingConstants.NEW_STATEMENT));
			
			String[] queriesArray = stringBuilder.toString().split(QueryCachingConstants.NEW_STATEMENT);
			response = graphDBHandler.executeCypherQueryMultiple(queriesArray);
		} catch (InsightsCustomException e) {
			log.error("Exception in neo4j query execution", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return response.getJson();
	}

	
	/**
     * Fetching Query Cache Results from Ehcache 
     *
     * Check key is available in Ehcache if not found fetches data from ne04j
     */
	private JsonObject getEhCachedResults(String requestPayload) {

		try {
			JsonObject requestJson = parser.parse(requestPayload).getAsJsonObject();
			
			JsonObject dataJson = requestJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
					.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject();

			boolean isCacheResult = dataJson.get(QueryCachingConstants.RESULT_CACHE)
					.getAsBoolean();

			if (isCacheResult) {

				String cachingType = dataJson.get(QueryCachingConstants.CACHING_TYPE).getAsString();

				int cachingValue = dataJson.get(QueryCachingConstants.CACHING_VALUE).getAsInt();

				Long startTime = dataJson.get(QueryCachingConstants.START_TIME).getAsLong();

				Long endTime = dataJson.get(QueryCachingConstants.END_TIME).getAsLong();

				log.debug("Caching Type {} Caching Value {}  startTimeStr {} endTimeStr {} ", cachingType,
						cachingValue, startTime, endTime);

				String cacheKey = getQueryHash(requestJson, cachingType, cachingValue, startTime, endTime);

				JsonObject ehResponse = null;

				if (datasourceCache.get(cacheKey) != null) {
					log.debug("Record found in cache");
					EhcacheValue<JsonObject> ehcacheValue = datasourceCache.get(cacheKey);
					ehResponse = (ehcacheValue != null ? ehcacheValue.getObject()
							: getNeo4jDatasourceResults(requestPayload));
					return ehResponse;
				} else {
					Long durationSeconds = endTime - startTime;
					log.debug("Fetching Results From Neo4j and storing in cache");
					return fetchRecordFromGraphDBAndsaveInEH(requestPayload, cacheKey, durationSeconds,cachingValue,cachingType);
				}

			} else {
				log.debug("Query Caching Option Is Not Selected. Fetching Results From Neo4j.");
				return getNeo4jDatasourceResults(requestPayload);
			}

		} catch (Exception e) {
			log.error("Query Caching - Error in capturing EH Cache response", e);
		}

		return null;
	}

	/**
     * Creates unique hash value to store in cache 
     *
     * It returns unique alphanumeric value which can be used as a cache key
     */
	private String getQueryHash(JsonObject requestJson, String cachingType, int cachingValue, Long startTime,
			Long endTime) {
		String queryHash;
		StringBuilder tempStatementsCombination = new StringBuilder();

        JsonArray jsonArray=requestJson.get(QueryCachingConstants.STATEMENTS).getAsJsonArray();
        
        Iterator<JsonElement> iterator = requestJson.get(QueryCachingConstants.STATEMENTS).getAsJsonArray().iterator();
		
		Stream.of(jsonArray).forEach(obj-> {
				
			String statement=iterator.next().getAsJsonObject().get(QueryCachingConstants.STATEMENT).getAsString();
			
			tempStatementsCombination.append(getStatementWithoutTime(statement, String.valueOf(startTime),
					String.valueOf(endTime)));			
		});		
		String statement = tempStatementsCombination.toString();
		String cacheDetailsHash = getCacheDetailsHash(cachingType, cachingValue);
		queryHash = DigestUtils.md5Hex(statement + cacheDetailsHash).toUpperCase();
		
		return queryHash;
	}

	private static String getCacheDetailsHash(String cacheType, int cachingValue) {
		String cacheDetails = "Cache Type: " + cacheType + " and Cache Value: " + cachingValue;
		return DigestUtils.md5Hex(cacheDetails).toUpperCase();
	}

	private static String getStatementWithoutTime(String statement, String startTime, String endTime) {
		return statement.replace(String.valueOf(startTime), QueryCachingConstants.START_TIME_IN_STATEMENT_REPLACE)
				.replace(String.valueOf(endTime), QueryCachingConstants.END_TIME_IN_STATEMENT_REPLACE);
	}

	/**
     * If cache key not found fetches data from Ne04j DB and stores in EHcache  
     *
     * Returns data as a response
     */
	private JsonObject fetchRecordFromGraphDBAndsaveInEH(String requestPayload, String cacheKey,
			Long durationSeconds, int cachingValue, String cachingType) throws InsightsCustomException {
		
		JsonObject graphResponse = getNeo4jDatasourceResults(requestPayload);
		
		long fixedSeconds = (cachingValue * (60 * 60));
		
		long varianceSeconds = (durationSeconds * cachingValue) / 100;

		 if (graphResponse !=null && cachingType.equalsIgnoreCase(QueryCachingConstants.FIXED_TIME)) {

			EhcacheValue<JsonObject> ehcacheValue = new EhcacheValue<>(graphResponse,
					Duration.of(fixedSeconds, ChronoUnit.SECONDS));
			datasourceCache.put(cacheKey, ehcacheValue);
		}else {
			EhcacheValue<JsonObject> ehcacheValue = new EhcacheValue<>(graphResponse,
					Duration.of(varianceSeconds, ChronoUnit.SECONDS));
			datasourceCache.put(cacheKey, ehcacheValue);
		}
			return graphResponse;
	}
}
