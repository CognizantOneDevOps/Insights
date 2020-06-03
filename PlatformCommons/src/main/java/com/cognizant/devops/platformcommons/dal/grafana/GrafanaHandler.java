/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.dal.grafana;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.NewCookie;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.exception.RestAPI404Exception;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



public class GrafanaHandler {
	
	private static final String GRAFANA_END_POINT = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	
	
	/**
	 * @param url
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public String grafanaGet(String path, Map<String, String> headers) throws InsightsCustomException{
		String response = null;
		try {
			String url = GRAFANA_END_POINT + path;
			response = RestApiHandler.doGet(url, headers);
		} catch (RestAPI404Exception e) {
			response = getResponseMessage(e.getMessage());
		} catch (InsightsCustomException e) {
			throw e;
		}
		return response;
	}
	
	/**
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public String grafanaPost(String path, JsonElement requestJson, Map<String, String> headers) throws InsightsCustomException{
		String response = null;
		try {
			String url = GRAFANA_END_POINT + path;
			JsonObject jsonObject = requestJson.getAsJsonObject();
			response = RestApiHandler.doPost(url, jsonObject, headers);
		} catch (RestAPI404Exception e) {
			response = getResponseMessage(e.getMessage());
		} catch (InsightsCustomException e) {
			throw e;
		}
		return response;
	}
	
	/**
	 * @param url
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public String grafanaDelete(String path, Map<String, String> headers) throws InsightsCustomException{
		String response = null;
		try {
			String url = GRAFANA_END_POINT + path;
			response = RestApiHandler.doDelete(url, headers);
		} catch (RestAPI404Exception e) {
			response = getResponseMessage(e.getMessage());
		} catch (InsightsCustomException e) {
			throw e;
		}
		return response;
	}

	/**
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public String grafanaPatch(String path, JsonElement requestJson, Map<String, String> headers) throws InsightsCustomException{
		String response = null;
		try {
			String url = GRAFANA_END_POINT + path;
			JsonObject jsonObject = requestJson.getAsJsonObject();
			response = RestApiHandler.doPatch(url, jsonObject, headers);
		} catch (RestAPI404Exception e) {
			response = getResponseMessage(e.getMessage());
		} catch (InsightsCustomException e) {
			throw e;
		}
		return response;
	}
	
	/**
	 * @param loginApiUrl
	 * @param loginRequestParams
	 * @return
	 * @throws InsightsCustomException
	 */
	public List<NewCookie> getGrafanaCookies(String loginApiPath, JsonObject loginRequestParams, Map<String, String> headers) throws InsightsCustomException {
		String url = GRAFANA_END_POINT  + loginApiPath;
		Map<String, NewCookie> cookies = RestApiHandler.getCookies(url, loginRequestParams, headers);
		List<NewCookie> list = new ArrayList<>();
		list.addAll(cookies.values());
		return list;
		
	}

	/**
	 * Extract error message from exception in case of 404 exception
	 * 
	 * @param messageJson
	 * @return
	 */
	private String getResponseMessage(String messageJson) {
		return new JsonParser().parse(messageJson).getAsJsonObject().get("data").getAsString();
	}


}
