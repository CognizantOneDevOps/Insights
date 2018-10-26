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
package com.cognizant.devops.platformservice.config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.GrafanaData;
import com.cognizant.devops.platformcommons.config.GraphData;
import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.cognizant.devops.platformdal.tools.layout.ToolsLayout;
import com.cognizant.devops.platformdal.tools.layout.ToolsLayoutDAL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

public final class InsightsConfiguration {
	
	//private static Logger log = Logger.getLogger(InsightsConfiguration.class);
	private static org.apache.logging.log4j.Logger log = LogManager.getLogger(InsightsConfiguration.class);
	private static final String NEO4J_DS = "Neo4j_DS";
	private static final String ElaticSearch_DS = "ElaticSearch_DS";

	private InsightsConfiguration() {

	}

	/**
	 * Update following: 1. Default layout JSON's 2. Default Datasources in Grafana
	 * 3. Default Dashboards in Grafana
	 */
	public static void doInsightsConfiguration() {
		log.info("Coming inside do insights");
		// updateLayouts();
		updateDatasources();
		//updateDashboards();
	}

	private static void updateLayouts() {
		ToolsLayoutDAL dal = new ToolsLayoutDAL();
		List<ToolsLayout> allToolLayouts = dal.getAllToolLayouts();
		if (!allToolLayouts.isEmpty()) {
			log.info("Tools Layout already updated. Skipping the layout update.");
			return;
		}
		List<String> availabelLayouts = getFilesFromDir("/layouts");
		if (availabelLayouts.isEmpty()) {
			log.error("No default tools layout found. Please correct.");
			return;
		}
		for (String layout : availabelLayouts) {
			JsonObject layoutJson = loadJsonFile("/layouts/" + layout);
			String toolCategory = layoutJson.get("category").getAsString();
			String toolName = layoutJson.get("toolName").getAsString();
			boolean isLayoutSaved = dal.saveToolLayout(toolName, toolCategory, layoutJson);
			if (!isLayoutSaved) {
				log.error("Layout is not updated for : " + layout);
			}
		}
	}

	private static JsonObject loadJsonFile(String path) {
		/*
		 * InputStream in = InsightsConfiguration.class.getResourceAsStream(path);
		 * BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		 * JsonObject json = new JsonParser().parse(reader).getAsJsonObject(); try {
		 * reader.close(); } catch (IOException e) { log.error(
		 * "Unable to close the reader.", e); }
		 */
		JsonObject json = null;
		try (InputStream in = InsightsConfiguration.class.getResourceAsStream(path);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			json = new JsonParser().parse(reader).getAsJsonObject();
		} catch (IOException e) {
			log.error("unable to read json" + e);
		}
		return json;
	}

