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

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * 
 * @author 146414 This class will hold all the config options required for
 *         application setup. These options will also be persisted in DB
 *
 */
public class ApplicationConfigProvider implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1624602880L;
	
	private static ApplicationConfigProvider instance = new ApplicationConfigProvider();
	private EndpointData endpointData = new EndpointData();
	private SparkConfigurations sparkConfigurations = new SparkConfigurations();
	@Valid
	private GraphData graph = new GraphData();
	@Valid
	private GrafanaData grafana = new GrafanaData();
	@Valid
	private MessageQueueDataModel messageQueue = new MessageQueueDataModel();
	private String insightsServiceURL;
	private String insightsTimeZone = "UTC";
	@Valid
	private PostgreData postgre = new PostgreData();
	private Date refreshTime;
	private List<String> trustedHosts = new ArrayList<>(3);
	private boolean enableOnlineDatatagging = false;
	private EmailConfiguration emailConfiguration = new EmailConfiguration();
	private CorrelationConfig correlations = new CorrelationConfig();
	private AgentDetails agentDetails = new AgentDetails();
	private QueryCache queryCache = new QueryCache();
	private boolean enableAuditEngine = false;
	private boolean enableWebHookEngine = false;
	private boolean enableDataArchivalEngine = false;
	private String driverLocation;

	private Vault vault = new Vault();
	@NotEmpty @NotBlank
	private String autheticationProtocol = "NativeGrafana";
	private SingleSignOnConfig singleSignOnConfig = new SingleSignOnConfig();
	private AssessmentReport assessmentReport = new AssessmentReport();	
	private WorkflowDetails workflowDetails = new WorkflowDetails();
	private MlConfiguration mlConfiguration = new MlConfiguration();
	private ProxyConfiguration proxyConfiguration = new ProxyConfiguration();

    private WebhookEngine webhookEngine= new WebhookEngine();
    private String pdfkey;
	private ApplicationLogLevel applicationLogLevel = new ApplicationLogLevel();
    
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

	public Date getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(Date refreshTime) {
		this.refreshTime = refreshTime;
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
	
	public MlConfiguration getMlConfiguration() {
		return mlConfiguration;
	}

	public void setMlConfiguration(MlConfiguration mlConfiguration) {
		this.mlConfiguration = mlConfiguration;
	}

	public ProxyConfiguration getProxyConfiguration() {
		return proxyConfiguration;
	}

	public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
		this.proxyConfiguration = proxyConfiguration;
	}

	public ApplicationLogLevel getApplicationLogLevel() {
		return applicationLogLevel;
	}

	public void setApplicationLogLevel(ApplicationLogLevel applicationLogLevel) {
		this.applicationLogLevel = applicationLogLevel;
	}
}
