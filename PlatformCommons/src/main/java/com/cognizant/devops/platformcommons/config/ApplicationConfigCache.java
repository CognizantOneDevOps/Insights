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
package com.cognizant.devops.platformcommons.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.util.Strings;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.LogLevelConstants;
import com.cognizant.devops.platformcommons.dal.vault.VaultHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

public class ApplicationConfigCache {
	static Logger log = LogManager.getLogger(ApplicationConfigCache.class.getName());

	private ApplicationConfigCache() {

	}

	/**
	 * update config file
	 * 
	 * @return
	 */
	public static boolean updateConfigCahe() {
		File configFile = new File(ConfigOptions.CONFIG_FILE_RESOLVED_PATH);
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			log.warn("iSight config folder and file created at : {} ", configFile.getAbsolutePath());
		} else {
			log.warn("iSight config file found at : {} ", configFile.getAbsolutePath());
		}
		try (JsonWriter writer = new JsonWriter(new FileWriter(configFile))) {
			writer.setIndent("  ");
			new GsonBuilder().disableHtmlEscaping().create().toJson(ApplicationConfigProvider.getInstance(),
					ApplicationConfigProvider.class, writer);
		} catch (IOException e) {
			log.error(e);
		}
		return false;
	}

	/**
	 * Initialize server config
	 * 
	 * @return true if all property of ApplicationConfigProvider initialize
	 *         correctly
	 * @throws Exception
	 */
	public static boolean loadConfigCache() throws InsightsCustomException {

		JsonObject serverConfigJsonfromStorage = new JsonObject();
		JsonObject serverConfigJsonfromVault = new JsonObject();
		Gson gson = new Gson();
		try {
			VaultHandler vaultHandler = new VaultHandler();
			serverConfigJsonfromStorage = ApplicationConfigCache.loadServerConfigFromFile();
			ApplicationConfigProvider config = gson.fromJson(serverConfigJsonfromStorage,
					ApplicationConfigProvider.class);
			if (config.getVault().isVaultEnable()) {
				serverConfigJsonfromVault = vaultHandler.fetchServerConfigFromVault("local",
						config.getVault().getVaultEndPoint(), config.getVault().getSecretEngine(),
						config.getVault().getVaultToken());
			}
			if (!serverConfigJsonfromVault.entrySet().isEmpty()) {
				ApplicationConfigCache.initializeUsingJson(serverConfigJsonfromVault);
			} else {
				ApplicationConfigCache.initializeUsingJson(serverConfigJsonfromStorage);
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			throw e;
		} catch (Exception e) {
			log.error("Execption while loding config ", e);
			throw e;
		}
		return true;
	}

	/**
	 * Initialize server config, loadInitialConfigCache
	 * this method will not throw exception allow to load default server config
	 * 
	 * @return true if all property of ApplicationConfigProvider initialize
	 *         correctly
	 * @throws Exception
	 */
	public static boolean loadInitialConfigCache() throws InsightsCustomException {

		JsonObject serverConfigJsonfromStorage = new JsonObject();
		JsonObject serverConfigJsonfromVault = new JsonObject();
		Gson gson = new Gson();
		try {
			VaultHandler vaultHandler = new VaultHandler();
			serverConfigJsonfromStorage = ApplicationConfigCache.loadServerConfigFromFile();
			ApplicationConfigProvider config = gson.fromJson(serverConfigJsonfromStorage,
					ApplicationConfigProvider.class);
			if (config.getVault().isVaultEnable()) {
				try {
					serverConfigJsonfromVault = vaultHandler.fetchServerConfigFromVault("local",
							config.getVault().getVaultEndPoint(), config.getVault().getSecretEngine(),
							config.getVault().getVaultToken());
				} catch (Exception e) {
					log.error(e);
				}
			}
			if (!serverConfigJsonfromVault.entrySet().isEmpty()) {
				ApplicationConfigCache.initializeUsingJson(serverConfigJsonfromVault);
			} else {
				ApplicationConfigCache.initializeUsingJson(serverConfigJsonfromStorage);
			}
		} catch (Exception e) {
			log.error("Execption while loding config ", e);
			throw e;
		}
		return true;
	}

	public static JsonObject loadServerConfigFromFile() {
		JsonObject serverConfig = new JsonObject();
		if (System.getenv().get(ConfigOptions.INSIGHTS_HOME) == null) {
			log.error("INSIGHTS_HOME environment variable is not set.");
		}
		File configFile = new File(ConfigOptions.CONFIG_FILE_RESOLVED_PATH);
		StringBuilder json = new StringBuilder();

		if (configFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(configFile), 1024)) {
				String line;
				while ((line = reader.readLine()) != null) {
					json.append(line);
				}
				String payload = json.toString().replaceAll("\\t", "");

				serverConfig = new JsonParser().parse(payload).getAsJsonObject();
			} catch (FileNotFoundException e) {
				log.error("Config file not found", e);
			} catch (Exception e) {
				log.error("Unable to read the file", e);
			}
		} else {
			log.error("Unable to load server config from the file");
		}

		return serverConfig;
	}

	public static boolean initConfigCacheFromResources() {
		URL resource = ApplicationConfigCache.class.getClassLoader().getResource(ConfigOptions.CONFIG_FILE);
		File configFile = new File(resource.getFile());
		StringBuilder json = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile), 1024)) {
			String line;
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}
		} catch (Exception e) {
			log.error(e);
		}
		return initialize(json.toString());
	}

	/**
	 * Initialize server config from string object
	 * 
	 * @param json
	 * @return true if all property of ApplicationConfigProvider initialize
	 *         correctly/successfully
	 */
	public static boolean initialize(String json) {
		try {
			Gson gson = new Gson();
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(json);
			if (jsonElement != null && jsonElement.isJsonObject()) {
				ApplicationConfigProvider config = gson.fromJson(jsonElement.getAsJsonObject(),
						ApplicationConfigProvider.class);
				ApplicationConfigProvider.updateConfig(config);
			}
		} catch (JsonIOException e) {
			log.error(" {} file is not found  ", ConfigOptions.CONFIG_FILE, e);
		} catch (Exception e) {
			log.error(" {} file is has Execption  ", ConfigOptions.CONFIG_FILE, e);
		}
		return false;
	}

	/**
	 * Initialize ApplicationConfigProvider from json object
	 * 
	 * @param json
	 * @return true if all ApplicationConfigProvider property initialize correctly
	 */
	public static boolean initializeUsingJson(JsonObject json) {
		try {
			Gson gson = new Gson();
			if (json != null) {
				ApplicationConfigProvider config = gson.fromJson(json, ApplicationConfigProvider.class);
				ApplicationConfigProvider.updateConfig(config);
			}
		} catch (JsonIOException e) {
			log.error(" {} file is not found  ", ConfigOptions.CONFIG_FILE, e);
		} catch (Exception e) {
			log.error("  issue in file {} ", ConfigOptions.CONFIG_FILE, e);
		}
		return false;
	}

	/**
	 * Read server config from file system
	 * 
	 * @return
	 */
	public static String readConfigFile() {
		File configFile = new File(ConfigOptions.CONFIG_FILE_RESOLVED_PATH);
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile), 1024)) {
			String s = null;
			StringBuilder sb = new StringBuilder();
			while ((s = reader.readLine()) != null) {
				sb.append(s);
			}
			return sb.toString();
		} catch (Exception e) {
			log.error(e);
		}
		return "";
	}

	/**
	 * Save servre config file on file system and Initialize
	 * ApplicationConfigProvider object
	 * 
	 * @param serverConfigFile
	 * @return true if ApplicationConfigProvider object initialize successfully
	 * @throws InsightsCustomException
	 */
	public static boolean saveConfigFile(JsonObject serverConfigFile) throws InsightsCustomException {
		File configFile = new File(ConfigOptions.CONFIG_FILE_RESOLVED_PATH);
		try (FileWriter file = new FileWriter(configFile)) {
			file.write(serverConfigFile.toString());
			file.flush();
		} catch (Exception e) {
			log.error("Error writing modified json file", e);
			throw new InsightsCustomException("Error writing modified json file " + e.getMessage());
		}
		return initializeUsingJson(serverConfigFile);
	}

	/**
	 * Validate server config detail file
	 * 
	 * @return true id validation successfully completed
	 */
	public static boolean validateServerConfig() {
		boolean isValidate = Boolean.FALSE;
		try {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			Validator validator = factory.getValidator();
			Set<ConstraintViolation<ApplicationConfigProvider>> violations = validator
					.validate(ApplicationConfigProvider.getInstance());
			violations.forEach(
					g -> log.error("Error while validating server config json === {} ===== {}  ", g.getMessage(), g));
			if (violations.isEmpty()) {
				isValidate = Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error("  issue in file {} ", ConfigOptions.CONFIG_FILE, e);
		}
		return isValidate;
	}

	public static void updateLogLevel(String serviceName) {
		try {
			String logLevel = "DEBUG";
			boolean updateLevelTransitiveDependency = ApplicationConfigProvider.getInstance().getApplicationLogLevel()
					.isUpdateLevelTransitiveDependency();
			Map<String, Level> removeLoggerString = new HashMap<>();

			LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);

			if (ApplicationConfigProvider.getInstance().getApplicationLogLevel().getServiceLogLevel()
					.has(serviceName)) {
				logLevel = ApplicationConfigProvider.getInstance().getApplicationLogLevel().getServiceLogLevel()
						.get(serviceName).getAsString();
			}

			RollingFileAppender appenderServiceBase = context.getConfiguration().getAppender(serviceName);
			ConsoleAppender appenderServiceConsole = context.getConfiguration()
					.getAppender(LogLevelConstants.CONSOLE_APPENDER);

			if (appenderServiceBase == null) {
				throw new InsightsCustomException(
						"No Logger Appender with name " + serviceName + " defined in log4j.xml ");
			}

			log.debug(" logLevel : {} ====== appenderUpdated Name : {}  ==== appender file Name : {}  ", logLevel,
					appenderServiceBase.getName(), appenderServiceBase.getFileName());

			context.getConfiguration().getAppenders().forEach((key, appender) -> {
				log.debug("file Appender information {} ", key);
			});

			/*In LoggerConfig and in Configuration we are not allowed to update any object property like appenderRef 
			 * so best way to delete that LoggerConfig and add it again 
			*/

			Map<String, LoggerConfig> allloggerList = context.getConfiguration().getLoggers();
			for (Map.Entry<String, LoggerConfig> entry : allloggerList.entrySet()) {
					context.getConfiguration().removeLogger(entry.getValue().getName());
					removeLoggerString.put(entry.getValue().getName(), entry.getValue().getLevel());
					context.updateLoggers();
			}


			/* Add previously deleted LoggerConfig with modified AppenderRef
			*/
			for (Entry<String, Level> addNewLooger : removeLoggerString.entrySet()) {
				Level addedLoggerLevel = addNewLooger.getValue();
				if (LogLevelConstants.UPDATED_LOGGER_LIST.contains(addNewLooger.getKey())
						|| updateLevelTransitiveDependency) {
					addedLoggerLevel = Level.valueOf(logLevel);
				}
				log.error("Updated logger information {} {} ", addNewLooger.getKey(), addedLoggerLevel);
				AppenderRef newAppenderRef = AppenderRef.createAppenderRef(serviceName, addedLoggerLevel, null);
				AppenderRef ref2 = AppenderRef.createAppenderRef(LogLevelConstants.CONSOLE_APPENDER,
						addedLoggerLevel, null);
				AppenderRef[] refs = new AppenderRef[] { newAppenderRef, ref2 };

				LoggerConfig loggerConfig = LoggerConfig.createLogger(false, addedLoggerLevel,
						addNewLooger.getKey(), "true", refs, null, context.getConfiguration(), null);
				loggerConfig.addAppender(appenderServiceBase, addedLoggerLevel, null);
				loggerConfig.addAppender(appenderServiceConsole, addedLoggerLevel, null);

				context.getConfiguration().addLogger(addNewLooger.getKey(), loggerConfig);
				context.updateLoggers();
			}

			context.getConfiguration().getLoggers().forEach((key, loggerConfig) -> {
				log.error("logger level information Updated {} {} {} ", key, loggerConfig.getName(),
						loggerConfig.getLevel());
				loggerConfig.getAppenderRefs().forEach((appenderRef) -> {
					log.error("   AppenderRef for log level {} is {} ", loggerConfig.getName(), appenderRef.getRef());
				});
			});
		} catch (Exception e) {
			log.error(e);
		}
	}
}
