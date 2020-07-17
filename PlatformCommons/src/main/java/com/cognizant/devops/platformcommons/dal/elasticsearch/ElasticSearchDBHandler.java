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
package com.cognizant.devops.platformcommons.dal.elasticsearch;

import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ElasticSearchDBHandler {


	/**
	 * @param url
	 * @return
	 * @throws InsightsCustomException
	 */
	public String search(String url) throws InsightsCustomException {
		return RestApiHandler.doGet(url, null);
	}

	/**
	 * @param sourceESUrl
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public JsonObject queryES(String sourceESUrl, String query) throws InsightsCustomException {

		JsonObject data = null;
		JsonObject requestJson = new JsonParser().parse(query).getAsJsonObject();
		String response = RestApiHandler.doPost(sourceESUrl, requestJson, null);
		data = new JsonParser().parse(response).getAsJsonObject();
		return data;
	}
}