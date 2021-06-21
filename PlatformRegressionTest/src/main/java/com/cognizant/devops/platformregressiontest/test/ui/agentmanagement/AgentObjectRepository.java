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

	@FindBy(xpath = "//div[contains(text(),'Agent List ')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-icon[@title='Add Agent']")
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

	@FindBy(xpath = "((//td[@class='mat-cell-header']//span[text()='user'])//following::input)[1]")
	WebElement mqUser;

	@FindBy(xpath = "((//td[@class='mat-cell-header']//span[text()='password'])//following::input)[1]")
	WebElement mqPassword;// span[text()=' CANCEL ']

	@FindBy(xpath = "((//td[@class='mat-cell-header']//span[text()='agentId'])//following::input)[1]")
	WebElement agentIdValue;

	@FindBy(xpath = "((//td[@class='mat-cell-header']//span[text()='accessToken'])//following::input)[1]")
	WebElement accessToken;

	@FindBy(xpath = "((//td[@class='mat-cell-header']//span[text()='getRepos'])//following::input)[1]")
	WebElement getRepos;

	@FindBy(xpath = "((//td[@class='mat-cell-header']//span[text()='commitsBaseEndPoint'])//following::input)[1]")
	WebElement commitsBaseEndPoint;

	@FindBy(xpath = "//span[text()=' ADD ']")
	WebElement addAgent;

	@FindBy(xpath = "//span[text()=' CANCEL ']")
	WebElement cancel;

	@FindBy(xpath = "//div[text()=' Success ']")
	WebElement success;

	@FindBy(xpath = "//div[text() =  ' Error ']")
	WebElement duplicateError;

	@FindBy(xpath = "//span[text()=' CANCEL ']")
	WebElement cancelButton;

	@FindBy(xpath = "//div[text()=' Cancel Agent ']")
	WebElement cancelAgentMessage;

	@FindBy(xpath = "//div[contains(@class, 'mat-select-trigger')]")
	WebElement selectToolName;

	@FindBy(xpath = "//mat-option[@role='option']")
	List<WebElement> toolNameList;

	@FindBy(xpath = "//td[contains(@class, 'mat-column-AgentKey')]")
	List<WebElement> agentsList;

	@FindBy(xpath = "//mat-icon[@title='Stop ']")
	WebElement stopAgent;

	@FindBy(xpath = "//mat-icon[@title=' Start ']")
	WebElement startAgent;

	@FindBy(xpath = "//mat-icon[@title='Edit ']")
	WebElement editAgent;

	@FindBy(xpath = "((//td[@class='mat-cell-header']//span[text()='data'])//following::input)[1]")
	WebElement dataLabelName;

	@FindBy(xpath = "((//td[@class='mat-cell-header']//span[text()='health'])//following::input)[1]")
	WebElement healthLabelName;

	@FindBy(xpath = "//span[text()=' UPDATE ']")
	WebElement update;

	@FindBy(xpath = "//mat-icon[@title='Delete ']")
	WebElement deleteAgent;

	@FindBy(xpath = "//span[text()='Select Type']")
	WebElement selectType;

	@FindBy(xpath = "//span[contains(text(),'Agent Configuration')]")
	WebElement agentConfigurationHeading;

	@FindBy(xpath = "//td[@title='isOnlineRegistration']")
	WebElement isOnlineRegistration;

	@FindBy(xpath = "((//td[@title='isOnlineRegistration']//span[text()='isOnlineRegistration'])//following::input)[1]")
	WebElement isOnlineRegistrationValue;

	@FindBy(xpath = "//td[@title='browseRepoUrl']")
	WebElement browseRepoUrl;

	@FindBy(xpath = "((//td[@title='browseRepoUrl']//span[text()='browseRepoUrl'])//following::input)[1]")
	WebElement browseRepoUrlValue;

	@FindBy(xpath = "//td[@title='downloadRepoUrl']")
	WebElement downloadRepoUrl;

	@FindBy(xpath = "((//td[@title='downloadRepoUrl']//span[text()='downloadRepoUrl'])//following::input)[1]")
	WebElement downloadRepoUrlValue;
	
	@FindBy(xpath = "//td[contains(@title,'onlineRegistrationMod')]")
	WebElement onlineRegistrationMode;
	
	@FindBy(xpath = "((//td[@title='onlineRegistrationMode']//span[contains(text(), 'onlineRegistrationMod')])//following::input)[1]\r\n" + 
			"")
	WebElement onlineRegistrationModeValue;
	
	@FindBy(xpath = "//td[contains(@title,'offlineAgentPath')]")
	WebElement offlineAgentPath;
	
	@FindBy(xpath = "((//td[contains(@title,'offlineAgentPath')]//span[text()='offlineAgentPath'])//following::input)[1]")
	WebElement offlineAgentPathValue;
	
	@FindBy(xpath = "//td[contains(@title,'unzipPath')]")
	WebElement unzipPath;
	
	@FindBy(xpath = "((//td[contains(@title,'unzipPath')]//span[text()='unzipPath'])//following::input)[1]")
	WebElement unzipPathValue;
	
	@FindBy(xpath = "//td[contains(@title,'agentExchange')]")
	WebElement agentExchange;
	
	@FindBy(xpath = "((//td[contains(@title,'agentExchange')]//span[text()='agentExchange'])//following::input)[1]")
	WebElement agentExchangeValue;
	
	@FindBy(xpath = "//td[contains(@title,'agentPkgQueue')]")
	WebElement agentPkgQueue;
	
	@FindBy(xpath = "((//td[contains(@title,'agentPkgQueue')]//span[text()='agentPkgQueue'])//following::input)[1]")
	WebElement agentPkgQueueValue;
	
	@FindBy(xpath = "//mat-icon[@title='Save']")
	WebElement save;

	@FindBy(xpath = "//span[@class='mat-button-wrapper' and contains(text(), 'OK')]")
	WebElement ok;

	@FindBy(xpath = "//span[@class='mat-button-wrapper' and contains(text(), 'YES')]")
	WebElement yes;

	@FindBy(xpath = "//div[contains(@class,'textPadding')]")
	WebElement successMsg;
	
	@FindBy(xpath = "//div[contains(text(), 'Agent Registration Failed. ')]")
	WebElement errorMsg;

	@FindBy(xpath = "//div[contains(text(), 'Success')]")
	WebElement successHeading;

}
