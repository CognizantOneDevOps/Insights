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

public final class ServiceStatusConstants {
	public static final String POSTGRESQL_HOST = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	public static final String PLATFORM_SERVICE_HOST = ApplicationConfigProvider.getInstance().getInsightsServiceURL();
	public static final String INSIGHTS_INFERENCE_MASTER_HOST = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkMasterExecutionEndPoint();
	public static final String NEO4J_HOST = ApplicationConfigProvider.getInstance().getGraph().getEndpoint();
	public static final String ES_HOST = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint();
	public static final String RABBIT_MQ = "http://" + ApplicationConfigProvider.getInstance().getMessageQueue().getHost() + ":15672";
	public static final String NEO4J = "Neo4j";
	public static final String PGSQL = "PostgreSQL";
	public static final String PLATFORM_SERVICE = "Platform Service";
	public static final String INSIGHTSINFERENCE = "Insights Inference Engine";
	public static final String ES = "Elasticsearch";
	public static final String TYPE = "type";
	public static final String DB = "Database";
	public static final String SERVICE = "Service";
	public static final String DEMON_AGENT = "Demon Agent";
	public static final String PLATFORM_WEBHOOK_SUBSCRIBER = "Platform WebhookSubscriber";
	public static final String PLATFORM_WEBHOOKENGINE = "Platform WebhookEngine";
	public static final String PLATFORM_AUDIT_ENGINE = "Platform AuditEngine";
	public static final String PLATFORM_ENGINE = "Platform Engine";
	public static final String PLATFORM_DATA_ARCHIVAL_ENGINE = "Platform DataArchivalEngine";
	public static final String RABBITMQ = "RabbitMQ";
	public static final String AGENTS = "Agents";
	public static final String PLATFORM_WORKFLOW = "Platform Workflow";
	public static final String OTHERS = "Others";
	public static final String LOKI_HOST = "http://localhost:3100";
	public static final String PROMTAIL_HOST = "http://localhost:9080";
	public static final String LOKI = "Loki";
	public static final String PROMTAIL = "Promtail";
	public static final String GRAFANA_HOST = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	public static final String GRAFANA = "Grafana";
	public static final String VAULT_HOST = ApplicationConfigProvider.getInstance().getVault().getVaultEndPoint();
	public static final String VAULT = "Vault";
	public static final String H2O_HOST = ApplicationConfigProvider.getInstance().getMlConfiguration().getH2oEndpoint();
	public static final String H2O = "H2O";
	public static final String PYTHON = "Python";

}
