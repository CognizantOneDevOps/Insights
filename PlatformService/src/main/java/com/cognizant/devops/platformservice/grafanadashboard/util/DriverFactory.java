package com.cognizant.devops.platformservice.grafanadashboard.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public class DriverFactory {


	private DriverFactory()
	{
	}
	private static DriverFactory instance = new DriverFactory();

	public static DriverFactory getInstance()
	{
		return instance;
	}

	ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>() // thread local driver object for webdriver
	{
		@Override
		protected WebDriver initialValue()
		{
			System.setProperty("webdriver.gecko.driver", ApplicationConfigProvider.getInstance().getDriverLocation());
			FirefoxBinary firefoxBinary = new FirefoxBinary();
			firefoxBinary.addCommandLineOptions("--headless");
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			firefoxOptions.setBinary(firefoxBinary);
			return new FirefoxDriver(firefoxOptions); // can be replaced with other browser drivers
		}
	};

	public WebDriver getDriver() // call this method to get the driver object and launch the browser
	{
		return driver.get();
	}

	public void removeDriver() // Quits the driver and closes the browser
	{
		driver.get().quit();
		driver.remove();
	}
}
