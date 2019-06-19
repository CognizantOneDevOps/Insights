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
package com.cognizant.devops.platforminsights.core.minmax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platforminsights.configs.ConfigConstants;
import com.cognizant.devops.platforminsights.core.BaseActionImpl;
import com.cognizant.devops.platforminsights.core.function.Neo4jDBImp;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.cognizant.devops.platforminsights.datamodel.Neo4jKPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MinMaxActionImpl extends BaseActionImpl {

	private static final Logger log = LogManager.getLogger(MinMaxActionImpl.class);
	public MinMaxActionImpl(KPIDefinition kpiDefinition) {
		super(kpiDefinition);
	}

	public MinMaxActionImpl(Neo4jKPIDefinition neo4jKpiDefinition) {
		super(neo4jKpiDefinition);
	}

	@Override
	protected Map<String, Object> execute()  throws InsightsJobFailedException {
		Map<String, Object> resultMap = new HashMap<>();
		ElasticSearchDBHandler esDBHandler = new ElasticSearchDBHandler();
		try {
			log.debug("Entering Minimum and Maximum block");
			String esQuery = getEsQueryWithDates(kpiDefinition.getSchedule(),kpiDefinition.getEsquery());
			log.debug("MINMAX query - "+esQuery);
			JsonObject jsonObj = esDBHandler.queryES(ConfigConstants.SPARK_ES_HOST+":"+ConfigConstants.SPARK_ES_PORT+"/"+kpiDefinition.getEsResource()+"/_search?size=0&filter_path=aggregations", esQuery);
			JsonObject aggObj = jsonObj.get("aggregations").getAsJsonObject().get("minMaxOutput").getAsJsonObject();
			log.debug("  aggObj  " + aggObj);
			JsonElement jsonElement = aggObj.get("value");
			if ( jsonElement.isJsonNull()){
				resultMap = getResultMap(0L,null);
			}
			else{
				resultMap = getResultMap(jsonElement.getAsLong(),null);
			}
			saveResult(resultMap);
		}catch (Exception e) {
			log.error("Exception while running Minimum and Maximum operation -- "+ kpiDefinition.getKpiID()+": "+kpiDefinition.getName(), e);
			throw new InsightsJobFailedException("Exception while running Minimum and Maximum operation -- "+ kpiDefinition.getKpiID()+": "+kpiDefinition.getName(), e);
		}
		return resultMap;
	}

	@Override
	protected void executeNeo4jGraphQuery() {
		//if (kpiDefinition.getDbType().equalsIgnoreCase("neo4j")) {
		try {
			Neo4jDBImp graphDb = new Neo4jDBImp(neo4jKpiDefinition);
			List<Map<String, Object>> graphResposne = graphDb.getNeo4jResult();
			log.debug(" graphResposne  " + graphResposne);
			saveResultInNeo4j(graphResposne);

		} catch (Exception e) {
			log.error("Sum calculation job failed for kpiID - " + kpiDefinition.getKpiID(), e);
		}
		//}
	}
}
