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
package com.cognizant.devops.platformservice.neo4jpluginlogs.controller;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformservice.neo4jpluginlogs.service.Neo4jPluginLogsService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/externalApi")
public class Neo4jPluginLogsController {

	private static final String CHART_TYPE = "chartType";
	private static final String PANEL_NAME = "panelName";
	private static final String PANELS = "panels";
	private static final String EVENT_NAME = "eventName";
	private static final String ORG_NAME = "orgName";
	Cache<Integer, String> dashboardCache;
	Cache<Integer, String> orgCache;
	Cache<String, String> dashboardEventCache;

	public static final Long GRAFANA_DASHBOARD_CACHE_HEAP_SIZE_BYTES=1000000l;
	static Logger log = LogManager.getLogger(Neo4jPluginLogsController.class);

	@Autowired
	Neo4jPluginLogsService neo4jPluginLogsService;

	{

		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("grafana",
						CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
								ResourcePoolsBuilder.newResourcePoolsBuilder().heap(30, EntryUnit.ENTRIES).offheap(10,
										MemoryUnit.MB)))
				.build();
		cacheManager.init();
		dashboardCache = cacheManager.createCache("dashboard",
				CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Integer.class, String.class,
						ResourcePoolsBuilder.heap(GRAFANA_DASHBOARD_CACHE_HEAP_SIZE_BYTES))
				.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1L))));

		orgCache = cacheManager.createCache("org",
				CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Integer.class, String.class,
						ResourcePoolsBuilder.heap(GRAFANA_DASHBOARD_CACHE_HEAP_SIZE_BYTES))
				.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1L))));

		dashboardEventCache = cacheManager.createCache("dashboardEvent",
				CacheConfigurationBuilder
				.newCacheConfigurationBuilder(String.class, String.class,
						ResourcePoolsBuilder.heap(GRAFANA_DASHBOARD_CACHE_HEAP_SIZE_BYTES))
				.withExpiry(ExpiryPolicyBuilder.noExpiration()));


	}

	@PostMapping(value = "/Neo4jPluginLog/logDashboardInfo", produces = MediaType.APPLICATION_JSON_VALUE)
	public void logDashboardInfo(@RequestBody JsonObject dashboardDetails) {
		String panelId = dashboardDetails.get("panelId").getAsString();
		int dashboardId = dashboardDetails.get("dashboardId").getAsInt();
		if(dashboardDetails.get("orgId") != null) {
			int orgId = dashboardDetails.get("orgId").getAsInt();
			if (!dashboardDetails.has(ORG_NAME)) {
				if(dashboardCache.get(dashboardId) == null ) {
					JsonObject dashboardJson = neo4jPluginLogsService.getDashboardDetails();
					JsonArray recordArray = dashboardJson.get("records").getAsJsonArray();
					recordArray.forEach(record -> dashboardCache.put(record.getAsJsonObject().get("id").getAsInt(),record.getAsJsonObject().get("dashboard").getAsString()));
					if(orgCache.get(orgId) == null ) {
						JsonObject orgJson = neo4jPluginLogsService.fetchOrgDetails();
						JsonArray orgArray = orgJson.get("records").getAsJsonArray();
						orgArray.forEach(record -> orgCache.put(record.getAsJsonObject().get("id").getAsInt(),record.getAsJsonObject().get(ORG_NAME).getAsString()));
						dashboardDetails.addProperty(ORG_NAME, orgCache.get(orgId));
					}else {
						dashboardDetails.addProperty(ORG_NAME, orgCache.get(orgId));
					}
					processDashboardJsonForLog(dashboardDetails, panelId, dashboardCache.get(dashboardId));
				}else {
					String orgName = orgCache.get(orgId);
					dashboardDetails.addProperty(ORG_NAME, orgName);
					String dashboardJson = dashboardCache.get(dashboardId);
					processDashboardJsonForLog(dashboardDetails, panelId, dashboardJson);
				}
				logDashboardView(dashboardDetails, dashboardId);
			}else {
				log.info(dashboardDetails);
				logDashboardView(dashboardDetails, dashboardId);
			}
		}
	}


	private synchronized void logDashboardView(JsonObject dashboardDetails, int dashboardId) {
		String cacheUserId = dashboardDetails.get("userId").getAsString();
		String cacheDashboardId = "-1";
		if(dashboardEventCache.get(cacheUserId) != null) {
			cacheDashboardId = dashboardEventCache.get(cacheUserId);
		}
		if(!cacheDashboardId.equals(String.valueOf(dashboardId))) {
			JsonObject jsonObject = new JsonParser().parse(dashboardDetails.toString()).getAsJsonObject();
			jsonObject.addProperty(EVENT_NAME, "dashboard-view");
			jsonObject.addProperty(PANEL_NAME, "");
			jsonObject.addProperty(CHART_TYPE, "");
			jsonObject.addProperty("query", "");
			dashboardEventCache.put(cacheUserId, String.valueOf(dashboardId));
			log.info(jsonObject);
		}
	}

	private void processDashboardJsonForLog(JsonObject dashboardDetails, String panelId, String dashboardJson) {
		String eventType = dashboardDetails.get(EVENT_NAME).getAsString();
		JsonObject dashboardJsonObject = new JsonParser().parse(dashboardJson).getAsJsonObject();
		if(dashboardJsonObject.get(PANELS).getAsJsonArray().size() > 0) {
			dashboardDetails.addProperty("uid", dashboardJsonObject.get("uid").getAsString());
			dashboardDetails.addProperty("dashboardName", dashboardJsonObject.get("title").getAsString());
			dashboardDetails.addProperty("panelCount", dashboardJsonObject.get(PANELS).getAsJsonArray().size());
			if("data-request".equals(eventType)) {
				dashboardJsonObject.get(PANELS).getAsJsonArray().forEach(panel -> {
					if(panel.getAsJsonObject().get("id").getAsLong() == Long.parseLong(panelId)){
						dashboardDetails.addProperty(PANEL_NAME, panel.getAsJsonObject().get("title").getAsString());
						dashboardDetails.addProperty(CHART_TYPE, panel.getAsJsonObject().get("type").getAsString());
					}
				});
			}
			dashboardDetails.addProperty(EVENT_NAME, "data-request");
			log.info(dashboardDetails);
		}
	}
}
