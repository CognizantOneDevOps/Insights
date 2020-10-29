/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformreports.assessment.core;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.util.SystemStatus;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformdal.dal.PostgresMetadataHandler;
import com.cognizant.devops.platformdal.healthutil.HealthUtil;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowtask.utils.MQMessageConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SystemNotificationDetailSubscriber extends WorkflowTaskSubscriberHandler {
	private static Logger log = LogManager.getLogger(SystemNotificationDetailSubscriber.class.getName());
	HealthUtil healthUtil = new HealthUtil();
	
	public SystemNotificationDetailSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		// TODO Auto-generated method stub
		String incomingTaskMessage = new String(body, MQMessageConstants.MESSAGE_ENCODING);
		JsonObject incomingTaskMessageJson = new JsonParser().parse(incomingTaskMessage).getAsJsonObject();
		getDataComponentStatus();
		
	}
	
	private JsonObject getDataComponentStatus() {
		JsonObject dataComponentStatus = new JsonObject();
		String username = null;
		String password = null;
		String authToken = null;
		String hostEndPoint = "";
		String apiUrl = "";
		hostEndPoint = ServiceStatusConstants.POSTGRESQL_HOST;
		apiUrl = hostEndPoint;
		JsonObject postgreStatus = healthUtil.getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
				ServiceStatusConstants.PgSQL, Boolean.FALSE, username, password, authToken);
		dataComponentStatus.add(ServiceStatusConstants.PgSQL, postgreStatus);
		log.debug("After Postgres================");
		/* Neo4j health check */
		hostEndPoint = ServiceStatusConstants.NEO4J_HOST;
		apiUrl = hostEndPoint + "/db/data/";
		authToken = ApplicationConfigProvider.getInstance().getGraph().getAuthToken();
		JsonObject neo4jStatus = healthUtil.getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
				ServiceStatusConstants.Neo4j, Boolean.TRUE, username, password, authToken);
		dataComponentStatus.add(ServiceStatusConstants.Neo4j, neo4jStatus);
		log.debug("After Neo4j================");
		/* Elastic Search health check */
		hostEndPoint = ServiceStatusConstants.ES_HOST;
		apiUrl = hostEndPoint;
		JsonObject esStatus = healthUtil.getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
				ServiceStatusConstants.ES, Boolean.FALSE, username, password, authToken);
		dataComponentStatus.add(ServiceStatusConstants.ES, esStatus);
		log.debug("After ElasticSearch================");
		/* Rabbit Mq health check */
		hostEndPoint = ServiceStatusConstants.RABBIT_MQ;
		apiUrl = hostEndPoint + "/api/overview";
		authToken = null;
		username = ApplicationConfigProvider.getInstance().getMessageQueue().getUser();
		password = ApplicationConfigProvider.getInstance().getMessageQueue().getPassword();
		JsonObject rabbitMq = healthUtil.getClientResponse(hostEndPoint, apiUrl, ServiceStatusConstants.DB,
				ServiceStatusConstants.RabbitMq, Boolean.TRUE, username, password, authToken);
		dataComponentStatus.add(ServiceStatusConstants.RabbitMq, rabbitMq);
		return dataComponentStatus;
		
	}

}
