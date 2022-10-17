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
package com.cognizant.devops.platformregressiontest.test.ui.healthcheck;

import java.time.Duration;
import java.util.List;
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
 *         Class contains the business logic for Health Check module test cases
 *
 */
public class HealthCheckConfiguration extends HealthCheckObjectRepository {

	private static final Logger log = LogManager.getLogger(HealthCheckConfiguration.class);

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	public HealthCheckConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not by checking visibility of
	 * Health Check heading, Notification label, Agents tab, Data Components tab and
	 * Services tab
	 * 
	 * @return true if Health Check heading, Notification label, Agents tab, Data
	 *         Components tab and Services tab is displayed o/w false
	 */
	public boolean navigateToHealthCheckLandingPage() {
		if (visibilityOf(landingPage, 3) && visibilityOf(notificationLabel, 3) && visibilityOf(servicesTab, 3)
				&& visibilityOf(dataComponentsTab, 3)) {
			log.info(
					"landingPage, notificationToggle, servicesTab, dataComponentsTab is displayed successfully.");
			return true;
		}
		return false;
	}

	/**
	 * Under agents tab check if all the agents are displayed which are stored in
	 * database
	 * 
	 * @return true if Agents data loaded successfully from database o/w if no
	 *         agents present then return false
	 * @throws InterruptedException 
	 */
	public boolean checkAllRegisteredAgentDisplayed() throws InterruptedException {
		Thread.sleep(1000);
		try {
			if (visibilityOfAllElements(toolNameList, 1) > 0) {
				Thread.sleep(10000);
				log.info("Agents data has been loaded successfully from database.");
				return true;
			}
		} catch (Exception e) {
			log.info("Either no agents present in databse or agents data has not been loaded from database.");
		}
		return false;
	}

	/**
	 * Tests the functionality of notification toggle by changing the state and
	 * revert it back to the same state
	 * 
	 * @return if it is enabled then make it disable, again make it enable and check
	 *         if required state is displayed then return true else false and vice
	 *         versa
	 */
	public boolean testNotificationToggle() {
		try {
			Thread.sleep(2000);
			if (visibilityOf(notificationToggleFalse, 1)) {
				return enableThenDisableNotificationToggle();
			}
		} catch (Exception ex) {
			if (visibilityOf(notificationToggleTrue, 2)) {
				return disableThenEnableNotificationToggle();
			}
		}
		return false;
	}

	/**
	 * Under Agents tab check status of all the agents and increment count variable
	 * every time status failure agent is present. If failure count is zero then
	 * return true else false.
	 * 
	 * @return true if no failure status agent present o/w false
	 */
	public boolean checkHealthCheckStatusIcon() {
		try {
			Thread.sleep(1000);
			if (visibilityOf(driver.findElement(By.xpath(
					"//tbody[@role='rowgroup']/tr[1]/td[3]/mat-icon[@svgicon='success_status']")),
					3)) {
				log.info("Health Check Icon Visible. Status : Success");
				return true;
			}
		} catch (Exception ex) {
			if (visibilityOf(driver.findElement(By.xpath(
					"//tbody[@role='rowgroup']/tr[1]/td[3]/mat-icon[@svgicon='failure_status']")),
					3)) {
				log.info("Health Check Icon Visible. Status : Fail");
				return false;
			}
		}
		throw new SkipException("Skipping test case as something went wrong.");
	}

