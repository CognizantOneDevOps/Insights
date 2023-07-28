/*******************************************************************************
 *  * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.testngInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.CommonDALUtils;
import com.google.gson.JsonObject;

@Test
public class TestngSQSInitializerTest {

	static Logger log = LogManager.getLogger(TestngSQSInitializerTest.class);

	public static String TESTNG_TESTDATA = "TestNG_TestData";
	public static String TESTNG_PLATFORMENGINE = "PlatformEngine";
	public static String TESTNG_PLATFORMSERVICE = "PlatformService";
	public static String H2_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS testdb\\;SET SCHEMA testdb";
	public static String H2_DRIVER = "org.h2.Driver";
	public static String H2_DIALECT = "org.hibernate.dialect.H2Dialect";

	@BeforeSuite
	public void testOnStartup() throws Exception {
		try {
			/*
			 * To update the provider name in server-config.json file
			 */
			JsonObject serverConfigJsonfromStorage = ApplicationConfigCache.loadServerConfigFromFile();
			serverConfigJsonfromStorage.get("messageQueue").getAsJsonObject().remove("providerName");
			serverConfigJsonfromStorage.get("messageQueue").getAsJsonObject().addProperty("providerName", "AWSSQS");
			ApplicationConfigCache.saveConfigFile(serverConfigJsonfromStorage);
			ApplicationConfigCache.initializeUsingJson(serverConfigJsonfromStorage);
			log.debug("Server Config Stored Successfully");
			Thread.sleep(2000);
			ApplicationConfigCache.loadConfigCache();
			CommonDALUtils.changeAnnotationValue();
			loadDBDetails();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void loadDBDetails() throws InsightsCustomException {
		try {
			ApplicationConfigProvider.getInstance().getPostgre().setInsightsDBUrl(H2_DB_URL);
			ApplicationConfigProvider.getInstance().getPostgre().setDriver(H2_DRIVER);
			ApplicationConfigProvider.getInstance().getPostgre().setDialect(H2_DIALECT);
			ApplicationConfigProvider.updateConfig(ApplicationConfigProvider.getInstance());
		} catch (Exception ex) {
			throw new InsightsCustomException("Issue in loading db");
		}
	}

	@AfterSuite
	public void tearDown() throws Exception {
		try {
			/*
			 * To revert the provider name in server-config.json file
			 */
			JsonObject serverConfigJsonfromStorage = ApplicationConfigCache.loadServerConfigFromFile();
			serverConfigJsonfromStorage.get("messageQueue").getAsJsonObject().remove("providerName");
			serverConfigJsonfromStorage.get("messageQueue").getAsJsonObject().addProperty("providerName", "RabbitMQ");
			ApplicationConfigCache.saveConfigFile(serverConfigJsonfromStorage);
			ApplicationConfigCache.initializeUsingJson(serverConfigJsonfromStorage);
			log.debug("Server Config Successfully Reverted");
		} catch (InsightsCustomException e) {
			log.error(e);
			throw new InsightsCustomException("Error while reverting server config file " + e.getMessage());
		}
	}

}