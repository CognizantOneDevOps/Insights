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
package com.cognizant.devops.platformservice.rest.setup;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EndpointData;
import com.cognizant.devops.platformcommons.config.LDAPConfiguration;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author 146414
 * This class will accept the iSight configurations required for interacting with other tools.
 */
@RestController
@RequestMapping("/configure")
public class SetupApplication {
	static Logger log = LogManager.getLogger(SetupApplication.class.getName());
	
	@RequestMapping(value = "/read", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public String loadConfig(){
		JsonParser parser = new JsonParser();
		JsonElement data = parser.parse(ApplicationConfigCache.readConfigFile());
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(data);
	}
	
	@RequestMapping(value = "/loadConfigFromResources", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public String reInitialize(){
		ApplicationConfigCache.initConfigCacheFromResources();
		JsonParser parser = new JsonParser();
		JsonElement data = parser.parse(ApplicationConfigCache.readConfigFile());
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(data);
	}
	
	@RequestMapping(value = "/endpoints", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public String configureToolsEndpoints(@RequestBody EndpointData endPointData){
		if(endPointData == null){
			return "{ \"message\": \"EndpointData is null\"}";
		}
		ApplicationConfigProvider applicationConfig = ApplicationConfigProvider.getInstance();
		applicationConfig.setEndpointData(endPointData);
		ApplicationConfigCache.updateConfigCahe();
		return "{ \"message\": \"Application endpoint configured successfully\"}";
	}
	
	@RequestMapping(value = "/ldapUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String configureLDAPUser(@RequestBody LDAPConfiguration userData){
		if(userData == null){
			return "{ \"message\": \"LDAPUser is null\"}";
		}
		ApplicationConfigProvider applicationConfig = ApplicationConfigProvider.getInstance();
		//applicationConfig.setLdapConfiguration(userData);
		ApplicationConfigCache.updateConfigCahe();
		return "{ message: \"User configured successfully\"}";
	}
	
	@RequestMapping(value = "/validateHost", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject validateHost(@RequestParam String host){
		
		ApplicationConfigProvider applicationConfig = ApplicationConfigProvider.getInstance();
		List<String> whiteList = applicationConfig.getTrustedHosts();
		Iterator<String> whiteListItr = whiteList.listIterator();
		Boolean isTrusted = Boolean.FALSE;
		
		while(whiteListItr.hasNext()) {
			String whiteHost = whiteListItr.next();
			isTrusted = host.contains(whiteHost);
			if(isTrusted) {
				break;
			}
		}
		JsonObject jsonObj = new JsonObject();
		jsonObj.addProperty("isTrusted",isTrusted );
		/*if(isTrusted) {
			return "{\"isTrusted\":true}";
		} else {
			return "{\"isTrusted\":false}";
		}*/
		return jsonObj;
	}
	
	@RequestMapping(value = "/grafanaEndPoint", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject grafanaEndPoint(){
		ApplicationConfigProvider applicationConfig = ApplicationConfigProvider.getInstance();
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.addProperty("grafanaEndPoint", applicationConfig.getGrafana().getGrafanaEndpoint());
		return jsonResponse;

	}
}