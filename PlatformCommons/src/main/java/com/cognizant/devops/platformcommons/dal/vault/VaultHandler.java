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
package com.cognizant.devops.platformcommons.dal.vault;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;



public class VaultHandler {
	
	private static Logger log = LogManager.getLogger(VaultHandler.class);
	private static final String VAULT_TOKEN = ApplicationConfigProvider.getInstance().getVault().getVaultToken();
	private static final String VAULT_URL = ApplicationConfigProvider.getInstance().getVault().getVaultEndPoint() 
			+ ApplicationConfigProvider.getInstance().getVault().getSecretEngine() +"/data/";

	
	
	/**
	 * Stores userid/pwd/token and aws credentials in vault engine as per vaultId.
	 * 
	 * @param dataMap
	 * @param vaultId
	 * @return response
	 * @throws InsightsCustomException
	 */
	public String storeToVault(JsonObject dataJson, String vaultId) throws InsightsCustomException {
		String response = null;
		Map<String, String> headers = new HashMap<>();

		try {
			headers.put("X-Vault-Token", VAULT_TOKEN);
			JsonObject requestJson = new JsonObject();
			requestJson.add("data", dataJson);
			log.debug("Request body for vault {} -- ", requestJson);
			String url = VAULT_URL + vaultId;
			response = RestApiHandler.doPost(url, requestJson, headers);

		} catch (Exception e) {
			log.error("Error while Storing to vault agent {} ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return response;
	}

	/**
	 * fetches userid/pwd/token and aws credentials in vault engine as per vaultID.
	 * 
	 * @param vaultId
	 * @return data
	 * @throws InsightsCustomException
	 */
	public String fetchFromVault(String vaultId) throws InsightsCustomException {
		String data = null;
		Map<String, String> headers = new HashMap<>();
		
		try {
			headers.put("X-Vault-Token", VAULT_TOKEN);
			String url = VAULT_URL + vaultId;
			data = RestApiHandler.doGet(url, headers);
		} catch (Exception e) {
			log.error("Error while fetching secret from vault {}-- ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return data;
	}
	
	public String storeToVault(JsonObject dataJson, String vaultId, String url) throws InsightsCustomException {
		String response = null;
		Map<String, String> headers = new HashMap<>();

		try {
			headers.put("X-Vault-Token", VAULT_TOKEN);
			JsonObject requestJson = new JsonObject();
			requestJson.add("data", dataJson);
			log.debug("Request body for vault {} -- ", requestJson);
			response = RestApiHandler.doPost(url, requestJson, headers);

		} catch (Exception e) {
			log.error("Error while Storing to vault agent {} ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return response;
	}

}
