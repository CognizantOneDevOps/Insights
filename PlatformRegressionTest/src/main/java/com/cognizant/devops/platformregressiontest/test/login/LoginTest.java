package com.cognizant.devops.platformregressiontest.test.login;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.common.CommonUtils;
import com.cognizant.devops.platformregressiontest.common.ConfigOptionsTest;

public class LoginTest {

	@BeforeMethod
	public Properties onInit() throws IOException {

		Properties CommonUtils = null;

		return CommonUtils;
	}

	@Test(priority = 1)
	public void login() throws Exception {

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.AUTO_DIR + File.separator
				+ ConfigOptionsTest.CHROME_DIR + File.separator + ConfigOptionsTest.DRIVER_FILE;
		System.setProperty("webdriver.chrome.driver", path);

		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.navigate().to(CommonUtils.getProperty("baseURI") + "/app");
		driver.findElement(By.xpath("//input[contains(@name,'username')]")).sendKeys("admin");
		driver.findElement(By.xpath("//input[contains(@autocomplete,'new-password')]")).sendKeys("admin");
		driver.findElement(By.xpath("//button[contains(@class,'sigBtn')]")).click();
		Thread.sleep(4000);
		driver.findElement(By.xpath("(//p[contains(@class,'line-child')])[4]")).click();
		Thread.sleep(2000);
		driver.findElement(By.xpath("(//p[contains(@class, 'mat-list-text')])[3]")).click();
		Assert.assertEquals(driver.getTitle(), "Insights");
	}

}
