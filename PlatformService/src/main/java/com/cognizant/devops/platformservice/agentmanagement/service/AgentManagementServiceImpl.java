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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
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
import com.cognizant.devops.platformcommons.core.enums.AGENTACTION;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformservice.agentmanagement.util.AgentManagementUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Service("agentManagementService")
public class AgentManagementServiceImpl implements AgentManagementService {
	private static Logger log = Logger.getLogger(AgentManagementServiceImpl.class);

	private static final String FILETYPE = ".zip";
	private static final String ISDATESUPPORTED = "isDataUpdateSupported";
	private static final String SUCCESS = "SUCCESS";

	@Override
	public String registerAgent(String toolName, String agentVersion, String osversion, String configDetails,
			String trackingDetails) throws InsightsCustomException {

		try {
			String agentId = getAgentkey(toolName);

			Gson gson = new Gson();
			JsonElement jelement = gson.fromJson(configDetails.trim(), JsonElement.class);
			JsonObject json = jelement.getAsJsonObject();
			json.addProperty("agentId", agentId);
			json.addProperty("osversion", osversion);
			json.addProperty("agentVersion", agentVersion);
			json.get("subscribe").getAsJsonObject().addProperty("agentCtrlQueue", agentId);

			boolean isDataUpdateSupported = false;
			if (json.get(ISDATESUPPORTED) != null && !json.get(ISDATESUPPORTED).isJsonNull()) {
				isDataUpdateSupported = json.get(ISDATESUPPORTED).getAsBoolean();
			}
			String uniqueKey = agentId;
			Date updateDate = Timestamp.valueOf(LocalDateTime.now());

			// Update tracking.json file
			if (!trackingDetails.isEmpty()) {
				JsonElement trackingJsonElement = gson.fromJson(trackingDetails.trim(), JsonElement.class);
				JsonObject trackingDetailsJson = trackingJsonElement.getAsJsonObject();
				updateTrackingJson(toolName, trackingDetailsJson);
			}

			// Create zip/tar file with updated config.json
			Path agentZipPath = updateAgentConfig(toolName, json);
			byte[] data = Files.readAllBytes(agentZipPath);

			String fileName = toolName + FILETYPE;
			sendAgentPackage(data, AGENTACTION.REGISTER.name(), fileName, agentId, toolName, osversion);
			performAgentAction(agentId, AGENTACTION.START.name());

			// Delete tracking.json
			if (!trackingDetails.isEmpty()) {
				deleteTrackingJson(toolName);
			}

			// register agent in DB
			AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
			agentConfigDAL.saveAgentConfigFromUI(agentId, json.get("toolCategory").getAsString(), toolName, json,
					isDataUpdateSupported, uniqueKey, agentVersion, osversion, updateDate);

		} catch (Exception e) {
			log.error("Error while registering agent " + toolName, e);
			throw new InsightsCustomException(e.toString());
		}

		return SUCCESS;
	}

	@Override
	public String uninstallAgent(String agentId, String toolName, String osversion) throws InsightsCustomException {
		try {
			uninstallAgent(AGENTACTION.UNINSTALL.name(), agentId, toolName, osversion);
			AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
			agentConfigDAL.deleteAgentConfigurations(agentId);
		} catch (Exception e) {
			log.error("Error while un-installing agent..", e);
			throw new InsightsCustomException(e.toString());
		}

		return SUCCESS;
	}

	@Override
	public String startStopAgent(String agentId, String action) throws InsightsCustomException {
		try {
			performAgentAction(agentId, action);
			AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
			agentConfigDAL.updateAgentRunningStatus(agentId, AGENTACTION.valueOf(action));

		} catch (Exception e) {
			log.error("Error while agent " + action, e);
			throw new InsightsCustomException(e.toString());
		}
		return SUCCESS;
	}