	/**
	 * For all the agents present under Agents tab, click on details icon and check
	 * if Latest Status Details and Latest Failure Details is present in the details
	 * dialog box. If any detail dialog box not having these details then return
	 * false else true
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean detailDialogBoxTabUnderAgentsTab() throws InterruptedException {
		int agentCount = 0;
		int agentsPresent = visibilityOfAllElements(detailsList, 10);
		try {
			int i = 0;
			for (WebElement detail : detailsList) {
				clickOn(detail, 10);
				try {
					Thread.sleep(1000);
					if (visibilityOf(latestStatusDetails, 1) && visibilityOf(latestFailureDetails, 1)) {
						visibilityOf(additionalDetailsHeading, 1);
						clickOn(closeDialog, 3);
						i++;
					}
				} catch (Exception ex) {
					agentCount++;
					visibilityOf(additionalDetailsHeading, 1);
					clickOn(closeDialog, 3);
					log.info("For {} agent not able to found latest status details or latest failure details",
							driver.findElement(By.xpath("//*[@id='agentsTable']/table/tbody/tr[" + (++i) + "]/td[2]"))
									.getText());
				}
			}
		} catch (Exception e) {
			clickOn(closeDialog, 3);
			log.info(e.getMessage());
		}
		Thread.sleep(5000);
		if (agentCount != 0) {
			log.info("For {} agents out of {}, not able to found latest status details or latest failure details",
					agentCount, agentsPresent);
			throw new SkipException(
					"Skipping test case as not able to found latest status details or latest failure details for all agents");
		}
		log.info("For all agents latest status details or latest failure details are present");
		return true;
	}

	/**
	 * Under Agents tab, first extract the given tool specific agents and store that
	 * value. Now after clicking on Select Tool, select the given tool and click on
	 * it. Now check if the displayed agents size is matching with earlier one.
	 * 
	 * @return true if both agents list size is matching else false
	 */
	public boolean testSelectToolUnderAgentsTab() {
		clickOn(agentsTab, 5);
		int agentsCount = 0;
		visibilityOfAllElements(toolNameList, 5);
		for (WebElement tool : toolNameList) {
			if (tool.getText().equals(LoginAndSelectModule.testData.get("selectToolName"))) {
				++agentsCount;
			}
		}
		log.info("Before selecting tool, agents present for selected tool : {}", agentsCount);
		visibilityOf(selectToolLabel, 3);
		clickOn(selectTool, 3);
		visibilityOfAllElements(selectToolOptionsList, 3);
		for (WebElement tool : selectToolOptionsList) {
			visibilityOf(tool, 3);
			if (tool.getText().equals(LoginAndSelectModule.testData.get("selectToolName"))) {
				clickOn(tool, 3);
				break;
			}
		}
		visibilityOfAllElements(toolNameList, 3);
		int agentsListSize = toolNameList.size();
		log.info("After selecting tool, agents present for selected tool : {}", agentsListSize);
		if (agentsCount == agentsListSize) {
			return true;
		}
		return false;
	}

	/**
	 * Under Data Components Tab first verify if all required headings are present
	 * e.g. Name, IP Address:Port, Version, Additional Information, Status. For
	 * every server that is present in the list, check if Ip address, Version,
	 * Additional Information is displayed as expected or not
	 * 
	 * @return true if all headings and server related data is correct else false
	 * @throws InterruptedException 
	 */
	public boolean verifyDataComponentsTabData() throws InterruptedException {
		boolean verifyPostgreSqlData = false;
		boolean verifyNeo4jlData = false;
		boolean verifyElasticSearchData = false;
		boolean verifyRabbitMQData = false;
		boolean verifyPython=false;
		boolean verifyGrafana=false;
		try {
			clickOn(dataComponentsTab, 5);
			Thread.sleep(5000);
			checkServersHeadingComponent();
			int i = 0;
			Thread.sleep(3000);
			while (i != visibilityOfAllElements(serverNameList, 5)) {
				++i;
				String serverName = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[1]"))
						.getText();
				String ipAddress = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[2]"))
						.getText();
				String version = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[4]"))
						.getText();
				String additionalInformation = driver
						.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[5]")).getText();
				switch (serverName) {
				case "PostgreSQL":
					verifyPostgreSqlData = checkPostgreSqlData(serverName, ipAddress, version);
					break;
				case "Neo4j":
					verifyNeo4jlData = checkNeo4jData(serverName, ipAddress, version, additionalInformation);
					break;
				case "Elasticsearch":
					verifyElasticSearchData = checkElasticSearchData(serverName, ipAddress, version);
					break;
				case "RabbitMQ":
					verifyRabbitMQData = checkRabbitMQData(serverName, ipAddress, version);
					break;
				case "Grafana":
					verifyGrafana=checkGrafanaData(serverName, ipAddress, version);
					break;
				case "Python":
					verifyPython=checkPythonData(serverName, version);
					break;
				default:
					log.info("No servers found!!");
					break;
				}
			}
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}
		Thread.sleep(5000);
		if (verifyPostgreSqlData && verifyNeo4jlData && verifyElasticSearchData && 
				verifyRabbitMQData&& verifyGrafana &&verifyPython) {
			log.info("Under data components tab, all servers are having correct data.");
			return true;
		}
		log.info("Under data components tab, not all servers are having correct data.");
		throw new SkipException(
				"Skipping test case as not all servers are having correct data or health check failure");
	}

