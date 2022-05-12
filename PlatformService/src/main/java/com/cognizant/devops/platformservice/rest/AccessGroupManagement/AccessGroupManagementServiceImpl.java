/*********************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
 *******************************************************************************/
package com.cognizant.devops.platformservice.rest.AccessGroupManagement;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.dal.vault.VaultHandler;
import com.cognizant.devops.platformdal.grafanadatabase.GrafanaDatabaseDAL;
import com.cognizant.devops.platformservice.rest.es.models.DashboardModel;
import com.cognizant.devops.platformservice.rest.es.models.DashboardResponse;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


@Service("accessGrpMgmtServiceImpl")
public class AccessGroupManagementServiceImpl {
	
	private static final Logger log = LogManager.getLogger(AccessGroupManagementServiceImpl.class);
	@Autowired
	private HttpServletRequest httpRequest;
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	VaultHandler vaultHandler = new VaultHandler();
	GrafanaDatabaseDAL grafanaDBDAL = new GrafanaDatabaseDAL();
	
	
	
	public DashboardResponse loadGrafanaDashboardData() {
		DashboardResponse dashboardResponse = new DashboardResponse();
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			String grafanaResponse = grafanaHandler.grafanaGet("/api/search", headers);
			JsonElement response = JsonUtils.parseString(grafanaResponse);
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
				model.setId(dashboardData.get(PlatformServiceConstants.TITLE).getAsString());
				model.setTitle(dashboardData.get(PlatformServiceConstants.TITLE).getAsString());
				if (dashboardData.has("type") && "dash-db".equals(dashboardData.get("type").getAsString())) {
						if (grafanaVersion.contains("5.")) {
							model.setUrl(grafanaIframeUrl + grafanaDomainUrl + dashboardData.get("url").getAsString());
						} else {
							model.setUrl(grafanaIframeUrl + grafanaUrl + dashboardData.get("uri").getAsString());
						}
						dashboardResponse.addDashboard(model);
					}
			    }
		} catch (Exception e) {
			log.error(e);
		}
		return dashboardResponse;
	}
	
	public String grafanaUrl(String baseUrl) {
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

	public List<JsonObject> getDashboardByOrg(int orgid) {
		List<JsonObject> returnResponse = new ArrayList<>();
		List<Object[]> records =  grafanaDBDAL.fetchDashboardDetailsByOrgId(orgid);
		records.stream().forEach(record -> {
			JsonObject rec = new JsonObject();
			rec.addProperty("uid",(String) record[0]);
			rec.addProperty(PlatformServiceConstants.TITLE,(String) record[1]);
			rec.addProperty("orgId", orgid);
			returnResponse.add(rec);
		});
		return returnResponse;
	}

	public JsonObject getDashboardByUid(String uuid,int orgid) {
		JsonObject returnResponse = new JsonObject() ;
		JsonObject dashboardJson= new JsonObject();
		List<Object[]> records =  grafanaDBDAL.fetchDashboardDetailsByUUId(uuid,orgid);
		for (Object[] objects : records) {
			dashboardJson= JsonUtils.parseStringAsJsonObject(String.valueOf(objects[1]));
		}
		returnResponse.add("dashboard", dashboardJson);
		return returnResponse;
	}
}
