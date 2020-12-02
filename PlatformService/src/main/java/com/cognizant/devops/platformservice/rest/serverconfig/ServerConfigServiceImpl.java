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
package com.cognizant.devops.platformservice.rest.serverconfig;

import java.io.InputStream;
import java.util.Scanner;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.cognizant.devops.platformcommons.core.util.CommonUtils;
import com.cognizant.devops.platformcommons.dal.vault.VaultHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.config.PlatformServiceStatusProvider;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Service("serverConfigServiceImpl")
public class ServerConfigServiceImpl {
	
	private static final Logger log = LogManager.getLogger(ServerConfigServiceImpl.class);
	VaultHandler vaultHandler = new VaultHandler();
	Gson gson = new Gson();
	
	/** This method fetch and validate server config data
	 * @return
	 * @throws InsightsCustomException
	 */
	public boolean getServerConfigStatus() throws InsightsCustomException {
		boolean status = Boolean.FALSE;
		try {
			status=ApplicationConfigCache.validateServerConfig();
		} catch (Exception e) {
			log.error(" Error while loding loadServerConfigDetail ",e);
		}
		return status;
	}
	
	/**This method store all data of server config in vault database or on file system  
	 * @param serverConfigJson
	 * @param clientId
	 * @return
	 * @throws InsightsCustomException
	 */
	public boolean saveServerConfigTemplate(String serverConfigJson,String clientId) throws InsightsCustomException {
		
		ApplicationConfigProvider config;
		try {
			String passkey = serverConfigJson.substring(0, 15);
			String auth= serverConfigJson.substring( 15, serverConfigJson.length());
			String serverConfigJsonDecrypt = AES256Cryptor.decrypt(auth, passkey);
			config = gson.fromJson(serverConfigJsonDecrypt, ApplicationConfigProvider.class);
			JsonObject serverConfigReceivedJson = new JsonParser().parse(serverConfigJsonDecrypt).getAsJsonObject();
			if (config.getVault().isVaultEnable()) {
				String vaultURLDetail =  "/sys/raw/"+ config.getVault().getSecretEngine() + "/" + clientId + "/serverConfig";
				vaultHandler.storeToVaultJsonInDB(serverConfigReceivedJson, 
						config.getVault().getVaultEndPoint(),
						vaultURLDetail,
						config.getVault().getVaultToken());
			} else {
				ApplicationConfigCache.saveConfigFile(serverConfigReceivedJson);
				//update vault detail in insightsConfiguration 
			}
			ApplicationConfigCache.initializeUsingJson(serverConfigReceivedJson);
			PlatformServiceStatusProvider.getInstance().createPlatformServiceStatusNode(
					"PlatformService Server Config Stored Successfully  ", PlatformServiceConstants.SUCCESS);
		} catch (InsightsCustomException e) {
			log.error(e);
			PlatformServiceStatusProvider.getInstance().createPlatformServiceStatusNode(
					"Error in storing server config ", PlatformServiceConstants.FAILURE);
			throw new InsightsCustomException("Error while saving server config file " +e.getMessage());
		}
		return true;
	}
	
	/** Method used to fetch server config detail from vault or from file system
	 * @return
	 * @throws InsightsCustomException
	 */
	public String getServerConfigTemplate() throws InsightsCustomException {
		StringBuilder sb = new StringBuilder();
		String encodededData =null;
		JsonObject serverConfigJsonMerged = new JsonObject();
		JsonObject serverConfigJsonfromStorage = new JsonObject();
		JsonObject serverConfigJsonfromVault = new JsonObject();
		try (InputStream serverConfigTemplateStream = getClass().getClassLoader().getResourceAsStream("server-config-template.json");
				Scanner sc= new Scanner(serverConfigTemplateStream);){
					      
		     while(sc.hasNext()){
		         sb.append(sc.nextLine());
		      }
		     log.debug("server config template {} ",sb);
		     serverConfigJsonfromStorage = ApplicationConfigCache.loadServerConfigFromFile();
		     JsonObject serverConfigTempate = new JsonParser().parse(sb.toString()).getAsJsonObject();
		     ApplicationConfigProvider config = gson.fromJson(serverConfigJsonfromStorage, ApplicationConfigProvider.class);
		     if(config.getVault().isVaultEnable()) {
		    	 try {
					serverConfigJsonfromVault = vaultHandler.fetchServerConfigFromVault("local",
							 config.getVault().getVaultEndPoint(),
							 config.getVault().getSecretEngine(),
							 config.getVault().getVaultToken());
				} catch (Exception e) {
					log.error(e);
				}
		     }
		     /* If Vault is enable and server confing is not present in vault then merge serverConfigTempate 
		      * 	with server config file (from file system)
		      * If Vault is enable and server confing is pesent then then merge serverConfigTempate 
		      * 	with data present in vault 
		      * If Vault is disable and merge serverConfigTempate 
		      * 	with server config file (from file system)
		      */
		    if(!serverConfigJsonfromVault.entrySet().isEmpty()) {
		    	serverConfigJsonMerged= CommonUtils.mergeTwoJson(serverConfigTempate,serverConfigJsonfromVault);
		    	serverConfigTempate.add("trustedHosts", serverConfigJsonfromVault.get("trustedHosts"));
		    }else {
		    	serverConfigJsonMerged= CommonUtils.mergeTwoJson(serverConfigTempate,serverConfigJsonfromStorage);
		    	serverConfigTempate.add("trustedHosts", serverConfigJsonfromStorage.get("trustedHosts"));
		    }
		    String passKey = UUID.randomUUID().toString().substring(0, 15);
		    encodededData = passKey+AES256Cryptor.encrypt(serverConfigJsonMerged.toString(), passKey);
		} catch (Exception e) {
			log.error("Error while reading report template ",e);
			throw new InsightsCustomException("Error while reading report template "+e.getMessage());
		}
		
		return encodededData;
	}
}
