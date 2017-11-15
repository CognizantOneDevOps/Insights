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
import java.util.List;

import com.google.gson.JsonObject;

public class GraphResponse {
	private List<NodeData> nodes = new ArrayList<NodeData>();
	private JsonObject json;
	
	public boolean addNode(NodeData data){
		return nodes.add(data);
	}
	
	public boolean addAllNodes(List<NodeData> nodes){
		return this.nodes.addAll(nodes);
	}

	public List<NodeData> getNodes() {
		return nodes;
	}

	public JsonObject getJson() {
		return json;
	}

	public void setJson(JsonObject json) {
		this.json = json;
	}
}
