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
package com.cognizant.devops.platformregressiontest.test.ui.webhook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the objects used for Webhook Configuration module test
 *         cases
 *
 */
public class WebhookObjectRepository extends LoginAndSelectModule{

	Map<String, String> testData = new HashMap<>();

	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement addWebhook;

	@FindBy(xpath = "//input[@name ='webhookName']")
	WebElement webhookName;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> toolNameList;

	@FindBy(xpath = "//mat-select[@placeholder='Select Tool']")
	WebElement selectTool;

	@FindBy(xpath = "//span[contains(text(),'Select Data Format')]")
	WebElement dataFormat;

	@FindBy(xpath = "//mat-option[contains(@class, 'mat-focus-indicator')]")
	List<WebElement> dataFormatList;

	@FindBy(xpath = "//input[@name='labelDisplay']")
	WebElement labelName;

	@FindBy(xpath = "//textarea[@name ='dynamicTemplate']")
	WebElement dynamicTemplate;

	@FindBy(xpath = "//textarea[@name ='responseTemplate']")
	WebElement responseTemplate;

	@FindBy(xpath = "//input[@placeholder='Time Field']")
	WebElement timeField;

	@FindBy(xpath = "//input[@placeholder='Time Format']")
	WebElement timeFormat;

	@FindBy(xpath = "//textarea[@name ='eventConfig']")
	WebElement eventConfig;

	@FindBy(xpath = "//mat-slide-toggle[@id='nodeUpdation']")
	WebElement nodeToggleButton;

	@FindBy(xpath = "//mat-slide-toggle[@id='eventProcessing']")
	WebElement eventToggleButton;

	@FindBy(xpath = "//input[@placeholder='Field required for updation']")
	WebElement nodeField;

	@FindBy(xpath = "//table//tbody[@role='rowgroup']")
	WebElement webhookDetailsTable;

	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement editButton;

	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement saveButton;

	@FindBy(xpath = "//button[@id='yesBtn']")
	WebElement yesButton;

	@FindBy(xpath = "//span[text()= 'NO']")
	WebElement noButton;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement okButton;

	@FindBy(xpath =  "//span[contains(text(),'successfully.')]")
	WebElement successMessage;
	
	@FindBy(xpath =  "//span[contains(text(),'Changes made to')]")
	WebElement editSuccessMessage;
	
	@FindBy(xpath = "//span[contains(text(),'already exists.')]")
	WebElement errorMessage;
	
	@FindBy(xpath = "//span[contains(text(),'Invalid label Name.')]")
	WebElement invalidMessage;
	
	@FindBy(xpath = "//span[contains(text(),'Incorrect Dynamic Template')]")
	WebElement incorrectMessage;
	
	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;

	@FindBy(xpath = "//div[@class='closeIconCss']//mat-icon")
	WebElement closeIcon;

	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement redirectButton;
	
	@FindBy(xpath = "//td[contains(@class, 'mat-column-WebHookName')]")
	List<WebElement> webhooknameList;

}
