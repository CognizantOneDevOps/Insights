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
package com.cognizant.devops.platformcommons.core.util;

import java.util.List;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonObject;

public  class SystemStatus {
	private static Logger log = Logger.getLogger(SystemStatus.class.getName());
	
	public static JsonObject addSystemInformationInNeo4j(String version,List<JsonObject> dataList,List<String> labels) {
		Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
		boolean status = Boolean.TRUE;
		JsonObject response=null;
		try {
			String queryLabel = "";
			for (String label : labels) {
				if (label != null && label.trim().length() > 0) {
					queryLabel += ":" + label;
				}
			}
	
			String cypherQuery = "CREATE (n" + queryLabel + " {props} ) return count(n)";
	
			response = graphDBHandler.executeQueryWithData(cypherQuery,dataList);
			log.info("  GraphDB response created " + response);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
	}

}
