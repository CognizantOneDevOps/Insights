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

	@FindBy(xpath = "//mat-icon[@title='Add New WebHook']")
	WebElement addWebhook;

	@FindBy(xpath = "//input[@name ='webhookName']")
	WebElement webhookName;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> toolNameList;

	@FindBy(xpath = "//div/span[contains(text(),'Select Tool')]")
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

	@FindBy(xpath = "//input[@placeholder=' Time Field']")
	WebElement timeField;

	@FindBy(xpath = "//input[@placeholder='Time Format']")
	WebElement timeFormat;

	@FindBy(xpath = "//textarea[@name ='eventConfig']")
	WebElement eventConfig;

	@FindBy(xpath = "//tr[9]//div[@class='mat-slide-toggle-thumb']")
	WebElement nodeToggleButton;

	@FindBy(xpath = "//tr[3]//div[@class='mat-slide-toggle-thumb']")
	WebElement eventToggleButton;

	@FindBy(xpath = "//input[@placeholder='Field required for updation']")
	WebElement nodeField;

	@FindBy(xpath = "//table//tbody[@role='rowgroup']")
	WebElement webhookDetailsTable;

	@FindBy(xpath = "//mat-icon[@title='Edit']")
	WebElement editButton;

	@FindBy(xpath = "//mat-icon[@title='Save']")
	WebElement saveButton;

	@FindBy(xpath = "//span[text()= 'YES']")
	WebElement yesButton;

	@FindBy(xpath = "//span[text()= 'NO']")
	WebElement noButton;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement okButton;

	@FindBy(xpath = "//div[contains(text(), ' Success')]")
	WebElement successMessage;

	@FindBy(xpath = "//div[contains(text(), ' Error')]")
	WebElement errorMessage;

	@FindBy(xpath = "//div[@class='closeIconCss']//mat-icon")
	WebElement closeIcon;

	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectButton;
	
	@FindBy(xpath = "//td[contains(@class, 'mat-column-WebHookName')]")
	List<WebElement> webhooknameList;

}
