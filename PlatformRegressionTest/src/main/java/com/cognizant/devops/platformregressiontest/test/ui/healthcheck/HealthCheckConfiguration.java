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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
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

	WebDriverWait wait = new WebDriverWait(driver, 20);

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
		if (visibilityOf(landingPage, 10) && visibilityOf(notificationLabel, 10) && visibilityOf(servicesTab, 10)
				&& visibilityOf(dataComponentsTab, 10) && visibilityOf(agentsTab, 10)) {
			log.info(
					"landingPage, notificationToggle, servicesTab, dataComponentsTab, agentsTab is displayed successfully.");
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
	 */
	public boolean checkAllRegisteredAgentDisplayed() {
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		try {
			if (visibilityOfAllElements(toolNameList, 1) > 0) {
				driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
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
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			if (visibilityOf(notificationToggleFalse, 1)) {
				return enableThenDisableNotificationToggle();
			}
		} catch (Exception ex) {
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			if (visibilityOf(driver.findElement(By.xpath(
					"//tbody[@role='rowgroup']/tr[1]/td[5]/mat-icon[@data-mat-icon-name='healthcheck_success_status']")),
					1)) {
				log.info("Health Check Icon Visible. Status : Success");
				return true;
			}
		} catch (Exception ex) {
			if (visibilityOf(driver.findElement(By.xpath(
					"//tbody[@role='rowgroup']/tr[1]/td[5]/mat-icon[@data-mat-icon-name='healthcheck_failure_status']")),
					1)) {
				log.info("Health Check Icon Visible. Status : Fail");
				return true;
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
					driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
					if (visibilityOf(latestStatusDetails, 1) && visibilityOf(latestFailureDetails, 1)) {
						visibilityOf(additionalDetailsHeading, 1);
						clickOn(closeDialog, 10);
						i++;
					}
				} catch (Exception ex) {
					agentCount++;
					visibilityOf(additionalDetailsHeading, 1);
					clickOn(closeDialog, 10);
					log.info("For {} agent not able to found latest status details or latest failure details",
							driver.findElement(By.xpath("//*[@id='agentsTable']/table/tbody/tr[" + (++i) + "]/td[2]"))
									.getText());
				}
			}
		} catch (Exception e) {
			clickOn(closeDialog, 10);
			log.info(e.getMessage());
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
		clickOn(agentsTab, 10);
		int agentsCount = 0;
		visibilityOfAllElements(toolNameList, 10);
		for (WebElement tool : toolNameList) {
			if (tool.getText().equals(LoginAndSelectModule.testData.get("selectToolName"))) {
				++agentsCount;
			}
		}
		log.info("Before selecting tool, agents present for selected tool : {}", agentsCount);
		visibilityOf(selectToolLabel, 10);
		clickOn(selectTool, 10);
		visibilityOfAllElements(selectToolOptionsList, 10);
		for (WebElement tool : selectToolOptionsList) {
			visibilityOf(tool, 10);
			if (tool.getText().equals(LoginAndSelectModule.testData.get("selectToolName"))) {
				clickOn(tool, 10);
				break;
			}
		}
		visibilityOfAllElements(toolNameList, 10);
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
	 */
	public boolean verifyDataComponentsTabData() {
		boolean verifyPostgreSqlData = false;
		boolean verifyNeo4jlData = false;
		boolean verifyElasticSearchData = false;
		boolean verifyRabbitMQData = false;
		try {
			clickOn(dataComponentsTab, 10);
			Thread.sleep(5000);
			checkServersHeadingComponent();
			int i = 0;
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			Thread.sleep(3000);
			while (i != visibilityOfAllElements(serverNameList, 10)) {
				++i;
				String serverName = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[1]"))
						.getText();
				String ipAddress = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[2]"))
						.getText();
				String version = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[3]"))
						.getText();
				String additionalInformation = driver
						.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[4]")).getText();
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
				default:
					log.info("No servers found!!");
					break;
				}
			}
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		if (verifyPostgreSqlData && verifyNeo4jlData && verifyElasticSearchData && verifyRabbitMQData) {
			log.info("Under data components tab, all servers are having correct data.");
			return true;
		}
		log.info("Under data components tab, not all servers are having correct data.");
		throw new SkipException(
				"Skipping test case as not all servers are having correct data or health check failure");
	}

	/**
	 * Under Data Components tab, check if all the headings present or not i.e Name,
	 * IP Address:Port, Version, Status, Additional Information
	 * 
	 * @return true if all headings present else false
	 */
	private boolean checkServersHeadingComponent() {
		if (visibilityOf(serverNameHeading, 10) && visibilityOf(ipAddressHeading, 10)
				&& visibilityOf(versionHeading, 10) && visibilityOf(infoHeading, 10)
				&& visibilityOf(statusHeading, 10)) {
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
	 */
	public boolean serverHealthCheckStatus() {
		int failureCount = 0;
		int i = 0;
		clickOn(dataComponentsTab, 10);
		log.info("Servers present on UI : {} ", visibilityOfAllElements(serverNameList, 10));
		while (i != visibilityOfAllElements(dataComponentsTabData, 10)) {
			++i;
			String serverName = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[1]")).getText();
			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			try {
				if (visibilityOf(driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i
						+ "]/td[5]/mat-icon[@data-mat-icon-name='healthcheck_success_status']")), 1))
					log.info("{} Health Check Status : Success", serverName);
			} catch (Exception ex) {
				++failureCount;
				if (visibilityOf(driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i
						+ "]/td[5]/mat-icon[@data-mat-icon-name='healthcheck_failure_status']")), 1))
					log.info("{} Health Check Status : Fail", serverName);
			}
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		if (failureCount != 0) {
			log.info("Skipping test cases as health Check Failure for some server");
			throw new SkipException("Skipping test cases as health Check Failure for some server");
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
			int servicesCount = visibilityOfAllElements(servicesTabData, 10);
			while (i != servicesCount) {
				++i;
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
				case "Platform WebhookEngine":
					verifyWebhookEngine = verifyWebhookEngineData(serviceName, ipAddress, version);
					break;
				case "Platform WebhookSubscriber":
					verifyWebhookSubscriber = verifyWebhookSubscriberData(serviceName, ipAddress, version);
					break;
				case "Platform AuditEngine":
					verifyWebhookSubscriber = verifyPlatformAuditEngineData(serviceName, ipAddress, version);
					break;
				default:
					log.info("No servers found!!");
					break;
				}
			}
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}
		if (verifyWebhookSubscriber && verifyWebhookEngine && verifyPlatformWorkflow && verifyPlatformEngine
				&& verifyPlatformService && verifyPlatformAuditEngine) {
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
	 */
	public boolean serviceHealthCheckStatus() {
		int failureCount = 0;
		int i = 0;
		clickOn(servicesTab, 10);
		log.info("Services present on UI : {} ", visibilityOfAllElements(servicesTabData, 10));
		while (i != visibilityOfAllElements(servicesTabData, 10)) {
			++i;
			String serviceName = driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i + "]/td[1]"))
					.getText();
			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			try {
				if (visibilityOf(driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i
						+ "]/td[4]/mat-icon[@data-mat-icon-name='healthcheck_success_status']")), 1))
					log.info("{} Health Check Status : Success", serviceName);
			} catch (Exception ex) {
				++failureCount;
				if (visibilityOf(driver.findElement(By.xpath("//tbody[@role='rowgroup']/tr[" + i
						+ "]/td[4]/mat-icon[@data-mat-icon-name='healthcheck_failure_status']")), 1))
					log.info("{} Health Check Status : Fail", serviceName);
			}
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		if (failureCount != 0) {
			log.info("Skipping test cases as health Check Failure for some service");
			throw new SkipException("Skipping test cases as health Check Failure for some service");
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
					driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
					if (visibilityOf(latestStatusDetailsService, 2)) {
						visibilityOf(additionalDetailsHeading, 10);
						clickOn(closeDialog, 10);
						i++;
					}
				} catch (Exception ex) {
					failureDetails++;
					visibilityOf(additionalDetailsHeading, 3);
					clickOn(closeDialog, 10);
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
			clickOn(closeDialog, 10);
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
		clickOn(yes, 10);
		visibilityOf(success, 10);
		visibilityOf(notificationDisabledMsg, 10);
		log.info(notificationDisabledMsg.getText());
		clickOn(ok, 10);
		if (visibilityOf(notificationToggleFalse, 10) && visibilityOf(notificationLabel, 10)) {
			clickOn(notificationToggleFalse, 10);
			visibilityOf(enableNotificationHeading, 10);
			clickOn(yes, 10);
			visibilityOf(success, 10);
			visibilityOf(notificationEnabledMsg, 10);
			log.info(notificationEnabledMsg.getText());
			clickOn(ok, 10);
			if (visibilityOf(notificationToggleTrue, 10) && visibilityOf(notificationHistory, 10)
					&& visibilityOf(notificationHistoryDetails, 10) && visibilityOf(notificationLabel, 10)) {
				clickOn(notificationHistoryDetails, 10);
				visibilityOf(healthNotificationHeading, 10);
				clickOn(closeDialog, 10);
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
	 */
	private boolean enableThenDisableNotificationToggle() {
		log.info("Notification Toggle is in disable mode");
		clickOn(notificationToggleFalse, 10);
		visibilityOf(enableNotificationHeading, 10);
		clickOn(yes, 10);
		visibilityOf(success, 10);
		visibilityOf(notificationEnabledMsg, 10);
		log.info(notificationEnabledMsg.getText());
		clickOn(ok, 10);
		if (visibilityOf(notificationToggleTrue, 10) && visibilityOf(notificationHistory, 10)
				&& visibilityOf(notificationHistoryDetails, 10) && visibilityOf(notificationLabel, 10)) {
			clickOn(notificationHistoryDetails, 10);
			visibilityOf(healthNotificationHeading, 10);
			clickOn(closeDialog, 10);
			if (visibilityOf(notificationToggleTrue, 10)) {
				log.info("Notification Toggle is in enable mode");
				clickOn(notificationToggleTrue, 10);
				visibilityOf(disableNotificationHeading, 10);
				clickOn(yes, 10);
				visibilityOf(success, 10);
				visibilityOf(notificationDisabledMsg, 10);
				log.info(notificationDisabledMsg.getText());
				clickOn(ok, 10);
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
			clickOn(closeDialog, 10);
			log.info("{} : Latest Status Details present in details dialog box", serviceName);
			return true;
		}
		clickOn(closeDialog, 10);
		return false;
	}

	private boolean verifyPlatformAuditEngineData(String serviceName, String ipAddress, String version) {
		String auditEnginePort = LoginAndSelectModule.testData.get("auditEnginePort");
		String auditEngineVersion = LoginAndSelectModule.testData.get("auditEngineVersion");
		if (ipAddress.contains(auditEnginePort) && version.contains(auditEngineVersion)) {
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
		if (ipAddress.contains(webhookSubscriberPort) && version.contains(webhookSubscriberVersion)) {
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
		if (ipAddress.contains(webhookEnginePort) && version.contains(webhookEngineVersion)) {
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
		if (ipAddress.contains(platformWorkflowPort) && version.contains(platformWorkflowVersion)) {
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
		if (ipAddress.contains(platformEnginePort) && version.contains(platformEngineVersion)) {
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
		if (ipAddress.contains(platformServicePort) && version.contains(platformServiceVersion)) {
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
		new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfAllElements(element));
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
		new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOf(element));
		return element.isDisplayed();
	}

	/**
	 * wait until the visibility of web element then click on the web element
	 * 
	 * @param element
	 * @param timeout
	 */
	public static void clickOn(WebElement element, int timeout) {
		new WebDriverWait(driver, timeout).until(ExpectedConditions.elementToBeClickable(element));
		element.click();
	}

}
