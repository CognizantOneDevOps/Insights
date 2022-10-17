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
package com.cognizant.devops.platformregressiontest.test.ui.serverconfiguration;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the objects used for server configuration module test cases
 *
 */
public class ServerConfigurationObjectRepository extends LoginAndSelectModule{

	@FindBy(xpath = "//span//b[contains(text(),'Server Configuration')]")
	WebElement landingPage;
	
	@FindBy(xpath = "((//td[@title='isOnlineRegistration']//span[text()='isOnlineRegistration'])//following::input)[1]")
	WebElement isOnlineRegistrationValue;
	
	@FindBy(xpath = "((//td[contains(@title,'offlineAgentPath')]//span[text()='offlineAgentPath'])//following::input)[1]")
	WebElement offlineAgentPathValue;

	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement saveButton;
	
	@FindBy(xpath = "//button[@id= 'yesBtn']")
	WebElement yesButton;
	
	@FindBy(xpath = "//div//h1[contains(text(), 'Success')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//button[@id= 'onOkClose']")
	WebElement okClose;
	
	@FindBy(xpath = "//mat-icon[@svgicon='homeBck']")
	WebElement redirectButton;
	
	@FindBy(xpath = "//input[@placeholder='Search']")
	WebElement mainLandingPage;
	
	@FindBy(xpath = "//input[@name='grafana_grafanaEndpoint']")
	WebElement grafanaInput;

	@FindBy(xpath = "//input[@name='grafana_grafanaDBEndpoint']")
	WebElement grafanaDBEndpoint;
	
	@FindBy(xpath = "//input[@name='grafana_adminUserName']")
	WebElement grafanaadminUserName;

	@FindBy(xpath = "//input[@name='grafana_adminUserPassword']")
	WebElement grafanaadminUserPassword;

	@FindBy(xpath = "//span[contains(text(),'Please fill Grafana details.')]")
	WebElement grafanaErrorMsg;
	
	@FindBy(xpath = "//input[@name='graph_endpoint']")
	WebElement graphendpoint;

	@FindBy(xpath = "//input[@name='graph_authToken']")
	WebElement graphauthToken;
	
	@FindBy(xpath = "//input[@name='graph_boltEndPoint']")
	WebElement graphboltEndPoint;

	@FindBy(xpath = "//span[contains(text(),'Please fill Neo4j details.')]")
	WebElement neo4jErrorMsg;
	
	@FindBy(xpath = "//input[@name='postgre_userName']")
	WebElement postgreUserName;

	@FindBy(xpath = "//input[@name='postgre_password']")
	WebElement postgrePassword;

	@FindBy(xpath = "//input[@name='postgre_insightsDBUrl']")
	WebElement postgreInsightsDBUrl;

	@FindBy(xpath = "//input[@name='postgre_grafanaDBUrl']")
	WebElement postgreGrafanaDBUrl;

	@FindBy(xpath = "//span[contains(text(),'Please fill Postgre details.')]")
	WebElement postgreErrorMsg;

	@FindBy(xpath = "//input[@name='messageQueue_host']")
	WebElement messageQueueHost;
	
	@FindBy(xpath = "//input[@name='messageQueue_user']")
	WebElement messageQueueUser;

	@FindBy(xpath = "//input[@name='messageQueue_password']")
	WebElement messageQueuePassword;

	@FindBy(xpath = "//span[contains(text(),'Please fill Message Queue Config details.')]")
	WebElement msgQueueErrorMsg;

	@FindBy(xpath = "//input[@name='insightsServiceURL']")
	WebElement insightsServiceURL;
	
	@FindBy(xpath = "//span[contains(text(),'Please fill insightsServiceURL, it cannot be empty.')]")
	WebElement serviceUrlErrorMsg;

	@FindBy(xpath = "//input[@name='trustedHosts']")
	WebElement trustedHosts;

	@FindBy(xpath = "//span[contains(text(),'Please fill trusted hosts details, it cannot be empty.')]")
	WebElement trustedHostsErrorMsg;
	
	@FindBy(xpath = "//input[@name='singleSignOnConfig_tokenSigningKey']")
	WebElement singleSignOnConfig;

	@FindBy(xpath = "//span[contains(text(),'Please fill SingleSignOnConfig tokenSigningKey details.')]")
	WebElement signOnErrorMsg;

	@FindBy(xpath = "//input[@name='applicationLogLevel']")
	WebElement applicationLogLevel;

	@FindBy(xpath = "//span[contains(text(),'Please fill authentication protocol.')]")
	WebElement applicationLogErrorMsg;

	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;
	
	@FindBy(xpath = "//input[@name='endpointData_elasticSearchEndpoint']")
	WebElement elasticSearchEndpoint;

	

}
