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
package com.cognizant.devops.platformregressiontest.test.ui.workflow;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.ui.testdata.WorkflowDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.WorkflowTakDataModel;

public class WorkflowTaskConfiguration extends WorkflowTaskObjectRepository {

	private static final Logger log = LogManager.getLogger(WorkflowTaskConfiguration.class);

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	
	boolean isValid =false;

	public WorkflowTaskConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not by checking visibility of
	 * Workflow Task Management
	 * 
	 * 
	 * @return true if Workflow Task Management is displayed o/w false
	 */
	public boolean navigateToWorkFlowLandingPage() {
		if (visibilityOf(landingPage, 3)) {
			log.info("landingPage of workflow task management");
			return true;
		}
		return false;
	}

	public boolean saveWorkflow(WorkflowTakDataModel data) throws InterruptedException {
			clickOn(addWorkflow, 1);
			selectworkflowType(data.getWorkflowtype());
			mqChannel.sendKeys(data.getMqchannel());
			description.sendKeys(data.getDescription());
			dependency.sendKeys(data.getDependency());
			classDetail.sendKeys(data.getComponentname());
            Thread.sleep(2000);
            wait.until(ExpectedConditions.visibilityOf(save)).click();
			try {
				if (existTask.isDisplayed()) {
					clickOn(crossClose,3);
					clickOn(backButton, 1);
					log.debug("Skipping test case as task : {} already exists", data.getDescription());
					throw new SkipException(
							"Skipping test case as task : " + data.getDescription() + " already exists");
				}
			} catch (NoSuchElementException e) {
				log.info("Saving new workflowtask : {} exception : {}", data.getDescription(),
						e.getMessage());
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
				clickOn(yesBtn,5);
				crossClose.click();
				return true;
			}
			return true;
		} 	

	/**
	 * This method handles Workflowtype selection.
	 * 
	 * @param type
	 * @throws InterruptedException
	 */
	public String selectworkflowType(String type) throws InterruptedException {
		visibilityOf(workflowType, 2);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		clickOn(workflowType, 2);
		visibilityOfAllElements(typeList, 2);
		for (WebElement typeList : typeList) {
			if ((typeList.getText()).equals(type)) {
				visibilityOf(typeList, 2);
				clickOn(typeList, 4);
				break;
			}
		}
		return type;
	}

	/**
	 * This method handles workflow deletion.
	 * 
	 * @param workflowTakDataModel
	 * @return
	 * @throws InterruptedException
	 */
	public boolean deleteWorkflow(WorkflowTakDataModel data) throws InterruptedException {
		selectWorkFlowTask(data.getDescription());
		clickOn(trashWorkflow, 2);
		visibilityOf(yesBtn, 1);
		clickOn(yesBtn, 1);
		try {
			wait.until(ExpectedConditions.visibilityOf(workflowDelete));
			if (workflowDelete.isDisplayed()) {
				wait.until(ExpectedConditions.elementToBeClickable(crossClose));
				crossClose.click();
				log.info("workflow {} deleted successfully ", data.getDescription());
				return true;
			}
		} catch (Exception e) {
			log.error(" unable to workflow kpiId {}  with exception {} :", data.getDescription(), e.getMessage());
			throw new SkipException("Deleting is disabled");
		}
		crossClose.click();
		return false;
	}
    
	public boolean validateMandatoryFields(WorkflowTakDataModel data) throws InterruptedException {
		try {
			selectWorkFlowTask(data.getDescription());
			clickOn(editWorkflow, 1);
			description.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			Thread.sleep(500);
			wait.until(ExpectedConditions.visibilityOf(save)).click();
			if(visibilityOf(invalidMsg, 2))
			clickOn(crossClose,2);
			clickOn(backButton,2);
			this.isValid=true;
			return this.isValid;
		} catch (Exception e) {
			log.info("Something went wrong while saving task : {} exception : {}", data.getDescription(),
					e.getMessage());
			return false;
		}
	}

	public boolean updateWorkflow(WorkflowTakDataModel data) throws InterruptedException {
		try {
			selectWorkFlowTask(data.getDescription());
			clickOn(editWorkflow, 1);
			description.sendKeys(data.getDescription());
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			wait.until(ExpectedConditions.visibilityOf(save)).click();
			clickOn(yesBtn, 2);
			success.isDisplayed();
			crossClose.click();
			return true;
		} catch (Exception e) {
			log.info("Something went wrong while saving task : {} exception : {}", data.getDescription(),
					e.getMessage());
			return false;
		}
	}

	/**
	 * This method handles workFlow Description
	 * 
	 * @param desc
	 * @throws InterruptedException
	 */
	public void selectWorkFlowTask(String desc) throws InterruptedException {
		for (int i = 0; i < workflowList.size(); i++) {
			if (workflowList.get(i).getText().equals(desc)) {
				List<WebElement> radioButtons = workflowList.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				Thread.sleep(10000);
				radioButtons.get(i).click();
				break;
			}
		}
	}

	/**
	 * wait until the visibility of list of web elements
	 * 
	 * @param element
	 * @return size of list of web elements
	 */
	public static int visibilityOfAllElements(List<WebElement> element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout))
				.until(ExpectedConditions.visibilityOfAllElements(element));
		return element.size();
	}

	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 */
	public static boolean visibilityOf(WebElement element, int timeout) {
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
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.elementToBeClickable(element));
		element.click();
	}

}
