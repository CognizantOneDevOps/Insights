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
package com.cognizant.devops.platformservice.correlationbuilder.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("correlationBuilderService")
public class CorrelationBuilderServiceImpl implements CorrelationBuilderService {
	private static Logger log = LogManager.getLogger(CorrelationBuilderServiceImpl.class);

	@Override
	public Object getCorrelationJson() throws InsightsCustomException {

		String agentPath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		Path dir = Paths.get(agentPath);
		Object config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.CORRELATION_TEMPLATE));
				FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(reader);
			config = obj;
		} catch (IOException e) {
			log.error("Offline file reading issue", e);
			throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
		}
		return config;
	}

	@Override
	public Object getNeo4jJson() throws InsightsCustomException {
		String agentPath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		Path dir = Paths.get(agentPath);
		Object config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.NEO4J_TEMPLATE));
				FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(reader);
			config = obj;
		} catch (IOException e) {
			log.error("Offline file reading issue", e);
			throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
		}
		return config;
	}

	@Override
	public String saveConfig(String config) throws InsightsCustomException {
		String configFilePath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		File configFile = null;
		JsonArray correlationJson = new JsonArray();
		// Writing json to file
		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(config);
		if (json.has("data")) {
			correlationJson = json.get("data").getAsJsonArray();
		Path dir = Paths.get(configFilePath);
		Path source = Paths.get(System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR
				+ File.separator + ConfigOptions.CORRELATION_TEMPLATE);
		Path target = Paths.get(System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR
				+ File.separator + ConfigOptions.CORRELATION);
		
		try {
			if (source.toFile().exists()) {
				Files.copy(source, target);
			} else {
				Files.createFile(source);
			}
		} catch (IOException e1) {
			log.error("Fail to Copy or create file. "+e1.getMessage().toString());
		}

		try {

			Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE, (path, attrs) -> attrs.isRegularFile()
					&& path.toString().endsWith(ConfigOptions.CORRELATION_TEMPLATE));
			configFile = paths.limit(1).findFirst().get().toFile();
		 FileWriter file = new FileWriter(configFile);
			file.write(correlationJson.toString());
			file.flush();
		} catch (Exception e) {
			log.error(e);
		}

		return PlatformServiceConstants.SUCCESS;
		}
		else
		{
			return PlatformServiceConstants.FAILURE;
		}
	}

}
