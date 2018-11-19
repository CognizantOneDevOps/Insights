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
package com.cognizant.devops.platforminsights.core.count;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platforminsights.configs.ConfigConstants;
import com.cognizant.devops.platforminsights.core.BaseActionImpl;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CountActionImpl extends BaseActionImpl {

	private static final Logger log = LogManager.getLogger(CountActionImpl.class);
	public CountActionImpl(KPIDefinition kpiDefinition) {
		super(kpiDefinition);
	}

	@Override
	protected Map<String, Object> execute() {
		Map<String, Object> resultMap = new HashMap<>();
		ElasticSearchDBHandler esDBHandler = new ElasticSearchDBHandler();
		try {
			String esQuery = getEsQueryWithDates(kpiDefinition.getSchedule(),kpiDefinition.getEsquery());
			log.debug("Counting KPIs");
			if(kpiDefinition.isGroupBy()) {
				log.debug("GroupBy found True. Entering GroupBy method + Query -"+esQuery);
				JsonObject jsonObj = esDBHandler.queryES(ConfigConstants.SPARK_ES_HOST+":"+ConfigConstants.SPARK_ES_PORT+"/"+kpiDefinition.getEsResource()+"/_search?size=0&filter_path=aggregations", esQuery);
				String jsonObjStr = jsonObj.toString();
				ObjectMapper mapper = new ObjectMapper();
	              mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
	              ESResultMapper eSResultMapper = mapper.readValue(jsonObjStr, ESResultMapper.class);				
	              ArrayList<Map<String, Object>> AggregatedArrayMap = eSResultMapper.getAggregations().getTerms().getBuckets();
				
				int bucketLen = AggregatedArrayMap.size();
				if (bucketLen == 0){
					log.debug("Bucket found to be NULL");					
					resultMap = getResultMap(0L, "No records were found");
					saveResult(resultMap);
					return resultMap;
				}
				List<Map<String, Object>> resultList = new ArrayList<>();
				for(Map<String, Object> element : AggregatedArrayMap){
					log.debug("Counting KPI result");
					Object [] keys = element.keySet().toArray();
					
					String key = (String)keys[0];
					String groupByValue = element.get(key).toString();
					key = (String)keys[1];
					Long totalDoc = ((Integer)element.get(key)).longValue();
										
					resultMap = getResultMap(totalDoc,groupByValue);
					resultList.add(resultMap);
				}
				saveResult(resultList);
			} else {
				log.debug("GroupBy found False. Counting KPI result -  Query - "+esQuery);
				JsonObject jsonObj = esDBHandler.queryES(ConfigConstants.SPARK_ES_HOST+":"+ConfigConstants.SPARK_ES_PORT+"/"+kpiDefinition.getEsResource()+"/_count", esQuery);
				JsonElement jsonElement = jsonObj.get("count");
				resultMap = getResultMap(jsonElement.getAsLong(),null);
				saveResult(resultMap);
			}
		} catch (Exception e) {
			log.error("Exception while running count operation", e);
		}
		return resultMap;
	}
}
