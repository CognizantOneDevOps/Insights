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
package com.cognizant.devops.platforminsights.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.SystemStatus;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonObject;

public class InsightsStatusProvider  {
	private static Logger log = Logger.getLogger(InsightsStatusProvider.class);
	Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
	private static final String DATE_TIME_FORMAT = "yyyy/MM/dd hh:mm a";
	private static SimpleDateFormat  dtf = new SimpleDateFormat(DATE_TIME_FORMAT);
	
	public static boolean createInsightStatusNode(String message,String status) {
			String version = "";
			version = InsightsStatusProvider.class.getPackage().getImplementationVersion();
			log.debug( " Insights version " +  version    );
			List<JsonObject> dataList = new ArrayList<JsonObject>();
			List<String> labels = new ArrayList<String>();
			labels.add("HEALTH");
			labels.add("INSIGHTS");
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("version", version==null?"-":version);
			jsonObj.addProperty("message", message);
			jsonObj.addProperty("inSightsTime",System.currentTimeMillis());
			jsonObj.addProperty("inSightsTimeX", dtf.format(new Date()));
			jsonObj.addProperty(PlatformServiceConstants.STATUS,status);
			dataList.add(jsonObj);
			JsonObject response=SystemStatus.addSystemInformationInNeo4j(version, dataList, labels);
			return Boolean.TRUE;
	}
}
