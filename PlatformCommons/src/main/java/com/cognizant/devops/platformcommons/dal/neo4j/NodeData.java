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

public class NodeData {

	private Map<String, String> propertyMap = new HashMap<>();
	private Map<String, NodeData> relationMap = new HashMap<>();
	private List<String> labels = new ArrayList<>();
	
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
	public void setPropertyMap(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}
	
	public void setProperty(String name, String value){
		propertyMap.put(name, value);
	}
	
	public String getProperty (String name){
		return propertyMap.get(name);
	}
	
	public Map<String, NodeData> getRelationMap() {
		return relationMap;
	}
	public void setRelationMap(Map<String, NodeData> relationMap) {
		this.relationMap = relationMap;
	}
	public List<String> getLabels() {
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
}
