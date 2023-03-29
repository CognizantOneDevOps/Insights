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
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class DataArchivalEngineData {
	private static Logger log = LogManager.getLogger(DataArchivalEngineData.class.getName());

	String archivalFailName = "Archive_test_engine_fail";
	String nonExistingArchivalRecord = "Not_existing_record";
	long epochStartDate = 0l;
	long epochEndDate = 0l;
	int daysToRetain = 0;
	Long createdOn = 0l;
	Long expiryDate = 0l;
	Date updateDate = Timestamp.valueOf(LocalDateTime.now());

	public void publishDataArchivalDetails(String routingKey, String publishDataJson)
			throws InsightsCustomException, IOException, TimeoutException {
		Connection connection = null;
		Channel channel = null;
		try {
			connection = RabbitMQConnectionProvider.getConnection();
			channel = connection.createChannel();
			String queueName = routingKey.replace(".", "_");
			channel.exchangeDeclare(MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE, true);
			channel.queueDeclare(queueName, true, false, false, RabbitMQConnectionProvider.getQueueArguments());
			channel.queueBind(queueName, MQMessageConstants.EXCHANGE_NAME, routingKey);
			channel.basicPublish(MQMessageConstants.EXCHANGE_NAME, routingKey, null, publishDataJson.getBytes());
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			channel.close();
			//connection.close();
		}
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
