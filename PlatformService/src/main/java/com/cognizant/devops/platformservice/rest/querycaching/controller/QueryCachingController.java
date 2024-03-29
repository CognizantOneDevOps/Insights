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
package com.cognizant.devops.platformservice.rest.querycaching.controller;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformservice.rest.querycaching.service.QueryCachingService;
import com.cognizant.devops.platformservice.rest.querycaching.service.QueryCachingServiceImpl;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/externalApi")
public class QueryCachingController {

	private static Logger log = LogManager.getLogger(QueryCachingServiceImpl.class);
	@Autowired
	QueryCachingService queryCachingService;

	@PostMapping(value = "/datasource/neo4jds", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getNeo4jDatasource(HttpServletRequest request) {
		String payloadRequest = null;
		JsonObject results = new JsonObject();
		try {
			payloadRequest = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			results = queryCachingService.getCacheResults(payloadRequest);
	} catch (Exception e) {
			log.error(e);
		}

		return results;
	}

}
