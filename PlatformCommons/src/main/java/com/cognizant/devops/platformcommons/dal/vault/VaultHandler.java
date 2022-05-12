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
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.exception.RestAPI404Exception;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import jakarta.ws.rs.ProcessingException;



public class VaultHandler {
	
	private static Logger log = LogManager.getLogger(VaultHandler.class);
	private String VAULT_TOKEN = ApplicationConfigProvider.getInstance().getVault().getVaultToken();
	private String VAULT_URL = ApplicationConfigProvider.getInstance().getVault().getVaultEndPoint()
			+ ApplicationConfigProvider.getInstance().getVault().getSecretEngine() +"/data/";

	
	
	public VaultHandler() {
		VAULT_TOKEN = ApplicationConfigProvider.getInstance().getVault().getVaultToken();
		VAULT_URL = ApplicationConfigProvider.getInstance().getVault().getVaultEndPoint()
				+ ApplicationConfigProvider.getInstance().getVault().getSecretEngine() + "/data/";
	}

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
			headers.put(PlatformServiceConstants.VAULT_TOKEN, VAULT_TOKEN);
			JsonObject requestJson = new JsonObject();
			requestJson.add("data", dataJson);
			log.debug("return Request body for vault  {} -- ", requestJson);
			String url = VAULT_URL + vaultId;
			response = RestApiHandler.doPost(url, requestJson, headers);

		} catch (Exception e) {
			log.error("Error while Storing data to vault  {} ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return response;
	}

	public String storeToVaultDB(String dataJson, String url) throws InsightsCustomException {
		String response = null;
		Map<String, String> headers = new HashMap<>();

		try {
			headers.put(PlatformServiceConstants.VAULT_TOKEN, VAULT_TOKEN);
			JsonObject requestJson = new JsonObject();
			requestJson.addProperty(PlatformServiceConstants.VAULT_DATA_VALUE, dataJson);
			log.debug("url  {} ", url);
			//log.debug("Request body for vault {} -- ", requestJson);
			response = RestApiHandler.doPost(url, requestJson, headers);

		} catch (Exception e) {
			log.error("Error while Storing date to vault with database  {} ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return response;
	}
	
	public String storeToVaultJsonInDB(JsonObject dataJson, String vaultEndPoint, String vaultUrlDetail, String vaultToken) throws InsightsCustomException {
		String response = null;
		Map<String, String> headers = new HashMap<>();
		try {
			boolean vaultStatus = getVaultStatus(vaultEndPoint,vaultToken);
			if(vaultStatus) {
				String vaultServerConfigURL =vaultEndPoint +vaultUrlDetail;
				headers.put(PlatformServiceConstants.VAULT_TOKEN, vaultToken);
				JsonObject requestJson = new JsonObject();
				requestJson.addProperty(PlatformServiceConstants.VAULT_DATA_VALUE, String.valueOf(dataJson));
				log.debug("url  {} ", vaultServerConfigURL);
				//log.debug("Request body for vault {} -- ", requestJson);
				response = RestApiHandler.doPost(vaultServerConfigURL, requestJson, headers);
			}else {
				throw new InsightsCustomException("Vault_sealed : Vault is not ready, make sure vault is unsealed ");
			}
		} catch (Exception e) {
			log.error("Error while Storing date to vault with database  {} ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return response;
	}

	public JsonObject fetchServerConfigFromVault(String clientId,String vaultUrl, String vaultSecretEngine, String vaultToken) throws InsightsCustomException {
		JsonObject serverConfig = new JsonObject();
		try {
			boolean vaultStatus = getVaultStatus(vaultUrl,vaultToken);
			if(vaultStatus) {
				String vaultServerConfigURL = vaultUrl
						+ "/sys/raw/" + vaultSecretEngine+ "/"
						+ clientId + "/serverConfig";
				log.debug(" vaultServerConfigURL {} ", vaultServerConfigURL);
				String dataFromVault = fetchFromVaultDB(vaultServerConfigURL,vaultToken);
				if(dataFromVault !=null) {
					JsonObject parsedJson = JsonUtils.parseStringAsJsonObject(dataFromVault);
					if (parsedJson.has("data")
							&& parsedJson.get("data").getAsJsonObject().has(PlatformServiceConstants.VAULT_DATA_VALUE)) {
						JsonElement serverConfigelement = JsonUtils.parseString(parsedJson.get("data").getAsJsonObject().get(PlatformServiceConstants.VAULT_DATA_VALUE).toString());
						serverConfig = JsonUtils.parseStringAsJsonObject(serverConfigelement.getAsString()); 
					} else {
						log.error("Reading server config from Vault is not proper format {} ", parsedJson);
					}
				}else {
					log.debug("No Data found in vault ");
				}
			}else {
				throw new InsightsCustomException("Vault is not ready, make sure vault is unsealed ");
			}
			//log.debug(" serverConfig   ======== {} ", serverConfig);
		} catch (InsightsCustomException e) {
			log.error(" Error while reading server config from Vault ", e);
			throw new InsightsCustomException(e.getMessage());
		} catch (JsonParseException e) {
			log.error(" Error while reading server config from Vault, Json parsing exception ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return serverConfig;
	}

	public String fetchFromVaultDB(String url,String vaultToken) throws InsightsCustomException {
		String data = null;
		Map<String, String> headers = new HashMap<>();
		try {
			headers.put(PlatformServiceConstants.VAULT_TOKEN, vaultToken);
			data = RestApiHandler.doGet(url, headers);
		} catch (RestAPI404Exception e) {
			log.error(" Data not present in vault {}-- ", e);
			throw new InsightsCustomException("Data not present in vault");
		}catch (Exception e) {
			log.error("Error while fetching secret from vault -- ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return data;
	}
	
	public boolean getVaultStatus(String vaultBaseUrl,String vaultToken) throws InsightsCustomException {
		boolean vaultStatusRet = Boolean.FALSE;
		Map<String, String> headers = new HashMap<>();
		try {
			String vaultStatusURL = vaultBaseUrl
					+ "/sys/seal-status";
			headers.put(PlatformServiceConstants.VAULT_TOKEN, vaultToken);
			String data = RestApiHandler.doGet(vaultStatusURL, headers);
			JsonObject vaultStatus = JsonUtils.parseStringAsJsonObject(data);
			if(vaultStatus.has("sealed") && vaultStatus.get("sealed").getAsBoolean()) {
				throw new InsightsCustomException("Vault_sealed");
			}
			vaultStatusRet = Boolean.TRUE;
		} catch (RestAPI404Exception e) {
			log.error(" Data not present in vault {}-- ", e);
		}catch (ProcessingException |InsightsCustomException e) {
			log.error("Error while fetching secret from vault {}-- ", e);
			throw e;
		}
		return vaultStatusRet;
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
			headers.put(PlatformServiceConstants.VAULT_TOKEN, VAULT_TOKEN);
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
			headers.put(PlatformServiceConstants.VAULT_TOKEN, VAULT_TOKEN);
			JsonObject requestJson = new JsonObject();
			requestJson.add("data", dataJson);
			response = RestApiHandler.doPost(url, requestJson, headers);
		} catch (Exception e) {
			log.error("Error while Storing to vault agent {} ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return response;
	}
}
