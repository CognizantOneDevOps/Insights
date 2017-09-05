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

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformengine.message.core.MessageConstants;
import com.cognizant.devops.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class AgentDataSubscriber extends EngineSubscriberResponseHandler{
	private static Logger log = Logger.getLogger(AgentDataSubscriber.class.getName());
	
	public AgentDataSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}
	private boolean dataUpdateSupported;
	private String uniqueKey;
	private String category;
	
	public AgentDataSubscriber(String routingKey, boolean dataUpdateSupported, String uniqueKey, String category) throws Exception {
		super(routingKey);
		this.dataUpdateSupported = dataUpdateSupported;
		this.uniqueKey = uniqueKey;
		this.category = category;
	}

	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException{
		ApplicationConfigProvider.performSystemCheck();
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String message = new String(body, MessageConstants.MESSAGE_ENCODING);
		String routingKey = envelope.getRoutingKey();
		List<String> labels = new ArrayList<String>();
		labels.add("RAW");
		labels.addAll(Arrays.asList(routingKey.split(MessageConstants.ROUTING_KEY_SEPERATOR)));
		List<JsonObject> dataList = new ArrayList<JsonObject>();
		JsonElement json = new JsonParser().parse(message);
		if(json.isJsonArray()){
			JsonArray asJsonArray = json.getAsJsonArray();
			for(JsonElement e : asJsonArray){
				if(e.isJsonObject()){
					dataList.add(e.getAsJsonObject());
				}
			}
			try {
				String cypherQuery = null;
				if(dataUpdateSupported){
					String labelsStr = routingKey.replace(".", ":");
					String relation = category+"_UPDATED_TO";
					cypherQuery = buildCypherQuery(labelsStr, uniqueKey, relation);
				}else{
					String queryLabel = "";
					for(String label : labels){
						queryLabel += ":"+label;
					}
					cypherQuery = "UNWIND {props} AS properties CREATE (n"+queryLabel+") set n=properties return count(n)";
				}
				List<List<JsonObject>> partitionList = partitionList(dataList, 1000);
				for(List<JsonObject> chunk : partitionList){
					JsonObject graphResponse = dbHandler.bulkCreateNodes(chunk, labels, cypherQuery);
					if(graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0){
						log.error("Unable to insert nodes for routing key: "+routingKey+", error occured: "+graphResponse);
						log.error(chunk);
					}
				}
				getChannel().basicAck(envelope.getDeliveryTag(), false);
			} catch (GraphDBException e) {
				log.error(e);
			}
		}
	}
	
	private <T> List<List<T>> partitionList(List<T> list, final int size) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += size) {
	       /* parts.add(new ArrayList<T>(
	            list.subList(i, Math.min(N, i + size)))
	        );*/
	    	parts.add(getPartitionSubList(list,i,size,N));                 
	    }
	    return parts;
	}
	
	private <T> ArrayList<T> getPartitionSubList(List<T> list, int index,int size, final int N){
		return new ArrayList<T>(list.subList(index, Math.min(N, index + size)));
	}
	
	private String buildCypherQuery(String labels, String fieldName, String relation){
		StringBuffer query = new StringBuffer();
		query.append("UNWIND {props} AS properties CREATE (new:LATEST) set new=properties ").append(" ");
		query.append("WITH new,").append(" ");
		if(fieldName.contains(",")){
			String[] fields = fieldName.split(",");
			JsonObject searchCriteria = new JsonObject();
			for(String field : fields){
				searchCriteria.addProperty(field, field);
				query.append("new.").append(field).append(" as ").append(field).append(",");
			}
			query.delete(query.length()-1, query.length());
			query.append(" ");
			query.append("OPTIONAL match (old:LATEST:").append(labels).append(searchCriteria.toString().replace("\"", "")).append(")").append(" ");
		}else{
			query.append("new.").append(fieldName).append(" as key").append(" ");
			query.append("OPTIONAL match (old:LATEST:").append(labels).append("{").append(fieldName).append(":key})").append(" ");
		}
		query.append("with new, collect(old) as oldNodes").append(" ");
		query.append("set new:").append(labels).append(" ");
		query.append("foreach (old in oldNodes | remove old:LATEST ").append(" CREATE (old) -[r:").append(relation).append("]-> (new))").append(" ");
		query.append("return count(new), count(oldNodes)").append(" ");
		return query.toString();
	}
}
