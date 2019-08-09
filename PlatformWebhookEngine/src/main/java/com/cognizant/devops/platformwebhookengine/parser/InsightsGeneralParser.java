/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformwebhookengine.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsGeneralParser implements InsightsWebhookParserInterface {
	private static Logger log = LogManager.getLogger(InsightsGeneralParser.class.getName());
    public List < JsonObject > parseToolData(String responseTemplate, String toolData) {

       try {
        String keyMqInitial;
       
        JsonParser parser = new JsonParser();
        List < JsonObject > retrunJsonList = new ArrayList < JsonObject > (0);
        JsonElement json = (JsonElement) parser.parse(toolData);
        Map < String, Object > rabbitMqflattenedJsonMap = JsonFlattener.flattenAsMap(json.toString());
        Map < String, Object > finalJson = new HashMap < String, Object > ();
        for (Map.Entry < String, Object > entry: rabbitMqflattenedJsonMap.entrySet()) {
            keyMqInitial = entry.getKey();
            String value = responseTemplate;
            String[] keyValuePairs = value.split(","); // split the string to creat key-value pairs
            for (String pair: keyValuePairs) // iterate over the pairs
            {
                String[] entry1 = pair.split("="); // split the pairs to get key and value
                Boolean testResult;
                testResult = keyMqInitial.equals(entry1[0].trim());
                if (testResult) {
                    finalJson.put(entry1[1].trim(), entry.getValue());
                
                }
            }

        }
       
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser parse = new JsonParser();
        finalJson.put("source", "webhook");
        String prettyJson = prettyGson.toJson(finalJson);
        JsonElement element = parse.parse(prettyJson);
        retrunJsonList.add(element.getAsJsonObject());
      //  String nestedJson = JsonUnflattener.unflatten(prettyJson);
        
        return retrunJsonList;
       }
       catch(Exception e)
       {
    	   log.error(e);
    	   List<JsonObject> list = new ArrayList<JsonObject>();
    	  
    	   for(JsonObject jsonResponse: list) {
    		   jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
      		   jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, e.getMessage());
    	   } 		 		
   		
   		return list;
    	  
       }
    }

}