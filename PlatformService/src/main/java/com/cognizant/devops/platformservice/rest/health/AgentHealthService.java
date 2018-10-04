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
package com.cognizant.devops.platformservice.rest.health;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/agent")
public class AgentHealthService {
	static Logger log = Logger.getLogger(AgentHealthService.class.getName());
	
	@RequestMapping(value = "/globalHealth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadAllAgentsHealth(){
		StringBuffer label = new StringBuffer(":HEALTH:LATEST");
		return loadHealthData(label.toString());
	}
	
	@RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadAgentsHealth(@RequestParam String category, @RequestParam String tool){
		if(StringUtils.isEmpty(category) || StringUtils.isEmpty(tool)){
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
		}
		log.debug(" message tool name "+category+"  "+tool);
		StringBuffer label = new StringBuffer(":HEALTH");
		if(category.equalsIgnoreCase(ServiceStatusConstants.PlatformEngine)) {
			label.append(":").append("ENGINE");
		}else if(category.equalsIgnoreCase(ServiceStatusConstants.InsightsInference)) {
			label.append(":").append("INSIGHTS");
		}else {
			label.append(":").append(category);
			label.append(":").append(tool);	
		}
		return loadHealthData(label.toString());
	}

	private JsonObject loadHealthData(String label) {
		String query = "MATCH (n"+label+") where n.inSightsTime IS NOT NULL return n order by n.inSightsTime DESC limit 100";
		try { 
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			GraphResponse response = dbHandler.executeCypherQuery(query);
			return PlatformServiceUtil.buildSuccessResponseWithData(response);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
	}
}
