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
package com.cognizant.devops.platformregressiontest.test.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.SkipException;
import org.testng.annotations.AfterSuite;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;

public class LoginAndSelectModule {

	public static WebDriver driver;
	public static Map<String, String> testData = new HashMap<>();

	public static void initialization() {
		if (driver == null) {
			String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator + File.separator
					+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.CHROME_DIR + File.separator
					+ ConfigOptionsTest.DRIVER_FILE;
			ChromeOptions options = new ChromeOptions();
			options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
			System.setProperty("webdriver.chrome.driver", path);
			driver = new ChromeDriver(options);
			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
			driver.navigate().to(CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("baseURL"));
		}
	}

	/* Selects module above Configuration on UI */
	public static void selectMenuOption(String optionName) {
		List<WebElement> menuList = driver.findElements(By.xpath("//p[contains(@class,'line-child')]"));
		for (WebElement reuqiredOption : menuList) {
			if (reuqiredOption.getText().equals(optionName)) {
				reuqiredOption.click();
				break;

			}

		}
	}

	/* Selects modules under Configuration on UI */
	public static void selectModuleOnClickingConfig(String moduleName) throws InterruptedException {
		Thread.sleep(1000);
		driver.findElement(By.xpath("//a[@title='Configuration']")).click();
		List<WebElement> menuList = driver.findElements(By.xpath("//p[contains(@class,'line-child')]"));
		for (WebElement reuqiredOption : menuList) {
			if (reuqiredOption.getText().equals(moduleName)) {
				reuqiredOption.click();
				break;
			}

		}
	}

	public static void selectModuleOnClickingDashboardgroups(String moduleName) throws InterruptedException {
		Thread.sleep(1000);
		driver.findElement(By.xpath("//p[contains(text(),'Dashboard Groups ')]")).click();
		List<WebElement> menuList = driver.findElements(By.xpath("//p[contains(@class,'line-child')]"));
		for (WebElement reuqiredOption : menuList) {
			if (reuqiredOption.getText().equals(moduleName)) {
				reuqiredOption.click();
				break;
			}

		}
	}

	/* Selects modules under Configuration on UI */
	public static void selectModuleUnderConfiguration(String moduleName) {
		List<WebElement> menuList = driver.findElements(By.xpath("//p[contains(@class,'line-child')]"));
		for (WebElement reuqiredOption : menuList) {
			if (reuqiredOption.getText().equals(moduleName)) {
				reuqiredOption.click();
				break;
			}
		}
	}

	public static void selectModuleKPIConfiguration(String moduleName) {
		driver.findElement(By.xpath("//p[text()='Report Configuration ']")).click();
		List<WebElement> menuList = driver.findElements(By.xpath("//p[contains(@class,'line-child')]"));
		for (WebElement reuqiredOption : menuList) {
			if (reuqiredOption.getText().equals(moduleName)) {
				reuqiredOption.click();
				break;
			}
		}
	}

	public static String getData(String jsonFile) {
		JsonElement jsonData;
		try {
			String path = new File(System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator + ConfigOptionsTest.AUTO_DIR
				+ File.separator + jsonFile).getCanonicalPath();
			jsonData = JsonUtils.parseReader(new FileReader(path));
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			throw new SkipException("skipped this test case as resources not found.");
		}
		testData = new Gson().fromJson(jsonData, Map.class);
		return jsonFile;
	}

	@AfterSuite
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}
}
