/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformengine.test.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSSession;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.AWSSQSProvider;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQProvider;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDAL;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDefinition;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;

public class EngineTestData {
	private static Logger log = LogManager.getLogger(EngineTestData.class);
	public static String gitConfig = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"SCM.GIT.config\",\"agentCtrlQueue\":\"GITTEST8800\"},\"publish\":{\"data\":\"SCM.GIT_UNTEST.DATA\",\"health\":\"SCM.GIT_UNTEST.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"sha\":\"commitId\",\"commit\":{\"message\":\"message\",\"author\":{\"name\":\"authorName\",\"date\":\"commitTime\"}}}},\"agentId\":\"GITTEST8800\",\"enableBranches\":false,\"enableBrancheDeletion\":false,\"enableDataValidation\":true,\"toolCategory\":\"SCM\",\"toolsTimeZone\":\"GMT\",\"insightsTimeZone\":\"Asia/Kolkata\",\"enableValueArray\":false,\"useResponseTemplate\":true,\"auth\":\"base64\",\"runSchedule\":30,\"timeStampField\":\"commitTime\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"startFrom\":\"2019-03-01 15:46:33\",\"accessToken\":\"56\",\"getRepos\":\"https://api.github.com/users/SanketCTSI/repos\",\"commitsBaseEndPoint\":\"https://api.github.com/repos/InsightsCTSI/\",\"isDebugAllowed\":false,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v6.0\",\"toolName\":\"GIT\"}";
	public static String jenkinsConfig = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"CI.JENKINS.config\",\"agentCtrlQueue\":\"JENKINSTEST8800\"},\"publish\":{\"data\":\"CI.JENKINS_UNTEST.DATA\",\"health\":\"CI.JENKINS_UNTEST.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":true,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"actions\":[{\"causes\":[{\"shortDescription\":\"shortDescription\"}]},{\"remoteUrls\":[\"scmUrl\"]},{\"url\":\"sonarUrl\"}],\"changeSet\":{\"items\":[{\"commitId\":\"scmCommitId\",\"author\":{\"fullName\":\"scmAuthor\"},\"date\":\"buildDate\"}],\"kind\":\"scmKind\"},\"duration\":\"duration\",\"id\":\"buildNumber\",\"number\":\"number\",\"result\":\"result\",\"timestamp\":\"buildTimestamp\",\"url\":\"buildUrl\"}},\"jobDetails\":{\"rundeckJobId\":\"maven2-moduleset/publishers/org.jenkinsci.plugins.rundeck.RundeckNotifier/jobId\",\"scmRemoteUrl\":\"maven2-moduleset/scm/userRemoteConfigs/hudson.plugins.git.UserRemoteConfig/url\",\"nexusRepoUrl\":\"maven2-moduleset/publishers/hudson.maven.RedeployPublisher/url\",\"groupId\":\"maven2-moduleset/rootModule/groupId\",\"artifactId\":\"maven2-moduleset/rootModule/artifactId\"},\"agentId\":\"JENKINSTEST8800\",\"toolCategory\":\"CI\",\"toolsTimeZone\":\"Asia/Kolkata\",\"enableDataValidation\":true,\"useResponseTemplate\":true,\"useAllBuildsApi\":true,\"isDebugAllowed\":false,\"enableValueArray\":false,\"userid\":\"username\",\"passwd\":\"123\",\"runSchedule\":30,\"baseUrl\":\"http://127.0.0.1:8080/\",\"jenkinsMasters\":{\"master1\":\"http://127.0.0.1:8080/\",\"master2\":\"http://127.0.0.1:8080/\"},\"timeStampField\":\"buildTimestamp\",\"timeStampFormat\":\"epoch\",\"isEpochTimeFormat\":true,\"startFrom\":\"2019-03-01 15:46:33\",\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v6.0\",\"toolName\":\"JENKINS\"}";
	public static String rabbitMQGITTestPlayload = "{\"data\": [{\"inSightsTime\": 1539497182, \"execId\": \"5f84de70-ee48-11e9-b674-020c9a7827b8\", \"categoryName\": \"SCM\", \"jiraKey\": \"LS-7345942999\", \"toolName\": \"GIT\", \"commitId\": \"CM-7569369619\", \"inSightsTimeX\": \"2018-10-14T06:06:22Z\", \"gitCommitId\": \"2YW2jPX3ve45xWaTjjPxUC2lS9D7d0bK\", \"gitAuthorName\": \"Mayank\", \"gitReponame\": \"InsightsTest\", \"gitCommiTime\": \"2018-10-14T06:06:22Z\", \"message\": \"This commit is associated with jira-key : LS-7345942999\", \"repoName\": \"InsightsTest\"}], \"metadata\": {\"labels\": [\"GIT_UNTEST\"]}}";
	public static String rabbitMQJENKINSTestPayload = "{\"data\": [{\"inSightsTime\": 1539497302, \"status\": \"Success\", \"execId\": \"5f84de70-ee48-11e9-b674-020c9a7827b8\", \"scmcommitId\": \"CM-7569369619\", \"categoryName\": \"CI\", \"environment\": \"RELEASE\", \"toolName\": \"JENKINS\", \"projectName\": \"PaymentServices\", \"jobName\": \"BillingApproved\", \"buildNumber\": \"9046453184\", \"inSightsTimeX\": \"2018-10-14T06:08:22Z\", \"buildUrl\": \"productv4.3.devops.com\", \"master\": \"master2\", \"result\": \"ABORTED\", \"startTime\": \"2018-10-14T06:08:22Z\", \"projectID\": \"1001\", \"duration\": 129, \"endTime\": \"2018-10-14T06:10:31Z\", \"jenkins_date\": \"2018-10-14 06:08:22\"}], \"metadata\": {\"labels\": [\"JENKINS_UNTEST\"]}}";
	public static String gitConsumerTag = "SCM_GIT_UNTEST_DATA";
	public static String gitQueueName = "SCM_GIT_UNTEST_DATA";
	public static String gitRoutingKey = "SCM.GIT_UNTEST.DATA";
	public static String jenkinConsumerTag = "CI_JENKINS_UNTEST_DATA";
	public static String jenkinQueueName = "CI_JENKINS_UNTEST_DATA";
	public static String jenkinsRoutingKey = "CI.JENKINS_UNTEST.DATA";
	public static Boolean dataUpdateSupported = false;
	public static String osversion = "windows";
	public static Date updateDate = Timestamp.valueOf(LocalDateTime.now());
	public static String agentVersion = "v6.0";
	public static String gitToolCategory = "SCM";
	public static String jenkinToolCategory = "CI";
	public static String gitLabelName = "GIT_UNTEST";
	public static String jenkinLabelName = "JENKINS_UNTEST";
	public static String saveDataConfig = "{\"destination\":{\"toolName\":\"JENKINS\",\"toolCategory\":\"CI\",\"labelName\":\"JENKINS_UNTEST\",\"fields\":[\"scmcommitId\"]},\"source\":{\"toolName\":\"GIT\",\"toolCategory\":\"SCM\",\"labelName\":\"GIT_UNTEST\",\"fields\":[\"commitId\"]},\"relationName\":\"TEST_FROM_GIT_TO_JENKINS\",\"relationship_properties\":[],\"isSelfRelation\":false}";
	public static String saveEnrichmentData = "[{\"queryName\":\"Add test to GIT_UNTEST\",\"cypherQuery\":\"match(n:GIT_UNTEST) where not exists (n.test) set n.test=\\\"completed\\\" return count(n)\",\"runSchedule\":10,\"lastExecutionTime\":\"2020/12/16 11:48 AM\",\"recordsProcessed\":0,\"queryProcessingTime\":1598}]";
	public static String saveCorrelationConfig = "[{\"destination\":{\"toolName\":\"GIT\",\"toolCategory\":\"SCM\",\"labelName\":\"GIT_UNTEST\",\"fields\":[\"key\"]},\"source\":{\"toolName\":\"JIRA\",\"toolCategory\":\"ALM\",\"labelName\":\"JIRA_UNTEST\",\"fields\":[\"almkey\"]},\"relationName\":\"TEST_FROM_GIT_TO_JIRA\",\"relationship_properties\":[],\"isSelfRelation\":false}]";
	static InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	static InsightsSchedulerTaskDAL schedularTaskDAL = new InsightsSchedulerTaskDAL();

