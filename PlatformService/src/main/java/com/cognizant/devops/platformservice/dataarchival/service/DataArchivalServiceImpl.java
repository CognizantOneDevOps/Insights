/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.dataarchival.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.MessageQueueDataModel;
import com.cognizant.devops.platformcommons.constants.DataArchivalConstants;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.dataArchivalConfig.DataArchivalConfigDal;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Service("dataArchivalService")
public class DataArchivalServiceImpl implements DataArchivalService {

	static Logger log = LogManager.getLogger(DataArchivalServiceImpl.class);
	DataArchivalConfigDal dataArchivalConfigdal = new DataArchivalConfigDal();
	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();

	/**
	 *
	 * Method to save Data Archival record
	 * 
	 * @param archivalDetailsJson
	 * @return String
	 * @throws InsightsCustomException
	 */
	@Override
	public String saveDataArchivalDetails(JsonObject archivalDetailsJson) throws InsightsCustomException {
		try {
			String archivalName = archivalDetailsJson.get("archivalName").getAsString();
			String startDate = archivalDetailsJson.get("startDate").getAsString();
			long epochStartDate = InsightsUtils.getEpochTime(startDate) / 1000;
			String endDate = archivalDetailsJson.get("endDate").getAsString();
			long epochEndDate = InsightsUtils.getEpochTime(endDate) / 1000;
			String author = archivalDetailsJson.get("author").getAsString();
			int daysToRetain = archivalDetailsJson.get("daysToRetain").getAsInt();
			Long createdOn = InsightsUtils.getTodayTime() / 1000;
			Long expiryDate = getExpiryDate(createdOn, daysToRetain);
			String status = DataArchivalStatus.INPROGRESS.name();
			if (epochStartDate > epochEndDate) {
				throw new InsightsCustomException("Start Date cannot be greater then End Date");
			} else if (ValidationUtils.checkAgentIdString(archivalName)) {
				throw new InsightsCustomException(
						"Archival name has to be Alpha numeric with '_' as special character");
			} else if (archivalName.isEmpty()) {
				throw new InsightsCustomException("Please enter valid archival name, it cannot be blank");
			}

			List<AgentConfig> agentDetails = agentConfigDAL.getAgentConfigurations(DataArchivalConstants.TOOLNAME,
					DataArchivalConstants.TOOLCATEGORY);
			if (!agentDetails.isEmpty()) {
				JsonObject agentJson = new JsonParser().parse(agentDetails.get(0).getAgentJson()).getAsJsonObject();
				String routingKey = agentJson.get("subscribe").getAsJsonObject().get("dataArchivalQueue").getAsString();
				InsightsDataArchivalConfig dataArchivalRecord = dataArchivalConfigdal
						.getSpecificArchivalRecord(archivalName);
				if (dataArchivalRecord == null) {
					JsonObject publishDataJson = new JsonObject();
					publishDataJson.addProperty("archivalName", archivalName);
					publishDataJson.addProperty("startDate", startDate);
					publishDataJson.addProperty("endDate", endDate);
					publishDataJson.addProperty("daysToRetain", daysToRetain);
					publishDataArchivalDetails(routingKey, publishDataJson.toString());

					InsightsDataArchivalConfig dataArchivalConfig = new InsightsDataArchivalConfig();
					dataArchivalConfig.setArchivalName(archivalName);
					dataArchivalConfig.setStartDate(epochStartDate);
					dataArchivalConfig.setEndDate(epochEndDate);
					dataArchivalConfig.setAuthor(author);
					dataArchivalConfig.setStatus(status);
					dataArchivalConfig.setExpiryDate(expiryDate);
					dataArchivalConfig.setDaysToRetain(daysToRetain);
					dataArchivalConfig.setCreatedOn(createdOn);
					dataArchivalConfigdal.saveDataArchivalConfiguration(dataArchivalConfig);
				} else {
					throw new InsightsCustomException("Archival Name already exists.");
				}
			} else {
				throw new InsightsCustomException("Data Archival agent not present.");
			}

		} catch (InsightsCustomException e) {
			log.error("Error while saving archival details .. {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		} catch (Exception e) {
			log.error("Error occured .. {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return "SUCCESS";
	}

	/**
	 *
	 * Method to get all Data Archival records
	 * 
	 * @return List<InsightsDataArchivalConfig>
	 * @throws InsightsCustomException
	 */
	@Override
	public List<InsightsDataArchivalConfig> getAllArchivalRecord() throws InsightsCustomException {
		List<InsightsDataArchivalConfig> archivedRecordList = new ArrayList<>();
		try {
			archivedRecordList = dataArchivalConfigdal.getAllArchivalRecord();
		} catch (Exception e) {
			log.error("Error getting all archival records {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return archivedRecordList;
	}

	/**
	 *
	 * Method to get all Active Data Archival records
	 * 
	 * @return List<InsightsDataArchivalConfig>
	 * @throws InsightsCustomException
	 */
	@Override
	public List<InsightsDataArchivalConfig> getActiveArchivalList() throws InsightsCustomException {
		List<InsightsDataArchivalConfig> activeArchivedList = new ArrayList<>();
		try {
			activeArchivedList = dataArchivalConfigdal.getActiveList();
		} catch (Exception e) {
			log.error("Error getting active archival records {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return activeArchivedList;
	}

	/**
	 *
	 * Method to inactivate Data Archival record
	 * 
	 * @param archivalName
	 * @return Boolean
	 * @throws InsightsCustomException
	 */
	@Override
	public Boolean inactivateArchivalRecord(String archivalName) throws InsightsCustomException {
		try {
			dataArchivalConfigdal.updateArchivalStatus(archivalName, DataArchivalStatus.INACTIVE.name());
		} catch (Exception e) {
			log.error("Error while updating archival status  {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return true;
	}

	/**
	 *
	 * Method to activate Data Archival record
	 * 
	 * @param archivalName
	 * @return Boolean
	 * @throws InsightsCustomException
	 */
	@Override
	public Boolean activateArchivalRecord(String archivalName) throws InsightsCustomException {
		try {
			dataArchivalConfigdal.updateArchivalStatus(archivalName, DataArchivalStatus.ACTIVE.name());
		} catch (Exception e) {
			log.error("Error while updating archival status  {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return true;
	}

	/**
	 *
	 * Method to delete Data Archival record
	 * 
	 * @param archivalName
	 * @return Boolean
	 * @throws InsightsCustomException
	 */
	@Override
	public Boolean deleteArchivalRecord(String archivalName) throws InsightsCustomException {
		try {
			String status = dataArchivalConfigdal.getSpecificArchivalRecord(archivalName).getStatus();
			if (status.equalsIgnoreCase(DataArchivalStatus.INACTIVE.name())
					|| status.equalsIgnoreCase(DataArchivalStatus.INPROGRESS.name())) {
				dataArchivalConfigdal.deleteArchivalRecord(archivalName);
			} else {
				throw new InsightsCustomException(
						"Please change status of archival record, only inactive and inprogress records can be deleted.");
			}
		} catch (Exception e) {
			log.error("Error while deleting archived data {} ", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}

		return true;
	}

	/**
	 *
	 * Method to update Data Archival Source URL
	 * 
	 * @param archivalURLDetailsJson
	 * @return Boolean
	 * @throws InsightsCustomException
	 */
	@Override
	public Boolean updateArchivalSourceUrl(JsonObject archivalURLDetailsJson) throws InsightsCustomException {
		try {
			String archivalName = archivalURLDetailsJson.get("archivalName").getAsString();
			String sourceURL = archivalURLDetailsJson.get("sourceUrl").getAsString();
			dataArchivalConfigdal.updateArchivalSourceUrl(archivalName, sourceURL);
		} catch (Exception e) {
			log.error("Error while updating archival source url  {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return true;
	}

	/**
	 * Method to calculate Expiry Date
	 * 
	 * @param createdOn
	 * @param daysToRetain
	 * @return
	 */
	private Long getExpiryDate(Long createdOn, int daysToRetain) {
		Long days = (long) (daysToRetain * 24 * 60 * 60);
		return (createdOn + days);

	}

	/**
	 * Method to publish message to RabbitMq
	 * 
	 * @param routingKey
	 * @param publishDataJson
	 * @throws IOException
	 * @throws TimeoutException
	 */
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

}
