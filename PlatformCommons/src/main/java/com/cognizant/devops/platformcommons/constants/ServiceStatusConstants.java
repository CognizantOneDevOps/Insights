/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
 package com.cognizant.devops.platformcommons.constants;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public interface ServiceStatusConstants {
	String POSTGRESQL_HOST = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	String PLATFORM_SERVICE_HOST = ApplicationConfigProvider.getInstance().getInsightsServiceURL();
	String INSIGHTS_INFERENCE_MASTER_HOST = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkMasterExecutionEndPoint();
	String NEO4J_HOST = ApplicationConfigProvider.getInstance().getGraph().getEndpoint();
	String ES_HOST = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint();
	String RABBIT_MQ="http://"+ApplicationConfigProvider.getInstance().getMessageQueue().getHost()+":15672";
	String Neo4j = "Neo4j";
	String PgSQL = "PostgreSQL";
	String PlatformService = "Platform Service";
	String InsightsInference = "Insights Inference Engine";
	String ES = "Elasticsearch";
	String type = "type";
	String DB = "Database";
	String Service = "Service";
	String DemonAgent="Demon Agent";
	String PlatformWebhookSubscriber="Platform WebhookSubscriber";
	String PlatformWebhookEngine="Platform WebhookEngine";
	String PlatformAuditEngine="Platform AuditEngine";
	String PlatformEngine="Platform Engine";
	String PlatformDataArchivalEngine="Platform DataArchivalEngine";
	String RabbitMq="RabbitMQ";
	String Agents="Agents";
}
