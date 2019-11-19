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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformauditing.blockchaindatacollection.modules.blockchainprocessing.JiraProcessingExecutor;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.MessageQueueDataModel;
import com.cognizant.devops.platformcommons.constants.MessageConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;

public class EngineTestData {
	
	/*
	 * Webhook test data
	 */
	private static Logger LOG = LogManager.getLogger(EngineTestData.class);

	public static String responseTemplate = "head_commit.id=commitId,head_commit.message=message,head_commit.timestamp=commitTime,head_commit.author.name=authorName";
	public static String toolName = "GIT";
	public static String labelName = "SCM:GIT76:DATA";
	public static String mqChannel = "IPW_git_demo";
	public static String webhookName = "git_demo";
	public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	public static LocalDateTime now = LocalDateTime.now();
	public static String commitId = "\"86ef096bb924674a69cd2198e2964b76aa75d88b\"";
	public static String authorName = "\"Insights_test\"";
	public static String message = "\"Update Hello\"";
	public static String timestamp = "\"2019-09-16T04:52:25Z\"";
	public static String fieldNotFoundinToolData = "head_commit.newid=commitId,head_commit.newmessage=message";
	public static String toolData = "{\"head_commit\":{\"id\":\"86ef096bb924674a69cd2198e2964b76aa75d88b\",\"tree_id\":\"d1face44ae7151dfaf6387b5eaf3075419583dcc\",\"distinct\":true,\"message\":\"Update Hello\",\"timestamp\":\"2019-09-16T04:52:25Z\",\"author\":{\"name\":\"Insights_test\",\"email\":\"46189557+insights@users.noreply.github.com\",\"username\":\"insights546\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[],\"removed\":[],\"modified\":[\"Hello\"]}}";
	public static String expectedOutput = "{\"commitTime\":\"2019-09-16T04:52:25Z\",\"authorName\":\"Insights_test\",\"insightsTime\":"
			+ ZonedDateTime.now().toInstant().toEpochMilli()
			+ ",\"webhookName\":\"git_demo\",\"commitId\":\"86ef096bb924674a69cd2198e2964b76aa75d88b\",\"source\":\"webhook\",\"message\":\"Update Hello\",\"inSightsTimeX\":\""
			+ dtf.format(now) + "\",\"labelName\":\"SCM:GIT76:DATA\",\"toollName\":\"GIT\"}";

	
	
	public static JsonObject getJsonObject(String jsonString) {
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(jsonString.trim(), JsonElement.class);
		return jelement.getAsJsonObject();
	}

	public static void publishMessage(String queueName, String routingKey, String playload) throws IOException, TimeoutException {
	
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
				channel.exchangeDeclare(exchangeName, MessageConstants.EXCHANGE_TYPE);
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
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (n:"+nodeName+") where n."+compareFlag+"='86ef096bb924674a69cd2198e2964b76aa75d88b' return n";
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
		Gson gson = new Gson();
	    map = gson.fromJson(finalJson, Map.class);
		}
		catch (GraphDBException e) {
			LOG.error(e);
		} 
		return map;				
	}
	
}
