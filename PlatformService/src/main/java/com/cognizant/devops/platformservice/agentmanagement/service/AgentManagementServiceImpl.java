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
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MessageConstants;
import com.cognizant.devops.platformcommons.core.enums.AGENTSTATUS;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformservice.agentmanagement.util.AgentManagementUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


@Service("agentManagementService")
public class AgentManagementServiceImpl  implements AgentManagementService{
	private static Logger LOG = Logger.getLogger(AgentManagementServiceImpl.class);

	@Override
	public String registerAgent(String toolName,String agentVersion,String osversion,String configDetails) throws InsightsCustomException {

		try {
			String agentId = getAgentkey(toolName);

			Gson gson = new Gson();
			JsonElement jelement = gson.fromJson(configDetails.trim(),JsonElement.class);
			JsonObject  json = jelement.getAsJsonObject();
			json.addProperty("agentId",agentId);
			json.addProperty("osversion",osversion);
			json.addProperty("agentVersion",agentVersion);
			json.get("subscribe").getAsJsonObject().addProperty("agentCtrlQueue" ,agentId);
			boolean isDataUpdateSupported = false;
			if(json.get("isDataUpdateSupported") != null && !json.get("isDataUpdateSupported").isJsonNull()) {
				isDataUpdateSupported = json.get("isDataUpdateSupported").getAsBoolean();
			}  
			String uniqueKey = agentId;
			Date updateDate= Timestamp.valueOf(LocalDateTime.now());

			// register agent in DB
			AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
			agentConfigDAL.saveAgentConfigFromUI(agentId , toolName,json, isDataUpdateSupported, uniqueKey,agentVersion,osversion,updateDate);

			//Create zip/tar file with updated config.json
			Path agentZipPath =updateAgentConfig(toolName,agentId,json);
			byte[] data = Files.readAllBytes(agentZipPath);
			sendAgentPackage(data,agentId,agentId,toolName,osversion);
			performAgentAction(agentId,AGENTSTATUS.RUNNING.name());
		} catch (Exception e) {
			LOG.error("Error while registering agent "+toolName, e);
			throw new InsightsCustomException(e.toString());
		}

		//call installAgent method
		//String status = installAgent(agentId, toolName,agentZipPath.getFileName().toString(), osversion);
		//return status;
		return "SUCCESS";
	}


