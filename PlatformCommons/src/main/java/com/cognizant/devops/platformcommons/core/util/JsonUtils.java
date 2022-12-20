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
package com.cognizant.devops.platformcommons.core.util;

import java.io.Reader;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtils {
	private static final Logger log = LogManager.getLogger(JsonUtils.class);
	
	private JsonUtils() {
		
	}
	/** This method is used to merge two Json object 
	 * @param json1Obj
	 * @param json2Obj
	 * @return
	 */
	public static JsonObject mergeTwoJson(JsonObject json1Obj, JsonObject json2Obj) {

		try {
			Set<Entry<String, JsonElement>> entrySet1 = json1Obj.entrySet();
			for (Entry<String, JsonElement> entry : entrySet1) {
				String key1 = entry.getKey();
				if (json2Obj.get(key1) != null) {
					JsonElement tempEle2 = json2Obj.get(key1);
					JsonElement tempEle1 = entry.getValue();
					if (tempEle2.isJsonObject() && tempEle1.isJsonObject()) {
						JsonObject mergedObj = mergeTwoJson(tempEle1.getAsJsonObject(), tempEle2.getAsJsonObject());
						entry.setValue(mergedObj);
					}else if(tempEle2.isJsonPrimitive() && tempEle1.isJsonPrimitive()) {
						entry.setValue(tempEle2);
					}
				}
			}
			Set<Entry<String, JsonElement>> entrySet2 = json2Obj.entrySet();
			for (Entry<String, JsonElement> entry : entrySet2) {
				String key2 = entry.getKey();
				if (json1Obj.get(key2) == null) {
					json1Obj.add(key2, entry.getValue());
				}
			}
		} catch (Exception e) {
			log.error(" Error while mergeing two json ",e);
		}		
		return json1Obj;
	}
	
	public static JsonElement parseString(String requestText) {
		JsonElement response = null ;
		try {			
			response = JsonParser.parseString(requestText);
		} catch (Exception e) {
			 log.error(" Error in parseString "); 
			 log.error(e);		
		}	
		return response; 
	}
	
	public static JsonElement parseReader(Reader requestReader) {
		JsonElement response = null;	
		try {			
			response = JsonParser.parseReader(requestReader);
		} catch (Exception e) {
			log.error(" Error in parseReader ");
			log.error(e);
		}
		return response;
	}
	
	public static JsonObject parseStringAsJsonObject(String requestText) {
		JsonObject response = null;
		try {			
			response =  parseString(requestText).getAsJsonObject();
		} catch (Exception e) {
			log.error(" Error in parseStringAsJsonObject ");
			log.error(e);			
		}
		return response;
	}
	
	public static JsonElement parseStringAsJsonElement(String requestText) {
		JsonElement response = null;
		try {			
			response = JsonParser.parseString(requestText);
		} catch (Exception e) {
			log.error(" Error in parseStringAsJsonElement ");
			log.error(e);
		}
		return response;
	}
	
	public static JsonObject parseReaderAsJsonObject(Reader requestReader) {
		JsonObject response = null;
		try {			
			response =  parseReader(requestReader).getAsJsonObject();
		} catch (Exception e) {
			log.error(" Error in parseReaderAsJsonObject "); 
			log.error(e);			 
		}
		return response;
	}
	
	public static JsonArray parseStringAsJsonArray(String requestText) {
		JsonArray response = null;
		try {
			response = parseString(requestText).getAsJsonArray();
		} catch (Exception e) {			
			log.error(" Error in parseStringAsJsonArray "); 
			log.error(e);			 
		}	
		return response;
	}
	
	
	public static String getValueFromJson(JsonElement jsonElement,String property) {
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		if(jsonObject.has(property)) {
			return jsonObject.get(property).getAsString();
		}
		return "-";
	}
}