	public static JsonObject getJsonObject(String jsonString) {
		Gson gson = new Gson();
		JsonElement jelement = gson.fromJson(jsonString.trim(), JsonElement.class);
		return jelement.getAsJsonObject();
	}

	public static void publishMessage(String routingKey, String playload)
			throws IOException, TimeoutException {
		try {
			if (ApplicationConfigProvider.getInstance().getMessageQueue().getProviderName().equalsIgnoreCase("AWSSQS"))
				publishSQSMessage(routingKey, playload);
			else
				publishRMQMessage(routingKey, playload);
		} catch (InsightsCustomException | JMSException e) {
			log.error(e.getMessage());
		}
	}

	public static void publishRMQMessage(String routingKey, String publishDataJson)
			throws InsightsCustomException, IOException, TimeoutException {
		String queueName = routingKey.replace(".", "_");
		Channel channel = null;
		try {
			channel = RabbitMQProvider.getConnection().createChannel();
			channel = RabbitMQProvider.initilizeChannel(channel, routingKey, queueName,
					MQMessageConstants.EXCHANGE_NAME, MQMessageConstants.EXCHANGE_TYPE);
			channel.basicPublish(MQMessageConstants.EXCHANGE_NAME, routingKey, null, publishDataJson.getBytes());
		} catch (IOException e) {
			log.debug("Message not published in queue", e);
		}
	}
	
