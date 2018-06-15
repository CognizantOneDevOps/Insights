package com.cognizant.devops.platformservice.rest.querycaching.service;

public interface QueryCachingConstants {

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
	int ZEROTH_INDEX = 0;
	String CACHE_RESULT = "cacheResult";
	String NEW_STATEMENT = "newStatement";

}
