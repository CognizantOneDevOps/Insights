package com.cognizant.devops.platformengine.modules.correlation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
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
		updateGitNodesWithJiraKey();
		correlateGitAndJira();
		correlateGitAndJenkins();
	}
	
	private void updateGitNodesWithJiraKey() {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			String paginationCypher = "MATCH (n:DATA:GIT:RAW) return count(n) as count";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(paginationCypher);
			int resultCount = paginationResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsInt();
			while(resultCount > 0) {
				String gitDataFetchCypher = "MATCH (source:DATA:GIT:RAW) WITH { uuid: source.uuid, commitId: source.commitId, message: source.message} as data limit 2000 return collect(data)";
				GraphResponse response = dbHandler.executeCypherQuery(gitDataFetchCypher);
				JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
				if(rows.isJsonNull() || rows.size() == 0) {
					return;
				}
				JsonArray dataList = rows.get(0).getAsJsonArray();
				String addJiraKeysCypher = "UNWIND {props} as properties MATCH (source:GIT:DATA:RAW {uuid : properties.uuid, commitId: properties.commitId}) "
						+ "set source.jiraKeys = properties.jiraKeys set source.jiraRelAdded = false return count(source)";
				String updateRawLabelCypher = "UNWIND {props} as properties MATCH (source:GIT:DATA:RAW {uuid : properties.uuid, commitId: properties.commitId}) "
						+ "remove source:RAW return count(source)";
				List<JsonObject> jiraKeysCypherProps = new ArrayList<JsonObject>();
				List<JsonObject> updateRawLabelCypherProps = new ArrayList<JsonObject>();
				for(JsonElement dataElem : dataList) {
					JsonObject dataJson = dataElem.getAsJsonObject();
					JsonElement messageElem = dataJson.get("message");
					if(messageElem.isJsonPrimitive()) {
						String message = messageElem.getAsString();
						JsonArray jiraKeys = new JsonArray();
						while(message.contains("-")) {
							Matcher m = p.matcher(message);
							if(m.find()) {
								jiraKeys.add(m.group());
								message = message.replaceAll(m.group(), "");
							}else {
								break;
							}
						}
						StringBuffer gitUpdateCypher = new StringBuffer();
						gitUpdateCypher.append("MATCH (source:DATA:GIT:RAW { uuid:").append(dataJson.get("uuid").getAsString())
						.append(", commitId:").append(dataJson.get("commitId").getAsString()).append("})").append("\n");
						JsonObject data = new JsonObject();
						data.addProperty("uuid", dataJson.get("uuid").getAsString());
						data.addProperty("commitId", dataJson.get("commitId").getAsString());
						if(jiraKeys.size() > 0) {
							data.add("jiraKeys", jiraKeys);
							jiraKeysCypherProps.add(data);
						}else {
							updateRawLabelCypherProps.add(data);
						}
					}
				}
				if(updateRawLabelCypherProps.size() > 0) {
					JsonObject bulkCreateNodes = dbHandler.bulkCreateNodes(updateRawLabelCypherProps, null, updateRawLabelCypher);
					log.debug(bulkCreateNodes);
				}
				if(jiraKeysCypherProps.size() > 0) {
					JsonObject bulkCreateNodes = dbHandler.bulkCreateNodes(jiraKeysCypherProps, null, addJiraKeysCypher);
					log.debug(bulkCreateNodes);
				}
				resultCount = resultCount - 500;
			}
		} catch (GraphDBException e) {
			log.error(e);
		}
	}
	
	private void correlateGitAndJira() {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			String paginationCypher = "MATCH (source:DATA:GIT:RAW) where exists(source.jiraKeys) return count(source) as count";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(paginationCypher);
			int resultCount = paginationResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsInt();
			while(resultCount > 0) {
				String gitDataFetchCypher = "MATCH (source:DATA:GIT:RAW) where exists(source.jiraKeys) "
						+ "WITH { uuid: source.uuid, commitId: source.commitId, jiraKeys: source.jiraKeys} as data limit 500 return collect(data)";
				GraphResponse response = dbHandler.executeCypherQuery(gitDataFetchCypher);
				JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
				if(rows.isJsonNull() || rows.size() == 0) {
					return;
				}
				JsonArray dataJsonArray = rows.get(0).getAsJsonArray();
				String gitToJiraCorrelationCypher = "UNWIND {props} as properties "
						+ "MATCH (source:DATA:GIT:RAW { uuid: properties.uuid, commitId: properties.commitId}) "
						+ "MATCH (destination:JIRA:DATA) where destination.key IN properties.jiraKeys "
						+ "CREATE (source) -[r:GIT_COMMIT_WITH_JIRA_KEY]-> (destination) "
						+ "remove source:RAW ";
				List<JsonObject> dataList = new ArrayList<JsonObject>();
				for(JsonElement data : dataJsonArray) {
					dataList.add(data.getAsJsonObject());
				}

				JsonObject bulkCreateNodesResponse = dbHandler.bulkCreateNodes(dataList, null, gitToJiraCorrelationCypher);
				log.debug("GIT-JIRA correlations executed. "+bulkCreateNodesResponse);
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
	
	public static void main(String[] args) {
		ApplicationConfigCache.loadConfigCache();
		CustomCorrelations cor = new CustomCorrelations();
		cor.correlateGitAndJira();
	}
}
 