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
package com.cognizant.devops.platformengine.message.subscriber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.constants.MessageConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class AgentHealthSubscriber extends EngineSubscriberResponseHandler{
	private static Logger log = LogManager.getLogger(AgentHealthSubscriber.class.getName());
	
	public AgentHealthSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException{
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String message = new String(body, MessageConstants.MESSAGE_ENCODING);
		String routingKey = envelope.getRoutingKey();
		//routingKey=routingKey.replace("_", ".");
		log.debug(consumerTag+" [x] Received '" + routingKey + "':'" + message + "'");
		List<String> labels = Arrays.asList(routingKey.split(MessageConstants.ROUTING_KEY_SEPERATOR));
		List<JsonObject> dataList = new ArrayList<JsonObject>();
		JsonElement json = new JsonParser().parse(message);
		String agentId="";
		if(json.isJsonArray()){
			JsonArray asJsonArray = json.getAsJsonArray();
			for(JsonElement e : asJsonArray){
				if(e.isJsonObject()){
					JsonObject jsonObject = e.getAsJsonObject();
					if(jsonObject.has("agentId")) {
						agentId=jsonObject.get("agentId").getAsString();
					}
					//log.debug("agentId   === "+agentId);
					if(labels.size()>1){
						jsonObject.addProperty("category", labels.get(0));
						jsonObject.addProperty("toolName", labels.get(1));						
					}
					dataList.add(jsonObject);
				}
			}
			try {
				String healthLabels = ":LATEST:"+routingKey.replace(".", ":");
				String healthQuery;
				
				if(!agentId.equalsIgnoreCase("")) {
					healthQuery= "Match";
					healthQuery = healthQuery + " (old"+healthLabels+")"; // {} 
					healthQuery = healthQuery + " where old.agentId='"+agentId+"' or old.agentId is null";
					healthQuery = healthQuery + " OPTIONAL MATCH (old) <-[:UPDATED_TO*10]-(purge)  where old.agentId='"+agentId+"'";
					healthQuery = healthQuery + " CREATE (new"+healthLabels+" {props}) ";
					healthQuery = healthQuery + " MERGE  (new)<-[r:UPDATED_TO]-(old)";
					healthQuery = healthQuery + " REMOVE old:LATEST";
					healthQuery = healthQuery + " detach delete purge ";
					healthQuery = healthQuery + " return old,new";
			
				}else {
					healthQuery = "Match (old"+healthLabels+")";
					healthQuery = healthQuery + " OPTIONAL MATCH (old) <-[:UPDATED_TO*10]-(purge) ";
					healthQuery = healthQuery + " CREATE (new"+healthLabels+" {props})";
					healthQuery = healthQuery + " MERGE  (new)<-[r:UPDATED_TO]-(old)";
					healthQuery = healthQuery + " REMOVE old:LATEST";
					healthQuery = healthQuery + " detach delete purge ";
					healthQuery = healthQuery + " return old,new";
				}
				
				//log.debug("arg0 git2  ======"+healthQuery );
				JsonObject graphResponse = dbHandler.executeQueryWithData(healthQuery, dataList);
				//log.debug("arg0 ====== "+graphResponse.get("response"));
				if(graphResponse.get("response").getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().size() == 0) {
					//log.debug("arg0 === data not present ====== " );
					healthQuery="";
					healthQuery = healthQuery + " CREATE (new"+healthLabels+" {props})";
					JsonObject graphResponse1 = dbHandler.executeQueryWithData(healthQuery, dataList);
					//log.debug("arg1 ====== "+graphResponse1.get("response"));
				}
				if(graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0){
					log.error("Unable to insert health nodes for routing key: "+routingKey+", error occured: "+graphResponse);
					log.error(dataList);
					EngineStatusLogger.getInstance().createEngineStatusNode("Unable to insert health nodes for routing key: "+routingKey,PlatformServiceConstants.FAILURE);
				}
				getChannel().basicAck(envelope.getDeliveryTag(), false);
			} catch (GraphDBException e) {
				log.error(e);
			}
		}
	}	
}