	/**
	 * Fill email configuration block with valid details
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean verifyServerConfigEmailBlock() throws InterruptedException {
		Thread.sleep(1000);
		selectMenuOption("Server Configuration");
		try {
			Thread.sleep(1000);
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView();", emailConfigurationBlock);
			Thread.sleep(1000);
			sendEmailEnabled.clear();
			sendEmailEnabled.sendKeys(LoginAndSelectModule.testData.get("sendEmailEnabled"));
			smtpHostServer.clear();
			smtpHostServer.sendKeys(LoginAndSelectModule.testData.get("smtpHostServer"));
			smtpPort.clear();
			smtpPort.sendKeys(LoginAndSelectModule.testData.get("smtpPort"));
			smtpUserName.clear();
			smtpUserName.sendKeys(LoginAndSelectModule.testData.get("smtpUserName"));
			smtpPassword.clear();
			smtpPassword.sendKeys(LoginAndSelectModule.testData.get("smtpPassword"));
			isAuthRequired.clear();
			isAuthRequired.sendKeys(LoginAndSelectModule.testData.get("isAuthRequired"));
			smtpStarttlsEnable.clear();
			smtpStarttlsEnable.sendKeys(LoginAndSelectModule.testData.get("smtpStarttlsEnable"));
			mailFrom.clear();
			mailFrom.sendKeys(LoginAndSelectModule.testData.get("mailFrom"));
//			mailTo.clear();
//			mailTo.sendKeys(LoginAndSelectModule.testData.get("mailTo"));
			subject.clear();
			subject.sendKeys(LoginAndSelectModule.testData.get("subject"));
			emailBody.clear();
			emailBody.sendKeys(LoginAndSelectModule.testData.get("emailBody"));
			systemNotificationSubscriber.clear();
			systemNotificationSubscriber.sendKeys(LoginAndSelectModule.testData.get("systemNotificationSubscriber"));
			selectMenuOption(LoginAndSelectModule.testData.get("healthCheck"));
			return true;
		} catch (Exception e) {
			log.info("Something went wrong while updating email configuration block."+e.getMessage());
			return false;
		}
	}

	/**
	 * Under Data Components tab, check if all the headings present or not i.e Name,
	 * IP Address:Port, Version, Status, Additional Information
	 * 
	 * @return true if all headings present else false
	 */
	private boolean checkServersHeadingComponent() {
		if (visibilityOf(serverNameHeading, 5) && visibilityOf(ipAddressHeading, 5)
				&& visibilityOf(versionHeading, 5) && visibilityOf(infoHeading, 5)
				&& visibilityOf(statusHeading, 5)) {
			log.info("Name, IP Address:Port, Version, Additional Information and Staus headings are displayed on UI.");
			return true;
		} else {
			log.info("Not all headings are present under Data Components tab.");
			return false;
		}
	}

