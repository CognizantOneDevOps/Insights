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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@Service("correlationBuilderService")
public class CorrelationBuilderServiceImpl implements CorrelationBuilderService {
	private static Logger log = LogManager.getLogger(CorrelationBuilderServiceImpl.class);

	@Override
	public Object getCorrelationJson() throws InsightsCustomException {
		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		List<CorrelationConfiguration> correlationList = null;
		try {
			correlationList = correlationConfigDAL.getAllCorrelations();
		} catch (Exception e) {
			log.error("Error getting all relationShips", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return correlationList;
	}

	@Override
	public JsonObject getNeo4jJson() throws InsightsCustomException {
		String agentPath = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + ConfigOptions.CONFIG_DIR;
		Path dir = Paths.get(agentPath);
		JsonObject config = new JsonObject();
		try {
			Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
					(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.NEO4J_TEMPLATE));
			FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile());
			JsonParser parser = new JsonParser();
			config = (JsonObject) parser.parse(reader);

		} catch (IOException | JsonSyntaxException | JsonIOException e) {
			log.error("Offline file reading issue", e.getMessage());
			throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
		}
		return config;
	}

	@Override
	public String saveConfig(String config) throws InsightsCustomException {

		List<CorrelationJson> correlations = loadCorrelations(config);

		List<CorrelationConfiguration> correlationConfigList = new ArrayList<CorrelationConfiguration>();
		for (CorrelationJson correlation : correlations) {

			CorrelationConfiguration correlationConfig = new CorrelationConfiguration();
			correlationConfig.setSourceToolName(correlation.getSource().getSourceToolName());
			correlationConfig.setSourceToolCategory(correlation.getSource().getSourceToolCategory());
			correlationConfig.setSourceLabelName(correlation.getSource().getSourceLabelName());
			correlationConfig.setSourceFields(String.join(",", correlation.getSource().getSourceFields()));
			correlationConfig.setDestinationToolName(correlation.getDestination().getDestinationToolName());
			correlationConfig.setDestinationToolCategory(correlation.getDestination().getDestinationToolCategory());
			correlationConfig.setDestinationLabelName(correlation.getDestination().getDestinationLabelName());
			correlationConfig
					.setDestinationFields(String.join(",", correlation.getDestination().getDestinationFields()));
			correlationConfig.setRelationName(correlation.getRelationName());
			correlationConfig.setPropertyList(String.join(",", correlation.getPropertyList()));
			correlationConfig.setEnableCorrelation(correlation.isEnableCorrelation());
			correlationConfigList.add(correlationConfig);
		}

		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		for (CorrelationConfiguration saveCorrelationJson : correlationConfigList) {
			correlationConfigDAL.saveCorrelationConfig(saveCorrelationJson);
		}
		return config;

	}

	@Override
	public String updateCorrelation(String configDetails) throws InsightsCustomException {

		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(configDetails);
		String relationName = json.get("relationName").getAsString();
		Boolean flag = json.get("correlationFlag").getAsBoolean();
		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		correlationConfigDAL.updateCorrelationConfig(relationName, flag);
		return configDetails;
	}

	@Override
	public String deleteCorrelation(String configDetails) throws InsightsCustomException {

		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(configDetails);
		String relationName = json.get("relationName").getAsString();
		Boolean flag = json.get("correlationFlag").getAsBoolean();
		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		correlationConfigDAL.deleteCorrelationConfig(relationName, flag);

		return configDetails;
	}

	private List<CorrelationJson> loadCorrelations(String config) {
		JsonArray correlationJson = new JsonArray();
		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(config);
		correlationJson = json.get("data").getAsJsonArray();
		CorrelationJson[] correlationArray = new Gson().fromJson(correlationJson, CorrelationJson[].class);
		List<CorrelationJson> correlations = Arrays.asList(correlationArray);
		return correlations;
	}

}