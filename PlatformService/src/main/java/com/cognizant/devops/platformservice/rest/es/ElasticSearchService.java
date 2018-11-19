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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.cognizant.devops.platformservice.rest.es.models.DashboardModel;
import com.cognizant.devops.platformservice.rest.es.models.DashboardResponse;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

@RestController
@RequestMapping("/search")
public class ElasticSearchService {
	static Logger log = LogManager.getLogger(ElasticSearchService.class.getName());

	@Autowired
	private HttpServletRequest context;

	@RequestMapping(value = "/dashboards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String loadDashboardData() {
		DashboardResponse dashboardResponse = new DashboardResponse();
		try {
			String authHeader = context.getHeader("Authorization");
			String decodedAuthHeader = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), "UTF-8");
			String[] authTokens = decodedAuthHeader.split(":");
			JsonObject loginRequestParams = new JsonObject();
			loginRequestParams.addProperty("user", authTokens[0]);
			// req.addProperty("email", "");
			loginRequestParams.addProperty("password", authTokens[1]);
			String loginApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/login";
			ClientResponse grafanaLoginResponse = RestHandler.doPost(loginApiUrl, loginRequestParams, null);
			List<String> cookies = grafanaLoginResponse.getHeaders().get("Set-Cookie");
			StringBuffer cookie = new StringBuffer();
			for (String c : cookies) {
				cookie.append(c.split(";")[0]).append(";");
			}
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Cookie", cookie.toString());
			String dashboardApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint()
					+ "/api/search";
			ClientResponse grafanaResponse = RestHandler.doGet(dashboardApiUrl, null, headers);
			JsonElement response = new JsonParser().parse(grafanaResponse.getEntity(String.class));
			JsonArray dashboardsJsonArray = response.getAsJsonArray();
			String grafanaBaseUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaExternalEndPoint();
			if (grafanaBaseUrl == null) {
				grafanaBaseUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
			}
			String grafanaUrl = grafanaBaseUrl + "/dashboard/";
			String grafanaIframeUrl = grafanaBaseUrl + "/dashboard/script/iSight.js?url=";
			String grafanaDomainUrl = grafanaUrl(grafanaBaseUrl);
			String grafanaVersion = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaVersion();
			if (grafanaVersion == null) {
				grafanaVersion = "4.6.2";
			}
			for (JsonElement data : dashboardsJsonArray) {
				JsonObject dashboardData = data.getAsJsonObject();
				DashboardModel model = new DashboardModel();
				model.setId(dashboardData.get("title").getAsString());
				model.setTitle(dashboardData.get("title").getAsString());
				if (dashboardData.has("type")) {
					if ("dash-db".equals(dashboardData.get("type").getAsString())) {
						if (grafanaVersion.contains("5.")) {
							model.setUrl(grafanaIframeUrl + grafanaDomainUrl + dashboardData.get("url").getAsString());
						} else {
							model.setUrl(grafanaIframeUrl + grafanaUrl + dashboardData.get("uri").getAsString());
						}
						dashboardResponse.addDashboard(model);
					}
				}
			}
			/*
			 * ElasticSearchDBHandler dbHandler = new ElasticSearchDBHandler();
			 * String url =
			 * ApplicationConfigProvider.getInstance().getEndpointData().
			 * getElasticSearchEndpoint()
			 * +"/.kibana/dashboard/_search?fields=_id,title"; JsonElement
			 * response = new JsonParser().parse(dbHandler.search(url));
			 * if(response.isJsonObject()){ JsonObject jsonObject =
			 * response.getAsJsonObject(); JsonArray jsonArray =
			 * jsonObject.get("hits").getAsJsonObject().get("hits").
			 * getAsJsonArray(); for(JsonElement entry : jsonArray){ JsonObject
			 * data = entry.getAsJsonObject(); DashboardModel model = new
			 * DashboardModel(); model.setId(data.get("_id").getAsString());
			 * model.setTitle(data.get("fields").getAsJsonObject().get("title").
			 * getAsJsonArray().get(0).getAsString());
			 * model.setUrl(ApplicationConfigProvider.getInstance().
			 * getEndpointData().getKibanaEndpoint()+"/app/kibana#/dashboard/"+
			 * model.getId()+"?embed=true");
			 * dashboardResponse.addDashboard(model); } }
			 */
		} catch (Exception e) {
			log.error(e);
		}
		return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(dashboardResponse);
	}

	@RequestMapping(value = "/data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String searchData(@RequestParam String query, @RequestParam(required = false, defaultValue = "0") int from,
			@RequestParam(required = false, defaultValue = "100") int size) {
		ElasticSearchDBHandler dbHandler = new ElasticSearchDBHandler();
		String url = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint()
				+ "/neo4j-index/_search?from=" + from + "&size=" + size + "&q=*" + query + "*";
		return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
				.toJson(new JsonParser().parse(dbHandler.search(url)));
	}

	@RequestMapping(value = "/copyKibana", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String copyKibanaIndex(@RequestParam String from, @RequestParam String to) {
		if (from == null || to == null || to.contains("localhost:9200")) {
			return "Invalid Params";
		}
		ElasticSearchDBHandler dbHandler = new ElasticSearchDBHandler();
		String dashboardFromUrl = from + "/.kibana/dashboard/_search?q=*&size=10000&from=0";
		String dashboardToUrl = to + "/.kibana/dashboard/";
		JsonObject dashboards = dbHandler.loadVisualization(dashboardFromUrl);
		copyKibanaType(dashboards, dashboardToUrl);
		// dbHandler.cloneVisualizations(dashboardToUrl, dashboards);

		String visualizationsFromUrl = from + "/.kibana/visualization/_search?q=*&size=10000&from=0";
		String visualizationsToUrl = to + "/.kibana/visualization/";
		JsonObject visualizations = dbHandler.loadVisualization(visualizationsFromUrl);
		copyKibanaType(visualizations, visualizationsToUrl);
		// dbHandler.cloneVisualizations(visualizationsToUrl, visualizations);
		return "Data Cloned";
	}

	private void copyKibanaType(JsonObject data, String baseUrl) {
		ElasticSearchDBHandler dbHandler = new ElasticSearchDBHandler();
		if (null != data.get("hits")) {
			JsonArray asJsonArray = data.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
			for (JsonElement vizEle : asJsonArray) {
				JsonObject viz = vizEle.getAsJsonObject();
				dbHandler.cloneVisualizations(baseUrl + viz.get("_id").getAsString(),
						viz.get("_source").getAsJsonObject());
			}
		}
	}

	private String grafanaUrl(String baseUrl) {
		String parsedUrl = null;
		try {
			URL uri = new URL(baseUrl);
			parsedUrl = uri.getProtocol() + "://" + uri.getHost();
			if (uri.getPort() > -1) {
				parsedUrl = parsedUrl + ":" + uri.getPort();
			}
		} catch (MalformedURLException e) {
			log.error("Error in Parsing Grafana URL", e);
		}
		return parsedUrl;
	}
}
