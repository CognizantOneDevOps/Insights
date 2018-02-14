/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.agentmanagement.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Service("agentManagementService")
public class AgentManagementServiceImpl  implements AgentManagementService{
	private static Logger log = Logger.getLogger(AgentManagementServiceImpl.class);

	@Override
	public String registerAgent(String configDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String installAgent(String agentId, String toolName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String startStopAgent(String agentId, String action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateAgent(String agentId, String configDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject getAgentDetails() {
		
		Map<String,ArrayList<String>>  agentDetails = new HashMap<String,ArrayList<String>>();
		String url = ApplicationConfigProvider.getInstance().getDocrootUrl();
		JsonObject details = new JsonObject();
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Elements rows = doc.getElementsByTag("a");
			for (Element element : rows) {
				if( element.text().startsWith("v")){
					String version = StringUtils.stripEnd(element.text(),"/");
					ArrayList<String> toolJson = getAgents(version);
					agentDetails.put(version, toolJson);
				}
			}
		} catch (IOException e) {
			log.debug(e);
		}
		details.add("details", new Gson().toJsonTree(agentDetails));
		return details;
	}

	private ArrayList<String> getAgents(String string) {
		
		ArrayList<String> tools = new ArrayList<String>();
		tools.add("GIT");
		tools.add("JIRA");
		tools.add("JENKINS");
		return tools;
	}

}
