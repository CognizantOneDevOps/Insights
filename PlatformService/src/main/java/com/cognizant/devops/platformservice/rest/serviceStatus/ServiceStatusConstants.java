package com.cognizant.devops.platformservice.rest.serviceStatus;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public interface ServiceStatusConstants {
	String POSTGRESQL_HOST = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	String PLATFORM_SERVICE_HOST = ApplicationConfigProvider.getInstance().getInsightsServiceURL();
	String INSIGHTS_INFERENCE_MASTER_HOST = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkMasterExecutionEndPoint();
	String NEO4J_HOST = ApplicationConfigProvider.getInstance().getGraph().getEndpoint();
	String ES_HOST = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint();
	String Neo4j = "Neo4j";
	String PgSQL = "PostgreSQL";
	String PlatformService = "Platform Service";
	String InsightsInference = "Insights Inference Engine";
	String ES = "Elasticsearch";
	String type = "type";
	String DB = "Database";
	String Service = "Service";
}
