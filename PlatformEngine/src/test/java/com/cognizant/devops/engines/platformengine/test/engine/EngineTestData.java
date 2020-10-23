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
package com.cognizant.devops.engines.platformengine.test.engine;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.MessageQueueDataModel;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
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

public class EngineTestData {
	private static Logger log = LogManager.getLogger(EngineTestData.class);
	public static String gitConfig = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SCM.GIT.config\",\"agentCtrlQueue\":\"GITTEST8800\"},\"publish\":{\"data\":\"SCM.GIT_UNTEST.DATA\",\"health\":\"SCM.GIT_UNTEST.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"sha\":\"commitId\",\"commit\":{\"message\":\"message\",\"author\":{\"name\":\"authorName\",\"date\":\"commitTime\"}}}},\"agentId\":\"GITTEST8800\",\"enableBranches\":false,\"enableBrancheDeletion\":false,\"enableDataValidation\":true,\"toolCategory\":\"SCM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"enableValueArray\":false,\"useResponseTemplate\":true,\"auth\":\"base64\",\"runSchedule\":30,\"timeStampField\":\"commitTime\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"startFrom\":\"2019-03-01 15:46:33\",\"accessToken\":\"56\",\"getRepos\":\"https://api.github.com/users/SanketCTSI/repos\",\"commitsBaseEndPoint\":\"https://api.github.com/repos/InsightsCTSI/\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v6.0\",\"toolName\":\"GIT\"}";
	public static String jenkinsConfig = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"CI.JENKINS.config\",\"agentCtrlQueue\":\"JENKINSTEST8800\"},\"publish\":{\"data\":\"CI.JENKINS_UNTEST.DATA\",\"health\":\"CI.JENKINS_UNTEST.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"actions\":[{\"causes\":[{\"shortDescription\":\"shortDescription\"}]},{\"remoteUrls\":[\"scmUrl\"]},{\"url\":\"sonarUrl\"}],\"changeSet\":{\"items\":[{\"commitId\":\"scmCommitId\",\"author\":{\"fullName\":\"scmAuthor\"},\"date\":\"buildDate\"}],\"kind\":\"scmKind\"},\"duration\":\"duration\",\"id\":\"buildNumber\",\"number\":\"number\",\"result\":\"result\",\"timestamp\":\"buildTimestamp\",\"url\":\"buildUrl\"}},\"jobDetails\":{\"rundeckJobId\":\"maven2-moduleset/publishers/org.jenkinsci.plugins.rundeck.RundeckNotifier/jobId\",\"scmRemoteUrl\":\"maven2-moduleset/scm/userRemoteConfigs/hudson.plugins.git.UserRemoteConfig/url\",\"nexusRepoUrl\":\"maven2-moduleset/publishers/hudson.maven.RedeployPublisher/url\",\"groupId\":\"maven2-moduleset/rootModule/groupId\",\"artifactId\":\"maven2-moduleset/rootModule/artifactId\"},\"agentId\":\"JENKINSTEST8800\",\"toolCategory\":\"CI\",\"toolsTimeZone\":\"Asia/Kolkata\",\"enableDataValidation\":true,\"useResponseTemplate\":true,\"useAllBuildsApi\":true,\"isDebugAllowed\":false,\"enableValueArray\":false,\"userid\":\"username\",\"passwd\":\"123\",\"runSchedule\":30,\"baseUrl\":\"http://127.0.0.1:8080/\",\"jenkinsMasters\":{\"master1\":\"http://127.0.0.1:8080/\",\"master2\":\"http://127.0.0.1:8080/\"},\"timeStampField\":\"buildTimestamp\",\"timeStampFormat\":\"epoch\",\"isEpochTimeFormat\":true,\"startFrom\":\"2019-03-01 15:46:33\",\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v6.0\",\"toolName\":\"JENKINS\"}";
	public static String rabbitMQGITTestPlayload = "{\"data\": [{\"inSightsTime\": 1539497182, \"execId\": \"5f84de70-ee48-11e9-b674-020c9a7827b8\", \"categoryName\": \"SCM\", \"jiraKey\": \"LS-7345942999\", \"toolName\": \"GIT\", \"commitId\": \"CM-7569369619\", \"inSightsTimeX\": \"2018-10-14T06:06:22Z\", \"gitCommitId\": \"2YW2jPX3ve45xWaTjjPxUC2lS9D7d0bK\", \"gitAuthorName\": \"Mayank\", \"gitReponame\": \"InsightsTest\", \"gitCommiTime\": \"2018-10-14T06:06:22Z\", \"message\": \"This commit is associated with jira-key : LS-7345942999\", \"repoName\": \"InsightsTest\"}], \"metadata\": {\"labels\": [\"GIT_UNTEST\"]}}";
	public static String rabbitMQJENKINSTestPayload = "{\"data\": [{\"inSightsTime\": 1539497302, \"status\": \"Success\", \"execId\": \"5f84de70-ee48-11e9-b674-020c9a7827b8\", \"scmcommitId\": \"CM-7569369619\", \"categoryName\": \"CI\", \"environment\": \"RELEASE\", \"toolName\": \"JENKINS\", \"projectName\": \"PaymentServices\", \"jobName\": \"BillingApproved\", \"buildNumber\": \"9046453184\", \"inSightsTimeX\": \"2018-10-14T06:08:22Z\", \"buildUrl\": \"productv4.3.devops.com\", \"master\": \"master2\", \"result\": \"ABORTED\", \"startTime\": \"2018-10-14T06:08:22Z\", \"projectID\": \"1001\", \"duration\": 129, \"endTime\": \"2018-10-14T06:10:31Z\", \"jenkins_date\": \"2018-10-14 06:08:22\"}], \"metadata\": {\"labels\": [\"JENKINS_UNTEST\"]}}";
	public static String gitConsumerTag = "SCM_GIT_DATA";
	public static String gitQueueName = "SCM_GIT_DATA";
	public static String gitRoutingKey = "SCM.GIT_UNTEST.DATA";
	public static String jenkinConsumerTag = "CI_JENKINS_DATA";
	public static String jenkinQueueName = "CI_JENKINS_DATA";
	public static String jenkinsRoutingKey = "CI.JENKINS_UNTEST.DATA";
	public static Boolean dataUpdateSupported = false;
	public static String osversion = "windows";
	public static Date updateDate = Timestamp.valueOf(LocalDateTime.now());
	public static String agentVersion = "v6.0";
	public static String gitToolCategory = "SCM";
	public static String jenkinToolCategory = "CI";
	public static String gitLabelName = "GIT_UNTEST";
	public static String jenkinLabelName = "JENKINS_UNTEST";
	public static String saveDataConfig = "{\"destination\":{\"toolName\":\"JENKINS\",\"toolCategory\":\"CI\",\"labelName\":\"JENKINS_UNTEST\",\"fields\":[\"scmcommitId\"]},\"source\":{\"toolName\":\"GIT\",\"toolCategory\":\"SCM\",\"labelName\":\"GIT_UNTEST\",\"fields\":[\"commitId\"]},\"relationName\":\"TEST_FROM_GIT_TO_JENKINS\",\"relationship_properties\":[],\"isSelfRelation\":false}";

