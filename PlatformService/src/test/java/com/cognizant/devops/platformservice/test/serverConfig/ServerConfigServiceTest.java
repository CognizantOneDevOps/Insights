/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.serverConfig;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.serverconfig.ServerConfigController;
import com.cognizant.devops.platformservice.rest.serverconfig.ServerConfigServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ServerConfigServiceTest extends ServerConfigTestData {

	private static final Logger log = LogManager.getLogger(ServerConfigServiceTest.class);

	ServerConfigServiceImpl serverConfigServiceImpl = new ServerConfigServiceImpl();
	ServerConfigController serverConfigController = new ServerConfigController();

	String host = null;
	Gson gson = new Gson();

	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
//			ApplicationConfigCache.loadConfigCache();
		} catch (Exception e) {
			log.error("message", e);
		}

	}

	@Test(priority = 1)
	public void testServerConfigStatus() throws InsightsCustomException {
		try {
			JsonObject serverConfigStatus = serverConfigController.getServerConfigStatus();
			Assert.assertNotNull(serverConfigStatus);
			boolean isServerConfigAvailable = Boolean.FALSE;
			if (serverConfigStatus.has("data")) {
				isServerConfigAvailable = serverConfigStatus.get("data").getAsJsonObject()
						.get("isServerConfigAvailable").getAsBoolean();
			}
			Assert.assertTrue(isServerConfigAvailable);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testServerConfigDetail() throws InsightsCustomException {
		try {
			JsonObject serverConfigStatus = serverConfigController.getServerConfigTemplate();
			Assert.assertNotNull(serverConfigStatus);
			boolean isServerConfigValidated = Boolean.FALSE;
			if(serverConfigStatus.has("data")) {
				String serverConfigJsonDecrypt = extractServerConfig(serverConfigStatus);
				ApplicationConfigProvider config = gson.fromJson(serverConfigJsonDecrypt,
						ApplicationConfigProvider.class);
				ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
				Validator validator = factory.getValidator();
				Set<ConstraintViolation<ApplicationConfigProvider>> violations = validator.validate(config);
				if (violations.isEmpty()) {
					isServerConfigValidated = Boolean.TRUE;
				}
			}
			Assert.assertTrue(isServerConfigValidated);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testsaveServerConfigDetail() throws InsightsCustomException {
		try {
			JsonObject serverConfigStatus = serverConfigController.getServerConfigTemplate();
			Assert.assertNotNull(serverConfigStatus);
			boolean isServerConfigValidated = Boolean.FALSE;
			if (serverConfigStatus.has("data")) {
				String serverConfigJsonDecrypt = extractServerConfig(serverConfigStatus);
				String serverConfigJsonEncrypt = encryptServerConfig(serverConfigJsonDecrypt);
				JsonObject serverConfigSaveStatus = serverConfigController
						.saveServerConfigDetail(serverConfigJsonEncrypt);
				if (serverConfigSaveStatus.has("data")) {
					String saveMessage = serverConfigSaveStatus.get("data").getAsJsonObject().get("message").toString();
					if (saveMessage.contains("successfully")) {
						isServerConfigValidated = Boolean.TRUE;
					}
				}
			}
			Assert.assertTrue(isServerConfigValidated);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@AfterClass
	public void cleanUp() throws InsightsCustomException {
		
		
	}
}
