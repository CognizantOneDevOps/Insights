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
package com.cognizant.devops.platformcommons.dal.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * @author Vishal Ganjare (vganjare)
 * This class will hold the details about the Neo4j fields for which the index has been enabled.
 *
 */
public class Neo4jFieldIndexRegistry {
	private static final Logger log = LogManager.getLogger(Neo4jFieldIndexRegistry.class);
	private static Map<String, List<String>> indexedFieldsRegistry = new HashMap<String, List<String>>();
	private static final Neo4jFieldIndexRegistry instance = new Neo4jFieldIndexRegistry();
	
	private Neo4jFieldIndexRegistry() {
		loadFieldIndices();
		addDefaultFieldIndices();
	}
	
	public static Neo4jFieldIndexRegistry getInstance() {
		return instance;
	}

	/**
	 * Add index for give label and field.
	 * @param label
	 * @param field
	 */
	public synchronized void syncFieldIndex(String label, String field) {
		if(ApplicationConfigProvider.getInstance().isEnableFieldIndex()) {
			List<String> indexedFields = indexedFieldsRegistry.get(label);
			if(indexedFields == null) {
				indexedFields = new ArrayList<String>();
				indexedFieldsRegistry.put(label, indexedFields);
			}
			if(!indexedFields.contains(field)) {
				Neo4jDBHandler dbHandler = new Neo4jDBHandler();
				JsonObject addFieldIndex = dbHandler.addFieldIndex(label, field);
				indexedFields.add(field);
				log.debug(addFieldIndex);
			}
		}
	}
	
	/**
	 * Load the field indices from Neo4j
	 */
	private void loadFieldIndices() {
		indexedFieldsRegistry.clear();
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		JsonArray fieldIndices = dbHandler.loadFieldIndices();
		for(JsonElement fieldIndexElem : fieldIndices) {
			JsonObject fieldIndex = fieldIndexElem.getAsJsonObject();
			String label = fieldIndex.get("label").getAsString();
			String field = fieldIndex.getAsJsonArray("property_keys").get(0).getAsString();
			List<String> fields = indexedFieldsRegistry.get(label);
			if(fields == null) {
				fields = new ArrayList<String>();
				indexedFieldsRegistry.put(label, fields);
			}
			fields.add(field);
		}
	}
	
	/**
	 * Add the indices for default fields in InSights.
	 */
	private void addDefaultFieldIndices() {
		syncFieldIndex("DATA", "uuid");
		syncFieldIndex("DATA", "toolName");
		syncFieldIndex("DATA", "correlationTime");
		syncFieldIndex("DATA", "maxCorrelationTime");
		syncFieldIndex("DATA", "inSightsTime");
		syncFieldIndex("DATA", "inSightsTimeX");
	}
}
