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

import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsGeneralParser implements InsightsWebhookParserInterface {
	
	public List<JsonObject> parseToolData(String responseTemplate, String toolData) {

		String keyMq = "";
		String keyRT = "";
		String keyMqInitial;
		String keyRTInitial;
		Object valueRT;
		String removeString = "";

		JsonParser parser = new JsonParser();
		List<JsonObject>  retrunJsonList = new ArrayList<JsonObject>(0);
		JsonElement json = (JsonElement) parser.parse(toolData);
		Map<String, Object> rabbitMqflattenedJsonMap = JsonFlattener.flattenAsMap(json.toString());
		Map<String, Object> finalJson = new HashMap<String, Object>();
		JsonObject neo4jjson = new JsonObject();
		char b1;
		char b2;
		Boolean hault = false;
		// rabbitMqflattenedJsonMap.forEach((k, v) -> keyMq = k);
		for (Map.Entry<String, Object> entry : rabbitMqflattenedJsonMap.entrySet()) {
			//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			keyMqInitial = entry.getKey();
			int l1 = keyMqInitial.length();
			// System.out.println(l1);
			for (int i = 0; i < l1; i++) {
				b1 = keyMqInitial.charAt(i);
				keyMq = keyMqInitial;
				// System.out.print(b1);
				if (b1 == '[') {
					hault = true;
				} else if (b1 == ']') {
					hault = false;

					removeString = removeString.substring(1);
					// log.error(removeString);
					keyMq = keyMqInitial.replaceAll(removeString, "");
					// System.out.println("keyMq"+keyMq);
					removeString = "";
				}

				if (hault) {
					removeString = removeString + b1;
				}

			}
			String value = responseTemplate;
			// value = value.substring(1, value.length()-1); //remove curly brackets
			String[] keyValuePairs = value.split(","); // split the string to creat key-value pairs
			Map<String, String> map = new HashMap<>();

			for (String pair : keyValuePairs) // iterate over the pairs
			{
				String[] entry1 = pair.split("="); // split the pairs to get key and value
				// s map.put(entry1[0].trim(), entry1[1].trim());

				// add them to the hashmap and trim whitespaces

				Boolean testResult;
				// System.out.println("bEFORE Compare...keyRT" + keyRT);
				// System.out.println("bEFORE Compare...keyMQ" + keyMq);
				testResult = keyMq.equals(entry1[0].trim());
				// log.error(testResult);

				if (testResult) {
					// finalJson.put(keyRTInitial, entry.getValue());
					finalJson.put(entry1[1].trim(), entry.getValue());
					System.out.println(finalJson);
				}
			}

			/*
			 * for (Map.Entry<String, Object> insideEntry :
			 * responseTemplateflattenedJsonMap.entrySet()) { keyRTInitial =
			 * insideEntry.getKey(); valueRT=insideEntry.getValue();
			 * //System.out.println(keyRTInitial); int l2 = keyRTInitial.length();
			 * //log.error("Length of the key of Response Template" + l2); for (int i = 0; i
			 * < l2; i++) { b1 = keyRTInitial.charAt(i); keyRT = keyRTInitial;
			 * 
			 * if (b1 == '[') { hault = true; } else if (b1 == ']') { hault = false;
			 * 
			 * removeString = removeString.substring(1); keyRT =
			 * keyRTInitial.replaceAll(removeString, ""); //System.out.
			 * println("Final string after the removal of index in response template " +
			 * keyRT); removeString = ""; }
			 * 
			 * if (hault) { removeString = removeString + b1; }
			 * 
			 * } Boolean testResult; //System.out.println("bEFORE Compare...keyRT" + keyRT);
			 * //System.out.println("bEFORE Compare...keyMQ" + keyMq); testResult =
			 * keyMq.equals(keyRT); // log.error(testResult);
			 * 
			 * if (testResult) { // finalJson.put(keyRTInitial, entry.getValue());
			 * finalJson.put(valueRT.toString(), entry.getValue());
			 * //System.out.println(finalJson); } Gson prettyGson = new
			 * GsonBuilder().setPrettyPrinting().create(); String stringtoContinue =
			 * prettyGson.toJson(entry.getValue()); JsonObject json2 = (JsonObject)
			 * parser.parse(stringtoContinue); neo4jjson.add(valueRT.toString(), json2);
			 * 
			 * }
			 */

		}
		// sGsonBuilder gsonMapBuilder = new GsonBuilder();
		/*
		 * Gson gsonObject = gsonMapBuilder.create(); String JSONObject =
		 * gsonObject.toJson(finalJson);
		 * 
		 * System.out.println("First method"+JSONObject);
		 */
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser parse = new JsonParser();
		String prettyJson = prettyGson.toJson(finalJson);
		JsonElement element = parse.parse(prettyJson);
	    System.out.println("second method" + prettyJson);
	    retrunJsonList.add(element.getAsJsonObject());
		String nestedJson = JsonUnflattener.unflatten(prettyJson);
		//System.out.println("\n=====Unflatten it back to original JSON===== \n" + nestedJson);

		/*
		 * String maptostring= finalJson.toString(); String nestedJson =
		 * JsonUnflattener.unflatten(maptostring);
		 * 
		 * JsonObject finaljson = (JsonObject) parser.parse(nestedJson);
		 * System.out.println("\n=====Unflatten it back to original JSON===== \n" +
		 * nestedJson);
		 */

		return retrunJsonList;
	}

}
