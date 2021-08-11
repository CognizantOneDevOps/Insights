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
package com.cognizant.devops.platformregressiontest.test.engineautomation;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class DataCleanupObjectrepository extends LoginAndSelectModule {

	@FindBy(xpath = "//td[contains(@class, 'mat-column-AgentKey')]")
	List<WebElement> agentsList;
	
	@FindBy(xpath = "//mat-icon[@title='Delete ']")
	WebElement deleteAgent;
	
	@FindBy(xpath = "//mat-icon[@title='Stop ']")
	WebElement stopAgent;

	@FindBy(xpath = "//mat-icon[@title=' Start ']")
	WebElement startAgent;
	
	@FindBy(xpath = "//span[@class='mat-button-wrapper' and contains(text(), 'OK')]")
	WebElement ok;
	
	@FindBy(xpath = "//span[@class='mat-button-wrapper' and contains(text(), 'YES')]")
	WebElement yes;
	
	@FindBy(xpath = "//table//tbody[@role='rowgroup']")
	WebElement configurationDetailsTable;
	
	@FindBy(xpath = "//mat-icon[@title='Delete']")
	WebElement deleteButton;
	
	@FindBy(xpath = "//div[contains(text(), ' Success')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[text()= 'YES']")
	WebElement yesButton;

	@FindBy(xpath = "//span[text()= 'OK']")
	WebElement okButton;
	
	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectButton;
}
