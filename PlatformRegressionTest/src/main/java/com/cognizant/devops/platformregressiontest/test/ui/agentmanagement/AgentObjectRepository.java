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
package com.cognizant.devops.platformregressiontest.test.ui.agentmanagement;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class AgentObjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//div[@id='agentList']")
	WebElement landingPage;

	@FindBy(xpath = "//button[@id='addAgent']")
	WebElement clickAddButton;

	@FindBy(xpath = "//mat-select[@name ='selectedOS']")
	WebElement selectOs;

	@FindBy(xpath = "//div[@role='listbox']//mat-option[contains(@class, 'mat-focus-indicator')]")
	List<WebElement> osList;

	@FindBy(xpath = "//mat-select[@name ='selectedType']")
	WebElement selectAgentType;
	
	@FindBy(xpath = "//div[@role='listbox']//mat-option[contains(@class, 'mat-focus-indicator')]")
	List<WebElement> typeList;

	@FindBy(xpath = "//mat-select[@name ='selectedVersion']")
	WebElement selectVersion;

	@FindBy(xpath = "//div[@role='listbox']//mat-option[contains(@role, 'option')]")
	List<WebElement> versionList;

	@FindBy(xpath = "//mat-select[@name ='selectedTool']")
	WebElement selectTools;


	@FindBy(xpath = "//div[@role='listbox']//mat-option[contains(@class, 'mat-focus-indicator')]")
	List<WebElement> toolsList;

	@FindBy(xpath = "//input[@name='user']")
	WebElement mqUser;

	@FindBy(xpath = "//input[@name='password']")
	WebElement mqPassword;// span[text()=' CANCEL ']a

	@FindBy(xpath = "//input[@name='agentId']")
	WebElement agentIdValue;
	
	@FindBy(xpath = "//input[@name='accessToken']")
	WebElement accessToken;

	@FindBy(xpath = "//input[@name='getRepos']")
	WebElement getRepos;

	@FindBy(xpath = "//input[@name='commitsBaseEndPoint']")
	WebElement commitsBaseEndPoint;

	@FindBy(xpath = "//button[@id='addAgent']")
	WebElement addAgent;

	@FindBy(xpath = "//span[text()=' CANCEL ']")
	WebElement cancel;

	@FindBy(xpath = "//div[text()=' Success ']")
	WebElement success;
	
	@FindBy(xpath = "//span[text()='Agent updated Successfully']")
	WebElement updateSuccess;

	@FindBy(xpath = "//div[text() =  ' Error ']")
	WebElement duplicateError;

	@FindBy(xpath = "//button[@id='cancelAgent']")
	WebElement cancelButton;

	@FindBy(xpath = "//div[text()=' Cancel Agent ']")
	WebElement cancelAgentMessage;

	@FindBy(xpath = "//div[contains(@class, 'mat-select-trigger')]")
	WebElement selectToolName;

	@FindBy(xpath = "//mat-option[@role='option']")
	List<WebElement> toolNameList;

	@FindBy(xpath = "//td[contains(@class, 'mat-column-AgentKey')]")
	List<WebElement> agentsList;

	@FindBy(xpath = "//button[@id='stopAgent']")
	WebElement stopAgent;

	@FindBy(xpath = "//button[@id='startAgent']")
	WebElement startAgent;

	@FindBy(xpath = "//button[@id='editAgent']")
	WebElement editAgent;

	@FindBy(xpath = "//input[@name='data']")
	WebElement dataLabelName;

	@FindBy(xpath = "//input[@name='health']")
	WebElement healthLabelName;

	@FindBy(xpath = "//div[text()='Update']")
	WebElement update;

	@FindBy(xpath = "//button[@id='deleteAgent']")
	WebElement deleteAgent;

	@FindBy(xpath = "//span[text()='Select Type']")
	WebElement selectType;

	@FindBy(xpath = "//div[contains(text(),'Agent Configuration')]")
	WebElement agentConfigurationHeading;

	@FindBy(xpath = "//input[@name='agentDetails_isOnlineRegistration']")
	WebElement isOnlineRegistration;

	@FindBy(xpath = "((//td[@title='isOnlineRegistration']//span[text()='isOnlineRegistration'])//following::input)[1]")
	WebElement isOnlineRegistrationValue;

	@FindBy(xpath = "//input[@name='agentDetails_browseRepoUrl']")
	WebElement browseRepoUrl;

	@FindBy(xpath = "((//td[@title='browseRepoUrl']//span[text()='browseRepoUrl'])//following::input)[1]")
	WebElement browseRepoUrlValue;

	@FindBy(xpath = "//input[@name='agentDetails_downloadRepoUrl']")
	WebElement downloadRepoUrl;

	@FindBy(xpath = "((//td[@title='downloadRepoUrl']//span[text()='downloadRepoUrl'])//following::input)[1]")
	WebElement downloadRepoUrlValue;
	
	@FindBy(xpath = "//input[contains(@name,'agentDetails_onlineRegistrationMode')]")
	WebElement onlineRegistrationMode;
	
	@FindBy(xpath = "((//td[@title='onlineRegistrationMode']//span[contains(text(), 'onlineRegistrationMod')])//following::input)[1]\r\n" + 
			"")
	WebElement onlineRegistrationModeValue;
	
	@FindBy(xpath = "//input[contains(@name,'agentDetails_offlineAgentPath')]")
	WebElement offlineAgentPath;
	
	@FindBy(xpath = "((//td[contains(@title,'offlineAgentPath')]//span[text()='offlineAgentPath'])//following::input)[1]")
	WebElement offlineAgentPathValue;
	
	@FindBy(xpath = "//input[contains(@name,'agentDetails_unzipPath')]")
	WebElement unzipPath;
	
	@FindBy(xpath = "((//td[contains(@title,'unzipPath')]//span[text()='unzipPath'])//following::input)[1]")
	WebElement unzipPathValue;
	
	@FindBy(xpath = "//input[contains(@name,'agentDetails_agentExchange')]")
	WebElement agentExchange;
	
	@FindBy(xpath = "((//td[contains(@title,'agentExchange')]//span[text()='agentExchange'])//following::input)[1]")
	WebElement agentExchangeValue;
	
	@FindBy(xpath = "//input[contains(@name,'agentDetails_agentPkgQueue')]")
	WebElement agentPkgQueue;
	
	@FindBy(xpath = "((//td[contains(@title,'agentPkgQueue')]//span[text()='agentPkgQueue'])//following::input)[1]")
	WebElement agentPkgQueueValue;
	
	@FindBy(xpath = "//button[@id='saveServerConfig']")
	WebElement save;

	@FindBy(xpath = "//button[@id='onOkClose']")
	WebElement ok;
	
	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;

	@FindBy(xpath = "//button[@id='yesBtn']")
	WebElement yes;

	@FindBy(xpath = "//div[contains(@class,'textPadding')]")
	WebElement successMsg;
	
	@FindBy(xpath = "//span[contains(text(),'Agent Registered Successfully')]")
	WebElement regSuccessMsg;
	
	@FindBy(xpath = "//span[contains(text(), 'Agent Registration Failed. ')]")
	WebElement errorMsg;

	@FindBy(xpath = "//h1[contains(text(), 'Success')]")
	WebElement successHeading;
	
	@FindBy(xpath = "//span[contains(text(), 'This is not a webhook Agent')]")
	WebElement notWebhookMsg;
	
	@FindBy(xpath = "//span[contains(text(), 'This is not a ROI Agent')]")
	WebElement notROIMsg;

	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement backBtn;

}
