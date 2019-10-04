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
package com.cognizant.devops.platforminsights.core;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platforminsights.dal.Neo4jDBImpl;
import com.cognizant.devops.platforminsights.datamodel.InferenceConfigDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public abstract class BaseActionImpl {

	private static Logger log = LogManager.getLogger(BaseActionImpl.class);

	protected InferenceConfigDefinition kpiDefinition;
	Neo4jDBHandler dbHandler = new Neo4jDBHandler();
	Gson gson = new Gson();
	JsonParser jsonParser = new JsonParser();

	public BaseActionImpl() {

	}

	public BaseActionImpl(InferenceConfigDefinition kpiDefinition) {
		this.kpiDefinition = kpiDefinition;
	}


	protected abstract void execute() throws InsightsJobFailedException;

	protected String getEsQueryWithDates(JobSchedule schedule, String esQuery) {

		Long fromDate = InsightsUtils.getDataFromTime(schedule.name());
		esQuery = esQuery.replace("__dataFromTime__", fromDate.toString());
		Long toDate = InsightsUtils.getDataToTime(schedule.name());
		esQuery = esQuery.replace("__dataToTime__", toDate.toString());
		return esQuery;
	}

	protected void executeNeo4jGraphQuery() {
		try {
			Neo4jDBImpl graphDb = new Neo4jDBImpl(kpiDefinition);
			List<Map<String, Object>> graphResposne = graphDb.getResult();
			graphDb.saveResult(graphResposne);

		} catch (Exception e) {
			log.error("Sum calculation job failed for kpiID - " + kpiDefinition.getKpiID(), e);
		}
	}

	protected Map<String, Object> executeESQuery() {
		return null;

	}




}
