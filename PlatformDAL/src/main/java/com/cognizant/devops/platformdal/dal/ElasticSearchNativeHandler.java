/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.dal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ElasticSearchNativeHandler {
	private static Logger log = LogManager.getLogger(ElasticSearchNativeHandler.class.getName());
	private static final String STATUS ="status";
	
	public List<JsonObject> getESResult(String esQuery, String indexName) {
		ElasticSearchDBHandler esDbHandler = new ElasticSearchDBHandler();
		List<JsonObject> recordlist = new ArrayList<>();
		try {
			String sourceESCacheUrl = ApplicationConfigProvider.getInstance().getEndpointData()
					.getElasticSearchEndpoint() + "/" + indexName;

			JsonObject esResponse = esDbHandler.queryES(sourceESCacheUrl + "/_search", esQuery);

			if (esResponse.has(STATUS) && esResponse.get(STATUS).getAsInt() == 404) {
				log.debug("Worlflow Detail ====  Elastic Serach data not retirved . Message is {} ", esResponse);
			} else {
				JsonArray esResponseArray = esResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
				for (JsonElement esResponseSource : esResponseArray) {
					JsonObject parsedJson = JsonUtils.parseStringAsJsonObject(esResponseSource.toString());
					JsonObject recordKPI = parsedJson.get("_source").getAsJsonObject();
					log.debug(" recordKPI {} ", recordKPI);
					recordlist.add(recordKPI);
				}
			}
			log.debug(" esResponse API response {} ", esResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return recordlist;
	}

	public List<JsonObject> saveESResult(String indexName, List<JsonObject> rows) {
		ElasticSearchDBHandler esDbHandler = new ElasticSearchDBHandler();
		List<JsonObject> recordlist = new ArrayList<>();
		try {
			String sourceESCacheUrl = ApplicationConfigProvider.getInstance().getEndpointData()
					.getElasticSearchEndpoint() + "/" + indexName + "/_bulk";
			StringBuilder bulkESJsons = new StringBuilder();
			for (JsonElement bulkItem : rows) {
				JsonObject bulkItemJson = new JsonObject();
				String uuid = UUID.randomUUID().toString();
				bulkItem.getAsJsonObject().addProperty("uuid", uuid);
				bulkItemJson.add("create", bulkItem.getAsJsonObject());
				bulkESJsons.append(bulkItemJson);
			}
			bulkESJsons = bulkESJsons.append("\\n");
			log.debug(" bulk Item Json {} ", bulkESJsons);
			JsonObject esResponse = esDbHandler.queryES(sourceESCacheUrl, bulkESJsons.toString());

			if (esResponse.has(STATUS) && esResponse.get(STATUS).getAsInt() == 404) {
				log.debug("Worlflow Detail ====  Elastic Serach data not retirved . Message is {} ", esResponse);
			} else {
				JsonArray esResponseArray = esResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
				for (JsonElement esResponseSource : esResponseArray) {
					JsonObject parsedJson = JsonUtils.parseStringAsJsonObject(esResponseSource.toString());
					JsonObject recordKPI = parsedJson.get("_source").getAsJsonObject();
					log.debug(" recordKPI {} ", recordKPI);
					recordlist.add(recordKPI);
				}
			}
			log.debug(" esResponse API response {} ", esResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return recordlist;
	}
}