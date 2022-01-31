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

	@FindBy(xpath = "//div//a[contains(text(),'Server Configuration')]")
	WebElement landingPage;
	
	@FindBy(xpath = "((//td[@title='isOnlineRegistration']//span[text()='isOnlineRegistration'])//following::input)[1]")
	WebElement isOnlineRegistrationValue;
	
	@FindBy(xpath = "((//td[contains(@title,'offlineAgentPath')]//span[text()='offlineAgentPath'])//following::input)[1]")
	WebElement offlineAgentPathValue;

	@FindBy(xpath = "//mat-icon[@title='Save']")
	WebElement saveButton;
	
	@FindBy(xpath = "//span[text()= 'YES']")
	WebElement yesButton;
	
	@FindBy(xpath = "//div[contains(text(), ' Success')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement okButton;
	
	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectButton;

}
