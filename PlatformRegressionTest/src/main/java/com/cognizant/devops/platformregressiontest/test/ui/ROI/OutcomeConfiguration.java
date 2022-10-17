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
package com.cognizant.devops.platformregressiontest.test.ui.ROI;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.OutcomeConfigDataModel;

public class OutcomeConfiguration extends OutcomeObjectRepository {
	
	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	private static final Logger log = LogManager.getLogger(OutcomeConfiguration.class);
	
	public OutcomeConfiguration() {
		PageFactory.initElements(driver, this);
	}
	
	/**
	 * checks whether landing page is displayed or not by checking visibility of
	 * Outcome Configuration heading
	 * 
	 *
	 */

	public boolean navigateToOutcomeConfig() {
		try {
			if (landingPage.isDisplayed())
				return true;
		} catch (Exception e) {
			throw new SkipException("Something went wrong while navigating to landing page");
		}
		return false;
	}
	
	public boolean configureOutcomeWithoutReqParams(OutcomeConfigDataModel data) {
		
		try {
			visibilityOf(clickAddButton, 2);
			clickOn(clickAddButton, 2);
			visibilityOf(mainHeader, 2);
			visibilityOf(outcomeName, 2);
			outcomeName.sendKeys(data.getOutcomeName());
			visibilityOf(outcomeType, 2);
			outcomeType.sendKeys(data.getOutcomeType());
			visibilityOf(toolName, 2);
			toolName.sendKeys(data.getToolName());
			visibilityOf(metricUrl, 2);
			metricUrl.sendKeys(data.getMetricUrl());
			Thread.sleep(2000);
			clickOn(save,2);
			visibilityOf(saveOutcome, 2);
			clickOn(yes,2);
			if (visibilityOf(successMessage, 2)) {
			clickOn(crossClose, 2);
			return true;
			}
			return false;
		}
		catch (Exception ex) {
			if(visibilityOf(duplicateOutcome,2)) {
				clickOn(crossClose, 2);
				clickOn(backButton,2);
				throw new SkipException("Outcome already available. Please try with different outcome name.");
			}
		return false;
		}	
	}
	
	public boolean validateDuplicateOutcome(OutcomeConfigDataModel data) {
		try {
			visibilityOf(clickAddButton, 2);
			clickOn(clickAddButton, 2);
			visibilityOf(mainHeader, 2);
			visibilityOf(outcomeName, 2);
			outcomeName.sendKeys(data.getOutcomeName());
			visibilityOf(outcomeType, 2);
			outcomeType.sendKeys(data.getOutcomeType());
			visibilityOf(toolName, 2);
			toolName.sendKeys(data.getToolName());
			visibilityOf(metricUrl, 2);
			metricUrl.sendKeys(data.getMetricUrl());
			Thread.sleep(2000);
     		clickOn(save,2);
			visibilityOf(saveOutcome, 2);
			clickOn(yes,2);
			visibilityOf(duplicateOutcome,2);
			clickOn(crossClose, 2);
			clickOn(backButton,2);
			return true;
		}
		catch(Exception ex) {
			log.info("Outcome validation unsuccessful");
		}
		return false;	
	}
	
	public boolean configureOutcomeWithReqParams(OutcomeConfigDataModel data) {
		try {
			visibilityOf(clickAddButton, 2);
			clickOn(clickAddButton, 2);
			visibilityOf(mainHeader, 2);
			visibilityOf(outcomeName, 2);
			outcomeName.sendKeys(data.getOutcomeName());
			visibilityOf(outcomeType, 2);
			outcomeType.sendKeys(data.getOutcomeType());
			visibilityOf(toolName, 2);
			toolName.sendKeys(data.getToolName());
			visibilityOf(metricUrl, 2);
			metricUrl.sendKeys(data.getMetricUrl());
			clickOn(clickAddButton, 2); 
			visibilityOf(nameRequestParam, 2);
			nameRequestParam.sendKeys(data.getReqParamName());
			visibilityOf(valueRequestParam, 2);
			valueRequestParam.sendKeys(data.getReqParamValue());
			Thread.sleep(3000);
			clickOn(save,2);
			visibilityOf(saveOutcome, 2);
			clickOn(yes,2);
			if (visibilityOf(successMessage, 2)) {
			clickOn(crossClose, 2);
			return true;
			}
			return false;
		}
		catch (Exception ex) {
			if(visibilityOf(duplicateOutcome,2)) {
				clickOn(crossClose, 2);
				clickOn(backButton,2);
				throw new SkipException("Something went wrong while creating outcome for" + ex.getMessage());
			}
		return false;
		}	
	}
	
