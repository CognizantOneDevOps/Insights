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
package com.cognizant.devops.platformservice.rest.serviceStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@RestController
@RequestMapping("/InsightsServiceStatus")
public class InsightsServiceStatus {
	
private static final String VERSION = "version";
private static final String PLATFORM_SERVICE_VERSION_FILE = "version.properties";

static Logger log = Logger.getLogger(InsightsServiceStatus.class.getName());
@RequestMapping(value = "/getStatus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ResponseBody
public JsonObject loadProperties() throws JsonSyntaxException, IOException{
	JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty("InSights Service Version",getVersionDetails(PLATFORM_SERVICE_VERSION_FILE));
    return jsonObj;
}

private static String getVersionDetails(String fileName) throws IOException {
	InputStream input = InsightsServiceStatus.class.getClassLoader().getResourceAsStream(fileName);
	try {
		Properties prop = new Properties();
		prop.load(input);
		if(input != null){
			return prop.getProperty(VERSION);
		}
		
	}finally {
		try {
			if( null != input ){
			   input.close();
			}
		} catch (IOException e) {
			log.error("Error while capturing PlatformService version",e);
		}
	}
	
	
	return "";
}

}
