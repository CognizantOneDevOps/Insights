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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.enums.AGENTACTION;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.vault.VaultHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformservice.agentmanagement.util.AgentManagementUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Service("agentManagementService")
public class AgentManagementServiceImpl implements AgentManagementService {
	private static Logger log = LogManager.getLogger(AgentManagementServiceImpl.class);
	private static final String ZIPEXTENSION = ".zip";
	private static final String SUCCESS = "SUCCESS";
	private static final String CONFIG = "config.json";
	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	VaultHandler vaultHandler = new VaultHandler();
	String filePath = ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath();

	@Override
	public String registerAgent(String toolName, String agentVersion, String osversion, String configDetails,
			String trackingDetails, boolean vault) throws InsightsCustomException {
		try {
			String agentId = null;
			Gson gson = new Gson();
			JsonElement jelement = gson.fromJson(configDetails.trim(), JsonElement.class);
			JsonObject json = jelement.getAsJsonObject();
			json.addProperty("osversion", osversion);
			json.addProperty("agentVersion", agentVersion);
			json.addProperty("toolName", toolName.toUpperCase());
			String labelName = getLabelName(configDetails);
			json.addProperty("labelName", labelName);
			if (json.get("agentId") == null || json.get("agentId").getAsString().isEmpty()) {
				agentId = getAgentkey(toolName);
			} else {
				agentId = json.get("agentId").getAsString();
			}
			if (ValidationUtils.checkAgentIdString(agentId)) {
				throw new InsightsCustomException("Agent Id has to be Alpha numeric with '_' as special character");
			}
			if (agentId.equalsIgnoreCase(toolName)) {
				throw new InsightsCustomException("Agent Id and Tool name cannot be the same.");
			}
			// Condition to check whether the agent ID is existing already in database or
			// not.
			if (agentConfigDAL.isAgentIdExisting(agentId)) {
				throw new InsightsCustomException("Agent Id already exsits.");
			}
			json.get("subscribe").getAsJsonObject().addProperty("agentCtrlQueue", agentId);
			Date updateDate = Timestamp.valueOf(LocalDateTime.now());
			/**
			 * Create agent based folder and complete basic steps using agent instance 1.
			 * Create folder with instance id (agent key) 2. Copy all files from agent
			 * folder under instance id folder 3. Replace __AGENT_KEY__ with instance id in
			 * service file based on OS 4. Rename InSights<agentName>Agent.sh to
			 * instanceId.sh name 5. Use new path in rest of the steps for agent
			 * registration
			 */
			setupAgentInstanceCreation(toolName, osversion, agentId);
			// Update tracking.json file
			if (!trackingDetails.isEmpty()) {
				JsonElement trackingJsonElement = gson.fromJson(trackingDetails.trim(), JsonElement.class);
				JsonObject trackingDetailsJson = trackingJsonElement.getAsJsonObject();
				updateTrackingJson(toolName, trackingDetailsJson, agentId);
			}
			// Store secrets to vault based on agentsSecretDetails in config.json
			if (vault) {
				log.debug("-- Store secrets to vault for Agent {} --", agentId);
				prepareSecret(agentId, json);
			}
			// Create zip/tar file with updated config.json
			// Zipping back the agent folder
			Path agentZipPath = updateAgentConfig(toolName, json, agentId);
			byte[] data = Files.readAllBytes(agentZipPath);
			String fileName = agentId + ZIPEXTENSION;
			// Sending the packet in Rabbit MQ
			sendAgentPackage(data, AGENTACTION.REGISTER.name(), fileName, agentId, toolName, osversion);
			performAgentAction(agentId, toolName, osversion, AGENTACTION.START.name(), agentId);
			// Delete tracking.json
			if (!trackingDetails.isEmpty()) {
				deleteTrackingJson(agentId);
			}
			// register agent in DB
			agentConfigDAL.saveAgentConfigFromUI(agentId, json.get("toolCategory").getAsString(), labelName, toolName,
					json, agentVersion, osversion, updateDate, vault);
			return SUCCESS;
		} catch (Exception e) {
			log.error("Error while registering agent {} ", toolName, e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	@Override
	public String uninstallAgent(String agentId, String toolName, String osversion) throws InsightsCustomException {
		try {
			uninstallAgent(AGENTACTION.UNINSTALL.name(), agentId, toolName, osversion);
			agentConfigDAL.deleteAgentConfigurations(agentId);
		} catch (Exception e) {
			log.error("Error while un-installing agent..", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return SUCCESS;
	}

	@Override
	public String startStopAgent(String agentId, String toolName, String osversion, String action)
			throws InsightsCustomException {
		try {
			String agentDaemonQueueName = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentPkgQueue();
			if (AGENTACTION.START.equals(AGENTACTION.valueOf(action))) {
				performAgentAction(agentId, toolName, osversion, action, agentDaemonQueueName);
			} else if (AGENTACTION.STOP.equals(AGENTACTION.valueOf(action)) && "WINDOWS".equalsIgnoreCase(osversion)) {
				performAgentAction(agentId, toolName, osversion, action, agentDaemonQueueName);
			} else {
				performAgentAction(agentId, toolName, osversion, action, agentId);
			}
			// Update status in DB
			agentConfigDAL.updateAgentRunningStatus(agentId, AGENTACTION.valueOf(action));
		} catch (Exception e) {
			log.error("Error while agent {} ", action, e);
			throw new InsightsCustomException(e.toString());
		}
		return SUCCESS;
	}

	@Override
	public String updateAgent(String agentId, String configDetails, String toolName, String agentVersion,
			String osversion, boolean vault) throws InsightsCustomException {
		try {
			if (!agentConfigDAL.isAgentIdExisting(agentId)) {
				throw new InsightsCustomException("No data found for agent id " + agentId);
			}
			// Get latest agent code
			String labelName = null;
			getToolRawConfigFile(agentVersion, toolName);
			setupAgentInstanceCreation(toolName, osversion, agentId);
			AgentConfigTO agentConfig = getAgentDetails(agentId);
			String oldVersion = agentConfig.getAgentVersion();
			log.debug("Previous Agent version {} ---", agentConfig.getAgentVersion());
			if (!oldVersion.equals(agentVersion)) {
				// Get latest agent code
				getToolRawConfigFile(agentVersion, toolName);
				setupAgentInstanceCreation(toolName, osversion, agentId);
			}
			Gson gson = new Gson();
			JsonElement jelement = gson.fromJson(configDetails.trim(), JsonElement.class);
			JsonObject json = jelement.getAsJsonObject();
			json.addProperty("osversion", osversion);
			json.addProperty("agentVersion", agentVersion);
			Date updateDate = Timestamp.valueOf(LocalDateTime.now());
			if (vault) {
				log.debug("--update Store secrets to vault --");
				Map<String, String> dataMap = getToolbasedSecret(json, agentId);
				updateSecrets(agentId, dataMap, json);
			}
			Path agentZipPath = updateAgentConfig(toolName, json, agentId);
			byte[] data = Files.readAllBytes(agentZipPath);
			String fileName = agentId + ZIPEXTENSION;
			sendAgentPackage(data, AGENTACTION.UPDATE.name(), fileName, agentId, toolName, osversion);
			labelName = this.getLabelName(configDetails);
			agentConfigDAL.updateAgentConfigFromUI(agentId, json.get("toolCategory").getAsString(), labelName, toolName,
					json, agentVersion, osversion, updateDate, vault);
			return SUCCESS;
		} catch (Exception e) {
			log.error("Error while updating agent", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	@Override
	public List<AgentConfigTO> getRegisteredAgents() throws InsightsCustomException {
		List<AgentConfigTO> agentList = null;
		try {
			List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
			agentList = new ArrayList<>(agentConfigList.size());
			for (AgentConfig agentConfig : agentConfigList) {
				AgentConfigTO to = new AgentConfigTO();
				BeanUtils.copyProperties(agentConfig, to, "agentJson", "updatedDate", "vault");
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
	public Map<String, ArrayList<String>> getDocrootAvailableAgentList() throws InsightsCustomException {
		Map<String, ArrayList<String>> agentDetails = new TreeMap<>();
		String url = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl();
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Elements rows = doc.getElementsByTag("a");
			for (Element element : rows) {
				if (null != element.text() && element.text().startsWith("v")) {
					String version = StringUtils.stripEnd(element.text(), "/");
					ArrayList<String> toolJson = getAgentsFromDocroot(version);
					agentDetails.put(version, toolJson);
				}
			}
		} catch (IOException e) {
			log.error("Error while getting system agent list from docroot ", e);
			throw new InsightsCustomException(e.toString());
		}
		return agentDetails;
	}

	@Override
	public Map<String, ArrayList<String>> getRepoAvailableAgentList() throws InsightsCustomException {
		Map<String, ArrayList<String>> agentDetails = new TreeMap<>();
		log.debug("Inside getRepoAvailableAgentList for nexus repo ");
		String url = ApplicationConfigProvider.getInstance().getAgentDetails().getBrowseRepoUrl();
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Elements rows = doc.getElementsByTag("a");
			for (Element element : rows) {
				if (null != element.text() && element.text().startsWith("v")) {
					String version = element.text();
					ArrayList<String> toolJson = getAgentsForRepo(version);
					agentDetails.put(version, toolJson);
				}
			}
		} catch (IOException e) {
			log.error("Error while getting system agent list ", e);
			throw new InsightsCustomException(e.toString());
		}
		return agentDetails;
	}

	@Override
	public String getToolRawConfigFile(String version, String tool) throws InsightsCustomException {
		String configJson = null;
		String toolPath = null;
		if (!ApplicationConfigProvider.getInstance().getAgentDetails().isOnlineRegistration()) {
			configJson = getOfflineToolRawConfigFile(version, tool);
		} else {
			try {
				if (ApplicationConfigProvider.getInstance().getAgentDetails()
						.getOnlineRegistrationMode().equalsIgnoreCase(ConfigOptions.ONLINE_REGISTRATION_MODE_DOCROOT)) {
					toolPath = ApplicationConfigProvider.getInstance().getAgentDetails().getDocrootUrl() + "/" + version
							+ "/agents/" + tool;
					toolPath = toolPath.trim() + "/" + tool.trim() + ZIPEXTENSION;
				} else {
					toolPath = ApplicationConfigProvider.getInstance().getAgentDetails().getDownloadRepoUrl() + "/"
							+ version + "/agents/" + tool;
					toolPath = toolPath.trim() + "/" + tool.trim() + ZIPEXTENSION;
				}
				String targetDir = ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath()
						+ File.separator + tool;
				configJson = AgentManagementUtil.getInstance()
						.getAgentConfigfile(new URL(toolPath), new File(targetDir)).toString();
			} catch (IOException e) {
				log.error("Error in getting raw config file ", e);
				throw new InsightsCustomException(e.toString());

			}
		}
		return configJson;
	}

	private ArrayList<String> getAgentsForRepo(String version) {
		Document doc;
		String url = ApplicationConfigProvider.getInstance().getAgentDetails().getBrowseRepoUrl() + "/" + version
				+ "/agents/";
		ArrayList<String> tools = new ArrayList<>();
		try {
			doc = Jsoup.connect(url).get();
			Elements rows = doc.getElementsByTag("a");
			for (Element element : rows) {
				if (null != element.text() && !(element.text().startsWith("Parent"))) {
					tools.add(element.text());
				}
			}
		} catch (IOException e) {
			log.error("Error while fetching agents", e);
		}
		return tools;
	}

	private ArrayList<String> getAgentsFromDocroot(String version) {
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
			log.error("Error while fetching agents", e);
		}
		return tools;
	}

	public Map<String, ArrayList<String>> getOfflineSystemAvailableAgentList() throws InsightsCustomException {
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
		String agentPath = filePath + File.separator + tool;
		try {
			FileUtils.copyDirectory(new File(offlinePath), new File(agentPath));
		} catch (IOException e) {
			log.error("Error while copying offline tool files to unzip path", e);
			throw new InsightsCustomException(
					"Error while copying offline tool files to unzip path -" + e.getMessage());
		}
		Path dir = Paths.get(agentPath);
		String config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(CONFIG));
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

	/**
	 * Create agent based folder and complete basic steps using agent instance 1.
	 * Create folder with instance id (agent key) 2. Copy all files from agent
	 * folder uder instance id folder 3. Replace __AGENT_KEY__ with instance id in
	 * service file based on OS 4. Rename InSights<agentName>Agent.sh to
	 * instanceId.sh name 5. Use new path in rest of the steps for agent
	 * registration
	 * 
	 * @throws IOException
	 */
	private void setupAgentInstanceCreation(String toolName, String osversion, String agentId) throws IOException {
		Path toolUnzipPath = Paths.get(filePath + File.separator + toolName);
		File instanceDir = new File(filePath + File.separator + agentId);
		if (!instanceDir.exists()) {
			instanceDir.mkdir();
		}
		copyServiceFileToInstanceFolder(toolName, agentId, osversion);
		copyPythonCodeToInstanceFolder(toolName, agentId);
	}

	private void copyServiceFileToInstanceFolder(String toolName, String agentId, String osversion) throws IOException {
		Path sourceFilePath = Paths.get(filePath + File.separator + toolName);
		Path destinationFilePath = Paths.get(filePath + File.separator + agentId);
		if ("Windows".equalsIgnoreCase(osversion)) {
			Path destinationFile = destinationFilePath.resolve(agentId + ".bat");
			Files.copy(sourceFilePath.resolve(toolName + "agent.bat"), destinationFile, REPLACE_EXISTING);
			addAgentKeyToServiceFile(destinationFile, agentId);
		} else if ("linux".equalsIgnoreCase(osversion)) {
			Path destinationFile = destinationFilePath.resolve(agentId + ".sh");
			Files.copy(sourceFilePath.resolve(toolName + "agent.sh"), destinationFile, REPLACE_EXISTING);
			addAgentKeyToServiceFile(destinationFile, agentId);
			addProcessKeyToServiceFile(destinationFile, agentId);
		} else if ("Ubuntu".equalsIgnoreCase(osversion)) {
			Path destinationFile = destinationFilePath.resolve(agentId + ".sh");
			Files.copy(sourceFilePath.resolve(toolName + "agent.sh"), destinationFile, REPLACE_EXISTING);
			addAgentKeyToServiceFile(destinationFile, agentId);
			addProcessKeyToServiceFile(destinationFile, agentId);

			Path destinationServiceFile = destinationFilePath.resolve(agentId + ".service");
			Files.copy(sourceFilePath.resolve(toolName + "agent.service"), destinationServiceFile, REPLACE_EXISTING);
			addAgentKeyToServiceFile(destinationServiceFile, agentId);
		}
	}

	private void copyPythonCodeToInstanceFolder(String toolName, String agentId) throws IOException {
		Path sourcePath = Paths.get(filePath + File.separator + toolName);
		Path destinationPath = Paths.get(filePath + File.separator + agentId);
		// Copy __init__.py to agent instance folder, otherwise python code wont work
		Files.copy(
				Paths.get(
						filePath + File.separator + toolName + File.separator + "com" + File.separator + "__init__.py"),
				Paths.get(filePath + File.separator + agentId + File.separator + "__init__.py"), REPLACE_EXISTING);
		FileUtils.deleteDirectory(destinationPath.resolve("com").toFile());
		FileUtils.copyDirectory(sourcePath.resolve("com").toFile(), destinationPath.resolve("com").toFile());
	}

	private void addAgentKeyToServiceFile(Path destinationFile, String agentId) throws IOException {
		try (Stream<String> lines = Files.lines(destinationFile)) {
			List<String> replaced = lines.map(line -> line.replaceAll("__AGENT_KEY__", agentId))
					.collect(Collectors.toList());
			Files.write(destinationFile, replaced);
		}
	}

	private void addProcessKeyToServiceFile(Path destinationFile, String agentId) throws IOException {
		String psKey = getPSKey(agentId);
		try (Stream<String> lines = Files.lines(destinationFile)) {
			List<String> replaced = lines.map(line -> line.replaceAll("__PS_KEY__", psKey))
					.collect(Collectors.toList());
			Files.write(destinationFile, replaced);
		}
	}

	private String getPSKey(String agentId) {
		Character firstChar = agentId.charAt(0);
		return "[" + firstChar + "]" + agentId.substring(1) + ".com";
	}

	private Path updateAgentConfig(String toolName, JsonObject json, String agentId)
			throws IOException, InsightsCustomException {
		String configFilePath = filePath + File.separator + agentId;
		File configFile = null;
		// Writing json to file
		Path dir = Paths.get(configFilePath);
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(CONFIG))) {

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
				agentId);
		Path zipPath = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),
				agentId + ZIPEXTENSION);
		Path agentZipPath = null;
		try {
			agentZipPath = AgentManagementUtil.getInstance().getAgentZipFolder(sourceFolderPath, zipPath);
		} catch (Exception e) {
			log.error("Error creatig final zip file with modified json file", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return agentZipPath;

	}

	private String updateTrackingJson(String toolName, JsonObject trackingDetails, String agentId)
			throws IOException, InsightsCustomException {
		String trackingFilePath = filePath + File.separator + agentId;
		File trackingFile = null;
		// Writing json to file
		Path dir = Paths.get(trackingFilePath);
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(CONFIG))) {
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
			throw new InsightsCustomException(e.getMessage());
		}
		return SUCCESS;

	}

	private String deleteTrackingJson(String agentId) throws IOException, InsightsCustomException {
		String trackingFilePath = filePath + File.separator + agentId;
		File trackingFile = null;
		// Writing json to file
		Path dir = Paths.get(trackingFilePath);
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(CONFIG))) {

			trackingFile = paths.limit(1).findFirst().get().toFile();
			trackingFile = trackingFile.getParentFile();
			dir = Paths.get(trackingFile.toString() + File.separator + "tracking.json");
			trackingFile = dir.toFile();
		}
		try {
			if (trackingFile.exists()) {
				Files.delete(trackingFile.toPath());
			}
		} catch (NullPointerException e) {
			log.error("No tracking json file found!", e);
			throw new InsightsCustomException(e.getMessage());
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

	private void performAgentAction(String agentId, String toolName, String osversion, String action, String queueName)
			throws TimeoutException, IOException {
		Map<String, Object> headers = new HashMap<>();
		headers.put("osType", osversion);
		headers.put("agentToolName", toolName);
		headers.put("agentId", agentId);
		headers.put("action", action);
		BasicProperties props = getBasicProperties(headers);
		publishAgentAction(queueName, action.getBytes(), props);
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
		channel.exchangeDeclare(exchangeName, MQMessageConstants.EXCHANGE_TYPE, true);
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
		return toolName + "_" + Instant.now().toEpochMilli();
	}

	private String getLabelName(String configDetails) throws InsightsCustomException {
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(configDetails.trim(), JsonElement.class);
		JsonObject json = jelement.getAsJsonObject();
		List<String> labelDataValue = null;
		try {
			String labelData = json.get("publish").getAsJsonObject().get("data").getAsString().toUpperCase();
			String labelHealth = json.get("publish").getAsJsonObject().get("health").getAsString().toUpperCase();
			if (ValidationUtils.checkLabelNameString(labelData)) {
				throw new InsightsCustomException(
						"Invalid data label Name, it should contain only alphanumeric character,underscore & dot");
			}
			if (ValidationUtils.checkLabelNameString(labelHealth)) {
				throw new InsightsCustomException(
						"Invalid health label Name, it should contain only alphanumeric character,underscore & dot");
			}
			labelDataValue = Arrays.asList(labelData.split(MQMessageConstants.ROUTING_KEY_SEPERATOR));

		} catch (Exception e) {
			log.error("Invalid label Name {} ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return labelDataValue.get(1);

	}

	/**
	 * Prepare vault based structure and store in vault.
	 * 
	 * @param agentId
	 * @param json
	 * @return
	 * @throws InsightsCustomException
	 * @throws RuntimeException
	 * @throws ClientHandlerException
	 * @throws UniformInterfaceException
	 * @throws IOException
	 */
	private void prepareSecret(String agentId, JsonObject json) throws InsightsCustomException, IOException {
		Map<String, String> dataMap = getToolbasedSecret(json, agentId);
		if (!dataMap.isEmpty()) {
			vaultSecret(agentId, null, dataMap);
		}
	}

	/**
	 * fetches tool based secrets to be stored in vault from {agentSecretDetails} in
	 * config.json
	 * 
	 * @param toolName
	 * @param json
	 * @param agentId
	 * @return dataMap
	 * @throws IOException
	 */
	private Map<String, String> getToolbasedSecret(JsonObject json, String agentId) throws IOException {
		HashMap<String, String> secretMap = new HashMap<>();
		String configFilePath = filePath + File.separator + agentId;
		File configFile = null;
		Path dir = Paths.get(configFilePath);
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(CONFIG))) {
			configFile = paths.limit(1).findFirst().get().toFile();
		}
		JsonObject configObject = AgentManagementUtil.getInstance().convertFileToJSON(configFile);
		JsonArray agentSecrets = configObject.get("agentSecretDetails").getAsJsonArray();

		for (JsonElement secret : agentSecrets) {
			if (json.has(secret.getAsString())) {
				secretMap.put(secret.getAsString(), json.get(secret.getAsString()).getAsString());
				json.addProperty(secret.getAsString(), "*****");
			} else {
				log.debug("No such secret in config.json ");
			}
		}
		// TO Fetch during update
		json.add("agentSecretDetails", agentSecrets);
		// Add Vault creds to be used when running pyhton agents
		JsonObject vaultObj = new JsonObject();
		vaultObj.addProperty("getFromVault", true);
		vaultObj.addProperty("secretEngine", ApplicationConfigProvider.getInstance().getVault().getSecretEngine());
		vaultObj.addProperty("readToken", ApplicationConfigProvider.getInstance().getVault().getVaultToken());
		vaultObj.addProperty("vaultUrl", ApplicationConfigProvider.getInstance().getVault().getVaultEndPoint());
		json.add("vault", vaultObj);
		log.debug("Updated Json {} without creds --", json);
		return secretMap;
	}

	/**
	 * Compare vault secret with UI value and update the vault with new secrets
	 * 
	 * @param agentId
	 * @param secretMap
	 * @param json
	 * @throws InsightsCustomException
	 * @throws IOException
	 */
	private void updateSecrets(String agentId, Map<String, String> updatedDataMap, JsonObject json)
			throws InsightsCustomException, IOException {
		String vaultresponse = vaultHandler.fetchFromVault(agentId);
		JsonObject vaultObject = new JsonParser().parse(vaultresponse).getAsJsonObject();
		JsonObject vaultData = vaultObject.get("data").getAsJsonObject().get("data").getAsJsonObject();
		// update secrets based on vault values
		for (Entry<String, String> field : updatedDataMap.entrySet()) {
			if (vaultData.has(field.getKey()) && !field.getValue().contains("***")) {
				updatedDataMap.put(field.getKey(), field.getValue());
			} else if (vaultData.has(field.getKey())) {
				updatedDataMap.put(field.getKey(), vaultData.get(field.getKey()).getAsString());
			} else {
				updatedDataMap.put(field.getKey(), field.getValue());
			}
		}
		if (!updatedDataMap.isEmpty()) {
			vaultSecret(agentId, vaultData, updatedDataMap);
		}

	}

	private Map<String, String> vaultSecret(String agentId, JsonObject vaultData, Map<String, String> updatedDataMap)
			throws InsightsCustomException {
		Gson gson = new Gson();
		JsonObject updatedDataJson = gson.toJsonTree(updatedDataMap).getAsJsonObject();
		// In registeragent case existing vaultData is null and in updateAgent case it
		// has exising valut value
		if ((vaultData == null && !updatedDataMap.isEmpty())
				|| (vaultData != null && vaultData.entrySet().size() > 0 && !updatedDataJson.equals(vaultData))) {
			vaultHandler.storeToVault(updatedDataJson, agentId);
		}
		return updatedDataMap;

	}

}