	@Override
	public String updateAgent(String agentId, String configDetails, String toolName, String agentVersion,
			String osversion) throws InsightsCustomException {

		try {
			// Get latest agent code
			getToolRawConfigFile(agentVersion, toolName);

			Gson gson = new Gson();
			JsonElement jelement = gson.fromJson(configDetails.trim(), JsonElement.class);
			JsonObject json = jelement.getAsJsonObject();
			json.addProperty("osversion", osversion);
			json.addProperty("agentVersion", agentVersion);

			boolean isDataUpdateSupported = false;
			if (json.get(ISDATESUPPORTED) != null && !json.get(ISDATESUPPORTED).isJsonNull()) {
				isDataUpdateSupported = json.get(ISDATESUPPORTED).getAsBoolean();
			}
			String uniqueKey = agentId;
			Date updateDate = Timestamp.valueOf(LocalDateTime.now());

			Path agentZipPath = updateAgentConfig(toolName, json);

			byte[] data = Files.readAllBytes(agentZipPath);

			String fileName = toolName + FILETYPE;

			sendAgentPackage(data, AGENTACTION.UPDATE.name(), fileName, agentId, toolName, osversion);

			AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
			agentConfigDAL.saveAgentConfigFromUI(agentId, json.get("toolCategory").getAsString(), toolName, json,
					isDataUpdateSupported, uniqueKey, agentVersion, osversion, updateDate);

		} catch (Exception e) {
			log.error("Error updating and installing agent", e);
			throw new InsightsCustomException(e.toString());
		}

		return SUCCESS;
	}

	@Override
	public List<AgentConfigTO> getRegisteredAgents() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfigTO> agentList = null;
		try {
			List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
			agentList = new ArrayList<>(agentConfigList.size());
			for (AgentConfig agentConfig : agentConfigList) {
				AgentConfigTO to = new AgentConfigTO();
				BeanUtils.copyProperties(agentConfig, to);
				agentList.add(to);
			}
		} catch (Exception e) {
			log.error("Error getting all agent config", e);
			throw new InsightsCustomException(e.toString());
		}

