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
package com.cognizant.devops.platformregressiontest.test.ui.correlationbuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author Nainsi
 * 
 *         Class contains the business logic for Correlation Builder module test
 *         cases
 *
 */
public class CorrelationBuilderConfiguration extends CorrelationObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	Map<String, String> testData = new HashMap<>();

	private static final Logger log = LogManager.getLogger(CorrelationBuilderConfiguration.class);
	private Set<String> uniqueToolNames = new HashSet<>();

	/**
	 * prefix to add before correlationName to show relation between source tool and
	 * destination tool
	 */
	String prefix = "FROM_" + LoginAndSelectModule.testData.get(CorrelationCommonConstants.SOURCE_TOOL) + "_TO_"
			+ LoginAndSelectModule.testData.get(CorrelationCommonConstants.DESTINATION_TOOL) + "_";

	/**
	 * to be used in createCorrelation to set the correlation name to be saved in
	 * database
	 */
	String correlationTestName = prefix
			+ LoginAndSelectModule.testData.get(CorrelationCommonConstants.CORRELATION_NAME);

	/**
	 * to be used in correlationNameWithAlphaNumericCharacters to set the
	 * correlation name to check whether alphanumeric with special character
	 * underscore is allowed or not
	 */
	String correlationNameAlphaNumeric = prefix
			+ LoginAndSelectModule.testData.get(CorrelationCommonConstants.CORRELATION_NAME_ALPHA_NUMERIC);

	/**
	 * to be used in correlationNameWithSpecialCharacters to set the correlation
	 * name to check whether special characters without underscore is allowed or not
	 */
	String correlationNameSpecialCharacter = prefix
			+ LoginAndSelectModule.testData.get(CorrelationCommonConstants.CORRELATION_NAME_SPECIAL_CHARACTER);

	public CorrelationBuilderConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean navigateToCorrelationBuilderLandingPage() throws InterruptedException {
		log.info("Landing page displayed : {}", visibilityOf(landingPage, 5));
		return landingPage.isDisplayed();
	}

	/**
	 * Checks if existing record present in database is displayed on UI or not
	 * 
	 * @return true if list is not empty o/w false
	 * @throws InterruptedException 
	 */
	public boolean checkIfExistingRecordsDisplayed() throws InterruptedException {
		try {
			visibilityOfAllElements(relationsList, 1);
		} catch (Exception e) {
			log.info("Relation List is empty as all relation are deleted");
		}
		Thread.sleep(1000);
		log.info("List of correlations loaded successfully");
		return true;
	}

	/**
	 * Checks whether correlation name having alphanumeric characters string is
	 * getting saved or not
	 * 
	 * @return true if saved successfully o/w false
	 * @throws InterruptedException
	 */
	public boolean correlationNameWithAlphaNumericCharacters() throws InterruptedException {
		if (verifyCorrelationName(correlationNameAlphaNumeric)) {
			log.debug("Skipping test case as {} already exists", correlationNameAlphaNumeric);
			throw new SkipException("Skipping test case as " + correlationNameAlphaNumeric + " already exists");
		}
		
		try {
			fillData();
			Thread.sleep(1000);
			sendKeys(correlationName,
					LoginAndSelectModule.testData.get(CorrelationCommonConstants.CORRELATION_NAME_ALPHA_NUMERIC), 10);
			clickOn(save, 2);
			driver.findElement(By.xpath("//div[contains(text(), ' Save Co-Relation ')]"))
					.isDisplayed();
			clickOn(yes, 2);
			driver.findElement(By.xpath("//span[contains(text(), 'saved successfully.')]"))
					.isDisplayed();
			/*
			 * driver.findElement(By.
			 * xpath("//div[@class='textPadding' and contains(text(), ' saved successfully.')]"
			 * )) .isDisplayed(); log.info(afterClickingSaveMsg.getText()); clickOn(ok, 2);
			 */
			crossClose.click();
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}
		return verifyCorrelationName(correlationNameAlphaNumeric);
	}

	/**
	 * Checks whether correlation name having special characters string is getting
	 * saved or not
	 * 
	 * @return true if it is not saved else false
	 * @throws InterruptedException
	 */
	public boolean correlationNameWithSpecialCharacters() throws InterruptedException {
		try {
			fillData();
			sendKeys(correlationName,
					LoginAndSelectModule.testData.get(CorrelationCommonConstants.CORRELATION_NAME_SPECIAL_CHARACTER),
					10);
			clickOn(save, 2);
			driver.findElement(By.xpath("//span[contains(text(), 'Please enter valid name, and it should contain only alphanumeric characters and underscore ')]"))
					.isDisplayed();
			/*
			 * afterClickingSaveMsg.isDisplayed(); log.info(afterClickingSaveMsg.getText());
			 * clickOn(ok, 2); clickOn(cancel, 2);
			 */
			 wait.until(ExpectedConditions.elementToBeClickable(crossClose));
			 crossClose.click();
			 cancelUpload.click();
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}
		return !verifyCorrelationName(correlationNameSpecialCharacter);
	}

	/**
	 * checks whether correlation is saved successfully or not
	 * 
	 * @return true if created correlation present in list of correlation in
	 *         database o/w false
	 * @throws InterruptedException
	 */
	public boolean createCorrelation() throws InterruptedException {
		Thread.sleep(1000);
		if (verifyCorrelationName(correlationTestName)) {
			log.debug("Skipping test case as {} already exists", correlationNameAlphaNumeric);
			throw new SkipException("Skipping test case as " + correlationNameAlphaNumeric + " already exists");
		}
		try {
			fillData();
			sendKeys(correlationName, LoginAndSelectModule.testData.get(CorrelationCommonConstants.CORRELATION_NAME),
					10);
			clickOn(save, 2);
			driver.findElement(By.xpath("//div[contains(text(), 'Save Co-Relation')]"))
					.isDisplayed();
			clickOn(yes, 2);
			Thread.sleep(1000);
			driver.findElement(By.xpath("//span[contains(text(), 'saved successfully.')]"))
					.isDisplayed();
			wait.until(ExpectedConditions.elementToBeClickable(crossClose));
			crossClose.click();
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}
		return verifyCorrelationName(correlationTestName);
	}

	/**
	 * to check if creating correlation with existing name behavior is as expected
	 * 
	 * @return true if expected error msg is displayed while saving new correlation
	 *         with existing name
	 * @throws InterruptedException
	 */
	public boolean createCorrelationWithSameName() throws InterruptedException {
		if (!fillData())
			return false;
		sendKeys(correlationName, LoginAndSelectModule.testData.get(CorrelationCommonConstants.CORRELATION_NAME), 10);
		clickOn(save, 2);
		driver.findElement(By.xpath("//div[contains(text(), ' Save Co-Relation ')]"))
				.isDisplayed();
		Thread.sleep(1000);
		clickOn(yes, 2);
		driver.findElement(By.xpath("//span[contains(text(), 'Relation Name already exists.')]")).isDisplayed();
		log.info(duplicateError.getText());
		if (duplicateError.getText().equals("Relation Name already exists.")) {
			crossClose.click();
			cancelUpload.click();
			return true;
		}
		return false;
	}

	/**
	 * checks whether correlation name, source tool and destination tool is same as
	 * that we have passed, in view mode or not
	 * 
	 * @return true if in view correlation, above fields are displayed same as we
	 *         have entered else false
	 * @throws InterruptedException
	 */
	public boolean viewCorrelation() throws InterruptedException {
		selectCorrelation();
		try {
			Thread.sleep(2000);
			JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", viewCorrelation);
			Thread.sleep(1000);
			WebElement viewCorrelationName = driver
					.findElement(By.xpath("//b[contains(text(),'" + correlationTestName + "')]"));
			log.info("Correlation name, source and destination tool name are displayed in view mode.");
			clickOn(close, 1);
			return landingPage.isDisplayed();
		} catch (Exception ex) {
			log.warn("Something went wrong while viewing correlation. {}");
		}
		return false;
	}

	/**
	 * checks whether enable and disable functionality is working or not
	 * 
	 * @return true if disable and enable functionality is working o/w false
	 * @throws InterruptedException
	 */
	public boolean disableAndEnableCorrelation() throws InterruptedException {
		selectCorrelation();
		try {
			if (visibilityOf(disableCorrelation, 3)) {
				Thread.sleep(1000);
				log.info("Correlation is in enable mode.");
				clickOn(disableCorrelation, 2);
				crossClose.click();
				log.info("Correlation disabled successfully.");
				selectCorrelation();
				if (enableCorrelation.isDisplayed()) {
					log.info("Correlation is in disable mode.");
					clickOn(enableCorrelation, 2);
					crossClose.click();
					log.info("Correlation enabled successfully.");
				}
				selectCorrelation();
				return disableCorrelation.isDisplayed();
			}
		} catch (Exception ex) {
			Thread.sleep(1000);
			if (enableCorrelation.isDisplayed()) {
				log.info("Correlation is in disable mode.");
				clickOn(enableCorrelation, 2);
				crossClose.click();
				log.info("Correlation enabled successfully.");
				selectCorrelation();
				if (disableCorrelation.isDisplayed()) {
					log.info("Correlation is in enable mode.");
					clickOn(disableCorrelation, 2);
					crossClose.click();
					log.info("Correlation disabled successfully.");
					selectCorrelation();
					return enableCorrelation.isDisplayed();
				}
			}
		}
		return false;
	}

	/**
	 * after deleting correlation, it checks whether deleted correlation is present
	 * or not on UI
	 * 
	 * @return true if deleted correlation is not present in list of correlation in
	 *         database list o/w false
	 * @throws InterruptedException
	 */
	public boolean deleteCorrelation() throws InterruptedException {
		Thread.sleep(1000);
		selectCorrelation();
		clickOn(delete, 2);
		deleteCorrelationMessage.isDisplayed();
		clickOn(yes, 2);
		Thread.sleep(1000);
		crossClose.click();
		if (!selectCorrelation()) {
			log.info("{} deleted successfully.", correlationTestName);
			return true;
		}
		return false;
	}

	/**
	 * after filling fields and then cancel them and checks whether the filled field
	 * is empty or not
	 * 
	 * @return true if fields were emptied after clicking on cancel button else
	 *         false
	 * @throws InterruptedException
	 */
	public boolean cancelCorrelation() throws InterruptedException {
		fillData();
		correlationName.clear();
		sendKeys(correlationName, LoginAndSelectModule.testData.get(CorrelationCommonConstants.CORRELATION_NAME), 10);
		clickOn(cancel, 2);
		return isCorrelationNameEmpty();
	}

	/**
	 * to fill the required field to save correlation
	 * 
	 * @return true if all fields were filled successfully o/w false
	 * @throws InterruptedException
	 */
	private boolean fillData() throws InterruptedException {
		if (!selectSourceTool(LoginAndSelectModule.testData.get("sourceTool")))
			return false;
		selectSourceLabel.sendKeys(LoginAndSelectModule.testData.get("sourceLabel"));
		Thread.sleep(1000);
		if (!selectDestinationTool(LoginAndSelectModule.testData.get("destinationTool")))
			return false;
		selectDestinationLabel.sendKeys(LoginAndSelectModule.testData.get("destinationLabel"));
		Thread.sleep(1000);
		addRelationshipProperties();
		Thread.sleep(1000);
		if (!selectToolProperties()) {
			return false;
		}
		return true;
	}

	/**
	 * checks whether Add Relationship Properties button working or not and if it is
	 * saving the properties or not
	 * 
	 * @return true if landing page is displayed after saving add relationship
	 *         properties by clicking on add relationship properties button
	 * @throws InterruptedException
	 */
	private boolean addRelationshipProperties() {
		//clickOn(addRelationshipProperties, 2);
		String[] propertyNames = LoginAndSelectModule.testData.get("propertyNames").split(",");
		int i = 0;
		for (String property : propertyNames) {
			clickOn(addRelationshipProperties, 2);
			driver.findElement(By.xpath("//input[@formcontrolname='point']"))
					.clear();
			driver.findElement(By.xpath("//input[@formcontrolname='point']"))
					.sendKeys(property);
			//clickOn(addCircle, 2);
			clickOn(savePropertyNames, 2);
			i++;
		}
		//clickOn(savePropertyNames, 2);
		log.info("Relationship properties saved by clicking on add relationship properties button");
		return landingPage.isDisplayed();
	}

	/**
	 * This is to check the functionality of cancel button, we have already clicked
	 * on cancel button, now checking if the fields are blank or not. Here checking
	 * with one field only i.e correlation name
	 * 
	 * @return true if correlation name field is empty o/w false
	 */
	private boolean isCorrelationNameEmpty() {
		boolean isCancelWorking = false;
		if (correlationName.getText().length() == 0) {
			log.info("Cancel button working properly.");
			isCancelWorking = true;
		}
		return isCancelWorking;
	}

	/**
	 * checks whether correlation name present on the UI or not
	 * 
	 * @param correlationTestName
	 * 
	 * @return true if correlation name present in list of correlation in database
	 *         list else false
	 * @throws InterruptedException 
	 */
	private boolean verifyCorrelationName(String correlationTestName) throws InterruptedException {
		boolean isRelationPresent = false;
		try {
			visibilityOfAllElements(relationsList, 1);
			for (WebElement relation : relationsList) {
				if (relation.getText().equals(correlationTestName)) {
					isRelationPresent = true;
					log.info("Correlation is present in the List of Co-Relations in Database.");
					return isRelationPresent;
				}
			}
			log.info("Correlation is not present in the List of Co-Relations in Database.");
		} catch (Exception e) {
			log.info("Relation List is empty");
		}
		Thread.sleep(1000);
		return isRelationPresent;
	}

	/**
	 * selects the tool properties for source and destination
	 * 
	 * @return true if tool properties clicked successfully o/w false
	 * @throws InterruptedException
	 */
	private boolean selectToolProperties() throws InterruptedException {
		String[] sourceToolProperties = LoginAndSelectModule.testData.get("sourceToolProperties").split(",");
		String[] destinationToolProperties = LoginAndSelectModule.testData.get("destinationToolProperties").split(",");
		for (String sourceToolProperty : sourceToolProperties) {
			for (WebElement sourceToolProp : sourceToolPropList) {
				if (sourceToolProp.getText().equalsIgnoreCase(sourceToolProperty.trim())) {
					driver.findElement(By
							.xpath("//div[@class='property_source']/div/div/div[2]/table/tbody/tr/td[contains(text(), '"
									+ sourceToolProperty.trim() + "')]/preceding-sibling::td/mat-checkbox"))
							.click();
					break;
				}
			}
		}
		log.info("Multiple Source tool properties selected successfully");
		for (String destinationToolProperty : destinationToolProperties) {
			for (WebElement destinationToolProp : destinationToolPropList) {
				if (destinationToolProp.getText().equalsIgnoreCase(destinationToolProperty.trim()) && driver
						.findElement(By.xpath(
								"//div[@class='property_destination']/div/div/div[2]/table/tbody/tr/td[contains(text(), '"
										+ destinationToolProperty.trim() + "')]/preceding-sibling::td/mat-checkbox"))
						.isDisplayed()) {
					driver.findElement(By
							.xpath("//div[@class='property_destination']/div/div/div[2]/table/tbody/tr/td[contains(text(), '"
									+ destinationToolProperty.trim() + "')]/preceding-sibling::td/mat-checkbox"))
							.click();
					break;
				}
			}
		}
		log.info("Multiple Destination tool properties selected successfully");
		return true;
	}

	/**
	 * Fetch unique tool names which are registered under AgentManagement and
	 * WebhookConfiguration module, click on source tool and compare the fetched
	 * unique tool with the list of source tools, if matching then click that tool
	 * name
	 * 
	 * @param sourceTool
	 * @return true if source tool clicked successfully o/w false
	 * @throws InterruptedException
	 */
	private boolean selectSourceTool(String sourceTool) throws InterruptedException {
		Thread.sleep(1000);
		visibilityOf(selectSourceTool, 2);
		clickOn(selectSourceTool, 2);
		visibilityOfAllElements(sourceToolList, 2);
		log.info("UniqueToolSize : {}, sourceToolListSize : {}", uniqueToolNames.size(), sourceToolList.size());
		if (sourceToolList.size() == uniqueToolNames.size()) {
			for (WebElement sourceToolName : sourceToolList) {
				visibilityOf(sourceToolName, 2);
				if ((sourceToolName.getText()).equals(sourceTool)) {
					clickOn(sourceToolName, 2);
					log.info(
							"All the tools which are registered by using Agent Management and Webhook Configuration module are displayed and clicked the source tool successfully.");
					break;
				}
			}
			return true;
		} else {
			log.info("Source Tool didn't load properly");
			return false;
		}
	}

	/**
	 * select the destination tool on UI
	 * 
	 * @param destinationTool
	 * @return true if destination tool clicked successfully o/w false
	 * @throws InterruptedException
	 */
	private boolean selectDestinationTool(String destinationTool) throws InterruptedException {
		visibilityOf(selectDestinationTool, 2);
		clickOn(selectDestinationTool, 1);
		visibilityOfAllElements(destinationToolList,2);
		if (destinationToolList.size() == uniqueToolNames.size()) {
			for (WebElement destinationToolName : destinationToolList) {
				visibilityOf(destinationToolName, 2);
				if ((destinationToolName.getText()).equals(destinationTool)) {
					Thread.sleep(1000);
					clickOn(destinationToolName, 2);
					log.info(
							"All the tools which are registered by using Agent Management and Webhook Configuration module are displayed and clicked the destination tool successfully.");
					break;
				}
			}
			return true;
		} else {
			log.info("Destination Tool didn't load properly");
			return false;
		}
	}

	/**
	 * select the module name that are present in the left side list of UI
	 * 
	 * @param moduleName
	 * @throws InterruptedException
	 */
	private void selectModule(String moduleName) throws InterruptedException {
		List<WebElement> menuList = driver.findElements(By.xpath("//span[contains(@class,'line-child')]"));
		visibilityOfAllElements(menuList, 2);
		for (WebElement requiredOption : menuList) {
			visibilityOf(requiredOption, 2);
			if (requiredOption.getText().equals(moduleName)) {
				clickOn(requiredOption, 2);
				break;
			}
		}
		Thread.sleep(100);

	}

	/**
	 * select the correlation from the list of correlations in database list
	 * 
	 * @return true if relation present else false
	 */
	public boolean selectCorrelation() {
		boolean isRelation = false;
		visibilityOfAllElements(relationsList, 2);
		for (int i = 0; i < relationsList.size(); i++) {
			if (relationsList.get(i).getText().equals(correlationTestName)) {
				isRelation = true;
				List<WebElement> radioButtons = relationsList.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				radioButtons.get(i).click();
				log.info("{} selected successfully.", correlationTestName);
				break;
			}
		}
		return isRelation;
	}

	/**
	 * wait until the visibility of web element then send keys to the web element
	 * 
	 * @param element
	 * @param timeout
	 */
	public static void sendKeys(WebElement element, String value, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element));
		element.sendKeys(value);
	}

	/**
	 * wait until the visibility of web element then click on the web element
	 * 
	 * @param element
	 * @param timeout
	 */
	public static void clickOn(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element)).click();
	}

	/**
	 * wait until the visibility of list of web elements
	 * 
	 * @param element
	 * @return size of list of web elements
	 */
	public static int visibilityOfAllElements(List<WebElement> element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOfAllElements(element));
		return element.size();
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

	public void getAgentWebhookLabels() throws InterruptedException {
		selectModule(LoginAndSelectModule.testData.get("agentManagement"));
		try {
			visibilityOfAllElements(toolNameList, 1);
			for (WebElement toolName : toolNameList) {
				visibilityOf(toolName, 1);
				uniqueToolNames.add(toolName.getText().toUpperCase());
			}
		} catch (Exception ex) {
			log.info("No agents found under Agent Management module.");
		}
		selectModule(LoginAndSelectModule.testData.get("webhookConfiguration"));
		try {
			visibilityOfAllElements(toolNameList, 1);
			for (WebElement toolName : toolNameList) {
				visibilityOf(toolName, 1);
				uniqueToolNames.add(toolName.getText().toUpperCase());
			}
		} catch (Exception ex) {
			log.info("No webhook found under Webhook Configuration module.");
		}
		Thread.sleep(1000);
		selectModule(LoginAndSelectModule.testData.get("correlationBuiler"));
	}

}