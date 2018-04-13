package com.cognizant.devops.platformservice.rest.querycaching.controller;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformservice.rest.querycaching.service.QueryCachingService;
import com.cognizant.devops.platformservice.rest.querycaching.service.QueryCachingServiceImpl;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/datasource")
public class QueryCachingController {

	private static Logger log = Logger.getLogger(QueryCachingServiceImpl.class);
	@Autowired
	QueryCachingService queryCachingService;

	@RequestMapping(value = "/neo4jds", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public  JsonObject getNeo4jDatasource(HttpServletRequest request )  {
		String payloadRequest = null;
		JsonObject results = new JsonObject();
		try {
			payloadRequest = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			results = queryCachingService.getCacheResults(payloadRequest);
		} catch (GraphDBException | IOException e) {
			log.error(e);
		}

		return results;
	}


}
