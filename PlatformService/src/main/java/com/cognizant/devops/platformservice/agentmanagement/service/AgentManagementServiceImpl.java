package com.cognizant.devops.platformservice.agentmanagement.service;

import java.io.IOException;
import java.util.ArrayList;

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
		
		String url=ApplicationConfigProvider.getInstance().getDocrootUrl();
		ArrayList<String> versions=new ArrayList<String>();
		JsonObject details=new JsonObject();
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Elements rows = doc.getElementsByTag("a");
			for (Element element : rows) {
				if( element.text().startsWith("v")){
					//System.out.println(element.text());
					versions.add(StringUtils.stripEnd(element.text(),"/"));
				}
			}
		} catch (IOException e) {
			log.debug(e);
		}
		String versionJson = new Gson().toJson(versions);
		String toolJson = getAgents();
		
		details.add("versions", new Gson().toJsonTree(versionJson));
		details.add("tools", new Gson().toJsonTree(toolJson));
		
		return details;
	}

	private String getAgents() {
		ArrayList<String> tools=new ArrayList<String>();
		tools.add("GIT");
		tools.add("JIRA");
		tools.add("JENKINS");
		
		String toolJson =new Gson().toJson(tools);
		return toolJson;
	}

}
