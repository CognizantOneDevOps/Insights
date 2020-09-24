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
package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author 146414 This class will hold all the config options required for
 *         application setup. These options will also be persisted in DB
 *
 */
public class ApplicationConfigProvider implements Serializable {
	private static ApplicationConfigProvider instance = new ApplicationConfigProvider();
	private EndpointData endpointData = new EndpointData();
	private SparkConfigurations sparkConfigurations = new SparkConfigurations();
	// private LDAPConfiguration ldapConfiguration = new LDAPConfiguration();
	private GraphData graph = new GraphData();
	private GrafanaData grafana = new GrafanaData();
	private MessageQueueDataModel messageQueue = new MessageQueueDataModel();
	private String insightsServiceURL;
	// private boolean disableAuth = false;
	private String insightsTimeZone = "US/Central";
	private PostgreData postgre;
	private String userId;
	private String password;
	private String proxyHost;
	private int proxyPort;
	private Date refreshTime;
	private List<String> trustedHosts = new ArrayList<String>(3);
	private boolean enableOnlineDatatagging = false;
	private EmailConfiguration emailConfiguration = new EmailConfiguration();
	private CorrelationConfig correlations;
	private boolean enableFieldIndex;
	private boolean enableOnlineBackup = false;
	private AgentDetails agentDetails = new AgentDetails();
	private QueryCache queryCache = new QueryCache();
	private boolean enableAuditEngine = false;
	private boolean enableWebHookEngine = false;
	private boolean enableDataArchivalEngine = false;
	private SchedulerConfigData schedulerConfigData = new SchedulerConfigData();
	private String driverLocation;

	private Vault vault = new Vault();
	private String autheticationProtocol = "NativeGrafana";
	private SingleSignOnConfig singleSignOnConfig = new SingleSignOnConfig();
	private AssessmentReport assessmentReport = new AssessmentReport();	
	private WorkflowDetails workflowDetails = new WorkflowDetails();
    private WebhookEngine webhookEngine= new WebhookEngine();
    private String pdfkey;
    
	private ApplicationConfigProvider() {
		this.refreshTime = new Date(new Date().getTime() - 86400000);
	}

	public WebhookEngine getWebhookEngine() {
		return webhookEngine;
	}

	public void setWebhookEngine(WebhookEngine webhookEngine) {
		this.webhookEngine = webhookEngine;
	}

	public static void performSystemCheck() {
		if ((new Date().getTime() - instance.refreshTime.getTime()) >= 86400000) {
			instance.refreshTime = new Date();
		}
	}

	public static ApplicationConfigProvider getInstance() {
		return instance;
	}

	public static void updateConfig(ApplicationConfigProvider cachedInstance) {
		instance = cachedInstance;
	}

	public EndpointData getEndpointData() {
		return endpointData;
	}

	public void setEndpointData(EndpointData endpointData) {
		this.endpointData = endpointData;
	}

	public SparkConfigurations getSparkConfigurations() {
		return sparkConfigurations;
	}

	public void setSparkConfigurations(SparkConfigurations sparkConfigurations) {
		this.sparkConfigurations = sparkConfigurations;
	}

	public MessageQueueDataModel getMessageQueue() {
		return messageQueue;
	}

	public void setMessageQueue(MessageQueueDataModel messageQueue) {
		this.messageQueue = messageQueue;
	}

	public GraphData getGraph() {
		return graph;
	}

	public void setGraph(GraphData graph) {
		this.graph = graph;
	}

	public GrafanaData getGrafana() {
		return grafana;
	}

	public void setGrafana(GrafanaData grafana) {
		this.grafana = grafana;
	}

	public String getInsightsTimeZone() {
		return insightsTimeZone;
	}

	public void setInsightsTimeZone(String insightsTimeZone) {
		this.insightsTimeZone = insightsTimeZone;
	}

	public PostgreData getPostgre() {
		return postgre;
	}

