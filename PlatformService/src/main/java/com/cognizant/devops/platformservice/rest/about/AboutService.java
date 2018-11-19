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
package com.cognizant.devops.platformservice.rest.about;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


@RestController
@RequestMapping("/about")
public class AboutService {
private static final String VERSION = "version";
private static final String PLATFORM_SERVICE_VERSION_JSON = "PlatformServiceVersion.json";
private static final String PLATFORM_ENGINE_VERSION_JSON = "PlatformEngineVersion."
		+ "json";
static Logger log = LogManager.getLogger(AboutService.class.getName());
	
	@RequestMapping(value = "/read", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public JsonObject loadProperties() throws JsonSyntaxException, IOException{
		JsonObject jsonObj = new JsonObject();
		jsonObj.addProperty("InSights Engine", getVersionDetails(PLATFORM_SERVICE_VERSION_JSON));
	    jsonObj.addProperty("InSights Service",getVersionDetails(PLATFORM_SERVICE_VERSION_JSON));
	    jsonObj.addProperty("InSights UI", getVersionDetails(PLATFORM_SERVICE_VERSION_JSON));
	    return jsonObj;
	}
	
	private static String getVersionDetails(String fileName) {
		BufferedReader reader = null;
		try {
			InputStream in = AboutService.class.getClassLoader().getResourceAsStream(fileName); 
			reader = new BufferedReader(new InputStreamReader(in));
			JsonElement jsonElement = new JsonParser().parse(reader);
			if (jsonElement != null && jsonElement.isJsonObject()) {
				return jsonElement.getAsJsonObject().get(VERSION).getAsString();
			}
		}finally {
			try {
				if( null != reader ){
				   reader.close();
				}
			} catch (IOException e) {
				log.error("error while getting version",e);
			}
		}
		
		
		return "";
	}
	
}