	public static JsonObject getJsonObject(String jsonString) {
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(jsonString.trim(), JsonElement.class);
		return jelement.getAsJsonObject();
	}

	public static void publishMessage(String queueName, String routingKey, String playload) {
	
		ConnectionFactory factory = new ConnectionFactory();
		MessageQueueDataModel messageQueueConfig = ApplicationConfigProvider.getInstance().getMessageQueue();
		factory.setHost(messageQueueConfig.getHost());
		factory.setUsername(messageQueueConfig.getUser());
		factory.setPassword(messageQueueConfig.getPassword());
		String exchangeName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentExchange();

		Connection connection = null;
		Channel channel = null;
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
		} catch (IOException e) {
			log.error(e);
		} catch (TimeoutException e) {
			log.error(e);
		}

		String message = new GsonBuilder().disableHtmlEscaping().create().toJson(playload);
		message = message.substring(1, message.length() - 1).replace("\\", "");
		try {
			DeclareOk exchangeResp = channel.exchangeDeclarePassive(exchangeName);
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
				log.error(e1);
			}

		}

	}
	
	public static Map readNeo4JData(String nodeName , String compareFlag)
	{
		Map map=null;
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:"+nodeName+") where n."+compareFlag+"='CM-7569369619' return n";
		log.debug(" query  {} ", query);
		GraphResponse neo4jResponse;
		//JsonArray parentArray = new JsonArray();
		try {
		
		neo4jResponse = dbHandler.executeCypherQuery(query);

		JsonElement tooldataObject = neo4jResponse.getJson().get("results")
					                        .getAsJsonArray().get(0)
					                        .getAsJsonObject().get("data")
					                        .getAsJsonArray()
					                        .get(0).getAsJsonObject()
					                        .get("row");			

		String finalJson = tooldataObject.toString().replace("[", "").replace("]", "");
		
		Gson gson = new Gson();

	    map = gson.fromJson(finalJson, Map.class);
		}
		catch (InsightsCustomException e) {
			log.error(e);
		} 
		return map;				
	  
	}
	
	public static CorrelationConfiguration loadCorrelation(String config) {
		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(config);
		CorrelationConfiguration correlationConfig = new CorrelationConfiguration();
		CorrelationJson correlation = new Gson().fromJson(json, CorrelationJson.class);

		correlationConfig.setSourceToolName(correlation.getSource().getToolName());
		correlationConfig.setSourceToolCategory(correlation.getSource().getToolCategory());
		if (null == correlation.getSource().getLabelName()) {
			correlationConfig.setSourceLabelName(correlation.getSource().getToolName());
		} else {
			correlationConfig.setSourceLabelName(correlation.getSource().getLabelName());
		}
		correlationConfig.setSourceFields(String.join(",", correlation.getSource().getFields()));
		correlationConfig.setDestinationToolName(correlation.getDestination().getToolName());
		correlationConfig.setDestinationToolCategory(correlation.getDestination().getToolCategory());
		correlationConfig.setDestinationLabelName(correlation.getDestination().getLabelName());
		if (null == correlation.getDestination().getLabelName()) {
			correlationConfig.setDestinationLabelName(correlation.getDestination().getToolName());
		} else {
			correlationConfig.setDestinationLabelName(correlation.getDestination().getLabelName());
		}
		correlationConfig.setDestinationFields(String.join(",", correlation.getDestination().getFields()));
		correlationConfig.setRelationName(correlation.getRelationName());
		if (correlation.getPropertyList().length > 0) {
			correlationConfig.setPropertyList(String.join(",", correlation.getPropertyList()));
		}
		correlationConfig.setEnableCorrelation(correlation.isEnableCorrelation());
		correlationConfig.setSelfRelation(correlation.isSelfRelation());

		return correlationConfig;
	}

}
