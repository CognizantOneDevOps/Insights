package com.cognizant.devops.platformengine.modules.correlation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CustomCorrelations {
	private static Logger log = Logger.getLogger(CustomCorrelations.class);
	
	public void executeCorrelations() {
		correlateGitAndJira();
	}
	
	private void correlateGitAndJira() {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String gitDataFetchCypher = "MATCH (source:DATA:GIT) where not ((source) -[:GIT_COMMIT_WITH_JIRA_KEY]-> (:JIRA:DATA)) WITH { uuid: source.uuid, commitId: source.commitId, message: source.message} as data limit 500 return collect(data)";
		try {
			GraphResponse response = dbHandler.executeCypherQuery(gitDataFetchCypher);
			JsonObject json = response.getJson();
			JsonArray rows = json.get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
			if(rows.isJsonNull() || rows.size() == 0) {
				return;
			}
			JsonArray dataList = rows.get(0).getAsJsonArray();
			List<String> correlationCyphers = new ArrayList<String>();
			for(JsonElement dataElem : dataList) {
				JsonObject dataJson = dataElem.getAsJsonObject();
				JsonElement messageElem = dataJson.get("message");
				if(messageElem.isJsonPrimitive()) {
					String message = messageElem.getAsString();
					message = message.replaceAll("- ", "-");
					message = message.replaceAll(" -", "-");
					String[] tokens = message.split(" ");
					List<String> jiraKeys = new ArrayList<String>();
					for(String token : tokens) {
						if(token.contains("-")) {
							String[] subTokens = token.split("-");
							if(subTokens.length > 1) {
								try {
									int numPart = Integer.valueOf(subTokens[1]);
									jiraKeys.add(subTokens[0].trim()+"-"+numPart);
								}catch(Exception e) {
									log.warn("Unable to parse the message", e);
								}
							}
						}
					}
					if(jiraKeys.size() > 0) {
						JsonArray jiraKeyJson = new JsonArray();
						for(String key : jiraKeys) {
							jiraKeyJson.add(key);
						}
						//We need to add a time filter as well.
						StringBuffer correlationCypher = new StringBuffer();
						correlationCypher.append("MATCH (source:DATA:GIT { uuid:").append(dataJson.get("uuid").toString())
								.append(", commitId:").append(dataJson.get("commitId").toString()).append("})").append("\n");
						correlationCypher.append("MATCH (destination:JIRA:DATA) where destination.key IN ").append(jiraKeyJson.toString()).append("\n");
						correlationCypher.append("CREATE UNIQUE (source) -[r:GIT_COMMIT_WITH_JIRA_KEY]-> (destination)");
						correlationCyphers.add(correlationCypher.toString());
					}
				}
			}
			if(correlationCyphers.size() > 0) {
				JsonObject queryResponse = dbHandler.bulkCreateCorrelations(correlationCyphers);
			}
		} catch (GraphDBException e) {
			log.error(e);
		}
	}
}
