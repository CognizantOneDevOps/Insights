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
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("correlationBuilderService")
public class CorrelationBuilderServiceImpl implements CorrelationBuilderService {
	private static Logger log = LogManager.getLogger(AgentManagementServiceImpl.class);
	
	@Override
	public Object getCorrelationJson() throws InsightsCustomException {
		// TODO Auto-generated method stub
		//Path dir = Paths.get(filePath);
		String agentPath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		Path dir = Paths.get(agentPath);
		Object config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.CORRELATION_TEMPLATE));
				FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {

			JsonParser parser = new JsonParser();
			Object obj = parser.parse(reader);
			//config = ((JsonArray) obj).toString();
			config=obj;
		} catch (IOException e) {
			log.error("Offline file reading issue", e);
			throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
		}
		log.error(agentPath);
		log.error("config"+config); 
		return config;
	} 
	
	@Override
	public Object getNeo4jJson() throws InsightsCustomException {
		// TODO Auto-generated method stub
		//Path dir = Paths.get(filePath);
		String agentPath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		Path dir = Paths.get(agentPath);
		Object config = null;
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.NEO4J_TEMPLATE));
				FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {

			JsonParser parser = new JsonParser();
			Object obj = parser.parse(reader);
			//config = ((JsonArray) obj).toString();
			config=obj;
		} catch (IOException e) {
			log.error("Offline file reading issue", e);
			throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
		}
		log.error(agentPath);
		log.error("config"+config); 
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
		if(json.has("data")) {
			correlationJson = json.get("data").getAsJsonArray();
		}
		log.debug("saveconfig"+config);
		log.debug("correlationJson "+correlationJson);
		Path dir = Paths.get(configFilePath);
		Path source = Paths.get(System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR+File.separator+ConfigOptions.CORRELATION_TEMPLATE);
	    Path target = Paths.get(System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR+File.separator+ConfigOptions.CORRELATION);
	    try {
	        Files.copy(source, target);
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
		try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.CORRELATION_TEMPLATE))) {

			configFile = paths.limit(1).findFirst().get().toFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log.debug("  arg0 " + configFile);
		try (FileWriter file = new FileWriter(configFile)) {
			file.write(correlationJson.toString());
			file.flush();
		} catch (IOException e) {
			log.error(e);
		} 
		return "succcess";

	}

}
