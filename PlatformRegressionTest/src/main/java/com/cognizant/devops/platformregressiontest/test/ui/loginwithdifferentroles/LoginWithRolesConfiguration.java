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
package com.cognizant.devops.platformregressiontest.test.ui.loginwithdifferentroles;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the logic for login with different roles test cases
 *
 */
public class LoginWithRolesConfiguration extends LoginWithRolesObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	private static final Logger log = LogManager.getLogger(LoginWithRolesConfiguration.class);

	public LoginWithRolesConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * Logs out of the application and logs in using created user credentials
	 * 
	 * @return true if login is successful o/w false
	 * @throws InterruptedException
	 */
	public boolean loginWithCreatedUserID() {
		boolean loggedIn = false;
		logout.click();
		wait.until(ExpectedConditions.visibilityOf(loginUserName));
		loginUserName.sendKeys(LoginAndSelectModule.testData.get("name"));
		loginPassword.sendKeys(LoginAndSelectModule.testData.get("password"));
		wait.until(ExpectedConditions.elementToBeClickable(logonButton));
		logonButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(landingPage));
		try {
			if (landingPage.isDisplayed()) {
				log.info("login successful");
				loggedIn = true;
			}
		} catch (Exception e) {
			log.info("login unsuccessful");
			return false;
		}
		return loggedIn;
	}

	/**
	 * Checks if admin role's menu are displayed correct
	 * 
	 * @return true if admin role's menu are displayed successfully o/w false
	 * @throws InterruptedException
	 */
	public boolean checkAdminrole() throws InterruptedException {
		Thread.sleep(1000);
		wait.until(ExpectedConditions.elementToBeClickable(dashboardGroups));
		dashboardGroups.click();
		if (clickDashboard("Main Org.")) {
			wait.until(ExpectedConditions.visibilityOf(roleinWelcome));
			Thread.sleep(2000);
			if ((roleinWelcome.getText()).contains("Role: Admin") && checkAdminMenu() && checkadmindashboardAccess()) {
				return true;
			} else {
				log.debug("User does not have admin access");
				return false;
			}
		} else {
			log.debug("Access group does not exists");
			throw new SkipException("Skipping test case as admin access group does not exists");
		}
	}

	/**
	 * Checks whether the admin role's menu are displayed correctly
	 * 
	 * @return true if displayed successfully o/w false
	 */
	private boolean checkAdminMenu() {
		try {
			if (dashboardGroups.isDisplayed() && auditReporting.isDisplayed() && playlist.isDisplayed()
					&& reportManagement.isDisplayed() && dataDictionary.isDisplayed() && healthCheck.isDisplayed()
					&& dashboardReport.isDisplayed() && configuration.isDisplayed()) {
				log.info("All the admin's side menu are displayed");
				return true;
			}
		} catch (Exception e) {
			log.info("All the admin's side menu are not displayed {}", e);
			return false;
		}
		return false;
	}

	/**
	 * Checks whether admin has permission to create, edit and view grafana panel
	 * 
	 * @return true if all permissions are displayed o/w false
	 * @throws InterruptedException
	 */
	private boolean checkadmindashboardAccess() {
		listView.click();
		wait.until(ExpectedConditions.elementToBeClickable(generaldashboard));
		generaldashboard.click();
		wait.until(ExpectedConditions.elementToBeClickable(lokiDashboard));
		lokiDashboard.click();
		driver.switchTo().frame(driver.findElement(By.xpath(iframePath)));
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.switchTo().frame(0);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		panelTitle.click();
		try {
			if (exploreMenu.isDisplayed() && editMenu.isDisplayed() && viewMenu.isDisplayed()) {
				log.info("admin has create, edit and view access");
				driver.switchTo().defaultContent();
				return true;
			}
		} catch (Exception e) {
			log.info("All the admin access are not displayed {}", e);
			driver.switchTo().defaultContent();
			return false;
		}
		driver.switchTo().defaultContent();
		return false;
	}

	/**
	 * Checks if editor role's menu are displayed correct
	 * 
	 * @return true if editor role's menu are displayed successfully o/w false
	 * @throws InterruptedException
	 */
	public boolean checkEditorRole() throws InterruptedException {
		if (clickDashboard(LoginAndSelectModule.testData.get("accessGroup1"))) {
			wait.until(ExpectedConditions.visibilityOf(roleinWelcome));
			Thread.sleep(2000);
			if ((roleinWelcome.getText()).contains("Role: Editor") && editorAndviewermenu()
					&& checkeditordashboardAccess()) {
				return true;
			}
		} else {
			log.debug("Editor Access group does not exists");
			throw new SkipException("Skipping test case as Editor access group does not exists");
		}
		log.debug("something went wrong");
		return false;
	}

	/**
	 * Checks whether editor has permission to edit and view only and not create
	 * permission in grafana panel
	 * 
	 * @return true if all permissions are displayed o/w false
	 * @throws InterruptedException
	 */
	private boolean checkeditordashboardAccess() {
		clickHere.click();
		driver.switchTo().frame(driver.findElement(By.xpath(iframePath)));
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.switchTo().frame(driver.findElement(By.xpath("//iframe[contains(@id,'iSightIframe')]")));
		wait.until(ExpectedConditions.elementToBeClickable(dashboards));
		dashboards.click();
		try {
			if (driver.findElements(By.xpath(explorePath)).isEmpty()) {
				log.info("explore or create option is not available for editor");
				if (editMenu.isDisplayed() && viewMenu.isDisplayed()) {
					log.info("editor has only edit and view access");
					driver.switchTo().defaultContent();
					return true;
				}
			}
		} catch (Exception e) {
			log.info("All the editor access are not displayed {}", e);
			driver.switchTo().defaultContent();
			return false;
		}
		return false;
	}

	/**
	 * Checks if viewer role's menu are displayed correct
	 * 
	 * @return true if viewer role's menu are displayed successfully o/w false
	 * @throws InterruptedException
	 */
	public boolean checkViewerRole() throws InterruptedException {
		if (clickDashboard(LoginAndSelectModule.testData.get("accessGroup2"))) {
			wait.until(ExpectedConditions.visibilityOf(roleinWelcome));
			Thread.sleep(2000);
			if ((roleinWelcome.getText()).contains("Role: Viewer") && editorAndviewermenu()
					&& checkviewerdashboardAccess()) {
				return true;
			}
		} else {
			log.debug("Access group does not exists");
			throw new SkipException("Skipping test case as admin access group does not exists");
		}
		log.debug("unexpected error");
		return false;
	}

	/**
	 * Checks whether viewer has permission to view only and not has permission to
	 * create and edit in grafana panel
	 * 
	 * @return true if all permissions are displayed o/w false
	 * @throws InterruptedException
	 */
	private boolean checkviewerdashboardAccess() throws InterruptedException {
		Thread.sleep(500);
		clickHere.click();
		driver.switchTo().frame(driver.findElement(By.xpath(iframePath)));
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.switchTo().frame(driver.findElement(By.xpath("//iframe[contains(@id,'iSightIframe')]")));
		wait.until(ExpectedConditions.elementToBeClickable(dashboards));
		dashboards.click();
		try {
			if (driver.findElements(By.xpath(explorePath)).isEmpty()
					&& driver.findElements(By.xpath(editPath)).isEmpty()) {
				log.info("Edit and explore options are not available for viewer");
				if (viewMenu.isDisplayed()) {
					log.info("viewer has only view access");
					driver.switchTo().defaultContent();
					return true;
				}
			}
		} catch (Exception e) {
			log.info("All the viewer access are not displayed {}", e);
			driver.switchTo().defaultContent();
			return false;
		}
		return false;
	}

	/**
	 * Clicks on a particular dashboard from a list of dashboards available
	 * 
	 * @param accessGroupname
	 * @return true if clicking dashboard is successful o/w false
	 * @throws InterruptedException
	 */
	private boolean clickDashboard(String accessGroupname) throws InterruptedException {
		Thread.sleep(500);
		for (WebElement group : accessGroups) {
			if ((group.getText()).equals(accessGroupname)) {
				group.click();
				log.info("{} dashboard is clicked", accessGroupname);
				return true;
			}
		}
		log.info("{} dashboard is not displayed on the dashboard groups", accessGroupname);
		return false;
	}

	/**
	 * Checks whether the editor/viewer role's side menu are displayed correctly
	 * 
	 * @return true if displayed successfully o/w false
	 */
	private boolean editorAndviewermenu() {
		wait.until(ExpectedConditions.elementToBeClickable(dashboardGroups));
		try {
			if (dashboardGroups.isDisplayed() && playlist.isDisplayed() && reportManagement.isDisplayed()
					&& dataDictionary.isDisplayed()) {
				log.info("All the Editor/Viewer side menu are displayed");
				return true;
			}
		} catch (Exception e) {
			log.info("All the Editor/Viewer side menu are not displayed {}", e);
			return false;
		}
		return false;
	}

	/**
	 * logs out and logs in as default user
	 * 
	 * @return true if login is successful and landing page is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean loginasdefaultuser() {
		getData(ConfigOptionsTest.LOGIN_DIR + File.separator + ConfigOptionsTest.LOGIN_JSON_FILE);
		logout.click();
		wait.until(ExpectedConditions.visibilityOf(loginUserName));
		loginUserName.sendKeys(LoginAndSelectModule.testData.get("username"));
		loginPassword.sendKeys(LoginAndSelectModule.testData.get("password"));
		wait.until(ExpectedConditions.elementToBeClickable(logonButton));
		logonButton.click();
		log.info("admin login success");
		wait.until(ExpectedConditions.visibilityOf(landingPage));
		return landingPage.isDisplayed();
	}

	public boolean deleteUserFromAllAccessGroup() throws InterruptedException {
		getData(ConfigOptionsTest.GROUPS_AND_USERS_DIR + File.separator + ConfigOptionsTest.GROUP_JSON_FILE);
		deleteUser("Main Org.");
		Thread.sleep(1000);
		deleteUser(LoginAndSelectModule.testData.get("accessGroup1"));
		Thread.sleep(1000);
		deleteUser(LoginAndSelectModule.testData.get("accessGroup2"));
		if(count==0) {
			return true;
		}else {
		log.debug("user does not exist to be deleted");
		throw new SkipException("Skipping test case as user does not exist");
		}
	}
	
	/**
	 * Deletes the user from DB & checks whether deletion is successful
	 * 
	 * @return true if deletion is successful o/w false
	 * @throws InterruptedException
	 */
	public void deleteUser(String accessGroupName) {
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		verifyAccessGroupName(accessGroupName);
		if (searchLoginID(LoginAndSelectModule.testData.get("userName"))) {
			WebElement rws = userDetailsTable.findElement(By.tagName("tr"));
			List<WebElement> cols = rws.findElements(By.tagName("td"));
			if ((cols.get(1).getText()).equals(LoginAndSelectModule.testData.get("name"))) {
				cols.get(0).findElement(By.xpath("//span[@class='mat-radio-container']")).click();
				wait.until(ExpectedConditions.elementToBeClickable(deleteButton));
				deleteButton.click();
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			}
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			okButton.click();
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		} else {
			count++;
		}
	}
	
	/**
	 * Checks whether the access group name is present in the UI or not
	 * 
	 * @param accessName
	 * @return true if access group name present in list of access group in database
	 *         list else false
	 */
	public boolean verifyAccessGroupName(String accessName) {
		if (accessName != null) {
			accessGroup.click();
			for (WebElement access : accessGroupList) {
				if (access.getText().equals(accessName)) {
					log.info("{} Access Group is clicked successfully.", accessName);
					wait.until(ExpectedConditions.elementToBeClickable(access));
					access.click();
					return true;
				}
			}
			accessGroup.sendKeys(Keys.ESCAPE);
		}
		return false;
	}
	
	/**
	 * Searches the user name in User details table
	 * 
	 * @param userName
	 * @return true if user is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean searchLoginID(String userName) {
		searchBoxInLandingPage.clear();
		searchBoxInLandingPage.sendKeys(userName);
		try {
			if (userDetailsTable.isDisplayed()) {
				log.info("{} is present in the User Table.", userName);
				return true;
			}
		} catch (Exception e) {
			log.info("{} is not present in the User Table.", userName);
			return false;
		}
		return false;
	}
}
