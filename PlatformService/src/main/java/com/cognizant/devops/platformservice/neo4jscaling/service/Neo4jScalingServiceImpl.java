/*******************************************************************************
 * Copyright 2024 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.neo4jscaling.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.AWSSQSProvider;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQProvider;
import com.cognizant.devops.platformdal.neo4jScaling.InsightsReplicaConfig;
import com.cognizant.devops.platformdal.neo4jScaling.InsightsReplicaConfigDAL;
import com.cognizant.devops.platformdal.neo4jScaling.InsightsStreamsSourceConfig;
import com.cognizant.devops.platformdal.neo4jScaling.InsightsStreamsSourceConfigDAL;
import com.cognizant.devops.platformservice.security.config.saml.ResourceLoaderService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.ws.rs.ProcessingException;

@Service("neo4jScalingService")
public class Neo4jScalingServiceImpl implements Neo4jScalingService {
	private static Logger log = LogManager.getLogger(Neo4jScalingServiceImpl.class);
	InsightsStreamsSourceConfigDAL streamsConfigDAL = new InsightsStreamsSourceConfigDAL();
	InsightsReplicaConfigDAL replicaConfigDAL = new InsightsReplicaConfigDAL();
	@Autowired
	ResourceLoaderService resourceLoaderService;

	private String mqProviderName = ApplicationConfigProvider.getInstance().getMessageQueue().getProviderName();
	private static final String REPLICA_PACKAGE_QUEUE = "INSIGHTS.REPLICA.PACKAGE";
	private static final String STREAMSCONF = "STREAMSCONF"; 
	private static final String INSIGHTS_HOME = "INSIGHTS_HOME";
	private static final String REPLICA_DAEMON = "ReplicaDaemon";
	private String RESULTS = "results";
	private String ROW = "row";
	private String DATA = "data";
	private String REPLICA_IP = "replicaIP";
	private String REPLICA_NAME = "replicaName";
	private String REPLICA_ENDPOINT = "replicaEndpoint";
	private String SINK_STREAMS_CONFIG_FILE = "SinkStreams.conf";
	private String SOURCE_STREAMS_CONFIG_FILE = "SourceStreams.conf";

	@Override
	public JsonObject getNeo4jScalingConfigs() throws InsightsCustomException {
		JsonObject response = new JsonObject();
		try {
			InsightsStreamsSourceConfig sourceStreamsConfig = streamsConfigDAL.getStreamsSourceConfig(STREAMSCONF);
			if(sourceStreamsConfig == null) {
				return response;
			}
			List<InsightsReplicaConfig> allReplicaConfig = replicaConfigDAL.getAllReplicaConfig();
			JsonObject sourceStreamsConfigJson = new JsonObject();
			sourceStreamsConfigJson.addProperty("kafkaEndpoint", sourceStreamsConfig.getKafkaEndpoint());
			sourceStreamsConfigJson.addProperty("topicName", sourceStreamsConfig.getTopicName());
			sourceStreamsConfigJson.addProperty("sourceServerIP", sourceStreamsConfig.getSourceIP());
			sourceStreamsConfigJson.addProperty("nodeLabels", sourceStreamsConfig.getNodeLabels());
			sourceStreamsConfigJson.addProperty("relationshipLabels", sourceStreamsConfig.getRelationshipLabels());
			response.add("sourceStreamsConfig", sourceStreamsConfigJson);
			JsonArray replicaJsonArr = new JsonArray();

			for (InsightsReplicaConfig insightsReplicaConfig : allReplicaConfig) {
				JsonObject replicaJson = new JsonObject();
				replicaJson.addProperty(REPLICA_IP, insightsReplicaConfig.getReplicaIP());
				replicaJson.addProperty(REPLICA_NAME, insightsReplicaConfig.getReplicaName());
				replicaJson.addProperty(REPLICA_ENDPOINT, insightsReplicaConfig.getReplicaEndpoint());
				replicaJsonArr.add(replicaJson);
			}
			response.add("replicaConfig", replicaJsonArr);
		} catch (Exception e) {
			log.error("Error while fetching all scaling configs", e);
			throw new InsightsCustomException("Error while fetching all scaling configs: " + e.toString());
		}
		return response;
	}

	// Main method to save the Neo4j scaling details & perform tasks.
	@Override
	public String saveNeo4jScalingConfigs(JsonObject sourceConfigJson, JsonObject replicaConfigJson)
			throws InsightsCustomException {
		String message = "";
		try {
			// To save source config details
			saveSourceConfigInDB(sourceConfigJson);
			// To save Replica config details
			saveReplicaConfigsInDB(replicaConfigJson);
			// To update source streams file
			updateSourceStreamsConfigFile();
			// To update replica streams file
			updateSinkStreamsConfigFiles();
			// To publish agent message
			publishMessageForAgent();
			message = "Successfully Saved Configs";
		} catch (Exception e) {
			log.error("Error saving neo4j scaling configs {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return message;
	}

	public int getTotalNodes(String ip, String labels) throws InsightsCustomException {
		int result = 0;
		JsonObject graphResult = new JsonObject();
		labels = labels.replace("{*}", "");
		String[] lablesArr = labels.split(";");
		try (GraphDBHandler graphDBHandler = new GraphDBHandler("4.4.4", ip)) {
			for (String label : lablesArr) {
				if (!label.equals("")) {
					graphResult = graphDBHandler
							.executeCypherQueryForJsonResponse("Match (n:" + label + ") return count(n) as totalNodes");
					result += graphResult.get(RESULTS).getAsJsonArray().get(0).getAsJsonObject().get(DATA)
							.getAsJsonArray().get(0).getAsJsonObject().get(ROW).getAsJsonArray().get(0).getAsInt();
				}
			}
		} catch (ProcessingException e) {
			log.error("Error while fetching total node count from Neo4j {}", e.getMessage());
			throw new InsightsCustomException("Failed to get node count from Neo4j");
		} catch (Exception e) {
			log.error("Error while fetching total node count {}", e.getMessage());
			throw new InsightsCustomException("Failed to get node count due to exeception");
		}
		return result;
	}

	public int getTotalRelations(String ip, String labels) throws InsightsCustomException {
		int result = 0;
		JsonObject graphResult = new JsonObject();
		labels = labels.replace("{*}", "");
		String[] lablesArr = labels.split(";");
		try (GraphDBHandler graphDBHandler = new GraphDBHandler("4.4.4", ip)) {
			for (String label : lablesArr) {
				if (!label.equals("")) {
					graphResult = graphDBHandler.executeCypherQueryForJsonResponse(
							"Match p=()-[r:" + label + "]->() return count(r) as totalRelations");
					result += graphResult.get(RESULTS).getAsJsonArray().get(0).getAsJsonObject().get(DATA)
							.getAsJsonArray().get(0).getAsJsonObject().get(ROW).getAsJsonArray().get(0).getAsInt();
				}
			}
		} catch (ProcessingException e) {
			log.error("Error while fetching total relationship count from Neo4j {}", e.getMessage());
			throw new InsightsCustomException("Failed to get relationship count from Neo4j");
		} catch (Exception e) {
			log.error("Error while fetching total relationship count {}", e.getMessage());
			throw new InsightsCustomException("Failed to get relationship count due to exeception");
		}
		return result;
	}

	private void performAgentAction(String action, String queueName, JsonObject payload)
			throws InsightsCustomException {
		JsonObject json = new JsonObject();
		json.addProperty(AgentCommonConstant.ACTION, action);
		json.add("payload", payload);
		publishAgentAction(queueName, json);
	}

	private void publishAgentAction(String routingKey, JsonObject json) throws InsightsCustomException {
		String queueName = routingKey.replace(".", "_");
		try {
			if (this.mqProviderName.equalsIgnoreCase("AWSSQS"))
				AWSSQSProvider.publish(queueName, json.toString());
			else {
				RabbitMQProvider.publish(queueName, json.toString());
			}
		} catch (Exception e) {
			log.error("Error while publishing message to MQ {}", e.getMessage());
			throw new InsightsCustomException("Failed to publish message to MQ");
		}
	}

	private void saveSourceConfigInDB(JsonObject sourceConfigJson) throws InsightsCustomException {
		try {
			boolean isUpdate = false;
			InsightsStreamsSourceConfig streamsSourceConfig = streamsConfigDAL.getStreamsSourceConfig(STREAMSCONF);
			if (streamsSourceConfig == null) {
				streamsSourceConfig = new InsightsStreamsSourceConfig();
			} else {
				isUpdate = true;
			}
			streamsSourceConfig.setKafkaEndpoint(sourceConfigJson.get("kafkaEndpoint").getAsString());
			streamsSourceConfig.setConfigID(STREAMSCONF);
			streamsSourceConfig.setTopicName(sourceConfigJson.get("topicName").getAsString());
			streamsSourceConfig.setSourceIP(getMasterNeo4jIP());
			streamsSourceConfig.setNodeLabels(sourceConfigJson.get("nodeLabels").getAsString());
			streamsSourceConfig.setRelationshipLabels(sourceConfigJson.get("relationshipLabels").getAsString());

			if (isUpdate) {
				streamsConfigDAL.update(streamsSourceConfig);
			} else {
				streamsConfigDAL.save(streamsSourceConfig);
			}
		} catch (Exception e) {
			log.error("Error while saving source config in DB {}", e.getMessage());
			throw new InsightsCustomException("Failed to save source config");
		}
	}

	private void saveReplicaConfigsInDB(JsonObject replicaConfigJson) throws InsightsCustomException {
		try {
			List<InsightsReplicaConfig> allReplicaConfig = replicaConfigDAL.getAllReplicaConfig();
			String masterIP = getMasterNeo4jIP();
			for (JsonElement replica : replicaConfigJson.get("replicas").getAsJsonArray()) {
				boolean isFound = false;
				String replicaIP = replica.getAsJsonObject().get(REPLICA_IP).getAsString();
				String replicaEndpoint = replica.getAsJsonObject().get(REPLICA_ENDPOINT).getAsString();
				if (replicaIP.equalsIgnoreCase(masterIP) || replicaEndpoint.contains(masterIP)) {
					throw new InsightsCustomException("Can't save master neo4j as replica");
				}
				if (getTotalNodes(replicaEndpoint, "DATA{*}") >= 0) {
					for (InsightsReplicaConfig insightsReplicaConfig : allReplicaConfig) {
						if (insightsReplicaConfig.getReplicaName()
								.equals(replica.getAsJsonObject().get(REPLICA_NAME).getAsString())) {
							isFound = true;
							break;
						}
					}
					if (!isFound) {
						InsightsReplicaConfig replicaConfig = new InsightsReplicaConfig();
						replicaConfig.setReplicaName(replica.getAsJsonObject().get(REPLICA_NAME).getAsString());
						replicaConfig.setReplicaIP(replica.getAsJsonObject().get(REPLICA_IP).getAsString());
						replicaConfig.setReplicaEndpoint(replica.getAsJsonObject().get(REPLICA_ENDPOINT).getAsString());
						replicaConfigDAL.save(replicaConfig);
					}
				}
			}

		} catch (Exception e) {
			log.error("Error while saving replica configs in DB {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	private void updateSourceStreamsConfigFile() throws InsightsCustomException {
		try {
			Resource resource = resourceLoaderService.getResource("classpath:source-streams-template.conf");
			InputStream sourceConfInput = resource.getInputStream();
			File configFile = new File(
					System.getenv().get(INSIGHTS_HOME) + File.separator + REPLICA_DAEMON + File.separator + SOURCE_STREAMS_CONFIG_FILE);
			InsightsStreamsSourceConfig sourceStreamsConfig = streamsConfigDAL.getStreamsSourceConfig(STREAMSCONF);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(sourceConfInput));
					FileWriter writer = new FileWriter(configFile);) {
				String line;
				while ((line = reader.readLine()) != null) {
					String template = line.replace(PlatformServiceConstants.LF, PlatformServiceConstants.EMPTY)
							.replace(PlatformServiceConstants.CR, PlatformServiceConstants.EMPTY)
							.replace(PlatformServiceConstants.TAB, PlatformServiceConstants.EMPTY);

					String validatedstr = ValidationUtils.cleanXSSWithHTMLCheckForCypherQuery(template);
					validatedstr = validatedstr.replace("<TOPIC_NAME>", sourceStreamsConfig.getTopicName());
					validatedstr = validatedstr.replace("<KAFKA_ENDPOINT>", sourceStreamsConfig.getKafkaEndpoint());
					validatedstr = validatedstr.replace("<NODE_PATTERN>", sourceStreamsConfig.getNodeLabels());
					validatedstr = validatedstr.replace("<REL_PATTERN>", sourceStreamsConfig.getRelationshipLabels());
					validatedstr = validatedstr.replace("<DB_NAME>",
							ApplicationConfigProvider.getInstance().getGraph().getDatabaseName());
					writer.write(validatedstr + "\r\n");
				}
			}

		} catch (Exception e) {
			log.error("Error while updating source streams config file {}", e.getMessage());
			throw new InsightsCustomException("Failed to update source streams config file");
		}
	}

	private void updateSinkStreamsConfigFiles() throws InsightsCustomException {
		try {
			Resource resource = resourceLoaderService.getResource("classpath:sink-streams-template.conf");
			List<InsightsReplicaConfig> allReplicaConfig = replicaConfigDAL.getAllReplicaConfig();
			InsightsStreamsSourceConfig sourceStreamsConfig = streamsConfigDAL.getStreamsSourceConfig(STREAMSCONF);

			for (InsightsReplicaConfig insightsReplicaConfig : allReplicaConfig) {
				InputStream sinkConfInput = resource.getInputStream();
				File configFile = new File(System.getenv().get(INSIGHTS_HOME) + File.separator + REPLICA_DAEMON + File.separator 
						+ insightsReplicaConfig.getReplicaName() + "_" + SINK_STREAMS_CONFIG_FILE);

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(sinkConfInput));
						FileWriter writer = new FileWriter(configFile);) {
					String line;
					while ((line = reader.readLine()) != null) {
						String template = line.replace(PlatformServiceConstants.LF, PlatformServiceConstants.EMPTY)
								.replace(PlatformServiceConstants.CR, PlatformServiceConstants.EMPTY)
								.replace(PlatformServiceConstants.TAB, PlatformServiceConstants.EMPTY);

						String validatedstr = ValidationUtils.cleanXSSWithHTMLCheckForCypherQuery(template);
						validatedstr = validatedstr.replace("<TOPIC_NAME>", sourceStreamsConfig.getTopicName());
						validatedstr = validatedstr.replace("<KAFKA_ENDPOINT>", sourceStreamsConfig.getKafkaEndpoint());
						validatedstr = validatedstr.replace("<REPLICA_NAME>", insightsReplicaConfig.getReplicaName());
						writer.write(validatedstr + "\r\n");
					}
				}
			}

		} catch (Exception e) {
			log.error("Error while updating replica streams config file {}", e.getMessage());
			throw new InsightsCustomException("Failed to update replica streams config file");
		}
	}
	
	private void publishMessageForAgent() throws InsightsCustomException {
		JsonObject payloadJson = new JsonObject();
		JsonObject sourceJson = new JsonObject();
		JsonArray replicaJsonArr = new JsonArray();
		try {
			List<InsightsReplicaConfig> allReplicaConfig = replicaConfigDAL.getAllReplicaConfig();
			InsightsStreamsSourceConfig sourceStreamsConfig = streamsConfigDAL.getStreamsSourceConfig(STREAMSCONF);
			File sourceConfigFile = new File(
					System.getenv().get(INSIGHTS_HOME) + File.separator + REPLICA_DAEMON + File.separator + SOURCE_STREAMS_CONFIG_FILE);
			if (sourceConfigFile.exists() && !sourceConfigFile.isDirectory()) {
				sourceJson.addProperty("ip", sourceStreamsConfig.getSourceIP());
				sourceJson.addProperty("kafka_endpoint", sourceStreamsConfig.getKafkaEndpoint().replace(":", " "));
				sourceJson.addProperty("fileName", SOURCE_STREAMS_CONFIG_FILE);
			} else {
				throw new InsightsCustomException("Source Streams Config file doesn't exist");
			}
			for (InsightsReplicaConfig insightsReplicaConfig : allReplicaConfig) {
				JsonObject replicaJson = new JsonObject();
				File sinkConfigFile = new File(System.getenv().get(INSIGHTS_HOME) + File.separator
						+ REPLICA_DAEMON + File.separator + insightsReplicaConfig.getReplicaName() + "_" + SINK_STREAMS_CONFIG_FILE);
				if (sinkConfigFile.exists() && !sinkConfigFile.isDirectory()) {
					replicaJson.addProperty("ip", insightsReplicaConfig.getReplicaIP());
					replicaJson.addProperty("name", insightsReplicaConfig.getReplicaName());
					replicaJson.addProperty("fileName",
							insightsReplicaConfig.getReplicaName() + "_" + SINK_STREAMS_CONFIG_FILE);
				} else {
					throw new InsightsCustomException(
							insightsReplicaConfig.getReplicaName() + " Sink Streams Config file doesn't exist");
				}
				replicaJsonArr.add(replicaJson);
			}
			payloadJson.add("source", sourceJson);
			payloadJson.add("replicas", replicaJsonArr);
			performAgentAction("RESYNC", REPLICA_PACKAGE_QUEUE, payloadJson);
		} catch (Exception e) {
			log.error("Error while publishing message to MQ {}", e.getMessage());
			throw new InsightsCustomException("Failed to publish message to MQ");
		}
	}

	@Override
	public JsonArray getAllReplicas() throws InsightsCustomException {
		JsonArray replicaJsonArr = new JsonArray();
		try {
			InsightsStreamsSourceConfig sourceStreamsConfig = streamsConfigDAL.getStreamsSourceConfig(STREAMSCONF);
			if(sourceStreamsConfig == null) {
				return replicaJsonArr;
			}
			int sourceNodeCount = getTotalNodes(ApplicationConfigProvider.getInstance().getGraph().getEndpoint(),
					sourceStreamsConfig.getNodeLabels());
			int sourceRelCount = getTotalRelations(ApplicationConfigProvider.getInstance().getGraph().getEndpoint(),
					sourceStreamsConfig.getRelationshipLabels());

			List<InsightsReplicaConfig> allReplicaConfig = replicaConfigDAL.getAllReplicaConfig();
			for (InsightsReplicaConfig insightsReplicaConfig : allReplicaConfig) {
				JsonObject replicaJson = new JsonObject();
				int replicaNodeCount = getTotalNodes(insightsReplicaConfig.getReplicaEndpoint(),
						sourceStreamsConfig.getNodeLabels());
				int replicaRelCount = getTotalRelations(insightsReplicaConfig.getReplicaEndpoint(),
						sourceStreamsConfig.getRelationshipLabels());
				replicaJson.addProperty("name", insightsReplicaConfig.getReplicaName());
				replicaJson.addProperty("endpoint", insightsReplicaConfig.getReplicaEndpoint());
				replicaJson.addProperty("nodeCount", replicaNodeCount);
				replicaJson.addProperty("relationshipCount", replicaRelCount);
				replicaJson.addProperty("nodeBehind", sourceNodeCount - replicaNodeCount);
				replicaJson.addProperty("relationshipBehind", sourceRelCount - replicaRelCount);
				replicaJsonArr.add(replicaJson);
			}
		} catch (Exception e) {
			log.error("Error while fetching replicas from DB {}", e.getMessage());
			throw new InsightsCustomException("Failed to fetch replicas from DB");
		}
		return replicaJsonArr;
	}

	@Override
	public String deleteReplica(String replicaName) throws InsightsCustomException {
		JsonObject payloadJson = new JsonObject();
		try {
			InsightsReplicaConfig replica = replicaConfigDAL.getReplicaByName(replicaName);
			payloadJson.addProperty("ip", replica.getReplicaIP());
			payloadJson.addProperty("name", replica.getReplicaName());
			performAgentAction("DELETE", REPLICA_PACKAGE_QUEUE, payloadJson);
			replicaConfigDAL.delete(replica);
		} catch (Exception e) {
			log.error("Error while deleting replica {}", e.getMessage());
			throw new InsightsCustomException("Failed to delete replica");
		}
		return "Successfully deleted " + replicaName;
	}

	@Override
	public String resyncAll() throws InsightsCustomException {
		try {
			publishMessageForAgent();
		} catch (Exception e) {
			log.error("Error while resyncing replicas {}", e.getMessage());
			throw new InsightsCustomException("Failed to resync replicas");
		}
		return "Resyncing replicas initiated";
	}
	
	private static String getMasterNeo4jIP() throws InsightsCustomException{
		String ipAddress = "-1";
		try {
			String endpoint = ApplicationConfigProvider.getInstance().getGraph().getEndpoint();
			String pattern = "\\d{1,3}(\\.\\d{1,3}){3}";
			Pattern compiledPattern = Pattern.compile(pattern);
			Matcher matcher = compiledPattern.matcher(endpoint);
			if (matcher.find()) {
				ipAddress = matcher.group();
			}
		} catch (Exception e) {
			throw new InsightsCustomException("Failed to extract master IP address");
		}
		return ipAddress;
	}

	@Override
	public JsonObject getNeo4jScalingLogDetails() throws InsightsCustomException {
		JsonObject payloadJson = new JsonObject();
		File folder = new File(System.getenv().get(INSIGHTS_HOME) + File.separator + REPLICA_DAEMON);
		try {
			if (folder.listFiles() != null) {
				for (File file : folder.listFiles()) {
					if (file.isFile() && file.getName().endsWith(".log")) {
						JsonArray logDetails = new JsonArray();
						try (FileReader readFile = new FileReader(file);
								BufferedReader reader = new BufferedReader(readFile);) {
							String line;
							while ((line = reader.readLine()) != null) {
								JsonObject logObj = new JsonObject();
								logObj.addProperty("executionTime", line.split("=")[0]);
								logObj.addProperty("status", line.split("=")[1].split(":")[0]);
								logObj.addProperty("message", line.split("=")[1].split(":")[1]);
								logDetails.add(logObj);
							}
						}
						payloadJson.add(file.getName(), logDetails);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error while fetching neo4j scaling log details", e.getMessage());
			throw new InsightsCustomException("Failed to fetch neo4j scaling log details= " + e.getMessage());
		}
		return payloadJson;
	}
	
}
