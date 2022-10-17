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
package com.cognizant.devops.platformregressiontest.test.ui.healthcheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author Nainsi
 * 
 *         Class contains the objects used for Health Check module test cases
 *
 */
public class HealthCheckObjectRepository extends LoginAndSelectModule {

	Map<String, String> testData = new HashMap<>();

	@FindBy(xpath = "//div[@id='healthCheckTitle']")
	WebElement landingPage;

	@FindBy(xpath = "//label[@class='main-text' and contains(text(),' Notification ')]")
	WebElement notificationLabel;

	@FindBy(xpath = "//span[contains(text(),'Notification History')]")
	WebElement notificationHistory;
	
	@FindBy(xpath = "//mat-icon[@svgicon='healthcheck_show_details']")
	WebElement notificationHistoryDetails;

	@FindBy(xpath = "//h1[contains(text(),'Workflow History Detail - Health Notification')]")
	WebElement healthNotificationHeading;

	@FindBy(xpath = "//div[@role='tab'] //div[contains(text(),'Services')]")
	WebElement servicesTab;

	@FindBy(xpath = "//div[@role='tab'] //div[contains(text(),'Data Components')]")
	WebElement dataComponentsTab;

	@FindBy(xpath = "//div[@role='tab'] //div[contains(text(),'Agents')]")
	WebElement agentsTab;

	@FindBy(xpath = "//mat-icon[@svgicon='healthcheck_show_details']")
	List<WebElement> detailsList;

	@FindBy(xpath = "//div[contains(text(),'Latest Status Details')]")
	WebElement latestStatusDetails;

	@FindBy(xpath = "//div[contains(text(),'Last Failure Details')]")
	WebElement latestFailureDetails;

	@FindBy(xpath = "//li[contains(text(),'Additional Details')]")
	WebElement additionalDetailsHeading;

	@FindBy(xpath = "//tr[@role='row' and contains(@class,'cdk-row')]")
	List<WebElement> dataComponentsTabData;

	@FindBy(xpath = "//tr[@role='row' and contains(@class,'cdk-row')]")
	List<WebElement> servicesTabData;

	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement closeDialog;

	@FindBy(xpath = "//tr[contains(@class,'tableHeaderCss')]")
	WebElement dataComponentHeaderRowData;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'mat-column-serverName')]")
	WebElement serverNameHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'mat-column-serverName')]")
	WebElement serviceNameHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'mat-column-ipAddress')]")
	WebElement serviceIpHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'mat-column-version')]")
	WebElement servicVersionHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'mat-column-status')]")
	WebElement serviceStatusHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'mat-column-details')]")
	WebElement serviceDetailsHeading;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'mat-column-ipAddress')]")
	WebElement ipAddressHeading;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'mat-column-version')]")
	WebElement versionHeading;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'mat-column-info')]")
	WebElement infoHeading;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'mat-column-status')]")
	WebElement statusHeading;

	@FindBy(xpath = "//div[contains(text(),'Enable Notification')]")
	WebElement enableNotificationHeading;

	@FindBy(xpath = "//div[contains(text(),'Disable Notification')]")
	WebElement disableNotificationHeading;

	@FindBy(xpath = "//div[@class='mat-tab-label-content' and contains(text(),'Latest Status Details')]")
	WebElement latestStatusDetailsService;

	@FindBy(xpath = "//mat-slide-toggle/label/span/input[contains(@aria-checked,'false')]/following-sibling::span")
	WebElement notificationToggleFalse;

	@FindBy(xpath = "//mat-slide-toggle/label/span/input[contains(@aria-checked,'true')]/following-sibling::span")
	WebElement notificationToggleTrue;

	@FindBy(xpath = "//button[@id='yesBtn']")
	WebElement yes;

	@FindBy(xpath = "//button[@id= 'Okay']")
	WebElement ok;
	
	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;


	@FindBy(xpath = "//div[contains(text(),'Email Configuration')]")
	WebElement emailConfigurationBlock;

	@FindBy(xpath = "//input[@name='emailConfiguration_sendEmailEnabled']")
	WebElement sendEmailEnabled;

	@FindBy(xpath = "//input[@name='emailConfiguration_smtpHostServer']")
	WebElement smtpHostServer;

	@FindBy(xpath = "//input[@name='emailConfiguration_smtpPort']")
	WebElement smtpPort;

	@FindBy(xpath = "//input[@name='emailConfiguration_smtpUserName']")
	WebElement smtpUserName;

	@FindBy(xpath = "//input[@name='emailConfiguration_smtpPassword']")
	WebElement smtpPassword;

	@FindBy(xpath = "//input[@name='emailConfiguration_isAuthRequired']")
	WebElement isAuthRequired;

	@FindBy(xpath = "//input[@name='emailConfiguration_smtpStarttlsEnable']")
	WebElement smtpStarttlsEnable;

	@FindBy(xpath = "//input[@name='emailConfiguration_mailFrom']")
	WebElement mailFrom;

	@FindBy(xpath = "//input[@name='emailConfiguration_mailTo']")
	WebElement mailTo;

	@FindBy(xpath = "//input[@name='emailConfiguration_subject']")
	WebElement subject;

	@FindBy(xpath = "//input[@name='emailConfiguration_emailBody']")
	WebElement emailBody;

	@FindBy(xpath = "//input[@name='emailConfiguration_systemNotificationSubscriber']")
	WebElement systemNotificationSubscriber;

	@FindBy(xpath = "//td[contains(@class,'agentKey')]")
	List<WebElement> instanceIdList;

	@FindBy(xpath = "//td[contains(@class,'status')]")
	List<WebElement> agentsStatusList;

	@FindBy(xpath = "//td[contains(@class,'toolName')]")
	List<WebElement> toolNameList;

	@FindBy(xpath = "//span[contains(text(), 'Notification enabled successfully!')]")
	WebElement successEnable;
	
	@FindBy(xpath = "//span[contains(text(), 'Notification disabled successfully!')]")
	WebElement successDisable;

	@FindBy(xpath = "//div[@class='textPadding' and contains(text(), 'Notification enabled')]")
	WebElement notificationEnabledMsg;

	@FindBy(xpath = "//div[@class='textPadding' and contains(text(), 'Notification disabled')]")
	WebElement notificationDisabledMsg;

	@FindBy(xpath = "//label[@class='mainText' and contains(text(), 'Select Tool')]")
	WebElement selectToolLabel;

	@FindBy(xpath = "//mat-select[@name='selectAgentTool']")
	WebElement selectTool;

	@FindBy(xpath = "//div[@role='listbox']/mat-option")
	List<WebElement> selectToolOptionsList;

	@FindBy(xpath = "//td[contains(@class,'serverName ')]")
	List<WebElement> serverNameList;
}
