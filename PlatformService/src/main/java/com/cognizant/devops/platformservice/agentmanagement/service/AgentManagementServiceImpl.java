/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.agentmanagement.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformengine.message.core.MessageConstants;
import com.cognizant.devops.platformservice.agentmanagement.util.AgentManagementUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Service("agentManagementService")
public class AgentManagementServiceImpl  implements AgentManagementService{
	private static Logger LOG = Logger.getLogger(AgentManagementServiceImpl.class);

	@Override
	public String registerAgent(String toolName,String agentVersion,String osversion,String configDetails) {
		// register agent in DB
		//Create zip/tar file with updated config.json
		//call installAgent method
		return null;
	}

	@Override
	public String installAgent(String agentId,String toolName,String fileName,String osversion){
		try {
			String agentDaemonQueueName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentPkgQueue();
			Path path = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),toolName,fileName);
			byte[] data = Files.readAllBytes(path);
			sendAgentPackage(agentDaemonQueueName,data,fileName,agentId,toolName,osversion);
		} catch (Exception e) {
			LOG.error("Error while installing agent..", e);
			return "FAILED";
		}
		
		return "SUCCESS";
	}

	@Override
	public String startStopAgent(String agentId, String action) {
		try {
			//Update status in DB and push message to MQ. If MQ fails, DB should be reverted.
			performAgentAction(agentId,action.getBytes());
		} catch (Exception e) {
			LOG.error("Error while installing agent..", e);
			return "FAILED";
		}
		return "SUCCESS";
	}

	@Override
	public String updateAgent(String agentId, String configDetails) {
		// Code to update DB
		// Call installAgent method and pass all required values
		return null;
	}

	@Override
	public JsonObject getAgentDetails() {

		/*System.setProperty("http.proxyHost", "proxy.cognizant.com");
		System.setProperty("http.proxyPort","6050");*/
		Map<String,ArrayList<String>>  agentDetails = new HashMap<String,ArrayList<String>>();
		String url = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl();
		JsonObject details = new JsonObject();
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Elements rows = doc.getElementsByTag("a");
			for (Element element : rows) {
				if( null != element.text() && element.text().startsWith("v")){
					String version = StringUtils.stripEnd(element.text(),"/");
					ArrayList<String> toolJson = getAgents(version);
					agentDetails.put(version, toolJson);

				}
			}
		} catch (IOException e) {
			LOG.debug(e);
		}
		details.add("details", new Gson().toJsonTree(agentDetails));
		return details;
	}

	private ArrayList<String> getAgents(String version) {

		/*System.setProperty("http.proxyHost", "proxy.cognizant.com");
		System.setProperty("http.proxyPort","6050");*/
		Document doc;
		String url = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl()+"/"+version+"/agents/testagents/";
		ArrayList<String> tools = new ArrayList<String>();
		try {
			doc = Jsoup.connect(url).get();
			Elements rows = doc.getElementsByTag("a");
			for (Element element : rows) {
				if(null != element.text() && element.text().endsWith("/")){
					tools.add( StringUtils.stripEnd(element.text(),"/"));

				}

			}
		} catch (IOException e) {
			LOG.debug(e);
		}
		return tools;
	}

	@Override
	public JsonObject getConfigFile(String version, String tool) {

		JsonObject configJson = null;
		try {
			String filePath = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl()
					+"/"+version+"/agents/testagents/"+tool;
			filePath=filePath.trim()+"/"+tool.trim()+".zip";
			String targetDir =  ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath();
			configJson = AgentManagementUtil.getInstance().getAgentConfigfile(new URL(filePath), new File(targetDir));
		} catch (IOException e) {
			LOG.debug(e);
		}
		return configJson;
	}
	
	private void sendAgentPackage(String routingKey, byte[] data, String fileName, String agentId, String toolName, String osversion) throws Exception {
		BasicProperties props = getBasicProperties(fileName, agentId, toolName, osversion);
		publishAgentAction(routingKey, data, props);
	}
	
	private void performAgentAction(String agentId, byte[] action) throws Exception {
		BasicProperties props = getBasicProperties(null, agentId, null, null);
		publishAgentAction(agentId, action, props);
	}
	
	private void publishAgentAction(String routingKey, byte[] data, BasicProperties props) throws Exception {
		String exchangeName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentExchange();
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(ApplicationConfigProvider.getInstance().getMessageQueue().getHost());
        factory.setUsername(ApplicationConfigProvider.getInstance().getMessageQueue().getUser());
		factory.setPassword(ApplicationConfigProvider.getInstance().getMessageQueue().getPassword());
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, MessageConstants.EXCHANGE_TYPE,true);
        channel.queueDeclare(routingKey, true, false, false, null);
        channel.queueBind(routingKey, exchangeName, routingKey);
        channel.basicPublish(exchangeName, routingKey, props, data);
        
        channel.close();
        connection.close();
	}
	
	private BasicProperties getBasicProperties(String fileName,String agentId, String toolName, String osversion) {
		
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put("fileName", fileName);
		headers.put("osType",osversion);
		headers.put("agentToolName", toolName);
		headers.put("agentId", agentId);
		
		BasicProperties.Builder propertiesBuilder = new BasicProperties.Builder();
		propertiesBuilder.headers(headers);
		propertiesBuilder.contentEncoding("gzip");
		
		return propertiesBuilder.build();
	}

}
