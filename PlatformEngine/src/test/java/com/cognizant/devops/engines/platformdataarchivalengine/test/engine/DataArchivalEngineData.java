/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

package com.cognizant.devops.engines.platformdataarchivalengine.test.engine;

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
import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class DataArchivalEngineData {
	private static Logger log = LogManager.getLogger(DataArchivalEngineData.class.getName());
	String archivalRecord = "{\"archivalName\":\"Archive_test_engine\",\"startDate\":\"2020-08-01T00:00:00Z\",\"endDate\":\"2020-08-06T00:00:00Z\",\"daysToRetain\":8,\"author\":\"\"}";
	JsonObject archivalDetailsJson = convertStringIntoJson(archivalRecord);
	String archivalName = archivalDetailsJson.get("archivalName").getAsString();
	String startDate = archivalDetailsJson.get("startDate").getAsString();
	long epochStartDate = InsightsUtils.getEpochTime(startDate) / 1000;
	String endDate = archivalDetailsJson.get("endDate").getAsString();
	long epochEndDate = InsightsUtils.getEpochTime(endDate) / 1000;
	String author = archivalDetailsJson.get("author").getAsString();
	int daysToRetain = archivalDetailsJson.get("daysToRetain").getAsInt();
	Long createdOn = InsightsUtils.getTodayTime() / 1000;
	Long expiryDate = getExpiryDate(createdOn, daysToRetain);
	String status = DataArchivalStatus.INPROGRESS.name();

	String archivalFailName = "Archive_test_engine_fail";
	String nonExistingArchivalRecord = "Not_existing_record";

	String configDetails = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SYSTEM.ELASTICTRANSFER.CONFIG\",\"agentCtrlQueue\":\"Elastictransfer_agent_test\",\"dataArchivalQueue\":\"SYSTEM.ELASTICTRANSFER.CONFIG\"},\"publish\":{\"data\":\"SYSTEM.ELASTICTRANSFER.DATA\",\"health\":\"SYSTEM.ELASTICTRANSFER.HEALTH\"},\"agentId\":\"Elastictransfer_agent_test\",\"toolCategory\":\"SYSTEM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"startFrom\":\"2017-10-01 00:00:01\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v6.8\",\"toolName\":\"ELASTICTRANSFER\",\"labelName\":\"ELASTICTRANSFER\"}";
	JsonObject agentJson = convertStringIntoJson(configDetails);
	String trackingDetails = "";
	Date updateDate = Timestamp.valueOf(LocalDateTime.now());
	Boolean vault = false;
	String routingKey = agentJson.get("publish").getAsJsonObject().get("data").getAsString();
	String routingKeyHealth = agentJson.get("publish").getAsJsonObject().get("health").getAsString();
	String routingKeyHealthNeo4j = "SYSTEM:ELASTICTRANSFER:HEALTH";
	String urlMessage = "{\"data\":[{\"status\":\"Success\",\"execId\":\"e3f59e8a-de05-11ea-a84e-0050569522bd\",\"sourceUrl\":\"http://localhost:7575\",\"categoryName\":\"SYSTEM\",\"archivalName\":\"Archive_test_engine\",\"toolName\":\"ELASTICTRANSFER\"}]}";
	String urlMessageWithNonExistingArchivalName = "{\"data\":[{\"status\":\"Success\",\"execId\":\"e3f59e8a-de05-11ea-a84e-0050569522be\",\"sourceUrl\":\"http://localhost:7575\",\"categoryName\":\"SYSTEM\",\"archivalName\":\"Not_existing_record\",\"toolName\":\"ELASTICTRANSFER\"}]}";
	String urlMessageWithEmptyArchivalName = "{\"data\":[{\"status\":\"Success\",\"execId\":\"e3f59e8a-de05-11ea-a84e-0050569522bf\",\"sourceUrl\":\"http://localhost:7575\",\"categoryName\":\"SYSTEM\",\"archivalName\":\"\",\"toolName\":\"ELASTICTRANSFER\"}]}";
	String urlMessageWithEmptyURL = "{\"data\":[{\"status\":\"Success\",\"execId\":\"e3f59e8a-de05-11ea-a84e-0050569522bc\",\"sourceUrl\":\"\",\"categoryName\":\"SYSTEM\",\"archivalName\":\"Not_existing_record\",\"toolName\":\"ELASTICTRANSFER\"}]}";

	String successHealthMessage = "[{\"inSightsTime\": 1596177570.931, \"status\": \"success\", \"execId\": \"96a32c9e-d2f8-11ea-83ef-005056955e85\", \"categoryName\": \"SYSTEM\", \"executionTime\": 9, \"inSightsTimeX\": \"2020-07-31T06:39:30Z\", \"toolName\": \"ELASTICTRANSFER\", \"message\": \"Success message\", \"agentId\": \"Elastictransfer_agent_test\"}]";
	String failureHealthMessage = "[{\"inSightsTime\": 1596177570.931, \"status\": \"failure\", \"execId\": \"96a32c9e-d2f8-11ea-83ef-005056955e95\", \"categoryName\": \"SYSTEM\", \"executionTime\": 9, \"inSightsTimeX\": \"2020-07-31T06:39:30Z\", \"toolName\": \"ELASTICTRANSFER\", \"message\": \"Error occurred: unsupported operand type(s) for +: 'NoneType' and 'str'\", \"agentId\": \"Elastictransfer_agent_test\"}]";
	String successHealthMessageString = "{\"inSightsTime\": 1596177570.931, \"status\": \"success\", \"execId\": \"96a32c9e-d2f8-11ea-83ef-005056955e85\", \"categoryName\": \"SYSTEM\", \"executionTime\": 9, \"inSightsTimeX\": \"2020-07-31T06:39:30Z\", \"toolName\": \"ELASTICTRANSFER\", \"message\": \"Success message\", \"agentId\": \"Elastictransfer_agent_test\"}";
	String failureHealthMessageString = "{\"inSightsTime\": 1596177570.931, \"status\": \"failure\", \"execId\": \"96a32c9e-d2f8-11ea-83ef-005056955e95\", \"categoryName\": \"SYSTEM\", \"executionTime\": 9, \"inSightsTimeX\": \"2020-07-31T06:39:30Z\", \"toolName\": \"ELASTICTRANSFER\", \"message\": \"Error occurred: unsupported operand type(s) for +: 'NoneType' and 'str'\", \"agentId\": \"Elastictransfer_agent_test\"}";
	JsonObject urlJson = convertStringIntoJson(urlMessage);
	JsonObject successHealthMessageJson = convertStringIntoJson(successHealthMessageString);
	JsonObject failureHealthMessageJson = convertStringIntoJson(failureHealthMessageString);

	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		JsonParser parser = new JsonParser();
		objectJson = parser.parse(convertregisterkpi).getAsJsonObject();
		return objectJson;
	}

	public void publishDataArchivalDetails(String routingKey, String publishDataJson)
			throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		MessageQueueDataModel messageQueueConfig = ApplicationConfigProvider.getInstance().getMessageQueue();
		factory.setHost(messageQueueConfig.getHost());
		factory.setUsername(messageQueueConfig.getUser());
		factory.setPassword(messageQueueConfig.getPassword());
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		String queueName = routingKey.replace(".", "_");
		channel.exchangeDeclare(MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE, true);
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, MQMessageConstants.EXCHANGE_NAME, routingKey);
		channel.basicPublish(MQMessageConstants.EXCHANGE_NAME, routingKey, null, publishDataJson.getBytes());
		channel.close();
		connection.close();
	}

	public Long getExpiryDate(Long createdOn, int daysToRetain) {
		Long days = (long) (daysToRetain * 24 * 60 * 60);
		return (createdOn + days);

	}

	public int readNeo4JData(String nodeName, String value) {
		int countOfRecords = 0;
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:" + nodeName + ") where n.execId='" + value + "' return n";
		log.debug(" query  {} ", query);
		GraphResponse neo4jResponse;
		try {

			neo4jResponse = dbHandler.executeCypherQuery(query);

			JsonArray tooldataObject = neo4jResponse.getJson().get("results").getAsJsonArray();

			countOfRecords = tooldataObject.size();
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return countOfRecords;

	}

}
