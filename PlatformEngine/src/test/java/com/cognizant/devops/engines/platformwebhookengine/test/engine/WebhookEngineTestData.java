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
package com.cognizant.devops.engines.platformwebhookengine.test.engine;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.MessageQueueDataModel;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class WebhookEngineTestData {
	
	/*
	 * Webhook test data
	 */
	private static Logger LOG = LogManager.getLogger(WebhookEngineTestData.class);
	JsonParser parser = new JsonParser();
	public String responseTemplate = "head_commit.message=message,head_commit.timestamp=commitTime,repository.updated_at=updated_at,repository.created_at=created_at,repository.pushed_at=pushed_at,head_commit.id=commitId,head_commit.author.name=authorName";
	public String dynamicTemplate = "{\"ref\":\"refGIT\",\"repository\":{\"name\":\"repositoryName\"},\"commits\":[{\"id\":\"commitIdDY\",\"url\":\"commitURLDY\",\"timestamp\":\"commitTimeDY\"}]}";
	public String toolName = "GIT";
	public String labelName = "SCM:GIT76:DATA";
	public String dataFormat = "JSON";
	public final String WEBHOOK_SUBSCRIBER_HEALTH_QUEUE = "WEBHOOKSUBSCRIBER_HEALTH";
	public final String WEBHOOK_SUBSCRIBER_HEALTH_ROUTING_KEY = WEBHOOK_SUBSCRIBER_HEALTH_QUEUE.replace("_", ".");
	public String healthMessageInstanceName = "localWebhookApp_PlatformInsightsWebHook_testData";
	public String mqChannel = "IPW_git_demo";
	public String webhookName = "git_demo";
	public String webhookNameException = "git_testException";
	public Boolean isUpdateRequired = Boolean.FALSE;
	public String fieldUsedForUpdate = "commitId";
	public DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	public LocalDateTime now = LocalDateTime.now();
	public String commitId = "8ea6c42b96d5c0ffdaf3622720450e2d5def75e6";
	public String authorName = "\"Insights_test\"";
	public String message = "\"ITD-215 testing of webhook\"";
	public String timestamp = "\"2020-03-11T18:17:37+05:30\"";
	public String fieldNotFoundinToolData = "head_commit.newid=commitId,head_commit.newmessage=message";
	public String toolData = "{\"ref\":\"refs/heads/master\",\"before\":\"bab6047988c06c10c36e8224d8b03b26db8e9b18\",\"after\":\"8ea6c42b96d5c0ffdaf3622720450e2d5def75e6\",\"repository\":{\"id\":141991164,\"node_id\":\"MDEwOlJlcG9zaXRvcnkxNDE5OTExNjQ=\",\"name\":\"insightTest\",\"full_name\":\"Insights_test/insightTest\",\"private\":false,\"owner\":{\"name\":\"Insights_test\",\"email\":\"41290302+Insights_test@users.noreply.github.com\",\"login\":\"Insights_test\",\"id\":41290302,\"node_id\":\"MDQ6VXNlcjQxMjkwMzAy\",\"avatar_url\":\"https://avatars0.githubusercontent.com/u/41290302?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/Insights_test\",\"html_url\":\"https://github.com/Insights_test\",\"followers_url\":\"https://api.github.com/users/Insights_test/followers\",\"following_url\":\"https://api.github.com/users/Insights_test/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/Insights_test/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/Insights_test/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/Insights_test/subscriptions\",\"organizations_url\":\"https://api.github.com/users/Insights_test/orgs\",\"repos_url\":\"https://api.github.com/users/Insights_test/repos\",\"events_url\":\"https://api.github.com/users/Insights_test/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/Insights_test/received_events\",\"type\":\"User\",\"site_admin\":false},\"html_url\":\"https://github.com/Insights_test/insightTest\",\"description\":\"insightTest\",\"fork\":false,\"url\":\"https://github.com/Insights_test/insightTest\",\"forks_url\":\"https://api.github.com/repos/Insights_test/insightTest/forks\",\"keys_url\":\"https://api.github.com/repos/Insights_test/insightTest/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/Insights_test/insightTest/collaborators{/collaborator}\",\"teams_url\":\"https://api.github.com/repos/Insights_test/insightTest/teams\",\"hooks_url\":\"https://api.github.com/repos/Insights_test/insightTest/hooks\",\"issue_events_url\":\"https://api.github.com/repos/Insights_test/insightTest/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/Insights_test/insightTest/events\",\"assignees_url\":\"https://api.github.com/repos/Insights_test/insightTest/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/Insights_test/insightTest/branches{/branch}\",\"tags_url\":\"https://api.github.com/repos/Insights_test/insightTest/tags\",\"blobs_url\":\"https://api.github.com/repos/Insights_test/insightTest/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/Insights_test/insightTest/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/Insights_test/insightTest/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/Insights_test/insightTest/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/Insights_test/insightTest/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/Insights_test/insightTest/languages\",\"stargazers_url\":\"https://api.github.com/repos/Insights_test/insightTest/stargazers\",\"contributors_url\":\"https://api.github.com/repos/Insights_test/insightTest/contributors\",\"subscribers_url\":\"https://api.github.com/repos/Insights_test/insightTest/subscribers\",\"subscription_url\":\"https://api.github.com/repos/Insights_test/insightTest/subscription\",\"commits_url\":\"https://api.github.com/repos/Insights_test/insightTest/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/Insights_test/insightTest/git/commits{/sha}\",\"comments_url\":\"https://api.github.com/repos/Insights_test/insightTest/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/Insights_test/insightTest/issues/comments{/number}\",\"contents_url\":\"https://api.github.com/repos/Insights_test/insightTest/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/Insights_test/insightTest/compare/{base}...{head}\",\"merges_url\":\"https://api.github.com/repos/Insights_test/insightTest/merges\",\"archive_url\":\"https://api.github.com/repos/Insights_test/insightTest/{archive_format}{/ref}\",\"downloads_url\":\"https://api.github.com/repos/Insights_test/insightTest/downloads\",\"issues_url\":\"https://api.github.com/repos/Insights_test/insightTest/issues{/number}\",\"pulls_url\":\"https://api.github.com/repos/Insights_test/insightTest/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/Insights_test/insightTest/milestones{/number}\",\"notifications_url\":\"https://api.github.com/repos/Insights_test/insightTest/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/Insights_test/insightTest/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/Insights_test/insightTest/releases{/id}\",\"deployments_url\":\"https://api.github.com/repos/Insights_test/insightTest/deployments\",\"created_at\":1532337586,\"updated_at\":\"2020-03-11T09:02:04Z\",\"pushed_at\":1583930857,\"git_url\":\"git://github.com/Insights_test/insightTest.git\",\"ssh_url\":\"git@github.com:Insights_test/insightTest.git\",\"clone_url\":\"https://github.com/Insights_test/insightTest.git\",\"svn_url\":\"https://github.com/Insights_test/insightTest\",\"homepage\":null,\"size\":62,\"stargazers_count\":0,\"watchers_count\":0,\"language\":\"Python\",\"has_issues\":true,\"has_projects\":true,\"has_downloads\":true,\"has_wiki\":true,\"has_pages\":false,\"forks_count\":0,\"mirror_url\":null,\"archived\":false,\"disabled\":false,\"open_issues_count\":0,\"license\":null,\"forks\":0,\"open_issues\":0,\"watchers\":0,\"default_branch\":\"master\",\"stargazers\":0,\"master_branch\":\"master\"},\"pusher\":{\"name\":\"Insights_test\",\"email\":\"41290302+Insights_test@users.noreply.github.com\"},\"sender\":{\"login\":\"Insights_test\",\"id\":41290302,\"node_id\":\"MDQ6VXNlcjQxMjkwMzAy\",\"avatar_url\":\"https://avatars0.githubusercontent.com/u/41290302?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/Insights_test\",\"html_url\":\"https://github.com/Insights_test\",\"followers_url\":\"https://api.github.com/users/Insights_test/followers\",\"following_url\":\"https://api.github.com/users/Insights_test/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/Insights_test/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/Insights_test/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/Insights_test/subscriptions\",\"organizations_url\":\"https://api.github.com/users/Insights_test/orgs\",\"repos_url\":\"https://api.github.com/users/Insights_test/repos\",\"events_url\":\"https://api.github.com/users/Insights_test/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/Insights_test/received_events\",\"type\":\"User\",\"site_admin\":false},\"created\":false,\"deleted\":false,\"forced\":false,\"base_ref\":null,\"compare\":\"https://github.com/Insights_test/insightTest/compare/bab6047988c0...8ea6c42b96d5\",\"commits\":[{\"id\":\"8ea6c42b96d5c0ffdaf3622720450e2d5def75e6\",\"tree_id\":\"32e78a8c0dfb0cf61d2c277ed53224e6ab73b92b\",\"distinct\":true,\"message\":\"ITD-215 testing of webhook\",\"timestamp\":\"2020-03-11T18:17:37+05:30\",\"url\":\"https://github.com/Insights_test/insightTest/commit/8ea6c42b96d5c0ffdaf3622720450e2d5def75e6\",\"author\":{\"name\":\"Insights_test\",\"email\":\"41290302+Insights_test@users.noreply.github.com\",\"username\":\"Insights_test\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[],\"removed\":[],\"modified\":[\"config.json\"]}],\"head_commit\":{\"id\":\"8ea6c42b96d5c0ffdaf3622720450e2d5def75e6\",\"tree_id\":\"32e78a8c0dfb0cf61d2c277ed53224e6ab73b92b\",\"distinct\":true,\"message\":\"ITD-215 testing of webhook\",\"timestamp\":\"2020-03-11T18:17:37+05:30\",\"url\":\"https://github.com/Insights_test/insightTest/commit/8ea6c42b96d5c0ffdaf3622720450e2d5def75e6\",\"author\":{\"name\":\"Insights_test\",\"email\":\"41290302+Insights_test@users.noreply.github.com\",\"username\":\"Insights_test\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[],\"removed\":[],\"modified\":[\"config.json\"]}}";
	public String expectedOutput = "{\"commitTime\":\"2019-09-16T04:52:25Z\",\"authorName\":\"Insights_test\",\"insightsTime\":"
			+ ZonedDateTime.now().toInstant().toEpochMilli()
			+ ",\"webhookName\":\"git_demo\",\"commitId\":\"86ef096bb924674a69cd2198e2964b76aa75d88b\",\"source\":\"webhook\",\"message\":\"Update Hello\",\"inSightsTimeX\":\""
			+ dtf.format(now) + "\",\"labelName\":\"SCM:GIT76:DATA\",\"toollName\":\"GIT\"}";
	String derivedOpsJson = "[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pushed_at\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"git_demo\"},{\"wid\":-1,\"operationName\":\"dataEnrichment\",\"operationFields\":{\"sourceProperty\":\"message\",\"keyPattern\":\"-\",\"targetProperty\":\"messageEnrichExtractwb2\"},\"webhookName\":\"git_demo\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"commitTime\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ssXXX\"},\"webhookName\":\"git_demo\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"updated_at\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ss'Z'\"},\"webhookName\":\"git_demo\"}]";

	String derivedOpsJsonWithoutEpoch = "[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"commitTime\",\"epochTime\":false,\"timeFormat\":\"yyyy-MM-dd'T'HH:mm:ssXXX\"},\"webhookName\":\"git_demo\"},{\"wid\":-1,\"operationName\":\"dataEnrichment\",\"operationFields\":{\"sourceProperty\":\"message\",\"keyPattern\":\"-\",\"targetProperty\":\"messageEnrichExtractwb2\"},\"webhookName\":\"git_demo\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"commitTime\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ssXXX\"},\"webhookName\":\"git_demo\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"updated_at\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ss'Z'\"},\"webhookName\":\"git_demo\"}]";

	public String derivedOpsJsonWithoutEpochPivotal = "[{\"wid\":220,\"operationName\":\"dataEnrichment\",\"operationFields\":{\"sourceProperty\":\"message\",\"keyPattern\":\"-\",\"targetProperty\":\"message\"},\"webhookName\":\"pivotal_test\"},{\"wid\":219,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"pivotalTime\",\"epochTime\":true,\"timeFormat\":\"\"},\"webhookName\":\"pivotal_test\"},{\"wid\":-1,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"created_at\",\"epochTime\":false,\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ssXXX\"},\"webhookName\":\"pivotal_test\"}]";

	public String healthWebhookData = "{\"version\":\"6.5-SNAPSHOT\",\"message\":\" Instance Name localWebhookApp_PlatformInsightsWebHook : Connection with Rabbit Mq for host localhost established successfully. \",\"inSightsTime\":1585721858499,\"inSightsTimeX\":\"2020-04-01T06:17:38Z\",\"instanceName\":\"localWebhookApp_PlatformInsightsWebHook_testData\",\"serverPort\":0,\"status\":\"success\"}";

	public Set<WebhookDerivedConfig> derivedOperationsArray = getderivedOperationsJSONArray(derivedOpsJson);

	public Set<WebhookDerivedConfig> derivedOperationsArrayWithoutEpoch = getderivedOperationsJSONArray(
			derivedOpsJsonWithoutEpoch);

	public Set<WebhookDerivedConfig> derivedOperationsArrayWithoutEpochPivotal = getderivedOperationsJSONArray(
			derivedOpsJsonWithoutEpochPivotal);

	private Set<WebhookDerivedConfig> getderivedOperationsJSONArray(String derivedOpsJsonLocal) {
		Set<WebhookDerivedConfig> setWebhookDerivedConfigs = new HashSet<WebhookDerivedConfig>();
		JsonArray array = new JsonArray();
		array = parser.parse(derivedOpsJsonLocal).getAsJsonArray();
		for (JsonElement webhookDerivedConfigJson : array) {
			WebhookDerivedConfig webhookDerivedConfig = new WebhookDerivedConfig();
			JsonObject receivedObject = webhookDerivedConfigJson.getAsJsonObject();
			int wid = receivedObject.get("wid").getAsInt();
			webhookDerivedConfig.setOperationName(receivedObject.get("operationName").getAsString());
			webhookDerivedConfig.setOperationFields(receivedObject.get("operationFields").toString());
			webhookDerivedConfig.setWebhookName(webhookName);
			if (wid != -1) {
				webhookDerivedConfig.setWid(wid);
			}
			setWebhookDerivedConfigs.add(webhookDerivedConfig);
		}
		return setWebhookDerivedConfigs;
	}

	
	
	public JsonObject getJsonObject(String jsonString) {
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(jsonString.trim(), JsonElement.class);
		return jelement.getAsJsonObject();
	}
	
	@SuppressWarnings("unused")
	public void publishMessage(String queueName, String routingKey, String playload)
			throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		MessageQueueDataModel messageQueueConfig = ApplicationConfigProvider.getInstance().getMessageQueue();
		factory.setHost(messageQueueConfig.getHost());
		factory.setUsername(messageQueueConfig.getUser());
		factory.setPassword(messageQueueConfig.getPassword());
		String exchangeName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentExchange();
		Connection connection = null;
		Channel channel = null;
		connection = factory.newConnection();
		channel = connection.createChannel();
		String message = new GsonBuilder().disableHtmlEscaping().create().toJson(playload);
		message = message.substring(1, message.length() - 1).replace("\\", "");
		DeclareOk exchangeResp;
		try {
			exchangeResp = channel.exchangeDeclarePassive(exchangeName);
			channel.queueDeclare(queueName, true, false, false, null);
			channel.queueBind(queueName, exchangeName, routingKey);
			channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
			connection.close();
		} catch (IOException e) {

			/*
			 * if exception raised means exchange doesen't exists creating
			 * exchange in next block
			 */

			try {
				channel.exchangeDeclare(exchangeName, MQMessageConstants.EXCHANGE_TYPE);
				channel.queueDeclare(queueName, true, false, false, null);
				channel.queueBind(queueName, exchangeName, routingKey);
				channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
				connection.close();
			} catch (IOException e1) {
				LOG.error(e1);
			}

		}

	}
	
	@SuppressWarnings("rawtypes")
	public static Map readNeo4JData(String nodeName , String compareFlag)
	{
	
		Map map=null;
		ObjectMapper mapper = new ObjectMapper();
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:" + nodeName + ") where n." + compareFlag
				+ "='8ea6c42b96d5c0ffdaf3622720450e2d5def75e6' return n";
		GraphResponse neo4jResponse;
		try {
		neo4jResponse = dbHandler.executeCypherQuery(query);
		JsonElement tooldataObject = neo4jResponse.getJson().get("results")
					                        .getAsJsonArray().get(0)
					                        .getAsJsonObject().get("data")
					                        .getAsJsonArray()
					                        .get(0).getAsJsonObject()
					                        .get("row");			

		String finalJson = tooldataObject.toString().replace("[", "").replace("]", "");
		//Gson gson = new Gson();
			//map = gson.fromJson(finalJson, Map.class); GraphDB
			map = mapper.readValue(finalJson, Map.class);
		} catch (Exception e) {
			LOG.error(e);
		} 
		return map;				
	}
	
}
