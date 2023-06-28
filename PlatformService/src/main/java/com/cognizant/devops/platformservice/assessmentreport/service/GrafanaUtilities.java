/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.assessmentreport.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class GrafanaUtilities {
	private static final Logger log = LogManager.getLogger(GrafanaUtilities.class);
	private static final String ORGANISATION = "organisation";
	private static final String NAME = "name";
	private static final String PDFTOKEN = "pdftoken";
	private static final String ADMIN = "Admin";
	private static final String ROLE = "role";
	private static final String ORG_NAME_PREFIX = "Report Org. ";
	private static final String BASICAUTH = "Basic ";
	
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
	
	@Autowired
	private HttpServletRequest httpRequest;

	public String createOrgAndSaveDashboardInGrafana(JsonObject requestDashboardObj, String userName) throws InsightsCustomException {
		String dashboardApiResponseObj = null;
		try {
			String orgName = ORG_NAME_PREFIX + userName;
			int orgId = -1;
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
					+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
			String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
			headers.put(AuthenticationUtils.AUTH_HEADER_KEY, BASICAUTH+ encodedString);
			// check if organization exists
			String orgResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.API_ORGS + "name/" + orgName, headers);
			if (!orgResponse.contains("id")) {
				// create org
				JsonObject request = new JsonObject();
				request.addProperty(NAME, orgName);
				orgResponse = grafanaHandler.grafanaPost(PlatformServiceConstants.API_ORGS, request, headers);
				JsonObject orgResponseJson = JsonUtils.parseStringAsJsonObject(orgResponse);
				orgId = orgResponseJson.get("orgId").getAsInt();
			} else {
				JsonObject orgResponseJson = JsonUtils.parseStringAsJsonObject(orgResponse);
				orgId = orgResponseJson.get("id").getAsInt();
			}
			// add user in organization
			addUser(orgId, userName, headers);
			// create API key
			generateGrafanaToken(orgId);
			// create datasource
			createDatasourceInGrafana(orgId);
			// create dashboard
			dashboardApiResponseObj = createDashboardInGrafana(orgId, requestDashboardObj);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return dashboardApiResponseObj;
	}
	/**
	 * Method to delete dashboard from Grafana
	 * 
	 * @param dashboardConfigJson
	 * @throws InsightsCustomException
	 */
	public void deleteDashboardFromGrafana(JsonObject dashboardConfigJson) throws InsightsCustomException {
		try {
			String dashboardDeleteUrl = PlatformServiceConstants.API_DASHBOARDS_UID +dashboardConfigJson.get("dashboard").getAsString();
			String response = grafanaHandler.grafanaDelete(dashboardDeleteUrl, getGrafanaHeaders(dashboardConfigJson.get(ORGANISATION).getAsInt()));
			log.debug(" dashboard delete response {} ", response);
		} catch (Exception e) {
			log.error("Error while deleting dashboard.", e);
			throw new InsightsCustomException(e.getMessage());
		}	
	}
	/**
	 * Method to create dashboard in grafana
	 * 
	 * @param orgId
	 * @param requestDashboardObj
	 * @return
	 * @throws InsightsCustomException
	 */
	private String createDashboardInGrafana(int orgId, JsonObject requestDashboardObj) throws InsightsCustomException {
		JsonObject dashboardApiResponseObj = null;
		try {
			String title = requestDashboardObj.get("dashboard").getAsJsonObject().get("title").getAsString();
			String dashboardApiResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.SEARCH_DASHBOARD_PATH + title , getGrafanaHeaders(orgId));
			if(dashboardApiResponse.equalsIgnoreCase("[]")) {
				dashboardApiResponse = grafanaHandler.grafanaPost(PlatformServiceConstants.API_DASHBOARD_PATH, requestDashboardObj, getGrafanaHeaders(orgId));
				log.debug(" dashboardApiResponse {} ", dashboardApiResponse);
				dashboardApiResponseObj = JsonUtils.parseStringAsJsonObject(dashboardApiResponse);
			} else {
				dashboardApiResponseObj = JsonUtils.parseStringAsJsonArray(dashboardApiResponse).get(0).getAsJsonObject();
			}
			dashboardApiResponseObj.addProperty("orgId", orgId);
		} catch (Exception e) {
			log.error("Error while creating dashboard.", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return dashboardApiResponseObj.toString();
	}
	/**
	 * Method to add user in organization
	 * 
	 * @param orgId
	 * @param userName
	 * @param headers
	 * @throws InsightsCustomException
	 */
	private void addUser(int orgId, String userName, Map<String, String> headers) throws InsightsCustomException {
		try {
			boolean userExists = false;
			String orgUserPath = PlatformServiceConstants.API_ORGS + orgId + PlatformServiceConstants.USERS;
			String getUserResponse = grafanaHandler.grafanaGet(orgUserPath, headers);
			JsonArray getUserResponseObj = JsonUtils.parseStringAsJsonArray(getUserResponse);
			for(JsonElement item: getUserResponseObj) {
				if(item.getAsJsonObject().get("login").getAsString().equalsIgnoreCase(userName)) {
					userExists = true;
				}
			}
			if(!userExists) {
				JsonObject requestUserData = new JsonObject();
				requestUserData.addProperty(PlatformServiceConstants.LOGIN_OR_EMAIL, userName);
				requestUserData.addProperty(ROLE, ADMIN);
				String addUserResponse = grafanaHandler.grafanaPost(orgUserPath, requestUserData, headers);
				log.debug(" addUserResponse {} ", addUserResponse);
			}
		} catch(Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}	
	}
	/**
	 * Method to create datasource in grafana
	 * 
	 * @param orgId
	 * @throws InsightsCustomException
	 */
	private void createDatasourceInGrafana(int orgId) throws InsightsCustomException {
		try {
			String getDatasourceResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.GET_DATASOURCE_PATH + "Neo4j Data Source", getGrafanaHeaders(orgId));
			JsonObject datasourceResponseObj = JsonUtils.parseStringAsJsonObject(getDatasourceResponse);
			if(datasourceResponseObj.get("id") == null) {
				byte[] decodedBytes = Base64.getDecoder().decode(ApplicationConfigProvider.getInstance().getGraph().getAuthToken());
				String decodedString = new String(decodedBytes);
				String[] userpass = decodedString.split(":"); 
				String datasourceUrl = ApplicationConfigProvider.getInstance().getGraph().getEndpoint() + "/db/data/transaction/commit?includeStats=true";
				JsonObject datasourceReqObj = new JsonObject();
				datasourceReqObj.addProperty("name", "Neo4j Data Source");
				datasourceReqObj.addProperty("type", "neo4j-datasource");
				datasourceReqObj.addProperty("url", datasourceUrl);
				datasourceReqObj.addProperty("access", "proxy");
				datasourceReqObj.addProperty("basicAuth", true);
				datasourceReqObj.addProperty("basicAuthUser", userpass[0]);
				JsonObject secureJsonData = new JsonObject();
				secureJsonData.addProperty("basicAuthPassword", userpass[1]);
				datasourceReqObj.add("secureJsonData", secureJsonData);
				String datasourceResponse = grafanaHandler.grafanaPost(PlatformServiceConstants.ADD_DATASOURCE_PATH, datasourceReqObj, getGrafanaHeaders(orgId));
				log.debug(" datasourceResponse {} ", datasourceResponse);
			}	
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}
	/** This method use to prepare Grafana header object 
	 * 
	 * @param orgId 
	 * @return map of headers 
	 */
	private Map<String, String> getGrafanaHeaders(int orgId) {
		GrafanaOrgToken grafanaOrgToken = grafanaDashboardConfigDAL.getTokenByOrgId(orgId);
		String token = "Bearer "+ AES256Cryptor.decrypt(grafanaOrgToken.getApiKey(), AssessmentReportAndWorkflowConstants.GRAFANA_PDF_TOKEN_SIGNING_KEY);
		Map<String, String> headers = new HashMap<>();
		headers.put(AuthenticationUtils.AUTH_HEADER_KEY, token);
		return headers;
	}

	/**
	 * Method use to create API key in Grafana and save created key in database.
	 * 
	 * @param orgId
	 * @throws InsightsCustomException
	 */
	public void generateGrafanaToken(int orgId) throws InsightsCustomException {
		try {
			Map<String, String> headers = generateGrafanaHeaderForAPIToken(orgId);
			int grafanaTokenId = checkGrafanaToken(headers);
			if (grafanaTokenId == -1) {
				generateGrafanaOrgToken(orgId, headers);
			}
		} catch (Exception e) {
			log.error("Unable to generate Grafana token  {}", e.getMessage());
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	private int checkGrafanaToken(Map<String, String> headers) throws InsightsCustomException {
		// check if API key exists in Grafana
		int grafanaTokenId = -1;
		String getApiKeyResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.API_AUTH_KEYS, headers);
		JsonArray getApiKeyResponseObj = JsonUtils.parseStringAsJsonArray(getApiKeyResponse);
		for (JsonElement jsonElement : getApiKeyResponseObj) {
			if (jsonElement.getAsJsonObject().has("id")
					&& jsonElement.getAsJsonObject().get("name").getAsString().equalsIgnoreCase(PDFTOKEN)) {
				grafanaTokenId = jsonElement.getAsJsonObject().get("id").getAsInt();
			}
		}
		return grafanaTokenId;
	}

	private void generateGrafanaOrgToken(int orgId, Map<String, String> headers) throws InsightsCustomException {
		GrafanaOrgToken grafanaOrgToken = new GrafanaOrgToken();
		GrafanaOrgToken token = grafanaDashboardConfigDAL.getTokenByOrgId(orgId);
		if (token != null) {
			grafanaDashboardConfigDAL.deleteGrafanaOrgToken(orgId);
		}
		JsonObject json = new JsonObject();
		json.addProperty(NAME, PDFTOKEN);
		json.addProperty(ROLE, ADMIN);
		String response = grafanaHandler.grafanaPost(PlatformServiceConstants.API_AUTH_KEYS, json, headers);
		JsonObject apiObj = JsonUtils.parseStringAsJsonObject(response);
		grafanaOrgToken.setOrgId(orgId);
		grafanaOrgToken.setApiKey(AES256Cryptor.encrypt(apiObj.get("key").getAsString(),
				AssessmentReportAndWorkflowConstants.GRAFANA_PDF_TOKEN_SIGNING_KEY));
		grafanaDashboardConfigDAL.saveGrafanaOrgToken(grafanaOrgToken);
	}

	private Map<String, String> generateGrafanaHeaderForAPIToken(int orgId) throws InsightsCustomException {
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
				+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
		String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
		headers.put(AuthenticationUtils.AUTH_HEADER_KEY, BASICAUTH + encodedString);
		headers.put("x-grafana-org-id", String.valueOf(orgId));
		return headers;
	}

	/**
	 * Method use to refresh API key and Grafana Token.
	 * 
	 * @param orgId
	 * @throws InsightsCustomException
	 */
	public void refreshGrafanaToken(int orgId) throws InsightsCustomException {
		try {
			Map<String, String> headers = generateGrafanaHeaderForAPIToken(orgId);
			int grafanaTokenId = checkGrafanaToken(headers);
			if (grafanaTokenId > -1) {
				deleteApiKeys(grafanaTokenId, headers);
			}
			generateGrafanaOrgToken(orgId, headers);
		} catch (Exception e) {
			log.error("Unable to generate Grafana token  {}", e.getMessage());
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Method use to delete an API key.
	 * 
	 * @param jsonElement, headers
	 * @throws InsightsCustomException
	 */
	private void deleteApiKeys(int tokenId, Map<String, String> headers) throws InsightsCustomException {
		try {
			grafanaHandler.grafanaDelete("/api/auth/keys/" + tokenId, headers);
		} catch (Exception e) {
			log.error(" Unable to delete API key ", e);
			throw new InsightsCustomException(e.getMessage());
		}

	}

}