	public boolean editOutcome(OutcomeConfigDataModel data) {
		try {
			selectOutcome(data.getOutcomeName());
			clickOn(clickEditButton,2);
			visibilityOf(metricUrl, 2);
			metricUrl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			metricUrl.sendKeys(data.getMetricUrl());
			Thread.sleep(1000);
			clickOn(clickAddButton, 2); 
			visibilityOf(nameRequestParam, 2);
			nameRequestParam.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			nameRequestParam.sendKeys(data.getReqParamName());
			visibilityOf(valueRequestParam, 2);
			valueRequestParam.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			valueRequestParam.sendKeys(data.getReqParamValue());
			Thread.sleep(2000);
			clickOn(save,2);
			clickOn(yes,2);
			visibilityOf(successEdit, 2);
			visibilityOf(crossClose, 2);
			clickOn(crossClose, 2);
			return true;	
		}
		catch(Exception ex) {
			log.info("Something went wrong in editing the Outcome");
			return false;
		}
	}
	
	public boolean checkRefresh(OutcomeConfigDataModel data) {
		try {
			log.info("refresh functionality successful");
			selectOutcome(data.getOutcomeName());
			clickOn(checkRefresh, 2);
			if (landingPage.isDisplayed()) {
				log.info("navigate to landing page successful");
				return true;
			}
		}
		catch(Exception ex) {
			log.info("Error in the Refresh button functionality");
		}
		return false;
	}
	
	public boolean deleteOutcome(OutcomeConfigDataModel data) {
		try {
			selectOutcome(data.getOutcomeName());
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			clickOn(clickDelete,2);
			clickOn(yes,2);
			visibilityOf(successDelete, 2);
			visibilityOf(crossClose, 2);
			clickOn(crossClose, 2);
			if (!selectOutcome(data.getOutcomeName())) {
				log.info("Outcome deleted successfully");
				return true;
			}
		}
		catch(Exception ex) {
			log.info("Failed to delete the outcome");
		}
		return false;
	}
	
	public boolean checkStatus(OutcomeConfigDataModel data) {
		try {
		selectOutcome(data.getOutcomeName());
		driver.findElement(
				By.xpath("//tr/td[contains(text(), '" + data.getOutcomeName() + "')]//following-sibling::td[3]")).click();
		visibilityOf(updateStatus,2);
		clickOn(yes,2);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		visibilityOf(successEdit, 2);
		visibilityOf(crossClose, 2);
		clickOn(crossClose, 2);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		log.info("Status check Successful");
		selectOutcome(data.getOutcomeName());
		driver.findElement(
				By.xpath("//tr/td[contains(text(), '" + data.getOutcomeName() + "')]//following-sibling::td[3]")).click();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		visibilityOf(updateStatus,2);
		clickOn(yes,2);
		visibilityOf(crossClose, 2);
		clickOn(crossClose, 2);
		return true;
		}
		catch(Exception ex) {
			log.info("Status check unsuccessful");
		}
		return false;
	}
	
	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 */
	private boolean visibilityOf(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element));
		return element.isDisplayed();
	}
	
	/**
	 * select report with the given title
	 * 
	 * @param reportTitle
	 * @return
	 */
	public boolean selectOutcome(String outcomeName) {
		
		String pageString = getTotalPages.getText().toString().substring(3);
		int ch = 0;
		
		boolean outcomeAvailable = false;
		try {
			wait.until(ExpectedConditions.visibilityOfAllElements(outcomeList));
			for (int j = 0; j < Integer.parseInt(pageString);j++) {
			for (int i = 0; i < outcomeList.size(); i++) {
				if (outcomeList.get(i).getText().equals(outcomeName)) {
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
					outcomeAvailable = true;
					List<WebElement> radioButton = outcomeList.get(i)
							.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
					radioButton.get(i).click();
					ch = 1;
					break;
				}
			}
			if (ch == 1)
				break;
			
			nextPage.click();
			}
		} catch (Exception e) {
			log.info("Outcome list is empty!!");
		}
		return outcomeAvailable;
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
	
}
