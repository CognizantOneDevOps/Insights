package com.cognizant.devops.platformservice.test.testngInitializer;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@WebAppConfiguration
public class TestngInitializerTest extends AbstractTestNGSpringContextTests{
	
	static Logger log = LogManager.getLogger(TestngInitializerTest.class);
	
	@BeforeTest
	public void testOnStartup() throws ServletException {
		ApplicationConfigCache.loadConfigCache();
		System.out.println("Testng initializer class to load Config Cache");
	}
}
