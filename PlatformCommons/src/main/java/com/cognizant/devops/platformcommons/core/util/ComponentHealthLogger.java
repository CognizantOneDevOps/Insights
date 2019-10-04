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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.google.gson.JsonObject;

public abstract class ComponentHealthLogger {
	
	private static final Logger log = LogManager.getLogger(ComponentHealthLogger.class);
	private  static final String TIMEZONE = "GMT";
	
	public boolean createComponentStatusNode(String label,String version,String message,String status,Map<String,String> parameter){
		JsonObject response = null;
		try {
			String utcdate = InsightsUtils.getUtcTime(TIMEZONE);
			List<JsonObject> dataList = new ArrayList<JsonObject>();
			List<String> labels = new ArrayList<String>();
			labels.addAll(Arrays.asList(label.split(":")));
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("version", version==null?"-":version);
			jsonObj.addProperty("message", message);
			jsonObj.addProperty("inSightsTime",System.currentTimeMillis());
			jsonObj.addProperty("inSightsTimeX", utcdate);
			jsonObj.addProperty(PlatformServiceConstants.STATUS,status);
			for (Map.Entry<String,String> entry: parameter.entrySet()){
				jsonObj.addProperty(entry.getKey(), entry.getValue());
			}
			dataList.add(jsonObj);
			response = SystemStatus.addSystemInformationInNeo4j(version, dataList, labels);
		} catch (Exception e) {
			log.error("Unable to create Node "+e.getMessage());
		}
		if (response!=null) {
			return Boolean.TRUE;
		}else {
			return Boolean.FALSE;
		}
	}
}
