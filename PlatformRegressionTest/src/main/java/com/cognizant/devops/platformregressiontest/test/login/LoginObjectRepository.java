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
package com.cognizant.devops.platformregressiontest.test.login;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class LoginObjectRepository extends LoginAndSelectModule {
	
	@FindBy(xpath = "//div[@id='invalidMsg']")
	WebElement invalidMsg;
	
	@FindBy(xpath = "//input[contains(@formcontrolname,'username')]")
	WebElement userName;
	
	@FindBy(xpath = "//input[contains(@name,'password')]")
	WebElement password;
	
	@FindBy(xpath = "//span[contains(text(),' LOG ON ')]")
	WebElement sigBtn;
	
	@FindBy(xpath = "//img[contains(@class,'toggleMenu')]")//
	WebElement checkNav;
	
	@FindBy(xpath = "//img[contains(@class,'toggleMenuNotExp')]") //
	WebElement checkNavOpen;
	
	@FindBy(xpath = "//span[contains(@id,'DashboardGroupsName')]") //
	WebElement dashGroup;
	
	@FindBy(xpath = "//button[contains(@class,'toggler-icon')]") //
	WebElement changeTheme;
	
	@FindBy(xpath = "//img[contains(@id,'darkTheme')]") //
	WebElement isLightTheme;
	
	@FindBy(xpath = "//img[contains(@id,'lightTheme')]") //
	WebElement isDarkTheme;

}
