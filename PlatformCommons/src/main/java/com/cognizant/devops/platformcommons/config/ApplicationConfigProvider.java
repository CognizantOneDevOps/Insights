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
package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author 146414
 *  This class will hold all the config options required for application setup.
 *  These options will also be persisted in DB
 *
 */
public class ApplicationConfigProvider implements Serializable{
	private static ApplicationConfigProvider instance = new ApplicationConfigProvider();
	private EndpointData endpointData = new EndpointData();
	private SparkConfigurations sparkConfigurations = new SparkConfigurations();
	private LDAPConfiguration ldapConfiguration = new LDAPConfiguration();
	private GraphData graph = new GraphData();
	private GrafanaData grafana = new GrafanaData();
	private MessageQueueDataModel messageQueue = new MessageQueueDataModel();
	private String insightsServiceURL;
	private boolean disableAuth = false;
	private String insightsTimeZone = "US/Central";
	private PostgreData postgre;
	private String userId;
	private String password;
	private String proxyHost;
	private int proxyPort;
	private Date refreshTime;
	private List<String> trustedHosts = new ArrayList<String>(3);	
    private boolean enableOnlineDatatagging = false;
	private boolean enableNativeUsers;
    private CorrelationConfig correlations;

    private EmailConfiguration emailConfiguration=new EmailConfiguration();
    private boolean enableFieldIndex;

    public boolean isEnableOnlineDatatagging() {
		return enableOnlineDatatagging;
	}

	public void setEnableOnlineDatatagging(boolean enableOnlineDatatagging) {
		this.enableOnlineDatatagging = enableOnlineDatatagging;
	}
    
    
	private ApplicationConfigProvider(){
		this.refreshTime = new Date(new Date().getTime() - 86400000);
	}
	
	public static void performSystemCheck(){
		if((new Date().getTime() - instance.refreshTime.getTime()) >= 86400000){
			instance.refreshTime = new Date();
		}
	}
	
	public static ApplicationConfigProvider getInstance(){
		return instance;
	}
	
	public static void updateConfig(ApplicationConfigProvider cachedInstance){
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

	public LDAPConfiguration getLdapConfiguration() {
		return ldapConfiguration;
	}

	public void setLdapConfiguration(LDAPConfiguration ldapConfiguration) {
		this.ldapConfiguration = ldapConfiguration;
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

	public boolean isDisableAuth() {
		return disableAuth;
	}

	public void setDisableAuth(boolean disableAuth) {
		this.disableAuth = disableAuth;
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

	public boolean isEnableNativeUsers() {
		return enableNativeUsers;
	}

	public void setEnableNativeUsers(boolean enableNativeUsers) {
		this.enableNativeUsers = enableNativeUsers;
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
}
