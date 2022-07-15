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

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public final class QueryCachingConstants {

	public static final String ES_HOST = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint();
	public static final String ES_CACHE_INDEX = ApplicationConfigProvider.getInstance().getQueryCache().getEsCacheIndex();
	public static final String DEFAULT_ES_CACHE_INDEX = "neo4j-cached-results";
	public static final String METADATA = "metadata";
	public static final String STATEMENT = "statement";
	public static final String STATEMENTS = "statements";
	public static final String TEST_DATABASE = "testDB";
	public static final String RESULT_CACHE = "resultCache";
	public static final String CACHING_TYPE = "cachingType";
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String CACHING_VALUE = "cachingValue";
	public static final String LOAD_CACHETIME_QUERY_FROM_RESOURCES = "/querycachingesquery/esQueryWithTime.json";
	public static final String LOAD_CACHEVARIANCE_QUERY_FROM_RESOURCES = "/querycachingesquery/esQueryWithVariance.json";
	public static final String QUERY_HASH = "queryHash";
	public static final String QUERY_CACHE = "__queryCache__";
	public static final String FIXED_TIME = "Fixed Time";
	public static final String CACHED_STARTTIME = "__cachedStartTime__";
	public static final String CACHED_ENDTIME = "__cachedEndTime__";
	public static final String START_TIME_IN_STATEMENT_REPLACE = "?START_TIME?";
	public static final String END_TIME_IN_STATEMENT_REPLACE = "?END_TIME?";
	public static final String START_TIME_RANGE = "startTimeRange";
	public static final String END_TIME_RANGE = "endTimeRange";
	public static final String START_TIME_REPLACE = "__startTime__";
	public static final String END_TIME_REPLACE = "__endTime__";
	public static final String CURRENT_TIME = "__currentTime__";
	public static final String CACHED_PREVIOUS_TIME = "__cachePreviousTime__";
	public static final String HAS_EXPIRED = "hasExpired";
	public static final String CREATION_TIME = "creationTime";
	public static final String QUERY_EXECUTION_TIME = "queryExecutionTime";
	public static final String NEO4J_RESULT_CREATION_TIME = "neo4jResultCreationTime";
	public static final int ZEROTH_INDEX = 0;
	public static final String CACHE_RESULT = "cacheResult";
	public static final String NEW_STATEMENT = "newStatement";
	public static final String START_TIME_IN_MS = "startTimeInMs";
	public static final String END_TIME_IN_MS = "endTimeInMs";
	public static final String CYPHER_QUERY = "cypherQuery";
	public static final String ES_STATUS = "status";
	public static final String ES_HITS = "hits";
}