	public static void publishSQSMessage(String routingKey, String data) throws InsightsCustomException, JMSException {

		SQSConnection connection = AWSSQSProvider.getSQSConnectionFromFactory();
		AmazonSQSMessagingClientWrapper client = AWSSQSProvider.getSQSClient(connection);
		Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
		String queueName = routingKey.replace(".", "_") + MQMessageConstants.FIFO_EXTENSION;
		if (!client.queueExists(queueName)) {
			AWSSQSProvider.createSQSQueue(queueName, client);
		}
		Queue queue = session.createQueue(queueName);
		MessageProducer producer = session.createProducer(queue);
		TextMessage message = session.createTextMessage(data);
		message.setStringProperty("JMSXGroupID", routingKey);
		producer.send(message);
	}
	
	public static boolean isQueueExists(String routingKey) {
		try {
			SQSConnection connection = AWSSQSProvider.getSQSConnectionFromFactory();
			AmazonSQSMessagingClientWrapper client = AWSSQSProvider.getSQSClient(connection);
			String queueName = routingKey.replace(".", "_") + MQMessageConstants.FIFO_EXTENSION;
			boolean result = client.queueExists(queueName);
			return result;
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	public static Map readNeo4JData(String nodeName, String compareFlag) {
		Map map = null;
		GraphDBHandler dbHandler = new GraphDBHandler();
		String query = "MATCH (n:" + nodeName + ") where n." + compareFlag + "='CM-7569369619' return n";
		log.debug(" query  {} ", query);
		GraphResponse neo4jResponse;
		// JsonArray parentArray = new JsonArray();
		try {

			neo4jResponse = dbHandler.executeCypherQuery(query);

			JsonElement tooldataObject = neo4jResponse.getJson().get("results").getAsJsonArray().get(0)
					.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("row");

			String finalJson = tooldataObject.toString().replace("[", "").replace("]", "");

			Gson gson = new Gson();

			map = gson.fromJson(finalJson, Map.class);
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return map;

	}

	public static CorrelationConfiguration loadCorrelation(String config) {
		JsonObject json = JsonUtils.parseStringAsJsonObject(config);
		CorrelationConfiguration correlationConfig = new CorrelationConfiguration();
		CorrelationJson correlation = new Gson().fromJson(json, CorrelationJson.class);

		correlationConfig.setSourceToolName(correlation.getSource().getToolName());
		correlationConfig.setSourceToolCategory(correlation.getSource().getToolCategory());
		if (null == correlation.getSource().getLabelName()) {
			correlationConfig.setSourceLabelName(correlation.getSource().getToolName());
		} else {
			correlationConfig.setSourceLabelName(correlation.getSource().getLabelName());
		}
		correlationConfig.setSourceFields(String.join(",", correlation.getSource().getFields()));
		correlationConfig.setDestinationToolName(correlation.getDestination().getToolName());
		correlationConfig.setDestinationToolCategory(correlation.getDestination().getToolCategory());
		correlationConfig.setDestinationLabelName(correlation.getDestination().getLabelName());
		if (null == correlation.getDestination().getLabelName()) {
			correlationConfig.setDestinationLabelName(correlation.getDestination().getToolName());
		} else {
			correlationConfig.setDestinationLabelName(correlation.getDestination().getLabelName());
		}
		correlationConfig.setDestinationFields(String.join(",", correlation.getDestination().getFields()));
		correlationConfig.setRelationName(correlation.getRelationName());
		if (correlation.getPropertyList().length > 0) {
			correlationConfig.setPropertyList(String.join(",", correlation.getPropertyList()));
		}
		correlationConfig.setEnableCorrelation(correlation.isEnableCorrelation());
		correlationConfig.setSelfRelation(correlation.isSelfRelation());

		return correlationConfig;
	}

	public static InsightsConfigFiles createDataEnrichmentData() {
		InsightsConfigFiles configFile = new InsightsConfigFiles();
		configFile.setFileName("DataEnrichmentTest");
		configFile.setFileType("JSON");
		configFile.setFileModule("DATAENRICHMENT");
		configFile.setFileData(saveEnrichmentData.getBytes());
		return configFile;
	}

	public static InsightsConfigFiles createCorrelationData() {
		InsightsConfigFiles configFile = new InsightsConfigFiles();
		configFile.setFileName("CorrelationTest");
		configFile.setFileType("JSON");
		configFile.setFileModule("CORRELATION");
		configFile.setFileData(saveCorrelationConfig.getBytes());
		return configFile;
	}

	public static InsightsConfigFiles createDataEnrichmentDataFile(String filePath) {
		InsightsConfigFiles configFile = new InsightsConfigFiles();
		int index = filePath.lastIndexOf("\\");
		String fileName = filePath.substring(index + 1, filePath.lastIndexOf("."));
		configFile.setFileName(fileName);
		configFile.setFileType("JSON");
		configFile.setFileModule("DATAENRICHMENT");
		try {
			configFile.setFileData(Files.readAllBytes(new File(filePath).toPath()));
			configFilesDAL.saveConfigurationFile(configFile);
		} catch (IOException e) {
			log.error(e);
		}

		return configFile;
	}

	public InsightsTools prepareInsightsToolData(int id, String toolName, String category,
			String agentCommunicationQueue, String toolConfigJson, Boolean isActive) {
		InsightsTools insightsTools = new InsightsTools();
		insightsTools.setId(id);
		insightsTools.setToolName(toolName);
		insightsTools.setCategory(category);
		insightsTools.setAgentCommunicationQueue(agentCommunicationQueue);
		insightsTools.setToolConfigJson(toolConfigJson);
		insightsTools.setIsActive(isActive);
		return insightsTools;
	}

	public static void SaveSchedulatTaskDefination(JsonObject schedulatTaskDefination) {
		InsightsSchedulerTaskDefinition insightsSchedulatTaskDefination = new InsightsSchedulerTaskDefinition();
		insightsSchedulatTaskDefination.setTimerTaskId(schedulatTaskDefination.get("timerTaskId").getAsInt());
		insightsSchedulatTaskDefination.setAction(schedulatTaskDefination.get("action").getAsString());
		insightsSchedulatTaskDefination
				.setComponentClassDetail(schedulatTaskDefination.get("componentClassDetail").getAsString());
		insightsSchedulatTaskDefination.setComponentName(schedulatTaskDefination.get("componentName").getAsString());
		insightsSchedulatTaskDefination.setSchedule(schedulatTaskDefination.get("schedule").getAsString());
		schedularTaskDAL.save(insightsSchedulatTaskDefination);
	}

	public static void saveToolsMappingLabel(String agentMappingJson) {
		List<JsonObject> nodeProperties = new ArrayList<>();
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(agentMappingJson);
			GraphDBHandler dbHandler = new GraphDBHandler();
			String constraintQuery = "CREATE CONSTRAINT ON (n:METADATA) ASSERT n.metadata_id  IS UNIQUE";
			if (ApplicationConfigProvider.getInstance().getGraph().getVersion().contains("4.")) {
				constraintQuery = "CREATE CONSTRAINT IF NOT EXISTS FOR (n:METADATA) REQUIRE n.metadata_id IS UNIQUE";
			}
			dbHandler.executeCypherQuery(constraintQuery);
			String query = "UNWIND $props AS properties " + "CREATE (n:METADATA:BUSINESSMAPPING) "
					+ "SET n = properties"; // DATATAGGING
			JsonObject json = JsonUtils.parseStringAsJsonObject(validatedResponse);
			log.debug("arg0 {} ", json);
			nodeProperties.add(json);
			JsonObject graphResponse = dbHandler.bulkCreateNodes(nodeProperties, query);
			if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
				log.error(graphResponse);
			}
		} catch (InsightsCustomException e) {
			log.error(e);
		}
	}
}
