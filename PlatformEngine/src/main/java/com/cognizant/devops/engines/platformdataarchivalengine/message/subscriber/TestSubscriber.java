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
package com.cognizant.devops.engines.platformdataarchivalengine.message.subscriber;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.MessageQueueDataModel;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class TestSubscriber {

	public static void main(String[] args) {
		try {
			ApplicationConfigCache.loadConfigCache();
			ApplicationConfigProvider.performSystemCheck();
			TestSubscriber obj = new TestSubscriber();
			AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
			//DataArchivalDataSubscriber sb = new DataArchivalDataSubscriber("SYSTEM.DATAARCHIVAL.DATA");
//			String configDetails = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SYSTEM.ELASTICTRANSFER.CONFIG\",\"agentCtrlQueue\":\"Elastic_transfer_agent_test\",\"dataArchivalQueue\":\"SYSTEM.ELASTICTRANSFER.CONFIG\"},\"publish\":{\"data\":\"SYSTEM.ELASTICTRANSFER.DATA\",\"health\":\"SYSTEM.ELASTICTRANSFER.HEALTH\"},\"agentId\":\"Elastic_transfer_agent_test\",\"toolCategory\":\"SYSTEM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"startFrom\":\"2017-10-01 00:00:01\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v6.8\",\"toolName\":\"ELASTICTRANSFER\",\"labelName\":\"ELASTICTRANSFER\"}";
//			JsonObject agentJson = convertStringIntoJson(configDetails);
//			String trackingDetails = "";
//			Date updateDate = Timestamp.valueOf(LocalDateTime.now());
//			Boolean vault = false;
//			Boolean status = agentConfigDAL.saveAgentConfigFromUI(agentJson.get("agentId").getAsString(), agentJson.get("toolCategory").getAsString(),agentJson.get("labelName").getAsString(), agentJson.get("toolName").getAsString(),
//					agentJson, agentJson.get("agentVersion").getAsString(), agentJson.get("osversion").getAsString(), updateDate, vault);
//			System.out.println("Status:"+status);
			obj.publishDataArchivalDetails("SYSTEM.ELASTICTRANSFER.DATA", "{\"archivalName\":\"Archive_1\",\"sourceUrl\":\"http://10.224.86.168:7575\"}");
			obj.publishDataArchivalDetails("SYSTEM.ELASTICTRANSFER.DATA", "{\"archivalName\":\"Archive_2\",\"sourceUrl\":\"http://10.224.86.168:7575\"}");
			//obj.publishDataArchivalDetails("SYSTEM.ELASTICTRANSFER.DATA", "{\"archivalName\":\"Archive_3\",\"sourceUrl\":\"http://10.224.86.168:7575\"}");
			//obj.publishDataArchivalDetails("SYSTEM.ELASTICTRANSFER.DATA", "{\"archivalName\":\"Archive_4\",\"sourceUrl\":\"http://10.224.86.168:7575\"}");
			obj.publishDataArchivalDetails("SYSTEM.ELASTICTRANSFER.HEALTH", "[{\"inSightsTime\": 1596177570.931, \"status\": \"failure\", \"execId\": \"96a32c9e-d2f8-11ea-83ef-005056955e95\", \"categoryName\": \"SYSTEM\", \"executionTime\": 9, \"inSightsTimeX\": \"2020-07-31T06:39:30Z\", \"toolName\": \"ELASTICTRANSFER\", \"message\": \"Error occurred: unsupported operand type(s) for +: 'NoneType' and 'str'\", \"agentId\": \"Elastic_transfer_agent_test\"}]");
			obj.publishDataArchivalDetails("SYSTEM.ELASTICTRANSFER.HEALTH", "[{\"inSightsTime\": 1596177570.931, \"status\": \"success\", \"execId\": \"96a32c9e-d2f8-11ea-83ef-005056955e99\", \"categoryName\": \"SYSTEM\", \"executionTime\": 9, \"inSightsTimeX\": \"2020-07-31T06:39:30Z\", \"toolName\": \"ELASTICTRANSFER\", \"message\": \"Success message\", \"agentId\": \"Elastic_transfer_agent_test\"}]");
		} catch (Exception e) {
			//System.out.println("Error:"+e);
		}
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
	static public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		JsonParser parser = new JsonParser();
		objectJson = parser.parse(convertregisterkpi).getAsJsonObject();
		return objectJson;
	}

}
