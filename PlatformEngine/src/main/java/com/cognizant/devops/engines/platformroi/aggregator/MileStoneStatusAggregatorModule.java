/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformroi.aggregator;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.platformroi.subscriber.MilestoneStatusCommunicationSubscriber;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;

public class MileStoneStatusAggregatorModule implements Job, ApplicationConfigInterface {
	private static Logger log = LogManager.getLogger(MileStoneStatusAggregatorModule.class);
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<>();

	private Map<String, String> loggingInfo = new HashMap<>();
	String jobName="";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug("MileStone Status Aggregator Module Module start");
		long startTime = System.currentTimeMillis();
		jobName=context.getJobDetail().getKey().getName();
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("MileStone Status Aggregator Module Started ",
				PlatformServiceConstants.SUCCESS,jobName);
		try {
			registerMilestoneStoneStatusSubscriber(MQMessageConstants.MILESTONE_STATUS_QUEUE);
		} catch (Exception e) {
			log.error("Unable to add subscriber ", e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Error occured while executing Milestone Status Aggregator " + e.getMessage(),
					PlatformServiceConstants.FAILURE,jobName);
		}
		log.debug("Milestone Aggregator Aggregator Module completed");
		long processingTime = System.currentTimeMillis() - startTime  ;
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Milestone Status Aggregator execution Completed",
				PlatformServiceConstants.SUCCESS,jobName,processingTime);
	}
	
	private void registerMilestoneStoneStatusSubscriber(String statusRoutingKey) {

		if (statusRoutingKey != null && !registry.containsKey(statusRoutingKey)) {
			try {
				registry.put(statusRoutingKey, new MilestoneStatusCommunicationSubscriber(statusRoutingKey));
				log.debug(
						" Type=MilestoneStatusCommunication toolName={} category={} agentId={} routingKey={} execId={} Data archival health queue {} subscribed successfully ",
						loggingInfo.get("toolName"), loggingInfo.get("category"), loggingInfo.get("agentId"),
						statusRoutingKey, "-", statusRoutingKey);
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Milestone Status Communication " + statusRoutingKey + " subscribed successfully ",
						PlatformServiceConstants.SUCCESS,jobName);
			} catch (Exception e) {
				log.error(
						" toolName={} category={} agentId={} routingKey={} Unable to add subscriber for routing key:{}",
						loggingInfo.get("toolName"), loggingInfo.get("category"), loggingInfo.get("agentId"),
						statusRoutingKey, statusRoutingKey, e);
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Error occured while executing aggregator for Milestone Status Communication  "
								+ e.getMessage(),
						PlatformServiceConstants.FAILURE,jobName);
			}
		}
	}
}
