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
package com.cognizant.devops.platformservice.rest.es;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@RestController
@RequestMapping("/search")
public class ElasticSearchService {
	static Logger log = LogManager.getLogger(ElasticSearchService.class.getName());

	@Autowired
	private HttpServletRequest context;

	@RequestMapping(value = "/data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String searchData(@RequestParam String query, @RequestParam(required = false, defaultValue = "0") int from,
			@RequestParam(required = false, defaultValue = "100") int size) throws JsonSyntaxException, InsightsCustomException {
		ElasticSearchDBHandler dbHandler = new ElasticSearchDBHandler();
		String url = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint()
				+ "/neo4j-index/_search?from=" + from + "&size=" + size + "&q=*" + query + "*";
		return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
				.toJson(new JsonParser().parse(dbHandler.search(url)));
	}
}
