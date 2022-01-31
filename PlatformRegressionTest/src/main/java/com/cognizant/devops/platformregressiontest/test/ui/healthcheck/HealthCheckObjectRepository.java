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

	@FindBy(xpath = "//label[@class='mainText' and contains(text(),' Notification ')]")
	WebElement notificationLabel;

	@FindBy(xpath = "//label[contains(text(),'Notification History ')]")
	WebElement notificationHistory;

	@FindBy(xpath = "//div[@class='arrange-notification-horizontally']/a/mat-icon")
	WebElement notificationHistoryDetails;

	@FindBy(xpath = "//li[contains(text(),'Workflow History Detail - Health Notification')]")
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

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'serverName')]")
	WebElement serverNameHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'serverName')]")
	WebElement serviceNameHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'ipAddress')]")
	WebElement serviceIpHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'version')]")
	WebElement servicVersionHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'status')]")
	WebElement serviceStatusHeading;

	@FindBy(xpath = "//div[@id='servicesTable']/table/thead/tr/th[contains(@class,'details')]")
	WebElement serviceDetailsHeading;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'ipAddress')]")
	WebElement ipAddressHeading;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'version')]")
	WebElement versionHeading;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'info')]")
	WebElement infoHeading;

	@FindBy(xpath = "//div[@id='dataComponentTable']/table/thead/tr/th[contains(@class,'status')]")
	WebElement statusHeading;

	@FindBy(xpath = "//div[@class='gridheadercenter' and contains(text(),'Enable Notification')]")
	WebElement enableNotificationHeading;

	@FindBy(xpath = "//div[@class='gridheadercenter' and contains(text(),'Disable Notification')]")
	WebElement disableNotificationHeading;

	@FindBy(xpath = "//div[@class='mat-tab-label-content' and contains(text(),'Latest Status Details')]")
	WebElement latestStatusDetailsService;

	@FindBy(xpath = "//mat-slide-toggle/label/div/input[contains(@aria-checked,'false')]/following-sibling::div")
	WebElement notificationToggleFalse;

	@FindBy(xpath = "//mat-slide-toggle/label/div/input[contains(@aria-checked,'true')]/following-sibling::div")
	WebElement notificationToggleTrue;

	@FindBy(xpath = "//span[contains(text(),'YES')]")
	WebElement yes;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement ok;

	@FindBy(xpath = "//span[contains(text(),'Email Configuration')]")
	WebElement emailConfigurationBlock;

	@FindBy(xpath = "((//td[@title='sendEmailEnabled']//span[text()='sendEmailEnabled'])//following::input)[1]")
	WebElement sendEmailEnabled;

	@FindBy(xpath = "((//td[@title='smtpHostServer']//span[text()='smtpHostServer'])//following::input)[1]")
	WebElement smtpHostServer;

	@FindBy(xpath = "((//td[@title='smtpPort']//span[text()='smtpPort'])//following::input)[1]")
	WebElement smtpPort;

	@FindBy(xpath = "((//td[@title='smtpUserName']//span[text()='smtpUserName'])//following::input)[1]")
	WebElement smtpUserName;

	@FindBy(xpath = "((//td[@title='smtpPassword']//span[text()='smtpPassword'])//following::input)[1]")
	WebElement smtpPassword;

	@FindBy(xpath = "((//td[@title='isAuthRequired']//span[text()='isAuthRequired'])//following::input)[1]")
	WebElement isAuthRequired;

	@FindBy(xpath = "((//td[@title='smtpStarttlsEnable']//span[text()='smtpStarttlsEnable'])//following::input)[1]")
	WebElement smtpStarttlsEnable;

	@FindBy(xpath = "((//td[@title='mailFrom']//span[text()='mailFrom'])//following::input)[1]")
	WebElement mailFrom;

	@FindBy(xpath = "((//td[@title='mailTo']//span[text()='mailTo'])//following::input)[1]")
	WebElement mailTo;

	@FindBy(xpath = "((//td[@title='subject']//span[text()='subject'])//following::input)[1]")
	WebElement subject;

	@FindBy(xpath = "((//td[@title='emailBody']//span[text()='emailBody'])//following::input)[1]")
	WebElement emailBody;

	@FindBy(xpath = "((//td[@title='systemNotificationSubscriber']//span//following::input)[1])")
	WebElement systemNotificationSubscriber;

	@FindBy(xpath = "//td[contains(@class,'agentKey')]")
	List<WebElement> instanceIdList;

	@FindBy(xpath = "//td[contains(@class,'status')]")
	List<WebElement> agentsStatusList;

	@FindBy(xpath = "//td[contains(@class,'toolName')]")
	List<WebElement> toolNameList;

	@FindBy(xpath = "//div[@class='gridheadercenter' and contains(text(), 'Success')]")
	WebElement success;

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
