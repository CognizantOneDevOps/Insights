/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformcommons.constants.DataArchivalConstants;
import com.cognizant.devops.platformcommons.constants.EngineConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.dataArchivalConfig.DataArchivalConfigDal;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DataArchivalDataSubscriber extends EngineSubscriberResponseHandler {
	
	private static Logger log = LogManager.getLogger(DataArchivalDataSubscriber.class);
	DataArchivalConfigDal dataArchivalConfigDAL = new DataArchivalConfigDal();
	private Map<String,String> loggingInfo = new ConcurrentHashMap<>();
	private String agentId;
	public DataArchivalDataSubscriber(String routingKey,String agentId) throws Exception {
		super(routingKey);
		this.agentId=agentId;
	}

	@Override
	public void handleDelivery(String routingKey, String message) throws InsightsCustomException {
		try {
			JsonArray messageJson = JsonUtils.parseStringAsJsonObject(message).get("data").getAsJsonArray();
			JsonObject updateURLJson = messageJson.get(0).getAsJsonObject();

			loggingInfo.put(AgentCommonConstant.TOOLNAME,String.valueOf(updateURLJson.get(AgentCommonConstant.TOOLNAME)));
			loggingInfo.put(AgentCommonConstant.CATEGORY,String.valueOf(updateURLJson.get("categoryName")));
			loggingInfo.put(AgentCommonConstant.AGENTID, agentId);
			loggingInfo.put(EngineConstants.EXECID,String.valueOf(updateURLJson.get(EngineConstants.EXECID)));

			if (PlatformServiceConstants.SUCCESS.equalsIgnoreCase(updateURLJson.get("status").getAsString())) {
				String containerID = updateURLJson.get(DataArchivalConstants.CONTAINERID).getAsString();
				if (containerID.isEmpty()) {
					throw new InsightsCustomException("ContainerID not present in message");
				}
				if(updateURLJson.has(DataArchivalConstants.TASK) && "remove_container".equalsIgnoreCase(updateURLJson.get(DataArchivalConstants.TASK).getAsString())) {
					InsightsDataArchivalConfig archivalConfigs = dataArchivalConfigDAL.getArchivalRecordByContainerId(containerID);
					dataArchivalConfigDAL.updateArchivalStatus(archivalConfigs.getArchivalName(), DataArchivalStatus.TERMINATED.toString());
				} else {
					String archivalName = updateURLJson.get(DataArchivalConstants.ARCHIVALNAME).getAsString();
					String sourceUrl = updateURLJson.get(DataArchivalConstants.SOURCEURL).getAsString();
					int boltPort = updateURLJson.get(DataArchivalConstants.BOLTPORT).getAsInt();
					if (archivalName.isEmpty()) {
						throw new InsightsCustomException("Archival name not present in message");
					} else if (sourceUrl.isEmpty()) {
						throw new InsightsCustomException("Container URL not present in message");
					}
					Long expiryDate = getExpiryDate(archivalName);
					log.debug(" Type=DataArchival toolName={} category={} agentId={} routingKey={} execId={} Inside Data archival data:- archivalName: {} , sourceUrl: {} ",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),"-",loggingInfo.get(EngineConstants.EXECID), archivalName, sourceUrl);
					dataArchivalConfigDAL.updateContainerDetails(archivalName, sourceUrl, containerID, expiryDate,boltPort);
					log.debug(" Type=DataArchival toolName={} category={} agentId={} routingKey={} execId={} Update Data Archival record with sourceurl and boltport",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),"-",loggingInfo.get(EngineConstants.EXECID));
				}
			} else {
				log.error(" toolName={} category={} agentId={} routingKey={} execId={} Failed status in Data archival message received from MQ",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),"-",loggingInfo.get(EngineConstants.EXECID));
				throw new InsightsCustomException("Failed status in Data archival message received from MQ.");
			}
		} catch (NoResultException e) {
			log.error("toolName={} category={} agentId={} execId={} No Record found occured. data message :{} ",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),loggingInfo.get(EngineConstants.EXECID) ,message, e);
			throw new NoResultException(e.getMessage());
		} catch (Exception e) {
			log.error("toolName={} category={} agentId={} execId={} Exception occured ",loggingInfo.get(AgentCommonConstant.TOOLNAME),loggingInfo.get(AgentCommonConstant.CATEGORY),loggingInfo.get(AgentCommonConstant.AGENTID),loggingInfo.get(EngineConstants.EXECID),e);
			throw new InsightsCustomException(e.getMessage());
		}

	}

	private Long getExpiryDate(String archivalName) {
		InsightsDataArchivalConfig config = dataArchivalConfigDAL.getSpecificArchivalRecord(archivalName);
		Long daysInMillis = TimeUnit.DAYS.toMillis(config.getDaysToRetain());
		return ((InsightsUtils.getTodayTime() + daysInMillis)/1000);
	}

}