		return agentList;
	}

	@Override
	public AgentConfigTO getAgentDetails(String agentId) throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();

		AgentConfigTO agentConfig = new AgentConfigTO();
		try {
			BeanUtils.copyProperties(agentConfigDAL.getAgentConfigurations(agentId), agentConfig);
		} catch (Exception e) {
			log.error("Error getting agent details", e);
			throw new InsightsCustomException(e.toString());
		}
		return agentConfig;
	}

	@Override
	public Map<String, ArrayList<String>> getSystemAvailableAgentList() throws InsightsCustomException {
		Map<String, ArrayList<String>> agentDetails = new TreeMap<>();

		if (!ApplicationConfigProvider.getInstance().getAgentDetails().isOnlineRegistration()) {
			agentDetails = getOfflineSystemAvailableAgentList();
		} else {
			String url = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl();
			Document doc;
			try {
				doc = Jsoup.connect(url).get();
				Elements rows = doc.getElementsByTag("a");
				for (Element element : rows) {
					if (null != element.text() && element.text().startsWith("v")) {
						String version = StringUtils.stripEnd(element.text(), "/");
						ArrayList<String> toolJson = getAgents(version);
						agentDetails.put(version, toolJson);

					}
				}
			} catch (IOException e) {
				log.error("Error while getting system agent list ", e);
				throw new InsightsCustomException(e.toString());
			}
		}

		return agentDetails;
	}

	@Override
	public String getToolRawConfigFile(String version, String tool) throws InsightsCustomException {
		String configJson = null;
		if (!ApplicationConfigProvider.getInstance().getAgentDetails().isOnlineRegistration()) {
			configJson = getOfflineToolRawConfigFile(version, tool);
		} else {
			try {
				String docrootToolPath = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl() + "/"
						+ version + "/agents/" + tool;
				docrootToolPath = docrootToolPath.trim() + "/" + tool.trim() + ".zip";
				String targetDir = ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath()
						+ File.separator + tool;
				configJson = AgentManagementUtil.getInstance()
						.getAgentConfigfile(new URL(docrootToolPath), new File(targetDir)).toString();
			} catch (IOException e) {
				log.error("Error in getting raw config file ", e);
				throw new InsightsCustomException(e.toString());

			}
		}
		return configJson;
	}

	private ArrayList<String> getAgents(String version) {

		Document doc;
		String url = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl() + "/" + version
				+ "/agents/";
		ArrayList<String> tools = new ArrayList<>();
		try {
			doc = Jsoup.connect(url).get();
			Elements rows = doc.getElementsByTag("a");
			for (Element element : rows) {
				if (null != element.text() && element.text().endsWith("/")) {
					tools.add(StringUtils.stripEnd(element.text(), "/"));

				}

			}
		} catch (IOException e) {
			log.debug(e);
		}
		return tools;
	}

	private Map<String, ArrayList<String>> getOfflineSystemAvailableAgentList() throws InsightsCustomException {
		String offlinePath = ApplicationConfigProvider.getInstance().getAgentDetails().getOfflineAgentPath();

		if (offlinePath == null || offlinePath.isEmpty()) {
			log.error("Offline folder path not available");
			throw new InsightsCustomException("Offline folder path not available");
		}

		File[] directories = new File(offlinePath).listFiles();
		Map<String, ArrayList<String>> agentDetails = new HashMap<>();
		for (int i = 0; i < directories.length; i++) {
			ArrayList<String> agentNames = new ArrayList<>(20);
			agentDetails.put(directories[i].getName(), agentNames);

			File[] agents = new File(offlinePath + File.separator + directories[i].getName()).listFiles();
			for (int j = 0; j < agents.length; j++) {
				agentNames.add(agents[j].getName());
			}
		}

		return agentDetails;
	}

	private String getOfflineToolRawConfigFile(String version, String tool) throws InsightsCustomException {
		String offlinePath = ApplicationConfigProvider.getInstance().getAgentDetails().getOfflineAgentPath()
				+ File.separator + version + File.separator + tool;
		String filePath = ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath() + File.separator
				+ tool;

		try {
			FileUtils.copyDirectory(new File(offlinePath), new File(filePath));
		} catch (IOException e) {
			log.error("Error while copying offline tool files to unzip path", e);
			throw new InsightsCustomException(
					"Error while copying offline tool files to unzip path -" + e.getMessage());
		}

		Path dir = Paths.get(filePath);
		String config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith("config.json"));
				FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {

			JsonParser parser = new JsonParser();
			Object obj = parser.parse(reader);
			config = ((JsonObject) obj).toString();
		} catch (IOException e) {
			log.error("Offline file reading issue", e);
			throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
		}

		return config;
	}

	private Path updateAgentConfig(String toolName, JsonObject json) throws IOException {
		String filePath = ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath() + File.separator
				+ toolName;
		File configFile = null;
		// Writing json to file
		Path dir = Paths.get(filePath);
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith("config.json"))) {

			configFile = paths.limit(1).findFirst().get().toFile();
		}

		try (FileWriter file = new FileWriter(configFile)) {
			file.write(json.toString());
			file.flush();
		} catch (IOException e) {
			log.error("Error writing modified json file", e);
			throw e;
		}
		Path sourceFolderPath = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),
				toolName);
		Path zipPath = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),
				toolName + ".zip");
		Path agentZipPath = null;
		try {
			agentZipPath = AgentManagementUtil.getInstance().getAgentZipFolder(sourceFolderPath, zipPath);
		} catch (Exception e) {
			log.error("Error creatig final zip file with modified json file", e);
			throw e;
		}
		return agentZipPath;

	}

	private String updateTrackingJson(String toolName, JsonObject trackingDetails) throws IOException {
		String filePath = ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath() + File.separator
				+ toolName;
		File trackingFile = null;
		// Writing json to file
		Path dir = Paths.get(filePath);
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith("config.json"))) {

			trackingFile = paths.limit(1).findFirst().get().toFile();
			trackingFile = trackingFile.getParentFile();
			dir = Paths.get(trackingFile.toString() + File.separator + "tracking.json");
			trackingFile = dir.toFile();
		}

		try (FileWriter file = new FileWriter(trackingFile)) {
			file.write(trackingDetails.toString());
			file.flush();
		} catch (IOException e) {
			log.error("Error writing tracking json file", e);
			throw e;
		}
		return SUCCESS;

	}

	private String deleteTrackingJson(String toolName) throws IOException {
		String filePath = ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath() + File.separator
				+ toolName;
		File trackingFile = null;
		// Writing json to file
		Path dir = Paths.get(filePath);
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith("config.json"))) {

			trackingFile = paths.limit(1).findFirst().get().toFile();
			trackingFile = trackingFile.getParentFile();
			dir = Paths.get(trackingFile.toString() + File.separator + "tracking.json");
			trackingFile = dir.toFile();
		}
		try {
			if (trackingFile.exists()) {
				trackingFile.delete();
			}
		} catch (NullPointerException e) {
			log.error("No tracking json file found!", e);
			throw e;
		}

		return SUCCESS;

	}

	private void sendAgentPackage(byte[] data, String action, String fileName, String agentId, String toolName,
			String osversion) throws IOException, TimeoutException {
		Map<String, Object> headers = new HashMap<>();
		headers.put("fileName", fileName);
		headers.put("osType", osversion);
		headers.put("agentToolName", toolName);
		headers.put("agentId", agentId);
		headers.put("action", action);

		BasicProperties props = getBasicProperties(headers);

		String agentDaemonQueueName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentPkgQueue();

		publishAgentAction(agentDaemonQueueName, data, props);
	}

	private void uninstallAgent(String action, String agentId, String toolName, String osversion)
			throws IOException, TimeoutException {
		Map<String, Object> headers = new HashMap<>();
		headers.put("osType", osversion);
		headers.put("agentToolName", toolName);
		headers.put("agentId", agentId);
		headers.put("action", action);

		BasicProperties props = getBasicProperties(headers);

		String agentDaemonQueueName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentPkgQueue();

		publishAgentAction(agentDaemonQueueName, action.getBytes(), props);
	}

	private void performAgentAction(String agentId, String action) throws TimeoutException, IOException {
		Map<String, Object> headers = new HashMap<>();
		headers.put("agentId", agentId);

		BasicProperties props = getBasicProperties(headers);
		// agentId will be queue id. Agent code will connect to MQ based on agentId
		// present in config.json
		publishAgentAction(agentId, action.getBytes(), props);
	}

	private void publishAgentAction(String routingKey, byte[] data, BasicProperties props)
			throws TimeoutException, IOException {
		String exchangeName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentExchange();
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(ApplicationConfigProvider.getInstance().getMessageQueue().getHost());
		factory.setUsername(ApplicationConfigProvider.getInstance().getMessageQueue().getUser());
		factory.setPassword(ApplicationConfigProvider.getInstance().getMessageQueue().getPassword());
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(exchangeName, MessageConstants.EXCHANGE_TYPE, true);
		channel.queueDeclare(routingKey, true, false, false, null);
		channel.queueBind(routingKey, exchangeName, routingKey);
		channel.basicPublish(exchangeName, routingKey, props, data);

		channel.close();
		connection.close();
	}

	private BasicProperties getBasicProperties(Map<String, Object> headers) {

		BasicProperties.Builder propertiesBuilder = new BasicProperties.Builder();
		propertiesBuilder.headers(headers);

		return propertiesBuilder.build();
	}

	private String getAgentkey(String toolName) {
		return toolName + "-" + Instant.now().toEpochMilli();
	}
}
