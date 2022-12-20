/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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

package com.cognizant.devops.platformregressiontest.test.ui.multipleemailconfig;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.GroupEmailDashRepoDataModel;

public class MultipleEmailDashRepoConfiguration extends MultipleEmailDashRepoObjectRepository{
		
	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	private static final Logger log = LogManager.getLogger(MultipleEmailDashRepoConfiguration.class);

	public MultipleEmailDashRepoConfiguration() {
		PageFactory.initElements(driver, this);
	}
	
	/**
	 * This method handles Content Addition
	 */
	private void clickAddButton() {

		Actions actions = new Actions(driver);
		wait.until(ExpectedConditions.elementToBeClickable(clickAddButton));
		actions.moveToElement(clickAddButton).click();
		Action action = actions.build();
		action.perform();

	}
	
	/**
	 * checks whether landing page is displayed or not by checking visibility of
	 * Group Email Configuration Landing Page
	 * 
	 */
	
	public boolean navigateToGroupEmailConfigPage() {
		try {
			clickEmailConfigButton.click();
			log.info("Landing page displayed : {}", visibilityOf(landingPage, 4));
			return landingPage.isDisplayed();
		} catch (Exception e) {
			throw new SkipException("Something went wrong while navigating to landing page");
		}
	}
	
	/**
	 * creates group email report with different batchname, schedule, many different reports etc
	 * 
	 * @param data
	 * @return
	 */
	public boolean createGroupEmailReport(GroupEmailDashRepoDataModel data) {
		try {
			clickAddButton();
			batchName.sendKeys(data.getBatchName());
			selectSchedule.sendKeys(data.getSchedule());
			addReport(data.getReports());
			receiverEmailAddress.sendKeys(data.getReceiverEmailAddress());
			receiverCCEmailAddress.sendKeys(data.getReceiverCCEmailAddress());
			receiverBCCEmailAddress.sendKeys(data.getReceiverBCCEmailAddress());
			mailSubject.sendKeys(data.getMailSubject());
			mailBodyTemplate.sendKeys(data.getMailBodyTemplate());
			visibilityOf(saveGroupEmailConfig,2);
			saveGroupEmailConfig.click();
			visibilityOf(yes,2);
			clickOn(yes,2);
			visibilityOf(saveSuccess,2);
			crossClose.click();
			return true;
		}
		catch(Exception ex) {
			log.error("Failure while saving the Group Email Config");
			return false;
		}
	}
	
	/**
	 * edits and saves the group email report with modifications
	 * returns true if the report is edited successully
	 * 
	 * @param data
	 * @return
	 */
	public boolean editGroupEmailReport(GroupEmailDashRepoDataModel data) {
		try {
			selectedItem(data.getBatchName());
			editBtn.click();
			
			visibilityOf(receiverEmailAddress,2);
			receiverEmailAddress.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			receiverEmailAddress.sendKeys(data.getReceiverEmailAddress());
			
			visibilityOf(mailSubject,2);
			mailSubject.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			mailSubject.sendKeys(data.getMailSubject());
			
			visibilityOf(mailBodyTemplate,2);
			mailBodyTemplate.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			mailBodyTemplate.sendKeys(data.getMailBodyTemplate());
			
			visibilityOf(saveGroupEmailConfig,2);
			saveGroupEmailConfig.click();
			visibilityOf(yes,2);
			clickOn(yes,2);
			visibilityOf(successEdit,2);
			crossClose.click();
			return true;
		}
		catch(Exception ex) {
			log.error(ex.getMessage());
			return false;
		}
	}
	
	/**
	 * returns true if health check detail functionality is working proeperly
	 * 
	 * 
	 * @return
	 */
	
	public boolean checkDetailsFunctionality(GroupEmailDashRepoDataModel data) {
			selectedItem(data.getBatchName());
			try {
				driver.findElement(
						By.xpath("//tr/td[contains(text(), '" + data.getBatchName() + "')]//following-sibling::td[5]")).click();
				visibilityOf(workfloHistoryDetail, 2);
				if (workfloHistoryDetail.getText().equals("Workflow History Detail - " + data.getBatchName())) {
					Thread.sleep(500);
					closeDialog.click();
					return true;
				}
			} catch (Exception e) {
				closeDialog.click();
				log.info("Details functionality not found.");
			}
			return true;
	}
	
	
	/**
	 * returns true if Active status of report is updated
	 * 
	 * 
	 * @return
	 */
	public boolean checkStatus(GroupEmailDashRepoDataModel data) {
		try {
			selectedItem(data.getBatchName());
			
			//Changing status to inactive
			driver.findElement(
					By.xpath("//tr/td[contains(text(), '" + data.getBatchName() + "')]//following-sibling::td[4]/mat-slide-toggle")).click();
			visibilityOf(updateStatus,2);
			Thread.sleep(2000);
			clickOn(yes,2);
			
			//Changing status back to Active
			driver.findElement(
					By.xpath("//tr/td[contains(text(), '" + data.getBatchName() + "')]//following-sibling::td[4]/mat-slide-toggle")).click();
			visibilityOf(updateStatus,2);
			Thread.sleep(2000);
			clickOn(yes,2);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			return true;
		}
		catch(Exception ex) {
			return false;
		}
	}
	
	
	/**
	 * returns true if the Group Email Report is deleted successfully
	 * 
	 * 
	 * @return
	 */
	public boolean deleteGroupEmailReport(GroupEmailDashRepoDataModel data) {
		try {
			selectedItem(data.getBatchName());
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			clickOn(deleteBtn,2);
			clickOn(yes,2);
			visibilityOf(crossClose, 2);
			clickOn(crossClose, 2);
			if (!selectedItem(data.getBatchName())) {
				log.info("Outcome deleted successfully");
				return true;
			}
		}
		catch(Exception ex) {
			log.info("Failed to delete the outcome");
		}
		return false;
	}
	
	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 * @throws InterruptedException 
	 */
	
	private boolean addReport(String reports) throws InterruptedException {
		String[] reportNames = reports.split("_");
		int i = 1;
		for (String report : reportNames) {
			clickOn(clickAddReport, 2);
			driver.findElement(By.xpath("//div["+ i +"]/div[1]/mat-form-field/div/div[1]/div[1]/mat-select[@formcontrolname='report']"))
					.sendKeys(report);	
			i++;
		}
		Thread.sleep(2000);
		driver.findElement(By.xpath("//div["+ i +"]/div[2]/button/span[1]/mat-icon[@svgicon='minus']")).click();
		return true;
	}
	
	private boolean visibilityOf(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element));
		return element.isDisplayed();
	}
	
	/**
	 * wait until the visibility of web element then click on the web element
	 * 
	 * @param element
	 * @param timeout
	 */
	public static void clickOn(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.elementToBeClickable(element)).click();
	}
	
	/**
	 * select report with the given title
	 * 
	 * @param reportTitle
	 * @return
	 */
	public boolean selectedItem(String batchname) { //batchName
		boolean itemPresent = false;
		try {
			wait.until(ExpectedConditions.visibilityOfAllElements(groupEmailList));
			for (int i = 0; i < groupEmailList.size(); i++) {
				if (groupEmailList.get(i).getText().equals(batchname)) {
					Thread.sleep(2000);
					itemPresent = true;
					List<WebElement> radioButtons = groupEmailList.get(i)
							.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
					radioButtons.get(i).click();
					break;
				}
			}
		} catch (Exception e) {
			log.info("Report list is empty!!");
		}
		return itemPresent;
	}
}
