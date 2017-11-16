package com.cognizant.devops.platformengine.modules.correlation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CustomCorrelations {
	private static Logger log = Logger.getLogger(CustomCorrelations.class);
	private static final Pattern p = Pattern.compile("((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)");
	
	public void executeCorrelations() {
		correlateGitAndJira();
		correlateGitAndJenkins();
	}
	
	private void correlateGitAndJira() {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			String paginationCypher = "MATCH (source:DATA:GIT) where not ((source) -[:GIT_COMMIT_WITH_JIRA_KEY]-> (:JIRA:DATA)) return count(source) as count";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(paginationCypher);
			int resultCount = paginationResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsInt();
			while(resultCount > 0) {
				String gitDataFetchCypher = "MATCH (source:DATA:GIT) where not ((source) -[:GIT_COMMIT_WITH_JIRA_KEY]-> (:JIRA:DATA)) WITH { uuid: source.uuid, commitId: source.commitId, message: source.message} as data limit 500 return collect(data)";
				GraphResponse response = dbHandler.executeCypherQuery(gitDataFetchCypher);
				JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
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
						List<String> jiraKeys = new ArrayList<String>();
						while(message.contains("-")) {
							Matcher m = p.matcher(message);
							if(m.find()) {
								jiraKeys.add(m.group());
								message = message.replaceAll(m.group(), "");
							}else {
								break;
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
							correlationCypher.append("CREATE (source) -[r:GIT_COMMIT_WITH_JIRA_KEY]-> (destination)");
							correlationCyphers.add(correlationCypher.toString());
						}
					}
				}
				if(correlationCyphers.size() > 0) {
					dbHandler.bulkCreateCorrelations(correlationCyphers);
					log.debug("GIT-JIRA correlations executed: "+correlationCyphers.size());
				}
				resultCount = resultCount - 500;
			}
		} catch (GraphDBException e) {
			log.error(e);
		}
	}
	
	
	private void correlateGitAndJenkins() {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			String paginationCypher = "MATCH (source:DATA:JENKINS) where not ((source) <-[:JENKINS_TRIGGERED_BY_GIT_COMMIT]- (:GIT:DATA)) AND "
					+ "(exists(source.lastBuiltRevision) OR exists(source.scmCommitId))  return count(distinct source) as count";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(paginationCypher);
			int resultCount = paginationResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsInt();
			while(resultCount > 0) {
				String gitDataFetchCypher = "MATCH (source:DATA:JENKINS) where not ((source) <-[:JENKINS_TRIGGERED_BY_GIT_COMMIT]- (:GIT:DATA)) "
						+ "AND (exists(source.lastBuiltRevision) OR exists(source.scmCommitId))  WITH source limit 500 "
						+ "WITH source, coalesce(source.lastBuiltRevision, \"\") + \" \" + coalesce(source.scmCommitId, \"\") as commitId  "
						+ "WITH source.uuid as uuid, split(commitId, \",\") as commits unwind commits as commit WITH uuid, commit where commit is not null "
						+ "WITH distinct uuid, collect(distinct commit) as commits WITH { uuid : uuid, commits: commits} as data "
						+ "return collect(data) as data";
				//Need to update the query to give commit ids as string array.
				GraphResponse response = dbHandler.executeCypherQuery(gitDataFetchCypher);
				JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
				if(rows.isJsonNull() || rows.size() == 0) {
					return;
				}
				JsonArray dataArray = rows.get(0).getAsJsonArray();
				List<String> correlationCyphers = new ArrayList<String>();
				for(JsonElement dataElem : dataArray) {
					JsonObject dataJson = dataElem.getAsJsonObject();
					JsonArray commits = dataJson.get("commits").getAsJsonArray();
					StringBuffer correlationCypher = new StringBuffer();
					correlationCypher.append("MATCH (source:JENKINS { uuid:").append(dataJson.get("uuid").toString()).append("})").append("\n");
					correlationCypher.append("MATCH (destination:GIT) where destination.commitId IN ").append(commits.toString().replaceAll(" ", "")).append("\n");
					correlationCypher.append("CREATE (source) <-[r:JENKINS_TRIGGERED_BY_GIT_COMMIT]- (destination)");
					correlationCyphers.add(correlationCypher.toString());
				}
				if(correlationCyphers.size() > 0) {
					dbHandler.bulkCreateCorrelations(correlationCyphers);
					log.debug("GIT-Jenkins correlations executed: "+correlationCyphers.size());
				}
				resultCount = resultCount - 500;
			}
		} catch (GraphDBException e) {
			log.error(e);
		}
	}
}
 