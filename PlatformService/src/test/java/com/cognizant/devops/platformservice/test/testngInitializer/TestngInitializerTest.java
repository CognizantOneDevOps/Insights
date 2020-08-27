/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.testngInitializer;

import javax.annotation.Resource;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@WebAppConfiguration
public class TestngInitializerTest extends AbstractTestNGSpringContextTests{
	
	static Logger log = LogManager.getLogger(TestngInitializerTest.class);
	
	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Resource
	private FilterChainProxy springSecurityFilterChain;

	static {
		ApplicationConfigCache.loadConfigCache();
		log.debug("Testng initializer class to load Config Cache .... static");

	}

	@BeforeSuite
	public void testOnStartup() throws ServletException {
		ApplicationConfigCache.loadConfigCache();
		log.debug("Testng initializer class to load Config Cache");
	}
}
