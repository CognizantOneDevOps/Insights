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
package com.cognizant.devops.platformregressiontest.test.ui.agentmanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class AgentManagementConfiguration extends LoginAndSelectModule {

	WebDriverWait wait = new WebDriverWait(driver, 20);

	Map<String, String> testData = new HashMap<>();

	@FindBy(xpath = "//div[contains(text(),'Agent List ')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-icon[@title='Add Agent']")
	WebElement clickAddButton;

	@FindBy(xpath = "//mat-select[@name ='selectedOS']")
	WebElement selectOs;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	private List<WebElement> osList;

	@FindBy(xpath = "//mat-select[@name ='selectedVersion']")
	WebElement selectVersion;

	@FindBy(xpath = "//mat-option[contains(@class, 'mat-focus-indicator')]")
	private List<WebElement> versionList;

	@FindBy(xpath = "//mat-select[@name ='selectedTool']")
	WebElement selectTools;

	@FindBy(xpath = "//mat-option[contains(@class, 'mat-focus-indicator')]")
	private List<WebElement> toolsList;

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

	@FindBy(xpath = "//span[text()='OK']")
	WebElement ok;

	@FindBy(xpath = "//span[text()=' CANCEL ']")
	WebElement cancelButton;

	@FindBy(xpath = "//div[text()=' Cancel Agent ']")
	WebElement cancelAgentMessage;

	@FindBy(xpath = "//div[contains(@class, 'mat-select-trigger')]")
	WebElement selectToolName;

	@FindBy(xpath = "//mat-option[@role='option']")
	private List<WebElement> toolNameList;

	@FindBy(xpath = "//td[contains(@class, 'mat-column-AgentKey')]")
	private List<WebElement> agentsList;

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

	@FindBy(xpath = "//span[text()='YES']")
	WebElement yes;

	public AgentManagementConfiguration() {
		PageFactory.initElements(driver, this);
	}

	public boolean navigateToAgentManagementLandingPage() {

		return landingPage.isDisplayed();
	}

	public boolean registerAgent() throws InterruptedException {

		clickAddButton.click();
		driver.manage().timeouts().implicitlyWait(60000, TimeUnit.SECONDS);
		selectOs(LoginAndSelectModule.testData.get("osName"));
		Thread.sleep(2000);
		Thread.sleep(2000);
		Thread.sleep(2000);
		selectVersion(LoginAndSelectModule.testData.get("version"));
		driver.manage().timeouts().implicitlyWait(60000, TimeUnit.SECONDS);
		selectTools(LoginAndSelectModule.testData.get("toolName"));
		wait.until(ExpectedConditions.visibilityOf(mqUser));
		mqUser.clear();
		Thread.sleep(2000);
		mqUser.sendKeys(LoginAndSelectModule.testData.get("mqUser"));
		mqPassword.clear();
		mqPassword.sendKeys(LoginAndSelectModule.testData.get("mqPassword"));
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView();", agentIdValue);
		agentIdValue.sendKeys(LoginAndSelectModule.testData.get("agentId"));
		accessToken.clear();
		accessToken.sendKeys(LoginAndSelectModule.testData.get("accessToken"));
		getRepos.clear();
		getRepos.sendKeys(LoginAndSelectModule.testData.get("getRepos"));
		commitsBaseEndPoint.clear();
		commitsBaseEndPoint.sendKeys(LoginAndSelectModule.testData.get("commitsBaseEndPoint"));
		wait.until(ExpectedConditions.visibilityOf(addAgent));
		addAgent.click();
		success.isDisplayed();
		ok.click();
		return verifyAgentId(LoginAndSelectModule.testData.get("agentId"));

	}

	public boolean registerAgentWithSameAgentId() throws InterruptedException {

		clickAddButton.click();
		driver.manage().timeouts().implicitlyWait(60000, TimeUnit.SECONDS);
		selectOs(LoginAndSelectModule.testData.get("osName"));
		Thread.sleep(2000);
		Thread.sleep(2000);
		selectVersion(LoginAndSelectModule.testData.get("version"));
		driver.manage().timeouts().implicitlyWait(60000, TimeUnit.SECONDS);
		selectTools(LoginAndSelectModule.testData.get("toolName"));
		wait.until(ExpectedConditions.visibilityOf(mqUser));
		mqUser.clear();
		Thread.sleep(2000);
		mqUser.sendKeys(LoginAndSelectModule.testData.get("mqUser"));
		mqPassword.clear();
		mqPassword.sendKeys(LoginAndSelectModule.testData.get("mqPassword"));
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView();", agentIdValue);
		agentIdValue.sendKeys(LoginAndSelectModule.testData.get("agentId"));
		accessToken.clear();
		accessToken.sendKeys(LoginAndSelectModule.testData.get("accessToken"));
		getRepos.clear();
		getRepos.sendKeys(LoginAndSelectModule.testData.get("getRepos"));
		commitsBaseEndPoint.clear();
		commitsBaseEndPoint.sendKeys(LoginAndSelectModule.testData.get("commitsBaseEndPoint"));
		wait.until(ExpectedConditions.visibilityOf(addAgent));
		addAgent.click();
		Thread.sleep(2000);
		duplicateError.isDisplayed();
		ok.click();
		Thread.sleep(2000);
		cancelButton.click();
		Thread.sleep(2000);
		cancelAgentMessage.isDisplayed();
		yes.click();
		return verifyAgentId(LoginAndSelectModule.testData.get("agentId"));

	}

	public boolean verifyAgentId(String agentId) {

		boolean isInstanceId = false;
		for (WebElement agent : agentsList) {

			if (agent.getText().equals(agentId)) {
				isInstanceId = true;
			}
		}

		return isInstanceId;
	}

	public boolean startAgent() throws InterruptedException {
		selectAgent();
		stopAgent.click();
		ok.click();
		Thread.sleep(2000);
		Thread.sleep(2000);
		selectAgent();
		startAgent.click();
		ok.click();
		return landingPage.isDisplayed();
	}

	public boolean updateAgent() throws InterruptedException {
		searchToolName(LoginAndSelectModule.testData.get("toolName"));
		selectAgent();
		editAgent.click();
		dataLabelName.clear();
		dataLabelName.sendKeys(LoginAndSelectModule.testData.get("dataLabelName"));
		healthLabelName.clear();
		healthLabelName.sendKeys(LoginAndSelectModule.testData.get("healthLabelName"));
		update.click();
		success.isDisplayed();
		ok.click();
		return landingPage.isDisplayed();
	}

	public boolean deleteAgent() throws InterruptedException {
		selectAgent();
		stopAgent.click();
		ok.click();
		Thread.sleep(2000);
		Thread.sleep(2000);
		selectAgent();
		Thread.sleep(2000);
		Thread.sleep(2000);
		deleteAgent.click();
		wait.until(ExpectedConditions.visibilityOf(yes));
		Thread.sleep(2000);
		yes.click();
		Thread.sleep(2000);
		Thread.sleep(2000);
		return landingPage.isDisplayed();
	}

	private String selectOs(String osName) {
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		selectOs.click();
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		for (WebElement os : osList) {
			if ((os.getText()).equals(osName)) {
				os.click();
				break;
			}
		}
		return osName;

	}

	private String selectVersion(String version) throws InterruptedException {
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		Thread.sleep(2000);
		selectVersion.click();
		Thread.sleep(2000);
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		for (WebElement versionValue : versionList) {
			if ((versionValue.getText()).equals(version)) {
				versionValue.click();
				break;
			}
		}
		return version;

	}

	private String selectTools(String toolName) throws InterruptedException {
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.visibilityOf(selectTools));
		Thread.sleep(2000);
		selectTools.click();
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		for (WebElement agentName : toolsList) {
			if ((agentName.getText()).equals(toolName)) {
				agentName.click();
				break;
			}
		}
		return toolName;

	}

	private String searchToolName(String tools) throws InterruptedException {
		wait.until(ExpectedConditions.visibilityOf(selectToolName));
		selectToolName.click();
		Thread.sleep(2000);
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		for (WebElement toolName : toolNameList) {
			if ((toolName.getText()).equals(tools)) {
				toolName.click();
				break;
			}
		}
		return tools;

	}

	public void selectAgent() {
		for (int i = 0; i < agentsList.size(); i++) {
			if (agentsList.get(i).getText().equals(LoginAndSelectModule.testData.get("agentId"))) {
				List<WebElement> radioButtons = agentsList.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				driver.manage().timeouts().implicitlyWait(3600, TimeUnit.SECONDS);
				radioButtons.get(i).click();
				break;
			}
		}

	}
}
