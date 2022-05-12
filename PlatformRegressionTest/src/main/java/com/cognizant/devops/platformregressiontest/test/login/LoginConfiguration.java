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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class LoginConfiguration extends LoginObjectRepository {
	
	private static final Logger log = LogManager.getLogger(LoginConfiguration.class);

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

	public LoginConfiguration() {
		PageFactory.initElements(driver, this);
	}

	public boolean loginWithInvalidCredentials() {
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		userName.sendKeys(LoginAndSelectModule.testData.get("invalidUsername"));
		password.sendKeys(LoginAndSelectModule.testData.get("invalidPassword"));
		sigBtn.click();
		try {
			wait.until(ExpectedConditions.visibilityOf(invalidMsg));
			log.info(invalidMsg.getText());
			return true;
		}catch (Exception e) {
			log.info("Invalid message not visible.");
			return false;
		}
	}

	public boolean loginWithValidCredentials() throws InterruptedException {
		userName.clear();
		userName.sendKeys(LoginAndSelectModule.testData.get("username"));
		password.clear();
		password.sendKeys(LoginAndSelectModule.testData.get("password"));
		sigBtn.click();
		Thread.sleep(4000);
		driver.findElement(By.xpath("(//p[contains(@class,'line-child')])[4]")).click();
		Thread.sleep(2000);
		log.info("Login successful.");
		return true;
	}

	public boolean checkLogoutFunctionality() {
		driver.findElement(By.xpath("(//p[contains(@class, 'mat-list-text')])[3]")).click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		if(driver.getTitle().equals("Insights")) {
			return true;
		}
		return false;
	}

}
