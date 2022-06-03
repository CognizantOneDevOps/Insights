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
	String PLATFORM_SERVICE_HOST = ApplicationConfigProvider.getInstance().getInsightsServiceURL();
	String INSIGHTS_INFERENCE_MASTER_HOST = ApplicationConfigProvider.getInstance().getSparkConfigurations()
			.getSparkMasterExecutionEndPoint();
	String ES_HOST = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint();
	String PlatformService = "Platform Service";
	String InsightsInference = "Insights Inference Engine";
	String ES = "Elasticsearch";
	String type = "type";
	String DB = "Database";
	String OTHERS = "Others";
	String Service = "Service";
	String DemonAgent = "Demon Agent";
	String PlatformWebhookSubscriber = "Platform WebhookSubscriber";
	String PlatformWebhookEngine = "Platform WebhookEngine";
	String PlatformAuditEngine = "Platform AuditEngine";
	String PlatformEngine = "Platform Engine";
	String PlatformDataArchivalEngine = "Platform DataArchivalEngine";
	String PgSQL = "PostgreSQL";
	String POSTGRESQL_HOST = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	String Neo4j = "Neo4j";
	String NEO4J_HOST = ApplicationConfigProvider.getInstance().getGraph().getEndpoint();
	String RabbitMq = "RabbitMQ";
	String RABBIT_MQ_HOST = "http://" + ApplicationConfigProvider.getInstance().getMessageQueue().getHost() + ":15672";
	String Agents = "Agents";
	String PlatformWorkflow = "Platform Workflow";
	String LOKI_HOST = "http://localhost:3100";
	String PROMTAIL_HOST = "http://localhost:9080";
	String LOKI = "Loki";
	String PROMTAIL = "Promtail";
	String GRAFANA_HOST = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	String GRAFANA = "Grafana";
	String VAULT_HOST = ApplicationConfigProvider.getInstance().getVault().getVaultEndPoint();
	String VAULT = "Vault";
	String H2O_HOST = ApplicationConfigProvider.getInstance().getMlConfiguration().getH2oEndpoint();
	String H2O = "H2O";
	String PYTHON = "Python";
}
