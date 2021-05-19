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

package com.cognizant.devops.platformservice.neo4jpluginlogs.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cognizant.devops.platformdal.grafanadatabase.GrafanaDatabaseDAL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service("neo4jPluginLogsService")
public class Neo4jPluginLogsServiceImpl implements Neo4jPluginLogsService{
	
	GrafanaDatabaseDAL neo4jPluginLogsDAL = new GrafanaDatabaseDAL();
	
	@Override
	public JsonObject getDashboardDetails() {
		List<Object[]> records =  neo4jPluginLogsDAL.fetchDashboardDetails();
		JsonArray recordData = new JsonArray();
		records.stream().forEach(record -> {
			JsonObject rec = new JsonObject();
			rec.addProperty("id",(int) record[0]);
			rec.addProperty("dashboard",(String) record[1]);
			recordData.add(rec);
		});
		JsonObject responseJson = new JsonObject();
		responseJson.add("records", recordData);
		return responseJson;
	}

	@Override
	public JsonObject fetchOrgDetails() {
		List<Object[]> records =  neo4jPluginLogsDAL.fetchOrgDetails();
		JsonArray recordData = new JsonArray();
		records.stream().forEach(record -> {
			JsonObject rec = new JsonObject();
			rec.addProperty("id",(int) record[0]);
			rec.addProperty("orgName",(String) record[1]);
			recordData.add(rec);
		});
		JsonObject responseJson = new JsonObject();
		responseJson.add("records", recordData);
		return responseJson;
	}
	
}