	@Override
	public String installAgent(String agentId,String toolName,String fileName,String osversion) throws InsightsCustomException{
		try {

			Path path = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),toolName,fileName);
			byte[] data = Files.readAllBytes(path);
			sendAgentPackage(data,fileName,agentId,toolName,osversion);
		} catch (Exception e) {
			LOG.error("Error while installing agent..", e);
			throw new InsightsCustomException(e.toString());
		}

		return "SUCCESS";
	}

	@Override
	public String startStopAgent(String agentId, String action) throws InsightsCustomException {
		try {
			AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
			agentConfigDAL.updateAgentRunningStatus(agentId,AGENTSTATUS.valueOf(action));
			performAgentAction(agentId,action);
		} catch (Exception e) {
			LOG.error("Error while installing agent..", e);
			throw new InsightsCustomException(e.toString());
		}
		return "SUCCESS";
	}

	@Override
	public String updateAgent(String agentId, String configDetails, String toolName, String agentVersion, String osversion) throws InsightsCustomException {

		try {
			//Get latest agent code
			getToolRawConfigFile(agentVersion,toolName);

			Gson gson = new Gson();
			JsonElement jelement = gson.fromJson(configDetails.trim(),JsonElement.class);
			JsonObject  json = jelement.getAsJsonObject();

			boolean isDataUpdateSupported = false;
			if(json.get("isDataUpdateSupported") != null && !json.get("isDataUpdateSupported").isJsonNull()) {
				isDataUpdateSupported = json.get("isDataUpdateSupported").getAsBoolean();
			}
			String uniqueKey = agentId;
			Date updateDate= Timestamp.valueOf(LocalDateTime.now());

			AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
			 agentConfigDAL.saveAgentConfigFromUI(agentId , toolName, json, isDataUpdateSupported, uniqueKey, agentVersion,osversion,updateDate);

			Path agentZipPath = updateAgentConfig(toolName,agentId,json);

			byte[] data = Files.readAllBytes(agentZipPath);
			sendAgentPackage(data,agentId,agentId,toolName,osversion);

		} catch (Exception e) {
			LOG.error("Error updating and installing agent", e);
			throw new InsightsCustomException(e.toString());
		}

		return "SUCCESS";
	}

	@Override
	public List<AgentConfigTO> getRegisteredAgents() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfigTO> agentList = null;
		try{
			List<AgentConfig> agentConfigList = agentConfigDAL.getAllAgentConfigurations();
			agentList = new ArrayList<>(agentConfigList.size());
			for(AgentConfig agentConfig : agentConfigList) {
				AgentConfigTO to = new AgentConfigTO();
				BeanUtils.copyProperties(agentConfig, to);
				agentList.add(to);
			}
		}catch(Exception e){
			LOG.error("Error getting all agent config", e);
			throw new InsightsCustomException(e.toString());
		}

		return agentList;
	}

	@Override
	public AgentConfigTO getAgentDetails(String agentId) throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();

		AgentConfigTO agentConfig = new AgentConfigTO();
		try{
			BeanUtils.copyProperties(agentConfigDAL.getAgentConfigurations(agentId), agentConfig);
		}catch(Exception e){
			LOG.error("Error getting agent details", e);
			throw new InsightsCustomException(e.toString());
		}
		return agentConfig;
	}

	@Override
	public Map<String, ArrayList<String>> getSystemAvailableAgentList()  throws InsightsCustomException{
		/*System.setProperty("http.proxyHost", "proxy.cognizant.com");
		System.setProperty("http.proxyPort","6050");*/
		Map<String,ArrayList<String>>  agentDetails = new HashMap<String,ArrayList<String>>();
		String url = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl();
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
			LOG.error("Error while getting system agent list ",e);
			throw new InsightsCustomException(e.toString());
		}
		return agentDetails;
	}

	@Override
	public String getToolRawConfigFile(String version, String tool) throws InsightsCustomException  {
		String configJson = null;
		try {
			/*System.setProperty("http.proxyHost", "proxy.cognizant.com");
			System.setProperty("http.proxyPort","6050");*/

			String filePath = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl()
					+"/"+version+"/agents/testagents/"+tool;
			filePath=filePath.trim()+"/"+tool.trim()+".zip";
			String targetDir =  ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath();
			configJson = AgentManagementUtil.getInstance().getAgentConfigfile(new URL(filePath), new File(targetDir)).toString();
		} catch (IOException e) {
			LOG.error("Error in getting raw config file ",e);
			throw new InsightsCustomException(e.toString());

		}
		return configJson;
	}

	private ArrayList<String> getAgents(String version) {

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

	private Path updateAgentConfig( String toolName,String agentId,JsonObject json) throws Exception {
		String filePath = ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath();
		filePath = filePath+"/"+toolName+"/com/cognizant/devops/platformagents/agents/";

		DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(filePath));
		Iterator<Path> pathIterator = paths.iterator();
		File configFile = null;
		try(Stream<Path> all =  Files.walk(Paths.get(filePath));){

			pathIterator = all.iterator();
			while(pathIterator.hasNext()) {
				Path path = pathIterator.next();
				if(path.toString().endsWith(".json")) {
					configFile = path.toFile();
				}
			}
		}catch(IOException e) {
			LOG.error("Error finding json file", e);
			throw e;
		}
		//Writing json to file
		try (FileWriter file = new FileWriter(configFile)) {
			file.write(json.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			LOG.error("Error writing modified json file", e);
			throw e;
		}
		Path sourceFolderPath = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),toolName);
		Path zipPath = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),toolName+".zip");
		Path agentZipPath = null;
		try {
			agentZipPath = AgentManagementUtil.getInstance().getAgentZipFolder(sourceFolderPath, zipPath);
		} catch (Exception e) {
			LOG.error("Error creatig final zip file with modified json file", e);
			throw e;
		}
		return agentZipPath;

	}

	private void sendAgentPackage(byte[] data, String fileName, String agentId, String toolName, String osversion) throws Exception {
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put("fileName", fileName);
		headers.put("osType",osversion);
		headers.put("agentToolName", toolName);
		headers.put("agentId", agentId);

		BasicProperties props = getBasicProperties(headers);

		String agentDaemonQueueName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentPkgQueue();

		publishAgentAction(agentDaemonQueueName, data, props);
	}

	private void performAgentAction(String agentId, String action) throws Exception {
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put("agentId", agentId);

		BasicProperties props = getBasicProperties(headers);
		//agentId will be queue id. Agent code will connect to MQ based on agentId present in config.json
		publishAgentAction(agentId, action.getBytes(), props);
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

	private BasicProperties getBasicProperties(Map<String,Object> headers) {

		BasicProperties.Builder propertiesBuilder = new BasicProperties.Builder();
		propertiesBuilder.headers(headers);

		return propertiesBuilder.build();
	}

	private String getAgentkey(String toolName) {
		return toolName + "-"+ Instant.now().toEpochMilli();
	}

	/* public static void main(String... args) {
		   ApplicationConfigCache.loadConfigCache();
		   AgentManagementServiceImpl impl = new AgentManagementServiceImpl();

		   JsonParser parser = new JsonParser();
		   JsonObject json = (JsonObject) parser.parse(impl.getToolRawConfigFile("v3.0", "bitbucket"));
		  // JsonObject json = impl.getToolRawConfigFile("v3.0", "bitbucket");
		   System.out.println(json);
		   String status = impl.registerAgent("bitbucket", "3.0", "WINDOWS", testConfig());
		   System.out.println(status);

		   impl.getRegisteredAgents();
		   //System.out.println(impl.getAgentDetails("bitbucket-1519302061371"));
		   //impl.startStopAgent("bitbucket-1519302061371", "RUNNING");
		   System.exit(0);
	   }*/

	public static String testConfig() {

		return "{\r\n" + 
				"	\"mqConfig\" : {\r\n" + 
				"		\"user\" : \"iSight\", \r\n" + 
				"		\"password\" : \"iSight\", \r\n" + 
				"		\"host\" : \"localhost\", \r\n" + 
				"		\"exchange\" : \"iSight\"\r\n" + 
				"	},\r\n" + 
				"	\"subscribe\" : {\r\n" + 
				"		\"config\" : \"SCM.GIT.config\"\r\n" + 
				"	},\r\n" + 
				"	\"publish\" : {\r\n" + 
				"		\"data\" : \"SCM.GIT.DATA\",\r\n" + 
				"		\"health\" : \"SCM.GIT.HEALTH\"\r\n" + 
				"	},\r\n" + 
				"	\"communication\":{\r\n" + 
				"		\"type\" : \"REST\" \r\n" + 
				"	},\r\n" + 
				"	\"dynamicTemplate\" : {\r\n" + 
				"		\"responseTemplate\" : {\r\n" + 
				"			\"sha\": \"commitId\",\r\n" + 
				"			\"commit\" : {\r\n" + 
				"				\"author\" : {\r\n" + 
				"					\"name\": \"authorName\",\r\n" + 
				"					\"date\": \"commitTime\"\r\n" + 
				"				}\r\n" + 
				"			}\r\n" + 
				"		}\r\n" + 
				"	},\r\n" + 
				"	\"enableBranches\" : false,\r\n" + 
				"	\"toolsTimeZone\" : \"GMT\",\r\n" + 
				"	\"insightsTimeZone\" : \"Asia/Kolkata\",\r\n" + 
				"	\"useResponseTemplate\" : true,\r\n" + 
				"	\"auth\" : \"base64\",\r\n" + 
				"	\"runSchedule\" : 30,\r\n" + 
				"	\"timeStampField\":\"commitTime\",\r\n" + 
				"	\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\r\n" + 
				"	\"StartFrom\" : \"2018-02-26 15:46:33\",\r\n" + 
				"	\"AccessToken\": \"AuthToken\",\r\n" + 
				"	\"GetRepos\":\"https://api.github.com/users/username/repos\",\r\n" + 
				"	\"CommitsBaseEndPoint\":\"https://api.github.com/repos/username/\",\r\n" + 
				"	\"isDebugAllowed\" : false,\r\n" + 
				"	\"loggingSetting\" : {\r\n" + 
				"		\"logLevel\" : \"WARN\"\r\n" + 
				"	}\r\n" + 
				"}";
	}

}
