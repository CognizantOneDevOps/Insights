/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.dal;

import java.util.Map;

public class InsightsRelationShip {

	private String name;
	private Map<String, Object> propertyMap;
	private InsightsGraphNode startNode;
	private InsightsGraphNode endNode;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}
	public void setPropertyMap(Map<String, Object> propertyMap) {
		this.propertyMap = propertyMap;
	}
	public InsightsGraphNode getStartNode() {
		return startNode;
	}
	public void setStartNode(InsightsGraphNode startNode) {
		this.startNode = startNode;
	}
	public InsightsGraphNode getEndNode() {
		return endNode;
	}
	public void setEndNode(InsightsGraphNode endNode) {
		this.endNode = endNode;
	}
	
}
