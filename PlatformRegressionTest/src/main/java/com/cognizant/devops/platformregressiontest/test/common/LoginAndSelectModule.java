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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.SkipException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class LoginAndSelectModule {

	public static WebDriver driver;
	public static Map<String, String> testData = new HashMap<>();

	public static void initialization() {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator + File.separator
				+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.CHROME_DIR + File.separator
				+ ConfigOptionsTest.DRIVER_FILE;
		System.setProperty("webdriver.chrome.driver", path);
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.navigate().to(CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("baseURL"));
		driver.findElement(By.xpath("//input[contains(@name,'username')]"))
				.sendKeys(CommonUtils.getProperty("username"));
		driver.findElement(By.xpath("//input[contains(@autocomplete,'new-password')]"))
				.sendKeys(CommonUtils.getProperty("password"));
		driver.findElement(By.xpath("//button[contains(@class,'sigBtn')]")).click();

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

	public static void selectModuleUnderConfiguration(String moduleName) {
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.findElement(By.xpath("//a[@title='Configuration']")).click();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		List<WebElement> menuList = driver.findElements(By.xpath("//p[contains(@class,'line-child')]"));
		for (WebElement reuqiredOption : menuList) {
			if (reuqiredOption.getText().equals(moduleName)) {
				reuqiredOption.click();
				break;
			}

		}
	}

	public static void selectModuleUnderReportConfiguration(String moduleName) {
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.findElement(By.xpath("//a[@title='Configuration']")).click();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.findElement(By.xpath("//p[text()='Report Configuration ']")).click();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		List<WebElement> menuList = driver.findElements(By.xpath("//p[contains(@class,'line-child')]"));
		for (WebElement reuqiredOption : menuList) {
			if (reuqiredOption.getText().equals(moduleName)) {
				reuqiredOption.click();
				break;
			}

		}
	}

	public static String getData(String jsonFile) {
		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator + ConfigOptionsTest.AUTO_DIR
				+ File.separator +jsonFile;
		JsonElement jsonData;
		try {
			jsonData = new JsonParser().parse(new FileReader(path));
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			throw new SkipException("skipped this test case as json file is not found.");
		}
		testData = new Gson().fromJson(jsonData, Map.class);
		return jsonFile;
	}

}
