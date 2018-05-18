package com.cognizant.devops.platformservice.rest.querycaching.service;

public interface QueryCachingConstants {

	String METADATA = "metadata";
	String STATEMENT = "statement";
	String STATEMENTS = "statements";
	String RESULT_CACHE = "resultCache";
	String CACHING_TYPE = "cachingType";
	String START_TIME = "startTime";
	String END_TIME = "endTime";
	String CACHE_TIME = "cacheTime";
	String CACHE_VARIANCE = "cacheVariance";
	String LOAD_CACHETIME_QUERY_FROM_RESOURCES = "/querycachingesquery/esQueryWithTime.json";
	String LOAD_CACHEVARIANCE_QUERY_FROM_RESOURCES = "/querycachingesquery/esQueryWithVariance.json";
	String QUERY_HASH = "queryHash";
	String QUERY_HASHING = "__queryCache__";
	String CACHED_STARTTIME = "__cachedStartTime__";
	String CACHED_ENDTIME = "__cachedEndTime__";
	String START_TIME_RANGE = "__startTime__";
	String END_TIME_RANGE = "__endTime__";
	String CURRENT_TIME = "__currentTime__";
	String CACHED_PREVIOUS_TIME = "__cachePreviousTime__";
	String HAS_EXPIRED = "hasExpired";
	String CREATION_TIME = "creationTime";

}