	public void setPostgre(PostgreData postgre) {
		this.postgre = postgre;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(Date refreshTime) {
		this.refreshTime = refreshTime;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public AgentDetails getAgentDetails() {
		return agentDetails;
	}

	public void setAgentDetails(AgentDetails agentDetails) {
		this.agentDetails = agentDetails;
	}

	public EmailConfiguration getEmailConfiguration() {
		return emailConfiguration;
	}

	public void setEmailConfiguration(EmailConfiguration emailConfiguration) {
		this.emailConfiguration = emailConfiguration;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public List<String> getTrustedHosts() {
		return trustedHosts;
	}

	public void setTrustedHosts(List<String> trustedHosts) {
		this.trustedHosts = trustedHosts;
	}

	public String getInsightsServiceURL() {
		return insightsServiceURL;
	}

	public void setInsightsServiceURL(String insightsServiceURL) {
		this.insightsServiceURL = insightsServiceURL;
	}

	public CorrelationConfig getCorrelations() {
		return correlations;
	}

	public void setCorrelations(CorrelationConfig correlations) {
		this.correlations = correlations;
	}

	public boolean isEnableFieldIndex() {
		return enableFieldIndex;
	}

	public void setEnableFieldIndex(boolean enableFieldIndex) {
		this.enableFieldIndex = enableFieldIndex;
	}

	public boolean isEnableOnlineBackup() {
		return enableOnlineBackup;
	}

	public void setEnableOnlineBackup(boolean enableOnlineBackup) {
		this.enableOnlineBackup = enableOnlineBackup;
	}

	public boolean isEnableOnlineDatatagging() {
		return enableOnlineDatatagging;
	}

	public QueryCache getQueryCache() {
		return queryCache;
	}

	public void setQueryCache(QueryCache queryCache) {
		this.queryCache = queryCache;
	}

	public void setEnableOnlineDatatagging(boolean enableOnlineDatatagging) {
		this.enableOnlineDatatagging = enableOnlineDatatagging;
	}

	public SchedulerConfigData getSchedulerConfigData() {
		return schedulerConfigData;
	}

	public void setSchedulerConfigData(SchedulerConfigData schedulerConfigData) {
		this.schedulerConfigData = schedulerConfigData;
	}

	public boolean isEnableAuditEngine() {
		return enableAuditEngine;
	}

	public void setEnableAuditEngine(boolean enableAuditEngine) {
		this.enableAuditEngine = enableAuditEngine;
	}

	public boolean isEnableWebHookEngine() {
		return enableWebHookEngine;
	}

	public void setEnableWebHookEngine(boolean enableWebHookEngine) {
		this.enableWebHookEngine = enableWebHookEngine;
	}

	public String getDriverLocation() {
		return driverLocation;
	}

	public void setDriverLocation(String driverLocation) {
		this.driverLocation = driverLocation;
	}

	public Vault getVault() {
		return vault;
	}

	public void setVault(Vault vault) {
		this.vault = vault;
	}

	public String getAutheticationProtocol() {
		return autheticationProtocol;
	}

	public void setAutheticationProtocol(String autheticationProtocol) {
		this.autheticationProtocol = autheticationProtocol;
	}

	public SingleSignOnConfig getSingleSignOnConfig() {
		return singleSignOnConfig;
	}

	public void setSingleSignOnConfig(SingleSignOnConfig singleSignOnConfig) {
		this.singleSignOnConfig = singleSignOnConfig;
	}

	public AssessmentReport getAssessmentReport() {
		return assessmentReport;
	}

	public void setAssessmentReport(AssessmentReport assessmentReport) {
		this.assessmentReport = assessmentReport;
	}

	public WorkflowDetails getWorkflowDetails() {
		return workflowDetails;
	}

	public void setWorkflowDetails(WorkflowDetails workflowDetails) {
		this.workflowDetails = workflowDetails;
	}

	public boolean isEnableDataArchivalEngine() {
		return enableDataArchivalEngine;
	}

	public void setEnableDataArchivalEngine(boolean enableDataArchivalEngine) {
		this.enableDataArchivalEngine = enableDataArchivalEngine;
	}

	public String getPdfkey() {
		return pdfkey;
	}

	public void setPdfkey(String pdfkey) {
		this.pdfkey = pdfkey;
	}
	
	
}
