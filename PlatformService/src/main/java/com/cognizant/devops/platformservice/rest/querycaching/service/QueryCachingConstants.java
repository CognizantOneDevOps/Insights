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

public interface QueryCachingConstants {

	String ES_HOST = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint();
	String ES_CACHE_INDEX = ApplicationConfigProvider.getInstance().getQueryCache().getEsCacheIndex();
	String DEFAULT_ES_CACHE_INDEX = "neo4j-cached-results/querycacheresult";
	String METADATA = "metadata";
	String STATEMENT = "statement";
	String STATEMENTS = "statements";
	String TEST_DATABASE = "testDB";
	String RESULT_CACHE = "resultCache";
	String CACHING_TYPE = "cachingType";
	String START_TIME = "startTime";
	String END_TIME = "endTime";
	String CACHING_VALUE = "cachingValue";
	String LOAD_CACHETIME_QUERY_FROM_RESOURCES = "/querycachingesquery/esQueryWithTime.json";
	String LOAD_CACHEVARIANCE_QUERY_FROM_RESOURCES = "/querycachingesquery/esQueryWithVariance.json";
	String QUERY_HASH = "queryHash";
	String QUERY_CACHE = "__queryCache__";
	String FIXED_TIME = "Fixed Time";
	String CACHED_STARTTIME = "__cachedStartTime__";
	String CACHED_ENDTIME = "__cachedEndTime__";
	String START_TIME_IN_STATEMENT_REPLACE = "?START_TIME?";
	String END_TIME_IN_STATEMENT_REPLACE = "?END_TIME?";
	String START_TIME_RANGE = "startTimeRange";
	String END_TIME_RANGE = "endTimeRange";
	String START_TIME_REPLACE = "__startTime__";
	String END_TIME_REPLACE = "__endTime__";
	String CURRENT_TIME = "__currentTime__";
	String CACHED_PREVIOUS_TIME = "__cachePreviousTime__";
	String HAS_EXPIRED = "hasExpired";
	String CREATION_TIME = "creationTime";
	String QUERY_EXECUTION_TIME = "queryExecutionTime";
	String NEO4J_RESULT_CREATION_TIME = "neo4jResultCreationTime";
	int ZEROTH_INDEX = 0;
	String CACHE_RESULT = "cacheResult";
	String NEW_STATEMENT = "newStatement";
	String START_TIME_IN_MS = "startTimeInMs";
	String END_TIME_IN_MS = "endTimeInMs";
	String CYPHER_QUERY = "cypherQuery";
}
