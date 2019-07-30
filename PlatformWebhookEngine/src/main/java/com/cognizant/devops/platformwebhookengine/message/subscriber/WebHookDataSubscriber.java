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
package com.cognizant.devops.platformwebhookengine.message.subscriber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.MessageConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
//import com.cognizant.devops.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformwebhookengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformwebhookengine.modules.aggregator.WebhookMappingData;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class WebHookDataSubscriber extends EngineSubscriberResponseHandler {

	private static Logger log = LogManager.getLogger(WebHookDataSubscriber.class.getName());
	String responseTemplate;
	JsonElement responseTemplateJson;

	public WebHookDataSubscriber(String routingKey, String responseTemplate) throws Exception {
		super(routingKey);

		this.responseTemplate = responseTemplate;

	}

	/*
	 * private boolean dataUpdateSupported; private String uniqueKey; private String
	 * category; private String toolName;
	 */
	List<WebhookMappingData> webhookMappingList = new ArrayList<WebhookMappingData>(0);

	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		String keyMq = "";
		String keyRT = "";
		String keyMqInitial;
		String keyRTInitial;
		Object valueRT;
		String removeString = "";
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String message = new String(body, MessageConstants.MESSAGE_ENCODING);
		String routingKey = envelope.getRoutingKey();
		// log.debug(responseTemplate);

		routingKey = routingKey.replace("_", ".");
		// log.debug(consumerTag+" [x] Received '" + routingKey + "':'" + message +"'");

		JsonParser parser = new JsonParser();
		JsonElement json = (JsonElement) parser.parse(message);
		// log.debug(json);
		// JsonElement responseTemplateJson = (JsonElement)
		// parser.parse(responseTemplate);

		// String flattenedJson =
		// JsonFlattener.flatten(responseTemplateJson.toString());
		// log.debug("\n=====Simple Flatten===== \n" + flattenedJson);
		/*
		 * Map<String, Object> responseTemplateflattenedJsonMap = JsonFlattener
		 * .flattenAsMap(responseTemplateJson.toString());
		 */

		// log.debug("\n=====Simple Flatten===== \n" +
		// responseTemplateflattenedJsonMap);

		Map<String, Object> rabbitMqflattenedJsonMap = JsonFlattener.flattenAsMap(json.toString());
		// System.out.println("Size of the rabbit mq json.." +
		// rabbitMqflattenedJsonMap.size());
		Map<String, Object> finalJson = new HashMap<String, Object>();
		JsonObject neo4jjson = new JsonObject();
		// log.debug("\n=====Simple Flatten===== \n" + rabbitMqflattenedJsonMap);
		char b1;
		char b2;
		Boolean hault = false;
		// rabbitMqflattenedJsonMap.forEach((k, v) -> keyMq = k);
		for (Map.Entry<String, Object> entry : rabbitMqflattenedJsonMap.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
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
				map.put(entry1[0].trim(), entry1[1].trim());

				// add them to the hashmap and trim whitespaces

				Boolean testResult;
				// System.out.println("bEFORE Compare...keyRT" + keyRT);
				// System.out.println("bEFORE Compare...keyMQ" + keyMq);
				testResult = keyMq.equals(entry1[0].trim());
				// log.error(testResult);

				if (testResult) {
					// finalJson.put(keyRTInitial, entry.getValue());
					finalJson.put(entry1[1].trim(), entry.getValue());
					// System.out.println(finalJson);
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
		String prettyJson = prettyGson.toJson(finalJson);
		// System.out.println("second method" + prettyJson);
		String nestedJson = JsonUnflattener.unflatten(prettyJson);
		System.out.println("\n=====Unflatten it back to original JSON===== \n" + nestedJson);

		/*
		 * String maptostring= finalJson.toString(); String nestedJson =
		 * JsonUnflattener.unflatten(maptostring);
		 * 
		 * JsonObject finaljson = (JsonObject) parser.parse(nestedJson);
		 * System.out.println("\n=====Unflatten it back to original JSON===== \n" +
		 * nestedJson);
		 */

	}

	// processJson(json);
	// processJson(responseTemplateJson);

	private void processJson(JsonElement jsonElement) {
		// {"results":[{"columns":["n"],"data":[{"row":[{"name":"Test
		// me"}]},{"row":[{"name":"Test me"}]}]}],"errors":[]}
		if (jsonElement.isJsonNull()) {
			log.debug("Null value " + jsonElement);
		} else if (jsonElement.isJsonArray()) {
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			log.error("Json Array found " + jsonArray.size());
			if (jsonArray.size() > 0) {
				for (JsonElement jsonArrayElement : jsonArray) {
					if (jsonArrayElement.isJsonObject()) {
						processJson(jsonArrayElement);
					} else {
						log.debug("Elemnet.." + jsonArrayElement);
					}
				}
			} else {
				log.debug("Null value" + jsonArray);
			}
		} else {
			log.error(jsonElement);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			log.debug(jsonObject);
			if (!jsonObject.isJsonNull()) {

				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {

					if (entry.getValue().isJsonNull()) {
						log.error("Null Value Found" + entry.getKey());
					} else if (entry.getValue().isJsonArray()) {
						processJson(entry.getValue());
						log.debug("returened from rabbit mq function");

					} else if (!entry.getValue().isJsonPrimitive()) {
						parseJsonPrimitive(entry);
						log.debug("Comeback from Json Promitive");

					} else {

						log.debug("  Key..." + entry.getKey() + "   Value..." + entry.getValue());
					}

				}
			}
		}
	}

	private void parseJsonPrimitive(Map.Entry<String, JsonElement> entry) {
		log.debug("Entry..." + entry);

		if (entry.getValue().isJsonArray()) {
			processJson(entry.getValue());
		} else if (!entry.getValue().isJsonNull()) {
			log.error("Entered the not null conditionss");
			JsonObject jsonObjectInternal = entry.getValue().getAsJsonObject();
			for (Map.Entry<String, JsonElement> entryAgain : jsonObjectInternal.entrySet()) {
				if (entry.getValue().isJsonArray()) {
					processJson(entry.getValue());
				} else if (!entryAgain.getValue().isJsonNull() && !entryAgain.getValue().isJsonPrimitive()) {
					parseJsonPrimitive(entryAgain);
				} else {
					log.debug("Key internal ..." + entryAgain.getKey() + " Value..." + entryAgain.getValue());
				}
			}
		}

		else {
			log.debug(" value of Key internal is null  ..." + entry.getKey());
		}
	}
}