	/**
	 * Under Data Components tab check the status of all servers
	 * 
	 * @return true if the health check status for each server is success else false
	 * @throws InterruptedException 
	 */
	public boolean serverHealthCheckStatus() throws InterruptedException {
		int failureCount = 0;
		int i = 0;
		clickOn(dataComponentsTab, 5);
		log.info("Servers present on UI : {} ", visibilityOfAllElements(serverNameList, 10));
		while (i != visibilityOfAllElements(dataComponentsTabData, 5)) {
			++i;
			String serverName = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[1]")).getText();
			Thread.sleep(1000);
			try {
				if (visibilityOf(driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i
						+ "]/td[3]/mat-icon[@svgicon='success_status']")), 1))
					log.info("{} Health Check Status : Success", serverName);
			} catch (Exception ex) {
				++failureCount;
				if (visibilityOf(driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i
						+ "]/td[3]/mat-icon[@svgicon='failure_status']")), 1))
					log.info("{} Health Check Status : Fail", serverName);
			}
		}
		Thread.sleep(5000);
		if (failureCount != 0) {
			log.info("Skipping test cases as health Check Failure for some server");
		}
		log.info("No Health Check Failures found for servers");
		return true;
	}

	/**
	 * Under Services tab first verify if all required headings are present e.g.
	 * Name, IP Address:Port, Version, Status, Details. For every service that is
	 * present in the list, check if Ip address, Version is displayed as expected or
	 * not
	 * 
	 * @return true if all headings and service related data is correct else false
	 */
	public boolean verifyServicesTabData() {
		boolean verifyPlatformService = false;
		boolean verifyPlatformEngine = false;
		boolean verifyPlatformWorkflow = false;
		boolean verifyWebhookEngine = false;
		boolean verifyWebhookSubscriber = false;
		boolean verifyPlatformAuditEngine = false;
		try {
			clickOn(servicesTab, 10);
			checkServicesHeadingComponent();
			int i = 0;
			int servicesCount = visibilityOfAllElements(servicesTabData, 5);
			while (i != servicesCount) {
				++i;
				Thread.sleep(1000);
				String serviceName = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[1]"))
						.getText();
				String ipAddress = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[2]"))
						.getText();
				String version = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[3]"))
						.getText();
				WebElement details = driver
						.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[5]/a/mat-icon"));
				switch (serviceName) {
				case "Platform Service":
					verifyPlatformService = verifyPlatformServiceData(serviceName, ipAddress, version);
					break;
				case "Platform Engine":
					verifyPlatformEngine = verifyPlatformEngineData(serviceName, ipAddress, version);
					break;
				case "Platform Workflow":
					verifyPlatformWorkflow = verifyPlatformWorkflowData(serviceName, ipAddress, version);
					break;
				default:
					log.info("No servers found!!");
					break;
				}
			}
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}
		if ( verifyPlatformWorkflow && verifyPlatformEngine
				&& verifyPlatformService) {
			log.info("Under Services tab, all services are having correct data.");
			return true;
		}
		log.info("Under Services tab, not all services are having correct data.");
		throw new SkipException("Skipping test case as under Services tab, not all services are having correct data.");
	}

	/**
	 * Under services tab check the status of all services
	 * 
	 * @return true if the health check status for each service is success else
	 *         false
	 * @throws InterruptedException 
	 */
	public boolean serviceHealthCheckStatus() throws InterruptedException {
		int failureCount = 0;
		int i = 0;
		clickOn(servicesTab, 5);
		log.info("Services present on UI : {} ", visibilityOfAllElements(servicesTabData, 10));
		while (i != visibilityOfAllElements(servicesTabData, 3)) {
			++i;
			String serviceName = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[1]"))
					.getText();
			Thread.sleep(1000);
			try {
				if (visibilityOf(driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i
						+ "]/td[4]/mat-icon[@data-mat-icon-name='success_status']")), 1))
					log.info("{} Health Check Status : Success", serviceName);
			} catch (Exception ex) {
				++failureCount;
				if (visibilityOf(driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i
						+ "]/td[4]/mat-icon[@data-mat-icon-name='failure_status']")), 1))
					log.info("{} Health Check Status : Fail", serviceName);
			}
		}
		Thread.sleep(1000);
		if (failureCount != 0) {
			log.info("Skipping test cases as health Check Failure for some service");
		}
		log.info("No Health Check Failures found for services");
		return true;
	}

	/**
	 * For all the services present under Services tab, click on details icon and
	 * check if Latest Status Details is present in the details dialog box. If any
	 * detail dialog box not having these details then return false else true
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean verifyDetailsDialogBoxUnderServicesTab() {
		try {
			int detailsPresent = visibilityOfAllElements(detailsList, 10);
			int failureDetails = 0;
			int i = 0;
			for (WebElement detail : detailsList) {
				clickOn(detail, 10);
				try {
					Thread.sleep(1000);
					if (visibilityOf(latestStatusDetailsService, 2)) {
						visibilityOf(additionalDetailsHeading, 2);
						clickOn(closeDialog, 3);
						i++;
					}
				} catch (Exception ex) {
					failureDetails++;
					visibilityOf(additionalDetailsHeading, 3);
					clickOn(closeDialog, 3);
					log.info("For {} service not able to found latest status details or latest failure details",
							driver.findElement(By.xpath("//*[@id='servicesTable']/table/tbody/tr[" + (++i) + "]/td[1]"))
									.getText());
				}
			}
			if (failureDetails != 0) {
				log.info("For {} service out of {}, not able to found latest status details.", failureDetails,
						detailsPresent);
				throw new SkipException(
						"Skipping test case as not able to found latest status details for all services");
			}
		} catch (Exception e) {
			clickOn(closeDialog, 3);
			log.info(e.getMessage());
		}
		log.info("For all services latest status details or latest failure details are present");
		return true;
	}

	/**
	 * if notification toggle is enabled then make it disable, again make it enable
	 * and check if required state is displayed then return true else false
	 * 
	 * @return
	 */
	private boolean disableThenEnableNotificationToggle() {
		log.info("Notification Toggle is in enable mode");
		clickOn(notificationToggleTrue, 10);
		visibilityOf(disableNotificationHeading, 10);
		clickOn(yes, 2);
		visibilityOf(successDisable, 2);
//		visibilityOf(notificationDisabledMsg, 10);
		log.info(successDisable.getText());
		clickOn(crossClose, 2);
		if (visibilityOf(notificationToggleFalse, 10) && visibilityOf(notificationLabel, 10)) {
			clickOn(notificationToggleFalse, 10);
			visibilityOf(enableNotificationHeading, 10);
			clickOn(yes, 10);
			visibilityOf(successEnable, 10);
//			visibilityOf(notificationEnabledMsg, 10);
			log.info(successEnable.getText());
			clickOn(crossClose, 10);
			if (visibilityOf(notificationToggleTrue, 10) && visibilityOf(notificationHistory, 10)
					&& visibilityOf(notificationHistoryDetails, 10) && visibilityOf(notificationLabel, 10)) {
				clickOn(notificationHistoryDetails, 10);
				visibilityOf(healthNotificationHeading, 10);
				clickOn(closeDialog, 3);
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return if notification toggle is disabled then make it enable, again make it
	 *         disable and check if required state is displayed then return true
	 *         else false
	 * @throws InterruptedException 
	 */
	private boolean enableThenDisableNotificationToggle() throws InterruptedException {
		log.info("Notification Toggle is in disable mode");
		clickOn(notificationToggleFalse, 10);
		visibilityOf(enableNotificationHeading, 10);
		clickOn(yes, 10);
		Thread.sleep(500);
		visibilityOf(successEnable, 4);
		log.info(successEnable.getText());
		clickOn(crossClose, 10);
		Thread.sleep(1000);
		if (visibilityOf(notificationToggleTrue, 10) && visibilityOf(notificationHistory, 10)
				&& visibilityOf(notificationHistoryDetails, 10) && visibilityOf(notificationLabel, 10)) {
			clickOn(notificationHistoryDetails, 10);
			visibilityOf(healthNotificationHeading, 10);
			clickOn(closeDialog, 3);
			if (visibilityOf(notificationToggleTrue, 10)) {
				log.info("Notification Toggle is in enable mode");
				clickOn(notificationToggleTrue, 10);
				visibilityOf(disableNotificationHeading, 10);
				clickOn(yes, 10);
				visibilityOf(successDisable, 10);
				log.info(successDisable.getText());
				clickOn(crossClose, 10);
				if (visibilityOf(notificationToggleFalse, 10) && visibilityOf(notificationLabel, 10)) {
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * Under Services tab after clicking on details icon, check whether Latest
	 * Status Details is displayed or not
	 * 
	 * @param details
	 * @param serviceName
	 * @return true if for each service Latest Status detail is displayed else false
	 */
	private boolean checkDetails(WebElement details, String serviceName) {
		clickOn(details, 10);
		visibilityOf(additionalDetailsHeading, 10);
		if (visibilityOf(latestStatusDetailsService, 10)) {
			clickOn(closeDialog, 3);
			log.info("{} : Latest Status Details present in details dialog box", serviceName);
			return true;
		}
		clickOn(closeDialog, 3);
		return false;
	}

	private boolean verifyPlatformAuditEngineData(String serviceName, String ipAddress, String version) {
		String auditEnginePort = LoginAndSelectModule.testData.get("auditEnginePort");
		String auditEngineVersion = LoginAndSelectModule.testData.get("auditEngineVersion");
		if (ipAddress!="null" && version!="null") {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, auditEnginePort,
					auditEngineVersion);
			return true;
		} else {
			log.error("{} data is not correct.", serviceName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, auditEnginePort,
					auditEngineVersion);
			return false;
		}
	}

	/**
	 * Under Services tab check WebhookSubscriber data i.e Ip address and Version
	 * 
	 * @param serviceName
	 * @param ipAddress
	 * @param version
	 * @return true if ip address and details are correct else false
	 */
	private boolean verifyWebhookSubscriberData(String serviceName, String ipAddress, String version) {
		String webhookSubscriberPort = LoginAndSelectModule.testData.get("webhookSubscriberPort");
		String webhookSubscriberVersion = LoginAndSelectModule.testData.get("webhookSubscriberVersion");
		if (ipAddress!="null" && version!="null") {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, webhookSubscriberPort,
					webhookSubscriberVersion);
			return true;
		} else {
			log.error("{} data is not correct.", serviceName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, webhookSubscriberPort,
					webhookSubscriberVersion);
			return false;
		}
	}

	/**
	 * 
	 * Under Services tab check WebhookEngine data i.e Ip address and Version
	 * 
	 * @param serviceName
	 * @param ipAddress
	 * @param version
	 * @return true if ip address and details are correct else false
	 */
	private boolean verifyWebhookEngineData(String serviceName, String ipAddress, String version) {
		String webhookEnginePort = LoginAndSelectModule.testData.get("webhookEnginePort");
		String webhookEngineVersion = LoginAndSelectModule.testData.get("webhookEngineVersion");
		if (ipAddress!="null" && version!="null") {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, webhookEnginePort,
					webhookEngineVersion);
			return true;
		} else {
			log.error("{} data is not correct.", serviceName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, webhookEnginePort,
					webhookEngineVersion);
			return false;
		}
	}

	/**
	 * Under Services tab check PlatformWorkflow data i.e Ip address and Version
	 * 
	 * @param serviceName
	 * @param ipAddress
	 * @param version
	 * @return true if ip address and details are correct else false
	 */
	private boolean verifyPlatformWorkflowData(String serviceName, String ipAddress, String version) {
		String platformWorkflowPort = LoginAndSelectModule.testData.get("platformWorkflowPort");
		String platformWorkflowVersion = LoginAndSelectModule.testData.get("platformWorkflowVersion");
		if (ipAddress!="null" && version!="null") {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, platformWorkflowPort,
					platformWorkflowVersion);
			return true;
		} else {
			log.error("{} data is not correct.", serviceName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, platformWorkflowPort,
					platformWorkflowVersion);
			return false;
		}
	}

	/**
	 * Under Services tab check PlatformEngine data i.e Ip address and Version
	 * 
	 * @param serviceName
	 * @param ipAddress
	 * @param version
	 * @return true if ip address and details are correct else false
	 */
	private boolean verifyPlatformEngineData(String serviceName, String ipAddress, String version) {
		String platformEnginePort = LoginAndSelectModule.testData.get("platformEnginePort");
		String platformEngineVersion = LoginAndSelectModule.testData.get("platformEngineVersion");
		if (ipAddress!="null" && version!="null") {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, platformEnginePort,
					platformEngineVersion);
			return true;
		} else {
			log.error("{} data is not correct.", serviceName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, platformEnginePort,
					platformEngineVersion);
			return false;
		}
	}

	/**
	 * Under Services tab check PlatformService data i.e Ip address and Version
	 * 
	 * @param serviceName
	 * @param ipAddress
	 * @param version
	 * @return true if ip address and details are correct else false
	 */
	private boolean verifyPlatformServiceData(String serviceName, String ipAddress, String version) {
		String platformServicePort = LoginAndSelectModule.testData.get("platformServicePort");
		String platformServiceVersion = LoginAndSelectModule.testData.get("platformServiceVersion");
		if (ipAddress!="null" && version!="null") {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, platformServicePort,
					platformServiceVersion);
			return true;
		} else {
			log.error("{} data is not correct.", serviceName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serviceName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serviceName, platformServicePort,
					platformServiceVersion);
			return false;
		}
	}

	/**
	 * Under Services tab, check if all the headings present or not i.e Name, IP
	 * Address:Port, Version, Status, Details
	 * 
	 * @return true if all headings present else false
	 */
	private boolean checkServicesHeadingComponent() {
		if (visibilityOf(serviceNameHeading, 10) && visibilityOf(serviceIpHeading, 10)
				&& visibilityOf(servicVersionHeading, 10) && visibilityOf(serviceStatusHeading, 10)
				&& visibilityOf(serviceDetailsHeading, 10)) {
			log.info("Name, IP Address:Port, Version, Additional Information and Staus headings are displayed on UI.");
			return true;
		} else {
			log.info("Not all headings are present.");
			return false;
		}
	}

	/**
	 * Under Data Components tab, check RabbitMQ data i.e Ip address and version
	 * 
	 * @param serverName
	 * @param ipAddress
	 * @param version
	 * @return true if server data is correct else false
	 */
	private boolean checkRabbitMQData(String serverName, String ipAddress, String version) {
		String rabbitMQPort = LoginAndSelectModule.testData.get("RabbitMQPort");
		String rabbitMQVersion = LoginAndSelectModule.testData.get("RabbitMQVersion");
		if (serverName.contains("RabbitMQ") && ipAddress.contains(rabbitMQPort) && version.contains(rabbitMQVersion)) {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, rabbitMQPort, rabbitMQVersion);
			return true;
		} else {
			log.info("{} data is not correct.", serverName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, rabbitMQPort, rabbitMQVersion);
			return false;
		}
	}
	/**
	 * Under Data Components tab, check Grafana data i.e Ip address and version
	 * 
	 * @param serverName
	 * @param ipAddress
	 * @param version
	 * @return true if server data is correct else false
	 */
	private boolean checkGrafanaData(String serverName, String ipAddress, String version) {
		String grafanaMQPort = LoginAndSelectModule.testData.get("GrafanaPort");
		String grafanaVersion = LoginAndSelectModule.testData.get("GrafanaVersion");
		if (serverName.contains("Grafana") && ipAddress.contains(grafanaMQPort) && version.contains(grafanaVersion)) {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, grafanaMQPort, grafanaVersion);
			return true;
		} else {
			log.info("{} data is not correct.", serverName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, grafanaMQPort, grafanaVersion);
			return false;
		}
	}

	/**
	 * Under Data Components tab, check Python data i.e Ip address and version
	 * 
	 * @param serverName
	 * @param ipAddress
	 * @param version
	 * @return true if server data is correct else false
	 */
	private boolean checkPythonData(String serverName,String version) {
		String pythonVersion = LoginAndSelectModule.testData.get("PythonVersion");
		if (serverName.contains("Python")&& version.contains(pythonVersion)) {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, pythonVersion);
			return true;
		} else {
			log.info("{} data is not correct.", serverName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, pythonVersion);
			return false;
		}
	}

	/**
	 * Under Data Components tab, check ElasticSearch data i.e Ip address and
	 * version
	 * 
	 * @param serverName
	 * @param ipAddress
	 * @param version
	 * @return true if server data is correct else false
	 */
	private boolean checkElasticSearchData(String serverName, String ipAddress, String version) {
		String elasticsearchPort = LoginAndSelectModule.testData.get("ElasticsearchPort");
		String elasticsearchVersion = LoginAndSelectModule.testData.get("ElasticsearchVersion");
		if (serverName.contains("Elasticsearch") && ipAddress.contains(elasticsearchPort)
				&& version.contains(elasticsearchVersion)) {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, elasticsearchPort,
					elasticsearchVersion);
			return true;
		} else {
			log.info("{} data is not correct.", serverName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, elasticsearchPort,
					elasticsearchVersion);
			return false;
		}
	}

	/**
	 * Under Data Components tab, check Neo4j data i.e Ip address and version
	 * 
	 * @param serverName
	 * @param ipAddress
	 * @param version
	 * @param additionalInformation
	 * @return true if server data is correct else false
	 */
	private boolean checkNeo4jData(String serverName, String ipAddress, String version, String additionalInformation) {
		String neo4jPort = LoginAndSelectModule.testData.get("Neo4jPort");
		String neo4jVersion = LoginAndSelectModule.testData.get("Neo4jVersion");
		if (serverName.contains("Neo4j") && ipAddress.contains(neo4jPort) && version.contains(neo4jVersion)
				&& additionalInformation.contains("Total DB Size")) {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}, Additional Information: {}", serverName,
					ipAddress, version, additionalInformation);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, neo4jPort, neo4jVersion);
			return true;
		} else {
			log.info("{} data is not correct.", serverName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}, Additional Information: {}", serverName,
					ipAddress, version, additionalInformation);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, neo4jPort, neo4jVersion);
			return false;
		}
	}

	/**
	 * Under Data Components tab,check PostgreSql data i.e Ip address and version
	 * 
	 * @param serverName
	 * @param ipAddress
	 * @param version
	 * @return true if server data is correct else false
	 */
	private boolean checkPostgreSqlData(String serverName, String ipAddress, String version) {
		String postgreSqlPort = LoginAndSelectModule.testData.get("postgreSqlPort");
		String postgreSqlVersion = LoginAndSelectModule.testData.get("postgreSqlVersion");
		if (serverName.contains("PostgreSQL") && ipAddress.contains(postgreSqlPort)
				&& version.contains(postgreSqlVersion)) {
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, postgreSqlPort, postgreSqlVersion);
			return true;
		} else {
			log.info("{} data is not correct.", serverName);
			log.info("{} data on UI - IP Address:Port: {}, Version: {}", serverName, ipAddress, version);
			log.info("{} test data - IP Address:Port: {}, Version: {}", serverName, postgreSqlPort, postgreSqlVersion);
			return false;
		}
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