	private static List<String> getFilesFromDir(String dirPath) {

		List<String> availabelLayouts = new ArrayList<String>();

		String resource;
		try (InputStream in = InsightsConfiguration.class.getResourceAsStream(dirPath);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in), 1024)) {
			if (in == null) {
				return availabelLayouts;
			}
			while ((resource = reader.readLine()) != null) {
				if (resource.endsWith(".json")) {
					availabelLayouts.add(resource);
				}
			}
			// reader.close();
		} catch (IOException e) {
			log.error("Unable to read resources : layouts.", e);
		} catch (NullPointerException e) {
			e.printStackTrace();
			log.error("Unable to read resources : layouts.", e);
		}
		return availabelLayouts;
	}

	private static void updateDatasources() {
		String grafanaBaseUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", buildAuthenticationHeader());
		String apiUrl = grafanaBaseUrl + "/api/datasources";
		ClientResponse response = RestHandler.doGet(apiUrl, null, headers);
		JsonArray datasources = new JsonParser().parse(response.getEntity(String.class)).getAsJsonArray();
		if (datasources.size() > 0) {
			log.info("Datasources are already configured.");
			return;
		}
		RestHandler.doPost(apiUrl, buildElasticSearchDataSourceRequest(), headers);
		RestHandler.doPost(apiUrl, buildNeo4jDataSourceRequest(), headers);
	}

	private static JsonObject buildElasticSearchDataSourceRequest() {
		JsonObject request = new JsonObject();
		request.addProperty("name", ElaticSearch_DS);
		request.addProperty("label", ElaticSearch_DS);
		request.addProperty("type", "elasticsearch");
		request.addProperty("access", "proxy");
		request.addProperty("url",
				ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint());
		request.addProperty("database", "neo4j-index-node");
		JsonObject jsonData = new JsonObject();
		jsonData.addProperty("esVersion", 2);
		jsonData.addProperty("timeField", "inSightsTimeX");
		request.add("jsonData", jsonData);
		return request;
	}

	private static JsonObject buildNeo4jDataSourceRequest() {
		GraphData graph = ApplicationConfigProvider.getInstance().getGraph();
		String authToken = graph.getAuthToken();
		String decodedAuthToken = new String(Base64.getDecoder().decode(authToken));
		String[] parts = decodedAuthToken.split(":");
		JsonObject request = new JsonObject();
		request.addProperty("name", NEO4J_DS);
		request.addProperty("label", NEO4J_DS);
		request.addProperty("type", "neo4j");
		request.addProperty("access", "proxy");
		request.addProperty("url", graph.getEndpoint() + "/db/data/transaction/commit?includeStats=true");
		request.addProperty("basicAuth", true);
		request.addProperty("basicAuthUser", parts[0]);
		request.addProperty("basicAuthPassword", parts[1]);
		return request;
	}

	private static void updateDashboards() {
		List<String> availableDashboards = getFilesFromDir("/dashboards");
		if (availableDashboards.isEmpty()) {
			log.info("No Default dashboards available.");
			return;
		}
		String grafanaBaseUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", buildAuthenticationHeader());
		switchOrg(grafanaBaseUrl, headers);
		JsonArray dashboardArray = loadDashboards(grafanaBaseUrl, headers);
		if (dashboardArray.size() > 0) {
			log.info("Dashboard are already configured.");
			return;
		}
		String apiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint()
				+ "/api/dashboards/import";
		JsonObject requestJson = null;
		JsonArray inputs = null;
		JsonObject jsonResponse = null;
		for (String dashboard : availableDashboards) {
			JsonObject dashboardJson = loadJsonFile("/dashboards/" + dashboard);
			requestJson = getNewJsonObject();
			requestJson.add("dashboard", dashboardJson);
			requestJson.addProperty("overwrite", true);
			inputs = getNewJsonArray();
			inputs.add(buildDSInput(NEO4J_DS, "neo4j"));
			inputs.add(buildDSInput(ElaticSearch_DS, "elasticsearch"));
			requestJson.add("inputs", inputs);
			ClientResponse response = RestHandler.doPost(apiUrl, requestJson, headers);
			jsonResponse = getNewJsonParser().parse(response.getEntity(String.class)).getAsJsonObject();
		}
	}

	private static JsonObject getNewJsonObject() {
		return new JsonObject();
	}

	private static JsonArray getNewJsonArray() {
		return new JsonArray();
	}

	private static JsonParser getNewJsonParser() {
		return new JsonParser();
	}

	private static JsonObject buildDSInput(String name, String pluginId) {
		JsonObject esInput = new JsonObject();
		esInput.addProperty("name", name);
		esInput.addProperty("pluginId", pluginId);
		esInput.addProperty("type", "datasource");
		esInput.addProperty("value", name);
		return esInput;
	}

	private static JsonArray loadDashboards(String grafanaBaseUrl, Map<String, String> headers) {
		String searchDashboardApiUrl = grafanaBaseUrl + "/api/search";
		ClientResponse response = RestHandler.doGet(searchDashboardApiUrl, null, headers);
		JsonArray dashboardArray = new JsonParser().parse(response.getEntity(String.class)).getAsJsonArray();
		return dashboardArray;
	}

	private static void switchOrg(String grafanaBaseUrl, Map<String, String> headers) {
		String apiUrl = grafanaBaseUrl + "/api/user/using/1";
		ClientResponse response = RestHandler.doPost(apiUrl, null, headers);
		log.info("Switch org result: " + response.getEntity(String.class));
	}

	private static String buildAuthenticationHeader() {
		GrafanaData grafana = ApplicationConfigProvider.getInstance().getGrafana();
		return "Basic " + Base64.getEncoder()
				.encodeToString((grafana.getAdminUserName() + ":" + grafana.getAdminUserPassword()).getBytes());
	}
}
